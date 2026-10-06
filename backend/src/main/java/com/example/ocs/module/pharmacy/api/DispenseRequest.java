package com.example.ocs.module.pharmacy.api;

import jakarta.validation.constraints.NotNull;

public record DispenseRequest(@NotNull Long prescriptionId) {}

