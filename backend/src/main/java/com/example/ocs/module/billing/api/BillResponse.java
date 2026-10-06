package com.example.ocs.module.billing.api;

import com.example.ocs.module.billing.domain.Bill;
import java.time.Instant;

public record BillResponse(
    long id,
    String serialNo,
    long patientUserId,
    String patientName,
    String sourceType,
    long sourceId,
    long amountTotalCents,
    String status,
    String paymentMethod,
    Instant createdAt
) {
  public static BillResponse from(Bill bill) {
    return new BillResponse(
        bill.getId(),
        bill.getSerialNo(),
        bill.getPatient().getId(),
        bill.getPatient().getDisplayName(),
        bill.getSourceType(),
        bill.getSourceId(),
        bill.getAmountTotalCents(),
        bill.getStatus().name(),
        bill.getPaymentMethod() == null ? null : bill.getPaymentMethod().name(),
        bill.getCreatedAt()
    );
  }
}
