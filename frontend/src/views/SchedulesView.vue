<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiListUsers, type User } from '../api/users'
import { apiCloseSchedule, apiCreateSchedule, apiListSchedules, type Schedule } from '../api/registration'
import { scheduleStatusLabel, scheduleStatusTag, timePeriodLabel } from '../utils/labels'

const loading = ref(false)
const schedules = ref<Schedule[]>([])
const users = ref<User[]>([])
const date = ref<string>(new Date().toISOString().slice(0, 10))

const doctors = computed(() => users.value.filter((u) => u.roles.includes('DOCTOR')))

const filter = reactive({
  doctorUserId: 0,
  timePeriod: '' as '' | 'AM' | 'PM',
})

const filtered = computed(() => {
  return schedules.value.filter((s) => {
    if (filter.doctorUserId && s.doctorUserId !== filter.doctorUserId) return false
    if (filter.timePeriod && s.timePeriod !== filter.timePeriod) return false
    return true
  })
})

const form = reactive({
  doctorUserId: 0,
  scheduleDate: date.value,
  timePeriod: 'AM' as 'AM' | 'PM',
  feeCents: 500,
  capacityTotal: 20,
})

async function load() {
  loading.value = true
  try {
    schedules.value = await apiListSchedules(date.value)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadUsers() {
  try {
    users.value = await apiListUsers()
    if (!form.doctorUserId && doctors.value.length > 0) {
      form.doctorUserId = doctors.value[0].id
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载医生失败')
  }
}

async function create() {
  try {
    await apiCreateSchedule({
      doctorUserId: form.doctorUserId,
      scheduleDate: form.scheduleDate,
      timePeriod: form.timePeriod,
      feeCents: form.feeCents,
      capacityTotal: form.capacityTotal,
    })
    ElMessage.success('创建成功')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '创建失败')
  }
}

async function closeSchedule(id: number) {
  try {
    await ElMessageBox.confirm('确认关闭该排班？关闭后将无法继续挂号。', '确认操作', { type: 'warning' })
    await apiCloseSchedule(id)
    ElMessage.success('已关闭')
    await load()
  } catch {
    // ignore
  }
}

onMounted(async () => {
  await loadUsers()
  await load()
})
</script>

<template>
  <el-space direction="vertical" size="large" fill>
    <el-card>
      <template #header>创建排班</template>
      <el-form label-position="top">
        <el-row :gutter="12">
          <el-col :span="6">
            <el-form-item label="医生">
              <el-select v-model="form.doctorUserId" filterable style="width: 100%">
                <el-option v-for="d in doctors" :key="d.id" :label="`${d.displayName} (#${d.id})`" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="日期">
              <el-date-picker v-model="form.scheduleDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="时段">
              <el-select v-model="form.timePeriod" style="width: 100%">
                <el-option label="上午" value="AM" />
                <el-option label="下午" value="PM" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="挂号费(分)">
              <el-input-number v-model="form.feeCents" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="号源总量">
              <el-input-number v-model="form.capacityTotal" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-button type="primary" @click="create">创建</el-button>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <div style="display: flex; gap: 10px; align-items: center">
            <div>排班列表</div>
            <el-tag type="info" effect="plain">支持筛选/关闭排班</el-tag>
          </div>
          <div style="display: flex; gap: 8px; align-items: center">
            <el-select v-model="filter.doctorUserId" placeholder="医生" clearable style="width: 160px">
              <el-option v-for="d in doctors" :key="d.id" :label="d.displayName" :value="d.id" />
            </el-select>
            <el-select v-model="filter.timePeriod" placeholder="时段" clearable style="width: 120px">
              <el-option label="上午" value="AM" />
              <el-option label="下午" value="PM" />
            </el-select>
            <el-date-picker v-model="date" type="date" value-format="YYYY-MM-DD" @change="load" />
            <el-button size="small" @click="load">刷新</el-button>
          </div>
        </div>
      </template>
      <el-table :data="filtered" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="doctorName" label="医生" width="160" />
        <el-table-column prop="scheduleDate" label="日期" width="120" />
        <el-table-column prop="timePeriod" label="时段" width="80">
          <template #default="{ row }">{{ timePeriodLabel(row.timePeriod) }}</template>
        </el-table-column>
        <el-table-column prop="feeCents" label="费用(分)" width="110" />
        <el-table-column prop="capacityTotal" label="总量" width="90" />
        <el-table-column prop="capacityRemaining" label="剩余" width="90" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="scheduleStatusTag(row.status)" effect="plain">{{ scheduleStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" type="danger" plain :disabled="row.status !== 'OPEN'" @click="closeSchedule(row.id)">
              关闭
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </el-space>
</template>
