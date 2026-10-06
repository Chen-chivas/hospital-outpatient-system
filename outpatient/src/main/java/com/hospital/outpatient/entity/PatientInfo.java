package com.hospital.outpatient.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("patient_info")
public class PatientInfo {
    @TableId
    private Long patientId;
    private String gender;
    private Integer age;
    private String address;
    private String emergencyContact;
    private String emergencyPhone;
    private Integer isSpecial;
    private String specialType;
}