package com.example.ocs.module.pharmacy.domain;

import com.example.ocs.module.user.domain.User;
import com.example.ocs.module.visit.domain.Prescription;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "dispense_records")
public class DispenseRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "prescription_id", nullable = false)
  private Prescription prescription;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "pharmacist_user_id", nullable = false)
  private User pharmacist;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private DispenseStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected DispenseRecord() {}

  public DispenseRecord(Prescription prescription, User pharmacist, Instant now) {
    this.prescription = prescription;
    this.pharmacist = pharmacist;
    this.status = DispenseStatus.DISPENSED;
    this.createdAt = now;
  }

  public Long getId() {
    return id;
  }

  public Prescription getPrescription() {
    return prescription;
  }

  public User getPharmacist() {
    return pharmacist;
  }

  public DispenseStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}

