package com.example.ocs.module.billing.infra;

import com.example.ocs.module.billing.domain.Bill;
import com.example.ocs.module.billing.domain.BillStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BillRepository extends JpaRepository<Bill, Long> {
  Optional<Bill> findBySerialNo(String serialNo);

  Optional<Bill> findBySourceTypeAndSourceId(String sourceType, long sourceId);

  List<Bill> findByPatient_Id(long patientUserId);

  List<Bill> findByStatus(BillStatus status);

  List<Bill> findByStatusAndCreatedAtGreaterThanEqual(BillStatus status, Instant createdAt);

  @Query("select coalesce(sum(b.amountTotalCents), 0) from Bill b where b.status = :status")
  long sumAmountTotalCentsByStatus(@Param("status") BillStatus status);
}
