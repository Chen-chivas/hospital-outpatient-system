package com.example.ocs.module.billing.infra;

import com.example.ocs.module.billing.domain.BillItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillItemRepository extends JpaRepository<BillItem, Long> {
  List<BillItem> findByBill_Id(long billId);

  List<BillItem> findByBill_IdIn(List<Long> billIds);
}
