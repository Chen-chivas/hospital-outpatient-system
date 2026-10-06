package com.example.ocs.module.audit.domain;

import com.example.ocs.module.user.domain.User;
import jakarta.persistence.Lob;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "actor_user_id")
  private User actor;

  @Column(nullable = false, length = 64)
  private String action;

  @Column(nullable = false, length = 64)
  private String module;

  @Column(name = "entity_type", length = 64)
  private String entityType;

  @Column(name = "entity_id")
  private Long entityId;

  @Column(name = "details_json")
  @Lob
  private String detailsJson;

  @Column(length = 64)
  private String ip;

  @Column(name = "user_agent", length = 256)
  private String userAgent;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected AuditLog() {}

  public AuditLog(User actor, String action, String module, String entityType, Long entityId, String detailsJson, String ip, String userAgent, Instant now) {
    this.actor = actor;
    this.action = action;
    this.module = module;
    this.entityType = entityType;
    this.entityId = entityId;
    this.detailsJson = detailsJson;
    this.ip = ip;
    this.userAgent = userAgent;
    this.createdAt = now;
  }

  public Long getId() {
    return id;
  }

  public User getActor() {
    return actor;
  }

  public String getAction() {
    return action;
  }

  public String getModule() {
    return module;
  }

  public String getEntityType() {
    return entityType;
  }

  public Long getEntityId() {
    return entityId;
  }

  public String getDetailsJson() {
    return detailsJson;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
