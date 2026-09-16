<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">发货管理</h2>
      <a-button type="primary" @click="openCreate">
        <template #icon><PlusOutlined /></template>
        新建发货
      </a-button>
    </div>
    <a-card class="content-card">
      <a-tabs v-model:activeKey="activeTab" @change="fetchData">
        <a-tab-pane key="all" tab="全部" />
        <a-tab-pane key="pending" tab="待发货" />
        <a-tab-pane key="shipped" tab="已发货" />
      </a-tabs>
      <a-spin :spinning="loading">
        <a-table :columns="columns" :data-source="dataSource" row-key="id"
          :pagination="{ current, pageSize, total, showSizeChanger: true, pageSizeOptions: ['10','20','50','100','200'] }"
          @change="handleTableChange">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'printStatus'">
              <a-tag :color="record.printStatus === '1' ? 'green' : 'default'">
                {{ record.printStatus === '1' ? '已打印' : '未打印' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="statusColor[record.status]">{{ statusMap[record.status] || record.status }}</a-tag>
            </template>
            <template v-if="column.key === 'createTime'">
              {{ record.createTime ? new Date(record.createTime).toLocaleString('zh-CN') : '-' }}
            </template>
            <template v-if="column.key === 'action'">
              <a @click="handlePrint(record)">打印</a>
              <a-divider type="vertical" />
              <a @click="handleGenPicking(record)">生成拣货</a>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <a-modal v-model:open="modalVisible" title="新建发货" width="800px" @ok="handleSubmit"
      :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 6 }">
        <a-form-item label="销售订单ID">
          <a-input v-model:value="formState.salesOrderId" placeholder="输入销售订单ID" />
        </a-form-item>
        <a-form-item label="发货方式">
          <a-select v-model:value="formState.shippingMethod" :options="shippingMethodOptions" style="width:100%" />
        </a-form-item>
        <a-divider>发货明细</a-divider>
        <a-table :columns="itemColumns" :data-source="formState.items" size="small" row-key="tempId" :pagination="false">
          <template #bodyCell="{ column, record, index }">
            <template v-if="column.key === 'product'">
              <a-select v-model:value="record.productId" :options="productOptions" style="width:100%" placeholder="选择产品" />
            </template>
            <template v-if="column.key === 'quantity'">
              <a-input-number v-model:value="record.quantity" :min="1" style="width:100%" />
            </template>
            <template v-if="column.key === 'batchNo'">
              <a-input v-model:value="record.batchNo" />
            </template>
            <template v-if="column.key === 'action'">
              <a @click="removeItem(index)" class="danger-link">删除</a>
            </template>
          </template>
        </a-table>
        <a-button type="dashed" block @click="addItem" style="margin-top:8px">
          <template #icon><PlusOutlined /></template> 添加产品
        </a-button>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import request from '@/api/request'

const activeTab = ref('all')
const loading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dataSource = ref<any[]>([])

const statusMap: Record<string, string> = { pending: '待发货', shipped: '已发货', delivered: '已签收' }
const statusColor: Record<string, string> = { pending: 'orange', shipped: 'blue', delivered: 'green' }
const shippingMethodOptions = [
  { label: '自提', value: 'self' },
  { label: '快递', value: 'express' },
  { label: '物流', value: 'logistics' }
]

const columns = [
  { title: '发货编号', dataIndex: 'shippingNo', width: 160 },
  { title: '销售订单ID', dataIndex: 'salesOrderId', width: 120 },
  { title: '发货方式', dataIndex: 'shippingMethod', width: 100 },
  { title: '打印状态', key: 'printStatus', width: 90 },
  { title: '状态', key: 'status', width: 80 },
  { title: '创建时间', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 160 }
]

const itemColumns = [
  { title: '产品', key: 'product', width: 200 },
  { title: '数量', key: 'quantity', width: 120 },
  { title: '批次号', key: 'batchNo', width: 160 },
  { title: '操作', key: 'action', width: 60 }
]

const productOptions = ref<Array<{ label: string; value: number }>>([])
const modalVisible = ref(false)
const submitting = ref(false)

const formState = reactive<{ salesOrderId: string; shippingMethod: string; items: any[] }>({
  salesOrderId: '', shippingMethod: 'express', items: []
})
let tempId = 0

function addItem() { formState.items.push({ tempId: ++tempId, productId: null, quantity: null, batchNo: '' }) }
function removeItem(index: number) { formState.items.splice(index, 1) }

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: current.value, pageSize: pageSize.value }
    if (activeTab.value !== 'all') params.status = activeTab.value
    const res = await request.get('/logistics/shippings', params) as any
    dataSource.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

async function fetchProducts() {
  try {
    const res = await request.get('/production/products', { pageNum: 1, pageSize: 200 }) as any
    const list = res.data?.records || []
    productOptions.value = list.map((p: any) => ({ label: p.productName || p.materialName, value: p.id }))
  } catch { }
}

function handleTableChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }

function openCreate() { formState.salesOrderId = ''; formState.shippingMethod = 'express'; formState.items = []; modalVisible.value = true }

async function handleSubmit() {
  if (submitting.value) return
  if (!formState.salesOrderId.trim()) { message.warning('请输入销售订单ID'); return }
  if (formState.items.length === 0) { message.warning('请添加产品'); return }
  submitting.value = true
  try {
    const items = formState.items.map(i => ({ productId: i.productId, quantity: i.quantity, batchNo: i.batchNo }))
    await request.post('/logistics/shippings', {
      salesOrderId: formState.salesOrderId,
      shippingMethod: formState.shippingMethod,
      items
    })
    message.success('创建成功')
    modalVisible.value = false; fetchData()
  } catch { } finally { submitting.value = false }
}

async function handlePrint(record: any) {
  try {
    await request.put(`/logistics/shippings/${record.id}/print`)
    message.success('打印标记成功')
    fetchData()
  } catch { }
}

async function handleGenPicking(record: any) {
  try {
    await request.post('/logistics/pickings', null, { params: { shippingId: record.id } })
    message.success('拣货生成成功')
  } catch { }
}

onMounted(() => { fetchData(); fetchProducts() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
