<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">考勤管理</h2>
    </div>
    <a-card class="content-card">
      <a-tabs v-model:activeKey="activeTab" @change="onTabChange">
        <a-tab-pane key="records" tab="打卡记录" />
        <a-tab-pane key="leave" tab="请假申请" />
        <a-tab-pane key="overtime" tab="加班申请" />
      </a-tabs>

      <a-spin :spinning="loading">
        <!-- 打卡记录 -->
        <template v-if="activeTab === 'records'">
          <a-table
            :columns="recordColumns"
            :data-source="recordData"
            row-key="id"
            :pagination="recordPagination"
            @change="handleRecordPageChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === '正常' ? 'green' : record.status === '迟到' || record.status === '早退' ? 'orange' : 'red'">
                  {{ record.status || '-' }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </template>

        <!-- 请假申请 -->
        <template v-if="activeTab === 'leave'">
          <div style="margin-bottom: 16px">
            <a-button type="primary" @click="openLeaveCreate">
              <template #icon><PlusOutlined /></template>
              申请请假
            </a-button>
          </div>
          <a-table
            :columns="leaveColumns"
            :data-source="leaveData"
            row-key="id"
            :pagination="leavePagination"
            @change="handleLeavePageChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'leaveType'">
                <a-tag :color="{ '事假': 'blue', '病假': 'purple', '年假': 'green', '婚假': 'pink' }[record.leaveType] || 'default'">
                  {{ record.leaveType }}
                </a-tag>
              </template>
              <template v-if="column.key === 'approvalStatus'">
                <a-tag :color="record.approvalStatus === '已通过' ? 'green' : record.approvalStatus === '已驳回' ? 'red' : 'orange'">
                  {{ record.approvalStatus || '待审批' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-popconfirm title="确认审批通过？" @confirm="approveLeave(record.id)">
                  <a>审批</a>
                </a-popconfirm>
              </template>
            </template>
          </a-table>
        </template>

        <!-- 加班申请 -->
        <template v-if="activeTab === 'overtime'">
          <div style="margin-bottom: 16px">
            <a-button type="primary" @click="openOvertimeCreate">
              <template #icon><PlusOutlined /></template>
              申请加班
            </a-button>
          </div>
          <a-table
            :columns="overtimeColumns"
            :data-source="overtimeData"
            row-key="id"
            :pagination="overtimePagination"
            @change="handleOvertimePageChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'approvalStatus'">
                <a-tag :color="record.approvalStatus === '已通过' ? 'green' : record.approvalStatus === '已驳回' ? 'red' : 'orange'">
                  {{ record.approvalStatus || '待审批' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-popconfirm title="确认审批通过？" @confirm="approveOvertime(record.id)">
                  <a>审批</a>
                </a-popconfirm>
              </template>
            </template>
          </a-table>
        </template>
      </a-spin>
    </a-card>

    <!-- 请假申请弹窗 -->
    <a-modal v-model:open="leaveModalVisible" title="申请请假" @ok="submitLeave" :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 5 }">
        <a-form-item label="请假类型">
          <a-select v-model:value="leaveForm.leaveType" placeholder="请选择类型" style="width:100%">
            <a-select-option value="事假">事假</a-select-option>
            <a-select-option value="病假">病假</a-select-option>
            <a-select-option value="年假">年假</a-select-option>
            <a-select-option value="婚假">婚假</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="开始时间">
          <a-date-picker v-model:value="leaveForm.startTime" show-time style="width:100%" />
        </a-form-item>
        <a-form-item label="结束时间">
          <a-date-picker v-model:value="leaveForm.endTime" show-time style="width:100%" />
        </a-form-item>
        <a-form-item label="请假原因">
          <a-textarea v-model:value="leaveForm.reason" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 加班申请弹窗 -->
    <a-modal v-model:open="overtimeModalVisible" title="申请加班" @ok="submitOvertime" :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 5 }">
        <a-form-item label="开始时间">
          <a-date-picker v-model:value="overtimeForm.startTime" show-time style="width:100%" />
        </a-form-item>
        <a-form-item label="结束时间">
          <a-date-picker v-model:value="overtimeForm.endTime" show-time style="width:100%" />
        </a-form-item>
        <a-form-item label="加班原因">
          <a-textarea v-model:value="overtimeForm.reason" :rows="3" />
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

const activeTab = ref('records')
const loading = ref(false)
const submitting = ref(false)

const recordColumns = [
  { title: '用户', dataIndex: 'userName', key: 'userName' },
  { title: '部门', dataIndex: 'deptName', key: 'deptName' },
  { title: '签到时间', dataIndex: 'loginTime', key: 'loginTime', width: 180 },
  { title: '签退时间', dataIndex: 'logoutTime', key: 'logoutTime', width: 180 },
  { title: '状态', key: 'status', width: 80 },
  { title: '备注', dataIndex: 'remark', key: 'remark' }
]

const leaveColumns = [
  { title: '用户', dataIndex: 'userName', key: 'userName' },
  { title: '请假类型', key: 'leaveType', width: 80 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 180 },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 180 },
  { title: '时长', dataIndex: 'duration', key: 'duration', width: 80 },
  { title: '审批状态', key: 'approvalStatus', width: 100 },
  { title: '操作', key: 'action', width: 80 }
]

const overtimeColumns = [
  { title: '用户', dataIndex: 'userName', key: 'userName' },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 180 },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 180 },
  { title: '时长', dataIndex: 'duration', key: 'duration', width: 80 },
  { title: '审批状态', key: 'approvalStatus', width: 100 },
  { title: '操作', key: 'action', width: 80 }
]

const recordData = ref<any[]>([])
const leaveData = ref<any[]>([])
const overtimeData = ref<any[]>([])

const recordPage = ref({ current: 1, pageSize: 10, total: 0 })
const leavePage = ref({ current: 1, pageSize: 10, total: 0 })
const overtimePage = ref({ current: 1, pageSize: 10, total: 0 })

const recordPagination = computed(() => ({
  current: recordPage.value.current,
  pageSize: recordPage.value.pageSize,
  total: recordPage.value.total,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '50', '100', '200'],
  showTotal: (t: number) => `共 ${t} 条`
}))

const leavePagination = computed(() => ({
  current: leavePage.value.current,
  pageSize: leavePage.value.pageSize,
  total: leavePage.value.total,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '50', '100', '200'],
  showTotal: (t: number) => `共 ${t} 条`
}))

const overtimePagination = computed(() => ({
  current: overtimePage.value.current,
  pageSize: overtimePage.value.pageSize,
  total: overtimePage.value.total,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '50', '100', '200'],
  showTotal: (t: number) => `共 ${t} 条`
}))

const leaveModalVisible = ref(false)
const overtimeModalVisible = ref(false)
const leaveForm = reactive({ leaveType: undefined as string | undefined, startTime: null, endTime: null, reason: '' })
const overtimeForm = reactive({ startTime: null, endTime: null, reason: '' })

async function fetchRecords() {
  loading.value = true
  try {
    const res = await request.get('/system/attendance/records', {
      pageNum: recordPage.value.current,
      pageSize: recordPage.value.pageSize
    }) as any
    recordData.value = res.data?.records || []
    recordPage.value.total = res.data?.total || 0
  } catch { message.error('加载打卡记录失败') } finally { loading.value = false }
}

async function fetchLeave() {
  loading.value = true
  try {
    const res = await request.get('/system/attendance/leave', {
      pageNum: leavePage.value.current,
      pageSize: leavePage.value.pageSize
    }) as any
    leaveData.value = res.data?.records || []
    leavePage.value.total = res.data?.total || 0
  } catch { message.error('加载请假记录失败') } finally { loading.value = false }
}

async function fetchOvertime() {
  loading.value = true
  try {
    const res = await request.get('/system/attendance/overtime', {
      pageNum: overtimePage.value.current,
      pageSize: overtimePage.value.pageSize
    }) as any
    overtimeData.value = res.data?.records || []
    overtimePage.value.total = res.data?.total || 0
  } catch { message.error('加载加班记录失败') } finally { loading.value = false }
}

function onTabChange() {
  if (activeTab.value === 'records') fetchRecords()
  else if (activeTab.value === 'leave') fetchLeave()
  else if (activeTab.value === 'overtime') fetchOvertime()
}

function handleRecordPageChange(pag: any) { recordPage.value.current = pag.current; recordPage.value.pageSize = pag.pageSize; fetchRecords() }
function handleLeavePageChange(pag: any) { leavePage.value.current = pag.current; leavePage.value.pageSize = pag.pageSize; fetchLeave() }
function handleOvertimePageChange(pag: any) { overtimePage.value.current = pag.current; overtimePage.value.pageSize = pag.pageSize; fetchOvertime() }

function openLeaveCreate() {
  leaveForm.leaveType = undefined; leaveForm.startTime = null; leaveForm.endTime = null; leaveForm.reason = ''
  leaveModalVisible.value = true
}

function openOvertimeCreate() {
  overtimeForm.startTime = null; overtimeForm.endTime = null; overtimeForm.reason = ''
  overtimeModalVisible.value = true
}

async function submitLeave() {
  if (!leaveForm.leaveType) { message.warning('请选择请假类型'); return }
  if (!leaveForm.startTime || !leaveForm.endTime) { message.warning('请选择时间'); return }
  submitting.value = true
  try {
    await request.post('/system/attendance/leave', {
      leaveType: leaveForm.leaveType,
      startTime: dayjs(leaveForm.startTime).format('YYYY-MM-DD HH:mm:ss'),
      endTime: dayjs(leaveForm.endTime).format('YYYY-MM-DD HH:mm:ss'),
      reason: leaveForm.reason
    })
    message.success('提交成功')
    leaveModalVisible.value = false
    fetchLeave()
  } catch { message.error('操作失败') } finally { submitting.value = false }
}

async function submitOvertime() {
  if (!overtimeForm.startTime || !overtimeForm.endTime) { message.warning('请选择时间'); return }
  submitting.value = true
  try {
    await request.post('/system/attendance/overtime', {
      startTime: dayjs(overtimeForm.startTime).format('YYYY-MM-DD HH:mm:ss'),
      endTime: dayjs(overtimeForm.endTime).format('YYYY-MM-DD HH:mm:ss'),
      reason: overtimeForm.reason
    })
    message.success('提交成功')
    overtimeModalVisible.value = false
    fetchOvertime()
  } catch { message.error('操作失败') } finally { submitting.value = false }
}

async function approveLeave(id: number) {
  try {
    await request.put(`/system/attendance/leave/${id}/approve`)
    message.success('审批成功')
    fetchLeave()
  } catch { message.error('操作失败') }
}

async function approveOvertime(id: number) {
  try {
    await request.put(`/system/attendance/overtime/${id}/approve`)
    message.success('审批成功')
    fetchOvertime()
  } catch { message.error('操作失败') }
}

onMounted(() => { fetchRecords() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
