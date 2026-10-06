package com.example.ocs.module.registration.application;

import com.example.ocs.common.exception.BusinessException;
import com.example.ocs.common.web.ErrorCode;
import com.example.ocs.module.registration.domain.Schedule;
import com.example.ocs.module.registration.domain.ScheduleStatus;
import com.example.ocs.module.registration.domain.TimePeriod;
import com.example.ocs.module.registration.infra.ScheduleRepository;
import com.example.ocs.module.user.infra.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {
  private final ScheduleRepository scheduleRepository;
  private final UserRepository userRepository;

  public ScheduleService(ScheduleRepository scheduleRepository, UserRepository userRepository) {
    this.scheduleRepository = scheduleRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public Schedule create(long doctorUserId, LocalDate date, TimePeriod timePeriod, long feeCents, int capacityTotal) {
    if (capacityTotal <= 0) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "capacityTotal must be > 0");
    }
    var doctor = userRepository.findById(doctorUserId)
        .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "doctor not found"));
    boolean isDoctor = doctor.getRoles().stream().anyMatch(r -> "DOCTOR".equals(r.getCode()));
    if (!isDoctor) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "user is not a doctor");
    }

    Instant now = Instant.now();
    Schedule schedule = new Schedule(doctor, date, timePeriod, feeCents, capacityTotal, now);
    return scheduleRepository.save(schedule);
  }

  public List<Schedule> list(LocalDate date, Long doctorUserId, String timePeriod, String status) {
    List<Schedule> base = date == null ? scheduleRepository.findAll() : scheduleRepository.findByScheduleDate(date);

    TimePeriod tp = null;
    if (timePeriod != null && !timePeriod.isBlank()) {
      try {
        tp = TimePeriod.valueOf(timePeriod.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
        throw new BusinessException(ErrorCode.BAD_REQUEST, "invalid timePeriod");
      }
    }

    ScheduleStatus ss = null;
    if (status != null && !status.isBlank()) {
      try {
        ss = ScheduleStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
        throw new BusinessException(ErrorCode.BAD_REQUEST, "invalid status");
      }
    }

    final TimePeriod finalTp = tp;
    final ScheduleStatus finalSs = ss;
    return base.stream()
        .filter(s -> doctorUserId == null || s.getDoctor().getId().equals(doctorUserId))
        .filter(s -> finalTp == null || s.getTimePeriod() == finalTp)
        .filter(s -> finalSs == null || s.getStatus() == finalSs)
        .toList();
  }

  @Transactional
  public Schedule close(long scheduleId) {
    Schedule schedule = scheduleRepository.findById(scheduleId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "schedule not found"));
    schedule.close();
    return scheduleRepository.save(schedule);
  }
}
