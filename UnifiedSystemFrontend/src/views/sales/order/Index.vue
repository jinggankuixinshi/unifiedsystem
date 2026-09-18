<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">销售报单</h2>
      <a-button type="primary" @click="openCreate">
        <template #icon><PlusOutlined /></template>
        新建报单
      </a-button>
    </div>
    <a-card class="content-card">
      <a-tabs v-model:activeKey="activeTab" @change="fetchData">
        <a-tab-pane key="all" tab="全部" />
        <a-tab-pane key="0" tab="待审批" />
        <a-tab-pane key="1" tab="已通过" />
      </a-tabs>
      <a-spin :spinning="loading">
        <a-table :columns="columns" :data-source="dataSource" row-key="id"
          :pagination="{ current, pageSize, total }" @change="handleTableChange">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'totalAmount'">
              ¥{{ Number(record.totalAmount).toLocaleString() }}
            </template>
            <template v-if="column.key === 'anomalyLevel'">
              <a-tag v-if="record.priceAnomalyLevel === 1" color="orange">轻度</a-tag>
              <a-tag v-else-if="record.priceAnomalyLevel === 2" color="volcano">中度</a-tag>
              <a-tag v-else-if="record.priceAnomalyLevel === 3" color="red">重度</a-tag>
              <span v-else>-</span>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="({0:'orange',1:'green',2:'red'} as Record<number, string>)[record.approvalStatus]">
                {{ ({0:'待审批',1:'已通过',2:'已驳回'} as Record<number, string>)[record.approvalStatus] }}
              </a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a @click="viewDetail(record)">查看</a>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <a-modal v-model:open="modalVisible" title="新建销售报单" width="800px"
      @ok="handleSubmit" :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="客户">
          <a-select v-model:value="customerId" :options="customerOptions" placeholder="选择客户" style="width:100%" />
        </a-form-item>
        <a-form-item label="付款方式">
          <a-select v-model:value="paymentMethod" :options="paymentOptions" style="width:100%" />
        </a-form-item>
        <a-form-item label="交货日期">
          <a-date-picker v-model:value="deliveryDate" style="width:100%" />
        </a-form-item>
        <a-form-item label="质保条款">
          <a-input v-model:value="warranty" />
        </a-form-item>
        <a-form-item label="低价特批理由">
          <a-textarea v-model:value="lowPriceReason" :rows="2"
            placeholder="中度及以上价格异常（低于均价70%）时必填，其余情况可留空" />
        </a-form-item>
      </a-form>
      <a-divider>报单明细</a-divider>
      <a-table :columns="itemCols" :data-source="items" size="small" row-key="tempId" :pagination="false">
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'product'">
            <a-select v-model:value="record.productId" :options="productOptions" style="width:100%" />
          </template>
          <template v-if="column.key === 'quantity'">
            <a-input-number v-model:value="record.quantity" :min="0" style="width:100%" />
          </template>
          <template v-if="column.key === 'unitPrice'">
            <a-input-number v-model:value="record.unitPrice" :min="0" :precision="2" style="width:100%" />
          </template>
          <template v-if="column.key === 'amount'">
            ¥{{ ((record.quantity || 0) * (record.unitPrice || 0)).toLocaleString() }}
          </template>
          <template v-if="column.key === 'action'">
            <a @click="items.splice(index, 1)" class="danger-link">删除</a>
          </template>
        </template>
      </a-table>
      <a-button type="dashed" block @click="items.push({ tempId: Date.now(), productId: null, specification: '', quantity: null, unitPrice: null })" style="margin-top:8px">
        <template #icon><PlusOutlined /></template> 添加产品
      </a-button>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import request from '@/api/request'
import dayjs from 'dayjs'

const activeTab = ref('all')
const loading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dataSource = ref<any[]>([])

const columns = [
  { title: '报单编号', dataIndex: 'orderNo' },
  { title: '客户ID', dataIndex: 'customerId' },
  { title: '总金额', key: 'totalAmount' },
  { title: '价格异常', key: 'anomalyLevel', width: 80 },
  { title: '状态', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 80 }
]
const itemCols = [
  { title: '产品', key: 'product', width: 200 },
  { title: '规格', dataIndex: 'specification' },
  { title: '数量', key: 'quantity', width: 100 },
  { title: '单价', key: 'unitPrice', width: 120 },
  { title: '金额', key: 'amount', width: 120 },
  { title: '操作', key: 'action', width: 60 }
]

const modalVisible = ref(false)
const submitting = ref(false)
const customerId = ref<number>()
const paymentMethod = ref('bank')
const deliveryDate = ref()
const warranty = ref('')
const lowPriceReason = ref('')
const items = ref<any[]>([])
const customerOptions = ref<Array<{ label: string; value: number }>>([])
const productOptions = ref<Array<{ label: string; value: number }>>([])
const paymentOptions = [
  { label: '银行转账', value: 'bank' },
  { label: '现金', value: 'cash' },
  { label: '月结30天', value: 'credit30' }
]

async function fetchData() {
  loading.value = true
  try {
    const status = activeTab.value === 'all' ? undefined : Number(activeTab.value)
    const res = await request.get('/sales/orders', { params: { pageNum: current.value, pageSize: pageSize.value, status } }) as any
    dataSource.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

async function fetchOptions() {
  try {
    const cRes = await request.get('/sales/customers') as any
    const list = cRes.data?.records || []
    customerOptions.value = list.map((c: any) => ({ label: c.customerName, value: c.id }))
    const pRes = await request.get('/production/products') as any
    const plist = pRes.data?.records || []
    productOptions.value = plist.map((p: any) => ({ label: p.productName, value: p.id }))
  } catch { }
}

function handleTableChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }
function openCreate() { items.value = []; lowPriceReason.value = ''; modalVisible.value = true }
function viewDetail(record: any) { message.info('详情功能开发中') }

async function handleSubmit() {
  if (submitting.value) return
  if (!customerId.value) { message.warning('请选择客户'); return }
  if (items.value.length === 0) { message.warning('请添加产品'); return }
  submitting.value = true
  try {
    const order = {
      customerId: customerId.value,
      paymentMethod: paymentMethod.value,
      deliveryDate: deliveryDate.value ? dayjs(deliveryDate.value).format('YYYY-MM-DD') : null,
      warrantyTerms: warranty.value,
      lowPriceReason: lowPriceReason.value || ''
    }
    const orderItems = items.value.map(i => ({
      productId: i.productId, specification: i.specification,
      quantity: i.quantity, unitPrice: i.unitPrice
    }))
    await request.post('/sales/orders', { order, items: orderItems })
    message.success('提交成功')
    modalVisible.value = false; fetchData()
  } catch { } finally { submitting.value = false }
}

onMounted(() => { fetchData(); fetchOptions() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
