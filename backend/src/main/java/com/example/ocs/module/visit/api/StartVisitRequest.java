package com.example.ocs.module.visit.api;

import jakarta.validation.constraints.NotNull;

public record StartVisitRequest(@NotNull Long registrationOrderId) {}

