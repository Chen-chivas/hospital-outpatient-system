package com.example.ocs.module.registration.application;

import com.example.ocs.common.exception.BusinessException;
import com.example.ocs.common.web.ErrorCode;
import com.example.ocs.module.billing.application.BillingService;
import com.example.ocs.module.billing.domain.BillStatus;
import com.example.ocs.module.registration.domain.RegistrationStatus;
import com.example.ocs.module.registration.domain.ScheduleStatus;
import com.example.ocs.module.registration.domain.StopClinicRequest;
import com.example.ocs.module.registration.domain.StopClinicRequestStatus;
import com.example.ocs.module.registration.infra.RegistrationOrderRepository;
import com.example.ocs.module.registration.infra.ScheduleRepository;
import com.example.ocs.module.registration.infra.StopClinicRequestRepository;
import com.example.ocs.module.user.infra.UserRepository;
import com.example.ocs.module.visit.infra.VisitRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StopClinicService {
  private final StopClinicRequestRepository stopClinicRequestRepository;
  private final ScheduleRepository scheduleRepository;
  private final UserRepository userRepository;
  private final RegistrationOrderRepository registrationOrderRepository;
  private final VisitRepository visitRepository;
  private final BillingService billingService;

  public StopClinicService(
      StopClinicRequestRepository stopClinicRequestRepository,
      ScheduleRepository scheduleRepository,
      UserRepository userRepository,
      RegistrationOrderRepository registrationOrderRepository,
      VisitRepository visitRepository,
      BillingService billingService
  ) {
    this.stopClinicRequestRepository = stopClinicRequestRepository;
    this.scheduleRepository = scheduleRepository;
    this.userRepository = userRepository;
    this.registrationOrderRepository = registrationOrderRepository;
    this.visitRepository = visitRepository;
    this.billingService = billingService;
  }

  @Transactional
  public StopClinicRequest create(long doctorUserId, long scheduleId, String reason) {
    var doctor = userRepository.findById(doctorUserId)
        .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "doctor not found"));
    var schedule = scheduleRepository.findById(scheduleId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "schedule not found"));

    if (schedule.getDoctor().getId() != doctorUserId) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "not your schedule");
    }
    if (schedule.getStatus() != ScheduleStatus.OPEN) {
      throw new BusinessException(ErrorCode.CONFLICT, "schedule is not open");
    }

    stopClinicRequestRepository.findTop1BySchedule_IdOrderByCreatedAtDesc(scheduleId).ifPresent(latest -> {
      if (latest.getStatus() == StopClinicRequestStatus.PENDING || latest.getStatus() == StopClinicRequestStatus.APPROVED) {
        throw new BusinessException(ErrorCode.CONFLICT, "stop-clinic request already exists for this schedule");
      }
    });

    Instant now = Instant.now();
    StopClinicRequest request = new StopClinicRequest(schedule, doctor, reason, now);
    return stopClinicRequestRepository.save(request);
  }

  public List<StopClinicRequest> listPending() {
    return stopClinicRequestRepository.findByStatusOrderByCreatedAtDesc(StopClinicRequestStatus.PENDING);
  }

  public List<StopClinicRequest> listByDoctor(long doctorUserId) {
    return stopClinicRequestRepository.findByDoctor_IdOrderByCreatedAtDesc(doctorUserId);
  }

  @Transactional
  public StopClinicRequest approve(long requestId) {
    StopClinicRequest request = stopClinicRequestRepository.findById(requestId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "stop-clinic request not found"));
    if (request.getStatus() != StopClinicRequestStatus.PENDING) {
      throw new BusinessException(ErrorCode.CONFLICT, "request is not pending");
    }

    request.approve();
    var schedule = request.getSchedule();
    schedule.close();

    var orders = registrationOrderRepository.findBySchedule_IdAndStatusIn(
        schedule.getId(),
        List.of(RegistrationStatus.CREATED, RegistrationStatus.PAID)
    );
    for (var order : orders) {
      if (visitRepository.findByRegistrationOrder_Id(order.getId()).isPresent()) {
        throw new BusinessException(ErrorCode.CONFLICT, "cannot stop clinic: visit already started");
      }
    }

    for (var order : orders) {
      billingService.findBySource("REGISTRATION", order.getId()).ifPresent(bill -> {
        if (bill.getStatus() == BillStatus.UNPAID) {
          billingService.voidBySource("REGISTRATION", order.getId());
        } else if (bill.getStatus() == BillStatus.PAID) {
          billingService.refundBySource("REGISTRATION", order.getId());
        }
      });
      order.cancel();
      try {
        schedule.incrementCapacity();
      } catch (IllegalStateException ignored) {
      }
      registrationOrderRepository.save(order);
    }

    stopClinicRequestRepository.save(request);
    scheduleRepository.save(schedule);
    return request;
  }

  @Transactional
  public StopClinicRequest reject(long requestId) {
    StopClinicRequest request = stopClinicRequestRepository.findById(requestId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "stop-clinic request not found"));
    if (request.getStatus() != StopClinicRequestStatus.PENDING) {
      throw new BusinessException(ErrorCode.CONFLICT, "request is not pending");
    }
    request.reject();
    return stopClinicRequestRepository.save(request);
  }
}
