package com.example.ocs.module.pharmacy.api;

import com.example.ocs.module.pharmacy.domain.DispenseRecord;
import java.time.Instant;

public record DispenseRecordResponse(
    long id,
    long prescriptionId,
    long pharmacistUserId,
    String pharmacistName,
    String status,
    Instant createdAt
) {
  public static DispenseRecordResponse from(DispenseRecord record) {
    return new DispenseRecordResponse(
        record.getId(),
        record.getPrescription().getId(),
        record.getPharmacist().getId(),
        record.getPharmacist().getDisplayName(),
        record.getStatus().name(),
        record.getCreatedAt()
    );
  }
}

