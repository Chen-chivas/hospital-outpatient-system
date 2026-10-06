package com.example.ocs.module.registration.infra;

import com.example.ocs.module.registration.domain.StopClinicRequest;
import com.example.ocs.module.registration.domain.StopClinicRequestStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StopClinicRequestRepository extends JpaRepository<StopClinicRequest, Long> {
  List<StopClinicRequest> findByStatusOrderByCreatedAtDesc(StopClinicRequestStatus status);

  List<StopClinicRequest> findByDoctor_IdOrderByCreatedAtDesc(long doctorUserId);

  Optional<StopClinicRequest> findTop1BySchedule_IdOrderByCreatedAtDesc(long scheduleId);
}

