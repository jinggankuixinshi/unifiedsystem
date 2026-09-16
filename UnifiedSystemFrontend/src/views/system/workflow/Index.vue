<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">审批工作台</h2>
    </div>
    <a-card class="content-card">
      <a-tabs v-model:activeKey="activeTab" @change="fetchData">
        <a-tab-pane key="pending" tab="待我审批" />
        <a-tab-pane key="my" tab="我的申请" />
      </a-tabs>
      <a-spin :spinning="loading">
        <a-table :columns="columns" :data-source="dataSource" row-key="id"
          :pagination="{ current, pageSize, total }" @change="handleTableChange">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'businessType'">
              <a-tag :color="typeColorMap[record.businessType] || 'default'">
                {{ typeNameMap[record.businessType] || record.businessType }}
              </a-tag>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="{pending:'orange',approved:'green',rejected:'red'}[record.status]">
                {{ {pending:'审批中',approved:'已通过',rejected:'已驳回'}[record.status] }}
              </a-tag>
            </template>
            <template v-if="column.key === 'action' && activeTab === 'pending'">
              <a-space>
                <a-button size="small" type="primary" @click="handleApprove(record)">通过</a-button>
                <a-button size="small" danger @click="showReject(record)">驳回</a-button>
              </a-space>
            </template>
            <template v-if="column.key === 'action' && activeTab === 'my'">
              <a-button size="small" @click="viewDetail(record)">查看详情</a-button>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <a-modal v-model:open="rejectVisible" title="驳回原因" @ok="handleReject"
      :confirm-loading="submitting" :maskClosable="false">
      <a-textarea v-model:value="rejectComment" :rows="3" placeholder="请填写驳回原因" />
    </a-modal>

    <a-modal v-model:open="detailVisible" title="审批详情" :footer="null" width="600px">
      <a-descriptions bordered :column="1" size="small">
        <a-descriptions-item label="业务类型">{{ typeNameMap[detailRecord?.businessType] }}</a-descriptions-item>
        <a-descriptions-item label="业务单据ID">{{ detailRecord?.businessId }}</a-descriptions-item>
        <a-descriptions-item label="审批状态">
          <a-tag :color="{pending:'orange',approved:'green',rejected:'red'}[detailRecord?.status]">
            {{ {pending:'审批中',approved:'已通过',rejected:'已驳回'}[detailRecord?.status] }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ detailRecord?.createTime }}</a-descriptions-item>
      </a-descriptions>
      <a-divider>审批记录</a-divider>
      <a-timeline>
        <a-timeline-item v-for="r in approvalRecords" :key="r.id"
          :color="{approve:'green',reject:'red',push_up:'orange'}[r.approverAction]">
          <div>审批人: {{ r.approverId }}</div>
          <div>操作: {{ {approve:'通过',reject:'驳回',push_up:'上推'}[r.approverAction] }}</div>
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

const activeTab = ref('pending')
const loading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dataSource = ref<any[]>([])

const columns = [
  { title: '业务类型', key: 'businessType', width: 100 },
  { title: '业务编号', dataIndex: 'businessId' },
  { title: '当前节点', dataIndex: 'currentNodeOrder' },
  { title: '状态', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 160 }
]

const rejectVisible = ref(false)
const detailVisible = ref(false)
const submitting = ref(false)
const rejectComment = ref('')
const rejectTarget = ref<any>(null)
const detailRecord = ref<any>(null)
const approvalRecords = ref<any[]>([])

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/workflow/pending') as any
    dataSource.value = (res.data || []).filter((r: any) =>
      activeTab.value === 'pending' ? r.status === 'pending' : true
    )
    total.value = dataSource.value.length
  } catch { } finally { loading.value = false }
}

function handleTableChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize }

async function handleApprove(record: any) {
  try {
    await request.post(`/workflow/instances/${record.id}/approve`, null, { params: { comment: '同意' } })
    message.success('审批通过'); fetchData()
  } catch { message.error('操作失败') }
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

async function viewDetail(record: any) {
  detailRecord.value = record; detailVisible.value = true
  try {
    const res = await request.get(`/workflow/instances/${record.id}/records`) as any
    approvalRecords.value = res.data || []
  } catch { approvalRecords.value = [] }
}

onMounted(fetchData)
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
.record-time { color: #9ca3af; font-size: 12px; }
</style>
