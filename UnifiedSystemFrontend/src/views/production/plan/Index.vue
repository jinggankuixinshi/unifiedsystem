<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">生产计划与排期</h2>
    </div>
    <a-card class="content-card">
      <a-tabs v-model:activeKey="activeTab" @change="onTabChange">
        <a-tab-pane key="plan" tab="生产计划" />
        <a-tab-pane key="schedule" tab="排期表" />
      </a-tabs>

      <a-spin :spinning="loading">
        <!-- 生产计划 -->
        <template v-if="activeTab === 'plan'">
          <div style="margin-bottom: 16px">
            <a-button type="primary" @click="openPlanCreate">
              <template #icon><PlusOutlined /></template>
              新建计划
            </a-button>
          </div>
          <a-table
            :columns="planColumns"
            :data-source="planData"
            row-key="id"
            :pagination="planPagination"
            @change="handlePlanPageChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="statusColor[record.status]">
                  {{ statusMap[record.status] || record.status }}
                </a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a @click="selectPlan(record)">排期</a>
              </template>
            </template>
          </a-table>
        </template>

        <!-- 排期表 -->
        <template v-if="activeTab === 'schedule'">
          <div style="margin-bottom: 16px">
            <a-select
              v-model:value="selectedPlanId"
              :options="planOptions"
              placeholder="请选择生产计划"
              style="width: 360px"
              @change="fetchSchedule"
            />
          </div>
          <a-empty v-if="!selectedPlanId" description="请选择生产计划查看排期" />
          <a-table
            v-else
            :columns="scheduleColumns"
            :data-source="scheduleData"
            row-key="id"
            :pagination="false"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="statusColor[record.status]">
                  {{ statusMap[record.status] || record.status }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </template>
      </a-spin>
    </a-card>

    <!-- 新建计划弹窗 -->
    <a-modal v-model:open="planModalVisible" title="新建生产计划" @ok="submitPlan" :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 6 }">
        <a-form-item label="计划编号">
          <a-input v-model:value="planForm.planNo" />
        </a-form-item>
        <a-form-item label="产品">
          <a-select v-model:value="planForm.productId" :options="productOptions" placeholder="选择产品" style="width:100%" />
        </a-form-item>
        <a-form-item label="计划数量">
          <a-input-number v-model:value="planForm.planQuantity" :min="1" style="width:100%" />
        </a-form-item>
        <a-form-item label="计划日期">
          <a-date-picker v-model:value="planForm.planDate" style="width:100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="planForm.remark" :rows="2" />
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

const activeTab = ref('plan')
const loading = ref(false)
const submitting = ref(false)
const selectedPlanId = ref<number>()

const planColumns = [
  { title: '计划编号', dataIndex: 'planNo', key: 'planNo' },
  { title: '产品名称', dataIndex: 'productName', key: 'productName' },
  { title: '计划数量', dataIndex: 'planQuantity', key: 'planQuantity', width: 100 },
  { title: '计划日期', dataIndex: 'planDate', key: 'planDate', width: 120 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 60 }
]

const scheduleColumns = [
  { title: '生产线', dataIndex: 'productionLine', key: 'productionLine' },
  { title: '班组', dataIndex: 'team', key: 'team' },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 180 },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 180 },
  { title: '状态', key: 'status', width: 80 }
]

const planData = ref<any[]>([])
const scheduleData = ref<any[]>([])
const productOptions = ref<Array<{ label: string; value: number }>>([])

const planPage = ref({ current: 1, pageSize: 10, total: 0 })

const planPagination = computed(() => ({
  current: planPage.value.current, pageSize: planPage.value.pageSize, total: planPage.value.total,
  showSizeChanger: true, pageSizeOptions: ['10', '20', '50', '100', '200'],
  showTotal: (t: number) => `共 ${t} 条`
}))

const planOptions = computed(() => planData.value.map((p: any) => ({ label: `[${p.planNo}] ${p.productName}`, value: p.id })))

const statusMap: Record<number, string> = { 0: '待排期', 1: '进行中', 2: '已完成', 3: '已取消' }
const statusColor: Record<number, string> = { 0: 'default', 1: 'orange', 2: 'green', 3: 'red' }

const planModalVisible = ref(false)
const planForm = reactive({ planNo: '', productId: undefined as number | undefined, planQuantity: 0, planDate: null, remark: '' })

async function fetchPlans() {
  loading.value = true
  try {
    const res = await request.get('/production/plans', {
      params: { pageNum: planPage.value.current, pageSize: planPage.value.pageSize }
    }) as any
    planData.value = res.data?.records || []
    planPage.value.total = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

async function fetchSchedule() {
  if (!selectedPlanId.value) return
  loading.value = true
  try {
    const res = await request.get(`/production/plans/${selectedPlanId.value}/schedules`) as any
    scheduleData.value = res.data || []
  } catch { } finally { loading.value = false }
}

async function fetchProducts() {
  try {
    const res = await request.get('/production/products', { params: { pageNum: 1, pageSize: 200 } }) as any
    const list = res.data?.records || []
    productOptions.value = list.map((p: any) => ({ label: p.productName, value: p.id }))
  } catch { }
}

function onTabChange() {
  if (activeTab.value === 'plan') fetchPlans()
  else if (activeTab.value === 'schedule') { scheduleData.value = []; selectedPlanId.value = undefined; fetchPlans() }
}

function handlePlanPageChange(pag: any) { planPage.value.current = pag.current; planPage.value.pageSize = pag.pageSize; fetchPlans() }

function selectPlan(record: any) {
  selectedPlanId.value = record.id
  activeTab.value = 'schedule'
  fetchSchedule()
}

function openPlanCreate() {
  planForm.planNo = ''; planForm.productId = undefined; planForm.planQuantity = 0; planForm.planDate = null; planForm.remark = ''
  planModalVisible.value = true
}

async function submitPlan() {
  if (!planForm.productId || !planForm.planQuantity) { message.warning('请填写完整信息'); return }
  submitting.value = true
  try {
    await request.post('/production/plans', {
      ...planForm,
      planDate: planForm.planDate ? dayjs(planForm.planDate).format('YYYY-MM-DD') : null
    })
    message.success('创建成功')
    planModalVisible.value = false
    fetchPlans()
  } catch { } finally { submitting.value = false }
}

onMounted(() => { fetchPlans(); fetchProducts() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
