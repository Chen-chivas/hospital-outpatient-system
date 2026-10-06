<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { apiDistributions, apiSummary, apiTrends, type Distributions, type Summary, type Trends } from '../api/statistics'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const canSee = computed(() => auth.hasAnyRole(['ADMIN', 'CASHIER']))
const loading = ref(false)
const data = ref<Summary | null>(null)
const trends = ref<Trends | null>(null)
const dist = ref<Distributions | null>(null)

const trendChartEl = ref<HTMLDivElement | null>(null)
const expensePieEl = ref<HTMLDivElement | null>(null)
const patientTypePieEl = ref<HTMLDivElement | null>(null)
const paymentMethodPieEl = ref<HTMLDivElement | null>(null)

let trendChart: echarts.ECharts | null = null
let expensePieChart: echarts.ECharts | null = null
let patientTypePieChart: echarts.ECharts | null = null
let paymentMethodPieChart: echarts.ECharts | null = null

const onResize = () => {
  trendChart?.resize()
  expensePieChart?.resize()
  patientTypePieChart?.resize()
  paymentMethodPieChart?.resize()
}

async function load() {
  if (!canSee.value) return
  loading.value = true
  try {
    data.value = await apiSummary()
    trends.value = await apiTrends(14)
    dist.value = await apiDistributions(30)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

function renderTrendChart() {
  if (!trendChartEl.value || !trends.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartEl.value)
  }
  const points = trends.value.points
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['挂号数', '已收款(分)'] },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: points.map((p) => p.date), axisLabel: { rotate: 30 } },
    yAxis: [
      { type: 'value', name: '挂号数' },
      { type: 'value', name: '金额(分)' },
    ],
    series: [
      { name: '挂号数', type: 'line', smooth: true, data: points.map((p) => p.registrations) },
      { name: '已收款(分)', type: 'bar', yAxisIndex: 1, data: points.map((p) => p.revenuePaidCents) },
    ],
  })
}

function renderPies() {
  if (!dist.value) return

  if (expensePieEl.value && !expensePieChart) {
    expensePieChart = echarts.init(expensePieEl.value)
  }
  if (patientTypePieEl.value && !patientTypePieChart) {
    patientTypePieChart = echarts.init(patientTypePieEl.value)
  }
  if (paymentMethodPieEl.value && !paymentMethodPieChart) {
    paymentMethodPieChart = echarts.init(paymentMethodPieEl.value)
  }

  const moneyYuan = (cents: number) => `￥${(cents / 100).toFixed(2)}`
  const pieOption = (title: string, data: { name: string; value: number }[], valueUnit?: 'cents' | 'count') => ({
    title: { text: title, left: 'center' },
    tooltip: {
      trigger: 'item',
      formatter: (p: any) => {
        const name = p?.name ?? '-'
        const value = Number(p?.value ?? 0)
        const percent = p?.percent ?? 0
        const formatted =
          valueUnit === 'cents' ? moneyYuan(value) : valueUnit === 'count' ? `${value} 人` : String(value)
        return `${name}<br/>${formatted}（${percent}%）`
      },
    },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '48%'],
        stillShowZeroSum: false,
        data,
      },
    ],
  })

  expensePieChart?.setOption(pieOption('费用结构', dist.value.expenseStructure, 'cents'))
  patientTypePieChart?.setOption(pieOption('患者类型', dist.value.patientTypes, 'count'))
  paymentMethodPieChart?.setOption(pieOption('支付方式', dist.value.paymentMethods, 'cents'))
}

onMounted(load)

watch(
  trends,
  () => {
    renderTrendChart()
  },
  { flush: 'post' }
)

watch(
  dist,
  () => {
    renderPies()
  },
  { flush: 'post' }
)

onMounted(() => {
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  trendChart?.dispose()
  expensePieChart?.dispose()
  patientTypePieChart?.dispose()
  paymentMethodPieChart?.dispose()
  trendChart = null
  expensePieChart = null
  patientTypePieChart = null
  paymentMethodPieChart = null
})
</script>

<template>
  <el-space direction="vertical" size="large" fill>
    <div>
      <h2 style="margin: 0 0 8px">概览</h2>
      <div style="opacity: 0.7">用于演示与验收的核心流程：挂号 → 收费 → 就诊 → 处方 → 再收费 → 发药。</div>
    </div>

    <el-alert v-if="!canSee" type="info" show-icon title="该账号没有统计权限（仅管理员/收费员可查看）。">
      <template #default>
        <div style="line-height: 1.6">
          你可以从左侧菜单进入对应模块体验完整流程（例如：患者挂号/缴费、医生就诊/开方、药师发药）。
        </div>
      </template>
    </el-alert>

    <el-skeleton v-if="loading" :rows="6" animated />

    <el-row v-else-if="data" :gutter="12">
      <el-col :xs="12" :sm="8" :md="4"><el-card>用户数：{{ data.users }}</el-card></el-col>
      <el-col :xs="12" :sm="8" :md="4"><el-card>排班数：{{ data.schedules }}</el-card></el-col>
      <el-col :xs="12" :sm="8" :md="4"><el-card>挂号单：{{ data.registrations }}</el-card></el-col>
      <el-col :xs="12" :sm="8" :md="4"><el-card>就诊数：{{ data.visits }}</el-card></el-col>
      <el-col :xs="12" :sm="8" :md="4"><el-card>处方数：{{ data.prescriptions }}</el-card></el-col>
      <el-col :xs="12" :sm="8" :md="4"><el-card>已收款(分)：{{ data.revenuePaidCents }}</el-card></el-col>
    </el-row>

    <el-card v-if="canSee && trends">
      <template #header>近 {{ trends.days }} 天趋势</template>
      <div ref="trendChartEl" style="height: 320px; width: 100%" />
    </el-card>

    <el-card v-if="canSee && dist">
      <template #header>近 {{ dist.days }} 天占比</template>
      <el-row :gutter="12">
        <el-col :xs="24" :sm="12" :md="8">
          <div ref="expensePieEl" style="height: 260px; width: 100%" />
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div ref="patientTypePieEl" style="height: 260px; width: 100%" />
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div ref="paymentMethodPieEl" style="height: 260px; width: 100%" />
        </el-col>
      </el-row>
    </el-card>
  </el-space>
</template>
