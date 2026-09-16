<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">调拨管理</h2>
      <a-button type="primary" @click="openCreate">
        <template #icon><PlusOutlined /></template>
        新建调拨
      </a-button>
    </div>
    <a-card class="content-card">
      <a-tabs v-model:activeKey="activeTab" @change="fetchData">
        <a-tab-pane key="all" tab="全部" />
        <a-tab-pane key="0" tab="待审批" />
        <a-tab-pane key="1" tab="已通过" />
        <a-tab-pane key="2" tab="已驳回" />
      </a-tabs>
      <a-spin :spinning="loading">
        <a-table :columns="columns" :data-source="dataSource" row-key="id"
          :pagination="{ current, pageSize, total, showSizeChanger: true, pageSizeOptions: ['10','20','50','100','200'] }"
          @change="handleTableChange">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'type'">
              <a-tag :color="typeColor[record.type]">{{ typeMap[record.type] || record.type }}</a-tag>
            </template>
            <template v-if="column.key === 'totalValue'">
              ¥{{ Number(record.totalValue || 0).toLocaleString() }}
            </template>
            <template v-if="column.key === 'approvalStatus'">
              <a-tag :color="statusColor[record.approvalStatus]">{{ statusMap[record.approvalStatus] }}</a-tag>
            </template>
            <template v-if="column.key === 'createTime'">
              {{ record.createTime ? new Date(record.createTime).toLocaleString('zh-CN') : '-' }}
            </template>
            <template v-if="column.key === 'action'">
              <a @click="viewDetail(record)">查看</a>
              <a-divider type="vertical" />
              <a v-if="record.approvalStatus === 1" @click="handleReceive(record)">签收</a>
              <span v-else style="color: #ccc">签收</span>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <a-modal v-model:open="modalVisible" title="新建调拨" width="800px" @ok="handleSubmit"
      :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 4 }">
        <a-form-item label="调拨类型">
          <a-radio-group v-model:value="formState.type">
            <a-radio value="normal">常规</a-radio>
            <a-radio value="sample">样品</a-radio>
            <a-radio value="scrap">报废</a-radio>
            <a-radio value="repair">返修</a-radio>
            <a-radio value="gift">赠送</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="备注" v-if="formState.type !== 'normal'">
          <a-textarea v-model:value="formState.remark" :rows="2" placeholder="特殊类型调拨请填写备注" />
        </a-form-item>
        <a-divider>调拨明细</a-divider>
        <a-table :columns="itemColumns" :data-source="formState.items" size="small" row-key="tempId" :pagination="false">
          <template #bodyCell="{ column, record, index }">
            <template v-if="column.key === 'product'">
              <a-select v-model:value="record.productId" :options="productOptions" style="width:100%" placeholder="选择产品" />
            </template>
            <template v-if="column.key === 'quantity'">
              <a-input-number v-model:value="record.quantity" :min="1" style="width:100%" />
            </template>
            <template v-if="column.key === 'unitValue'">
              <a-input-number v-model:value="record.unitValue" :min="0" :precision="2" style="width:100%" />
            </template>
            <template v-if="column.key === 'subtotal'">
              ¥{{ ((record.quantity || 0) * (record.unitValue || 0)).toLocaleString() }}
            </template>
            <template v-if="column.key === 'action'">
              <a @click="removeItem(index)" class="danger-link">删除</a>
            </template>
          </template>
        </a-table>
        <a-button type="dashed" block @click="addItem" style="margin-top:8px">
          <template #icon><PlusOutlined /></template> 添加
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

const typeMap: Record<string, string> = { normal: '常规', sample: '样品', scrap: '报废', repair: '返修', gift: '赠送' }
const typeColor: Record<string, string> = { normal: 'blue', sample: 'purple', scrap: 'red', repair: 'orange', gift: 'cyan' }
const statusMap: Record<number, string> = { 0: '待审批', 1: '已通过', 2: '已驳回' }
const statusColor: Record<number, string> = { 0: 'orange', 1: 'green', 2: 'red' }

const columns = [
  { title: '调拨编号', dataIndex: 'transferNo', width: 160 },
  { title: '类型', key: 'type', width: 80 },
  { title: '来源仓库', dataIndex: 'fromWarehouseId', width: 100 },
  { title: '目标仓库', dataIndex: 'toWarehouseId', width: 100 },
  { title: '总价值', key: 'totalValue', width: 120 },
  { title: '状态', key: 'approvalStatus', width: 80 },
  { title: '创建时间', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 130 }
]

const itemColumns = [
  { title: '产品', key: 'product', width: 200 },
  { title: '数量', key: 'quantity', width: 120 },
  { title: '单价', key: 'unitValue', width: 140 },
  { title: '小计', key: 'subtotal', width: 120 },
  { title: '操作', key: 'action', width: 60 }
]

const productOptions = ref<Array<{ label: string; value: number }>>([])
const modalVisible = ref(false)
const submitting = ref(false)

const formState = reactive<{ type: string; remark: string; items: any[] }>({ type: 'normal', remark: '', items: [] })
let tempId = 0

function addItem() { formState.items.push({ tempId: ++tempId, productId: null, quantity: null, unitValue: null }) }
function removeItem(index: number) { formState.items.splice(index, 1) }

async function fetchData() {
  loading.value = true
  try {
    const status = activeTab.value === 'all' ? undefined : Number(activeTab.value)
    const res = await request.get('/logistics/transfers', { pageNum: current.value, pageSize: pageSize.value, status }) as any
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
function openCreate() { formState.type = 'normal'; formState.remark = ''; formState.items = []; modalVisible.value = true }
function viewDetail(record: any) { message.info('详情功能开发中') }

async function handleReceive(record: any) {
  try {
    await request.put(`/logistics/transfers/${record.id}/receive`)
    message.success('签收成功')
    fetchData()
  } catch { }
}

async function handleSubmit() {
  if (submitting.value) return
  if (formState.type !== 'normal' && !formState.remark.trim()) { message.warning('特殊类型调拨请填写备注'); return }
  if (formState.items.length === 0) { message.warning('请至少添加一条明细'); return }
  submitting.value = true
  try {
    const items = formState.items.map(i => ({
      productId: i.productId, quantity: i.quantity, unitValue: i.unitValue
    }))
    await request.post('/logistics/transfers', { type: formState.type, remark: formState.remark, items })
    message.success('创建成功')
    modalVisible.value = false; fetchData()
  } catch { } finally { submitting.value = false }
}

onMounted(() => { fetchData(); fetchProducts() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
