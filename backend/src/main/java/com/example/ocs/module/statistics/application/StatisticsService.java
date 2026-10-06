package com.example.ocs.module.statistics.application;

import com.example.ocs.module.billing.domain.BillStatus;
import com.example.ocs.module.billing.domain.PaymentMethod;
import com.example.ocs.module.billing.infra.BillItemRepository;
import com.example.ocs.module.billing.infra.BillRepository;
import com.example.ocs.module.registration.infra.RegistrationOrderRepository;
import com.example.ocs.module.registration.infra.ScheduleRepository;
import com.example.ocs.module.statistics.api.PieSliceResponse;
import com.example.ocs.module.statistics.api.StatisticsDistributionsResponse;
import com.example.ocs.module.statistics.api.StatisticsSummaryResponse;
import com.example.ocs.module.statistics.api.TrendPointResponse;
import com.example.ocs.module.statistics.api.TrendsResponse;
import com.example.ocs.module.user.domain.PatientType;
import com.example.ocs.module.user.infra.UserRepository;
import com.example.ocs.module.visit.infra.PrescriptionRepository;
import com.example.ocs.module.visit.infra.VisitRepository;
import com.hospital.outpatient.mapper.RegistrationOrderMapper;
import com.hospital.outpatient.mapper.ScheduleMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StatisticsService {
  private final UserRepository userRepository;
  private final ScheduleRepository scheduleRepository;
  private final RegistrationOrderRepository registrationOrderRepository;
  private final VisitRepository visitRepository;
  private final PrescriptionRepository prescriptionRepository;
  private final BillRepository billRepository;
  private final BillItemRepository billItemRepository;
  private final RegistrationOrderMapper outpatientRegistrationOrderMapper;
  private final ScheduleMapper outpatientScheduleMapper;

  public StatisticsService(
      UserRepository userRepository,
      ScheduleRepository scheduleRepository,
      RegistrationOrderRepository registrationOrderRepository,
      VisitRepository visitRepository,
      PrescriptionRepository prescriptionRepository,
      BillRepository billRepository,
      BillItemRepository billItemRepository,
      RegistrationOrderMapper outpatientRegistrationOrderMapper,
      ScheduleMapper outpatientScheduleMapper
  ) {
    this.userRepository = userRepository;
    this.scheduleRepository = scheduleRepository;
    this.registrationOrderRepository = registrationOrderRepository;
    this.visitRepository = visitRepository;
    this.prescriptionRepository = prescriptionRepository;
    this.billRepository = billRepository;
    this.billItemRepository = billItemRepository;
    this.outpatientRegistrationOrderMapper = outpatientRegistrationOrderMapper;
    this.outpatientScheduleMapper = outpatientScheduleMapper;
  }

  public StatisticsSummaryResponse summary() {
    // 合并主模块和门诊模块的数据
    long totalRegistrations = registrationOrderRepository.count()
        + outpatientRegistrationOrderMapper.selectCount(null);
    long totalSchedules = scheduleRepository.count()
        + outpatientScheduleMapper.selectCount(null);

    return new StatisticsSummaryResponse(
        userRepository.count(),
        totalSchedules,
        totalRegistrations,
        visitRepository.count(),
        prescriptionRepository.count(),
        billRepository.count(),
        billRepository.sumAmountTotalCentsByStatus(BillStatus.PAID)
    );
  }

  public TrendsResponse trends(int days) {
    int finalDays = Math.max(7, Math.min(days, 90));
    ZoneId zone = ZoneId.systemDefault();
    LocalDate today = LocalDate.now(zone);
    LocalDate start = today.minusDays(finalDays - 1L);
    Instant startInstant = start.atStartOfDay(zone).toInstant();

    Map<LocalDate, Long> regCount = new HashMap<>();
    registrationOrderRepository.findByCreatedAtGreaterThanEqual(startInstant).forEach(o -> {
      LocalDate d = o.getCreatedAt().atZone(zone).toLocalDate();
      regCount.merge(d, 1L, Long::sum);
    });

    Map<LocalDate, Long> revenuePaid = new HashMap<>();
    billRepository.findByStatusAndCreatedAtGreaterThanEqual(BillStatus.PAID, startInstant).forEach(b -> {
      LocalDate d = b.getCreatedAt().atZone(zone).toLocalDate();
      revenuePaid.merge(d, b.getAmountTotalCents(), Long::sum);
    });

    List<TrendPointResponse> points = new ArrayList<>(finalDays);
    for (int i = 0; i < finalDays; i++) {
      LocalDate d = start.plusDays(i);
      points.add(new TrendPointResponse(d.toString(), regCount.getOrDefault(d, 0L), revenuePaid.getOrDefault(d, 0L)));
    }
    return new TrendsResponse(finalDays, points);
  }

  public StatisticsDistributionsResponse distributions(int days) {
    int finalDays = Math.max(7, Math.min(days, 90));
    ZoneId zone = ZoneId.systemDefault();
    LocalDate today = LocalDate.now(zone);
    LocalDate start = today.minusDays(finalDays - 1L);
    Instant startInstant = start.atStartOfDay(zone).toInstant();

    var paidBills = billRepository.findByStatusAndCreatedAtGreaterThanEqual(BillStatus.PAID, startInstant);

    List<PieSliceResponse> expenseStructure = buildExpenseStructure(paidBills);
    List<PieSliceResponse> paymentMethods = buildPaymentMethods(paidBills);

    var orders = registrationOrderRepository.findByCreatedAtGreaterThanEqual(startInstant);
    Set<Long> patientUserIds = orders.stream().map(o -> o.getPatient().getId()).filter(Objects::nonNull).collect(Collectors.toSet());
    Map<PatientType, Long> patientTypeCounts = new HashMap<>();
    if (!patientUserIds.isEmpty()) {
      userRepository.findAllById(patientUserIds).forEach(u -> {
        PatientType t = u.getPatientType() == null ? PatientType.UNKNOWN : u.getPatientType();
        patientTypeCounts.merge(t, 1L, Long::sum);
      });
    }
    List<PieSliceResponse> patientTypes = patientTypeCounts.entrySet().stream()
        .map(e -> new PieSliceResponse(patientTypeLabel(e.getKey()), e.getValue()))
        .sorted(Comparator.comparingLong(PieSliceResponse::value).reversed())
        .toList();

    return new StatisticsDistributionsResponse(finalDays, expenseStructure, patientTypes, paymentMethods);
  }

  private List<PieSliceResponse> buildExpenseStructure(List<com.example.ocs.module.billing.domain.Bill> paidBills) {
    List<Long> paidBillIds = paidBills.stream().map(com.example.ocs.module.billing.domain.Bill::getId).filter(Objects::nonNull).toList();
    if (paidBillIds.isEmpty()) {
      return List.of();
    }
    var items = billItemRepository.findByBill_IdIn(paidBillIds);
    Map<String, Long> amountByType = new HashMap<>();
    items.forEach(i -> amountByType.merge(i.getItemType(), i.getAmountCents(), Long::sum));
    return amountByType.entrySet().stream()
        .map(e -> new PieSliceResponse(expenseTypeLabel(e.getKey()), e.getValue()))
        .sorted(Comparator.comparingLong(PieSliceResponse::value).reversed())
        .toList();
  }

  private List<PieSliceResponse> buildPaymentMethods(List<com.example.ocs.module.billing.domain.Bill> paidBills) {
    Map<PaymentMethod, Long> amountByMethod = new HashMap<>();
    paidBills.forEach(b -> {
      PaymentMethod m = b.getPaymentMethod() == null ? PaymentMethod.UNKNOWN : b.getPaymentMethod();
      amountByMethod.merge(m, b.getAmountTotalCents(), Long::sum);
    });
    return amountByMethod.entrySet().stream()
        .map(e -> new PieSliceResponse(paymentMethodLabel(e.getKey()), e.getValue()))
        .sorted(Comparator.comparingLong(PieSliceResponse::value).reversed())
        .toList();
  }

  private String expenseTypeLabel(String itemType) {
    if (itemType == null) return "其他";
    return switch (itemType) {
      case "REG_FEE" -> "挂号费";
      case "DRUG" -> "药品费";
      default -> itemType;
    };
  }

  private String paymentMethodLabel(PaymentMethod method) {
    if (method == null) return "未知";
    return switch (method) {
      case CASH -> "现金";
      case WECHAT -> "微信";
      case ALIPAY -> "支付宝";
      case CARD -> "银行卡";
      case INSURANCE -> "医保";
      case OTHER -> "其他";
      case UNKNOWN -> "未知";
    };
  }

  private String patientTypeLabel(PatientType type) {
    if (type == null) return "未知";
    return switch (type) {
      case SELF_PAY -> "自费";
      case INSURANCE -> "医保";
      case COMMERCIAL -> "商保";
      case OTHER -> "其他";
      case UNKNOWN -> "未知";
    };
  }
}
