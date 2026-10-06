package com.hospital.outpatient.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("doctor_info")
public class DoctorInfo {
    @TableId
    private Long doctorId;
    private Integer deptId;
    private String title;
    private String specialty;
    private BigDecimal consultationFee;
    private BigDecimal expertFee;
}