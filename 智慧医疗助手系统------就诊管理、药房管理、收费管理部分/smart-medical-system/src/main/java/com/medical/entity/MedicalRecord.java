package com.medical.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MedicalRecord {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private String chiefComplaint;
    private String presentIllness;
    private String physicalExam;
    private String diagnosis;
    private String diagnosisCode;
    private String status;
    private LocalDateTime recordTime;
    private LocalDateTime updateTime;
}