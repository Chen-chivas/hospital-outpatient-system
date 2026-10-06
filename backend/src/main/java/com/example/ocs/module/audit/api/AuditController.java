package com.example.ocs.module.audit.api;

import com.example.ocs.common.web.ApiResponse;
import com.example.ocs.module.audit.infra.AuditLogRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuditController {
  private final AuditLogRepository auditLogRepository;

  public AuditController(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  @GetMapping("/api/audit/recent")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ApiResponse<List<AuditLogResponse>> recent() {
    return ApiResponse.success(auditLogRepository.findTop50ByOrderByCreatedAtDesc().stream().map(AuditLogResponse::from).toList());
  }
}

