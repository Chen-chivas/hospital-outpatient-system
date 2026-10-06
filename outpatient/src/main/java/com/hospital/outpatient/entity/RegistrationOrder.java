package com.hospital.outpatient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("registration_order")
public class RegistrationOrder {
    @TableId(type = IdType.AUTO)
    private Long orderId;
    private String serialNo;
    private Long patientId;
    private Long sourceId;
    private Long scheduleId;
    private Long doctorId;
    private Integer deptId;
    private LocalDateTime orderTime;
    private LocalDateTime lockExpireTime;
    private LocalDateTime payTime;
    private String payMethod;
    private BigDecimal payAmount;
    private String orderStatus;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private String createChannel;
    private LocalDateTime updateTime;
    private LocalDate visitDate; // 就诊日期
}