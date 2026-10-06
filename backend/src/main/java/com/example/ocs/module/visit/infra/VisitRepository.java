package com.example.ocs.module.visit.infra;

import com.example.ocs.module.visit.domain.Visit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRepository extends JpaRepository<Visit, Long> {
  Optional<Visit> findByRegistrationOrder_Id(long registrationOrderId);

  List<Visit> findByDoctor_Id(long doctorUserId);
}

