<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiListDrugs, type Drug } from '../api/pharmacy'
import { apiIssuePrescription, apiMyVisits, apiUpdateEmr, type Visit } from '../api/visit'
import { visitStatusLabel, visitStatusTag } from '../utils/labels'

const loading = ref(false)
const visits = ref<Visit[]>([])
const drugs = ref<Drug[]>([])

const selectedVisitId = ref<number | null>(null)
const selectedVisit = computed(() => visits.value.find((v) => v.id === selectedVisitId.value) ?? null)

const emr = reactive({
  chiefComplaint: '',
  historyPresentIllness: '',
  physicalExam: '',
  diagnosis: '',
  treatmentPlan: '',
})

const rxItems = ref<Array<{ drugId: number; quantity: number }>>([{ drugId: 0, quantity: 1 }])

async function load() {
  loading.value = true
  try {
    visits.value = await apiMyVisits()
    if (!selectedVisitId.value && visits.value.length > 0) {
      selectedVisitId.value = visits.value[0].id
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载就诊失败')
  } finally {
    loading.value = false
  }
}

async function loadDrugs() {
  try {
    drugs.value = await apiListDrugs()
    if (rxItems.value[0].drugId === 0 && drugs.value.length > 0) {
      rxItems.value[0].drugId = drugs.value[0].id
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载药品失败')
  }
}

async function saveEmr() {
  if (!selectedVisitId.value) return
  try {
    await apiUpdateEmr(selectedVisitId.value, emr)
    ElMessage.success('病历已保存')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '保存失败')
  }
}

function addRxRow() {
  rxItems.value.push({ drugId: drugs.value[0]?.id ?? 0, quantity: 1 })
}

function selectVisit(row: Visit) {
  selectedVisitId.value = row.id
}

function removeRxRow(idx: number) {
  rxItems.value.splice(idx, 1)
}

async function issueRx() {
  if (!selectedVisitId.value) return
  try {
    const items = rxItems.value.filter((x) => x.drugId > 0 && x.quantity > 0)
    if (items.length === 0) {
      ElMessage.warning('请至少添加 1 个药品')
      return
    }
    await apiIssuePrescription(selectedVisitId.value, items)
    ElMessage.success('处方已开立（已生成账单，请缴费后发药）')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '开立处方失败')
  }
}

onMounted(async () => {
  await loadDrugs()
  await load()
})
</script>

<template>
  <el-space direction="vertical" size="large" fill>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <div>我的就诊</div>
          <el-button size="small" @click="load">刷新</el-button>
        </div>
      </template>

      <el-table :data="visits" v-loading="loading" border @row-click="selectVisit">
        <el-table-column prop="id" label="就诊ID" width="90" />
        <el-table-column prop="registrationOrderId" label="挂号单ID" width="110" />
        <el-table-column prop="patientName" label="患者" width="140" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="visitStatusTag(row.status)" effect="plain">{{ visitStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-alert v-if="!selectedVisit" type="info" show-icon title="请选择一个就诊记录" />

    <el-card v-else>
      <template #header>电子病历（EMR） - 就诊ID: {{ selectedVisit.id }}</template>
      <el-form label-position="top">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="主诉">
              <el-input v-model="emr.chiefComplaint" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="现病史">
              <el-input v-model="emr.historyPresentIllness" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体格检查">
              <el-input v-model="emr.physicalExam" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="诊断">
              <el-input v-model="emr.diagnosis" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="处置/计划">
              <el-input v-model="emr.treatmentPlan" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-button type="primary" @click="saveEmr">保存病历</el-button>
      </el-form>
    </el-card>

    <el-card v-if="selectedVisit">
      <template #header>处方开立</template>
      <el-table :data="rxItems" border>
        <el-table-column label="药品">
          <template #default="{ row }">
            <el-select v-model="row.drugId" filterable style="width: 100%">
              <el-option v-for="d in drugs" :key="d.id" :label="`${d.name} (${d.code})`" :value="d.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="140">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ $index }">
            <el-button size="small" type="danger" plain @click="removeRxRow($index)" :disabled="rxItems.length <= 1">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 10px; display: flex; gap: 8px">
        <el-button @click="addRxRow">添加药品</el-button>
        <el-button type="primary" @click="issueRx">开立处方</el-button>
      </div>
    </el-card>
  </el-space>
</template>
