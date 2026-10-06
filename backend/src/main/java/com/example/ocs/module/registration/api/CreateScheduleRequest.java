package com.example.ocs.module.registration.api;

import com.example.ocs.module.registration.domain.TimePeriod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateScheduleRequest(
    @NotNull Long doctorUserId,
    @NotNull LocalDate scheduleDate,
    @NotNull TimePeriod timePeriod,
    @NotNull Long feeCents,
    @Min(1) int capacityTotal
) {}

