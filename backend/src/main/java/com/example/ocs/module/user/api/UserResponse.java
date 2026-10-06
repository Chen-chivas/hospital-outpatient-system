package com.example.ocs.module.user.api;

import com.example.ocs.module.user.domain.User;
import java.time.Instant;
import java.util.Set;

public record UserResponse(
    long id,
    String username,
    String displayName,
    String status,
    Set<String> roles,
    String patientType,
    Instant createdAt,
    Instant updatedAt
) {
  public static UserResponse from(User user) {
    var roleCodes = user.getRoles().stream().map(r -> r.getCode()).collect(java.util.stream.Collectors.toSet());
    return new UserResponse(
        user.getId(),
        user.getUsername(),
        user.getDisplayName(),
        user.getStatus().name(),
        roleCodes,
        user.getPatientType() == null ? null : user.getPatientType().name(),
        user.getCreatedAt(),
        user.getUpdatedAt()
    );
  }
}
