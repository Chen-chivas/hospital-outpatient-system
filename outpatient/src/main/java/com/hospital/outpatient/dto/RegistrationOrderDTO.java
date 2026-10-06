package com.hospital.outpatient.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class RegistrationOrderDTO {
    // 原有订单字段
    private Long orderId;
    private String serialNo;
    private LocalDateTime orderTime;
    private LocalDateTime lockExpireTime;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private String payMethod;
    private BigDecimal payAmount;
    private String orderStatus;
    private String cancelReason;
    private String createChannel;
    private LocalDate visitDate;

    // 新增展示字段
    private String doctorName;       // 医生姓名
    private String doctorTitle;      // 医生职称
    private String deptName;         // 就诊科室
    private LocalDateTime visitTime; // 完整就诊时间
    private LocalTime slotStartTime; // 时段开始
    private LocalTime slotEndTime;   // 时段结束
    private String patientName;      // 患者姓名

    // 辅助字段
    private long remainingSeconds;   // 支付剩余秒数（待支付订单用）
}
