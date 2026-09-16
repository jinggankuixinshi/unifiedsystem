<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">生产仓库</h2>
      <a-space>
        <a-button type="primary" @click="openInbound"><PlusOutlined /> 入库</a-button>
        <a-button @click="openOutbound"><MinusOutlined /> 出库</a-button>
      </a-space>
    </div>
    <a-card class="content-card">
      <a-spin :spinning="loading">
        <a-table :columns="columns" :data-source="dataSource" row-key="id" :pagination="false">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'quantity'">
              <span :style="{ color: record.quantity < 10 ? '#b44a4a' : '#1f2937' }">
                {{ record.quantity }}
              </span>
            </template>
            <template v-if="column.key === 'itemType'">
              <a-tag :color="record.itemType === 1 ? 'blue' : 'green'">
                {{ record.itemType === 1 ? '原料' : '成品' }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </a-spin>
      <div class="summary-bar">共 {{ dataSource.length }} 条记录</div>
    </a-card>

    <a-modal v-model:open="inboundVisible" title="生产入库" @ok="handleInbound"
      :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 5 }">
        <a-form-item label="入库类型">
          <a-radio-group v-model:value="inboundType">
            <a-radio value="purchase">采购入库</a-radio>
            <a-radio value="production">生产入库</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="物料/产品">
          <a-select v-model:value="inboundProductId" :options="productOptions" style="width:100%" />
        </a-form-item>
        <a-form-item label="数量">
          <a-input-number v-model:value="inboundQty" :min="0" style="width:100%" />
        </a-form-item>
        <a-form-item label="批次号">
          <a-input v-model:value="inboundBatch" />
        </a-form-item>
        <a-form-item label="库位">
          <a-input v-model:value="inboundLocation" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="outboundVisible" title="生产出库" @ok="handleOutbound"
      :confirm-loading="submittingOut" :maskClosable="false">
      <a-form :label-col="{ span: 5 }">
        <a-form-item label="出库类型">
          <a-radio-group v-model:value="outboundType">
            <a-radio value="production">生产领料</a-radio>
            <a-radio value="transfer">调拨出库</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="物料">
          <a-select v-model:value="outboundProductId" :options="productOptions" style="width:100%" />
        </a-form-item>
        <a-form-item label="数量">
          <a-input-number v-model:value="outboundQty" :min="0" style="width:100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { PlusOutlined, MinusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import request from '@/api/request'

const loading = ref(false)
const dataSource = ref<any[]>([])
const productOptions = ref<Array<{ label: string; value: number }>>([])

const columns = [
  { title: '物料/产品', dataIndex: 'materialProductId' },
  { title: '类型', key: 'itemType', width: 80 },
  { title: '批次号', dataIndex: 'batchNo' },
  { title: '库存数量', key: 'quantity' },
  { title: '库位', dataIndex: 'locationCode' },
  { title: '生产日期', dataIndex: 'productionDate' }
]

const inboundVisible = ref(false)
const outboundVisible = ref(false)
const submitting = ref(false)
const submittingOut = ref(false)
const inboundType = ref('production')
const inboundProductId = ref<number>()
const inboundQty = ref<number>()
const inboundBatch = ref('')
const inboundLocation = ref('')
const outboundType = ref('production')
const outboundProductId = ref<number>()
const outboundQty = ref<number>()

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/production/warehouse') as any
    dataSource.value = res.data || []
  } catch { } finally { loading.value = false }
}

async function fetchProducts() {
  try {
    const pRes = await request.get('/production/products') as any
    const list = pRes.data?.records || []
    productOptions.value = list.map((p: any) => ({ label: p.productName, value: p.id }))
  } catch { }
}

function openInbound() { inboundVisible.value = true }
function openOutbound() { outboundVisible.value = true }
async function handleInbound() {
  if (!inboundProductId.value || !inboundQty.value) { message.warning('请填写完整信息'); return }
  submitting.value = true
  try {
    await request.post('/production/warehouse/inbound', {
      inboundType: inboundType.value
    }, { params: { items: [{ materialProductId: inboundProductId.value, itemType: 2, quantity: inboundQty.value, batchNo: inboundBatch.value || Date.now().toString(), locationCode: inboundLocation.value || 'A-01' }] }})
    message.success('入库成功'); inboundVisible.value = false; fetchData()
  } catch { } finally { submitting.value = false }
}
async function handleOutbound() {
  if (!outboundProductId.value || !outboundQty.value) { message.warning('请填写完整信息'); return }
  submittingOut.value = true
  try {
    await request.post('/production/warehouse/outbound', {
      outboundType: outboundType.value
    }, { params: { items: [{ warehouseId: outboundProductId.value, quantity: outboundQty.value }] }})
    message.success('出库成功'); outboundVisible.value = false; fetchData()
  } catch { } finally { submittingOut.value = false }
}

onMounted(() => { fetchData(); fetchProducts() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
.summary-bar { margin-top: 12px; color: #9ca3af; font-size: 13px; }
</style>
