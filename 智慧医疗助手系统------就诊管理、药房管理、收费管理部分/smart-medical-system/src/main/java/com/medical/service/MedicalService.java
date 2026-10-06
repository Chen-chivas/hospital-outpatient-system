package com.medical.service;

import com.medical.entity.MedicalRecord;
import com.medical.entity.Patient;
import com.medical.entity.Prescription;
import com.medical.mapper.MedicalRecordMapper;
import com.medical.mapper.PatientMapper;
import com.medical.mapper.PrescriptionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MedicalService {

    @Autowired
    private MedicalRecordMapper medicalRecordMapper;

    @Autowired
    private PrescriptionMapper prescriptionMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private AIDiagnosisService aiDiagnosisService;

    public List<MedicalRecord> getWaitingPatients() {
        return medicalRecordMapper.selectWaitingPatients();
    }

    public MedicalRecord getMedicalRecordById(Long id) {
        return medicalRecordMapper.selectById(id);
    }

    public List<MedicalRecord> getPatientHistory(Long patientId) {
        return medicalRecordMapper.selectHistoryByPatientId(patientId);
    }

    public Patient getPatientInfo(Long patientId) {
        return patientMapper.selectById(patientId);
    }

    // 获取患者过敏史
    public String getAllergyHistory(Long patientId) {
        Patient patient = patientMapper.selectById(patientId);
        return patient != null ? patient.getAllergyHistory() : "无";
    }

    @Transactional
    public Map<String, Object> saveMedicalRecord(MedicalRecord record) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (record.getId() == null) {
                record.setStatus("已完成");
                medicalRecordMapper.insert(record);
            } else {
                medicalRecordMapper.update(record);
            }
            result.put("success", true);
            result.put("message", "病历保存成功");
            result.put("data", record);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "保存失败：" + e.getMessage());
        }
        return result;
    }

    @Transactional
    public Map<String, Object> createPrescription(Prescription prescription) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 检查过敏史
            String allergyHistory = getAllergyHistory(prescription.getPatientId());
            if (allergyHistory != null && !"无".equals(allergyHistory)) {
                String drugName = prescription.getDrugName();
                if (allergyHistory.contains("青霉素") && (drugName.contains("青霉素") || drugName.contains("阿莫西林"))) {
                    result.put("success", false);
                    result.put("message", "患者对青霉素类药物过敏，请更换药品");
                    return result;
                }
            }

            prescription.setPrescriptionNo("RX" + System.currentTimeMillis());
            prescription.setStatus("待审核");
            prescription.setCreateTime(LocalDateTime.now());
            prescriptionMapper.insert(prescription);
            result.put("success", true);
            result.put("message", "处方开具成功，等待药师审核");
            result.put("prescription", prescription);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "开具失败：" + e.getMessage());
        }
        return result;
    }

    public Map<String, Object> aiDiagnosis(Long recordId) {
        MedicalRecord record = medicalRecordMapper.selectById(recordId);
        if (record == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "病历不存在");
            return error;
        }
        return aiDiagnosisService.getDiagnosisSuggestion(record);
    }

    public Map<String, Object> checkAllergy(Long patientId, String drugName) {
        Map<String, Object> result = new HashMap<>();
        String allergyHistory = getAllergyHistory(patientId);
        if (allergyHistory != null && !"无".equals(allergyHistory)) {
            if (allergyHistory.contains("青霉素") && (drugName.contains("青霉素") || drugName.contains("阿莫西林"))) {
                result.put("hasAllergy", true);
                result.put("message", "患者对青霉素类药物过敏，请更换药品");
                return result;
            }
        }
        result.put("hasAllergy", false);
        result.put("message", "无过敏史，可以使用");
        return result;
    }
}