<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">客户管理</h2>
      <a-space>
        <a-input-search
          v-model:value="keyword"
          placeholder="搜索客户名称"
          style="width: 260px"
          @search="handleSearch"
        />
        <a-button type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新增客户
        </a-button>
      </a-space>
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
              <a-tag :color="record.status === '正常' ? 'green' : record.status === '禁用' ? 'red' : 'default'">
                {{ record.status || '正常' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a-space size="small">
                <a @click="openEdit(record)">编辑</a>
                <a-popconfirm v-if="record.status !== '禁用'" title="确认禁用该客户？" @confirm="toggleStatus(record.id, '禁用')">
                  <a>禁用</a>
                </a-popconfirm>
                <a-popconfirm title="确认存档该客户？" @confirm="toggleStatus(record.id, '存档')">
                  <a>存档</a>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <a-modal v-model:open="modalVisible" :title="modalTitle" @ok="handleSubmit" :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 6 }">
        <a-form-item label="客户编码">
          <a-input v-model:value="formState.customerCode" />
        </a-form-item>
        <a-form-item label="客户名称">
          <a-input v-model:value="formState.customerName" />
        </a-form-item>
        <a-form-item label="联系人">
          <a-input v-model:value="formState.contactPerson" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model:value="formState.phone" />
        </a-form-item>
        <a-form-item label="地址">
          <a-input v-model:value="formState.address" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="formState.status" style="width:100%">
            <a-select-option value="正常">正常</a-select-option>
            <a-select-option value="禁用">禁用</a-select-option>
            <a-select-option value="存档">存档</a-select-option>
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
import request from '@/api/request'

const loading = ref(false)
const keyword = ref('')
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dataSource = ref<any[]>([])

const columns = [
  { title: '客户编码', dataIndex: 'customerCode', key: 'customerCode' },
  { title: '客户名称', dataIndex: 'customerName', key: 'customerName' },
  { title: '联系人', dataIndex: 'contactPerson', key: 'contactPerson' },
  { title: '手机号', dataIndex: 'phone', key: 'phone' },
  { title: '地址', dataIndex: 'address', key: 'address', ellipsis: true },
  { title: '状态', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 160 }
]

const pagination = computed(() => ({
  current: current.value, pageSize: pageSize.value, total: total.value,
  showSizeChanger: true, pageSizeOptions: ['10', '20', '50', '100', '200'],
  showTotal: (t: number) => `共 ${t} 条`
}))

const modalVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number>()
const submitting = ref(false)

const formState = reactive({
  customerCode: '', customerName: '', contactPerson: '', phone: '', address: '', status: '正常'
})

const modalTitle = computed(() => isEdit.value ? '编辑客户' : '新增客户')

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/sales/customers', {
      pageNum: current.value, pageSize: pageSize.value, keyword: keyword.value
    }) as any
    dataSource.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { message.error('加载客户列表失败') } finally { loading.value = false }
}

function handleSearch() { current.value = 1; fetchData() }
function handlePageChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }

function openCreate() {
  isEdit.value = false
  formState.customerCode = ''; formState.customerName = ''; formState.contactPerson = ''
  formState.phone = ''; formState.address = ''; formState.status = '正常'
  modalVisible.value = true
}

function openEdit(record: any) {
  isEdit.value = true; editId.value = record.id
  formState.customerCode = record.customerCode; formState.customerName = record.customerName
  formState.contactPerson = record.contactPerson; formState.phone = record.phone
  formState.address = record.address; formState.status = record.status
  modalVisible.value = true
}

async function handleSubmit() {
  if (!formState.customerName) { message.warning('请输入客户名称'); return }
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/sales/customers/${editId.value}`, formState)
      message.success('更新成功')
    } else {
      await request.post('/sales/customers', formState)
      message.success('创建成功')
    }
    modalVisible.value = false
    fetchData()
  } catch { message.error('操作失败') } finally { submitting.value = false }
}

async function toggleStatus(id: number, status: string) {
  try {
    await request.put(`/sales/customers/${id}`, { status })
    message.success('操作成功')
    fetchData()
  } catch { message.error('操作失败') }
}

onMounted(() => { fetchData() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
