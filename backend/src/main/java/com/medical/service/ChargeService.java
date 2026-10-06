package com.medical.service;

import com.medical.entity.ChargeRecord;
import com.medical.entity.Prescription;
import com.medical.mapper.ChargeRecordMapper;
import com.medical.mapper.PrescriptionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChargeService {

    @Autowired
    private ChargeRecordMapper chargeRecordMapper;

    @Autowired
    private PrescriptionMapper prescriptionMapper;

    public List<ChargeRecord> getPendingCharges(Long patientId) {
        return chargeRecordMapper.selectPendingByPatientId(patientId);
    }

    public ChargeRecord getChargeById(Long id) {
        return chargeRecordMapper.selectById(id);
    }

    @Transactional(transactionManager = "smartTransactionManager")
    public Map<String, Object> createCharge(Long patientId, String patientName) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取该患者待支付的处方（待审核 或 待调配）
            List<Prescription> prescriptions = prescriptionMapper.selectByPatientId(patientId);
            boolean hasPendingPrescription = false;
            BigDecimal totalAmount = BigDecimal.ZERO;
            Long prescriptionId = null;
            String drugName = "";

            System.out.println("检查患者 " + patientId + " 的处方：");
            for (Prescription p : prescriptions) {
                System.out.println("处方ID：" + p.getId() + "，状态：" + p.getStatus() + "，金额：" + p.getTotalAmount());
                if ("待审核".equals(p.getStatus()) || "待调配".equals(p.getStatus())) {
                    hasPendingPrescription = true;
                    if (p.getTotalAmount() != null && p.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
                        totalAmount = p.getTotalAmount();
                    } else if (p.getUnitPrice() != null) {
                        totalAmount = p.getUnitPrice().multiply(new BigDecimal(p.getQuantity()));
                    } else {
                        totalAmount = new BigDecimal("100.00");
                    }
                    prescriptionId = p.getId();
                    drugName = p.getDrugName();
                    break;
                }
            }

            System.out.println("有待支付处方：" + hasPendingPrescription + "，总金额：" + totalAmount + "，处方ID：" + prescriptionId);

            // 如果没有待支付的处方，返回错误
            if (!hasPendingPrescription) {
                result.put("success", false);
                result.put("message", "该患者暂无待结算的处方，请先在【就诊管理】中开具处方");
                return result;
            }

            // 检查是否已有该处方的收费单
            ChargeRecord existing = chargeRecordMapper.selectByPrescriptionId(prescriptionId);
            if (existing != null) {
                if ("待支付".equals(existing.getStatus())) {
                    result.put("success", true);
                    result.put("message", "已有待支付收费单");
                    result.put("data", existing);
                    return result;
                } else if ("已支付".equals(existing.getStatus())) {
                    result.put("success", true);
                    result.put("message", "该处方已缴费，可直接去药房取药");
                    result.put("data", existing);
                    return result;
                }
            }

            ChargeRecord record = new ChargeRecord();
            record.setPatientId(patientId);
            record.setPatientName(patientName != null ? patientName : "患者" + patientId);
            record.setChargeNo("CH" + System.currentTimeMillis());
            record.setTotalAmount(totalAmount);
            record.setStatus("待支付");
            record.setPrescriptionId(prescriptionId);
            chargeRecordMapper.insert(record);

            result.put("success", true);
            result.put("message", "结算单创建成功");
            result.put("data", record);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "创建失败：" + e.getMessage());
        }
        return result;
    }

    public Map<String, Object> insuranceCalc(BigDecimal totalAmount, String insuranceType) {
        Map<String, Object> result = new HashMap<>();

        BigDecimal insuranceAmount = BigDecimal.ZERO;
        BigDecimal selfPay = totalAmount;

        if ("职工医保".equals(insuranceType)) {
            insuranceAmount = totalAmount.multiply(new BigDecimal("0.85"));
            selfPay = totalAmount.subtract(insuranceAmount);
        } else if ("居民医保".equals(insuranceType)) {
            insuranceAmount = totalAmount.multiply(new BigDecimal("0.70"));
            selfPay = totalAmount.subtract(insuranceAmount);
        } else if ("新农合".equals(insuranceType)) {
            insuranceAmount = totalAmount.multiply(new BigDecimal("0.60"));
            selfPay = totalAmount.subtract(insuranceAmount);
        } else {
            insuranceAmount = BigDecimal.ZERO;
            selfPay = totalAmount;
        }

        // 保留两位小数
        insuranceAmount = insuranceAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
        selfPay = selfPay.setScale(2, BigDecimal.ROUND_HALF_UP);

        result.put("insuranceAmount", insuranceAmount);
        result.put("selfPay", selfPay);
        result.put("success", true);
        return result;
    }

    @Transactional(transactionManager = "smartTransactionManager")
    public Map<String, Object> payCharge(Long id, String paymentMethod, BigDecimal insuranceAmount, BigDecimal selfPay) {
        Map<String, Object> result = new HashMap<>();
        ChargeRecord record = chargeRecordMapper.selectById(id);

        if (record == null) {
            result.put("success", false);
            result.put("message", "结算单不存在");
            return result;
        }

        if ("已支付".equals(record.getStatus())) {
            result.put("success", false);
            result.put("message", "该账单已支付");
            return result;
        }

        record.setStatus("已支付");
        record.setPaymentMethod(paymentMethod);
        record.setInsuranceAmount(insuranceAmount);
        record.setSelfPay(selfPay);
        record.setChargeTime(LocalDateTime.now());
        record.setInvoiceNo("INV" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        chargeRecordMapper.update(record);

        result.put("success", true);
        result.put("message", "支付成功");
        result.put("invoiceNo", record.getInvoiceNo());
        result.put("data", record);
        return result;
    }

    @Transactional(transactionManager = "smartTransactionManager")
    public Map<String, Object> refundCharge(Long id, String refundReason) {
        Map<String, Object> result = new HashMap<>();
        ChargeRecord record = chargeRecordMapper.selectById(id);

        if (record == null) {
            result.put("success", false);
            result.put("message", "结算单不存在");
            return result;
        }

        if (!"已支付".equals(record.getStatus())) {
            result.put("success", false);
            result.put("message", "只有已支付的订单才能退费");
            return result;
        }

        // 更新收费单状态为已退费
        chargeRecordMapper.refund(id, "退费原因：" + refundReason);

        // 可选：重新开启处方（这里简单处理）
        if (record.getPrescriptionId() != null) {
            Prescription prescription = prescriptionMapper.selectById(record.getPrescriptionId());
            if (prescription != null && "已完成".equals(prescription.getStatus())) {
                prescription.setStatus("待调配");
                prescriptionMapper.update(prescription);
            }
        }

        result.put("success", true);
        result.put("message", "退费成功，款项将原路返回");
        return result;
    }

    public Map<String, Object> generateFinancialReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<ChargeRecord> records = chargeRecordMapper.selectPaidBetween(startDateTime, endDateTime);

        BigDecimal totalIncome = records.stream()
                .map(ChargeRecord::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInsurance = records.stream()
                .map(r -> r.getInsuranceAmount() != null ? r.getInsuranceAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSelfPay = records.stream()
                .map(r -> r.getSelfPay() != null ? r.getSelfPay() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> paymentStats = chargeRecordMapper.selectPaymentMethodStats(startDateTime, endDateTime);
        List<Map<String, Object>> dailyStats = chargeRecordMapper.selectDailyIncome(startDateTime, endDateTime);

        BigDecimal todayIncome = chargeRecordMapper.selectTodayIncome();
        if (todayIncome == null) todayIncome = BigDecimal.ZERO;
        List<ChargeRecord> todayRecords = chargeRecordMapper.selectTodayPaid();

        report.put("success", true);
        report.put("totalIncome", totalIncome);
        report.put("totalInsurance", totalInsurance);
        report.put("totalSelfPay", totalSelfPay);
        report.put("totalCount", records.size());
        report.put("todayIncome", todayIncome);
        report.put("todayCount", todayRecords != null ? todayRecords.size() : 0);
        report.put("paymentMethodStats", paymentStats);
        report.put("dailyStats", dailyStats);
        report.put("records", records);

        return report;
    }

    public Map<String, Object> getTodayIncome() {
        Map<String, Object> result = new HashMap<>();
        BigDecimal todayIncome = chargeRecordMapper.selectTodayIncome();
        if (todayIncome == null) todayIncome = BigDecimal.ZERO;
        List<ChargeRecord> todayRecords = chargeRecordMapper.selectTodayPaid();
        result.put("success", true);
        result.put("todayIncome", todayIncome);
        result.put("todayCount", todayRecords != null ? todayRecords.size() : 0);
        result.put("todayRecords", todayRecords);
        return result;
    }
}