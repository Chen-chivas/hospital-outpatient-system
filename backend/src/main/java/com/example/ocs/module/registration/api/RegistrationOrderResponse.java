package com.example.ocs.module.registration.api;

import com.example.ocs.module.registration.domain.RegistrationOrder;
import java.time.Instant;
import java.time.LocalDate;

public record RegistrationOrderResponse(
    long id,
    String serialNo,
    long patientUserId,
    String patientName,
    long scheduleId,
    long doctorUserId,
    String doctorName,
    LocalDate scheduleDate,
    String timePeriod,
    String scheduleStatus,
    String channel,
    String status,
    long feeCents,
    String billStatus,
    Instant createdAt,
    Instant updatedAt
) {
  public static RegistrationOrderResponse from(RegistrationOrder order, String billStatus) {
    return new RegistrationOrderResponse(
        order.getId(),
        order.getSerialNo(),
        order.getPatient().getId(),
        order.getPatient().getDisplayName(),
        order.getSchedule().getId(),
        order.getSchedule().getDoctor().getId(),
        order.getSchedule().getDoctor().getDisplayName(),
        order.getSchedule().getScheduleDate(),
        order.getSchedule().getTimePeriod().name(),
        order.getSchedule().getStatus().name(),
        order.getChannel().name(),
        order.getStatus().name(),
        order.getFeeCents(),
        billStatus,
        order.getCreatedAt(),
        order.getUpdatedAt()
    );
  }

  public static RegistrationOrderResponse from(RegistrationOrder order) {
    return from(order, null);
  }
}
