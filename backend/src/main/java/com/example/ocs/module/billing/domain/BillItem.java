package com.example.ocs.module.billing.domain;

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
@Table(name = "bill_items")
public class BillItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bill_id", nullable = false)
  private Bill bill;

  @Column(name = "item_type", nullable = false, length = 32)
  private String itemType;

  @Column(nullable = false, length = 256)
  private String description;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "unit_price_cents", nullable = false)
  private long unitPriceCents;

  @Column(name = "amount_cents", nullable = false)
  private long amountCents;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected BillItem() {}

  public BillItem(Bill bill, String itemType, String description, int quantity, long unitPriceCents, Instant now) {
    this.bill = bill;
    this.itemType = itemType;
    this.description = description;
    this.quantity = quantity;
    this.unitPriceCents = unitPriceCents;
    this.amountCents = unitPriceCents * quantity;
    this.createdAt = now;
  }

  public Long getId() {
    return id;
  }

  public String getItemType() {
    return itemType;
  }

  public String getDescription() {
    return description;
  }

  public int getQuantity() {
    return quantity;
  }

  public long getUnitPriceCents() {
    return unitPriceCents;
  }

  public long getAmountCents() {
    return amountCents;
  }
}

