package com.example.ocs.module.registration.infra;

import com.example.ocs.module.registration.domain.RegistrationOrder;
import com.example.ocs.module.registration.domain.RegistrationStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationOrderRepository extends JpaRepository<RegistrationOrder, Long> {
  Optional<RegistrationOrder> findBySerialNo(String serialNo);

  List<RegistrationOrder> findByPatient_Id(long patientUserId);

  List<RegistrationOrder> findBySchedule_Doctor_Id(long doctorUserId);

  List<RegistrationOrder> findBySchedule_Id(long scheduleId);

  List<RegistrationOrder> findBySchedule_IdAndStatusIn(long scheduleId, List<RegistrationStatus> statuses);

  boolean existsByPatient_IdAndSchedule_IdAndStatusNot(long patientUserId, long scheduleId, RegistrationStatus status);

  List<RegistrationOrder> findByCreatedAtGreaterThanEqual(Instant createdAt);
}
