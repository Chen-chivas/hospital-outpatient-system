package com.medical.mapper;

import com.medical.entity.Prescription;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PrescriptionMapper {

    @Select("SELECT * FROM prescription WHERE status = '待审核' ORDER BY create_time DESC")
    List<Prescription> selectPendingPrescriptions();

    @Select("SELECT * FROM prescription WHERE status = '待调配' ORDER BY create_time DESC")
    List<Prescription> selectPendingDispense();

    @Select("SELECT * FROM prescription WHERE patient_id = #{patientId}")
    List<Prescription> selectByPatientId(Long patientId);

    @Select("SELECT * FROM prescription WHERE id = #{id}")
    Prescription selectById(Long id);

    @Select("SELECT * FROM prescription WHERE prescription_no = #{prescriptionNo}")
    Prescription selectByNo(String prescriptionNo);

    @Select("SELECT * FROM prescription WHERE status = '已完成' AND create_time >= #{startDate}")
    List<Prescription> selectCompletedSince(String startDate);

    @Insert("INSERT INTO prescription (medical_record_id, patient_id, doctor_id, prescription_no, drug_name, " +
            "specification, quantity, usage_desc, dosage, duration, unit_price, total_amount, status, create_time) " +
            "VALUES (#{medicalRecordId}, #{patientId}, #{doctorId}, #{prescriptionNo}, #{drugName}, " +
            "#{specification}, #{quantity}, #{usageDesc}, #{dosage}, #{duration}, #{unitPrice}, #{totalAmount}, #{status}, NOW())")
    int insert(Prescription prescription);

    @Update("UPDATE prescription SET status=#{status}, audit_time=NOW(), audit_result=#{auditResult}, reject_reason=#{rejectReason} WHERE id=#{id}")
    int update(Prescription prescription);

    @Update("UPDATE prescription SET status='已完成', audit_time=NOW() WHERE id=#{id}")
    int updateToCompleted(Long id);
}
