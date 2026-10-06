package com.example.ocs.module.visit.infra;

import com.example.ocs.module.visit.domain.Emr;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmrRepository extends JpaRepository<Emr, Long> {
  Optional<Emr> findByVisit_Id(long visitId);
}

