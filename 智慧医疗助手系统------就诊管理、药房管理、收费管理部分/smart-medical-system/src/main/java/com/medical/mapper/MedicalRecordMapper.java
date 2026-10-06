package com.medical.mapper;

import com.medical.entity.MedicalRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MedicalRecordMapper {

    @Select("SELECT * FROM medical_record WHERE status = '待接诊' ORDER BY record_time DESC")
    List<MedicalRecord> selectWaitingPatients();

    @Select("SELECT * FROM medical_record WHERE id = #{id}")
    MedicalRecord selectById(Long id);

    @Select("SELECT * FROM medical_record WHERE patient_id = #{patientId} ORDER BY record_time DESC")
    List<MedicalRecord> selectByPatientId(Long patientId);

    // 添加：获取患者历史病历（用于查看）
    @Select("SELECT * FROM medical_record WHERE patient_id = #{patientId} AND status = '已完成' ORDER BY record_time DESC")
    List<MedicalRecord> selectHistoryByPatientId(Long patientId);

    @Insert("INSERT INTO medical_record (patient_id, doctor_id, chief_complaint, present_illness, physical_exam, diagnosis, diagnosis_code, status, record_time) " +
            "VALUES (#{patientId}, #{doctorId}, #{chiefComplaint}, #{presentIllness}, #{physicalExam}, #{diagnosis}, #{diagnosisCode}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MedicalRecord record);

    @Update("UPDATE medical_record SET chief_complaint=#{chiefComplaint}, present_illness=#{presentIllness}, " +
            "physical_exam=#{physicalExam}, diagnosis=#{diagnosis}, diagnosis_code=#{diagnosisCode}, " +
            "status=#{status}, update_time=NOW() WHERE id=#{id}")
    int update(MedicalRecord record);
}
