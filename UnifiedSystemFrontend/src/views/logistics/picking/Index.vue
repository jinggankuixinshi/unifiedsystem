<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">拣货管理</h2>
      <a-button type="primary" @click="openGenerate">
        <template #icon><PlusOutlined /></template>
        生成拣货
      </a-button>
    </div>
    <a-card class="content-card">
      <a-spin :spinning="loading">
        <a-table :columns="columns" :data-source="dataSource" row-key="id"
          :pagination="{ current, pageSize, total, showSizeChanger: true, pageSizeOptions: ['10','20','50','100','200'] }"
          @change="handleTableChange">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="statusColor[record.status]">{{ statusMap[record.status] || record.status }}</a-tag>
            </template>
            <template v-if="column.key === 'createTime'">
              {{ record.createTime ? new Date(record.createTime).toLocaleString('zh-CN') : '-' }}
            </template>
            <template v-if="column.key === 'action'">
              <a @click="viewItems(record)">查看明细</a>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <a-modal v-model:open="modalVisible" title="生成拣货" @ok="handleGenerate"
      :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 6 }">
        <a-form-item label="发货单">
          <a-select v-model:value="selectedShippingId" :options="shippingOptions" placeholder="选择发货单" style="width:100%" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="itemsVisible" title="拣货明细" :footer="null" width="700px">
      <a-table :columns="itemColumns" :data-source="pickingItems" row-key="id" size="small" :pagination="false" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import request from '@/api/request'

const loading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dataSource = ref<any[]>([])

const statusMap: Record<number, string> = { 0: '待拣货', 1: '拣货中', 2: '已完成' }
const statusColor: Record<number, string> = { 0: 'blue', 1: 'orange', 2: 'green' }

const columns = [
  { title: '拣货编号', dataIndex: 'pickingNo', width: 160 },
  { title: '发货单ID', dataIndex: 'shippingId', width: 100 },
  { title: '操作员', dataIndex: 'operatorId', width: 100 },
  { title: '状态', key: 'status', width: 80 },
  { title: '创建时间', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 100 }
]

const itemColumns = [
  { title: '产品ID', dataIndex: 'productId' },
  { title: '批次号', dataIndex: 'batchNo' },
  { title: '数量', dataIndex: 'quantity' },
  { title: '库位编码', dataIndex: 'locationCode' }
]

const modalVisible = ref(false)
const itemsVisible = ref(false)
const submitting = ref(false)
const selectedShippingId = ref<number>()
const pickingItems = ref<any[]>([])
const shippingOptions = ref<Array<{ label: string; value: number }>>([])

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/logistics/pickings', { pageNum: current.value, pageSize: pageSize.value }) as any
    dataSource.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

async function fetchShippings() {
  try {
    const res = await request.get('/logistics/shippings', { pageNum: 1, pageSize: 200 }) as any
    const list = res.data?.records || []
    shippingOptions.value = list.map((s: any) => ({ label: `${s.shippingNo || s.id}`, value: s.id }))
  } catch { }
}

function handleTableChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }

function openGenerate() {
  selectedShippingId.value = undefined
  fetchShippings()
  modalVisible.value = true
}

async function handleGenerate() {
  if (!selectedShippingId.value) { message.warning('请选择发货单'); return }
  submitting.value = true
  try {
    await request.post('/logistics/pickings', null, { params: { shippingId: selectedShippingId.value } })
    message.success('拣货生成成功')
    modalVisible.value = false; fetchData()
  } catch { } finally { submitting.value = false }
}

async function viewItems(record: any) {
  try {
    const res = await request.get(`/logistics/pickings/${record.id}/items`) as any
    pickingItems.value = res.data?.records || res.data || []
    itemsVisible.value = true
  } catch { }
}

onMounted(() => { fetchData() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
