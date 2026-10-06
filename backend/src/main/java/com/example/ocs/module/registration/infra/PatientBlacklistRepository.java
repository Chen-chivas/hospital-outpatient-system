package com.example.ocs.module.registration.infra;

import com.example.ocs.module.registration.domain.PatientBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientBlacklistRepository extends JpaRepository<PatientBlacklist, Long> {}

