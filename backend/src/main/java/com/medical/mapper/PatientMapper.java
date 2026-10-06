package com.medical.mapper;

import com.medical.entity.Patient;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PatientMapper {

    @Select("SELECT * FROM patient WHERE id = #{id}")
    Patient selectById(Long id);

    @Select("SELECT * FROM patient WHERE patient_no = #{patientNo}")
    Patient selectByPatientNo(String patientNo);

    @Select("SELECT * FROM patient WHERE name LIKE CONCAT('%', #{name}, '%')")
    List<Patient> searchByName(String name);

    @Select("SELECT * FROM patient")
    List<Patient> selectAll();

    @Insert("INSERT INTO patient (patient_no, name, gender, birthday, phone, id_card, allergy_history, created_time) " +
            "VALUES (#{patientNo}, #{name}, #{gender}, #{birthday}, #{phone}, #{idCard}, #{allergyHistory}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Patient patient);

    @Update("UPDATE patient SET name=#{name}, gender=#{gender}, birthday=#{birthday}, " +
            "phone=#{phone}, id_card=#{idCard}, allergy_history=#{allergyHistory} WHERE id=#{id}")
    int update(Patient patient);
}
