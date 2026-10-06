<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiBillItems, apiListBills, apiPayBill, type Bill, type BillItem } from '../api/billing'
import { billSourceTypeLabel, billStatusLabel, billStatusTag, paymentMethodLabel } from '../utils/labels'

const loading = ref(false)
const bills = ref<Bill[]>([])
const status = ref<'' | 'UNPAID' | 'PAID' | 'REFUNDED' | 'VOIDED'>('UNPAID')
const keyword = ref('')
const itemsLoading = ref(false)
const items = ref<BillItem[]>([])
const dialogVisible = ref(false)

const payMethodByBillId = reactive<Record<number, string>>({})
const paymentMethodOptions = [
  { label: '现金', value: 'CASH' },
  { label: '银行卡', value: 'CARD' },
  { label: '微信', value: 'WECHAT' },
  { label: '支付宝', value: 'ALIPAY' },
  { label: '医保', value: 'INSURANCE' },
  { label: '其他', value: 'OTHER' },
]

const statusOptions = [
  { label: '未支付', value: 'UNPAID' },
  { label: '已支付', value: 'PAID' },
  { label: '已退款', value: 'REFUNDED' },
  { label: '已作废', value: 'VOIDED' },
]

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return bills.value
  return bills.value.filter((b) => {
    const hay = `${b.serialNo} ${b.patientName} ${billSourceTypeLabel(b.sourceType)} ${billStatusLabel(b.status)}`.toLowerCase()
    return hay.includes(kw)
  })
})

async function load() {
  loading.value = true
  try {
    bills.value = await apiListBills(status.value || undefined)
    for (const b of bills.value) {
      if (b.status === 'UNPAID' && !payMethodByBillId[b.id]) {
        payMethodByBillId[b.id] = 'CASH'
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
    ElMessage.success('收款成功')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '收款失败')
  }
}

onMounted(load)
</script>

<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div style="display: flex; align-items: center; gap: 10px">
          <div>收费管理</div>
          <el-segmented v-model="status" :options="statusOptions" @change="load" />
        </div>
        <div style="display: flex; gap: 8px; align-items: center">
          <el-input v-model="keyword" clearable placeholder="搜索 流水号/患者" style="width: 220px" />
          <el-button size="small" @click="load">刷新</el-button>
        </div>
      </div>
    </template>

    <el-table :data="filtered" v-loading="loading" border>
      <el-table-column prop="serialNo" label="流水号" width="220" />
      <el-table-column prop="patientName" label="患者" width="120" />
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
      <el-table-column label="操作" width="360">
        <template #default="{ row }">
          <el-button size="small" @click="viewItems(row.id)">明细</el-button>
          <el-select v-model="payMethodByBillId[row.id]" size="small" style="width: 120px" :disabled="row.status !== 'UNPAID'">
            <el-option v-for="o in paymentMethodOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
          <el-button size="small" type="primary" :disabled="row.status !== 'UNPAID'" @click="pay(row.id)">
            收款
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
