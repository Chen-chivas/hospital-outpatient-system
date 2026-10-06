package com.example.ocs.module.visit.domain;

import com.example.ocs.module.registration.domain.RegistrationOrder;
import com.example.ocs.module.user.domain.User;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "visits")
public class Visit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "registration_order_id", nullable = false, unique = true)
  private RegistrationOrder registrationOrder;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "doctor_user_id", nullable = false)
  private User doctor;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "patient_user_id", nullable = false)
  private User patient;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private VisitStatus status;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "ended_at")
  private Instant endedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Visit() {}

  public Visit(RegistrationOrder registrationOrder, User doctor, User patient, Instant now) {
    this.registrationOrder = registrationOrder;
    this.doctor = doctor;
    this.patient = patient;
    this.status = VisitStatus.IN_PROGRESS;
    this.startedAt = now;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public Long getId() {
    return id;
  }

  public RegistrationOrder getRegistrationOrder() {
    return registrationOrder;
  }

  public User getDoctor() {
    return doctor;
  }

  public User getPatient() {
    return patient;
  }

  public VisitStatus getStatus() {
    return status;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public Instant getEndedAt() {
    return endedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void finish() {
    this.status = VisitStatus.DONE;
    this.endedAt = Instant.now();
    this.updatedAt = this.endedAt;
  }
}

