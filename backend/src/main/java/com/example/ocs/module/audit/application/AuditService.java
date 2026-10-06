package com.example.ocs.module.audit.application;

import com.example.ocs.module.audit.domain.AuditLog;
import com.example.ocs.module.audit.infra.AuditLogRepository;
import com.example.ocs.module.user.infra.UserRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
  private final AuditLogRepository auditLogRepository;
  private final UserRepository userRepository;

  public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
    this.auditLogRepository = auditLogRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public void record(long actorUserId, String action, String module, String entityType, Long entityId, String detailsJson) {
    var actor = userRepository.findById(actorUserId).orElse(null);
    AuditLog log = new AuditLog(actor, action, module, entityType, entityId, detailsJson, null, null, Instant.now());
    auditLogRepository.save(log);
  }
}

