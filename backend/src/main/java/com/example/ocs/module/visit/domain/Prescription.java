package com.example.ocs.module.visit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "prescriptions")
public class Prescription {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "visit_id", nullable = false, unique = true)
  private Visit visit;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private PrescriptionStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Prescription() {}

  public Prescription(Visit visit, Instant now) {
    this.visit = visit;
    this.status = PrescriptionStatus.ISSUED;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public Long getId() {
    return id;
  }

  public Visit getVisit() {
    return visit;
  }

  public PrescriptionStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void markDispensed() {
    this.status = PrescriptionStatus.DISPENSED;
    this.updatedAt = Instant.now();
  }
}
