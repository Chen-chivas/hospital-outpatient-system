package com.example.ocs.module.pharmacy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "inventories")
public class Inventory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "drug_id", nullable = false, unique = true)
  private Drug drug;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Inventory() {}

  public Inventory(Drug drug, int quantity, Instant now) {
    this.drug = drug;
    this.quantity = quantity;
    this.updatedAt = now;
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

  public void add(int delta) {
    this.quantity += delta;
    this.updatedAt = Instant.now();
  }

  public void subtract(int delta) {
    if (delta <= 0) {
      return;
    }
    if (this.quantity < delta) {
      throw new IllegalStateException("insufficient inventory");
    }
    this.quantity -= delta;
    this.updatedAt = Instant.now();
  }
}

