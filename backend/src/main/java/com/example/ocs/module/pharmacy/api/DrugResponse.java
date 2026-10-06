package com.example.ocs.module.pharmacy.api;

import com.example.ocs.module.pharmacy.domain.Drug;

public record DrugResponse(long id, String code, String name, String spec, String unit, long priceCents, boolean enabled) {
  public static DrugResponse from(Drug drug) {
    return new DrugResponse(
        drug.getId(),
        drug.getCode(),
        drug.getName(),
        drug.getSpec(),
        drug.getUnit(),
        drug.getPriceCents(),
        drug.isEnabled()
    );
  }
}

