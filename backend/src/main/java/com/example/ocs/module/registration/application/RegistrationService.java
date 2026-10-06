package com.example.ocs.module.registration.application;

import com.example.ocs.common.exception.BusinessException;
import com.example.ocs.common.util.SerialNoGenerator;
import com.example.ocs.common.web.ErrorCode;
import com.example.ocs.module.billing.application.BillingService;
import com.example.ocs.module.billing.domain.BillStatus;
import com.example.ocs.module.registration.domain.RegistrationChannel;
import com.example.ocs.module.registration.domain.RegistrationOrder;
import com.example.ocs.module.registration.domain.RegistrationStatus;
import com.example.ocs.module.registration.domain.ScheduleStatus;
import com.example.ocs.module.registration.infra.RegistrationOrderRepository;
import com.example.ocs.module.registration.infra.ScheduleRepository;
import com.example.ocs.module.user.infra.UserRepository;
import com.example.ocs.module.visit.infra.VisitRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {
  private final ScheduleRepository scheduleRepository;
  private final RegistrationOrderRepository registrationOrderRepository;
  private final UserRepository userRepository;
  private final BillingService billingService;
  private final PatientBlacklistService patientBlacklistService;
  private final VisitRepository visitRepository;

  public RegistrationService(
      ScheduleRepository scheduleRepository,
      RegistrationOrderRepository registrationOrderRepository,
      UserRepository userRepository,
      BillingService billingService,
      PatientBlacklistService patientBlacklistService,
      VisitRepository visitRepository
  ) {
    this.scheduleRepository = scheduleRepository;
    this.registrationOrderRepository = registrationOrderRepository;
    this.userRepository = userRepository;
    this.billingService = billingService;
    this.patientBlacklistService = patientBlacklistService;
    this.visitRepository = visitRepository;
  }

  @Transactional
  public RegistrationOrder book(long patientUserId, long scheduleId, RegistrationChannel channel) {
    Instant now = Instant.now();
    var blacklist = patientBlacklistService.get(patientUserId);
    if (blacklist != null && blacklist.isBlacklistedAt(now)) {
      throw new BusinessException(ErrorCode.CONFLICT, "patient is temporarily blocked due to no-shows");
    }

    var patient = userRepository.findById(patientUserId)
        .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "patient not found"));

    var schedule = scheduleRepository.findById(scheduleId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "schedule not found"));

    if (schedule.getStatus() != ScheduleStatus.OPEN) {
      throw new BusinessException(ErrorCode.CONFLICT, "schedule is not open");
    }

    if (registrationOrderRepository.existsByPatient_IdAndSchedule_IdAndStatusNot(patientUserId, scheduleId, RegistrationStatus.CANCELED)) {
      throw new BusinessException(ErrorCode.CONFLICT, "already booked this schedule");
    }

    Instant scheduleStartAt = ScheduleTimeUtil.startAt(schedule, ZoneId.systemDefault());
    if (!now.isBefore(scheduleStartAt)) {
      throw new BusinessException(ErrorCode.CONFLICT, "schedule already started");
    }

    if (schedule.getCapacityRemaining() <= 0) {
      throw new BusinessException(ErrorCode.CONFLICT, "schedule is full");
    }

    try {
      schedule.decrementCapacity();
      String serialNo = SerialNoGenerator.next("REG");
      RegistrationOrder order = new RegistrationOrder(serialNo, patient, schedule, channel, schedule.getFeeCents(), now);
      RegistrationOrder saved = registrationOrderRepository.save(order);
      billingService.createBillForRegistration(saved);
      return saved;
    } catch (OptimisticLockingFailureException e) {
      throw new BusinessException(ErrorCode.CONFLICT, "schedule changed, please retry");
    }
  }

  @Transactional
  public RegistrationOrder cancelByPatient(long patientUserId, long registrationOrderId) {
    RegistrationOrder order = registrationOrderRepository.findById(registrationOrderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "registration order not found"));
    if (!Objects.equals(order.getPatient().getId(), patientUserId)) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "not your registration order");
    }

    if (order.getStatus() == RegistrationStatus.CANCELED) {
      return order;
    }
    if (order.getStatus() == RegistrationStatus.NO_SHOW || order.getStatus() == RegistrationStatus.COMPLETED) {
      throw new BusinessException(ErrorCode.CONFLICT, "registration order cannot be canceled");
    }
    if (visitRepository.findByRegistrationOrder_Id(order.getId()).isPresent()) {
      throw new BusinessException(ErrorCode.CONFLICT, "visit already started");
    }

    Instant now = Instant.now();
    Instant scheduleStartAt = ScheduleTimeUtil.startAt(order.getSchedule(), ZoneId.systemDefault());
    if (!now.isBefore(scheduleStartAt)) {
      throw new BusinessException(ErrorCode.CONFLICT, "schedule already started");
    }

    billingService.findBySource("REGISTRATION", order.getId()).ifPresent(bill -> {
      if (bill.getStatus() == BillStatus.UNPAID) {
        billingService.voidBySource("REGISTRATION", order.getId());
      } else if (bill.getStatus() == BillStatus.PAID) {
        billingService.refundBySource("REGISTRATION", order.getId());
      }
    });

    order.cancel();
    try {
      order.getSchedule().incrementCapacity();
      scheduleRepository.save(order.getSchedule());
    } catch (IllegalStateException ignored) {
    }
    return registrationOrderRepository.save(order);
  }

  @Transactional
  public RegistrationOrder rescheduleByPatient(long patientUserId, long registrationOrderId, long targetScheduleId) {
    RegistrationOrder order = registrationOrderRepository.findById(registrationOrderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "registration order not found"));
    if (!Objects.equals(order.getPatient().getId(), patientUserId)) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "not your registration order");
    }
    if (order.getStatus() == RegistrationStatus.CANCELED) {
      throw new BusinessException(ErrorCode.CONFLICT, "registration order is canceled");
    }
    if (order.getStatus() == RegistrationStatus.NO_SHOW || order.getStatus() == RegistrationStatus.COMPLETED) {
      throw new BusinessException(ErrorCode.CONFLICT, "registration order cannot be rescheduled");
    }
    if (visitRepository.findByRegistrationOrder_Id(order.getId()).isPresent()) {
      throw new BusinessException(ErrorCode.CONFLICT, "visit already started");
    }

    Instant now = Instant.now();
    Instant scheduleStartAt = ScheduleTimeUtil.startAt(order.getSchedule(), ZoneId.systemDefault());
    if (!now.isBefore(scheduleStartAt)) {
      throw new BusinessException(ErrorCode.CONFLICT, "schedule already started");
    }

    var target = scheduleRepository.findById(targetScheduleId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "target schedule not found"));
    if (target.getStatus() != ScheduleStatus.OPEN) {
      throw new BusinessException(ErrorCode.CONFLICT, "target schedule is not open");
    }
    if (target.getCapacityRemaining() <= 0) {
      throw new BusinessException(ErrorCode.CONFLICT, "target schedule is full");
    }
    if (registrationOrderRepository.existsByPatient_IdAndSchedule_IdAndStatusNot(patientUserId, targetScheduleId, RegistrationStatus.CANCELED)) {
      throw new BusinessException(ErrorCode.CONFLICT, "already booked target schedule");
    }

    if (target.getFeeCents() != order.getFeeCents()) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "fee mismatch, reschedule is not supported for different fees");
    }

    try {
      order.getSchedule().incrementCapacity();
      target.decrementCapacity();
      scheduleRepository.save(order.getSchedule());
      scheduleRepository.save(target);
      order.reschedule(target);
      return registrationOrderRepository.save(order);
    } catch (OptimisticLockingFailureException e) {
      throw new BusinessException(ErrorCode.CONFLICT, "schedule changed, please retry");
    }
  }

  @Transactional
  public RegistrationOrder markNoShow(long actorDoctorUserId, long registrationOrderId) {
    RegistrationOrder order = registrationOrderRepository.findById(registrationOrderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "registration order not found"));

    long doctorUserId = order.getSchedule().getDoctor().getId();
    if (doctorUserId != actorDoctorUserId) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "not your registration order");
    }
    if (order.getStatus() == RegistrationStatus.CANCELED) {
      throw new BusinessException(ErrorCode.CONFLICT, "registration order is canceled");
    }
    if (visitRepository.findByRegistrationOrder_Id(order.getId()).isPresent()) {
      throw new BusinessException(ErrorCode.CONFLICT, "visit already started");
    }

    Instant now = Instant.now();
    Instant scheduleEndAt = ScheduleTimeUtil.endAt(order.getSchedule(), ZoneId.systemDefault());
    if (now.isBefore(scheduleEndAt)) {
      throw new BusinessException(ErrorCode.CONFLICT, "schedule not finished yet");
    }

    order.markNoShow();
    RegistrationOrder saved = registrationOrderRepository.save(order);
    patientBlacklistService.recordNoShow(order.getPatient().getId());
    return saved;
  }

  public List<RegistrationOrder> listByPatient(long patientUserId) {
    return registrationOrderRepository.findByPatient_Id(patientUserId);
  }

  public List<RegistrationOrder> listByDoctor(long doctorUserId) {
    return registrationOrderRepository.findBySchedule_Doctor_Id(doctorUserId);
  }
}
