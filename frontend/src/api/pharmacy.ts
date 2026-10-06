import { apiGet, apiPost } from './http'

export type Drug = {
  id: number
  code: string
  name: string
  spec: string | null
  unit: string | null
  priceCents: number
  enabled: boolean
}

export type Inventory = {
  drugId: number
  drugName: string
  quantity: number
}

export type Prescription = {
  id: number
  visitId: number
  patientUserId: number
  patientName: string
  doctorUserId: number
  doctorName: string
  status: string
  createdAt: string
}

export type PrescriptionDetail = Prescription & {
  items: Array<{ id: number; drugId: number; drugName: string; quantity: number; unitPriceCents: number }>
}

export type DispenseRecord = {
  id: number
  prescriptionId: number
  pharmacistUserId: number
  pharmacistName: string
  status: string
  createdAt: string
}

export function apiListDrugs() {
  return apiGet<Drug[]>('/api/drugs')
}

export function apiCreateDrug(body: { code: string; name: string; spec?: string; unit?: string; priceCents: number }) {
  return apiPost<Drug>('/api/drugs', body)
}

export function apiGetInventory(drugId: number) {
  return apiGet<Inventory>(`/api/inventory/${drugId}`)
}

export function apiAdjustInventory(body: { drugId: number; delta: number }) {
  return apiPost<Inventory>('/api/inventory/adjust', body)
}

export function apiListPrescriptions() {
  return apiGet<Prescription[]>('/api/prescriptions')
}

export function apiPrescriptionDetail(prescriptionId: number) {
  return apiGet<PrescriptionDetail>(`/api/prescriptions/${prescriptionId}`)
}

export function apiDispense(prescriptionId: number) {
  return apiPost<DispenseRecord>('/api/pharmacy/dispense', { prescriptionId })
}
