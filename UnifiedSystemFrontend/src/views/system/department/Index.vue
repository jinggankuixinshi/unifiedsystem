<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">部门管理</h2>
      <a-button type="primary" @click="openCreate(null)">
        <template #icon><PlusOutlined /></template>
        新建部门
      </a-button>
    </div>
    <a-card class="content-card">
      <a-spin :spinning="loading">
        <div ref="tableWrapRef">
          <a-table
            :columns="columns"
            :data-source="deptTree"
            :pagination="false"
            row-key="id"
            v-model:expanded-row-keys="expandedKeys"
            :expand-icon-column-index="1"
            :row-class-name="rowClassName"
            :custom-row="customRow"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'drag'">
                <a-tooltip v-if="!isDraggable(record)" title="收起子级后可拖动排序">
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
                  <a @click="openCreate(record)">添加子部门</a>
                  <a @click="openEdit(record)">编辑</a>
                  <a-popconfirm title="确认删除？" @confirm="handleDelete(record.id)">
                    <a class="danger-link">删除</a>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </div>
        <div class="table-total">共 {{ totalCount }} 条</div>
      </a-spin>
    </a-card>

    <a-modal v-model:open="modalVisible" :title="isEdit ? '编辑部门' : '新建部门'"
      @ok="handleSubmit" :confirm-loading="submitting" :maskClosable="false">
      <a-form ref="formRef" :model="formState" :rules="rules" :label-col="{ span: 5 }">
        <a-form-item label="上级部门">
          <a-tree-select
            v-model:value="formState.parentId"
            :tree-data="deptOptions"
            :field-names="{ children: 'children', label: 'deptName', value: 'id' }"
            tree-default-expand-all
            allow-clear
            placeholder="不选则为根部门"
            style="width:100%"
          />
        </a-form-item>
        <a-form-item label="部门名称" name="deptName">
          <a-input v-model:value="formState.deptName" />
        </a-form-item>
        <a-form-item label="部门编码" name="deptCode">
          <a-input v-model:value="formState.deptCode" />
        </a-form-item>
        <a-form-item label="负责人">
          <a-input v-model:value="formState.leader" />
        </a-form-item>
        <a-form-item label="电话">
          <a-input v-model:value="formState.phone" />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model:checked="statusChecked" />
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
const deptTree = ref<any[]>([])
const deptOptions = ref<any[]>([])
const expandedKeys = ref<Array<string | number>>([])
const totalCount = ref(0)
const tableWrapRef = ref<HTMLElement>()
let sortable: Sortable | null = null

const columns = [
  { title: '', key: 'drag', width: 44, align: 'center' as const },
  { title: '部门名称', dataIndex: 'deptName' },
  { title: '部门编码', dataIndex: 'deptCode' },
  { title: '负责人', dataIndex: 'leader' },
  { title: '状态', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 200 }
]

const modalVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number>()
const formRef = ref()
const submitting = ref(false)
const statusChecked = ref(true)

const formState = reactive({ parentId: null as number | null, deptName: '', deptCode: '', leader: '', phone: '' })
const rules = {
  deptName: [{ required: true, message: '请输入部门名称' }],
  deptCode: [{ required: true, message: '请输入部门编码' }]
}

function countNodes(nodes: any[]): number {
  let count = 0
  for (const node of nodes) {
    count += 1
    if (Array.isArray(node.children) && node.children.length > 0) {
      count += countNodes(node.children)
    }
  }
  return count
}

const rowInfoMap = computed(() => {
  const map = new Map<number, { shade: number; colored: boolean; blockStart: boolean; blockEnd: boolean }>()
  const order: Array<{ id: number; block: number | null }> = []
  const walk = (nodes: any[], depth: number, block: number | null) => {
    for (const node of nodes) {
      const expanded = expandedKeys.value.includes(node.id)
      const selfBlock = block ?? (expanded ? node.id : null)
      map.set(node.id, {
        shade: Math.min(depth, 5),
        colored: selfBlock !== null,
        blockStart: false,
        blockEnd: false
      })
      order.push({ id: node.id, block: selfBlock })
      if (expanded && Array.isArray(node.children) && node.children.length > 0) {
        walk(node.children, depth + 1, selfBlock)
      }
    }
  }
  walk(deptTree.value, 1, null)
  order.forEach((cur, i) => {
    if (cur.block === null) return
    const info = map.get(cur.id)!
    if (i === 0 || order[i - 1].block !== cur.block) info.blockStart = true
    if (i === order.length - 1 || order[i + 1].block !== cur.block) info.blockEnd = true
  })
  return map
})

async function fetchTree() {
  loading.value = true
  try {
    const res = await request.get('/system/departments/tree') as any
    const tree = res.data || []
    totalCount.value = countNodes(tree)
    deptTree.value = tree
    deptOptions.value = tree
  } catch { message.error('加载失败') } finally { loading.value = false }
}

function isDraggable(record: any): boolean {
  const hasChildren = Array.isArray(record.children) && record.children.length > 0
  return !hasChildren || !expandedKeys.value.includes(record.id)
}

function rowClassName(record: any): string {
  const info = rowInfoMap.value.get(record.id)
  const classes: string[] = []
  if (info?.colored) {
    classes.push(`tree-depth-${info.shade}`, 'tree-block-member')
    if (info.blockStart) classes.push('tree-block-start')
    if (info.blockEnd) classes.push('tree-block-end')
  }
  classes.push(isDraggable(record) ? 'row-draggable' : 'row-locked')
  return classes.join(' ')
}

function customRow(record: any) {
  return {
    'data-row-key': String(record.id),
    'data-parent-id': String(record.parentId ?? 0)
  }
}

function setupDrag() {
  if (sortable) return
  const tbody = tableWrapRef.value?.querySelector<HTMLElement>('.ant-table-tbody')
  if (!tbody) return
  sortable = initRowDragSort(tbody, { sameParentOnly: true, onReorder: saveOrder })
}

async function saveOrder(dragged: HTMLElement) {
  const parentId = dragged.dataset.parentId ?? '0'
  const tbody = tableWrapRef.value?.querySelector<HTMLElement>('.ant-table-tbody')
  if (!tbody) return
  const rows = Array.from(tbody.querySelectorAll<HTMLElement>('tr[data-row-key]'))
    .filter(tr => (tr.dataset.parentId ?? '0') === parentId)
  const payload = rows.map((tr, index) => ({ id: Number(tr.dataset.rowKey), sortOrder: index + 1 }))
  try {
    await request.put('/system/departments/sort', payload)
    message.success('排序已保存')
  } catch {
    message.error('排序保存失败')
  } finally {
    fetchTree()
  }
}

function openCreate(parent: any) {
  isEdit.value = false
  formState.parentId = parent?.id || null
  formState.deptName = ''; formState.deptCode = ''; formState.leader = ''
  formState.phone = ''; statusChecked.value = true
  modalVisible.value = true
}

function openEdit(r: any) {
  isEdit.value = true; editId.value = r.id
  formState.parentId = r.parentId || null; formState.deptName = r.deptName
  formState.deptCode = r.deptCode; formState.leader = r.leader || ''
  formState.phone = r.phone || ''
  statusChecked.value = r.status === 1; modalVisible.value = true
}

async function handleSubmit() {
  if (submitting.value) return
  try { await formRef.value?.validate() } catch { return }
  submitting.value = true
  try {
    const payload = { ...formState, status: statusChecked.value ? 1 : 0 }
    if (isEdit.value) {
      await request.put(`/system/departments/${editId.value}`, payload); message.success('更新成功')
    } else {
      await request.post('/system/departments', payload); message.success('创建成功')
    }
    modalVisible.value = false; fetchTree()
  } catch { message.error('操作失败') } finally { submitting.value = false }
}

async function handleDelete(id: number) {
  try { await request.delete(`/system/departments/${id}`); message.success('删除成功'); fetchTree() } catch { message.error('删除失败') }
}

onMounted(async () => {
  await fetchTree()
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
