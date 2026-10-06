package com.example.ocs.module.pharmacy.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDrugRequest(
    @NotBlank @Size(max = 64) String code,
    @NotBlank @Size(max = 128) String name,
    @Size(max = 128) String spec,
    @Size(max = 32) String unit,
    @NotNull Long priceCents
) {}

