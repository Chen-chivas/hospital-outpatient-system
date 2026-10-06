package com.example.ocs.module.registration.api;

import com.example.ocs.module.registration.domain.PatientBlacklist;
import java.time.Instant;

public record PatientBlacklistResponse(
    long patientUserId,
    int noShowCount,
    Instant lastNoShowAt,
    Instant blacklistedUntil,
    Instant updatedAt
) {
  public static PatientBlacklistResponse from(PatientBlacklist blacklist) {
    return new PatientBlacklistResponse(
        blacklist.getPatientUserId(),
        blacklist.getNoShowCount(),
        blacklist.getLastNoShowAt(),
        blacklist.getBlacklistedUntil(),
        blacklist.getUpdatedAt()
    );
  }
}

