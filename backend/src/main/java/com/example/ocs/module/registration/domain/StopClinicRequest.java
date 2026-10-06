package com.example.ocs.module.registration.domain;

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
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "stop_clinic_requests")
public class StopClinicRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "schedule_id", nullable = false)
  private Schedule schedule;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "doctor_user_id", nullable = false)
  private User doctor;

  @Column(length = 256)
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private StopClinicRequestStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected StopClinicRequest() {}

  public StopClinicRequest(Schedule schedule, User doctor, String reason, Instant now) {
    this.schedule = schedule;
    this.doctor = doctor;
    this.reason = reason;
    this.status = StopClinicRequestStatus.PENDING;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public Long getId() {
    return id;
  }

  public Schedule getSchedule() {
    return schedule;
  }

  public User getDoctor() {
    return doctor;
  }

  public String getReason() {
    return reason;
  }

  public StopClinicRequestStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void approve() {
    this.status = StopClinicRequestStatus.APPROVED;
    this.updatedAt = Instant.now();
  }

  public void reject() {
    this.status = StopClinicRequestStatus.REJECTED;
    this.updatedAt = Instant.now();
  }
}

