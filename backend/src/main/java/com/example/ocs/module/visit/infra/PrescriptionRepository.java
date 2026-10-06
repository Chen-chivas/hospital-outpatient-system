package com.example.ocs.module.visit.infra;

import com.example.ocs.module.visit.domain.Prescription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
  Optional<Prescription> findByVisit_Id(long visitId);

  List<Prescription> findAllByOrderByCreatedAtDesc();
}
