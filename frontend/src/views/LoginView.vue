<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: 'admin123',
})

async function onSubmit() {
  loading.value = true
  try {
    await auth.login(form.username.trim(), form.password)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string | undefined) ?? '/dashboard'
    await router.push(redirect)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="wrap">
    <el-card class="card">
      <template #header>
        <div class="title">登录</div>
      </template>

      <el-form label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="current-password" show-password />
        </el-form-item>

        <el-button type="primary" :loading="loading" style="width: 100%" @click="onSubmit">登录</el-button>

        <div class="hint">
          默认账号（演示用）：admin/admin123、doctor1/doctor123、patient1/patient123、cashier1/cashier123、pharmacist1/pharmacist123
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.wrap {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 16px;
}
.card {
  width: 420px;
  max-width: 100%;
}
.title {
  font-weight: 700;
}
.hint {
  margin-top: 12px;
  font-size: 12px;
  opacity: 0.75;
  line-height: 1.4;
}
</style>

