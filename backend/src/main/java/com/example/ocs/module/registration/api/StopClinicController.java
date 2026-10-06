package com.example.ocs.module.registration.api;

import com.example.ocs.common.web.ApiResponse;
import com.example.ocs.module.audit.application.AuditService;
import com.example.ocs.module.registration.application.StopClinicService;
import com.example.ocs.security.OcsPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StopClinicController {
  private final StopClinicService stopClinicService;
  private final AuditService auditService;

  public StopClinicController(StopClinicService stopClinicService, AuditService auditService) {
    this.stopClinicService = stopClinicService;
    this.auditService = auditService;
  }

  @PostMapping("/api/stop-clinic-requests")
  @PreAuthorize("hasRole('DOCTOR')")
  public ApiResponse<StopClinicRequestResponse> create(@Valid @RequestBody CreateStopClinicRequest request, Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var created = stopClinicService.create(principal.userId(), request.scheduleId(), request.reason());
    auditService.record(principal.userId(), "CREATE", "registration", "StopClinicRequest", created.getId(), null);
    return ApiResponse.success(StopClinicRequestResponse.from(created));
  }

  @GetMapping("/api/stop-clinic-requests/my")
  @PreAuthorize("hasRole('DOCTOR')")
  public ApiResponse<List<StopClinicRequestResponse>> my(Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    return ApiResponse.success(stopClinicService.listByDoctor(principal.userId()).stream().map(StopClinicRequestResponse::from).toList());
  }

  @GetMapping("/api/stop-clinic-requests/pending")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<List<StopClinicRequestResponse>> pending() {
    return ApiResponse.success(stopClinicService.listPending().stream().map(StopClinicRequestResponse::from).toList());
  }

  @PostMapping("/api/stop-clinic-requests/{id}/approve")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<StopClinicRequestResponse> approve(@PathVariable("id") long id, Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var req = stopClinicService.approve(id);
    auditService.record(principal.userId(), "APPROVE", "registration", "StopClinicRequest", req.getId(), null);
    return ApiResponse.success(StopClinicRequestResponse.from(req));
  }

  @PostMapping("/api/stop-clinic-requests/{id}/reject")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<StopClinicRequestResponse> reject(@PathVariable("id") long id, Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var req = stopClinicService.reject(id);
    auditService.record(principal.userId(), "REJECT", "registration", "StopClinicRequest", req.getId(), null);
    return ApiResponse.success(StopClinicRequestResponse.from(req));
  }
}

