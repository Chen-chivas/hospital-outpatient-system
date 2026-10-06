package com.medical.mapper;

import com.medical.entity.DispenseRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface DispenseRecordMapper {

    @Select("SELECT * FROM dispense_record WHERE prescription_id = #{prescriptionId}")
    DispenseRecord selectByPrescriptionId(Long prescriptionId);

    @Select("SELECT * FROM dispense_record WHERE patient_id = #{patientId} ORDER BY dispense_time DESC")
    List<DispenseRecord> selectByPatientId(Long patientId);

    @Insert("INSERT INTO dispense_record (prescription_id, pharmacist_id, patient_id, drug_name, quantity, status) " +
            "VALUES (#{prescriptionId}, #{pharmacistId}, #{patientId}, #{drugName}, #{quantity}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DispenseRecord record);
}