import { apiGet, apiPost } from './http'

export type Bill = {
  id: number
  serialNo: string
  patientUserId: number
  patientName: string
  sourceType: string
  sourceId: number
  amountTotalCents: number
  status: string
  paymentMethod?: string | null
  createdAt: string
}

export type BillItem = {
  id: number
  itemType: string
  description: string
  quantity: number
  unitPriceCents: number
  amountCents: number
}

export function apiMyBills() {
  return apiGet<Bill[]>('/api/bills/my')
}

export function apiListBills(status?: string) {
  const q = status ? `?status=${encodeURIComponent(status)}` : ''
  return apiGet<Bill[]>(`/api/bills${q}`)
}

export function apiBillItems(billId: number) {
  return apiGet<BillItem[]>(`/api/bills/${billId}/items`)
}

export function apiPayBill(billId: number, paymentMethod?: string) {
  const body = paymentMethod ? { paymentMethod } : undefined
  return apiPost<Bill>(`/api/bills/${billId}/pay`, body)
}
