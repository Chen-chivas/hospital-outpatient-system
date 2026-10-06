package com.example.ocs.module.statistics.api;

import com.example.ocs.common.web.ApiResponse;
import com.example.ocs.module.statistics.application.StatisticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatisticsController {
  private final StatisticsService statisticsService;

  public StatisticsController(StatisticsService statisticsService) {
    this.statisticsService = statisticsService;
  }

  @GetMapping("/api/statistics/summary")
  @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
  public ApiResponse<StatisticsSummaryResponse> summary() {
    return ApiResponse.success(statisticsService.summary());
  }

  @GetMapping("/api/statistics/trends")
  @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
  public ApiResponse<TrendsResponse> trends(@RequestParam(value = "days", required = false, defaultValue = "14") int days) {
    return ApiResponse.success(statisticsService.trends(days));
  }

  @GetMapping("/api/statistics/distributions")
  @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
  public ApiResponse<StatisticsDistributionsResponse> distributions(
      @RequestParam(value = "days", required = false, defaultValue = "30") int days
  ) {
    return ApiResponse.success(statisticsService.distributions(days));
  }
}
