<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">合同管理</h2>
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
            <template v-if="column.key === 'signStatus'">
              <a-tag :color="record.signStatus === '已签署' ? 'green' : 'orange'">
                {{ record.signStatus || '未签署' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a-space>
                <a @click="viewPdf(record.id)">查看PDF</a>
                <a @click="downloadPdf(record.id)">下载</a>
              </a-space>
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
const dataSource = ref<any[]>([])

const columns = [
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo' },
  { title: '销售单号', dataIndex: 'salesOrderNo', key: 'salesOrderNo' },
  { title: '签署状态', key: 'signStatus', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 160 }
]

const pagination = computed(() => ({
  current: current.value, pageSize: pageSize.value, total: total.value,
  showSizeChanger: true, pageSizeOptions: ['10', '20', '50', '100', '200'],
  showTotal: (t: number) => `共 ${t} 条`
}))

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/sales/contracts', {
      params: { pageNum: current.value, pageSize: pageSize.value }
    }) as any
    dataSource.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

function handlePageChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }

function viewPdf(id: number) {
  window.open(`/api/sales/contracts/${id}/pdf`, '_blank')
}

function downloadPdf(id: number) {
  window.open(`/api/sales/contracts/${id}/download`, '_blank')
}

onMounted(() => { fetchData() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
