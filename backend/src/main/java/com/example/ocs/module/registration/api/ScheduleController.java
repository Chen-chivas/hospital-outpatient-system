package com.example.ocs.module.registration.api;

import com.example.ocs.common.web.ApiResponse;
import com.example.ocs.module.audit.application.AuditService;
import com.example.ocs.module.registration.application.ScheduleService;
import com.example.ocs.security.OcsPrincipal;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScheduleController {
  private final ScheduleService scheduleService;
  private final AuditService auditService;

  public ScheduleController(ScheduleService scheduleService, AuditService auditService) {
    this.scheduleService = scheduleService;
    this.auditService = auditService;
  }

  @PostMapping("/api/schedules")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<ScheduleResponse> create(@Valid @RequestBody CreateScheduleRequest request, Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var schedule = scheduleService.create(
        request.doctorUserId(),
        request.scheduleDate(),
        request.timePeriod(),
        request.feeCents(),
        request.capacityTotal()
    );
    auditService.record(principal.userId(), "CREATE", "registration", "Schedule", schedule.getId(), null);
    return ApiResponse.success(ScheduleResponse.from(schedule));
  }

  @GetMapping("/api/schedules")
  public ApiResponse<List<ScheduleResponse>> list(
      @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(value = "doctorUserId", required = false) Long doctorUserId,
      @RequestParam(value = "timePeriod", required = false) String timePeriod,
      @RequestParam(value = "status", required = false) String status
  ) {
    return ApiResponse.success(scheduleService.list(date, doctorUserId, timePeriod, status).stream().map(ScheduleResponse::from).toList());
  }

  @PostMapping("/api/schedules/{id}/close")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<ScheduleResponse> close(@PathVariable("id") long id, Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var schedule = scheduleService.close(id);
    auditService.record(principal.userId(), "CLOSE", "registration", "Schedule", schedule.getId(), null);
    return ApiResponse.success(ScheduleResponse.from(schedule));
  }
}
