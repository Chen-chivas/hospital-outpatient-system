package com.medical.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DrugInventory {
    private Long id;
    private String drugCode;
    private String drugName;
    private String specification;
    private String manufacturer;
    private Integer currentStock;
    private Integer minStock;
    private Integer maxStock;
    private String batchNo;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private BigDecimal purchasePrice;
    private BigDecimal retailPrice;
    private String location;
    private Integer lockQuantity;
}