package com.example.ocs.module.visit.api;

import com.example.ocs.common.web.ApiResponse;
import com.example.ocs.common.exception.BusinessException;
import com.example.ocs.common.web.ErrorCode;
import com.example.ocs.module.visit.infra.PrescriptionRepository;
import com.example.ocs.module.visit.infra.PrescriptionItemRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PrescriptionQueryController {
  private final PrescriptionRepository prescriptionRepository;
  private final PrescriptionItemRepository prescriptionItemRepository;

  public PrescriptionQueryController(PrescriptionRepository prescriptionRepository, PrescriptionItemRepository prescriptionItemRepository) {
    this.prescriptionRepository = prescriptionRepository;
    this.prescriptionItemRepository = prescriptionItemRepository;
  }

  @GetMapping("/api/prescriptions")
  @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
  public ApiResponse<List<PrescriptionSimpleResponse>> list() {
    return ApiResponse.success(prescriptionRepository.findAllByOrderByCreatedAtDesc().stream().map(PrescriptionSimpleResponse::from).toList());
  }

  @GetMapping("/api/prescriptions/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
  public ApiResponse<PrescriptionDetailResponse> detail(@PathVariable("id") long id) {
    var prescription = prescriptionRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "prescription not found"));
    var items = prescriptionItemRepository.findByPrescription_Id(id).stream()
        .map(PrescriptionItemResponse::from)
        .toList();
    return ApiResponse.success(PrescriptionDetailResponse.from(prescription, items));
  }
}
