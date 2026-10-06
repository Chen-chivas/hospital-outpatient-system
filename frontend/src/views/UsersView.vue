<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiCreateUser, apiListUsers, apiResetPassword, apiUpdateUserStatus, type User } from '../api/users'
import { patientTypeLabel, roleLabel, userStatusLabel, userStatusTag } from '../utils/labels'

const loading = ref(false)
const users = ref<User[]>([])

const query = reactive({
  keyword: '',
  role: '' as '' | string,
  status: '' as '' | 'ACTIVE' | 'DISABLED',
})

const form = reactive({
  username: '',
  password: '',
  displayName: '',
  roles: [] as string[],
  patientType: '' as '' | string,
})

const roleOptions = ['ADMIN', 'DOCTOR', 'NURSE', 'CASHIER', 'PHARMACIST', 'PATIENT']
const roleSelectOptions = roleOptions.map((r) => ({ label: roleLabel(r), value: r }))
const patientTypeOptions = [
  { label: '自费', value: 'SELF_PAY' },
  { label: '医保', value: 'INSURANCE' },
  { label: '商保', value: 'COMMERCIAL' },
  { label: '其他', value: 'OTHER' },
]
const isPatient = computed(() => form.roles.includes('PATIENT'))

const filtered = computed(() => {
  const kw = query.keyword.trim().toLowerCase()
  return users.value.filter((u) => {
    if (query.status && u.status !== query.status) return false
    if (query.role && !u.roles.includes(query.role)) return false
    if (kw) {
      const hay = `${u.username} ${u.displayName}`.toLowerCase()
      if (!hay.includes(kw)) return false
    }
    return true
  })
})

async function load() {
  loading.value = true
  try {
    users.value = await apiListUsers()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

async function create() {
  try {
    await apiCreateUser({
      username: form.username.trim(),
      password: form.password,
      displayName: form.displayName.trim(),
      roles: form.roles,
      patientType: isPatient.value && form.patientType ? form.patientType : undefined,
    })
    ElMessage.success('创建成功')
    form.username = ''
    form.password = ''
    form.displayName = ''
    form.roles = []
    form.patientType = ''
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '创建失败')
  }
}

async function toggleStatus(u: User) {
  const next = u.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const nextLabel = userStatusLabel(next)
  try {
    await ElMessageBox.confirm(`确认将用户「${u.username}」状态修改为：${nextLabel}？`, '确认操作', { type: 'warning' })
    await apiUpdateUserStatus(u.id, next)
    ElMessage.success('状态已更新')
    await load()
  } catch {
    // ignore
  }
}

async function resetPassword(u: User) {
  try {
    const { value } = await ElMessageBox.prompt(`为用户「${u.username}」设置新密码`, '重置密码', {
      inputType: 'password',
      inputPlaceholder: '至少 6 位',
      inputValidator: (v) => (v && v.length >= 6 ? true : '密码至少 6 位'),
    })
    await apiResetPassword(u.id, value)
    ElMessage.success('密码已重置')
  } catch {
    // ignore
  }
}

onMounted(load)
</script>

<template>
  <el-space direction="vertical" size="large" fill>
    <el-card>
      <template #header>创建用户</template>
      <el-form label-position="top">
        <el-row :gutter="12">
          <el-col :span="6">
            <el-form-item label="用户名">
              <el-input v-model="form.username" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="密码">
              <el-input v-model="form.password" type="password" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="显示名">
              <el-input v-model="form.displayName" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="角色">
              <el-select v-model="form.roles" multiple filterable style="width: 100%">
                <el-option v-for="r in roleSelectOptions" :key="r.value" :label="r.label" :value="r.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="6">
            <el-form-item label="患者类型">
              <el-select v-model="form.patientType" placeholder="仅患者可选" clearable style="width: 100%" :disabled="!isPatient">
                <el-option v-for="o in patientTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-button type="primary" @click="create">创建</el-button>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px">
          <div>用户列表</div>
          <div style="display: flex; gap: 8px; align-items: center">
            <el-input v-model="query.keyword" placeholder="搜索 用户名/显示名" clearable style="width: 220px" />
            <el-select v-model="query.role" placeholder="角色" clearable style="width: 140px">
              <el-option v-for="r in roleSelectOptions" :key="r.value" :label="r.label" :value="r.value" />
            </el-select>
            <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
              <el-option label="启用" value="ACTIVE" />
              <el-option label="禁用" value="DISABLED" />
            </el-select>
            <el-button size="small" @click="load">刷新</el-button>
          </div>
        </div>
      </template>
      <el-table :data="filtered" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="displayName" label="显示名" />
        <el-table-column prop="patientType" label="患者类型" width="110">
          <template #default="{ row }">{{ patientTypeLabel(row.patientType) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="userStatusTag(row.status)" effect="plain">{{ userStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色">
          <template #default="{ row }">
            <el-tag v-for="r in row.roles" :key="r" style="margin-right: 6px" size="small">{{ roleLabel(r) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button size="small" @click="toggleStatus(row)">{{ row.status === 'ACTIVE' ? '禁用' : '启用' }}</el-button>
            <el-button size="small" type="warning" plain @click="resetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </el-space>
</template>
