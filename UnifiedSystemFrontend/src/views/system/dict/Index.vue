<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">字典管理</h2>
    </div>
    <a-card class="content-card">
      <a-row :gutter="16">
        <!-- 左侧：字典类型 -->
        <a-col :span="7">
          <div style="margin-bottom: 12px; text-align: right">
            <a-button type="primary" size="small" @click="openTypeCreate">
              <template #icon><PlusOutlined /></template>
              新增字典类型
            </a-button>
          </div>
          <a-spin :spinning="typeLoading">
            <a-table
              :columns="typeColumns"
              :data-source="typeData"
              row-key="id"
              :pagination="typePagination"
              size="small"
              :row-class-name="(r: any) => selectedType?.id === r.id ? 'selected-row' : ''"
              @change="handleTypePageChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'status'">
                  <a-tag :color="record.status === '1' || record.status === 1 ? 'green' : 'red'">
                    {{ record.status === '1' || record.status === 1 ? '正常' : '停用' }}
                  </a-tag>
                </template>
                <template v-if="column.key === 'action'">
                  <a-space size="small">
                    <a @click="openTypeEdit(record)">编辑</a>
                    <a-popconfirm title="确认删除？" @confirm="deleteType(record.id)">
                      <a class="danger-link">删除</a>
                    </a-popconfirm>
                    <a @click="selectType(record)">数据</a>
                  </a-space>
                </template>
              </template>
            </a-table>
          </a-spin>
        </a-col>

        <!-- 右侧：字典数据 -->
        <a-col :span="17">
          <div style="margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center">
            <span v-if="selectedType" style="font-weight:600">{{ selectedType.dictName }} — 字典数据</span>
            <span v-else style="color:#999">请先选择左侧字典类型</span>
            <a-button v-if="selectedType" type="primary" size="small" @click="openDataCreate">
              <template #icon><PlusOutlined /></template>
              新增字典数据
            </a-button>
          </div>
          <a-spin :spinning="dataLoading">
            <a-empty v-if="!selectedType" description="请点击左侧字典类型的「数据」按钮查看" />
            <a-table
              v-else
              :columns="dataColumns"
              :data-source="dataSource"
              row-key="id"
              :pagination="dataPagination"
              size="small"
              @change="handleDataPageChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'status'">
                  <a-tag :color="record.status === '1' || record.status === 1 ? 'green' : 'red'">
                    {{ record.status === '1' || record.status === 1 ? '正常' : '停用' }}
                  </a-tag>
                </template>
                <template v-if="column.key === 'action'">
                  <a-space size="small">
                    <a @click="openDataEdit(record)">编辑</a>
                    <a-popconfirm title="确认删除？" @confirm="deleteData(record.id)">
                      <a class="danger-link">删除</a>
                    </a-popconfirm>
                  </a-space>
                </template>
              </template>
            </a-table>
          </a-spin>
        </a-col>
      </a-row>
    </a-card>

    <!-- 字典类型弹窗 -->
    <a-modal v-model:open="typeModalVisible" :title="typeModalTitle" @ok="submitType" :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 5 }">
        <a-form-item label="字典名称">
          <a-input v-model:value="typeForm.dictName" />
        </a-form-item>
        <a-form-item label="字典类型">
          <a-input v-model:value="typeForm.dictType" />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model:checked="typeForm.status" checked-children="正常" un-checked-children="停用" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="typeForm.remark" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 字典数据弹窗 -->
    <a-modal v-model:open="dataModalVisible" :title="dataModalTitle" @ok="submitData" :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 5 }">
        <a-form-item label="字典标签">
          <a-input v-model:value="dataForm.dictLabel" />
        </a-form-item>
        <a-form-item label="字典值">
          <a-input v-model:value="dataForm.dictValue" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="dataForm.sortOrder" :min="0" style="width:100%" />
        </a-form-item>
        <a-form-item label="CSS类名">
          <a-input v-model:value="dataForm.cssClass" />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model:checked="dataForm.status" checked-children="正常" un-checked-children="停用" />
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

const typeLoading = ref(false)
const dataLoading = ref(false)
const submitting = ref(false)

const typeColumns = [
  { title: '字典名称', dataIndex: 'dictName', key: 'dictName' },
  { title: '字典类型', dataIndex: 'dictType', key: 'dictType' },
  { title: '状态', key: 'status', width: 60 },
  { title: '操作', key: 'action', width: 120 }
]

const dataColumns = [
  { title: '字典标签', dataIndex: 'dictLabel', key: 'dictLabel' },
  { title: '字典值', dataIndex: 'dictValue', key: 'dictValue' },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 60 },
  { title: 'CSS类名', dataIndex: 'cssClass', key: 'cssClass' },
  { title: '状态', key: 'status', width: 60 },
  { title: '操作', key: 'action', width: 100 }
]

const typeData = ref<any[]>([])
const dataSource = ref<any[]>([])
const selectedType = ref<any>(null)

const typePage = ref({ current: 1, pageSize: 10, total: 0 })
const dataPage = ref({ current: 1, pageSize: 10, total: 0 })

const typePagination = computed(() => ({
  current: typePage.value.current, pageSize: typePage.value.pageSize, total: typePage.value.total,
  showSizeChanger: true, pageSizeOptions: ['10', '20', '50', '100', '200'],
  showTotal: (t: number) => `共 ${t} 条`
}))

const dataPagination = computed(() => ({
  current: dataPage.value.current, pageSize: dataPage.value.pageSize, total: dataPage.value.total,
  showSizeChanger: true, pageSizeOptions: ['10', '20', '50', '100', '200'],
  showTotal: (t: number) => `共 ${t} 条`
}))

const typeModalVisible = ref(false)
const isTypeEdit = ref(false)
const editTypeId = ref<number>()
const typeForm = reactive({ dictName: '', dictType: '', status: true, remark: '' })

const dataModalVisible = ref(false)
const isDataEdit = ref(false)
const editDataId = ref<number>()
const dataForm = reactive({ dictLabel: '', dictValue: '', sortOrder: 0, cssClass: '', status: true })

const typeModalTitle = computed(() => isTypeEdit.value ? '编辑字典类型' : '新增字典类型')
const dataModalTitle = computed(() => isDataEdit.value ? '编辑字典数据' : '新增字典数据')

async function fetchTypes() {
  typeLoading.value = true
  try {
    const res = await request.get('/system/dict/types') as any
    const list = res.data || []
    typeData.value = list
    typePage.value.total = list.length
  } catch { } finally { typeLoading.value = false }
}

async function fetchData() {
  if (!selectedType.value) return
  dataLoading.value = true
  try {
    const res = await request.get('/system/dict/data', {
      params: { type: selectedType.value.dictType }
    }) as any
    const list = res.data || []
    dataSource.value = list
    dataPage.value.total = list.length
  } catch { } finally { dataLoading.value = false }
}

function selectType(record: any) {
  selectedType.value = record
  dataPage.value.current = 1
  fetchData()
}

function handleTypePageChange(pag: any) { typePage.value.current = pag.current; typePage.value.pageSize = pag.pageSize; fetchTypes() }
function handleDataPageChange(pag: any) { dataPage.value.current = pag.current; dataPage.value.pageSize = pag.pageSize; fetchData() }

function openTypeCreate() {
  isTypeEdit.value = false
  typeForm.dictName = ''; typeForm.dictType = ''; typeForm.status = true; typeForm.remark = ''
  typeModalVisible.value = true
}

function openTypeEdit(record: any) {
  isTypeEdit.value = true; editTypeId.value = record.id
  typeForm.dictName = record.dictName; typeForm.dictType = record.dictType
  typeForm.status = record.status === '1' || record.status === 1; typeForm.remark = record.remark || ''
  typeModalVisible.value = true
}

async function submitType() {
  if (!typeForm.dictName || !typeForm.dictType) { message.warning('请填写完整信息'); return }
  submitting.value = true
  try {
    const params = { ...typeForm, status: typeForm.status ? '1' : '0' }
    if (isTypeEdit.value) {
      await request.put(`/system/dict/types/${editTypeId.value}`, params)
      message.success('更新成功')
    } else {
      await request.post('/system/dict/types', params)
      message.success('创建成功')
    }
    typeModalVisible.value = false
    fetchTypes()
  } catch { } finally { submitting.value = false }
}

async function deleteType(id: number) {
  try {
    await request.delete(`/system/dict/types/${id}`)
    message.success('删除成功')
    if (selectedType.value?.id === id) { selectedType.value = null; dataSource.value = [] }
    fetchTypes()
  } catch { }
}

function openDataCreate() {
  isDataEdit.value = false
  dataForm.dictLabel = ''; dataForm.dictValue = ''; dataForm.sortOrder = 0; dataForm.cssClass = ''; dataForm.status = true
  dataModalVisible.value = true
}

function openDataEdit(record: any) {
  isDataEdit.value = true; editDataId.value = record.id
  dataForm.dictLabel = record.dictLabel; dataForm.dictValue = record.dictValue
  dataForm.sortOrder = record.sortOrder || 0; dataForm.cssClass = record.cssClass || ''
  dataForm.status = record.status === '1' || record.status === 1
  dataModalVisible.value = true
}

async function submitData() {
  if (!dataForm.dictLabel || !dataForm.dictValue) { message.warning('请填写完整信息'); return }
  submitting.value = true
  try {
    const params = {
      ...dataForm,
      status: dataForm.status ? '1' : '0',
      dictType: selectedType.value.dictType
    }
    if (isDataEdit.value) {
      await request.put(`/system/dict/data/${editDataId.value}`, params)
      message.success('更新成功')
    } else {
      await request.post('/system/dict/data', params)
      message.success('创建成功')
    }
    dataModalVisible.value = false
    fetchData()
  } catch { } finally { submitting.value = false }
}

async function deleteData(id: number) {
  try {
    await request.delete(`/system/dict/data/${id}`)
    message.success('删除成功')
    fetchData()
  } catch { }
}

onMounted(() => { fetchTypes() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';

.selected-row {
  background-color: #e6f7ff;
}
</style>
