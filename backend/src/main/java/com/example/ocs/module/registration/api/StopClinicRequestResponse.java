package com.example.ocs.module.registration.api;

import com.example.ocs.module.registration.domain.StopClinicRequest;
import java.time.Instant;
import java.time.LocalDate;

public record StopClinicRequestResponse(
    long id,
    long scheduleId,
    LocalDate scheduleDate,
    String timePeriod,
    String scheduleStatus,
    long doctorUserId,
    String doctorName,
    String reason,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
  public static StopClinicRequestResponse from(StopClinicRequest req) {
    return new StopClinicRequestResponse(
        req.getId(),
        req.getSchedule().getId(),
        req.getSchedule().getScheduleDate(),
        req.getSchedule().getTimePeriod().name(),
        req.getSchedule().getStatus().name(),
        req.getDoctor().getId(),
        req.getDoctor().getDisplayName(),
        req.getReason(),
        req.getStatus().name(),
        req.getCreatedAt(),
        req.getUpdatedAt()
    );
  }
}

