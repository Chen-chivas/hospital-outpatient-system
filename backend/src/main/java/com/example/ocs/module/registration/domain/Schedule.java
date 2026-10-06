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
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "schedules")
public class Schedule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "doctor_user_id", nullable = false)
  private User doctor;

  @Column(name = "schedule_date", nullable = false)
  private LocalDate scheduleDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "time_period", nullable = false, length = 16)
  private TimePeriod timePeriod;

  @Column(name = "fee_cents", nullable = false)
  private long feeCents;

  @Column(name = "capacity_total", nullable = false)
  private int capacityTotal;

  @Column(name = "capacity_remaining", nullable = false)
  private int capacityRemaining;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ScheduleStatus status;

  @Version
  @Column(nullable = false)
  private long version;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Schedule() {}

  public Schedule(User doctor, LocalDate scheduleDate, TimePeriod timePeriod, long feeCents, int capacityTotal, Instant now) {
    this.doctor = doctor;
    this.scheduleDate = scheduleDate;
    this.timePeriod = timePeriod;
    this.feeCents = feeCents;
    this.capacityTotal = capacityTotal;
    this.capacityRemaining = capacityTotal;
    this.status = ScheduleStatus.OPEN;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public Long getId() {
    return id;
  }

  public User getDoctor() {
    return doctor;
  }

  public LocalDate getScheduleDate() {
    return scheduleDate;
  }

  public TimePeriod getTimePeriod() {
    return timePeriod;
  }

  public long getFeeCents() {
    return feeCents;
  }

  public int getCapacityTotal() {
    return capacityTotal;
  }

  public int getCapacityRemaining() {
    return capacityRemaining;
  }

  public ScheduleStatus getStatus() {
    return status;
  }

  public void close() {
    this.status = ScheduleStatus.CLOSED;
    this.updatedAt = Instant.now();
  }

  public void decrementCapacity() {
    if (capacityRemaining <= 0) {
      throw new IllegalStateException("no capacity remaining");
    }
    this.capacityRemaining -= 1;
    this.updatedAt = Instant.now();
  }

  public void incrementCapacity() {
    if (capacityRemaining >= capacityTotal) {
      throw new IllegalStateException("capacity is already full");
    }
    this.capacityRemaining += 1;
    this.updatedAt = Instant.now();
  }
}
