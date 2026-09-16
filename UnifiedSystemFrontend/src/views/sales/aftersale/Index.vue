<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">售后服务</h2>
      <a-button type="primary" @click="openCreate">
        <template #icon><PlusOutlined /></template>
        新增记录
      </a-button>
    </div>
    <a-card class="content-card">
      <a-spin :spinning="loading">
        <a-empty v-if="!loading && dataSource.length === 0" description="暂无售后记录" />
        <a-table v-else :columns="columns" :data-source="dataSource" row-key="id"
          :pagination="pagination" @change="handlePageChange">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === 1 ? 'green' : 'blue'">{{ record.status === 1 ? '已关闭' : '处理中' }}</a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a v-if="record.status !== 1" @click="handleClose(record)">关闭</a>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <a-modal v-model:open="modalVisible" title="新增售后记录" @ok="handleSubmit" :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 6 }">
        <a-form-item label="关联订单ID"><a-input v-model:value="formState.orderId" /></a-form-item>
        <a-form-item label="售后类型">
          <a-select v-model:value="formState.type" style="width:100%">
            <a-select-option value="return">退货</a-select-option>
            <a-select-option value="repair">维修</a-select-option>
            <a-select-option value="complaint">投诉</a-select-option>
            <a-select-option value="other">其他</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="问题描述"><a-textarea v-model:value="formState.description" :rows="3" /></a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { getAfterSales, createAfterSale, closeAfterSale } from '@/api/modules/sales'

const loading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dataSource = ref<any[]>([])

const columns = [
  { title: '订单ID', dataIndex: 'orderId', width: 100 },
  { title: '售后类型', dataIndex: 'type', width: 100 },
  { title: '问题描述', dataIndex: 'description', ellipsis: true },
  { title: '状态', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 80 }
]

const pagination = computed(() => ({
  current: current.value, pageSize: pageSize.value, total: total.value,
  showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条`
}))

const modalVisible = ref(false)
const submitting = ref(false)
const formState = reactive({ orderId: '', type: 'return', description: '' })

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getAfterSales({ pageNum: current.value, pageSize: pageSize.value })
    dataSource.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

function openCreate() { formState.orderId = ''; formState.type = 'return'; formState.description = ''; modalVisible.value = true }

async function handleSubmit() {
  if (!formState.orderId || !formState.description) { message.warning('请填写完整信息'); return }
  submitting.value = true
  try {
    await createAfterSale(formState)
    message.success('新增成功'); modalVisible.value = false; fetchData()
  } catch { } finally { submitting.value = false }
}

async function handleClose(record: any) {
  try {
    await closeAfterSale(record.id)
    message.success('已关闭'); fetchData()
  } catch { }
}

function handlePageChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }

onMounted(fetchData)
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
