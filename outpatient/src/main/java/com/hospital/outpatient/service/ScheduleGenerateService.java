package com.hospital.outpatient.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.outpatient.entity.DoctorInfo;
import com.hospital.outpatient.entity.NumberSource;
import com.hospital.outpatient.entity.Schedule;
import com.hospital.outpatient.mapper.DoctorInfoMapper;
import com.hospital.outpatient.mapper.NumberSourceMapper;
import com.hospital.outpatient.mapper.ScheduleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Slf4j
public class ScheduleGenerateService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private NumberSourceMapper numberSourceMapper;

    @Autowired
    private DoctorInfoMapper doctorInfoMapper;

    // 生成指定日期的所有排班和号源
    @Transactional(rollbackFor = Exception.class)
    public void generateScheduleForDate(LocalDate date) {
        log.info("开始生成{}的排班和号源", date);

        // 1. 检查当天是否已经生成过排班
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getScheduleDate, date);
        Long count = scheduleMapper.selectCount(wrapper);

        if (count > 0) {
            log.info("{}的排班已经生成过了，跳过", date);
            return;
        }

        // 2. 获取所有医生信息
        List<DoctorInfo> doctors = doctorInfoMapper.selectList(null);

        // 3. 为每个医生生成排班和号源
        for (DoctorInfo doctor : doctors) {
            // 根据医生ID生成不同的出诊时间（模拟真实医院排班）
            Schedule schedule = createDoctorSchedule(doctor, date);

            // 保存排班
            scheduleMapper.insert(schedule);
            log.info("生成医生{}的排班成功，排班ID：{}", doctor.getDoctorId(), schedule.getScheduleId());

            // 生成对应的号源
            generateNumberSources(schedule);
        }

        log.info("{}的排班和号源生成完成，共生成{}个医生的排班", date, doctors.size());
    }

    // 创建单个医生的排班
    private Schedule createDoctorSchedule(DoctorInfo doctor, LocalDate date) {
        Schedule schedule = new Schedule();
        schedule.setDoctorId(doctor.getDoctorId());
        schedule.setDeptId(doctor.getDeptId());
        schedule.setScheduleDate(date);
        schedule.setRegistrationFee(doctor.getConsultationFee());
        schedule.setAppointmentCycle(7);
        schedule.setCreateBy(1L); // 系统自动创建
        schedule.setStatus(1); // 正常状态

        // 根据医生ID分配不同的出诊时间（模拟）
        long doctorId = doctor.getDoctorId();
        if (doctorId % 3 == 1) {
            // 上午班：8:00-12:00
            schedule.setStartTime(LocalTime.of(8, 0));
            schedule.setEndTime(LocalTime.of(12, 0));
            schedule.setTimeSlotDuration(30); // 30分钟一个号
            schedule.setTotalSlots(8); // 共8个时段
            schedule.setSlotsPerTime(5); // 每个时段5个号
        } else if (doctorId % 3 == 2) {
            // 下午班：14:00-17:30
            schedule.setStartTime(LocalTime.of(14, 0));
            schedule.setEndTime(LocalTime.of(17, 30));
            schedule.setTimeSlotDuration(30);
            schedule.setTotalSlots(7);
            schedule.setSlotsPerTime(4);
        } else {
            // 全天班：8:00-12:00 和 14:00-17:30
            schedule.setStartTime(LocalTime.of(8, 0));
            schedule.setEndTime(LocalTime.of(17, 30));
            schedule.setTimeSlotDuration(30);
            schedule.setTotalSlots(15);
            schedule.setSlotsPerTime(3);
        }

        return schedule;
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
            numberSource.setStatus(1); // 可预约

            numberSourceMapper.insert(numberSource);

            // 计算下一个时段的开始时间
            currentTime = currentTime.plusMinutes(slotDuration);

            // 如果是中午12点，跳到下午14点
            if (currentTime.equals(LocalTime.of(12, 0))) {
                currentTime = LocalTime.of(14, 0);
            }
        }

        log.info("为排班{}生成了{}个号源", schedule.getScheduleId(), totalSlots);
    }
}