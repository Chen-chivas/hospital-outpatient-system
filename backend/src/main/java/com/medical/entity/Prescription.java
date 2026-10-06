package com.medical.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Prescription {
    private Long id;
    private Long medicalRecordId;
    private Long patientId;
    private Long doctorId;
    private String prescriptionNo;
    private String drugName;
    private String specification;
    private Integer quantity;
    private String usageDesc;
    private String dosage;
    private Integer duration;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime auditTime;
    private String auditResult;
    private String rejectReason;
}