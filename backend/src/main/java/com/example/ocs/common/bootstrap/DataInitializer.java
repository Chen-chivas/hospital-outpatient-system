package com.example.ocs.common.bootstrap;

import com.example.ocs.module.user.domain.PatientType;
import com.example.ocs.module.user.domain.Role;
import com.example.ocs.module.user.domain.User;
import com.example.ocs.module.user.domain.UserStatus;
import com.example.ocs.module.registration.domain.Schedule;
import com.example.ocs.module.registration.domain.TimePeriod;
import com.example.ocs.module.registration.infra.ScheduleRepository;
import com.example.ocs.module.pharmacy.domain.Drug;
import com.example.ocs.module.pharmacy.domain.Inventory;
import com.example.ocs.module.pharmacy.infra.DrugRepository;
import com.example.ocs.module.pharmacy.infra.InventoryRepository;
import com.example.ocs.module.user.infra.RoleRepository;
import com.example.ocs.module.user.infra.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final DrugRepository drugRepository;
  private final InventoryRepository inventoryRepository;
  private final ScheduleRepository scheduleRepository;

  public DataInitializer(
      RoleRepository roleRepository,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      DrugRepository drugRepository,
      InventoryRepository inventoryRepository,
      ScheduleRepository scheduleRepository
  ) {
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.drugRepository = drugRepository;
    this.inventoryRepository = inventoryRepository;
    this.scheduleRepository = scheduleRepository;
  }

  @Override
  @Transactional
  public void run(String... args) {
    Instant now = Instant.now();

    Map<String, String> roles = Map.of(
        "ADMIN", "管理员",
        "DOCTOR", "医生",
        "NURSE", "护士",
        "CASHIER", "收费员",
        "PHARMACIST", "药师",
        "PATIENT", "患者"
    );

    roles.forEach((code, name) -> roleRepository.findByCode(code).orElseGet(() -> roleRepository.save(new Role(code, name, now))));

    ensureUser("admin", "admin123", "系统管理员", "ADMIN");
    ensureUser("doctor1", "doctor123", "张医生", "DOCTOR");
    ensureUser("cashier1", "cashier123", "收费员1", "CASHIER");
    ensureUser("pharmacist1", "pharmacist123", "药师1", "PHARMACIST");
    ensureUser("patient1", "patient123", "患者1", "PATIENT", PatientType.SELF_PAY);
    ensureUser("patient2", "patient123", "患者2", "PATIENT", PatientType.INSURANCE);
    ensureUser("patient3", "patient123", "患者3", "PATIENT", PatientType.COMMERCIAL);

    ensureDrug("D001", "阿莫西林胶囊", "0.5g*24粒", "盒", 1500, 200);
    ensureDrug("D002", "对乙酰氨基酚片", "0.5g*20片", "盒", 800, 300);

    ensureSchedule("doctor1", LocalDate.now(), TimePeriod.AM, 500, 20);
  }

  private void ensureUser(String username, String rawPassword, String displayName, String roleCode) {
    ensureUser(username, rawPassword, displayName, roleCode, null);
  }

  private void ensureUser(String username, String rawPassword, String displayName, String roleCode, PatientType patientType) {
    if (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
      return;
    }

    Instant now = Instant.now();
    User user = new User(username, passwordEncoder.encode(rawPassword), displayName, UserStatus.ACTIVE, now);
    if (patientType != null) {
      user.setPatientType(patientType);
    }
    Role role = roleRepository.findByCode(roleCode).orElseThrow();
    user.getRoles().add(role);
    userRepository.save(user);
  }

  private void ensureDrug(String code, String name, String spec, String unit, long priceCents, int initQty) {
    if (drugRepository.findByCode(code).isPresent()) {
      return;
    }
    Instant now = Instant.now();
    Drug drug = drugRepository.save(new Drug(code, name, spec, unit, priceCents, now));
    inventoryRepository.save(new Inventory(drug, initQty, now));
  }

  private void ensureSchedule(String doctorUsername, LocalDate date, TimePeriod timePeriod, long feeCents, int capacityTotal) {
    var doctor = userRepository.findByUsernameIgnoreCase(doctorUsername).orElse(null);
    if (doctor == null) {
      return;
    }
    boolean exists = scheduleRepository.findByScheduleDate(date).stream()
        .anyMatch(s -> s.getDoctor().getId().equals(doctor.getId()) && s.getTimePeriod() == timePeriod);
    if (exists) {
      return;
    }
    scheduleRepository.save(new Schedule(doctor, date, timePeriod, feeCents, capacityTotal, Instant.now()));
  }
}
