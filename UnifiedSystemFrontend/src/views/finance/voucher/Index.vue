<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">记账凭证</h2>
      <a-button type="primary" @click="openCreate">
        <template #icon><PlusOutlined /></template>
        新建凭证
      </a-button>
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
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === '已审核' ? 'green' : 'orange'">
                {{ record.status || '待审核' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a @click="viewEntries(record)">查看分录</a>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <!-- 新建凭证弹窗 -->
    <a-modal v-model:open="modalVisible" title="新建记账凭证" width="800px"
      @ok="handleSubmit" :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 4 }">
        <a-form-item label="凭证日期">
          <a-date-picker v-model:value="voucherDate" style="width:200px" />
        </a-form-item>
        <a-form-item label="摘要">
          <a-input v-model:value="summary" />
        </a-form-item>
      </a-form>
      <a-divider>凭证分录</a-divider>
      <a-table :columns="entryColumns" :data-source="entries" size="small" row-key="tempId" :pagination="false">
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'subjectId'">
            <a-select v-model:value="record.subjectId" :options="subjectOptions" placeholder="选择科目" style="width:100%" />
          </template>
          <template v-if="column.key === 'debitAmount'">
            <a-input-number v-model:value="record.debitAmount" :min="0" :precision="2" style="width:100%" @change="calcBalance" />
          </template>
          <template v-if="column.key === 'creditAmount'">
            <a-input-number v-model:value="record.creditAmount" :min="0" :precision="2" style="width:100%" @change="calcBalance" />
          </template>
          <template v-if="column.key === 'summary'">
            <a-input v-model:value="record.summary" />
          </template>
          <template v-if="column.key === 'action'">
            <a @click="entries.splice(index, 1)" class="danger-link">删除</a>
          </template>
        </template>
      </a-table>
      <a-button type="dashed" block @click="entries.push({ tempId: Date.now(), subjectId: null, debitAmount: 0, creditAmount: 0, summary: '' })" style="margin-top:8px">
        <template #icon><PlusOutlined /></template> 添加分录
      </a-button>
      <a-divider />
      <div style="display:flex; justify-content: space-around">
        <span>借方合计: <strong style="color:#1677ff">¥{{ debitTotal.toLocaleString() }}</strong></span>
        <span>贷方合计: <strong style="color:#cf1322">¥{{ creditTotal.toLocaleString() }}</strong></span>
        <span :style="{ color: balanceOk ? '#389e0d' : '#cf1322' }">
          {{ balanceOk ? '借贷平衡' : '借贷不平衡' }}
        </span>
      </div>
    </a-modal>

    <!-- 查看分录弹窗 -->
    <a-modal v-model:open="entryModalVisible" title="查看分录" width="700px" :footer="null">
      <a-table :columns="viewEntryColumns" :data-source="entryModalData" size="small" row-key="id" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'debitAmount'">
            ¥{{ Number(record.debitAmount || 0).toFixed(2) }}
          </template>
          <template v-if="column.key === 'creditAmount'">
            ¥{{ Number(record.creditAmount || 0).toFixed(2) }}
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import request from '@/api/request'
import dayjs from 'dayjs'

const loading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dataSource = ref<any[]>([])

const columns = [
  { title: '凭证编号', dataIndex: 'voucherNo', key: 'voucherNo' },
  { title: '凭证日期', dataIndex: 'voucherDate', key: 'voucherDate', width: 130 },
  { title: '摘要', dataIndex: 'summary', key: 'summary' },
  { title: '制单人', dataIndex: 'creatorName', key: 'creatorName', width: 100 },
  { title: '状态', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 80 }
]

const entryColumns = [
  { title: '科目', key: 'subjectId', width: 200 },
  { title: '借方金额', key: 'debitAmount', width: 140 },
  { title: '贷方金额', key: 'creditAmount', width: 140 },
  { title: '摘要', key: 'summary' },
  { title: '操作', key: 'action', width: 60 }
]

const viewEntryColumns = [
  { title: '科目', dataIndex: 'subjectName', key: 'subjectName' },
  { title: '借方金额', key: 'debitAmount', width: 140 },
  { title: '贷方金额', key: 'creditAmount', width: 140 },
  { title: '摘要', dataIndex: 'summary', key: 'summary' }
]

const pagination = computed(() => ({
  current: current.value, pageSize: pageSize.value, total: total.value,
  showSizeChanger: true, pageSizeOptions: ['10', '20', '50', '100', '200'],
  showTotal: (t: number) => `共 ${t} 条`
}))

const modalVisible = ref(false)
const submitting = ref(false)
const voucherDate = ref()
const summary = ref('')
const entries = ref<Array<{ tempId: number; subjectId: number | null; debitAmount: number; creditAmount: number; summary: string }>>([])
const subjectOptions = ref<Array<{ label: string; value: number }>>([])

const entryModalVisible = ref(false)
const entryModalData = ref<any[]>([])

const debitTotal = computed(() => entries.value.reduce((sum, e) => sum + (e.debitAmount || 0), 0))
const creditTotal = computed(() => entries.value.reduce((sum, e) => sum + (e.creditAmount || 0), 0))
const balanceOk = computed(() => Math.abs(debitTotal.value - creditTotal.value) < 0.01 && entries.value.length > 0)

function calcBalance() { }

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/finance/vouchers', {
      pageNum: current.value, pageSize: pageSize.value
    }) as any
    dataSource.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { message.error('加载凭证失败') } finally { loading.value = false }
}

async function fetchSubjects() {
  try {
    const res = await request.get('/finance/subjects') as any
    const list = res.data || []
    const flatList = (items: any[], level = 0): any[] => {
      return items.reduce((acc: any[], item: any) => {
        acc.push({ label: '  '.repeat(level) + item.subjectName, value: item.id })
        if (item.children) acc.push(...flatList(item.children, level + 1))
        return acc
      }, [])
    }
    subjectOptions.value = flatList(list)
  } catch { }
}

function handlePageChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }

function openCreate() {
  voucherDate.value = null
  summary.value = ''
  entries.value = []
  modalVisible.value = true
}

async function handleSubmit() {
  if (!summary.value) { message.warning('请输入摘要'); return }
  if (entries.value.length === 0) { message.warning('请添加分录'); return }
  if (!balanceOk.value) { message.warning('借贷不平衡，请检查金额'); return }
  submitting.value = true
  try {
    await request.post('/finance/vouchers', {
      voucherDate: voucherDate.value ? dayjs(voucherDate.value).format('YYYY-MM-DD') : null,
      summary: summary.value,
      entries: entries.value.map(e => ({
        subjectId: e.subjectId,
        debitAmount: e.debitAmount || 0,
        creditAmount: e.creditAmount || 0,
        summary: e.summary
      }))
    })
    message.success('创建成功')
    modalVisible.value = false
    fetchData()
  } catch { message.error('操作失败') } finally { submitting.value = false }
}

function viewEntries(record: any) {
  entryModalData.value = record.entries || []
  entryModalVisible.value = true
}

onMounted(() => { fetchData(); fetchSubjects() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
