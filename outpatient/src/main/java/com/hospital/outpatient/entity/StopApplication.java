package com.hospital.outpatient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("stop_application")
public class StopApplication {
    @TableId(type = IdType.AUTO)
    private Long applyId;
    private Long doctorId;
    private Long scheduleId;
    private String stopReason;
    private String proofUrl;
    private LocalDateTime applyTime;
    private Long auditorId;
    private LocalDateTime auditTime;
    private String auditStatus;
    private String auditRemark;
}