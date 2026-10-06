package com.hospital.outpatient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("suspension_application")
public class SuspensionApplication {
    @TableId(type = IdType.AUTO)
    private Long applicationId;
    private Long doctorId;
    private LocalDate suspensionDate;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
    private LocalDateTime applyTime;
    private LocalDateTime auditTime;
    private Long auditorId;
    private String auditComment;
}