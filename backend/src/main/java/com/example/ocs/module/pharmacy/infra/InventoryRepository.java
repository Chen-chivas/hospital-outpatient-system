package com.example.ocs.module.pharmacy.infra;

import com.example.ocs.module.pharmacy.domain.Inventory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
  Optional<Inventory> findByDrug_Id(long drugId);
}

