package com.example.ocs.module.registration.api;

import com.example.ocs.module.registration.domain.Schedule;
import java.time.LocalDate;

public record ScheduleResponse(
    long id,
    long doctorUserId,
    String doctorName,
    LocalDate scheduleDate,
    String timePeriod,
    long feeCents,
    int capacityTotal,
    int capacityRemaining,
    String status
) {
  public static ScheduleResponse from(Schedule schedule) {
    return new ScheduleResponse(
        schedule.getId(),
        schedule.getDoctor().getId(),
        schedule.getDoctor().getDisplayName(),
        schedule.getScheduleDate(),
        schedule.getTimePeriod().name(),
        schedule.getFeeCents(),
        schedule.getCapacityTotal(),
        schedule.getCapacityRemaining(),
        schedule.getStatus().name()
    );
  }
}

