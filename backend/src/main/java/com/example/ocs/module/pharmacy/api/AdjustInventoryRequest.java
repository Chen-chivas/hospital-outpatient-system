package com.example.ocs.module.pharmacy.api;

import jakarta.validation.constraints.NotNull;

public record AdjustInventoryRequest(@NotNull Long drugId, int delta) {}

