package com.hospital.outpatient.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.outpatient.entity.DoctorInfo;
import com.hospital.outpatient.entity.NumberSource;
import com.hospital.outpatient.entity.RegistrationOrder;
import com.hospital.outpatient.entity.Schedule;
import com.hospital.outpatient.entity.SysUser;
import com.hospital.outpatient.mapper.DoctorInfoMapper;
import com.hospital.outpatient.mapper.NumberSourceMapper;
import com.hospital.outpatient.mapper.RegistrationOrderMapper;
import com.hospital.outpatient.mapper.ScheduleMapper;
import com.hospital.outpatient.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ScheduleService extends ServiceImpl<ScheduleMapper, Schedule> {

    @Autowired
    private DoctorInfoMapper doctorInfoMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private NumberSourceMapper numberSourceMapper;

    @Autowired
    private RegistrationOrderMapper registrationOrderMapper;

    // 根据科室ID查询未来7天排班
    public List<Schedule> getByDeptId(Integer deptId) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDeptId, deptId)
                .ge(Schedule::getScheduleDate, LocalDate.now())
                .le(Schedule::getScheduleDate, LocalDate.now().plusDays(7))
                .eq(Schedule::getStatus, 1)
                .orderByAsc(Schedule::getScheduleDate);
        return this.list(wrapper);
    }

    // 根据科室ID和日期查询排班
    public List<Schedule> getByDeptIdAndDate(Integer deptId, LocalDate visitDate) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDeptId, deptId)
                .eq(Schedule::getScheduleDate, visitDate)
                .eq(Schedule::getStatus, 1)
                .orderByAsc(Schedule::getStartTime);
        return this.list(wrapper);
    }

    // 根据日期和科室查询排班（管理员后台用）
    public List<Schedule> getByDateAndDept(LocalDate date, Integer deptId) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Schedule::getScheduleDate, date)
                .eq(deptId != null, Schedule::getDeptId, deptId)
                .orderByAsc(Schedule::getScheduleDate)
                .orderByAsc(Schedule::getStartTime);
        return this.list(wrapper);
    }

    // 根据ID查询排班详情
    public Schedule getById(Long scheduleId) {
        return super.getById(scheduleId);
    }

    // 获取医生ID到姓名的映射
    public Map<Long, String> getDoctorNameMap() {
        List<DoctorInfo> doctors = doctorInfoMapper.selectList(null);
        Map<Long, String> nameMap = new HashMap<>();
        for (DoctorInfo doctor : doctors) {
            SysUser user = sysUserMapper.selectById(doctor.getDoctorId());
            nameMap.put(doctor.getDoctorId(), user != null ? user.getRealName() : "未知");
        }
        return nameMap;
    }

    // 获取医生ID到职称的映射
    public Map<Long, String> getDoctorTitleMap() {
        List<DoctorInfo> doctors = doctorInfoMapper.selectList(null);
        Map<Long, String> titleMap = new HashMap<>();
        for (DoctorInfo doctor : doctors) {
            titleMap.put(doctor.getDoctorId(),
                    doctor.getTitle() != null ? doctor.getTitle() : "");
        }
        return titleMap;
    }

    // 获取单个医生姓名
    public String getDoctorName(Long doctorId) {
        SysUser user = sysUserMapper.selectById(doctorId);
        return user != null ? user.getRealName() : "未知医生";
    }

    // 保存或更新排班，并自动生成号源
    @Transactional(transactionManager = "hospitalTransactionManager", rollbackFor = Exception.class)
    public void saveOrUpdateWithNumberSources(Schedule schedule) {
        if (schedule.getScheduleId() != null) {
            // 编辑模式：安全处理旧号源（有订单的软删除，无订单的硬删除）
            safelyRemoveOldNumberSources(schedule.getScheduleId());
        }

        this.saveOrUpdate(schedule);
        log.info("保存排班成功：排班ID={}", schedule.getScheduleId());

        generateNumberSources(schedule);
    }

    // 删除排班及对应号源
    @Transactional(transactionManager = "hospitalTransactionManager", rollbackFor = Exception.class)
    public void removeWithNumberSources(Long scheduleId) {
        // 先取消该排班下所有待支付/已支付订单
        LambdaQueryWrapper<RegistrationOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(RegistrationOrder::getScheduleId, scheduleId)
                .in(RegistrationOrder::getOrderStatus, "PENDING", "PAID");
        List<RegistrationOrder> activeOrders = registrationOrderMapper.selectList(orderWrapper);
        for (RegistrationOrder order : activeOrders) {
            order.setOrderStatus("CANCELLED");
            order.setCancelTime(LocalDateTime.now());
            order.setCancelReason("排班已取消，系统自动退号");
            registrationOrderMapper.updateById(order);
            log.info("取消订单{}", order.getSerialNo());
        }

        // 安全处理号源：有订单的软删除(status=0)，无订单的硬删除
        safelyRemoveOldNumberSources(scheduleId);

        // 排班本身：有订单则软删除(status=0)，无订单则硬删除
        LambdaQueryWrapper<RegistrationOrder> allOrdersWrapper = new LambdaQueryWrapper<>();
        allOrdersWrapper.eq(RegistrationOrder::getScheduleId, scheduleId);
        long orderCount = registrationOrderMapper.selectCount(allOrdersWrapper);
        if (orderCount > 0) {
            Schedule schedule = this.getById(scheduleId);
            if (schedule != null) {
                schedule.setStatus(0); // 软删除：标记为已取消
                this.updateById(schedule);
                log.info("排班{}存在{}个历史订单，已标记为取消状态", scheduleId, orderCount);
            }
        } else {
            this.removeById(scheduleId);
            log.info("排班{}无关联订单，已物理删除", scheduleId);
        }
    }

    // 安全删除旧号源：有订单引用的标记为不可用(status=0)，无订单的直接物理删除
    private void safelyRemoveOldNumberSources(Long scheduleId) {
        LambdaQueryWrapper<NumberSource> sourceWrapper = new LambdaQueryWrapper<>();
        sourceWrapper.eq(NumberSource::getScheduleId, scheduleId);
        List<NumberSource> oldSources = numberSourceMapper.selectList(sourceWrapper);

        int softDeleted = 0;
        int hardDeleted = 0;
        for (NumberSource source : oldSources) {
            // 检查该号源是否有订单引用
            LambdaQueryWrapper<RegistrationOrder> orderCheck = new LambdaQueryWrapper<>();
            orderCheck.eq(RegistrationOrder::getSourceId, source.getSourceId());
            long count = registrationOrderMapper.selectCount(orderCheck);

            if (count > 0) {
                // 有订单引用：软删除（标记为不可用）
                source.setStatus(0);
                numberSourceMapper.updateById(source);
                softDeleted++;
            } else {
                // 无订单引用：物理删除
                numberSourceMapper.deleteById(source.getSourceId());
                hardDeleted++;
            }
        }
        log.info("排班{}旧号源处理完成：软删除{}个，物理删除{}个", scheduleId, softDeleted, hardDeleted);
    }

    // 生成排班对应的分时段号源
    private void generateNumberSources(Schedule schedule) {
        LocalTime currentTime = schedule.getStartTime();
        int slotDuration = schedule.getTimeSlotDuration();
        int totalSlots = schedule.getTotalSlots();
        int slotsPerTime = schedule.getSlotsPerTime();

        for (int i = 1; i <= totalSlots; i++) {
            NumberSource numberSource = new NumberSource();
            numberSource.setScheduleId(schedule.getScheduleId());
            numberSource.setSlotNumber(i);
            numberSource.setSlotStartTime(currentTime);
            numberSource.setSlotEndTime(currentTime.plusMinutes(slotDuration));
            numberSource.setTotalCount(slotsPerTime);
            numberSource.setRemainingCount(slotsPerTime);
            numberSource.setLockedCount(0);
            numberSource.setReleaseTime(LocalDate.now().minusDays(7).atStartOfDay());
            numberSource.setStatus(1);

            numberSourceMapper.insert(numberSource);

            currentTime = currentTime.plusMinutes(slotDuration);

            if (currentTime.equals(LocalTime.of(12, 0))) {
                currentTime = LocalTime.of(14, 0);
            }
        }

        log.info("为排班{}生成了{}个号源", schedule.getScheduleId(), totalSlots);
    }
}
