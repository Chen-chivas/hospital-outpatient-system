package com.example.ocs.module.pharmacy.application;

import com.example.ocs.common.exception.BusinessException;
import com.example.ocs.common.web.ErrorCode;
import com.example.ocs.module.pharmacy.domain.Drug;
import com.example.ocs.module.pharmacy.domain.DispenseRecord;
import com.example.ocs.module.pharmacy.domain.Inventory;
import com.example.ocs.module.pharmacy.infra.DrugRepository;
import com.example.ocs.module.pharmacy.infra.DispenseRecordRepository;
import com.example.ocs.module.pharmacy.infra.InventoryRepository;
import com.example.ocs.module.billing.application.BillingService;
import com.example.ocs.module.user.infra.UserRepository;
import com.example.ocs.module.visit.domain.PrescriptionStatus;
import com.example.ocs.module.visit.infra.PrescriptionItemRepository;
import com.example.ocs.module.visit.infra.PrescriptionRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PharmacyService {
  private final DrugRepository drugRepository;
  private final InventoryRepository inventoryRepository;
  private final PrescriptionRepository prescriptionRepository;
  private final PrescriptionItemRepository prescriptionItemRepository;
  private final UserRepository userRepository;
  private final DispenseRecordRepository dispenseRecordRepository;
  private final BillingService billingService;

  public PharmacyService(
      DrugRepository drugRepository,
      InventoryRepository inventoryRepository,
      PrescriptionRepository prescriptionRepository,
      PrescriptionItemRepository prescriptionItemRepository,
      UserRepository userRepository,
      DispenseRecordRepository dispenseRecordRepository,
      BillingService billingService
  ) {
    this.drugRepository = drugRepository;
    this.inventoryRepository = inventoryRepository;
    this.prescriptionRepository = prescriptionRepository;
    this.prescriptionItemRepository = prescriptionItemRepository;
    this.userRepository = userRepository;
    this.dispenseRecordRepository = dispenseRecordRepository;
    this.billingService = billingService;
  }

  @Transactional
  public Drug createDrug(String code, String name, String spec, String unit, long priceCents) {
    if (drugRepository.existsByCode(code)) {
      throw new BusinessException(ErrorCode.CONFLICT, "drug code already exists");
    }
    Instant now = Instant.now();
    Drug drug = new Drug(code, name, spec, unit, priceCents, now);
    Drug saved = drugRepository.save(drug);
    inventoryRepository.save(new Inventory(saved, 0, now));
    return saved;
  }

  public List<Drug> listDrugs() {
    return drugRepository.findAll();
  }

  @Transactional
  public Inventory adjustInventory(long drugId, int delta) {
    Inventory inv = inventoryRepository.findByDrug_Id(drugId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "inventory not found"));
    if (delta >= 0) {
      inv.add(delta);
    } else {
      inv.subtract(-delta);
    }
    return inventoryRepository.save(inv);
  }

  public Inventory getInventory(long drugId) {
    return inventoryRepository.findByDrug_Id(drugId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "inventory not found"));
  }

  @Transactional
  public DispenseRecord dispense(long prescriptionId, long pharmacistUserId) {
    if (!billingService.isPrescriptionPaid(prescriptionId)) {
      throw new BusinessException(ErrorCode.CONFLICT, "prescription bill not paid");
    }

    var prescription = prescriptionRepository.findById(prescriptionId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "prescription not found"));
    if (prescription.getStatus() != PrescriptionStatus.ISSUED) {
      throw new BusinessException(ErrorCode.CONFLICT, "prescription is not issuable");
    }

    var pharmacist = userRepository.findById(pharmacistUserId)
        .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "pharmacist not found"));

    var items = prescriptionItemRepository.findByPrescription_Id(prescriptionId);
    for (var item : items) {
      var inv = inventoryRepository.findByDrug_Id(item.getDrug().getId())
          .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "inventory not found"));
      try {
        inv.subtract(item.getQuantity());
      } catch (IllegalStateException e) {
        throw new BusinessException(ErrorCode.CONFLICT, "insufficient inventory: " + item.getDrug().getName());
      }
      inventoryRepository.save(inv);
    }

    prescription.markDispensed();
    prescriptionRepository.save(prescription);

    Instant now = Instant.now();
    DispenseRecord record = new DispenseRecord(prescription, pharmacist, now);
    return dispenseRecordRepository.save(record);
  }
}
