import { apiGet } from './http'

export type AuditLog = {
  id: number
  actorUserId: number | null
  actorName: string | null
  action: string
  module: string
  entityType: string | null
  entityId: number | null
  detailsJson: string | null
  createdAt: string
}

export function apiRecentAudit() {
  return apiGet<AuditLog[]>('/api/audit/recent')
}

