import { apiGet, apiPost, apiPut } from './http'

export type Visit = {
  id: number
  registrationOrderId: number
  doctorUserId: number
  doctorName: string
  patientUserId: number
  patientName: string
  status: string
  startedAt: string | null
  endedAt: string | null
}

export type Emr = {
  id: number
  visitId: number
  chiefComplaint: string | null
  historyPresentIllness: string | null
  physicalExam: string | null
  diagnosis: string | null
  treatmentPlan: string | null
}

export type PrescriptionItem = { id: number; drugId: number; drugName: string; quantity: number; unitPriceCents: number }
export type Prescription = { id: number; visitId: number; status: string; items: PrescriptionItem[] }

export function apiStartVisit(registrationOrderId: number) {
  return apiPost<Visit>('/api/visits/start', { registrationOrderId })
}

export function apiUpdateEmr(visitId: number, body: Partial<Emr>) {
  return apiPut<Emr>(`/api/visits/${visitId}/emr`, body)
}

export function apiIssuePrescription(visitId: number, items: Array<{ drugId: number; quantity: number; dosage?: string; frequency?: string; days?: number }>) {
  return apiPost<Prescription>(`/api/visits/${visitId}/prescription`, { items })
}

export function apiMyVisits() {
  return apiGet<Visit[]>('/api/visits/my')
}

