<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  apiAdjustInventory,
  apiDispense,
  apiGetInventory,
  apiListDrugs,
  apiListPrescriptions,
  apiPrescriptionDetail,
  type Drug,
  type Prescription,
  type PrescriptionDetail,
} from '../api/pharmacy'
import { prescriptionStatusLabel, prescriptionStatusTag } from '../utils/labels'

const loadingDrugs = ref(false)
const loadingRx = ref(false)
const drugs = ref<Drug[]>([])
const inventories = ref<Record<number, number>>({})
const prescriptions = ref<Prescription[]>([])
const drawerVisible = ref(false)
const drawerLoading = ref(false)
const selected = ref<PrescriptionDetail | null>(null)

const invForm = reactive({
  drugId: 0,
  delta: 10,
})

const drugOptions = computed(() => drugs.value.map((d) => ({ label: `${d.name} (${d.code})`, value: d.id })))

async function loadDrugs() {
  loadingDrugs.value = true
  try {
    drugs.value = await apiListDrugs()
    if (!invForm.drugId && drugs.value.length > 0) {
      invForm.drugId = drugs.value[0].id
    }
    await refreshInventories()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载药品失败')
  } finally {
    loadingDrugs.value = false
  }
}

async function refreshInventories() {
  const map: Record<number, number> = {}
  for (const d of drugs.value) {
    try {
      const inv = await apiGetInventory(d.id)
      map[d.id] = inv.quantity
    } catch {
      map[d.id] = 0
    }
  }
  inventories.value = map
}

async function adjust() {
  try {
    const inv = await apiAdjustInventory({ drugId: invForm.drugId, delta: invForm.delta })
    inventories.value[inv.drugId] = inv.quantity
    ElMessage.success('库存已调整')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '调整失败')
  }
}

async function loadPrescriptions() {
  loadingRx.value = true
  try {
    prescriptions.value = await apiListPrescriptions()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载处方失败')
  } finally {
    loadingRx.value = false
  }
}

async function dispense(prescriptionId: number) {
  try {
    await apiDispense(prescriptionId)
    ElMessage.success('发药成功')
    await loadPrescriptions()
    await refreshInventories()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '发药失败（可能未缴费或库存不足）')
  }
}

async function openPrescription(prescriptionId: number) {
  drawerVisible.value = true
  drawerLoading.value = true
  selected.value = null
  try {
    selected.value = await apiPrescriptionDetail(prescriptionId)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载处方详情失败')
  } finally {
    drawerLoading.value = false
  }
}

const rxTotal = computed(() => {
  if (!selected.value) return 0
  return selected.value.items.reduce((sum, it) => sum + it.unitPriceCents * it.quantity, 0)
})

onMounted(async () => {
  await loadDrugs()
  await loadPrescriptions()
})
</script>

<template>
  <el-space direction="vertical" size="large" fill>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <div>药品与库存</div>
          <el-button size="small" @click="loadDrugs">刷新</el-button>
        </div>
      </template>

      <el-form label-position="top">
        <el-row :gutter="12">
          <el-col :span="10">
            <el-form-item label="药品">
              <el-select v-model="invForm.drugId" filterable style="width: 100%">
                <el-option v-for="o in drugOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="调整数量（可负数）">
              <el-input-number v-model="invForm.delta" />
            </el-form-item>
          </el-col>
          <el-col :span="8" style="display: flex; align-items: flex-end">
            <el-button type="primary" @click="adjust">调整库存</el-button>
          </el-col>
        </el-row>
      </el-form>

      <el-table :data="drugs" v-loading="loadingDrugs" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="priceCents" label="单价(分)" width="110" />
        <el-table-column label="库存" width="100">
          <template #default="{ row }">
            {{ inventories[row.id] ?? '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <div>处方列表</div>
          <el-button size="small" @click="loadPrescriptions">刷新</el-button>
        </div>
      </template>

      <el-table :data="prescriptions" v-loading="loadingRx" border>
        <el-table-column prop="id" label="处方ID" width="90" />
        <el-table-column prop="patientName" label="患者" width="120" />
        <el-table-column prop="doctorName" label="医生" width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="prescriptionStatusTag(row.status)" effect="plain">{{ prescriptionStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="开立时间" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" @click="openPrescription(row.id)">详情</el-button>
            <el-button size="small" type="primary" :disabled="row.status !== 'ISSUED'" @click="dispense(row.id)">发药</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </el-space>

  <el-drawer v-model="drawerVisible" size="520px" title="处方详情">
    <el-skeleton v-if="drawerLoading" :rows="8" animated />
    <template v-else-if="selected">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="处方ID">{{ selected.id }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ selected.patientName }}（ID: {{ selected.patientUserId }}）</el-descriptions-item>
        <el-descriptions-item label="医生">{{ selected.doctorName }}（ID: {{ selected.doctorUserId }}）</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="prescriptionStatusTag(selected.status)" effect="plain">{{ prescriptionStatusLabel(selected.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="合计(分)">{{ rxTotal }}</el-descriptions-item>
      </el-descriptions>

      <div style="margin-top: 12px; font-weight: 600">处方明细</div>
      <el-table :data="selected.items" border style="margin-top: 8px">
        <el-table-column prop="drugName" label="药品" />
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column prop="unitPriceCents" label="单价(分)" width="110" />
        <el-table-column label="小计(分)" width="110">
          <template #default="{ row }">{{ row.unitPriceCents * row.quantity }}</template>
        </el-table-column>
      </el-table>
    </template>
    <template v-else>
      <el-empty description="暂无数据" />
    </template>
  </el-drawer>
</template>
