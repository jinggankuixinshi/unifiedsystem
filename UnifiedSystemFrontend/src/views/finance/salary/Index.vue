<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">工资管理</h2>
      <a-button type="primary" @click="openCreate">
        <template #icon><PlusOutlined /></template>
        新建工资单
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
            <template v-if="column.key === 'netSalary'">
              <span style="font-weight:700; color:#1677ff">
                ¥{{ Number(record.netSalary || 0).toFixed(2) }}
              </span>
            </template>
            <template v-if="column.key === 'amount'">
              ¥{{ Number(record[column.dataIndex] || 0).toFixed(2) }}
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === '已发放' ? 'green' : record.status === '待发放' ? 'orange' : 'default'">
                {{ record.status || '-' }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <!-- 新建工资单弹窗 -->
    <a-modal v-model:open="modalVisible" title="新建工资单" @ok="handleSubmit" :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 7 }">
        <a-form-item label="员工">
          <a-select v-model:value="salaryForm.userId" :options="userOptions" placeholder="选择员工" style="width:100%" />
        </a-form-item>
        <a-form-item label="工资月份">
          <a-month-picker v-model:value="salaryForm.salaryMonth" style="width:100%" />
        </a-form-item>
        <a-form-item label="基本工资">
          <a-input-number v-model:value="salaryForm.baseSalary" :min="0" :precision="2" style="width:100%" @change="calcNet" />
        </a-form-item>
        <a-form-item label="绩效奖金">
          <a-input-number v-model:value="salaryForm.performanceBonus" :min="0" :precision="2" style="width:100%" @change="calcNet" />
        </a-form-item>
        <a-form-item label="加班费">
          <a-input-number v-model:value="salaryForm.overtimePay" :min="0" :precision="2" style="width:100%" @change="calcNet" />
        </a-form-item>
        <a-form-item label="扣款">
          <a-input-number v-model:value="salaryForm.deduction" :min="0" :precision="2" style="width:100%" @change="calcNet" />
        </a-form-item>
        <a-form-item label="社保">
          <a-input-number v-model:value="salaryForm.socialInsurance" :min="0" :precision="2" style="width:100%" @change="calcNet" />
        </a-form-item>
        <a-form-item label="公积金">
          <a-input-number v-model:value="salaryForm.housingFund" :min="0" :precision="2" style="width:100%" @change="calcNet" />
        </a-form-item>
        <a-form-item label="实发工资">
          <a-input :value="'¥ ' + netSalary.toFixed(2)" disabled style="font-weight:700; color:#1677ff" />
        </a-form-item>
      </a-form>
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
  { title: '员工', dataIndex: 'userName', key: 'userName' },
  { title: '工资月份', dataIndex: 'salaryMonth', key: 'salaryMonth', width: 100 },
  { title: '基本工资', dataIndex: 'baseSalary', key: 'amount' },
  { title: '绩效奖金', dataIndex: 'performanceBonus', key: 'amount' },
  { title: '加班费', dataIndex: 'overtimePay', key: 'amount' },
  { title: '扣款', dataIndex: 'deduction', key: 'amount' },
  { title: '社保', dataIndex: 'socialInsurance', key: 'amount' },
  { title: '公积金', dataIndex: 'housingFund', key: 'amount' },
  { title: '实发工资', key: 'netSalary', width: 130 },
  { title: '状态', key: 'status', width: 80 }
]

const pagination = computed(() => ({
  current: current.value, pageSize: pageSize.value, total: total.value,
  showSizeChanger: true, pageSizeOptions: ['10', '20', '50', '100', '200'],
  showTotal: (t: number) => `共 ${t} 条`
}))

const modalVisible = ref(false)
const submitting = ref(false)
const salaryForm = reactive({
  userId: undefined as number | undefined,
  salaryMonth: null,
  baseSalary: 0, performanceBonus: 0, overtimePay: 0,
  deduction: 0, socialInsurance: 0, housingFund: 0
})

const userOptions = ref<Array<{ label: string; value: number }>>([])

const netSalary = computed(() => {
  const income = (salaryForm.baseSalary || 0) + (salaryForm.performanceBonus || 0) + (salaryForm.overtimePay || 0)
  const deduct = (salaryForm.deduction || 0) + (salaryForm.socialInsurance || 0) + (salaryForm.housingFund || 0)
  return income - deduct
})

function calcNet() { }

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/finance/salaries', {
      params: { pageNum: current.value, pageSize: pageSize.value }
    }) as any
    dataSource.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

async function fetchUsers() {
  try {
    const res = await request.get('/system/users', { params: { pageNum: 1, pageSize: 200 } }) as any
    const list = res.data?.records || []
    userOptions.value = list.map((u: any) => ({ label: u.realName || u.username, value: u.id }))
  } catch { }
}

function handlePageChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }

function openCreate() {
  salaryForm.userId = undefined; salaryForm.salaryMonth = null
  salaryForm.baseSalary = 0; salaryForm.performanceBonus = 0; salaryForm.overtimePay = 0
  salaryForm.deduction = 0; salaryForm.socialInsurance = 0; salaryForm.housingFund = 0
  modalVisible.value = true
}

async function handleSubmit() {
  if (!salaryForm.userId) { message.warning('请选择员工'); return }
  if (!salaryForm.salaryMonth) { message.warning('请选择工资月份'); return }
  submitting.value = true
  try {
    await request.post('/finance/salaries', {
      userId: salaryForm.userId,
      salaryMonth: salaryForm.salaryMonth ? dayjs(salaryForm.salaryMonth).format('YYYY-MM') : null,
      baseSalary: salaryForm.baseSalary,
      performanceBonus: salaryForm.performanceBonus,
      overtimePay: salaryForm.overtimePay,
      deduction: salaryForm.deduction,
      socialInsurance: salaryForm.socialInsurance,
      housingFund: salaryForm.housingFund,
      netSalary: netSalary.value
    })
    message.success('创建成功')
    modalVisible.value = false
    fetchData()
  } catch { } finally { submitting.value = false }
}

onMounted(() => { fetchData(); fetchUsers() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
