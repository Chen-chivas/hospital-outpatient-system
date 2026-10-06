package com.example.ocs.module.audit.api;

import com.example.ocs.module.audit.domain.AuditLog;
import java.time.Instant;

public record AuditLogResponse(
    long id,
    Long actorUserId,
    String actorName,
    String action,
    String module,
    String entityType,
    Long entityId,
    String detailsJson,
    Instant createdAt
) {
  public static AuditLogResponse from(AuditLog log) {
    return new AuditLogResponse(
        log.getId(),
        log.getActor() == null ? null : log.getActor().getId(),
        log.getActor() == null ? null : log.getActor().getDisplayName(),
        log.getAction(),
        log.getModule(),
        log.getEntityType(),
        log.getEntityId(),
        log.getDetailsJson(),
        log.getCreatedAt()
    );
  }
}

