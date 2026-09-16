<template>
  <div class="stat-card">
    <div class="stat-icon" :style="{ background: color + '14', color }">
      <component :is="icon" />
    </div>
    <div class="stat-content">
      <span class="stat-label">{{ title }}</span>
      <span class="stat-value">{{ value }}<small v-if="unit">{{ unit }}</small></span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { h } from 'vue'
import * as icons from '@ant-design/icons-vue'

const props = defineProps<{
  title: string
  value: string | number
  icon: string
  color: string
  unit?: string
}>()

const icon = h((icons as any)[props.icon] || icons.AppstoreOutlined)
</script>

<style scoped lang="less">
.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}
.stat-content {
  margin-left: 16px;
  display: flex;
  flex-direction: column;
}
.stat-label {
  font-size: 13px;
  color: #9ca3af;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
  small {
    font-size: 14px;
    font-weight: 400;
    color: #9ca3af;
    margin-left: 4px;
  }
}
</style>
