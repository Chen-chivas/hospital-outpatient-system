<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  apiApproveStopClinicRequest,
  apiPendingStopClinicRequests,
  apiRejectStopClinicRequest,
  type StopClinicRequest,
} from '../api/stopClinic'
import { stopClinicStatusLabel, stopClinicStatusTag } from '../utils/labels'

const loading = ref(false)
const rows = ref<StopClinicRequest[]>([])

const timePeriodLabel = (tp?: string) => (tp === 'AM' ? '上午' : tp === 'PM' ? '下午' : tp ?? '-')

async function load() {
  loading.value = true
  try {
    rows.value = await apiPendingStopClinicRequests()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

async function approve(id: number) {
  try {
    await ElMessageBox.confirm('确认通过停诊申请？通过后将关闭排班并自动取消/退款相关挂号单。', '确认操作', { type: 'warning' })
    await apiApproveStopClinicRequest(id)
    ElMessage.success('已通过')
    await load()
  } catch {
    // ignore
  }
}

async function reject(id: number) {
  try {
    await ElMessageBox.confirm('确认驳回停诊申请？', '确认操作', { type: 'warning' })
    await apiRejectStopClinicRequest(id)
    ElMessage.success('已驳回')
    await load()
  } catch {
    // ignore
  }
}

onMounted(load)
</script>

<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
          <div style="display: flex; align-items: center; gap: 10px">
          <div>停诊审核</div>
          <el-tag type="info" effect="plain">仅显示待审核</el-tag>
        </div>
        <el-button size="small" @click="load">刷新</el-button>
      </div>
    </template>

    <el-table :data="rows" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="doctorName" label="医生" width="160" />
      <el-table-column prop="scheduleDate" label="日期" width="120" />
      <el-table-column prop="timePeriod" label="时段" width="90">
        <template #default="{ row }">{{ timePeriodLabel(row.timePeriod) }}</template>
      </el-table-column>
      <el-table-column prop="scheduleId" label="排班ID" width="90" />
      <el-table-column prop="reason" label="原因" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="stopClinicStatusTag(row.status)" effect="plain">{{ stopClinicStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" type="success" plain :disabled="row.status !== 'PENDING'" @click="approve(row.id)">通过</el-button>
          <el-button size="small" type="danger" plain :disabled="row.status !== 'PENDING'" @click="reject(row.id)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>
