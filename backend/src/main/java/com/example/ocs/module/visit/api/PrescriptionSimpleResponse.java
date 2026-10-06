package com.example.ocs.module.visit.api;

import com.example.ocs.module.visit.domain.Prescription;
import java.time.Instant;

public record PrescriptionSimpleResponse(
    long id,
    long visitId,
    long patientUserId,
    String patientName,
    long doctorUserId,
    String doctorName,
    String status,
    Instant createdAt
) {
  public static PrescriptionSimpleResponse from(Prescription p) {
    return new PrescriptionSimpleResponse(
        p.getId(),
        p.getVisit().getId(),
        p.getVisit().getPatient().getId(),
        p.getVisit().getPatient().getDisplayName(),
        p.getVisit().getDoctor().getId(),
        p.getVisit().getDoctor().getDisplayName(),
        p.getStatus().name(),
        p.getCreatedAt()
    );
  }
}

