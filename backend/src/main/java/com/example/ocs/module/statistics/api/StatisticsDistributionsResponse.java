package com.example.ocs.module.statistics.api;

import java.util.List;

public record StatisticsDistributionsResponse(
    int days,
    List<PieSliceResponse> expenseStructure,
    List<PieSliceResponse> patientTypes,
    List<PieSliceResponse> paymentMethods
) {}

