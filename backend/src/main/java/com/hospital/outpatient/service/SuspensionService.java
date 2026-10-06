package com.hospital.outpatient.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.outpatient.entity.*;
import com.hospital.outpatient.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate; // 新增：缺失的日期类导入
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class SuspensionService {

    @Autowired
    private SuspensionApplicationMapper applicationMapper;
    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private NumberSourceMapper numberSourceMapper;
    @Autowired
    private RegistrationOrderMapper orderMapper;
    @Autowired
    private NumberSourceService numberSourceService;

    // 医生提交停诊申请
    public boolean submitApplication(Long doctorId, LocalDate suspensionDate, String reason) {
        // 检查是否已经提交过相同日期的申请
        LambdaQueryWrapper<SuspensionApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SuspensionApplication::getDoctorId, doctorId)
                .eq(SuspensionApplication::getSuspensionDate, suspensionDate)
                .in(SuspensionApplication::getStatus, "PENDING", "APPROVED");
        Long count = applicationMapper.selectCount(wrapper);
        if (count > 0) {
            return false; // 已经有申请
        }
        SuspensionApplication application = new SuspensionApplication();
        application.setDoctorId(doctorId);
        application.setSuspensionDate(suspensionDate);
        application.setReason(reason);
        application.setStatus("PENDING");
        application.setApplyTime(LocalDateTime.now());
        applicationMapper.insert(application);
        log.info("医生{}提交了{}的停诊申请", doctorId, suspensionDate);
        return true;
    }

    // 获取医生的所有申请
    public List<SuspensionApplication> getByDoctorId(Long doctorId) {
        LambdaQueryWrapper<SuspensionApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SuspensionApplication::getDoctorId, doctorId)
                .orderByDesc(SuspensionApplication::getApplyTime);
        return applicationMapper.selectList(wrapper);
    }

    // 获取所有待审核的申请
    public List<SuspensionApplication> getPendingApplications() {
        LambdaQueryWrapper<SuspensionApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SuspensionApplication::getStatus, "PENDING")
                .orderByAsc(SuspensionApplication::getApplyTime);
        return applicationMapper.selectList(wrapper);
    }

    // 获取所有申请（包含已审核）
    public List<SuspensionApplication> getAllApplications() {
        LambdaQueryWrapper<SuspensionApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SuspensionApplication::getApplyTime);
        return applicationMapper.selectList(wrapper);
    }

    // 审核申请（外层事务已包含所有内部操作）
    @Transactional(transactionManager = "hospitalTransactionManager", rollbackFor = Exception.class)
    public boolean auditApplication(Long applicationId, String status, String auditComment, Long auditorId) {
        SuspensionApplication application = applicationMapper.selectById(applicationId);
        if (application == null || !"PENDING".equals(application.getStatus())) {
            return false;
        }
        // 更新申请状态
        application.setStatus(status);
        application.setAuditTime(LocalDateTime.now());
        application.setAuditorId(auditorId);
        application.setAuditComment(auditComment);
        applicationMapper.updateById(application);
        // 如果审核通过，执行停诊处理
        if ("APPROVED".equals(status)) {
            processSuspension(application.getDoctorId(), application.getSuspensionDate());
        }
        log.info("申请{}审核结果：{}，审核人：{}", applicationId, status, auditorId);
        return true;
    }

    // 处理停诊：取消排班、取消订单、释放号源
    // 修正：移除private方法上的@Transactional（外层auditApplication已有事务）
    private void processSuspension(Long doctorId, LocalDate suspensionDate) {
        log.info("开始处理医生{}在{}的停诊", doctorId, suspensionDate);
        // 1. 查询该医生当天的所有排班
        LambdaQueryWrapper<Schedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(Schedule::getDoctorId, doctorId)
                .eq(Schedule::getScheduleDate, suspensionDate);
        List<Schedule> schedules = scheduleMapper.selectList(scheduleWrapper);
        for (Schedule schedule : schedules) {
            Long scheduleId = schedule.getScheduleId();
            log.info("处理排班{}", scheduleId);
            // 2. 查询该排班的所有订单
            LambdaQueryWrapper<RegistrationOrder> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(RegistrationOrder::getScheduleId, scheduleId)
                    .in(RegistrationOrder::getOrderStatus, "PENDING", "PAID");
            List<RegistrationOrder> orders = orderMapper.selectList(orderWrapper);
            // 3. 取消所有订单并释放号源
            for (RegistrationOrder order : orders) {
                // 更新订单状态
                order.setOrderStatus("CANCELLED");
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason("医生停诊，系统自动取消");
                orderMapper.updateById(order);
                // 释放号源
                numberSourceService.releaseNumberSource(order.getSourceId());
                log.info("取消订单{}，释放号源{}", order.getSerialNo(), order.getSourceId());
            }
            // 4. 安全处理排班和号源（有订单则软删除）
            LambdaQueryWrapper<NumberSource> numberWrapper = new LambdaQueryWrapper<>();
            numberWrapper.eq(NumberSource::getScheduleId, scheduleId);
            List<NumberSource> numberSources = numberSourceMapper.selectList(numberWrapper);

            // 标记号源为不可用（不物理删除，保留订单关联）
            for (NumberSource ns : numberSources) {
                ns.setStatus(0);
                numberSourceMapper.updateById(ns);
            }

            // 标记排班为已取消（不物理删除）
            schedule.setStatus(0);
            scheduleMapper.updateById(schedule);

            log.info("停诊处理完成：排班{}已标记取消，{}个号源已标记不可用", scheduleId, numberSources.size());
        }
        log.info("医生{}在{}的停诊处理完成，共取消{}个排班，{}个订单",
                doctorId, suspensionDate, schedules.size(),
                schedules.stream().mapToInt(s -> {
                    LambdaQueryWrapper<RegistrationOrder> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(RegistrationOrder::getScheduleId, s.getScheduleId())
                            .in(RegistrationOrder::getOrderStatus, "PENDING", "PAID");
                    return orderMapper.selectCount(wrapper).intValue();
                }).sum());
    }
}