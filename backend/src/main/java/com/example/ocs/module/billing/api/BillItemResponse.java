package com.example.ocs.module.billing.api;

import com.example.ocs.module.billing.domain.BillItem;

public record BillItemResponse(long id, String itemType, String description, int quantity, long unitPriceCents, long amountCents) {
  public static BillItemResponse from(BillItem item) {
    return new BillItemResponse(
        item.getId(),
        item.getItemType(),
        item.getDescription(),
        item.getQuantity(),
        item.getUnitPriceCents(),
        item.getAmountCents()
    );
  }
}

