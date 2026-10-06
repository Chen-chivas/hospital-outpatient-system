package com.example.ocs.module.billing.domain;

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
@Table(name = "bills")
public class Bill {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "serial_no", nullable = false, unique = true, length = 64)
  private String serialNo;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "patient_user_id", nullable = false)
  private User patient;

  @Column(name = "source_type", nullable = false, length = 32)
  private String sourceType;

  @Column(name = "source_id", nullable = false)
  private long sourceId;

  @Column(name = "amount_total_cents", nullable = false)
  private long amountTotalCents;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private BillStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", length = 16)
  private PaymentMethod paymentMethod;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Bill() {}

  public Bill(String serialNo, User patient, String sourceType, long sourceId, long amountTotalCents, Instant now) {
    this.serialNo = serialNo;
    this.patient = patient;
    this.sourceType = sourceType;
    this.sourceId = sourceId;
    this.amountTotalCents = amountTotalCents;
    this.status = BillStatus.UNPAID;
    this.paymentMethod = null;
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

  public String getSourceType() {
    return sourceType;
  }

  public long getSourceId() {
    return sourceId;
  }

  public long getAmountTotalCents() {
    return amountTotalCents;
  }

  public BillStatus getStatus() {
    return status;
  }

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void markPaid(PaymentMethod method) {
    this.status = BillStatus.PAID;
    this.paymentMethod = method;
    this.updatedAt = Instant.now();
  }

  public void markRefunded() {
    this.status = BillStatus.REFUNDED;
    this.updatedAt = Instant.now();
  }

  public void markVoided() {
    this.status = BillStatus.VOIDED;
    this.updatedAt = Instant.now();
  }
}
