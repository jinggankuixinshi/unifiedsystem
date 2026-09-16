<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">物流质检</h2>
    </div>
    <a-card class="content-card">
      <a-spin :spinning="loading">
        <a-table :columns="columns" :data-source="dataSource" row-key="id"
          :pagination="{ current, pageSize, total, showSizeChanger: true, pageSizeOptions: ['10','20','50','100','200'] }"
          @change="handleTableChange">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'checkType'">
              <a-tag :color="record.checkType === 'inbound' ? 'blue' : 'purple'">
                {{ record.checkType === 'inbound' ? '入库质检' : record.checkType === 'outbound' ? '出库质检' : record.checkType }}
              </a-tag>
            </template>
            <template v-if="column.key === 'result'">
              <a-tag :color="record.result === 'qualified' ? 'green' : 'red'">
                {{ record.result === 'qualified' ? '合格' : record.result === 'unqualified' ? '不合格' : record.result }}
              </a-tag>
            </template>
            <template v-if="column.key === 'checkTime'">
              {{ record.checkTime ? new Date(record.checkTime).toLocaleString('zh-CN') : '-' }}
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const loading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dataSource = ref<any[]>([])

const columns = [
  { title: '检验类型', key: 'checkType', width: 100 },
  { title: '产品信息', dataIndex: 'productId', width: 120 },
  { title: 'AQL标准', dataIndex: 'aqlStandard', width: 100 },
  { title: '抽样数量', dataIndex: 'sampleQuantity', width: 100 },
  { title: '合格数量', dataIndex: 'qualifiedQuantity', width: 100 },
  { title: '不合格数量', dataIndex: 'unqualifiedQuantity', width: 100 },
  { title: '检验结果', key: 'result', width: 90 },
  { title: '检验员', dataIndex: 'checkerId', width: 100 },
  { title: '检验时间', key: 'checkTime', width: 180 }
]

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/logistics/warehouse', {
      pageNum: current.value, pageSize: pageSize.value, includeQuality: true
    }) as any
    dataSource.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

function handleTableChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }

onMounted(() => { fetchData() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
