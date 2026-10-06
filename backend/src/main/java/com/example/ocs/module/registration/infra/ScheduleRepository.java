package com.example.ocs.module.registration.infra;

import com.example.ocs.module.registration.domain.Schedule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
  List<Schedule> findByScheduleDate(LocalDate scheduleDate);
}

