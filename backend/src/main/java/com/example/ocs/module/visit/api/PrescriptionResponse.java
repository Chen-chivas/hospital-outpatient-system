package com.example.ocs.module.visit.api;

import java.util.List;

public record PrescriptionResponse(long id, long visitId, String status, List<PrescriptionItemResponse> items) {}

