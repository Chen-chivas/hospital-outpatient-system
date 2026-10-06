package com.example.ocs.module.pharmacy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "drugs")
public class Drug {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String code;

  @Column(nullable = false, length = 128)
  private String name;

  @Column(length = 128)
  private String spec;

  @Column(length = 32)
  private String unit;

  @Column(name = "price_cents", nullable = false)
  private long priceCents;

  @Column(nullable = false)
  private boolean enabled;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Drug() {}

  public Drug(String code, String name, String spec, String unit, long priceCents, Instant now) {
    this.code = code;
    this.name = name;
    this.spec = spec;
    this.unit = unit;
    this.priceCents = priceCents;
    this.enabled = true;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public Long getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public String getSpec() {
    return spec;
  }

  public String getUnit() {
    return unit;
  }

  public long getPriceCents() {
    return priceCents;
  }

  public boolean isEnabled() {
    return enabled;
  }
}

