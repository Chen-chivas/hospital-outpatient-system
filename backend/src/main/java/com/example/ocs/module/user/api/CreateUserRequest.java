package com.example.ocs.module.user.api;

import com.example.ocs.module.user.domain.PatientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CreateUserRequest(
    @NotBlank @Size(max = 64) String username,
    @NotBlank @Size(min = 6, max = 64) String password,
    @NotBlank @Size(max = 64) String displayName,
    Set<@Size(max = 32) String> roles,
    PatientType patientType
) {}
