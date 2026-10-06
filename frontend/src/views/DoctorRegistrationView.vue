<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiDoctorRegistrations, apiListSchedules, apiMarkNoShow, type RegistrationOrder, type Schedule } from '../api/registration'
import {
  apiCreateStopClinicRequest,
  apiMyStopClinicRequests,
  type StopClinicRequest,
} from '../api/stopClinic'
import { apiStartVisit } from '../api/visit'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { scheduleStatusLabel, scheduleStatusTag, stopClinicStatusLabel, stopClinicStatusTag } from '../utils/labels'

const router = useRouter()
const auth = useAuthStore()

const activeTab = ref<'queue' | 'stopClinic'>('queue')

const loadingOrders = ref(false)
const orders = ref<RegistrationOrder[]>([])

const loadingSchedules = ref(false)
const schedules = ref<Schedule[]>([])

const loadingStop = ref(false)
const stopRequests = ref<StopClinicRequest[]>([])

const queueQuery = reactive({
  date: new Date().toISOString().slice(0, 10),
  timePeriod: '' as '' | 'AM' | 'PM',
  keyword: '',
})

const stopQuery = reactive({
  date: new Date().toISOString().slice(0, 10),
})

const timePeriodLabel = (tp?: string) => (tp === 'AM' ? '上午' : tp === 'PM' ? '下午' : tp ?? '-')

const regStatusLabel = (s: string) =>
  s === 'CREATED'
    ? '待支付'
    : s === 'PAID'
      ? '待就诊'
      : s === 'CANCELED'
        ? '已取消'
        : s === 'NO_SHOW'
          ? '爽约'
          : s === 'COMPLETED'
            ? '已完成'
            : s

const regStatusTag = (s: string) =>
  s === 'PAID' ? 'success' : s === 'CREATED' ? 'warning' : s === 'CANCELED' ? 'info' : s === 'NO_SHOW' ? 'danger' : 'info'

const billStatusLabel = (s?: string | null) =>
  s === 'UNPAID'
    ? '未支付'
    : s === 'PAID'
      ? '已支付'
      : s === 'VOIDED'
        ? '已作废'
        : s === 'REFUNDED'
          ? '已退款'
          : '-'

const billStatusTag = (s?: string | null) =>
  s === 'PAID' ? 'success' : s === 'UNPAID' ? 'warning' : s === 'REFUNDED' ? 'info' : s === 'VOIDED' ? 'info' : 'info'

const filteredOrders = computed(() =>
  orders.value.filter((o) => {
    if (queueQuery.date && o.scheduleDate !== queueQuery.date) return false
    if (queueQuery.timePeriod && o.timePeriod !== queueQuery.timePeriod) return false
    if (queueQuery.keyword && !o.patientName.toLowerCase().includes(queueQuery.keyword.trim().toLowerCase())) return false
    return true
  })
)

async function load() {
  loadingOrders.value = true
  try {
    orders.value = await apiDoctorRegistrations()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loadingOrders.value = false
  }
}

async function start(orderId: number) {
  try {
    const visit = await apiStartVisit(orderId)
    ElMessage.success(`已开始就诊（就诊ID: ${visit.id}）`)
    await router.push('/visits')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '开始就诊失败（可能未缴费）')
  }
}

async function noShow(orderId: number) {
  try {
    await ElMessageBox.confirm('确认标记该挂号为“爽约”？此操作将计入患者爽约记录。', '确认操作', { type: 'warning' })
    await apiMarkNoShow(orderId)
    ElMessage.success('已标记爽约')
    await load()
  } catch {
    // ignore
  }
}

async function loadMySchedules() {
  if (!auth.me?.userId) return
  loadingSchedules.value = true
  try {
    schedules.value = await apiListSchedules({ date: stopQuery.date, doctorUserId: auth.me.userId })
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载排班失败')
  } finally {
    loadingSchedules.value = false
  }
}

async function loadMyStopRequests() {
  loadingStop.value = true
  try {
    stopRequests.value = await apiMyStopClinicRequests()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载停诊申请失败')
  } finally {
    loadingStop.value = false
  }
}

const stopDialogVisible = ref(false)
const stopReason = ref('')
const stopSchedule = ref<Schedule | null>(null)

function openStopDialog(row: Schedule) {
  stopSchedule.value = row
  stopReason.value = ''
  stopDialogVisible.value = true
}

async function submitStop() {
  if (!stopSchedule.value) return
  try {
    await ElMessageBox.confirm('确认提交停诊申请？管理员审核通过后将自动取消该排班下的挂号单。', '确认提交', { type: 'warning' })
    await apiCreateStopClinicRequest({ scheduleId: stopSchedule.value.id, reason: stopReason.value })
    ElMessage.success('已提交停诊申请')
    stopDialogVisible.value = false
    await loadMySchedules()
    await loadMyStopRequests()
  } catch {
    // ignore
  }
}

onMounted(async () => {
  await load()
  await loadMySchedules()
  await loadMyStopRequests()
})
</script>

<template>
  <el-tabs v-model="activeTab" type="card">
    <el-tab-pane name="queue" label="挂号队列">
      <el-card>
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap">
            <div style="display: flex; gap: 10px; align-items: center">
              <div>我的挂号队列</div>
              <el-tag type="info" effect="plain">支持筛选/开始就诊/标记爽约</el-tag>
            </div>
            <div style="display: flex; gap: 10px; align-items: center; flex-wrap: wrap">
              <el-input v-model="queueQuery.keyword" placeholder="患者姓名关键字" clearable style="width: 180px" />
              <el-select v-model="queueQuery.timePeriod" placeholder="时段" clearable style="width: 120px">
                <el-option label="上午" value="AM" />
                <el-option label="下午" value="PM" />
              </el-select>
              <el-date-picker v-model="queueQuery.date" type="date" value-format="YYYY-MM-DD" />
              <el-button size="small" @click="load">刷新</el-button>
            </div>
          </div>
        </template>

        <el-table :data="filteredOrders" v-loading="loadingOrders" border>
          <el-table-column prop="serialNo" label="流水号" width="220" />
          <el-table-column prop="patientName" label="患者" width="140" />
          <el-table-column prop="scheduleDate" label="日期" width="120" />
          <el-table-column prop="timePeriod" label="时段" width="90">
            <template #default="{ row }">{{ timePeriodLabel(row.timePeriod) }}</template>
          </el-table-column>
          <el-table-column prop="billStatus" label="支付" width="110">
            <template #default="{ row }">
              <el-tag :type="billStatusTag(row.billStatus)" effect="plain">{{ billStatusLabel(row.billStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="挂号状态" width="110">
            <template #default="{ row }">
              <el-tag :type="regStatusTag(row.status)" effect="plain">{{ regStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220">
            <template #default="{ row }">
              <el-button size="small" type="primary" :disabled="row.billStatus !== 'PAID' || row.status !== 'PAID'" @click="start(row.id)">
                开始就诊
              </el-button>
              <el-button
                size="small"
                type="danger"
                plain
                :disabled="row.status === 'CANCELED' || row.status === 'NO_SHOW' || row.status === 'COMPLETED'"
                @click="noShow(row.id)"
              >
                标记爽约
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-tab-pane>

    <el-tab-pane name="stopClinic" label="停诊申请">
      <el-space direction="vertical" size="large" fill>
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap">
              <div style="display: flex; gap: 10px; align-items: center">
                <div>我的排班</div>
                <el-tag type="info" effect="plain">对单个排班提交停诊申请</el-tag>
              </div>
              <div style="display: flex; gap: 10px; align-items: center">
                <el-date-picker v-model="stopQuery.date" type="date" value-format="YYYY-MM-DD" @change="loadMySchedules" />
                <el-button size="small" @click="loadMySchedules">刷新</el-button>
              </div>
            </div>
          </template>

          <el-table :data="schedules" v-loading="loadingSchedules" border>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="scheduleDate" label="日期" width="120" />
            <el-table-column prop="timePeriod" label="时段" width="90">
              <template #default="{ row }">{{ timePeriodLabel(row.timePeriod) }}</template>
            </el-table-column>
            <el-table-column prop="capacityRemaining" label="剩余号源" width="100" />
          <el-table-column prop="status" label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="scheduleStatusTag(row.status)" effect="plain">{{ scheduleStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button size="small" type="danger" plain :disabled="row.status !== 'OPEN'" @click="openStopDialog(row)">申请停诊</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <div>我的停诊申请</div>
              <el-button size="small" @click="loadMyStopRequests">刷新</el-button>
            </div>
          </template>

          <el-table :data="stopRequests" v-loading="loadingStop" border>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="scheduleDate" label="日期" width="120" />
            <el-table-column prop="timePeriod" label="时段" width="90">
              <template #default="{ row }">{{ timePeriodLabel(row.timePeriod) }}</template>
            </el-table-column>
            <el-table-column prop="reason" label="原因" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="stopClinicStatusTag(row.status)" effect="plain">{{ stopClinicStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updatedAt" label="更新时间" width="180" />
          </el-table>
        </el-card>
      </el-space>
    </el-tab-pane>
  </el-tabs>

  <el-dialog v-model="stopDialogVisible" title="提交停诊申请" width="600px">
      <el-form label-position="top">
      <el-form-item label="排班">
        <el-text>{{ stopSchedule?.scheduleDate }} {{ timePeriodLabel(stopSchedule?.timePeriod) }}（排班ID: {{ stopSchedule?.id }}）</el-text>
      </el-form-item>
      <el-form-item label="原因(可选)">
        <el-input v-model="stopReason" type="textarea" :rows="3" placeholder="如：临时外出、会议、突发情况等" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="stopDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitStop">提交</el-button>
    </template>
  </el-dialog>
</template>
