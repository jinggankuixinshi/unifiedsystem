<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">物流仓库管理</h2>
    </div>
    <a-card class="content-card">
      <a-tabs v-model:activeKey="activeTab" @change="onTabChange">
        <a-tab-pane key="stock" tab="库存列表" />
        <a-tab-pane key="inbound" tab="入库" />
        <a-tab-pane key="outbound" tab="出库" />
        <a-tab-pane key="quality" tab="质检" />
      </a-tabs>

      <a-spin :spinning="loading">
        <!-- 库存列表 -->
        <a-table v-if="activeTab === 'stock'" :columns="stockColumns" :data-source="stockData" row-key="id"
          :pagination="{ current, pageSize, total, showSizeChanger: true, pageSizeOptions: ['10','20','50','100','200'] }"
          @change="handleTableChange" size="small">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'quantity'">
              <span :style="{ color: record.quantity <= (record.safetyStock || 0) ? '#cf1322' : '' }">
                {{ record.quantity }}
              </span>
            </template>
            <template v-if="column.key === 'inboundDate'">
              {{ record.inboundDate ? new Date(record.inboundDate).toLocaleDateString('zh-CN') : '-' }}
            </template>
            <template v-if="column.key === 'productionDate'">
              {{ record.productionDate ? new Date(record.productionDate).toLocaleDateString('zh-CN') : '-' }}
            </template>
          </template>
        </a-table>

        <!-- 入库表单 -->
        <a-form v-if="activeTab === 'inbound'" :model="inboundForm" :label-col="{ span: 4 }" style="max-width:600px">
          <a-form-item label="入库类型">
            <a-radio-group v-model:value="inboundForm.inboundType">
              <a-radio value="transfer">调拨入库</a-radio>
              <a-radio value="production">生产入库</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item label="产品">
            <a-select v-model:value="inboundForm.productId" :options="productOptions" placeholder="选择产品" style="width:100%" />
          </a-form-item>
          <a-form-item label="数量">
            <a-input-number v-model:value="inboundForm.quantity" :min="1" style="width:100%" />
          </a-form-item>
          <a-form-item label="批次号">
            <a-input v-model:value="inboundForm.batchNo" />
          </a-form-item>
          <a-form-item label="库位编码">
            <a-input v-model:value="inboundForm.locationCode" />
          </a-form-item>
          <a-form-item :wrapper-col="{ offset: 4 }">
            <a-button type="primary" :loading="submitting" @click="handleInbound">确认入库</a-button>
          </a-form-item>
        </a-form>

        <!-- 出库表单 -->
        <a-form v-if="activeTab === 'outbound'" :model="outboundForm" :label-col="{ span: 4 }" style="max-width:600px">
          <a-form-item label="出库类型">
            <a-radio-group v-model:value="outboundForm.outboundType">
              <a-radio value="sales">销售出库</a-radio>
              <a-radio value="transfer">调拨出库</a-radio>
              <a-radio value="scrap">报废出库</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item label="选择库存">
            <a-select v-model:value="outboundForm.warehouseId" :options="stockSelectOptions" placeholder="选择库存记录" style="width:100%" />
          </a-form-item>
          <a-form-item label="数量">
            <a-input-number v-model:value="outboundForm.quantity" :min="1" style="width:100%" />
          </a-form-item>
          <a-form-item :wrapper-col="{ offset: 4 }">
            <a-button type="primary" :loading="submitting" @click="handleOutbound">确认出库</a-button>
          </a-form-item>
        </a-form>

        <!-- 质检表单 -->
        <a-form v-if="activeTab === 'quality'" :model="qualityForm" :label-col="{ span: 4 }" style="max-width:600px">
          <a-form-item label="检验类型">
            <a-radio-group v-model:value="qualityForm.checkType">
              <a-radio value="inbound">入库质检</a-radio>
              <a-radio value="outbound">出库质检</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item label="选择库存">
            <a-select v-model:value="qualityForm.warehouseId" :options="stockSelectOptions" placeholder="选择库存记录" style="width:100%" />
          </a-form-item>
          <a-form-item label="AQL标准">
            <a-input v-model:value="qualityForm.aqlStandard" placeholder="如: 1.0, 2.5" />
          </a-form-item>
          <a-form-item label="抽样数量">
            <a-input-number v-model:value="qualityForm.sampleQuantity" :min="1" style="width:100%" />
          </a-form-item>
          <a-form-item label="合格数量">
            <a-input-number v-model:value="qualityForm.qualifiedQuantity" :min="0" style="width:100%" />
          </a-form-item>
          <a-form-item label="不合格数量">
            <a-input-number v-model:value="qualityForm.unqualifiedQuantity" :min="0" style="width:100%" />
          </a-form-item>
          <a-form-item label="备注">
            <a-textarea v-model:value="qualityForm.remark" :rows="2" />
          </a-form-item>
          <a-form-item :wrapper-col="{ offset: 4 }">
            <a-button type="primary" :loading="submitting" @click="handleQualityCheck">提交质检</a-button>
          </a-form-item>
        </a-form>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import request from '@/api/request'

const activeTab = ref('stock')
const loading = ref(false)
const submitting = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const stockData = ref<any[]>([])

const stockColumns = [
  { title: '产品ID', dataIndex: 'productId', width: 80 },
  { title: '批次号', dataIndex: 'batchNo', width: 140 },
  { title: '库存数量', key: 'quantity', width: 100 },
  { title: '库位编码', dataIndex: 'locationCode', width: 120 },
  { title: '入库日期', key: 'inboundDate', width: 120 },
  { title: '生产日期', key: 'productionDate', width: 120 }
]

const productOptions = ref<Array<{ label: string; value: number }>>([])
const stockSelectOptions = ref<Array<{ label: string; value: number }>>([])

const inboundForm = reactive({ inboundType: 'transfer', productId: undefined as number | undefined, quantity: undefined as number | undefined, batchNo: '', locationCode: '' })
const outboundForm = reactive({ outboundType: 'sales', warehouseId: undefined as number | undefined, quantity: undefined as number | undefined })
const qualityForm = reactive({ checkType: 'inbound', warehouseId: undefined as number | undefined, aqlStandard: '', sampleQuantity: undefined as number | undefined, qualifiedQuantity: undefined as number | undefined, unqualifiedQuantity: undefined as number | undefined, remark: '' })

async function fetchStock() {
  loading.value = true
  try {
    const res = await request.get('/logistics/warehouse', { pageNum: current.value, pageSize: pageSize.value }) as any
    stockData.value = res.data?.records || []
    total.value = res.data?.total || 0
    stockSelectOptions.value = stockData.value.map((s: any) => ({
      label: `${s.productId || '?'} - ${s.batchNo || '-'} (${s.quantity || 0})`,
      value: s.id
    }))
  } catch { } finally { loading.value = false }
}

async function fetchProducts() {
  try {
    const res = await request.get('/production/products', { pageNum: 1, pageSize: 200 }) as any
    const list = res.data?.records || []
    productOptions.value = list.map((p: any) => ({ label: p.productName || p.materialName, value: p.id }))
  } catch { }
}

function onTabChange() {
  if (activeTab.value === 'stock') fetchStock()
}

function handleTableChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchStock() }

async function handleInbound() {
  if (!inboundForm.productId) { message.warning('请选择产品'); return }
  if (!inboundForm.quantity || inboundForm.quantity <= 0) { message.warning('请输入有效数量'); return }
  submitting.value = true
  try {
    await request.post('/logistics/warehouse/inbound', inboundForm)
    message.success('入库成功')
    inboundForm.productId = undefined; inboundForm.quantity = undefined; inboundForm.batchNo = ''; inboundForm.locationCode = ''
    activeTab.value = 'stock'; fetchStock()
  } catch { } finally { submitting.value = false }
}

async function handleOutbound() {
  if (!outboundForm.warehouseId) { message.warning('请选择库存记录'); return }
  if (!outboundForm.quantity || outboundForm.quantity <= 0) { message.warning('请输入有效数量'); return }
  submitting.value = true
  try {
    await request.post('/logistics/warehouse/outbound', outboundForm)
    message.success('出库成功')
    outboundForm.warehouseId = undefined; outboundForm.quantity = undefined
    activeTab.value = 'stock'; fetchStock()
  } catch { } finally { submitting.value = false }
}

async function handleQualityCheck() {
  if (!qualityForm.warehouseId) { message.warning('请选择库存记录'); return }
  submitting.value = true
  try {
    await request.post('/logistics/warehouse/quality-check', qualityForm)
    message.success('质检提交成功')
    Object.assign(qualityForm, { checkType: 'inbound', warehouseId: undefined, aqlStandard: '', sampleQuantity: undefined, qualifiedQuantity: undefined, unqualifiedQuantity: undefined, remark: '' })
    activeTab.value = 'stock'; fetchStock()
  } catch { } finally { submitting.value = false }
}

onMounted(() => { fetchStock(); fetchProducts() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
