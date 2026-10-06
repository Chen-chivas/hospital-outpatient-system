package com.example.ocs.module.statistics.api;

public record StatisticsSummaryResponse(
    long users,
    long schedules,
    long registrations,
    long visits,
    long prescriptions,
    long bills,
    long revenuePaidCents
) {}

