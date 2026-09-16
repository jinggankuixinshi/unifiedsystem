<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">应收账款</h2>
      <a-space>
        <a-tag v-if="summary" color="blue">应收余额: ¥{{ summary.toLocaleString() }}</a-tag>
        <a-button type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新增应收
        </a-button>
      </a-space>
    </div>
    <a-card class="content-card">
      <a-spin :spinning="loading">
        <a-empty v-if="!loading && dataSource.length === 0" description="暂无应收账款" />
        <a-table v-else :columns="columns" :data-source="dataSource" row-key="id"
          :pagination="pagination" @change="handlePageChange">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'amount'">¥{{ Number(record.amount).toLocaleString() }}</template>
            <template v-if="column.key === 'balance'">
              <span :style="{ color: record.balance > 0 ? '#b44a4a' : '#5b8c5a' }">
                ¥{{ Number(record.balance).toLocaleString() }}
              </span>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === 1 ? 'green' : 'orange'">
                {{ record.status === 1 ? '已结清' : '未结清' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a v-if="record.status !== 1" @click="openPayment(record)">收款</a>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <a-modal v-model:open="createModalVisible" title="新增应收账款" @ok="handleCreate" :confirm-loading="submitting">
      <a-form :label-col="{ span: 6 }">
        <a-form-item label="客户ID"><a-input v-model:value="createForm.customerId" /></a-form-item>
        <a-form-item label="销售单ID"><a-input v-model:value="createForm.orderId" /></a-form-item>
        <a-form-item label="金额"><a-input-number v-model:value="createForm.amount" :min="0" :precision="2" style="width:100%" /></a-form-item>
        <a-form-item label="到期日"><a-date-picker v-model:value="createForm.dueDate" style="width:100%" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="paymentModalVisible" title="登记收款" @ok="handlePayment" :confirm-loading="paying">
      <a-form :label-col="{ span: 6 }">
        <a-form-item label="收款金额"><a-input-number v-model:value="paymentAmount" :min="0.01" :precision="2" style="width:100%" /></a-form-item>
        <a-form-item label="收款方式">
          <a-select v-model:value="paymentMethod" style="width:100%">
            <a-select-option value="bank">银行转账</a-select-option>
            <a-select-option value="cash">现金</a-select-option>
            <a-select-option value="check">支票</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { getReceivables, createReceivable, logReceivablePayment } from '@/api/modules/finance'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'

const loading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dataSource = ref<any[]>([])
const summary = ref(0)

const columns = [
  { title: '客户ID', dataIndex: 'customerId', width: 100 },
  { title: '销售单ID', dataIndex: 'salesOrderId', width: 100 },
  { title: '金额', key: 'amount', width: 120 },
  { title: '已收', dataIndex: 'receivedAmount', width: 120 },
  { title: '余额', key: 'balance', width: 120 },
  { title: '账期', dataIndex: 'dueDate', width: 120 },
  { title: '状态', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 80 }
]

const pagination = computed(() => ({
  current: current.value, pageSize: pageSize.value, total: total.value,
  showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条`
}))

const createModalVisible = ref(false)
const submitting = ref(false)
const createForm = reactive({ customerId: '', orderId: '', amount: 0, dueDate: null as Dayjs | null })

const paymentModalVisible = ref(false)
const paying = ref(false)
const payingId = ref<number>()
const paymentAmount = ref(0)
const paymentMethod = ref('bank')

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getReceivables({ pageNum: current.value, pageSize: pageSize.value })
    const records = res.data?.records || []
    dataSource.value = records
    total.value = res.data?.total || 0
    summary.value = records.filter((r: any) => r.status !== 1).reduce((s: number, r: any) => s + Number(r.balance || 0), 0)
  } catch { message.error('加载失败') } finally { loading.value = false }
}

function openCreate() { createForm.customerId = ''; createForm.orderId = ''; createForm.amount = 0; createForm.dueDate = dayjs(); createModalVisible.value = true }

async function handleCreate() {
  if (!createForm.customerId || !createForm.amount) { message.warning('请填写完整信息'); return }
  submitting.value = true
  try {
    await createReceivable({
      customerId: parseInt(createForm.customerId),
      salesOrderId: createForm.orderId ? parseInt(createForm.orderId) : null,
      amount: createForm.amount,
      dueDate: createForm.dueDate?.format('YYYY-MM-DD')
    })
    message.success('新增成功'); createModalVisible.value = false; fetchData()
  } catch { } finally { submitting.value = false }
}

function openPayment(record: any) {
  payingId.value = record.id; paymentAmount.value = record.balance; paymentMethod.value = 'bank'
  paymentModalVisible.value = true
}

async function handlePayment() {
  if (!paymentAmount.value || paymentAmount.value <= 0) { message.warning('请输入收款金额'); return }
  paying.value = true
  try {
    await logReceivablePayment(payingId.value!, paymentAmount.value, paymentMethod.value)
    message.success('收款登记成功'); paymentModalVisible.value = false; fetchData()
  } catch { } finally { paying.value = false }
}

function handlePageChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }

onMounted(fetchData)
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
