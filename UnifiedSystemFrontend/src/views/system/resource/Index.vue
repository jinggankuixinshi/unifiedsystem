<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">权限管理</h2>
      <a-button type="primary" @click="openCreate(null)">
        <template #icon><PlusOutlined /></template>
        新建资源
      </a-button>
    </div>
    <a-card class="content-card">
      <a-spin :spinning="loading">
        <div ref="tableWrapRef">
          <a-table
            :columns="columns"
            :data-source="treeData"
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
              <template v-if="column.key === 'type'">
                <a-tag :color="typeColorMap[record.type]">
                  {{ typeMap[record.type] || record.type }}
                </a-tag>
              </template>
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 1 ? 'green' : 'red'">
                  {{ record.status === 1 ? '启用' : '禁用' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-space>
                  <a @click="openCreate(record)">添加子级</a>
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

    <a-modal v-model:open="modalVisible" :title="isEdit ? '编辑资源' : '新建资源'"
      @ok="handleSubmit" :confirm-loading="submitting" :maskClosable="false">
      <a-form ref="formRef" :model="formState" :rules="rules" :label-col="{ span: 5 }">
        <a-form-item label="上级资源">
          <a-tree-select
            v-model:value="formState.parentId"
            :tree-data="resourceOptions"
            :field-names="{ children: 'children', label: 'name', value: 'id' }"
            tree-default-expand-all
            allow-clear
            placeholder="不选则为根资源"
            style="width:100%"
          />
        </a-form-item>
        <a-form-item label="资源名称" name="name">
          <a-input v-model:value="formState.name" />
        </a-form-item>
        <a-form-item label="资源编码" name="code">
          <a-input v-model:value="formState.code" />
        </a-form-item>
        <a-form-item label="类型" name="type">
          <a-radio-group v-model:value="formState.type">
            <a-radio :value="1">菜单</a-radio>
            <a-radio :value="2">按钮</a-radio>
            <a-radio :value="3">接口</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="路由路径">
          <a-input v-model:value="formState.path" />
        </a-form-item>
        <a-form-item label="图标">
          <a-input v-model:value="formState.icon" />
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

const typeMap: Record<number, string> = { 1: '菜单', 2: '按钮', 3: '接口' }
const typeColorMap: Record<number, string> = { 1: 'blue', 2: 'orange', 3: 'purple' }

const loading = ref(false)
const treeData = ref<any[]>([])
const resourceOptions = ref<any[]>([])
const expandedKeys = ref<Array<string | number>>([])
const totalCount = ref(0)
const tableWrapRef = ref<HTMLElement>()
let sortable: Sortable | null = null

const columns = [
  { title: '', key: 'drag', width: 44, align: 'center' as const },
  { title: '资源名称', dataIndex: 'name' },
  { title: '编码', dataIndex: 'code' },
  { title: '类型', key: 'type', width: 80 },
  { title: '路由', dataIndex: 'path' },
  { title: '状态', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 180 }
]

const modalVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number>()
const formRef = ref()
const submitting = ref(false)
const statusChecked = ref(true)

const formState = reactive({
  parentId: null as number | null, name: '', code: '', type: 1,
  path: '', icon: ''
})
const rules = {
  name: [{ required: true, message: '请输入资源名称' }],
  code: [{ required: true, message: '请输入资源编码' }],
  type: [{ required: true, message: '请选择类型' }]
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
  walk(treeData.value, 1, null)
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
    const res = await request.get('/system/resources/tree') as any
    const tree = res.data || []
    totalCount.value = countNodes(tree)
    treeData.value = tree
    resourceOptions.value = tree
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
    await request.put('/system/resources/sort', payload)
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
  formState.name = ''; formState.code = ''; formState.type = 1
  formState.path = ''; formState.icon = ''
  statusChecked.value = true; modalVisible.value = true
}

function openEdit(r: any) {
  isEdit.value = true; editId.value = r.id
  formState.parentId = r.parentId || null; formState.name = r.name
  formState.code = r.code; formState.type = r.type
  formState.path = r.path || ''; formState.icon = r.icon || ''
  statusChecked.value = r.status === 1
  modalVisible.value = true
}

async function handleSubmit() {
  if (submitting.value) return
  try { await formRef.value?.validate() } catch { return }
  submitting.value = true
  try {
    const payload = { ...formState, status: statusChecked.value ? 1 : 0 }
    if (isEdit.value) {
      await request.put(`/system/resources/${editId.value}`, payload); message.success('更新成功')
    } else {
      await request.post('/system/resources', payload); message.success('创建成功')
    }
    modalVisible.value = false; fetchTree()
  } catch { message.error('操作失败') } finally { submitting.value = false }
}

async function handleDelete(id: number) {
  try { await request.delete(`/system/resources/${id}`); message.success('删除成功'); fetchTree() } catch { message.error('删除失败') }
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
