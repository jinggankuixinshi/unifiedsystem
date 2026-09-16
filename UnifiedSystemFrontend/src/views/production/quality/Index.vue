<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">质检管理</h2>
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
            <template v-if="column.key === 'checkType'">
              <a-tag :color="record.checkType === '采购入库' ? 'blue' : 'purple'">
                {{ record.checkType || '-' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'result'">
              <a-tag :color="record.result === '合格' ? 'green' : 'red'">
                {{ record.result || '-' }}
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
const dataSource = ref<any[]>([])

const columns = [
  { title: '检验类型', key: 'checkType', width: 100 },
  { title: '仓库/库位', dataIndex: 'warehouseName', key: 'warehouseName' },
  { title: 'AQL标准', dataIndex: 'aqlStandard', key: 'aqlStandard', width: 100 },
  { title: '抽样数量', dataIndex: 'sampleQuantity', key: 'sampleQuantity', width: 100 },
  { title: '合格数量', dataIndex: 'qualifiedQuantity', key: 'qualifiedQuantity', width: 100 },
  { title: '不合格数量', dataIndex: 'unqualifiedQuantity', key: 'unqualifiedQuantity', width: 100 },
  { title: '检验结果', key: 'result', width: 80 },
  { title: '检验人', dataIndex: 'checkerName', key: 'checkerName', width: 100 },
  { title: '检验时间', dataIndex: 'checkTime', key: 'checkTime', width: 180 }
]

const pagination = computed(() => ({
  current: current.value, pageSize: pageSize.value, total: total.value,
  showSizeChanger: true, pageSizeOptions: ['10', '20', '50', '100', '200'],
  showTotal: (t: number) => `共 ${t} 条`
}))

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/production/quality', {
      params: { pageNum: current.value, pageSize: pageSize.value }
    }) as any
    dataSource.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

function handlePageChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }

onMounted(() => { fetchData() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
