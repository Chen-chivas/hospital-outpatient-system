package com.example.ocs.module.registration.api;

import jakarta.validation.constraints.NotNull;

public record CreateStopClinicRequest(
    @NotNull Long scheduleId,
    String reason
) {}

