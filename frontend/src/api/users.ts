import { apiGet, apiPatch, apiPost } from './http'

export type User = {
  id: number
  username: string
  displayName: string
  status: string
  roles: string[]
  patientType?: string | null
  createdAt: string
  updatedAt: string
}

export function apiListUsers() {
  return apiGet<User[]>('/api/users')
}

export function apiCreateUser(body: { username: string; password: string; displayName: string; roles: string[]; patientType?: string }) {
  return apiPost<User>('/api/users', body)
}

export function apiUpdateUserStatus(userId: number, status: 'ACTIVE' | 'DISABLED') {
  return apiPatch<User>(`/api/users/${userId}/status`, { status })
}

export function apiResetPassword(userId: number, newPassword: string) {
  return apiPost<User>(`/api/users/${userId}/reset-password`, { newPassword })
}
