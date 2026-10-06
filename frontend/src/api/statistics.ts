import { apiGet } from './http'

export type Summary = {
  users: number
  schedules: number
  registrations: number
  visits: number
  prescriptions: number
  bills: number
  revenuePaidCents: number
}

export function apiSummary() {
  return apiGet<Summary>('/api/statistics/summary')
}

export type TrendPoint = { date: string; registrations: number; revenuePaidCents: number }
export type Trends = { days: number; points: TrendPoint[] }

export function apiTrends(days = 14) {
  return apiGet<Trends>(`/api/statistics/trends?days=${encodeURIComponent(days)}`)
}

export type PieSlice = { name: string; value: number }
export type Distributions = {
  days: number
  expenseStructure: PieSlice[]
  patientTypes: PieSlice[]
  paymentMethods: PieSlice[]
}

export function apiDistributions(days = 30) {
  return apiGet<Distributions>(`/api/statistics/distributions?days=${encodeURIComponent(days)}`)
}
