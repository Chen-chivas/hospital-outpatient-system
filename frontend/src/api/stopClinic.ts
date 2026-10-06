import { apiGet, apiPost } from './http'

export type StopClinicRequest = {
  id: number
  scheduleId: number
  scheduleDate: string
  timePeriod: string
  scheduleStatus: string
  doctorUserId: number
  doctorName: string
  reason: string | null
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  createdAt: string
  updatedAt: string
}

export function apiCreateStopClinicRequest(body: { scheduleId: number; reason?: string }) {
  return apiPost<StopClinicRequest>('/api/stop-clinic-requests', body)
}

export function apiMyStopClinicRequests() {
  return apiGet<StopClinicRequest[]>('/api/stop-clinic-requests/my')
}

export function apiPendingStopClinicRequests() {
  return apiGet<StopClinicRequest[]>('/api/stop-clinic-requests/pending')
}

export function apiApproveStopClinicRequest(id: number) {
  return apiPost<StopClinicRequest>(`/api/stop-clinic-requests/${id}/approve`)
}

export function apiRejectStopClinicRequest(id: number) {
  return apiPost<StopClinicRequest>(`/api/stop-clinic-requests/${id}/reject`)
}

