<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">用户管理</h2>
      <a-space>
        <a-input-search
          v-model:value="keyword"
          placeholder="搜索用户名/姓名/手机号"
          style="width: 260px"
          @search="handleSearch"
          @change="handleSearch"
        />
        <a-button type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新建用户
        </a-button>
      </a-space>
    </div>
    <a-card class="content-card">
      <a-spin :spinning="loading">
        <a-table
          :columns="columns"
          :data-source="dataSource"
          :pagination="{ current, pageSize, total, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` }"
          row-key="id"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === 1 ? 'green' : 'red'">
                {{ record.status === 1 ? '正常' : '禁用' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a-space>
                <a @click="openEdit(record)">编辑</a>
                <a-popconfirm title="确认删除该用户？" @confirm="handleDelete(record.id)">
                  <a class="danger-link">删除</a>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      @ok="handleSubmit"
      :confirm-loading="submitting"
      :maskClosable="false"
    >
      <a-form ref="formRef" :model="formState" :rules="rules" :label-col="{ span: 5 }">
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="formState.username" />
        </a-form-item>
        <a-form-item label="密码" :name="isEdit ? undefined : 'password'">
          <a-input-password v-model:value="formState.password" :placeholder="isEdit ? '留空则不修改' : '请输入密码'" />
        </a-form-item>
        <a-form-item label="姓名" name="realName">
          <a-input v-model:value="formState.realName" />
        </a-form-item>
        <a-form-item label="部门" name="deptId">
          <a-tree-select
            v-model:value="formState.deptId"
            :tree-data="deptTree"
            :field-names="{ children: 'children', label: 'deptName', value: 'id' }"
            tree-default-expand-all
            placeholder="请选择部门"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="formState.phone" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="formState.email" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-switch v-model:checked="statusChecked" checked-children="正常" un-checked-children="禁用" />
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
  { title: '用户名', dataIndex: 'username', key: 'username' },
  { title: '姓名', dataIndex: 'realName', key: 'realName' },
  { title: '部门', dataIndex: 'deptName', key: 'deptName' },
  { title: '手机号', dataIndex: 'phone', key: 'phone' },
  { title: '状态', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 120 }
]

const modalVisible = ref(false)
const modalTitle = computed(() => isEdit.value ? '编辑用户' : '新建用户')
const formRef = ref()
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref<number>()
const statusChecked = ref(true)
const deptTree = ref<any[]>([])

const formState = reactive<any>({
  username: '', password: '', realName: '', deptId: null, phone: '', email: '', status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }],
  realName: [{ required: true, message: '请输入姓名' }],
  deptId: [{ required: true, message: '请选择部门' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/system/users', { params: { pageNum: current.value, pageSize: pageSize.value, keyword: keyword.value } })
    const data = res as any
    dataSource.value = data.data?.records || []
    total.value = data.data?.total || 0
  } catch { } finally {
    loading.value = false
  }
}

async function fetchDeptTree() {
  try {
    const res = await request.get('/system/departments/tree') as any
    deptTree.value = res.data || []
  } catch { }
}

function handleSearch() {
  current.value = 1
  fetchData()
}

function handleTableChange(pag: any) {
  current.value = pag.current
  pageSize.value = pag.pageSize
  fetchData()
}

function openCreate() {
  isEdit.value = false
  formState.username = ''
  formState.password = ''
  formState.realName = ''
  formState.deptId = null
  formState.phone = ''
  formState.email = ''
  statusChecked.value = true
  modalVisible.value = true
}

function openEdit(record: any) {
  isEdit.value = true
  editId.value = record.id
  formState.username = record.username
  formState.password = ''
  formState.realName = record.realName
  formState.deptId = record.deptId
  formState.phone = record.phone
  formState.email = record.email
  statusChecked.value = record.status === 1
  modalVisible.value = true
}

async function handleSubmit() {
  if (submitting.value) return
  try {
    await formRef.value?.validate()
  } catch { return }
  submitting.value = true
  try {
    formState.status = statusChecked.value ? 1 : 0
    if (isEdit.value) {
      await request.put(`/system/users/${editId.value}`, formState)
      message.success('更新成功')
    } else {
      await request.post('/system/users', formState)
      message.success('创建成功')
    }
    modalVisible.value = false
    fetchData()
  } catch { } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await request.delete(`/system/users/${id}`)
    message.success('删除成功')
    fetchData()
  } catch { }
}

onMounted(() => {
  fetchData()
  fetchDeptTree()
})
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
