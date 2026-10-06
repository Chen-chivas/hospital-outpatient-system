package com.medical.service;

import com.medical.entity.ChargeRecord;
import com.medical.entity.DispenseRecord;
import com.medical.entity.DrugInventory;
import com.medical.entity.Prescription;
import com.medical.mapper.ChargeRecordMapper;
import com.medical.mapper.DispenseRecordMapper;
import com.medical.mapper.DrugInventoryMapper;
import com.medical.mapper.PrescriptionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PharmacyService {

    @Autowired
    private PrescriptionMapper prescriptionMapper;

    @Autowired
    private DrugInventoryMapper drugInventoryMapper;

    @Autowired
    private DispenseRecordMapper dispenseRecordMapper;

    @Autowired
    private ChargeRecordMapper chargeRecordMapper;

    public List<Prescription> getPendingPrescriptions() {
        return prescriptionMapper.selectPendingPrescriptions();
    }

    public List<Prescription> getPendingDispense() {
        return prescriptionMapper.selectPendingDispense();
    }

    public List<DrugInventory> getAllDrugs() {
        return drugInventoryMapper.selectAll();
    }

    public List<DrugInventory> getStockWarnings() {
        return drugInventoryMapper.selectWarningStock();
    }

    public DrugInventory getDrugById(Long id) {
        return drugInventoryMapper.selectById(id);
    }

    public DrugInventory getDrugByCode(String code) {
        return drugInventoryMapper.selectByCode(code);
    }

    public List<DrugInventory> searchDrugs(String keyword) {
        return drugInventoryMapper.searchByName(keyword);
    }

    @Transactional(transactionManager = "smartTransactionManager")
    public Map<String, Object> auditPrescription(Long id, boolean approved, String rejectReason) {
        Map<String, Object> result = new HashMap<>();
        Prescription prescription = prescriptionMapper.selectById(id);

        if (prescription == null) {
            result.put("success", false);
            result.put("message", "处方不存在");
            return result;
        }

        if (approved) {
            prescription.setStatus("待调配");
            prescription.setAuditResult("通过");

            // 审核通过后，自动创建收费单（关联处方ID）
            try {
                // 检查是否已有该处方的收费单
                ChargeRecord existingCharge = chargeRecordMapper.selectByPrescriptionId(id);

                if (existingCharge == null) {
                    // 创建新的收费单，关联处方ID
                    ChargeRecord chargeRecord = new ChargeRecord();
                    chargeRecord.setPatientId(prescription.getPatientId());
                    chargeRecord.setPatientName("患者" + prescription.getPatientId());
                    chargeRecord.setChargeNo("CH" + System.currentTimeMillis());

                    // 计算总金额
                    BigDecimal totalAmount = prescription.getTotalAmount();
                    if (totalAmount == null && prescription.getUnitPrice() != null) {
                        totalAmount = prescription.getUnitPrice().multiply(new BigDecimal(prescription.getQuantity()));
                    }
                    if (totalAmount == null) {
                        totalAmount = new BigDecimal("100.00");
                    }
                    chargeRecord.setTotalAmount(totalAmount);
                    chargeRecord.setStatus("待支付");
                    chargeRecord.setPrescriptionId(id);
                    chargeRecordMapper.insert(chargeRecord);
                    result.put("chargeCreated", true);
                    result.put("chargeId", chargeRecord.getId());
                    result.put("message", "审核通过，待发药，已生成收费单");
                } else if ("待支付".equals(existingCharge.getStatus())) {
                    result.put("chargeCreated", true);
                    result.put("chargeId", existingCharge.getId());
                    result.put("message", "审核通过，待发药，已有待支付收费单");
                } else if ("已支付".equals(existingCharge.getStatus())) {
                    result.put("chargeCreated", true);
                    result.put("chargeId", existingCharge.getId());
                    result.put("message", "审核通过，待发药，收费单已支付，可直接发药");
                } else {
                    result.put("message", "审核通过，待发药");
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.put("chargeCreated", false);
                result.put("message", "审核通过，待发药（收费单创建失败：" + e.getMessage() + "）");
            }

            result.put("success", true);
        } else {
            prescription.setStatus("医师修改");
            prescription.setAuditResult("驳回");
            prescription.setRejectReason(rejectReason);
            result.put("success", false);
            result.put("message", "已驳回：" + rejectReason);
        }
        prescription.setAuditTime(LocalDateTime.now());
        prescriptionMapper.update(prescription);
        return result;
    }

    @Transactional(transactionManager = "smartTransactionManager")
    public Map<String, Object> dispensePrescription(Long id, Long pharmacistId) {
        Map<String, Object> result = new HashMap<>();
        Prescription prescription = prescriptionMapper.selectById(id);

        if (prescription == null) {
            result.put("success", false);
            result.put("message", "处方不存在");
            return result;
        }

        if (!"待调配".equals(prescription.getStatus())) {
            result.put("success", false);
            result.put("message", "处方状态不正确，当前状态：" + prescription.getStatus());
            return result;
        }

        // 检查该处方对应的收费单是否已支付
        ChargeRecord chargeRecord = chargeRecordMapper.selectByPrescriptionId(id);
        if (chargeRecord == null) {
            result.put("success", false);
            result.put("message", "未找到该处方的收费单，请先完成缴费");
            return result;
        }

        if (!"已支付".equals(chargeRecord.getStatus())) {
            result.put("success", false);
            result.put("message", "患者尚未缴费，当前状态：" + chargeRecord.getStatus() + "，请先完成缴费后再发药");
            return result;
        }

        // 检查库存并扣减
        DrugInventory drug = drugInventoryMapper.selectByName(prescription.getDrugName());
        if (drug == null) {
            result.put("success", false);
            result.put("message", "药品不存在");
            return result;
        }

        if (drug.getCurrentStock() < prescription.getQuantity()) {
            result.put("success", false);
            result.put("message", "库存不足，当前库存：" + drug.getCurrentStock());
            return result;
        }

        // 扣减库存
        drugInventoryMapper.deductStock(drug.getDrugCode(), prescription.getQuantity());

        // 更新处方状态
        prescriptionMapper.updateToCompleted(id);

        // 记录发药记录
        DispenseRecord record = new DispenseRecord();
        record.setPrescriptionId(id);
        record.setPharmacistId(pharmacistId);
        record.setPatientId(prescription.getPatientId());
        record.setDrugName(prescription.getDrugName());
        record.setQuantity(prescription.getQuantity());
        record.setStatus("已发药");
        dispenseRecordMapper.insert(record);

        result.put("success", true);
        result.put("message", "发药成功，已扣减库存");
        return result;
    }

    @Transactional(transactionManager = "smartTransactionManager")
    public Map<String, Object> addDrug(DrugInventory drug) {
        Map<String, Object> result = new HashMap<>();
        try {
            drugInventoryMapper.insert(drug);
            result.put("success", true);
            result.put("message", "药品添加成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败：" + e.getMessage());
        }
        return result;
    }

    @Transactional(transactionManager = "smartTransactionManager")
    public Map<String, Object> updateStock(Long id, int quantity) {
        Map<String, Object> result = new HashMap<>();
        DrugInventory drug = drugInventoryMapper.selectById(id);
        if (drug == null) {
            result.put("success", false);
            result.put("message", "药品不存在");
            return result;
        }
        drug.setCurrentStock(drug.getCurrentStock() + quantity);
        drugInventoryMapper.update(drug);
        result.put("success", true);
        result.put("message", "库存更新成功，当前库存：" + drug.getCurrentStock());
        return result;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void checkExpiryWarning() {
        List<DrugInventory> warnings = drugInventoryMapper.selectWarningStock();
        if (!warnings.isEmpty()) {
            System.out.println("========== 药品有效期预警 ==========");
            for (DrugInventory drug : warnings) {
                System.out.println("药品：" + drug.getDrugName() + "，有效期至：" + drug.getExpiryDate());
            }
        }
    }
}