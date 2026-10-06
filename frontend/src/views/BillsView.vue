<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { apiBillItems, apiMyBills, apiPayBill, type Bill, type BillItem } from '../api/billing'
import { billSourceTypeLabel, billStatusLabel, billStatusTag, paymentMethodLabel } from '../utils/labels'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const bills = ref<Bill[]>([])
const itemsLoading = ref(false)
const items = ref<BillItem[]>([])
const dialogVisible = ref(false)

const query = reactive({
  status: '' as '' | 'UNPAID' | 'PAID' | 'VOIDED' | 'REFUNDED',
  sourceType: '' as '' | 'REGISTRATION' | 'PRESCRIPTION',
  sourceId: '' as '' | string,
  keyword: '',
})

const payMethodByBillId = reactive<Record<number, string>>({})
const paymentMethodOptions = [
  { label: '支付宝', value: 'ALIPAY' },
  { label: '微信', value: 'WECHAT' },
  { label: '银行卡', value: 'CARD' },
  { label: '现金', value: 'CASH' },
  { label: '医保', value: 'INSURANCE' },
  { label: '其他', value: 'OTHER' },
]

const filtered = computed(() => {
  const statusPriority: Record<string, number> = { UNPAID: 0, PAID: 1, REFUNDED: 2, VOIDED: 3 }
  const list = bills.value
    .slice()
    .sort((a, b) => (statusPriority[a.status] ?? 99) - (statusPriority[b.status] ?? 99) || (b.createdAt || '').localeCompare(a.createdAt || ''))
  const kw = query.keyword.trim().toLowerCase()
  return list.filter((b) => {
    if (query.status && b.status !== query.status) return false
    if (query.sourceType && b.sourceType !== query.sourceType) return false
    if (query.sourceId && String(b.sourceId) !== String(query.sourceId)) return false
    if (kw) {
      const hay = `${b.serialNo} ${billSourceTypeLabel(b.sourceType)} ${billStatusLabel(b.status)}`.toLowerCase()
      if (!hay.includes(kw)) return false
    }
    return true
  })
})

function syncQueryFromRoute() {
  const status = typeof route.query.status === 'string' ? route.query.status : ''
  const sourceType = typeof route.query.sourceType === 'string' ? route.query.sourceType : ''
  const sourceId = typeof route.query.sourceId === 'string' ? route.query.sourceId : ''
  const keyword = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  query.status = (status as any) || ''
  query.sourceType = (sourceType as any) || ''
  query.sourceId = sourceId || ''
  query.keyword = keyword || ''
}

async function load() {
  loading.value = true
  try {
    bills.value = await apiMyBills()
    for (const b of bills.value) {
      if (b.status === 'UNPAID' && !payMethodByBillId[b.id]) {
        payMethodByBillId[b.id] = 'ALIPAY'
      }
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

async function viewItems(billId: number) {
  itemsLoading.value = true
  dialogVisible.value = true
  try {
    items.value = await apiBillItems(billId)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载明细失败')
  } finally {
    itemsLoading.value = false
  }
}

async function pay(billId: number) {
  try {
    await apiPayBill(billId, payMethodByBillId[billId])
    ElMessage.success('支付成功')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '支付失败')
  }
}

function clearRouteQuery() {
  if (!route.query || Object.keys(route.query).length === 0) return
  router.replace({ query: {} })
}

onMounted(async () => {
  syncQueryFromRoute()
  await load()
  clearRouteQuery()
})
</script>

<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>我的账单</div>
        <div style="display: flex; gap: 8px; align-items: center">
          <el-input v-model="query.keyword" clearable placeholder="搜索 流水号/状态/来源" style="width: 220px" />
          <el-select v-model="query.sourceType" clearable placeholder="来源" style="width: 120px">
            <el-option label="挂号" value="REGISTRATION" />
            <el-option label="处方" value="PRESCRIPTION" />
          </el-select>
          <el-select v-model="query.status" clearable placeholder="状态" style="width: 120px">
            <el-option label="未支付" value="UNPAID" />
            <el-option label="已支付" value="PAID" />
            <el-option label="已退款" value="REFUNDED" />
            <el-option label="已作废" value="VOIDED" />
          </el-select>
          <el-button size="small" @click="load">刷新</el-button>
        </div>
      </div>
    </template>

    <el-alert
      v-if="query.sourceId"
      type="info"
      show-icon
      style="margin-bottom: 10px"
      :title="`已定位到来源ID：${query.sourceId}（清空筛选可查看全部账单）`"
      @close="query.sourceId = ''"
      closable
    />

    <el-table :data="filtered" v-loading="loading" border>
      <el-table-column prop="serialNo" label="流水号" width="220" />
      <el-table-column prop="sourceType" label="来源" width="120">
        <template #default="{ row }">{{ billSourceTypeLabel(row.sourceType) }}</template>
      </el-table-column>
      <el-table-column prop="amountTotalCents" label="金额(分)" width="120" />
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="billStatusTag(row.status)" effect="plain">{{ billStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="paymentMethod" label="支付方式" width="110">
        <template #default="{ row }">{{ row.status === 'PAID' ? paymentMethodLabel(row.paymentMethod) : '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="380">
        <template #default="{ row }">
          <el-button size="small" @click="viewItems(row.id)">明细</el-button>
          <el-select v-model="payMethodByBillId[row.id]" size="small" style="width: 120px" :disabled="row.status !== 'UNPAID'">
            <el-option v-for="o in paymentMethodOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
          <el-button size="small" type="primary" :disabled="row.status !== 'UNPAID'" @click="pay(row.id)">
            支付
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" title="账单明细" width="600px">
    <el-table :data="items" v-loading="itemsLoading" border>
      <el-table-column prop="description" label="项目" />
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column prop="unitPriceCents" label="单价(分)" width="120" />
      <el-table-column prop="amountCents" label="小计(分)" width="120" />
    </el-table>
  </el-dialog>
</template>
