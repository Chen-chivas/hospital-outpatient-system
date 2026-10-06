package com.example.ocs.module.visit.application;

import com.example.ocs.common.exception.BusinessException;
import com.example.ocs.common.web.ErrorCode;
import com.example.ocs.module.billing.application.BillingService;
import com.example.ocs.module.pharmacy.infra.DrugRepository;
import com.example.ocs.module.registration.domain.RegistrationStatus;
import com.example.ocs.module.registration.infra.RegistrationOrderRepository;
import com.example.ocs.module.visit.domain.Emr;
import com.example.ocs.module.visit.domain.Prescription;
import com.example.ocs.module.visit.domain.PrescriptionItem;
import com.example.ocs.module.visit.domain.Visit;
import com.example.ocs.module.visit.infra.EmrRepository;
import com.example.ocs.module.visit.infra.PrescriptionItemRepository;
import com.example.ocs.module.visit.infra.PrescriptionRepository;
import com.example.ocs.module.visit.infra.VisitRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisitService {
  private final RegistrationOrderRepository registrationOrderRepository;
  private final VisitRepository visitRepository;
  private final EmrRepository emrRepository;
  private final PrescriptionRepository prescriptionRepository;
  private final PrescriptionItemRepository prescriptionItemRepository;
  private final DrugRepository drugRepository;
  private final BillingService billingService;

  public VisitService(
      RegistrationOrderRepository registrationOrderRepository,
      VisitRepository visitRepository,
      EmrRepository emrRepository,
      PrescriptionRepository prescriptionRepository,
      PrescriptionItemRepository prescriptionItemRepository,
      DrugRepository drugRepository,
      BillingService billingService
  ) {
    this.registrationOrderRepository = registrationOrderRepository;
    this.visitRepository = visitRepository;
    this.emrRepository = emrRepository;
    this.prescriptionRepository = prescriptionRepository;
    this.prescriptionItemRepository = prescriptionItemRepository;
    this.drugRepository = drugRepository;
    this.billingService = billingService;
  }

  @Transactional
  public Visit startVisit(long registrationOrderId, long actorDoctorUserId) {
    var order = registrationOrderRepository.findById(registrationOrderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "registration order not found"));

    long doctorUserId = order.getSchedule().getDoctor().getId();
    if (doctorUserId != actorDoctorUserId) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "not your registration order");
    }

    if (order.getStatus() == RegistrationStatus.CANCELED || order.getStatus() == RegistrationStatus.NO_SHOW) {
      throw new BusinessException(ErrorCode.CONFLICT, "registration order is not available");
    }

    if (!billingService.isRegistrationPaid(order.getId())) {
      throw new BusinessException(ErrorCode.CONFLICT, "registration fee not paid");
    }

    return visitRepository.findByRegistrationOrder_Id(order.getId())
        .orElseGet(() -> {
          Instant now = Instant.now();
          Visit visit = new Visit(order, order.getSchedule().getDoctor(), order.getPatient(), now);
          Visit saved = visitRepository.save(visit);
          emrRepository.save(new Emr(saved, now));
          return saved;
        });
  }

  @Transactional
  public Emr updateEmr(long visitId, long actorDoctorUserId, String chiefComplaint, String hpi, String physicalExam, String diagnosis, String plan) {
    Visit visit = visitRepository.findById(visitId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "visit not found"));
    if (visit.getDoctor().getId() != actorDoctorUserId) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "not your visit");
    }

    Emr emr = emrRepository.findByVisit_Id(visitId).orElseGet(() -> emrRepository.save(new Emr(visit, Instant.now())));
    emr.update(chiefComplaint, hpi, physicalExam, diagnosis, plan);
    return emrRepository.save(emr);
  }

  @Transactional
  public Prescription issuePrescription(long visitId, long actorDoctorUserId, List<PrescriptionItemDraft> drafts) {
    if (drafts == null || drafts.isEmpty()) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "prescription items required");
    }

    Visit visit = visitRepository.findById(visitId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "visit not found"));
    if (visit.getDoctor().getId() != actorDoctorUserId) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "not your visit");
    }

    if (prescriptionRepository.findByVisit_Id(visitId).isPresent()) {
      throw new BusinessException(ErrorCode.CONFLICT, "prescription already exists");
    }

    Instant now = Instant.now();
    Prescription prescription = prescriptionRepository.save(new Prescription(visit, now));

    List<PrescriptionItem> items = new ArrayList<>();
    for (PrescriptionItemDraft draft : drafts) {
      if (draft.quantity() <= 0) {
        throw new BusinessException(ErrorCode.BAD_REQUEST, "quantity must be > 0");
      }
      var drug = drugRepository.findById(draft.drugId())
          .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "drug not found: " + draft.drugId()));
      PrescriptionItem item = new PrescriptionItem(
          prescription,
          drug,
          draft.quantity(),
          draft.dosage(),
          draft.frequency(),
          draft.days(),
          drug.getPriceCents(),
          now
      );
      items.add(item);
    }
    prescriptionItemRepository.saveAll(items);

    billingService.createBillForPrescription(prescription.getId(), visit.getPatient(), items);
    return prescription;
  }

  public record PrescriptionItemDraft(long drugId, int quantity, String dosage, String frequency, Integer days) {}

  public List<Visit> listByDoctor(long doctorUserId) {
    return visitRepository.findByDoctor_Id(doctorUserId);
  }
}
