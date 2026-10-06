package com.example.ocs.module.registration.api;

import jakarta.validation.constraints.NotNull;

public record RescheduleRegistrationRequest(
    @NotNull Long targetScheduleId
) {}

