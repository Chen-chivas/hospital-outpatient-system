package com.hospital.outpatient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blacklist")
public class Blacklist {
    @TableId(type = IdType.AUTO, value = "black_id") // 匹配你的主键名
    private Long blackId;
    private Long patientId;
    private Integer missedCount; // 爽约次数
    private Integer isLimited; // 1=受限 0=不受限
    private LocalDateTime limitStartTime; // 限制开始时间
    private LocalDateTime limitEndTime; // 限制结束时间
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}