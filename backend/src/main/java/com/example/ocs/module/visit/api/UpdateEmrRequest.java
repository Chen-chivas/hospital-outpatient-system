package com.example.ocs.module.visit.api;

public record UpdateEmrRequest(
    String chiefComplaint,
    String historyPresentIllness,
    String physicalExam,
    String diagnosis,
    String treatmentPlan
) {}

