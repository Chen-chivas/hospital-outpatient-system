package com.medical.controller;

import com.medical.entity.MedicalRecord;
import com.medical.entity.Patient;
import com.medical.entity.Prescription;
import com.medical.service.MedicalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/smart")
public class TestController {

    @Autowired
    private MedicalService medicalService;

    @GetMapping({"", "/"})
    public String index(Model model) {
        model.addAttribute("waitingPatients", medicalService.getWaitingPatients());
        return "smart/index";
    }

    @GetMapping("/record/{patientId}")
    public String recordPage(@PathVariable Long patientId, Model model) {
        model.addAttribute("patientId", patientId);
        model.addAttribute("history", medicalService.getPatientHistory(patientId));
        model.addAttribute("patientInfo", medicalService.getPatientInfo(patientId));
        return "smart/record";
    }

    @PostMapping("/api/record/save")
    @ResponseBody
    public Object saveRecord(@RequestBody MedicalRecord record) {
        return medicalService.saveMedicalRecord(record);
    }

    @PostMapping("/api/prescription/create")
    @ResponseBody
    public Object createPrescription(@RequestBody Prescription prescription) {
        return medicalService.createPrescription(prescription);
    }

    @GetMapping("/api/ai-diagnosis/{recordId}")
    @ResponseBody
    public Object aiDiagnosis(@PathVariable Long recordId) {
        return medicalService.aiDiagnosis(recordId);
    }

    @GetMapping("/api/patient/{patientId}")
    @ResponseBody
    public Object getPatientInfo(@PathVariable Long patientId) {
        return medicalService.getPatientInfo(patientId);
    }

    @GetMapping("/api/patient-history/{patientId}")
    @ResponseBody
    public Object getPatientHistory(@PathVariable Long patientId) {
        return medicalService.getPatientHistory(patientId);
    }

    @GetMapping("/api/check-allergy")
    @ResponseBody
    public Object checkAllergy(@RequestParam Long patientId, @RequestParam String drugName) {
        return medicalService.checkAllergy(patientId, drugName);
    }
}
