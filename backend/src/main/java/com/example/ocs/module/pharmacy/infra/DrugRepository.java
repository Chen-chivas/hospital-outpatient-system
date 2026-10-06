package com.example.ocs.module.pharmacy.infra;

import com.example.ocs.module.pharmacy.domain.Drug;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrugRepository extends JpaRepository<Drug, Long> {
  Optional<Drug> findByCode(String code);

  boolean existsByCode(String code);
}

