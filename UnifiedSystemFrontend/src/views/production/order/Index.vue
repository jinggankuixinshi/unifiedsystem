<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">工单管理</h2>
    </div>
    <a-card class="content-card">
      <a-spin :spinning="loading">
        <a-table
          :columns="columns"
          :data-source="dataSource"
          row-key="id"
          :pagination="pagination"
          @change="handlePageChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'plannedOutput' || column.key === 'actualOutput'">
              {{ record[column.key] }} {{ record.unit || '' }}
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="statusColor[record.status]">
                {{ statusMap[record.status] || record.status }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import request from '@/api/request'

const loading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusMap: Record<number, string> = { 0: '待开工', 1: '生产中', 2: '已完工', 3: '异常' }
const statusColor: Record<number, string> = { 0: 'default', 1: 'blue', 2: 'green', 3: 'red' }
const dataSource = ref<any[]>([])

const columns = [
  { title: '工单编号', dataIndex: 'orderNo', key: 'orderNo' },
  { title: '产品名称', dataIndex: 'productName', key: 'productName' },
  { title: '计划产量', key: 'plannedOutput', width: 100 },
  { title: '实际产量', key: 'actualOutput', width: 100 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 180 },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 180 },
  { title: '状态', key: 'status', width: 80 }
]

const pagination = computed(() => ({
  current: current.value, pageSize: pageSize.value, total: total.value,
  showSizeChanger: true, pageSizeOptions: ['10', '20', '50', '100', '200'],
  showTotal: (t: number) => `共 ${t} 条`
}))

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/production/orders', {
      pageNum: current.value, pageSize: pageSize.value
    }) as any
    dataSource.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { message.error('加载工单失败') } finally { loading.value = false }
}

function handlePageChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }

onMounted(() => { fetchData() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
