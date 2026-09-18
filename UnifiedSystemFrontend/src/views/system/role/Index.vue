<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">角色管理</h2>
      <a-space>
        <a-input-search
          v-model:value="keyword"
          placeholder="搜索角色名称/编码"
          style="width: 240px"
          @search="handleSearch"
        />
        <a-button type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          新建角色
        </a-button>
      </a-space>
    </div>
    <a-card class="content-card">
      <a-spin :spinning="loading">
        <div ref="tableWrapRef">
          <a-table
            :columns="columns"
            :data-source="dataSource"
            :pagination="{ current, pageSize, total, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` }"
            row-key="id"
            :row-class-name="rowClassName"
            :custom-row="customRow"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'drag'">
                <a-tooltip v-if="!dragEnabled" title="清除搜索条件后可拖动排序">
                  <HolderOutlined class="drag-handle is-disabled" />
                </a-tooltip>
                <HolderOutlined v-else class="drag-handle" />
              </template>
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 1 ? 'green' : 'red'">
                  {{ record.status === 1 ? '启用' : '禁用' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-space>
                  <a @click="openEdit(record)">编辑</a>
                  <a-popconfirm title="确认删除？" @confirm="handleDelete(record.id)">
                    <a class="danger-link">删除</a>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </div>
      </a-spin>
    </a-card>

    <a-modal v-model:open="modalVisible" :title="modalTitle" @ok="handleSubmit"
      :confirm-loading="submitting" :maskClosable="false">
      <a-form ref="formRef" :model="formState" :rules="rules" :label-col="{ span: 5 }">
        <a-form-item label="角色名称" name="roleName">
          <a-input v-model:value="formState.roleName" />
        </a-form-item>
        <a-form-item label="角色编码" name="roleCode">
          <a-input v-model:value="formState.roleCode" :disabled="isEdit" />
        </a-form-item>
        <a-form-item label="等级" name="level">
          <a-input-number v-model:value="formState.level" :min="0" :max="1000" style="width:100%" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-switch v-model:checked="statusChecked" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="formState.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { PlusOutlined, HolderOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import request from '@/api/request'
import { initRowDragSort } from '@/utils/dragSort'
import type Sortable from 'sortablejs'

const loading = ref(false)
const keyword = ref('')
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dataSource = ref<any[]>([])
const tableWrapRef = ref<HTMLElement>()
let sortable: Sortable | null = null

const dragEnabled = computed(() => keyword.value.trim() === '')

const columns = [
  { title: '', key: 'drag', width: 44, align: 'center' as const },
  { title: '角色名称', dataIndex: 'roleName' },
  { title: '角色编码', dataIndex: 'roleCode' },
  { title: '等级', dataIndex: 'level', width: 70 },
  { title: '状态', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 120 }
]

const modalVisible = ref(false)
const modalTitle = computed(() => isEdit.value ? '编辑角色' : '新建角色')
const formRef = ref()
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref<number>()
const statusChecked = ref(true)

const formState = reactive({ roleName: '', roleCode: '', level: 0, status: 1, description: '' })
const rules = {
  roleName: [{ required: true, message: '请输入角色名称' }],
  roleCode: [{ required: true, message: '请输入角色编码' }],
}

function rowClassName(): string {
  return dragEnabled.value ? 'row-draggable' : 'row-locked'
}

function customRow(record: any) {
  return { 'data-row-key': String(record.id) }
}

function setupDrag() {
  if (sortable) return
  const tbody = tableWrapRef.value?.querySelector<HTMLElement>('.ant-table-tbody')
  if (!tbody) return
  sortable = initRowDragSort(tbody, { onReorder: saveOrder })
}

async function saveOrder() {
  const tbody = tableWrapRef.value?.querySelector<HTMLElement>('.ant-table-tbody')
  if (!tbody) return
  const rows = Array.from(tbody.querySelectorAll<HTMLElement>('tr[data-row-key]'))
  const offset = (current.value - 1) * pageSize.value
  const payload = rows.map((tr, index) => ({ id: Number(tr.dataset.rowKey), sortOrder: offset + index + 1 }))
  try {
    await request.put('/system/roles/sort', payload)
    message.success('排序已保存')
  } catch { } finally {
    fetchData()
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/system/roles', { params: { pageNum: current.value, pageSize: pageSize.value, keyword: keyword.value } }) as any
    dataSource.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

function handleSearch() { current.value = 1; fetchData() }
function handleTableChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }

function openCreate() {
  isEdit.value = false
  formState.roleName = ''; formState.roleCode = ''; formState.level = 0; formState.description = ''
  statusChecked.value = true; modalVisible.value = true
}

function openEdit(r: any) {
  isEdit.value = true; editId.value = r.id
  formState.roleName = r.roleName; formState.roleCode = r.roleCode
  formState.level = r.level || 0
  formState.description = r.description || ''; statusChecked.value = r.status === 1; modalVisible.value = true
}

async function handleSubmit() {
  if (submitting.value) return
  try { await formRef.value?.validate() } catch { return }
  submitting.value = true
  try {
    formState.status = statusChecked.value ? 1 : 0
    if (isEdit.value) {
      await request.put(`/system/roles/${editId.value}`, formState); message.success('更新成功')
    } else {
      await request.post('/system/roles', formState); message.success('创建成功')
    }
    modalVisible.value = false; fetchData()
  } catch { } finally { submitting.value = false }
}

async function handleDelete(id: number) {
  try { await request.delete(`/system/roles/${id}`); message.success('删除成功'); fetchData() } catch { }
}

onMounted(async () => {
  await fetchData()
  await nextTick()
  setupDrag()
})

onUnmounted(() => {
  sortable?.destroy()
  sortable = null
})
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
