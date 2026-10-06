package com.example.ocs.module.visit.api;

import com.example.ocs.module.visit.domain.Emr;

public record EmrResponse(long id, long visitId, String chiefComplaint, String historyPresentIllness, String physicalExam, String diagnosis, String treatmentPlan) {
  public static EmrResponse from(Emr emr) {
    return new EmrResponse(
        emr.getId(),
        emr.getVisit().getId(),
        emr.getChiefComplaint(),
        emr.getHistoryPresentIllness(),
        emr.getPhysicalExam(),
        emr.getDiagnosis(),
        emr.getTreatmentPlan()
    );
  }
}
