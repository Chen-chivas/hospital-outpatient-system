package com.hospital.outpatient.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.outpatient.dto.RegistrationOrderDTO;
import com.hospital.outpatient.entity.*;
import com.hospital.outpatient.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class RegistrationService {

    @Autowired
    private RegistrationOrderMapper orderMapper;

    @Autowired
    private NumberSourceService numberSourceService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysDepartmentMapper departmentMapper;

    @Autowired
    private NumberSourceMapper numberSourceMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private DoctorInfoMapper doctorInfoMapper;

    @Autowired
    private ScheduleService scheduleService;

    // 生成唯一流水号：REG + 年月日时分秒 + 4位随机数
    private String generateSerialNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "REG" + datePart + randomPart;
    }

    // 创建挂号订单
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(RegistrationOrder order) {
        try {
            // 1. 锁定号源
            boolean lockSuccess = numberSourceService.lockNumberSource(order.getSourceId());
            if (!lockSuccess) {
                return "号源已被抢完，请选择其他时段";
            }

            // 从排班里获取就诊日期并赋值给订单
            Schedule schedule = scheduleService.getById(order.getScheduleId());
            if (schedule == null) {
                numberSourceService.releaseNumberSource(order.getSourceId());
                return "排班信息不存在";
            }
            order.setVisitDate(schedule.getScheduleDate());

            // 2. 创建待支付订单
            order.setSerialNo(generateSerialNo());
            order.setOrderTime(LocalDateTime.now());
            order.setLockExpireTime(LocalDateTime.now().plusMinutes(30)); // 锁定30分钟
            order.setOrderStatus("PENDING"); // 待支付状态
            order.setCreateChannel("WEB");   // 线上渠道

            orderMapper.insert(order);
            log.info("创建订单成功，流水号：{}，患者ID：{}", order.getSerialNo(), order.getPatientId());

            return "success";
        } catch (Exception e) {
            log.error("创建订单失败", e);
            try {
                numberSourceService.releaseNumberSource(order.getSourceId());
            } catch (Exception ex) {
                log.error("释放号源失败", ex);
            }
            return "系统异常，请稍后重试";
        }
    }

    // 支付成功
    @Transactional(rollbackFor = Exception.class)
    public boolean paySuccess(Long orderId) {
        RegistrationOrder order = orderMapper.selectById(orderId);
        if (order == null || !"PENDING".equals(order.getOrderStatus())) {
            return false;
        }
        // 检查是否超时
        if (order.getLockExpireTime() != null &&
                order.getLockExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        order.setPayTime(LocalDateTime.now());
        order.setPayMethod("ONLINE");
        order.setOrderStatus("PAID");
        orderMapper.updateById(order);
        log.info("订单{}支付成功", order.getSerialNo());
        return true;
    }

    // 取消待支付订单
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelPendingOrder(Long orderId) {
        RegistrationOrder order = orderMapper.selectById(orderId);
        if (order == null || !"PENDING".equals(order.getOrderStatus())) {
            return false;
        }
        order.setOrderStatus("CANCELLED");
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason("用户主动取消订单");
        orderMapper.updateById(order);
        numberSourceService.releaseNumberSource(order.getSourceId());
        log.info("用户主动取消订单{}，号源{}已释放", order.getSerialNo(), order.getSourceId());
        return true;
    }

    // 退号（已支付订单）
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId, String reason) {
        RegistrationOrder order = orderMapper.selectById(orderId);
        if (order == null || !"PAID".equals(order.getOrderStatus())) {
            return false;
        }
        order.setOrderStatus("CANCELLED");
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        orderMapper.updateById(order);
        numberSourceService.releaseNumberSource(order.getSourceId());
        log.info("订单{}退号成功，号源{}已释放", order.getSerialNo(), order.getSourceId());
        return true;
    }

    // 查询患者所有订单（包含医生、科室、就诊时间等完整信息）
    public List<RegistrationOrderDTO> getByPatientIdWithDetails(Long patientId) {
        LambdaQueryWrapper<RegistrationOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegistrationOrder::getPatientId, patientId)
                .orderByDesc(RegistrationOrder::getOrderTime);
        List<RegistrationOrder> orders = orderMapper.selectList(wrapper);

        List<RegistrationOrderDTO> dtoList = new ArrayList<>();
        for (RegistrationOrder order : orders) {
            RegistrationOrderDTO dto = convertToDTO(order);
            dtoList.add(dto);
        }
        return dtoList;
    }

    // 单个订单转DTO
    public RegistrationOrderDTO getOrderDetail(Long orderId) {
        RegistrationOrder order = orderMapper.selectById(orderId);
        if (order == null) return null;
        return convertToDTO(order);
    }

    // 核心转换方法
    private RegistrationOrderDTO convertToDTO(RegistrationOrder order) {
        RegistrationOrderDTO dto = new RegistrationOrderDTO();
        // 复制基础字段
        dto.setOrderId(order.getOrderId());
        dto.setSerialNo(order.getSerialNo());
        dto.setOrderTime(order.getOrderTime());
        dto.setLockExpireTime(order.getLockExpireTime());
        dto.setPayTime(order.getPayTime());
        dto.setCancelTime(order.getCancelTime());
        dto.setPayMethod(order.getPayMethod());
        dto.setPayAmount(order.getPayAmount());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setCancelReason(order.getCancelReason());
        dto.setCreateChannel(order.getCreateChannel());
        dto.setVisitDate(order.getVisitDate());

        // 查询医生姓名和职称
        SysUser doctor = sysUserMapper.selectById(order.getDoctorId());
        if (doctor != null) {
            dto.setDoctorName(doctor.getRealName());
        } else {
            dto.setDoctorName("未知医生");
        }

        DoctorInfo doctorInfo = doctorInfoMapper.selectById(order.getDoctorId());
        if (doctorInfo != null) {
            dto.setDoctorTitle(doctorInfo.getTitle());
        } else {
            dto.setDoctorTitle("");
        }

        // 查询科室名称
        SysDepartment dept = departmentMapper.selectById(order.getDeptId());
        if (dept != null) {
            dto.setDeptName(dept.getDeptName());
        } else {
            dto.setDeptName("未知科室");
        }

        // 查询患者姓名
        SysUser patient = sysUserMapper.selectById(order.getPatientId());
        if (patient != null) {
            dto.setPatientName(patient.getRealName());
        } else {
            dto.setPatientName("未知患者");
        }

        // 查询就诊时间和时段
        NumberSource numberSource = numberSourceMapper.selectById(order.getSourceId());
        if (numberSource != null) {
            dto.setSlotStartTime(numberSource.getSlotStartTime());
            dto.setSlotEndTime(numberSource.getSlotEndTime());

            Schedule schedule = scheduleMapper.selectById(numberSource.getScheduleId());
            if (schedule != null) {
                LocalDateTime visitTime = LocalDateTime.of(
                        schedule.getScheduleDate(),
                        numberSource.getSlotStartTime()
                );
                dto.setVisitTime(visitTime);
            }
        }

        // 计算待支付剩余秒数
        if ("PENDING".equals(order.getOrderStatus()) && order.getLockExpireTime() != null) {
            long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), order.getLockExpireTime());
            dto.setRemainingSeconds(Math.max(0, seconds));
        }

        return dto;
    }

    // 处理超时未支付订单（每分钟执行一次）
    @Transactional(rollbackFor = Exception.class)
    public void processTimeoutOrders() {
        log.debug("开始执行超时订单检查任务，当前时间：{}", LocalDateTime.now());
        LambdaQueryWrapper<RegistrationOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegistrationOrder::getOrderStatus, "PENDING")
                .lt(RegistrationOrder::getLockExpireTime, LocalDateTime.now());
        List<RegistrationOrder> timeoutOrders = orderMapper.selectList(wrapper);

        if (timeoutOrders.isEmpty()) {
            log.debug("没有发现超时未支付订单");
            return;
        }

        log.info("发现{}个超时未支付订单，开始自动取消", timeoutOrders.size());
        for (RegistrationOrder order : timeoutOrders) {
            try {
                order.setOrderStatus("CANCELLED");
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason("订单超时未支付，系统自动取消");
                orderMapper.updateById(order);
                numberSourceService.releaseNumberSource(order.getSourceId());
                log.info("订单{}已自动取消，号源{}已释放", order.getSerialNo(), order.getSourceId());
            } catch (Exception e) {
                log.error("处理超时订单{}失败", order.getSerialNo(), e);
            }
        }
    }
}
