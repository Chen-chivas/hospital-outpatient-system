package com.example.ocs.module.registration.api;

import com.example.ocs.common.web.ApiResponse;
import com.example.ocs.module.audit.application.AuditService;
import com.example.ocs.module.billing.application.BillingService;
import com.example.ocs.module.registration.application.PatientBlacklistService;
import com.example.ocs.module.registration.application.RegistrationService;
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
public class RegistrationController {
  private final RegistrationService registrationService;
  private final PatientBlacklistService patientBlacklistService;
  private final BillingService billingService;
  private final AuditService auditService;

  public RegistrationController(
      RegistrationService registrationService,
      PatientBlacklistService patientBlacklistService,
      BillingService billingService,
      AuditService auditService
  ) {
    this.registrationService = registrationService;
    this.patientBlacklistService = patientBlacklistService;
    this.billingService = billingService;
    this.auditService = auditService;
  }

  private RegistrationOrderResponse toResponse(com.example.ocs.module.registration.domain.RegistrationOrder order) {
    String billStatus = billingService.findBySource("REGISTRATION", order.getId())
        .map(b -> b.getStatus().name())
        .orElse(null);
    return RegistrationOrderResponse.from(order, billStatus);
  }

  @PostMapping("/api/registrations/book")
  @PreAuthorize("hasRole('PATIENT')")
  public ApiResponse<RegistrationOrderResponse> book(@Valid @RequestBody BookRegistrationRequest request, Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var order = registrationService.book(principal.userId(), request.scheduleId(), request.channel());
    auditService.record(principal.userId(), "BOOK", "registration", "RegistrationOrder", order.getId(), null);
    return ApiResponse.success(toResponse(order));
  }

  @GetMapping("/api/registrations/blacklist/my")
  @PreAuthorize("hasRole('PATIENT')")
  public ApiResponse<PatientBlacklistResponse> myBlacklist(Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var blacklist = patientBlacklistService.get(principal.userId());
    if (blacklist == null) {
      return ApiResponse.success(null);
    }
    return ApiResponse.success(PatientBlacklistResponse.from(blacklist));
  }

  @PostMapping("/api/registrations/{id}/cancel")
  @PreAuthorize("hasRole('PATIENT')")
  public ApiResponse<RegistrationOrderResponse> cancel(@PathVariable("id") long id, Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var order = registrationService.cancelByPatient(principal.userId(), id);
    auditService.record(principal.userId(), "CANCEL", "registration", "RegistrationOrder", order.getId(), null);
    return ApiResponse.success(toResponse(order));
  }

  @PostMapping("/api/registrations/{id}/reschedule")
  @PreAuthorize("hasRole('PATIENT')")
  public ApiResponse<RegistrationOrderResponse> reschedule(
      @PathVariable("id") long id,
      @Valid @RequestBody RescheduleRegistrationRequest request,
      Authentication authentication
  ) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var order = registrationService.rescheduleByPatient(principal.userId(), id, request.targetScheduleId());
    auditService.record(principal.userId(), "RESCHEDULE", "registration", "RegistrationOrder", order.getId(), null);
    return ApiResponse.success(toResponse(order));
  }

  @PostMapping("/api/registrations/{id}/no-show")
  @PreAuthorize("hasRole('DOCTOR')")
  public ApiResponse<RegistrationOrderResponse> noShow(@PathVariable("id") long id, Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var order = registrationService.markNoShow(principal.userId(), id);
    auditService.record(principal.userId(), "NO_SHOW", "registration", "RegistrationOrder", order.getId(), null);
    return ApiResponse.success(toResponse(order));
  }

  @GetMapping("/api/registrations/my")
  @PreAuthorize("hasRole('PATIENT')")
  public ApiResponse<List<RegistrationOrderResponse>> my(Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    return ApiResponse.success(registrationService.listByPatient(principal.userId()).stream().map(this::toResponse).toList());
  }

  @GetMapping("/api/registrations/doctor/my")
  @PreAuthorize("hasRole('DOCTOR')")
  public ApiResponse<List<RegistrationOrderResponse>> doctorMy(Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    return ApiResponse.success(registrationService.listByDoctor(principal.userId()).stream().map(this::toResponse).toList());
  }
}
