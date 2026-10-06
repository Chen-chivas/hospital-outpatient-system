<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div style="min-height: 60vh; display: grid; place-items: center">
    <el-empty description="页面不存在">
      <el-button type="primary" @click="router.push('/dashboard')">返回首页</el-button>
    </el-empty>
  </div>
</template>

