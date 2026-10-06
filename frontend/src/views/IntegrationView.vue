<script setup lang="ts">
import { FirstAidKit, Tickets } from '@element-plus/icons-vue'

type IntegratedModule = {
  title: string
  subtitle: string
  entry: string
  icon: unknown
  links: { label: string; href: string }[]
}

const modules: IntegratedModule[] = [
  {
    title: '门诊挂号模块',
    subtitle: '患者挂号、医生停诊、管理员排班',
    entry: '/outpatient',
    icon: Tickets,
    links: [
      { label: '患者入口', href: '/outpatient/reg/login' },
      { label: '医生工作台', href: '/outpatient/doctor' },
      { label: '排班后台', href: '/outpatient/admin/schedule' },
    ],
  },
  {
    title: '智慧医疗模块',
    subtitle: '就诊管理、药房管理、收费管理',
    entry: '/smart',
    icon: FirstAidKit,
    links: [
      { label: '就诊管理', href: '/smart' },
      { label: '药房管理', href: '/smart/pharmacy' },
      { label: '收费管理', href: '/smart/charge' },
    ],
  },
]
</script>

<template>
  <el-space direction="vertical" size="large" fill class="integration-page">
    <div class="page-head">
      <div>
        <h2>集成模块</h2>
        <p>常用业务入口</p>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col v-for="item in modules" :key="item.title" :xs="24" :lg="12">
        <el-card class="module-card">
          <template #header>
            <div class="card-head">
              <div class="module-title">
                <el-icon size="26"><component :is="item.icon" /></el-icon>
                <div>
                  <h3>{{ item.title }}</h3>
                  <p>{{ item.subtitle }}</p>
                </div>
              </div>
              <el-tag>{{ item.entry }}</el-tag>
            </div>
          </template>

          <div class="actions">
            <el-button v-for="link in item.links" :key="link.href" tag="a" :href="link.href" target="_blank">
              {{ link.label }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </el-space>
</template>

<style scoped>
.integration-page {
  width: 100%;
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-head h2,
.module-title h3 {
  margin: 0;
}

.page-head p,
.module-title p {
  margin: 8px 0 0;
  color: var(--el-text-color-secondary);
}

.module-card {
  min-height: 190px;
}

.card-head,
.module-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-head {
  justify-content: space-between;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

@media (max-width: 720px) {
  .page-head,
  .card-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
