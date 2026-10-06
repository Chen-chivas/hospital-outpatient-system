package com.example.ocs.module.visit.api;

import com.example.ocs.module.visit.domain.PrescriptionItem;

public record PrescriptionItemResponse(long id, long drugId, String drugName, int quantity, long unitPriceCents) {
  public static PrescriptionItemResponse from(PrescriptionItem item) {
    return new PrescriptionItemResponse(item.getId(), item.getDrug().getId(), item.getDrug().getName(), item.getQuantity(), item.getUnitPriceCents());
  }
}

