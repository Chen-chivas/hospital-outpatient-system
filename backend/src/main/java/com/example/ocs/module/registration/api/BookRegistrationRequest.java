package com.example.ocs.module.registration.api;

import com.example.ocs.module.registration.domain.RegistrationChannel;
import jakarta.validation.constraints.NotNull;

public record BookRegistrationRequest(
    @NotNull Long scheduleId,
    @NotNull RegistrationChannel channel
) {}

