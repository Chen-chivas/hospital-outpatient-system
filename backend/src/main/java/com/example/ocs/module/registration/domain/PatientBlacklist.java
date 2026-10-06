package com.example.ocs.module.registration.domain;

import com.example.ocs.module.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "patient_blacklists")
public class PatientBlacklist {
  @Id
  @Column(name = "patient_user_id")
  private Long patientUserId;

  @MapsId
  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "patient_user_id", nullable = false)
  private User patient;

  @Column(name = "no_show_count", nullable = false)
  private int noShowCount;

  @Column(name = "last_no_show_at")
  private Instant lastNoShowAt;

  @Column(name = "blacklisted_until")
  private Instant blacklistedUntil;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected PatientBlacklist() {}

  public PatientBlacklist(User patient, Instant now) {
    this.patient = patient;
    this.patientUserId = patient.getId();
    this.noShowCount = 0;
    this.updatedAt = now;
  }

  public Long getPatientUserId() {
    return patientUserId;
  }

  public User getPatient() {
    return patient;
  }

  public int getNoShowCount() {
    return noShowCount;
  }

  public Instant getLastNoShowAt() {
    return lastNoShowAt;
  }

  public Instant getBlacklistedUntil() {
    return blacklistedUntil;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public boolean isBlacklistedAt(Instant now) {
    return blacklistedUntil != null && blacklistedUntil.isAfter(now);
  }

  public void recordNoShow(Instant now) {
    this.noShowCount += 1;
    this.lastNoShowAt = now;
    this.updatedAt = now;

    if (noShowCount >= 3) {
      this.blacklistedUntil = now.plus(Duration.ofDays(7));
    } else if (noShowCount >= 2) {
      this.blacklistedUntil = now.plus(Duration.ofDays(1));
    }
  }

  public void clearBlacklist(Instant now) {
    this.blacklistedUntil = null;
    this.updatedAt = now;
  }
}

