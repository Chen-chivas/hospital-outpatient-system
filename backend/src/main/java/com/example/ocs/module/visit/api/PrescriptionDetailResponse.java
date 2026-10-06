package com.example.ocs.module.visit.api;

import com.example.ocs.module.visit.domain.Prescription;
import java.time.Instant;
import java.util.List;

public record PrescriptionDetailResponse(
    long id,
    long visitId,
    long patientUserId,
    String patientName,
    long doctorUserId,
    String doctorName,
    String status,
    Instant createdAt,
    List<PrescriptionItemResponse> items
) {
  public static PrescriptionDetailResponse from(Prescription p, List<PrescriptionItemResponse> items) {
    return new PrescriptionDetailResponse(
        p.getId(),
        p.getVisit().getId(),
        p.getVisit().getPatient().getId(),
        p.getVisit().getPatient().getDisplayName(),
        p.getVisit().getDoctor().getId(),
        p.getVisit().getDoctor().getDisplayName(),
        p.getStatus().name(),
        p.getCreatedAt(),
        items
    );
  }
}

