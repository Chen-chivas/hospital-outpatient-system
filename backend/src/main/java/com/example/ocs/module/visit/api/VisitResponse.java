package com.example.ocs.module.visit.api;

import com.example.ocs.module.visit.domain.Visit;
import java.time.Instant;

public record VisitResponse(
    long id,
    long registrationOrderId,
    long doctorUserId,
    String doctorName,
    long patientUserId,
    String patientName,
    String status,
    Instant startedAt,
    Instant endedAt
) {
  public static VisitResponse from(Visit visit) {
    return new VisitResponse(
        visit.getId(),
        visit.getRegistrationOrder().getId(),
        visit.getDoctor().getId(),
        visit.getDoctor().getDisplayName(),
        visit.getPatient().getId(),
        visit.getPatient().getDisplayName(),
        visit.getStatus().name(),
        visit.getStartedAt(),
        visit.getEndedAt()
    );
  }
}

