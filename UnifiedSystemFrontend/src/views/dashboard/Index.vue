<template>
  <div class="dashboard">
    <h2 class="page-title">工作台</h2>
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :sm="12" :lg="6">
        <StatCard title="今日销售" :value="'¥' + formatNumber(stats.todaySales)" icon="DollarOutlined" color="#1e3a5f" />
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <StatCard title="待审批单据" :value="stats.pendingApprovals" icon="FileProtectOutlined" color="#d4953a" />
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <StatCard title="今日入库" :value="formatNumber(stats.todayInbound)" unit="件" icon="ImportOutlined" color="#5b8c5a" />
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <StatCard title="待发货订单" :value="stats.pendingShipments" icon="SendOutlined" color="#b44a4a" />
      </a-col>
    </a-row>
    <a-row :gutter="[16, 16]" style="margin-top: 16px">
      <a-col :xs="24" :lg="14">
        <a-card title="近7日销售额趋势" class="chart-card">
          <div ref="salesChartRef" style="height: 320px"></div>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="10">
        <a-card title="待办提醒" class="chart-card">
          <a-list :data-source="todos" :loading="loading" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-badge :status="item.type" :text="item.title" />
                <template #extra>
                  <span class="todo-time">{{ item.time }}</span>
                </template>
              </a-list-item>
            </template>
            <template #empty>
              <a-empty description="暂无待办事项" :image="SimpleEmptyPreset" />
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, h } from 'vue'
import {
  DollarOutlined, FileProtectOutlined, ImportOutlined, SendOutlined
} from '@ant-design/icons-vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import StatCard from '@/components/common/StatCard.vue'
import dayjs from 'dayjs'

echarts.use([LineChart, GridComponent, TooltipComponent, CanvasRenderer])

const loading = ref(false)
const salesChartRef = ref<HTMLElement>()

const stats = ref({
  todaySales: 0,
  pendingApprovals: 0,
  todayInbound: 0,
  pendingShipments: 0
})

const todos = ref<Array<{ type: string; title: string; time: string }>>([])

function formatNumber(val: number | string) {
  return Number(val).toLocaleString()
}

function initChart() {
  if (!salesChartRef.value) return
  const chart = echarts.init(salesChartRef.value)
  const days = Array.from({ length: 7 }, (_, i) =>
    dayjs().subtract(6 - i, 'day').format('MM-DD')
  )
  chart.setOption({
    color: ['#1e3a5f', '#5b8c5a'],
    grid: { top: 20, right: 20, bottom: 30, left: 50 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: days, axisLine: { lineStyle: { color: '#e5e7eb' } } },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f3f4f6', type: 'dashed' } },
      axisLabel: { show: true }
    },
    series: [{
      type: 'line',
      data: [120, 200, 150, 180, 220, 190, 250],
      smooth: true,
      lineStyle: { width: 3 },
      areaStyle: { color: 'rgba(30, 58, 95, 0.06)' }
    }]
  })
}

let resizeObserver: ResizeObserver | null = null

onMounted(() => {
  nextTick(initChart)
  if (salesChartRef.value) {
    resizeObserver = new ResizeObserver(() => {
      const instance = echarts.getInstanceByDom(salesChartRef.value!)
      instance?.resize()
    })
    resizeObserver.observe(salesChartRef.value)
  }
  stats.value = { todaySales: 125600, pendingApprovals: 8, todayInbound: 320, pendingShipments: 12 }
  todos.value = [
    { type: 'processing', title: '采购申请 #PR20260726-001 待审批', time: '10:30' },
    { type: 'warning', title: '销售报单 #SO20260726-003 异常低价', time: '09:15' },
    { type: 'error', title: '调拨单 #TR20260725-007 已超48小时未处理', time: '昨天' }
  ]
})

onUnmounted(() => {
  resizeObserver?.disconnect()
  const instance = salesChartRef.value && echarts.getInstanceByDom(salesChartRef.value)
  instance?.dispose()
})

const SimpleEmptyPreset = h('span')
</script>

<style scoped lang="less">
.dashboard { }
.page-title {
  font-size: 20px;
  color: #1f2937;
  margin-bottom: 24px;
}
.chart-card {
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.todo-time {
  color: #9ca3af;
  font-size: 12px;
}
</style>
