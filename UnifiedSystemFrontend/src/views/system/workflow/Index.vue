<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">审批工作台</h2>
    </div>
    <a-card class="content-card">
      <a-tabs v-model:activeKey="activeTab" @change="onTabChange">
        <a-tab-pane key="pending" tab="待我审批" />
        <a-tab-pane key="done" tab="我的已办" />
        <a-tab-pane key="my" tab="我的申请" />
      </a-tabs>
      <a-spin :spinning="loading">
        <a-table :columns="columns" :data-source="dataSource" row-key="id"
          :pagination="{ current, pageSize, total, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` }"
          @change="handleTableChange">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'businessType'">
              <a-tag :color="typeColorMap[record.businessType] || 'default'">
                {{ typeNameMap[record.businessType] || record.businessType }}
              </a-tag>
            </template>
            <template v-if="column.key === 'node'">
              <span v-if="record.status === 'pending'">第 {{ record.currentNodeOrder }} 级</span>
              <span v-else>-</span>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="statusColorMap[record.status] || 'default'">
                {{ statusNameMap[record.status] || record.status }}
              </a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a-space v-if="activeTab === 'pending'">
                <a-button size="small" type="primary" @click="handleApprove(record)">通过</a-button>
                <a-button size="small" danger @click="showReject(record)">驳回</a-button>
                <a-button size="small" @click="handlePushUp(record)">上推</a-button>
                <a-button size="small" @click="showDelegate(record)">委托</a-button>
              </a-space>
              <a-space v-else>
                <a-button size="small" @click="viewDetail(record)">查看详情</a-button>
                <a-popconfirm v-if="activeTab === 'my' && record.status === 'pending'"
                  title="确认撤销该申请？" @confirm="handleCancel(record)">
                  <a-button size="small" danger>撤销</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <a-modal v-model:open="rejectVisible" title="驳回原因" @ok="handleReject"
      :confirm-loading="submitting" :maskClosable="false">
      <a-textarea v-model:value="rejectComment" :rows="3" placeholder="请填写驳回原因" />
    </a-modal>

    <a-modal v-model:open="delegateVisible" title="委托审批" @ok="handleDelegate"
      :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 5 }">
        <a-form-item label="被委托人">
          <a-select v-model:value="delegateTargetId" :options="userOptions" show-search
            :filter-option="filterUser" placeholder="选择用户" style="width:100%" />
        </a-form-item>
        <a-form-item label="说明">
          <a-textarea v-model:value="delegateComment" :rows="2" placeholder="委托说明（可选）" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="detailVisible" title="审批详情" :footer="null" width="600px">
      <a-descriptions bordered :column="1" size="small">
        <a-descriptions-item label="业务类型">{{ typeNameMap[detailRecord?.businessType] }}</a-descriptions-item>
        <a-descriptions-item label="业务单据ID">{{ detailRecord?.businessId }}</a-descriptions-item>
        <a-descriptions-item label="审批状态">
          <a-tag :color="statusColorMap[detailRecord?.status] || 'default'">
            {{ statusNameMap[detailRecord?.status] || detailRecord?.status }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="当前节点">
          第 {{ detailRecord?.currentNodeOrder }} 级
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ detailRecord?.createTime }}</a-descriptions-item>
      </a-descriptions>
      <a-divider>审批记录</a-divider>
      <a-timeline>
        <a-timeline-item v-for="r in approvalRecords" :key="r.id"
          :color="({approve:'green',reject:'red',push_up:'orange',delegate:'blue'} as Record<string, string>)[r.approverAction]">
          <div>审批人: {{ r.approverId }}</div>
          <div>操作: {{ ({approve:'通过',reject:'驳回',push_up:'上推',delegate:'委托'} as Record<string, string>)[r.approverAction] }}</div>
          <div v-if="r.comment">意见: {{ r.comment }}</div>
          <div class="record-time">{{ r.actionTime }}</div>
        </a-timeline-item>
        <a-timeline-item v-if="!approvalRecords.length" color="gray">暂无审批记录</a-timeline-item>
      </a-timeline>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import request from '@/api/request'

const typeNameMap: Record<string, string> = {
  purchase_request: '采购申请', sales_order: '销售报单', transfer: '调拨单',
  expense: '费用报销', leave: '请假申请', overtime: '加班申请'
}
const typeColorMap: Record<string, string> = {
  purchase_request: 'blue', sales_order: 'purple', transfer: 'cyan',
  expense: 'orange', leave: 'green', overtime: 'volcano'
}
const statusNameMap: Record<string, string> = {
  pending: '审批中', approved: '已通过', rejected: '已驳回', cancelled: '已撤销'
}
const statusColorMap: Record<string, string> = {
  pending: 'orange', approved: 'green', rejected: 'red', cancelled: 'default'
}

const activeTab = ref('pending')
const loading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dataSource = ref<any[]>([])

const columns = [
  { title: '业务类型', key: 'businessType', width: 110 },
  { title: '业务编号', dataIndex: 'businessId', width: 100 },
  { title: '当前节点', key: 'node', width: 90 },
  { title: '状态', key: 'status', width: 90 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 260 }
]

const rejectVisible = ref(false)
const delegateVisible = ref(false)
const detailVisible = ref(false)
const submitting = ref(false)
const rejectComment = ref('')
const rejectTarget = ref<any>(null)
const delegateTarget = ref<any>(null)
const delegateTargetId = ref<number>()
const delegateComment = ref('')
const userOptions = ref<Array<{ label: string; value: number }>>([])
const detailRecord = ref<any>(null)
const approvalRecords = ref<any[]>([])

async function fetchData() {
  loading.value = true
  try {
    const endpoint = activeTab.value === 'pending'
      ? '/workflow/pending'
      : activeTab.value === 'done' ? '/workflow/done' : '/workflow/my-applications'
    const res = await request.get(endpoint) as any
    dataSource.value = res.data || []
    total.value = dataSource.value.length
  } catch { } finally { loading.value = false }
}

async function fetchUsers() {
  try {
    const res = await request.get('/system/users', { params: { pageNum: 1, pageSize: 200 } }) as any
    userOptions.value = (res.data?.records || []).map((u: any) => ({
      label: `${u.realName || u.username}（${u.username}）`,
      value: u.id
    }))
  } catch { }
}

function onTabChange() { current.value = 1; fetchData() }
function handleTableChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize }
function filterUser(input: string, option: any) { return String(option.label).toLowerCase().includes(input.toLowerCase()) }

async function handleApprove(record: any) {
  try {
    await request.post(`/workflow/instances/${record.id}/approve`, null, { params: { comment: '同意' } })
    message.success('审批通过'); fetchData()
  } catch { }
}

function showReject(record: any) {
  rejectTarget.value = record; rejectComment.value = ''; rejectVisible.value = true
}

async function handleReject() {
  if (!rejectComment.value) { message.warning('请填写驳回原因'); return }
  submitting.value = true
  try {
    await request.post(`/workflow/instances/${rejectTarget.value.id}/reject`, null, { params: { comment: rejectComment.value } })
    message.success('已驳回'); rejectVisible.value = false; fetchData()
  } catch { } finally { submitting.value = false }
}

async function handlePushUp(record: any) {
  try {
    await request.post(`/workflow/instances/${record.id}/push-up`, null, { params: { comment: '上推审批' } })
    message.success('已上推至终审'); fetchData()
  } catch { }
}

function showDelegate(record: any) {
  delegateTarget.value = record; delegateTargetId.value = undefined
  delegateComment.value = ''; delegateVisible.value = true
}

async function handleDelegate() {
  if (!delegateTargetId.value) { message.warning('请选择被委托人'); return }
  submitting.value = true
  try {
    await request.post(`/workflow/instances/${delegateTarget.value.id}/delegate`, null, {
      params: { targetUserId: delegateTargetId.value, comment: delegateComment.value }
    })
    message.success('已委托'); delegateVisible.value = false; fetchData()
  } catch { } finally { submitting.value = false }
}

async function handleCancel(record: any) {
  try {
    await request.post(`/workflow/instances/${record.id}/cancel`)
    message.success('已撤销'); fetchData()
  } catch { }
}

async function viewDetail(record: any) {
  detailRecord.value = record; detailVisible.value = true
  try {
    const res = await request.get(`/workflow/instances/${record.id}/records`) as any
    approvalRecords.value = res.data || []
  } catch { approvalRecords.value = [] }
}

onMounted(() => { fetchData(); fetchUsers() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
.record-time { color: #9ca3af; font-size: 12px; }
</style>
