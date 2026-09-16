<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">产品与BOM管理</h2>
    </div>
    <a-card class="content-card">
      <a-tabs v-model:activeKey="activeTab" @change="onTabChange">
        <a-tab-pane key="product" tab="产品管理" />
        <a-tab-pane key="bom" tab="BOM管理" />
        <a-tab-pane key="material" tab="物料管理" />
      </a-tabs>

      <a-spin :spinning="loading">
        <!-- 产品管理 -->
        <template v-if="activeTab === 'product'">
          <div style="margin-bottom: 16px">
            <a-button type="primary" @click="openProductCreate">
              <template #icon><PlusOutlined /></template>
              新增产品
            </a-button>
          </div>
          <a-table
            :columns="productColumns"
            :data-source="productData"
            row-key="id"
            :pagination="productPagination"
            @change="handleProductPageChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'basePrice'">
                ¥{{ Number(record.basePrice).toFixed(2) }}
              </template>
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 1 ? 'green' : 'red'">
                  {{ record.status === 1 ? '启用' : '停用' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-space size="small">
                  <a @click="openProductEdit(record)">编辑</a>
                  <a-popconfirm title="确认删除？" @confirm="deleteProduct(record.id)">
                    <a class="danger-link">删除</a>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </template>

        <!-- BOM管理 -->
        <template v-if="activeTab === 'bom'">
          <div style="margin-bottom: 16px">
            <a-select
              v-model:value="selectedProductId"
              :options="productOptions"
              placeholder="请先选择产品"
              style="width: 300px"
              @change="fetchBom"
            />
          </div>
          <a-empty v-if="!selectedProductId" description="请选择产品查看BOM清单" />
          <template v-else>
            <a-table
              :columns="bomColumns"
              :data-source="bomData"
              row-key="id"
              :pagination="false"
              size="small"
              :expandRowByClick="true"
            >
              <template #expandedRowRender="{ record }">
                <a-table
                  :columns="bomItemColumns"
                  :data-source="record.items || []"
                  row-key="id"
                  :pagination="false"
                  size="small"
                >
                  <template #bodyCell="{ column, record: item }">
                    <template v-if="column.key === 'quantity'">
                      {{ item.quantity }} {{ item.unit || '-' }}
                    </template>
                  </template>
                </a-table>
              </template>
            </a-table>
          </template>
        </template>

        <!-- 物料管理 -->
        <template v-if="activeTab === 'material'">
          <div style="margin-bottom: 16px">
            <a-button type="primary" @click="openMaterialCreate">
              <template #icon><PlusOutlined /></template>
              新增物料
            </a-button>
          </div>
          <a-table
            :columns="materialColumns"
            :data-source="materialData"
            row-key="id"
            :pagination="materialPagination"
            @change="handleMaterialPageChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'action'">
                <a-space size="small">
                  <a @click="openMaterialEdit(record)">编辑</a>
                  <a-popconfirm title="确认删除？" @confirm="deleteMaterial(record.id)">
                    <a class="danger-link">删除</a>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </template>
      </a-spin>
    </a-card>

    <!-- 产品弹窗 -->
    <a-modal v-model:open="productModalVisible" :title="productModalTitle" @ok="submitProduct" :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 6 }">
        <a-form-item label="产品编码">
          <a-input v-model:value="productForm.productCode" />
        </a-form-item>
        <a-form-item label="产品名称">
          <a-input v-model:value="productForm.productName" />
        </a-form-item>
        <a-form-item label="规格">
          <a-input v-model:value="productForm.specification" />
        </a-form-item>
        <a-form-item label="单位">
          <a-input v-model:value="productForm.unit" />
        </a-form-item>
        <a-form-item label="基础价格">
          <a-input-number v-model:value="productForm.basePrice" :min="0" :precision="2" style="width:100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model:checked="productForm.statusCheck" checked-children="启用" un-checked-children="停用" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 物料弹窗 -->
    <a-modal v-model:open="materialModalVisible" :title="materialModalTitle" @ok="submitMaterial" :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 6 }">
        <a-form-item label="物料编码">
          <a-input v-model:value="materialForm.materialCode" />
        </a-form-item>
        <a-form-item label="物料名称">
          <a-input v-model:value="materialForm.materialName" />
        </a-form-item>
        <a-form-item label="规格">
          <a-input v-model:value="materialForm.specification" />
        </a-form-item>
        <a-form-item label="单位">
          <a-input v-model:value="materialForm.unit" />
        </a-form-item>
        <a-form-item label="安全库存">
          <a-input-number v-model:value="materialForm.safetyStock" :min="0" style="width:100%" />
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

const activeTab = ref('product')
const loading = ref(false)
const submitting = ref(false)

const productColumns = [
  { title: '产品编码', dataIndex: 'productCode', key: 'productCode' },
  { title: '产品名称', dataIndex: 'productName', key: 'productName' },
  { title: '规格', dataIndex: 'specification', key: 'specification' },
  { title: '单位', dataIndex: 'unit', key: 'unit', width: 60 },
  { title: '基础价格', key: 'basePrice', width: 120 },
  { title: '状态', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 120 }
]

const bomColumns = [
  { title: '版本', dataIndex: 'version', key: 'version' },
  { title: '生效日期', dataIndex: 'effectiveDate', key: 'effectiveDate', width: 130 },
  { title: '产品名称', dataIndex: 'productName', key: 'productName' },
  { title: '物料数量', key: 'itemCount', width: 80 }
]

const bomItemColumns = [
  { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode' },
  { title: '物料名称', dataIndex: 'materialName', key: 'materialName' },
  { title: '用量', key: 'quantity', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' }
]

const materialColumns = [
  { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode' },
  { title: '物料名称', dataIndex: 'materialName', key: 'materialName' },
  { title: '规格', dataIndex: 'specification', key: 'specification' },
  { title: '单位', dataIndex: 'unit', key: 'unit', width: 60 },
  { title: '安全库存', dataIndex: 'safetyStock', key: 'safetyStock', width: 100 },
  { title: '操作', key: 'action', width: 120 }
]

const productData = ref<any[]>([])
const bomData = ref<any[]>([])
const materialData = ref<any[]>([])
const selectedProductId = ref<number>()
const productOptions = ref<Array<{ label: string; value: number }>>([])

const productPage = ref({ current: 1, pageSize: 10, total: 0 })
const materialPage = ref({ current: 1, pageSize: 10, total: 0 })

const pagination = (page: any) => computed(() => ({
  current: page.value.current, pageSize: page.value.pageSize, total: page.value.total,
  showSizeChanger: true, pageSizeOptions: ['10', '20', '50', '100', '200'],
  showTotal: (t: number) => `共 ${t} 条`
}))

const productPagination = pagination(productPage)
const materialPagination = pagination(materialPage)

const productModalVisible = ref(false)
const isProductEdit = ref(false)
const editProductId = ref<number>()
const productForm = reactive({ productCode: '', productName: '', specification: '', unit: '', basePrice: 0, statusCheck: true })

const materialModalVisible = ref(false)
const isMaterialEdit = ref(false)
const editMaterialId = ref<number>()
const materialForm = reactive({ materialCode: '', materialName: '', specification: '', unit: '', safetyStock: 0 })

const productModalTitle = computed(() => isProductEdit.value ? '编辑产品' : '新增产品')
const materialModalTitle = computed(() => isMaterialEdit.value ? '编辑物料' : '新增物料')

async function fetchProducts() {
  loading.value = true
  try {
    const res = await request.get('/production/products', {
      params: { pageNum: productPage.value.current, pageSize: productPage.value.pageSize }
    }) as any
    productData.value = res.data?.records || []
    productPage.value.total = res.data?.total || 0
    productOptions.value = (res.data?.records || []).map((p: any) => ({ label: p.productName, value: p.id }))
  } catch { } finally { loading.value = false }
}

async function fetchBom() {
  if (!selectedProductId.value) return
  loading.value = true
  try {
    const bomRes = await request.get(`/production/bom/product/${selectedProductId.value}`) as any
    const bom = bomRes.data
    if (bom && bom.id) {
      const itemsRes = await request.get(`/production/bom/${bom.id}/items`) as any
      bom.items = itemsRes.data || []
      bom.itemCount = bom.items.length
      bomData.value = [bom]
    } else {
      bomData.value = []
    }
  } catch { } finally { loading.value = false }
}

async function fetchMaterials() {
  loading.value = true
  try {
    const res = await request.get('/production/materials', {
      params: { pageNum: materialPage.value.current, pageSize: materialPage.value.pageSize }
    }) as any
    materialData.value = res.data?.records || []
    materialPage.value.total = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

function onTabChange() {
  if (activeTab.value === 'product') fetchProducts()
  else if (activeTab.value === 'bom') { bomData.value = []; selectedProductId.value = undefined; fetchProducts() }
  else if (activeTab.value === 'material') fetchMaterials()
}

function handleProductPageChange(pag: any) { productPage.value.current = pag.current; productPage.value.pageSize = pag.pageSize; fetchProducts() }
function handleMaterialPageChange(pag: any) { materialPage.value.current = pag.current; materialPage.value.pageSize = pag.pageSize; fetchMaterials() }

function openProductCreate() {
  isProductEdit.value = false
  productForm.productCode = ''; productForm.productName = ''; productForm.specification = ''; productForm.unit = ''
  productForm.basePrice = 0; productForm.statusCheck = true
  productModalVisible.value = true
}

function openProductEdit(record: any) {
  isProductEdit.value = true; editProductId.value = record.id
  productForm.productCode = record.productCode; productForm.productName = record.productName
  productForm.specification = record.specification; productForm.unit = record.unit
  productForm.basePrice = record.basePrice; productForm.statusCheck = record.status === 1
  productModalVisible.value = true
}

async function submitProduct() {
  submitting.value = true
  try {
    const params = { ...productForm, status: productForm.statusCheck ? 1 : 0 }
    delete (params as any).statusCheck
    if (isProductEdit.value) {
      await request.put(`/production/products/${editProductId.value}`, params)
      message.success('更新成功')
    } else {
      await request.post('/production/products', params)
      message.success('创建成功')
    }
    productModalVisible.value = false
    fetchProducts()
  } catch { } finally { submitting.value = false }
}

async function deleteProduct(id: number) {
  try {
    await request.delete(`/production/products/${id}`)
    message.success('删除成功')
    fetchProducts()
  } catch { }
}

function openMaterialCreate() {
  isMaterialEdit.value = false
  materialForm.materialCode = ''; materialForm.materialName = ''; materialForm.specification = ''; materialForm.unit = ''; materialForm.safetyStock = 0
  materialModalVisible.value = true
}

function openMaterialEdit(record: any) {
  isMaterialEdit.value = true; editMaterialId.value = record.id
  materialForm.materialCode = record.materialCode; materialForm.materialName = record.materialName
  materialForm.specification = record.specification; materialForm.unit = record.unit; materialForm.safetyStock = record.safetyStock || 0
  materialModalVisible.value = true
}

async function submitMaterial() {
  submitting.value = true
  try {
    if (isMaterialEdit.value) {
      await request.put(`/production/materials/${editMaterialId.value}`, materialForm)
      message.success('更新成功')
    } else {
      await request.post('/production/materials', materialForm)
      message.success('创建成功')
    }
    materialModalVisible.value = false
    fetchMaterials()
  } catch { } finally { submitting.value = false }
}

async function deleteMaterial(id: number) {
  try {
    await request.delete(`/production/materials/${id}`)
    message.success('删除成功')
    fetchMaterials()
  } catch { }
}

onMounted(() => { fetchProducts() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
