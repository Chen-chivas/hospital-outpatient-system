package com.medical.service;

import com.medical.entity.MedicalRecord;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AIDiagnosisService {

    // AI诊断建议（基于症状匹配）
    public Map<String, Object> getDiagnosisSuggestion(MedicalRecord record) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> suggestions = new ArrayList<>();

        String chiefComplaint = record.getChiefComplaint();
        if (chiefComplaint == null) {
            chiefComplaint = "";
        }

        // 基于关键词的AI建议
        if (chiefComplaint.contains("发热") && chiefComplaint.contains("咳嗽")) {
            Map<String, Object> suggestion1 = new HashMap<>();
            suggestion1.put("disease", "上呼吸道感染");
            suggestion1.put("confidence", 85);
            suggestion1.put("suggestion", "建议：血常规检查，多休息，多饮水");
            suggestions.add(suggestion1);

            Map<String, Object> suggestion2 = new HashMap<>();
            suggestion2.put("disease", "支气管炎");
            suggestion2.put("confidence", 65);
            suggestion2.put("suggestion", "建议：胸片检查，止咳化痰治疗");
            suggestions.add(suggestion2);
        }

        if (chiefComplaint.contains("头痛") || chiefComplaint.contains("恶心")) {
            Map<String, Object> suggestion1 = new HashMap<>();
            suggestion1.put("disease", "偏头痛");
            suggestion1.put("confidence", 70);
            suggestion1.put("suggestion", "建议：休息，避免强光刺激");
            suggestions.add(suggestion1);

            Map<String, Object> suggestion2 = new HashMap<>();
            suggestion2.put("disease", "高血压");
            suggestion2.put("confidence", 50);
            suggestion2.put("suggestion", "建议：测量血压");
            suggestions.add(suggestion2);
        }

        if (chiefComplaint.contains("腹痛") || chiefComplaint.contains("胃痛")) {
            Map<String, Object> suggestion1 = new HashMap<>();
            suggestion1.put("disease", "急性胃炎");
            suggestion1.put("confidence", 75);
            suggestion1.put("suggestion", "建议：清淡饮食，避免辛辣");
            suggestions.add(suggestion1);
        }

        if (suggestions.isEmpty()) {
            Map<String, Object> defaultSuggestion = new HashMap<>();
            defaultSuggestion.put("disease", "待进一步检查");
            defaultSuggestion.put("confidence", 30);
            defaultSuggestion.put("suggestion", "建议：完善相关检查后再次评估");
            suggestions.add(defaultSuggestion);
        }

        result.put("success", true);
        result.put("suggestions", suggestions);
        return result;
    }
}