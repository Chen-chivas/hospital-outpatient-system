package com.example.ocs.module.user.api;

import com.example.ocs.module.user.domain.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(@NotNull UserStatus status) {}

