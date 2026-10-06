package com.hospital.outpatient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("number_source")
public class NumberSource {
    @TableId(type = IdType.AUTO)
    private Long sourceId;
    private Long scheduleId;
    private Integer slotNumber;
    private LocalTime slotStartTime;
    private LocalTime slotEndTime;
    private Integer totalCount;
    private Integer remainingCount;
    private Integer lockedCount;
    private Integer status;
    private LocalDateTime releaseTime;
    private LocalDateTime updateTime;
}