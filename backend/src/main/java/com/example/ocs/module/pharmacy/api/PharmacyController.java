package com.example.ocs.module.pharmacy.api;

import com.example.ocs.common.web.ApiResponse;
import com.example.ocs.module.audit.application.AuditService;
import com.example.ocs.module.pharmacy.application.PharmacyService;
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
public class PharmacyController {
  private final PharmacyService pharmacyService;
  private final AuditService auditService;

  public PharmacyController(PharmacyService pharmacyService, AuditService auditService) {
    this.pharmacyService = pharmacyService;
    this.auditService = auditService;
  }

  @PostMapping("/api/drugs")
  @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
  public ApiResponse<DrugResponse> createDrug(@Valid @RequestBody CreateDrugRequest request, Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var drug = pharmacyService.createDrug(request.code(), request.name(), request.spec(), request.unit(), request.priceCents());
    auditService.record(principal.userId(), "CREATE", "pharmacy", "Drug", drug.getId(), null);
    return ApiResponse.success(DrugResponse.from(drug));
  }

  @GetMapping("/api/drugs")
  public ApiResponse<List<DrugResponse>> listDrugs() {
    return ApiResponse.success(pharmacyService.listDrugs().stream().map(DrugResponse::from).toList());
  }

  @PostMapping("/api/inventory/adjust")
  @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
  public ApiResponse<InventoryResponse> adjust(@Valid @RequestBody AdjustInventoryRequest request, Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var inv = pharmacyService.adjustInventory(request.drugId(), request.delta());
    auditService.record(principal.userId(), "ADJUST", "pharmacy", "Inventory", inv.getId(), null);
    return ApiResponse.success(InventoryResponse.from(inv));
  }

  @GetMapping("/api/inventory/{drugId}")
  @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
  public ApiResponse<InventoryResponse> get(@PathVariable("drugId") long drugId) {
    return ApiResponse.success(InventoryResponse.from(pharmacyService.getInventory(drugId)));
  }

  @PostMapping("/api/pharmacy/dispense")
  @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
  public ApiResponse<DispenseRecordResponse> dispense(@Valid @RequestBody DispenseRequest request, Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var record = pharmacyService.dispense(request.prescriptionId(), principal.userId());
    auditService.record(principal.userId(), "DISPENSE", "pharmacy", "DispenseRecord", record.getId(), null);
    return ApiResponse.success(DispenseRecordResponse.from(record));
  }
}
