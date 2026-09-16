<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">预算管理</h2>
      <a-space>
        <a-select v-model:value="filterYear" style="width: 120px" placeholder="年度" @change="fetchData">
          <a-select-option v-for="y in years" :key="y" :value="y">{{ y }}年</a-select-option>
        </a-select>
        <a-button type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新增预算
        </a-button>
      </a-space>
    </div>
    <a-card class="content-card">
      <a-spin :spinning="loading">
        <a-empty v-if="!loading && dataSource.length === 0" description="暂无预算数据" />
        <a-table v-else :columns="columns" :data-source="dataSource" row-key="id"
          :pagination="pagination" @change="handlePageChange">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'budgetAmount'">¥{{ Number(record.budgetAmount).toLocaleString() }}</template>
            <template v-if="column.key === 'executedAmount'">¥{{ Number(record.executedAmount).toLocaleString() }}</template>
            <template v-if="column.key === 'usage'">
              <a-progress :percent="record.budgetAmount ? Math.min(100, Math.round(record.executedAmount / record.budgetAmount * 100)) : 0"
                :status="record.executedAmount > record.budgetAmount ? 'exception' : 'normal'" size="small" />
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <a-modal v-model:open="modalVisible" :title="isEdit ? '编辑预算' : '新增预算'" @ok="handleSubmit" :confirm-loading="submitting">
      <a-form :label-col="{ span: 6 }">
        <a-form-item label="部门ID"><a-input v-model:value="formState.deptId" /></a-form-item>
        <a-form-item label="预算科目"><a-input v-model:value="formState.subjectId" placeholder="科目ID" /></a-form-item>
        <a-form-item label="年度"><a-input-number v-model:value="formState.year" :min="2024" :max="2030" style="width:100%" /></a-form-item>
        <a-form-item label="预算金额"><a-input-number v-model:value="formState.budgetAmount" :min="0" :precision="2" style="width:100%" /></a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { getBudgets, createBudget, updateBudget } from '@/api/modules/finance'

const loading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dataSource = ref<any[]>([])

const currentYear = new Date().getFullYear()
const filterYear = ref(currentYear)
const years = [currentYear - 1, currentYear, currentYear + 1]

const columns = [
  { title: '部门ID', dataIndex: 'deptId', width: 100 },
  { title: '预算科目', dataIndex: 'subjectId', width: 100 },
  { title: '年度', dataIndex: 'budgetYear', width: 80 },
  { title: '预算金额', key: 'budgetAmount', width: 130 },
  { title: '已执行', key: 'executedAmount', width: 130 },
  { title: '执行进度', key: 'usage', width: 180 }
]

const pagination = computed(() => ({
  current: current.value, pageSize: pageSize.value, total: total.value,
  showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条`
}))

const modalVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number>()
const submitting = ref(false)
const formState = reactive({ deptId: '', subjectId: '', year: currentYear, budgetAmount: 0 })

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getBudgets({ pageNum: current.value, pageSize: pageSize.value, year: filterYear.value })
    dataSource.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

function openCreate() {
  isEdit.value = false; formState.deptId = ''; formState.subjectId = ''
  formState.year = filterYear.value; formState.budgetAmount = 0
  modalVisible.value = true
}

function openEdit(record: any) {
  isEdit.value = true; editId.value = record.id
  formState.deptId = String(record.deptId); formState.subjectId = String(record.subjectId)
  formState.year = record.budgetYear; formState.budgetAmount = record.budgetAmount
  modalVisible.value = true
}

async function handleSubmit() {
  if (!formState.deptId || !formState.subjectId || !formState.budgetAmount) { message.warning('请填写完整信息'); return }
  submitting.value = true
  try {
    const payload = {
      deptId: parseInt(formState.deptId),
      subjectId: parseInt(formState.subjectId),
      budgetYear: formState.year,
      budgetAmount: formState.budgetAmount
    }
    if (isEdit.value) {
      await updateBudget(editId.value!, payload)
      message.success('更新成功')
    } else {
      await createBudget(payload)
      message.success('新增成功')
    }
    modalVisible.value = false; fetchData()
  } catch { } finally { submitting.value = false }
}

function handlePageChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }

onMounted(fetchData)
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
