import { apiGet, apiPost } from './http'

export type LoginRequest = { username: string; password: string }
export type LoginResponse = { token: string; userId: number; username: string; displayName: string; roles: string[] }
export type MeResponse = { userId: number; username: string; roles: string[] }

export function apiLogin(body: LoginRequest) {
  return apiPost<LoginResponse>('/api/auth/login', body)
}

export function apiMe() {
  return apiGet<MeResponse>('/api/auth/me')
}

