package com.example.ocs.module.registration.application;

import com.example.ocs.common.exception.BusinessException;
import com.example.ocs.common.web.ErrorCode;
import com.example.ocs.module.registration.domain.PatientBlacklist;
import com.example.ocs.module.registration.infra.PatientBlacklistRepository;
import com.example.ocs.module.user.infra.UserRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientBlacklistService {
  private final PatientBlacklistRepository patientBlacklistRepository;
  private final UserRepository userRepository;

  public PatientBlacklistService(PatientBlacklistRepository patientBlacklistRepository, UserRepository userRepository) {
    this.patientBlacklistRepository = patientBlacklistRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public PatientBlacklist getOrCreate(long patientUserId) {
    return patientBlacklistRepository.findById(patientUserId)
        .orElseGet(() -> {
          var patient = userRepository.findById(patientUserId)
              .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "patient not found"));
          PatientBlacklist created = new PatientBlacklist(patient, Instant.now());
          return patientBlacklistRepository.save(created);
        });
  }

  public PatientBlacklist get(long patientUserId) {
    return patientBlacklistRepository.findById(patientUserId).orElse(null);
  }

  @Transactional
  public PatientBlacklist recordNoShow(long patientUserId) {
    Instant now = Instant.now();
    PatientBlacklist blacklist = getOrCreate(patientUserId);
    blacklist.recordNoShow(now);
    return patientBlacklistRepository.save(blacklist);
  }
}
