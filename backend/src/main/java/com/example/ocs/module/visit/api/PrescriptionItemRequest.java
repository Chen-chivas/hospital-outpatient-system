package com.example.ocs.module.visit.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PrescriptionItemRequest(
    @NotNull Long drugId,
    @Min(1) int quantity,
    String dosage,
    String frequency,
    Integer days
) {}

