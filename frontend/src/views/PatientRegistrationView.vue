<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  apiBookRegistration,
  apiCancelRegistration,
  apiListSchedules,
  apiMyBlacklist,
  apiMyRegistrations,
  apiRescheduleRegistration,
  type PatientBlacklist,
  type RegistrationOrder,
  type Schedule,
} from '../api/registration'

const router = useRouter()

const activeTab = ref<'book' | 'mine'>('book')
const loadingSchedules = ref(false)
const loadingOrders = ref(false)
const loadingBlacklist = ref(false)

const schedules = ref<Schedule[]>([])
const orders = ref<RegistrationOrder[]>([])
const blacklist = ref<PatientBlacklist | null>(null)

const query = reactive({
  date: new Date().toISOString().slice(0, 10),
  timePeriod: '' as '' | 'AM' | 'PM',
  keyword: '',
  onlyOpen: true,
  onlyAvailable: true,
})

const timePeriodLabel = (tp?: string) => (tp === 'AM' ? '上午' : tp === 'PM' ? '下午' : tp ?? '-')
const moneyYuan = (cents: number) => `¥${(cents / 100).toFixed(2)}`

const scheduleStatusLabel = (s: string) => (s === 'OPEN' ? '可挂号' : '已关闭')
const scheduleStatusTag = (s: string) => (s === 'OPEN' ? 'success' : 'info')

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

const filteredSchedules = computed(() =>
  schedules.value.filter((s) => {
    if (query.timePeriod && s.timePeriod !== query.timePeriod) return false
    if (query.onlyOpen && s.status !== 'OPEN') return false
    if (query.onlyAvailable && s.capacityRemaining <= 0) return false
    if (query.keyword && !s.doctorName.toLowerCase().includes(query.keyword.trim().toLowerCase())) return false
    return true
  })
)

const flowActive = computed(() => {
  if (activeTab.value === 'book') return 0
  const hasUnpaid = orders.value.some((o) => o.billStatus === 'UNPAID')
  const hasPaid = orders.value.some((o) => o.billStatus === 'PAID')
  if (hasUnpaid) return 2
  if (hasPaid) return 3
  return 1
})

async function loadBlacklist() {
  loadingBlacklist.value = true
  try {
    blacklist.value = await apiMyBlacklist()
  } catch {
    blacklist.value = null
  } finally {
    loadingBlacklist.value = false
  }
}

async function loadSchedules() {
  loadingSchedules.value = true
  try {
    schedules.value = await apiListSchedules({
      date: query.date,
      timePeriod: query.timePeriod || undefined,
      status: query.onlyOpen ? 'OPEN' : undefined,
    })
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载排班失败')
  } finally {
    loadingSchedules.value = false
  }
}

async function loadOrders() {
  loadingOrders.value = true
  try {
    orders.value = await apiMyRegistrations()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载挂号单失败')
  } finally {
    loadingOrders.value = false
  }
}

async function book(row: Schedule) {
  try {
    await ElMessageBox.confirm(
      `确认挂号：${row.doctorName} / ${row.scheduleDate} ${timePeriodLabel(row.timePeriod)}？\n\n提示：挂号成功后需在“我的账单”完成支付，医生才能开始就诊。`,
      '确认挂号',
      { type: 'warning' }
    )
    const order = await apiBookRegistration({ scheduleId: row.id, channel: 'APP' })
    ElMessage.success('挂号成功')
    activeTab.value = 'mine'
    await loadSchedules()
    await loadOrders()
    await loadBlacklist()

    try {
      await ElMessageBox.confirm('已生成挂号费账单，是否立即去支付？', '去支付', {
        type: 'info',
        confirmButtonText: '去支付',
        cancelButtonText: '稍后',
      })
      goPay(order)
    } catch {
      // ignore
    }
  } catch {
    // ignore
  }
}

async function cancelOrder(row: RegistrationOrder) {
  try {
    await ElMessageBox.confirm(
      `确认取消挂号：${row.doctorName} / ${row.scheduleDate} ${timePeriodLabel(row.timePeriod)}？\n\n若已支付将自动退款，未支付将自动作废账单。`,
      '取消挂号',
      { type: 'warning' }
    )
    await apiCancelRegistration(row.id)
    ElMessage.success('已取消')
    await loadSchedules()
    await loadOrders()
    await loadBlacklist()
  } catch {
    // ignore
  }
}

const rescheduleVisible = ref(false)
const rescheduleLoading = ref(false)
const rescheduleOrder = ref<RegistrationOrder | null>(null)
const rescheduleDate = ref<string>(new Date().toISOString().slice(0, 10))
const rescheduleCandidates = ref<Schedule[]>([])

async function openReschedule(row: RegistrationOrder) {
  rescheduleOrder.value = row
  rescheduleDate.value = row.scheduleDate
  rescheduleVisible.value = true
  await loadRescheduleCandidates()
}

async function loadRescheduleCandidates() {
  if (!rescheduleOrder.value) return
  rescheduleLoading.value = true
  try {
    const list = await apiListSchedules({ date: rescheduleDate.value, status: 'OPEN' })
    rescheduleCandidates.value = list.filter(
      (s) => s.doctorUserId === rescheduleOrder.value?.doctorUserId && s.feeCents === rescheduleOrder.value?.feeCents
    )
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载可改期排班失败')
    rescheduleCandidates.value = []
  } finally {
    rescheduleLoading.value = false
  }
}

async function doReschedule(targetScheduleId: number) {
  if (!rescheduleOrder.value) return
  try {
    await ElMessageBox.confirm('确认改期？改期后原号源将释放，新号源将占用。', '确认改期', { type: 'warning' })
    await apiRescheduleRegistration(rescheduleOrder.value.id, targetScheduleId)
    ElMessage.success('改期成功')
    rescheduleVisible.value = false
    await loadSchedules()
    await loadOrders()
  } catch {
    // ignore
  }
}

function goPay(row?: RegistrationOrder) {
  if (!row) {
    router.push({ path: '/bills', query: { status: 'UNPAID' } })
    return
  }
  router.push({ path: '/bills', query: { status: 'UNPAID', sourceType: 'REGISTRATION', sourceId: String(row.id) } })
}

onMounted(async () => {
  await loadBlacklist()
  await loadSchedules()
  await loadOrders()
})
</script>

<template>
  <el-space direction="vertical" size="large" fill>
    <el-alert
      v-if="blacklist?.blacklistedUntil"
      v-loading="loadingBlacklist"
      type="error"
      show-icon
      title="挂号受限"
      :description="`因爽约记录，系统暂时限制挂号。解除时间：${blacklist.blacklistedUntil}`"
    />

    <el-card>
      <template #header>挂号流程指引</template>
      <el-steps :active="flowActive" align-center finish-status="success">
        <el-step title="选择排班" />
        <el-step title="提交挂号" />
        <el-step title="支付挂号费" />
        <el-step title="医生就诊" />
        <el-step title="处方缴费" />
        <el-step title="药房发药" />
      </el-steps>
      <div style="margin-top: 8px; opacity: 0.7; line-height: 1.6">
        挂号成功会自动生成“挂号费账单”。未支付时医生端无法开始就诊；处方开立后同样会生成“药品费账单”，缴费后药房才可发药。
      </div>
    </el-card>

    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane name="book" label="预约挂号">
        <el-card>
          <template #header>
            <div style="display: flex; align-items: center; justify-content: space-between; gap: 12px">
              <div style="display: flex; align-items: center; gap: 10px">
                <div>可挂号排班</div>
                <el-tag type="info" effect="plain">支持筛选/快速挂号</el-tag>
              </div>
              <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap">
                <el-input v-model="query.keyword" placeholder="医生姓名关键字" clearable style="width: 180px" />
                <el-select v-model="query.timePeriod" placeholder="时段" clearable style="width: 120px" @change="loadSchedules">
                  <el-option label="上午" value="AM" />
                  <el-option label="下午" value="PM" />
                </el-select>
                <el-date-picker v-model="query.date" type="date" value-format="YYYY-MM-DD" @change="loadSchedules" />
                <el-button size="small" @click="loadSchedules">刷新</el-button>
              </div>
            </div>
          </template>

          <el-table :data="filteredSchedules" v-loading="loadingSchedules" border>
            <el-table-column prop="doctorName" label="医生" width="160" />
            <el-table-column prop="scheduleDate" label="日期" width="120" />
            <el-table-column prop="timePeriod" label="时段" width="90">
              <template #default="{ row }">{{ timePeriodLabel(row.timePeriod) }}</template>
            </el-table-column>
            <el-table-column prop="feeCents" label="挂号费" width="120">
              <template #default="{ row }">{{ moneyYuan(row.feeCents) }}</template>
            </el-table-column>
            <el-table-column prop="capacityRemaining" label="剩余号源" width="100" />
            <el-table-column prop="status" label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="scheduleStatusTag(row.status)" effect="plain">{{ scheduleStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button
                  size="small"
                  type="primary"
                  :disabled="row.capacityRemaining <= 0 || row.status !== 'OPEN' || !!blacklist?.blacklistedUntil"
                  @click="book(row)"
                >
                  挂号
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane name="mine" label="我的挂号">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <div style="display: flex; align-items: center; gap: 10px">
                <div>我的挂号单</div>
                <el-tag type="info" effect="plain">支持取消/改期</el-tag>
              </div>
              <el-button size="small" @click="loadOrders">刷新</el-button>
            </div>
          </template>

          <el-table :data="orders" v-loading="loadingOrders" border>
            <el-table-column prop="serialNo" label="流水号" width="220" />
            <el-table-column prop="doctorName" label="医生" width="160" />
            <el-table-column prop="scheduleDate" label="日期" width="120" />
            <el-table-column prop="timePeriod" label="时段" width="90">
              <template #default="{ row }">{{ timePeriodLabel(row.timePeriod) }}</template>
            </el-table-column>
            <el-table-column prop="feeCents" label="费用" width="120">
              <template #default="{ row }">{{ moneyYuan(row.feeCents) }}</template>
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
            <el-table-column label="操作" width="300">
              <template #default="{ row }">
                <el-button size="small" :disabled="row.billStatus !== 'UNPAID'" @click="goPay(row)">去支付</el-button>
                <el-button
                  size="small"
                  type="warning"
                  plain
                  :disabled="row.status === 'CANCELED' || row.status === 'NO_SHOW' || row.status === 'COMPLETED'"
                  @click="openReschedule(row)"
                >
                  改期
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  plain
                  :disabled="row.status === 'CANCELED' || row.status === 'NO_SHOW' || row.status === 'COMPLETED'"
                  @click="cancelOrder(row)"
                >
                  取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </el-space>

  <el-dialog v-model="rescheduleVisible" title="改期" width="900px">
    <el-form label-position="top">
      <el-form-item label="选择目标日期">
        <el-date-picker v-model="rescheduleDate" type="date" value-format="YYYY-MM-DD" @change="loadRescheduleCandidates" />
      </el-form-item>
    </el-form>

    <el-table :data="rescheduleCandidates" v-loading="rescheduleLoading" border>
      <el-table-column prop="doctorName" label="医生" width="160" />
      <el-table-column prop="scheduleDate" label="日期" width="120" />
      <el-table-column prop="timePeriod" label="时段" width="90">
        <template #default="{ row }">{{ timePeriodLabel(row.timePeriod) }}</template>
      </el-table-column>
      <el-table-column prop="feeCents" label="挂号费" width="120">
        <template #default="{ row }">{{ moneyYuan(row.feeCents) }}</template>
      </el-table-column>
      <el-table-column prop="capacityRemaining" label="剩余号源" width="100" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="primary" :disabled="row.capacityRemaining <= 0" @click="doReschedule(row.id)">选择</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>
