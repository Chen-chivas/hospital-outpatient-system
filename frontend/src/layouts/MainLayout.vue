<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { roleLabel } from '../utils/labels'
import {
  Calendar,
  CircleClose,
  Coin,
  Document,
  Fold,
  HomeFilled,
  List,
  Money,
  Odometer,
  Operation,
  Setting,
  Tickets,
  User,
} from '@element-plus/icons-vue'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const isCollapse = ref(false)
const active = computed(() => route.path)
const roles = computed(() => new Set(auth.me?.roles ?? []))
const can = (r: string[]) => r.some((x) => roles.value.has(x))
const roleText = computed(() => (auth.me?.roles ?? []).map((r) => roleLabel(r)).join('，'))

const crumbs = computed(() =>
  route.matched
    .filter((m) => typeof m.meta?.title === 'string')
    .map((m) => ({ path: m.path, title: m.meta.title as string }))
)

async function onLogout() {
  auth.logout()
  await router.push('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-aside class="aside" :width="isCollapse ? '64px' : '220px'">
      <div class="brand" :class="{ collapsed: isCollapse }">
        <span class="brand-icon">OCS</span>
        <span v-if="!isCollapse" class="brand-title">门诊管理</span>
      </div>
      <el-scrollbar class="menu-scroll">
        <el-menu :default-active="active" router :collapse="isCollapse" :collapse-transition="false">
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>概览</span>
          </el-menu-item>

          <el-menu-item v-if="can(['ADMIN'])" index="/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>

          <el-menu-item v-if="can(['ADMIN'])" index="/schedules">
            <el-icon><Calendar /></el-icon>
            <span>排班管理</span>
          </el-menu-item>

          <el-menu-item v-if="can(['ADMIN'])" index="/stop-clinic">
            <el-icon><CircleClose /></el-icon>
            <span>停诊审核</span>
          </el-menu-item>

          <el-menu-item v-if="can(['PATIENT'])" index="/patient/registrations">
            <el-icon><Tickets /></el-icon>
            <span>挂号</span>
          </el-menu-item>

          <el-menu-item v-if="can(['PATIENT'])" index="/bills">
            <el-icon><Coin /></el-icon>
            <span>我的账单</span>
          </el-menu-item>

          <el-menu-item v-if="can(['DOCTOR'])" index="/doctor/registrations">
            <el-icon><List /></el-icon>
            <span>挂号列表</span>
          </el-menu-item>

          <el-menu-item v-if="can(['DOCTOR'])" index="/visits">
            <el-icon><Document /></el-icon>
            <span>就诊管理</span>
          </el-menu-item>

          <el-menu-item v-if="can(['PHARMACIST', 'ADMIN'])" index="/pharmacy">
            <el-icon><HomeFilled /></el-icon>
            <span>药房管理</span>
          </el-menu-item>

          <el-menu-item v-if="can(['CASHIER', 'ADMIN'])" index="/cashier/bills">
            <el-icon><Money /></el-icon>
            <span>收费管理</span>
          </el-menu-item>

          <el-menu-item v-if="can(['ADMIN'])" index="/audit">
            <el-icon><Setting /></el-icon>
            <span>审计日志</span>
          </el-menu-item>

          <el-menu-item index="/integrations">
            <el-icon><Operation /></el-icon>
            <span>集成模块</span>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="left">
          <el-button text class="collapse-btn" @click="isCollapse = !isCollapse">
            <el-icon><Fold /></el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="c in crumbs" :key="c.path">{{ c.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <el-dropdown trigger="click">
          <div class="profile">
            <div class="profile-main">
              <div class="name">{{ auth.me?.username }}</div>
              <div class="roles">{{ roleText }}</div>
            </div>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="onLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  min-height: 100vh;
}
.aside {
  border-right: 1px solid var(--el-border-color);
  background: var(--el-bg-color);
  transition: width 0.2s ease;
}
.brand {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  border-bottom: 1px solid var(--el-border-color);
  gap: 10px;
}
.brand-icon {
  height: 28px;
  width: 36px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  color: var(--el-color-primary);
  background: rgba(64, 158, 255, 0.12);
  font-weight: 800;
  letter-spacing: 0.5px;
}
.brand-title {
  font-weight: 800;
  letter-spacing: 0.5px;
}
.brand.collapsed {
  justify-content: center;
}
.menu-scroll {
  height: calc(100vh - 56px);
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--el-border-color);
}
.left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}
.collapse-btn {
  padding: 8px;
}
.profile {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 10px;
}
.profile:hover {
  background: rgba(0, 0, 0, 0.04);
}
.profile-main {
  display: flex;
  flex-direction: column;
}
.name {
  font-weight: 600;
}
.roles {
  opacity: 0.7;
  font-size: 12px;
}
.main {
  padding: 16px;
}
</style>
