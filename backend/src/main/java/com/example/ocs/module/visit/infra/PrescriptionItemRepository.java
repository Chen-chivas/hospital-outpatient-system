package com.example.ocs.module.visit.infra;

import com.example.ocs.module.visit.domain.PrescriptionItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {
  List<PrescriptionItem> findByPrescription_Id(long prescriptionId);
}

