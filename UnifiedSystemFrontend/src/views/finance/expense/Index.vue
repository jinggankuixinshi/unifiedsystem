<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">费用报销</h2>
      <a-button type="primary" @click="openCreate">
        <template #icon><PlusOutlined /></template>
        提交报销
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
            <template v-if="column.key === 'totalAmount'">¥{{ Number(record.totalAmount).toLocaleString() }}</template>
            <template v-if="column.key === 'expenseType'">
              <a-tag>{{ { travel: '差旅', office: '办公', entertainment: '招待', other: '其他' }[record.expenseType] || record.expenseType }}</a-tag>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="{0:'orange',1:'green',2:'red'}[record.approvalStatus]">
                {{ {0:'待审批',1:'已通过',2:'已驳回'}[record.approvalStatus] }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <a-modal v-model:open="modalVisible" title="提交报销" @ok="handleSubmit"
      :confirm-loading="submitting" :maskClosable="false" width="600px">
      <a-form :label-col="{ span: 5 }">
        <a-form-item label="报销类型">
          <a-select v-model:value="expenseType" :options="expenseTypeOptions" style="width:100%" />
        </a-form-item>
        <a-divider>费用明细</a-divider>
        <div v-for="(item, i) in expenseItems" :key="i" style="display:flex; gap:8px; margin-bottom:8px">
          <a-input v-model:value="item.itemName" placeholder="项目" style="flex:2" />
          <a-input-number v-model:value="item.amount" placeholder="金额" :min="0" :precision="2" style="flex:1" />
          <a-button size="small" danger @click="expenseItems.splice(i, 1)">删除</a-button>
        </div>
        <a-button type="dashed" block @click="addExpenseItem"><PlusOutlined /> 添加明细</a-button>
        <a-form-item label="备注" style="margin-top:12px">
          <a-textarea v-model:value="remark" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import request from '@/api/request'

const activeTab = ref('all')
const loading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dataSource = ref<any[]>([])

const columns = [
  { title: '报销编号', dataIndex: 'expenseNo' },
  { title: '类型', key: 'expenseType', width: 80 },
  { title: '金额', key: 'totalAmount' },
  { title: '状态', key: 'status', width: 80 },
  { title: '申请时间', dataIndex: 'createTime', width: 180 }
]

const expenseTypeOptions = [
  { label: '差旅', value: 'travel' },
  { label: '办公', value: 'office' },
  { label: '招待', value: 'entertainment' },
  { label: '其他', value: 'other' }
]

const modalVisible = ref(false)
const submitting = ref(false)
const expenseType = ref('travel')
const remark = ref('')
const expenseItems = ref<Array<{ itemName: string; amount: number }>>([])

function addExpenseItem() { expenseItems.value.push({ itemName: '', amount: 0 }) }

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/finance/expenses', { pageNum: current.value, pageSize: pageSize.value }) as any
    dataSource.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

function handleTableChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }
function openCreate() { expenseItems.value = []; modalVisible.value = true }

async function handleSubmit() {
  if (submitting.value) return
  if (expenseItems.value.length === 0 || expenseItems.value.some(i => !i.itemName || !i.amount)) {
    message.warning('请完整填写费用明细'); return
  }
  submitting.value = true
  try {
    await request.post('/finance/expenses', {
      expenseType: expenseType.value, remark: remark.value
    })
    message.success('提交成功')
    modalVisible.value = false; fetchData()
  } catch { } finally { submitting.value = false }
}

onMounted(fetchData)
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
