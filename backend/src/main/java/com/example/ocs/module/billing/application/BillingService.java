package com.example.ocs.module.billing.application;

import com.example.ocs.common.exception.BusinessException;
import com.example.ocs.common.util.SerialNoGenerator;
import com.example.ocs.common.web.ErrorCode;
import com.example.ocs.module.billing.domain.Bill;
import com.example.ocs.module.billing.domain.BillItem;
import com.example.ocs.module.billing.domain.BillStatus;
import com.example.ocs.module.billing.domain.PaymentMethod;
import com.example.ocs.module.billing.infra.BillItemRepository;
import com.example.ocs.module.billing.infra.BillRepository;
import com.example.ocs.module.registration.domain.RegistrationOrder;
import com.example.ocs.module.registration.infra.RegistrationOrderRepository;
import com.example.ocs.module.user.domain.User;
import com.example.ocs.module.visit.domain.PrescriptionItem;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingService {
  private final BillRepository billRepository;
  private final BillItemRepository billItemRepository;
  private final RegistrationOrderRepository registrationOrderRepository;

  public BillingService(
      BillRepository billRepository,
      BillItemRepository billItemRepository,
      RegistrationOrderRepository registrationOrderRepository
  ) {
    this.billRepository = billRepository;
    this.billItemRepository = billItemRepository;
    this.registrationOrderRepository = registrationOrderRepository;
  }

  @Transactional
  public Bill createBillForRegistration(RegistrationOrder order) {
    return billRepository.findBySourceTypeAndSourceId("REGISTRATION", order.getId())
        .orElseGet(() -> {
          Instant now = Instant.now();
          Bill bill = new Bill(SerialNoGenerator.next("BILL"), order.getPatient(), "REGISTRATION", order.getId(), order.getFeeCents(), now);
          Bill saved = billRepository.save(bill);
          billItemRepository.save(new BillItem(saved, "REG_FEE", "挂号费", 1, order.getFeeCents(), now));
          return saved;
        });
  }

  @Transactional
  public Bill createBillForPrescription(long prescriptionId, User patient, List<PrescriptionItem> items) {
    return billRepository.findBySourceTypeAndSourceId("PRESCRIPTION", prescriptionId)
        .orElseGet(() -> {
          long total = items.stream().mapToLong(i -> i.getUnitPriceCents() * (long) i.getQuantity()).sum();
          Instant now = Instant.now();
          Bill bill = new Bill(SerialNoGenerator.next("BILL"), patient, "PRESCRIPTION", prescriptionId, total, now);
          Bill saved = billRepository.save(bill);
          for (PrescriptionItem item : items) {
            billItemRepository.save(new BillItem(
                saved,
                "DRUG",
                item.getDrug().getName(),
                item.getQuantity(),
                item.getUnitPriceCents(),
                now
            ));
          }
          return saved;
        });
  }

  public boolean isRegistrationPaid(long registrationOrderId) {
    return billRepository.findBySourceTypeAndSourceId("REGISTRATION", registrationOrderId)
        .map(b -> b.getStatus() == BillStatus.PAID)
        .orElse(false);
  }

  public boolean isPrescriptionPaid(long prescriptionId) {
    return billRepository.findBySourceTypeAndSourceId("PRESCRIPTION", prescriptionId)
        .map(b -> b.getStatus() == BillStatus.PAID)
        .orElse(false);
  }

  public List<Bill> listByPatient(long patientUserId) {
    return billRepository.findByPatient_Id(patientUserId);
  }

  public List<Bill> listByStatus(BillStatus status) {
    return billRepository.findByStatus(status);
  }

  public Bill getById(long billId) {
    return billRepository.findById(billId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "bill not found"));
  }

  public Optional<Bill> findBySource(String sourceType, long sourceId) {
    return billRepository.findBySourceTypeAndSourceId(sourceType, sourceId);
  }

  public List<BillItem> listItems(long billId) {
    return billItemRepository.findByBill_Id(billId);
  }

  @Transactional
  public Bill pay(long billId, PaymentMethod method) {
    Bill bill = getById(billId);
    if (bill.getStatus() != BillStatus.UNPAID) {
      throw new BusinessException(ErrorCode.CONFLICT, "bill is not unpaid");
    }
    PaymentMethod finalMethod = method == null ? PaymentMethod.UNKNOWN : method;
    bill.markPaid(finalMethod);
    Bill saved = billRepository.save(bill);
    if ("REGISTRATION".equals(saved.getSourceType())) {
      registrationOrderRepository.findById(saved.getSourceId()).ifPresent(order -> {
        order.markPaid();
        registrationOrderRepository.save(order);
      });
    }
    return saved;
  }

  @Transactional
  public Bill voidBySource(String sourceType, long sourceId) {
    Bill bill = billRepository.findBySourceTypeAndSourceId(sourceType, sourceId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "bill not found"));
    if (bill.getStatus() == BillStatus.PAID) {
      throw new BusinessException(ErrorCode.CONFLICT, "cannot void a paid bill, refund instead");
    }
    if (bill.getStatus() == BillStatus.VOIDED) {
      return bill;
    }
    bill.markVoided();
    return billRepository.save(bill);
  }

  @Transactional
  public Bill refundBySource(String sourceType, long sourceId) {
    Bill bill = billRepository.findBySourceTypeAndSourceId(sourceType, sourceId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "bill not found"));
    if (bill.getStatus() != BillStatus.PAID) {
      throw new BusinessException(ErrorCode.CONFLICT, "bill is not paid");
    }
    bill.markRefunded();
    return billRepository.save(bill);
  }
}
