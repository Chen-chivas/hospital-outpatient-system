package com.medical.controller;

import com.medical.entity.ChargeRecord;
import com.medical.entity.Patient;
import com.medical.service.ChargeService;
import com.medical.mapper.PatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChargeController {

    @Autowired
    private ChargeService chargeService;

    @Autowired
    private PatientMapper patientMapper;

    @GetMapping("/charge")
    public String chargePage(Model model) {
        Map<String, Object> todayIncome = chargeService.getTodayIncome();
        model.addAttribute("todayIncome", todayIncome.get("todayIncome"));
        model.addAttribute("todayCount", todayIncome.get("todayCount"));
        return "charge";
    }

    @GetMapping("/api/charge/pending")
    @ResponseBody
    public Map<String, Object> getPendingCharges(@RequestParam Long patientId) {
        Map<String, Object> result = new HashMap<>();
        List<ChargeRecord> list = chargeService.getPendingCharges(patientId);
        result.put("success", true);
        result.put("data", list);
        return result;
    }

    // 修改：创建结算单时关联处方金额
    @PostMapping("/api/charge/create")
    @ResponseBody
    public Map<String, Object> createCharge(@RequestParam Long patientId) {
        Patient patient = patientMapper.selectById(patientId);
        String patientName = patient != null ? patient.getName() : "患者" + patientId;
        return chargeService.createCharge(patientId, patientName);
    }

    @PostMapping("/api/charge/insurance")
    @ResponseBody
    public Map<String, Object> insuranceCalc(@RequestParam BigDecimal totalAmount,
                                             @RequestParam(defaultValue = "职工医保") String insuranceType) {
        return chargeService.insuranceCalc(totalAmount, insuranceType);
    }

    @PostMapping("/api/charge/pay")
    @ResponseBody
    public Map<String, Object> payCharge(@RequestParam Long id,
                                         @RequestParam String paymentMethod,
                                         @RequestParam BigDecimal insuranceAmount,
                                         @RequestParam BigDecimal selfPay) {
        return chargeService.payCharge(id, paymentMethod, insuranceAmount, selfPay);
    }

    @PostMapping("/api/charge/refund")
    @ResponseBody
    public Map<String, Object> refundCharge(@RequestParam Long id, @RequestParam String refundReason) {
        return chargeService.refundCharge(id, refundReason);
    }

    @GetMapping("/api/charge/report")
    @ResponseBody
    public Map<String, Object> financialReport(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return chargeService.generateFinancialReport(startDate, endDate);
    }

    @GetMapping("/api/charge/today-income")
    @ResponseBody
    public Map<String, Object> getTodayIncome() {
        return chargeService.getTodayIncome();
    }

    @GetMapping("/api/charge/detail/{id}")
    @ResponseBody
    public Map<String, Object> getChargeDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", chargeService.getChargeById(id));
        return result;
    }
}