<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">审计日志</h2>
      <a-input-search
        v-model:value="keyword"
        placeholder="搜索操作人/模块/操作"
        style="width: 280px"
        @search="handleSearch"
      />
    </div>
    <a-card class="content-card">
      <a-spin :spinning="loading">
        <a-empty v-if="!loading && dataSource.length === 0" description="暂无审计日志" />
        <a-table
          v-else
          :columns="columns"
          :data-source="dataSource"
          row-key="id"
          :pagination="pagination"
          @change="handlePageChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'operation'">
              <a-tag :color="record.operation === 'DELETE' ? 'red' : record.operation === 'UPDATE' ? 'blue' : 'green'">
                {{ record.operation }}
              </a-tag>
            </template>
            <template v-if="column.key === 'detail'">
              <a-tooltip :title="record.detail">
                <span>{{ (record.detail || '').substring(0, 60) }}{{ (record.detail || '').length > 60 ? '...' : '' }}</span>
              </a-tooltip>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getAuditLogs } from '@/api/modules/system'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const keyword = ref('')
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dataSource = ref<any[]>([])

const columns = [
  { title: '操作人', dataIndex: 'realName', width: 100 },
  { title: '账号', dataIndex: 'username', width: 120 },
  { title: '模块', dataIndex: 'module', width: 100 },
  { title: '操作', key: 'operation', width: 80 },
  { title: '详情', key: 'detail', ellipsis: true },
  { title: 'IP', dataIndex: 'ip', width: 140 },
  { title: '操作时间', dataIndex: 'createTime', width: 180 }
]

const pagination = computed(() => ({
  current: current.value, pageSize: pageSize.value, total: total.value,
  showSizeChanger: true, pageSizeOptions: ['10', '20', '50'],
  showTotal: (t: number) => `共 ${t} 条`
}))

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getAuditLogs({ pageNum: current.value, pageSize: pageSize.value, keyword: keyword.value })
    dataSource.value = (res.data?.records || []).map((r: any) => ({ ...r, createTime: formatDateTime(r.createTime) }))
    total.value = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

function handleSearch() { current.value = 1; fetchData() }
function handlePageChange(pag: any) { current.value = pag.current; pageSize.value = pag.pageSize; fetchData() }

onMounted(fetchData)
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
