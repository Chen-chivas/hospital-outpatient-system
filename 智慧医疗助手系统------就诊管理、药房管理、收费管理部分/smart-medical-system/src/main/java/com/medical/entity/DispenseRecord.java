package com.medical.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DispenseRecord {
    private Long id;
    private Long prescriptionId;
    private Long pharmacistId;
    private Long patientId;
    private String drugName;
    private Integer quantity;
    private LocalDateTime dispenseTime;
    private String status;
}