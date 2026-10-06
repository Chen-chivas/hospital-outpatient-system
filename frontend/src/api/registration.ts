import { apiGet, apiPost } from './http'

export type Schedule = {
  id: number
  doctorUserId: number
  doctorName: string
  scheduleDate: string
  timePeriod: string
  feeCents: number
  capacityTotal: number
  capacityRemaining: number
  status: string
}

export type RegistrationOrder = {
  id: number
  serialNo: string
  patientUserId: number
  patientName: string
  scheduleId: number
  doctorUserId: number
  doctorName: string
  scheduleDate: string
  timePeriod: string
  scheduleStatus: string
  channel: string
  status: string
  feeCents: number
  billStatus: string | null
  createdAt: string
  updatedAt: string
}

export type ListScheduleQuery = {
  date?: string
  doctorUserId?: number
  timePeriod?: 'AM' | 'PM'
  status?: 'OPEN' | 'CLOSED'
}

export function apiListSchedules(query?: string | ListScheduleQuery) {
  const q =
    typeof query === 'string'
      ? `?date=${encodeURIComponent(query)}`
      : query
        ? `?${new URLSearchParams(
            Object.entries({
              date: query.date ?? '',
              doctorUserId: query.doctorUserId != null ? String(query.doctorUserId) : '',
              timePeriod: query.timePeriod ?? '',
              status: query.status ?? '',
            }).filter(([, v]) => v !== '')
          ).toString()}`
        : ''
  return apiGet<Schedule[]>(`/api/schedules${q}`)
}

export function apiCreateSchedule(body: {
  doctorUserId: number
  scheduleDate: string
  timePeriod: 'AM' | 'PM'
  feeCents: number
  capacityTotal: number
}) {
  return apiPost<Schedule>('/api/schedules', body)
}

export function apiCloseSchedule(scheduleId: number) {
  return apiPost<Schedule>(`/api/schedules/${scheduleId}/close`)
}

export function apiBookRegistration(body: { scheduleId: number; channel: 'APP' | 'ONSITE' }) {
  return apiPost<RegistrationOrder>('/api/registrations/book', body)
}

export function apiMyRegistrations() {
  return apiGet<RegistrationOrder[]>('/api/registrations/my')
}

export function apiDoctorRegistrations() {
  return apiGet<RegistrationOrder[]>('/api/registrations/doctor/my')
}

export function apiCancelRegistration(id: number) {
  return apiPost<RegistrationOrder>(`/api/registrations/${id}/cancel`)
}

export function apiRescheduleRegistration(id: number, targetScheduleId: number) {
  return apiPost<RegistrationOrder>(`/api/registrations/${id}/reschedule`, { targetScheduleId })
}

export function apiMarkNoShow(id: number) {
  return apiPost<RegistrationOrder>(`/api/registrations/${id}/no-show`)
}

export type PatientBlacklist = {
  patientUserId: number
  noShowCount: number
  lastNoShowAt: string | null
  blacklistedUntil: string | null
  updatedAt: string
}

export function apiMyBlacklist() {
  return apiGet<PatientBlacklist | null>('/api/registrations/blacklist/my')
}
