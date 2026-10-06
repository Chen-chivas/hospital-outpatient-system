package com.hospital.outpatient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("missed_appointment")
public class MissedAppointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;
    private Long orderId;
    private LocalDateTime missedTime;
    private String reason;
}