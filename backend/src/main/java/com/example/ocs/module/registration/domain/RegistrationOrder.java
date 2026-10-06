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
@Table(name = "registration_orders")
public class RegistrationOrder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "serial_no", nullable = false, unique = true, length = 64)
  private String serialNo;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "patient_user_id", nullable = false)
  private User patient;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "schedule_id", nullable = false)
  private Schedule schedule;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private RegistrationChannel channel;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private RegistrationStatus status;

  @Column(name = "fee_cents", nullable = false)
  private long feeCents;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected RegistrationOrder() {}

  public RegistrationOrder(String serialNo, User patient, Schedule schedule, RegistrationChannel channel, long feeCents, Instant now) {
    this.serialNo = serialNo;
    this.patient = patient;
    this.schedule = schedule;
    this.channel = channel;
    this.status = RegistrationStatus.CREATED;
    this.feeCents = feeCents;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public Long getId() {
    return id;
  }

  public String getSerialNo() {
    return serialNo;
  }

  public User getPatient() {
    return patient;
  }

  public Schedule getSchedule() {
    return schedule;
  }

  public RegistrationChannel getChannel() {
    return channel;
  }

  public RegistrationStatus getStatus() {
    return status;
  }

  public long getFeeCents() {
    return feeCents;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void markPaid() {
    this.status = RegistrationStatus.PAID;
    this.updatedAt = Instant.now();
  }

  public void cancel() {
    this.status = RegistrationStatus.CANCELED;
    this.updatedAt = Instant.now();
  }

  public void markNoShow() {
    this.status = RegistrationStatus.NO_SHOW;
    this.updatedAt = Instant.now();
  }

  public void reschedule(Schedule newSchedule) {
    this.schedule = newSchedule;
    this.feeCents = newSchedule.getFeeCents();
    this.updatedAt = Instant.now();
  }
}
