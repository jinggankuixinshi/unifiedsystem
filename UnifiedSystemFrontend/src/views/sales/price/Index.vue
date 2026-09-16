<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">价格管理</h2>
      <a-space>
        <a-radio-group v-model:value="viewMode" button-style="solid" size="small">
          <a-radio-button value="price">产品价格</a-radio-button>
          <a-radio-button value="config">异常配置</a-radio-button>
        </a-radio-group>
        <a-button v-if="viewMode === 'price'" type="primary" @click="openCreatePrice">
          <template #icon><PlusOutlined /></template>
          新增价格
        </a-button>
      </a-space>
    </div>
    <a-card class="content-card">
      <a-spin :spinning="loading">
        <template v-if="viewMode === 'price'">
          <a-empty v-if="!loading && priceData.length === 0" description="暂无价格数据" />
          <a-table v-else :columns="priceColumns" :data-source="priceData" row-key="id"
            :pagination="pagination" @change="handlePageChange" />
        </template>
        <template v-else>
          <a-form :label-col="{ span: 6 }" style="max-width: 500px">
            <a-form-item v-for="cfg in anomalyConfigs" :key="cfg.level" :label="cfg.label">
              <a-input-number v-model:value="cfg.threshold" :min="0" :max="1" :step="0.01" :precision="2"
                addon-after="%" style="width: 200px" />
            </a-form-item>
            <a-form-item><a-button type="primary" :loading="savingConfig" @click="saveAnomalyConfig">保存配置</a-button></a-form-item>
          </a-form>
        </template>
      </a-spin>
    </a-card>

    <a-modal v-model:open="priceModalVisible" title="新增产品价格" @ok="handlePriceSubmit" :confirm-loading="submitting">
      <a-form :label-col="{ span: 6 }">
        <a-form-item label="产品编码">
          <a-input v-model:value="priceFormState.productId" placeholder="输入产品ID" />
        </a-form-item>
        <a-form-item label="售价">
          <a-input-number v-model:value="priceFormState.price" :min="0" :precision="2" style="width:100%" />
        </a-form-item>
        <a-form-item label="生效日期">
          <a-date-picker v-model:value="priceFormState.date" style="width:100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { getProductPrices, createProductPrice, getAnomalyConfigs, updateAnomalyConfig } from '@/api/modules/sales'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'

const viewMode = ref<'price' | 'config'>('price')
const loading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const priceData = ref<any[]>([])

const priceColumns = [
  { title: '产品ID', dataIndex: 'productId' },
  { title: '售价', dataIndex: 'price' },
  { title: '生效日期', dataIndex: 'effectiveDate', width: 140 },
  { title: '来源', dataIndex: 'source', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 }
]

const pagination = computed(() => ({
  current: current.value, pageSize: pageSize.value, total: total.value,
  showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条`
}))

const anomalyConfigs = reactive([
  { level: 'mild', label: '轻度异常阈值（<85%）', threshold: 0.15 },
  { level: 'moderate', label: '中度异常阈值（<70%）', threshold: 0.30 },
  { level: 'severe', label: '重度异常阈值（<50%）', threshold: 0.50 },
])
const savingConfig = ref(false)

const priceModalVisible = ref(false)
const submitting = ref(false)
const priceFormState = reactive({ productId: '', price: 0, date: null as Dayjs | null })

async function fetchPrices() {
  loading.value = true
  try {
    const res: any = await getProductPrices({ pageNum: current.value, pageSize: pageSize.value })
    priceData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

async function fetchConfig() {
  try {
    const res: any = await getAnomalyConfigs()
    const configs = res.data
    if (Array.isArray(configs)) {
      for (const cfg of anomalyConfigs) {
        const match = configs.find((c: any) => c.level === cfg.level)
        if (match) cfg.threshold = match.threshold
      }
    }
  } catch { /* ignore */ }
}

function openCreatePrice() {
  priceFormState.productId = ''; priceFormState.price = 0; priceFormState.date = dayjs()
  priceModalVisible.value = true
}

async function handlePriceSubmit() {
  if (!priceFormState.productId) { message.warning('请输入产品ID'); return }
  submitting.value = true
  try {
    await createProductPrice({
      productId: parseInt(priceFormState.productId),
      price: priceFormState.price,
      effectiveDate: priceFormState.date?.format('YYYY-MM-DD')
    })
    message.success('新增成功'); priceModalVisible.value = false; fetchPrices()
  } catch { } finally { submitting.value = false }
}

async function saveAnomalyConfig() {
  savingConfig.value = true
  try {
    await updateAnomalyConfig(anomalyConfigs.map(c => ({ level: c.level, threshold: c.threshold })))
    message.success('保存成功')
  } catch { } finally { savingConfig.value = false }
}

function handlePageChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchPrices() }

onMounted(() => { fetchPrices(); fetchConfig() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
