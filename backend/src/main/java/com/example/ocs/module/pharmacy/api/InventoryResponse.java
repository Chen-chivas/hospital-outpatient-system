package com.example.ocs.module.pharmacy.api;

import com.example.ocs.module.pharmacy.domain.Inventory;

public record InventoryResponse(long drugId, String drugName, int quantity) {
  public static InventoryResponse from(Inventory inv) {
    return new InventoryResponse(inv.getDrug().getId(), inv.getDrug().getName(), inv.getQuantity());
  }
}

