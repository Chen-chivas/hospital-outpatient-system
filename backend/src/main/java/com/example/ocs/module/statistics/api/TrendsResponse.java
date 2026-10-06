package com.example.ocs.module.statistics.api;

import java.util.List;

public record TrendsResponse(int days, List<TrendPointResponse> points) {}

