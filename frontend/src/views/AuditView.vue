<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRecentAudit, type AuditLog } from '../api/audit'
import { auditActionLabel, auditModuleLabel } from '../utils/labels'

const loading = ref(false)
const logs = ref<AuditLog[]>([])

const query = reactive({
  keyword: '',
})
const page = ref(1)
const pageSize = ref(10)

const filtered = computed(() => {
  const kw = query.keyword.trim().toLowerCase()
  if (!kw) return logs.value
  return logs.value.filter((l) => {
    const hay = `${l.actorName ?? ''} ${l.module} ${l.action} ${l.entityType ?? ''} ${l.detailsJson ?? ''}`.toLowerCase()
    return hay.includes(kw)
  })
})

const paged = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filtered.value.slice(start, start + pageSize.value)
})

async function load() {
  loading.value = true
  try {
    logs.value = await apiRecentAudit()
    page.value = 1
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>最近审计日志（Top 50）</div>
        <div style="display: flex; gap: 8px; align-items: center">
          <el-input v-model="query.keyword" clearable placeholder="搜索 操作者/模块/动作/详情" style="width: 240px" />
          <el-button size="small" @click="load">刷新</el-button>
        </div>
      </div>
    </template>

    <el-table :data="paged" v-loading="loading" border>
      <el-table-column prop="createdAt" label="时间" width="220" />
      <el-table-column prop="actorName" label="操作者" width="120" />
      <el-table-column prop="module" label="模块" width="120">
        <template #default="{ row }">{{ auditModuleLabel(row.module) }}</template>
      </el-table-column>
      <el-table-column prop="action" label="动作" width="120">
        <template #default="{ row }">{{ auditActionLabel(row.action) }}</template>
      </el-table-column>
      <el-table-column prop="entityType" label="对象" width="140" />
      <el-table-column prop="entityId" label="对象ID" width="100" />
      <el-table-column prop="detailsJson" label="详情" />
    </el-table>

    <div style="display: flex; justify-content: flex-end; margin-top: 12px">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        :total="filtered.length"
      />
    </div>
  </el-card>
</template>
