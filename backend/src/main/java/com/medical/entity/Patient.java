package com.medical.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Patient {
    private Long id;
    private String patientNo;
    private String name;
    private String gender;
    private LocalDate birthday;
    private String phone;
    private String idCard;
    private String allergyHistory;  // 过敏史
    private LocalDateTime createdTime;
}