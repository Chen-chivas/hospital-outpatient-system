package com.example.ocs.module.visit.domain;

import com.example.ocs.module.pharmacy.domain.Drug;
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
@Table(name = "prescription_items")
public class PrescriptionItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "prescription_id", nullable = false)
  private Prescription prescription;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "drug_id", nullable = false)
  private Drug drug;

  @Column(nullable = false)
  private int quantity;

  @Column(length = 64)
  private String dosage;

  @Column(length = 64)
  private String frequency;

  @Column
  private Integer days;

  @Column(name = "unit_price_cents", nullable = false)
  private long unitPriceCents;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected PrescriptionItem() {}

  public PrescriptionItem(Prescription prescription, Drug drug, int quantity, String dosage, String frequency, Integer days, long unitPriceCents, Instant now) {
    this.prescription = prescription;
    this.drug = drug;
    this.quantity = quantity;
    this.dosage = dosage;
    this.frequency = frequency;
    this.days = days;
    this.unitPriceCents = unitPriceCents;
    this.createdAt = now;
  }

  public Long getId() {
    return id;
  }

  public Drug getDrug() {
    return drug;
  }

  public int getQuantity() {
    return quantity;
  }

  public long getUnitPriceCents() {
    return unitPriceCents;
  }
}

