package com.example.ocs.module.visit.api;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record IssuePrescriptionRequest(@NotEmpty List<PrescriptionItemRequest> items) {}

