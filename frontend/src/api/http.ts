import axios, { type AxiosRequestConfig } from 'axios'

export type ApiEnvelope<T> = {
  code: number
  message: string
  data: T
  timestamp: string
}

export const http = axios.create({
  baseURL: '',
  timeout: 15_000,
})

export function httpSetToken(token: string) {
  if (!token) {
    delete http.defaults.headers.common.Authorization
    return
  }
  http.defaults.headers.common.Authorization = `Bearer ${token}`
}

function unwrap<T>(env: ApiEnvelope<T>): T {
  if (env.code !== 0) {
    throw new Error(env.message || 'Request failed')
  }
  return env.data
}

export async function apiGet<T>(url: string, config?: AxiosRequestConfig) {
  const resp = await http.get<ApiEnvelope<T>>(url, config)
  return unwrap(resp.data)
}

export async function apiPost<T>(url: string, body?: any, config?: AxiosRequestConfig) {
  const resp = await http.post<ApiEnvelope<T>>(url, body, config)
  return unwrap(resp.data)
}

export async function apiPut<T>(url: string, body?: any, config?: AxiosRequestConfig) {
  const resp = await http.put<ApiEnvelope<T>>(url, body, config)
  return unwrap(resp.data)
}

export async function apiPatch<T>(url: string, body?: any, config?: AxiosRequestConfig) {
  const resp = await http.patch<ApiEnvelope<T>>(url, body, config)
  return unwrap(resp.data)
}
