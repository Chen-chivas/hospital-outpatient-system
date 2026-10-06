package com.example.ocs.module.pharmacy.infra;

import com.example.ocs.module.pharmacy.domain.DispenseRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispenseRecordRepository extends JpaRepository<DispenseRecord, Long> {
  List<DispenseRecord> findByPharmacist_Id(long pharmacistUserId);
}

