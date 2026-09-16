<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">采购申请</h2>
      <a-button type="primary" @click="openCreate">
        <template #icon><PlusOutlined /></template>
        新建采购申请
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
          :pagination="{ current, pageSize, total, showSizeChanger: true }" @change="handleTableChange">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'totalAmount'">
              {{ Number(record.totalAmount).toLocaleString() }}
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="statusColor[record.approvalStatus]">{{ statusMap[record.approvalStatus] }}</a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a @click="viewDetail(record)">查看</a>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <a-modal v-model:open="modalVisible" title="新建采购申请" width="700px" @ok="handleSubmit"
      :confirm-loading="submitting" :maskClosable="false">
      <a-form ref="formRef" :model="formState" :label-col="{ span: 5 }">
        <a-form-item label="申请事由" name="reason">
          <a-textarea v-model:value="formState.reason" :rows="2" />
        </a-form-item>
        <a-divider>采购明细</a-divider>
        <a-table :columns="itemColumns" :data-source="formState.items" size="small" row-key="tempId"
          :pagination="false">
          <template #bodyCell="{ column, record, index }">
            <template v-if="column.key === 'material'">
              <a-select v-model:value="record.materialId" :options="materialOptions" style="width:100%"
                placeholder="选择物料" />
            </template>
            <template v-if="column.key === 'quantity'">
              <a-input-number v-model:value="record.quantity" :min="0" style="width:100%" />
            </template>
            <template v-if="column.key === 'unitPrice'">
              <a-input-number v-model:value="record.unitPrice" :min="0" :precision="2" style="width:100%" />
            </template>
            <template v-if="column.key === 'amount'">
              {{ (record.quantity * record.unitPrice || 0).toFixed(2) }}
            </template>
            <template v-if="column.key === 'action'">
              <a @click="removeItem(index)" class="danger-link">删除</a>
            </template>
          </template>
        </a-table>
        <a-button type="dashed" block @click="addItem" style="margin-top:8px">
          <template #icon><PlusOutlined /></template> 添加物料
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

const statusMap: Record<number, string> = { 0: '待审批', 1: '已通过', 2: '已驳回' }
const statusColor: Record<number, string> = { 0: 'orange', 1: 'green', 2: 'red' }

const columns = [
  { title: '申请编号', dataIndex: 'requestNo' },
  { title: '申请金额', key: 'totalAmount' },
  { title: '申请人', dataIndex: 'applicantId' },
  { title: '状态', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 80 }
]

const itemColumns = [
  { title: '物料', key: 'material', width: 200 },
  { title: '数量', key: 'quantity', width: 120 },
  { title: '单价', key: 'unitPrice', width: 140 },
  { title: '金额', key: 'amount', width: 120 },
  { title: '操作', key: 'action', width: 60 }
]

const materialOptions = ref<Array<{ label: string; value: number }>>([])
const modalVisible = ref(false)
const formRef = ref()
const submitting = ref(false)

const formState = reactive<{ reason: string; items: any[] }>({
  reason: '',
  items: []
})

let tempId = 0

function addItem() {
  formState.items.push({ tempId: ++tempId, materialId: null, quantity: null, unitPrice: null, amount: 0 })
}

function removeItem(index: number) {
  formState.items.splice(index, 1)
}

async function fetchData() {
  loading.value = true
  try {
    const status = activeTab.value === 'all' ? undefined : Number(activeTab.value)
    const res = await request.get('/production/purchase', { pageNum: current.value, pageSize: pageSize.value, status }) as any
    dataSource.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

async function fetchMaterials() {
  try {
    const res = await request.get('/production/materials') as any
    const list = res.data?.records || []
    materialOptions.value = list.map((m: any) => ({ label: m.productName || m.materialName, value: m.id }))
  } catch { }
}

function handleTableChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }

function openCreate() {
  formState.reason = ''; formState.items = []; modalVisible.value = true
}

function viewDetail(record: any) {
  message.info('详情功能开发中')
}

async function handleSubmit() {
  if (submitting.value) return
  if (formState.items.length === 0) { message.warning('请至少添加一条物料'); return }
  submitting.value = true
  try {
    const items = formState.items.map(i => ({
      materialId: i.materialId, quantity: i.quantity, unitPrice: i.unitPrice,
      amount: (i.quantity || 0) * (i.unitPrice || 0)
    }))
    await request.post('/production/purchase', {
      request: { reason: formState.reason, approvalStatus: 0 },
      items
    })
    message.success('提交成功')
    modalVisible.value = false; fetchData()
  } catch { message.error('提交失败') } finally { submitting.value = false }
}

onMounted(() => { fetchData(); fetchMaterials() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
