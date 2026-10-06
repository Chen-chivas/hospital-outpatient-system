package com.medical.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FinancialReport {
    private Long id;
    private LocalDate reportDate;
    private BigDecimal totalIncome;
    private BigDecimal cashAmount;
    private BigDecimal wechatAmount;
    private BigDecimal alipayAmount;
    private BigDecimal insuranceAmount;
    private LocalDateTime createdTime;
}