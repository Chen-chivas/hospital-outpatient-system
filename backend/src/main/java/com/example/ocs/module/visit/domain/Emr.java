package com.example.ocs.module.visit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "emrs")
public class Emr {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "visit_id", nullable = false, unique = true)
  private Visit visit;

  @Column(name = "chief_complaint")
  @Lob
  private String chiefComplaint;

  @Column(name = "history_present_illness")
  @Lob
  private String historyPresentIllness;

  @Column(name = "physical_exam")
  @Lob
  private String physicalExam;

  @Column(name = "diagnosis")
  @Lob
  private String diagnosis;

  @Column(name = "treatment_plan")
  @Lob
  private String treatmentPlan;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Emr() {}

  public Emr(Visit visit, Instant now) {
    this.visit = visit;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public Long getId() {
    return id;
  }

  public Visit getVisit() {
    return visit;
  }

  public String getChiefComplaint() {
    return chiefComplaint;
  }

  public String getHistoryPresentIllness() {
    return historyPresentIllness;
  }

  public String getPhysicalExam() {
    return physicalExam;
  }

  public String getDiagnosis() {
    return diagnosis;
  }

  public String getTreatmentPlan() {
    return treatmentPlan;
  }

  public void update(String chiefComplaint, String historyPresentIllness, String physicalExam, String diagnosis, String treatmentPlan) {
    this.chiefComplaint = chiefComplaint;
    this.historyPresentIllness = historyPresentIllness;
    this.physicalExam = physicalExam;
    this.diagnosis = diagnosis;
    this.treatmentPlan = treatmentPlan;
    this.updatedAt = Instant.now();
  }
}
