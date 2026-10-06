package com.medical.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChargeRecord {
    private Long id;
    private Long patientId;
    private String patientName;
    private String chargeNo;
    private BigDecimal totalAmount;
    private BigDecimal insuranceAmount;
    private BigDecimal personalAccount;
    private BigDecimal cashAmount;
    private BigDecimal selfPay;
    private String paymentMethod;
    private String status;
    private LocalDateTime chargeTime;
    private String invoiceNo;
    private String insuranceSerialNo;
    private String remark;
    private Long prescriptionId;  // 新增：关联的处方ID
}