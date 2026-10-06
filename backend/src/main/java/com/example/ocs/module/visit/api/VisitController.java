package com.example.ocs.module.visit.api;

import com.example.ocs.common.web.ApiResponse;
import com.example.ocs.module.audit.application.AuditService;
import com.example.ocs.module.visit.application.VisitService;
import com.example.ocs.module.visit.infra.PrescriptionItemRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.ocs.security.OcsPrincipal;

@RestController
public class VisitController {
  private final VisitService visitService;
  private final PrescriptionItemRepository prescriptionItemRepository;
  private final AuditService auditService;

  public VisitController(VisitService visitService, PrescriptionItemRepository prescriptionItemRepository, AuditService auditService) {
    this.visitService = visitService;
    this.prescriptionItemRepository = prescriptionItemRepository;
    this.auditService = auditService;
  }

  @PostMapping("/api/visits/start")
  @PreAuthorize("hasRole('DOCTOR')")
  public ApiResponse<VisitResponse> start(@Valid @RequestBody StartVisitRequest request, Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var visit = visitService.startVisit(request.registrationOrderId(), principal.userId());
    auditService.record(principal.userId(), "START", "visit", "Visit", visit.getId(), null);
    return ApiResponse.success(VisitResponse.from(visit));
  }

  @PutMapping("/api/visits/{visitId}/emr")
  @PreAuthorize("hasRole('DOCTOR')")
  public ApiResponse<EmrResponse> updateEmr(
      @PathVariable("visitId") long visitId,
      @RequestBody UpdateEmrRequest request,
      Authentication authentication
  ) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var emr = visitService.updateEmr(
        visitId,
        principal.userId(),
        request.chiefComplaint(),
        request.historyPresentIllness(),
        request.physicalExam(),
        request.diagnosis(),
        request.treatmentPlan()
    );
    auditService.record(principal.userId(), "UPDATE", "visit", "EMR", emr.getId(), null);
    return ApiResponse.success(EmrResponse.from(emr));
  }

  @PostMapping("/api/visits/{visitId}/prescription")
  @PreAuthorize("hasRole('DOCTOR')")
  public ApiResponse<PrescriptionResponse> issuePrescription(
      @PathVariable("visitId") long visitId,
      @Valid @RequestBody IssuePrescriptionRequest request,
      Authentication authentication
  ) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var drafts = request.items().stream()
        .map(i -> new VisitService.PrescriptionItemDraft(i.drugId(), i.quantity(), i.dosage(), i.frequency(), i.days()))
        .toList();
    var prescription = visitService.issuePrescription(visitId, principal.userId(), drafts);
    var items = prescriptionItemRepository.findByPrescription_Id(prescription.getId()).stream()
        .map(PrescriptionItemResponse::from)
        .toList();
    auditService.record(principal.userId(), "ISSUE", "visit", "Prescription", prescription.getId(), null);
    return ApiResponse.success(new PrescriptionResponse(prescription.getId(), visitId, prescription.getStatus().name(), items));
  }

  @GetMapping("/api/visits/my")
  @PreAuthorize("hasRole('DOCTOR')")
  public ApiResponse<List<VisitResponse>> my(Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    return ApiResponse.success(visitService.listByDoctor(principal.userId()).stream().map(VisitResponse::from).toList());
  }
}
