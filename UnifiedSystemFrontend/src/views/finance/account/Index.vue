<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">账户与科目</h2>
    </div>
    <a-card class="content-card">
      <a-tabs v-model:activeKey="activeTab" @change="onTabChange">
        <a-tab-pane key="account" tab="账户管理" />
        <a-tab-pane key="subject" tab="会计科目" />
      </a-tabs>

      <a-spin :spinning="loading">
        <!-- 账户管理 -->
        <template v-if="activeTab === 'account'">
          <div style="margin-bottom: 16px">
            <a-button type="primary" @click="openAccountCreate">
              <template #icon><PlusOutlined /></template>
              新增账户
            </a-button>
          </div>
          <a-table
            :columns="accountColumns"
            :data-source="accountData"
            row-key="id"
            :pagination="accountPagination"
            @change="handleAccountPageChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'balance'">
                <span style="font-weight:600">¥{{ Number(record.balance || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</span>
              </template>
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 1 ? 'green' : 'red'">
                  {{ record.status === 1 ? '启用' : '停用' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-space size="small">
                  <a @click="openAccountEdit(record)">编辑</a>
                  <a-popconfirm title="确认删除？" @confirm="deleteAccount(record.id)">
                    <a class="danger-link">删除</a>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </template>

        <!-- 会计科目 -->
        <template v-if="activeTab === 'subject'">
          <a-table
            :columns="subjectColumns"
            :data-source="subjectData"
            row-key="id"
            :pagination="false"
            :defaultExpandAllRows="false"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'type'">
                <a-tag :color="{ '资产': 'blue', '负债': 'orange', '权益': 'purple', '收入': 'green', '费用': 'red' }[record.type] || 'default'">
                  {{ record.type || '-' }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </template>
      </a-spin>
    </a-card>

    <!-- 账户弹窗 -->
    <a-modal v-model:open="accountModalVisible" :title="accountModalTitle" @ok="submitAccount" :confirm-loading="submitting" :maskClosable="false">
      <a-form :label-col="{ span: 6 }">
        <a-form-item label="账户名称">
          <a-input v-model:value="accountForm.accountName" />
        </a-form-item>
        <a-form-item label="账号">
          <a-input v-model:value="accountForm.accountNo" />
        </a-form-item>
        <a-form-item label="开户行">
          <a-input v-model:value="accountForm.bankName" />
        </a-form-item>
        <a-form-item label="账户类型">
          <a-select v-model:value="accountForm.accountType" style="width:100%">
            <a-select-option value="现金">现金</a-select-option>
            <a-select-option value="银行存款">银行存款</a-select-option>
            <a-select-option value="支付宝">支付宝</a-select-option>
            <a-select-option value="微信">微信</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="余额">
          <a-input-number v-model:value="accountForm.balance" :min="0" :precision="2" style="width:100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model:checked="accountForm.statusCheck" checked-children="启用" un-checked-children="停用" />
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

const activeTab = ref('account')
const loading = ref(false)
const submitting = ref(false)

const accountColumns = [
  { title: '账户名称', dataIndex: 'accountName', key: 'accountName' },
  { title: '账号', dataIndex: 'accountNo', key: 'accountNo' },
  { title: '开户行', dataIndex: 'bankName', key: 'bankName' },
  { title: '账户类型', dataIndex: 'accountType', key: 'accountType', width: 100 },
  { title: '余额', key: 'balance', width: 150 },
  { title: '状态', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 120 }
]

const subjectColumns = [
  { title: '科目编码', dataIndex: 'subjectCode', key: 'subjectCode', width: 120 },
  { title: '科目名称', dataIndex: 'subjectName', key: 'subjectName' },
  { title: '类型', key: 'type', width: 80 },
  { title: '级别', dataIndex: 'level', key: 'level', width: 60 }
]

const accountData = ref<any[]>([])
const subjectData = ref<any[]>([])

const accountPage = ref({ current: 1, pageSize: 10, total: 0 })

const accountPagination = computed(() => ({
  current: accountPage.value.current, pageSize: accountPage.value.pageSize, total: accountPage.value.total,
  showSizeChanger: true, pageSizeOptions: ['10', '20', '50', '100', '200'],
  showTotal: (t: number) => `共 ${t} 条`
}))

const accountModalVisible = ref(false)
const isAccountEdit = ref(false)
const editAccountId = ref<number>()
const accountForm = reactive({ accountName: '', accountNo: '', bankName: '', accountType: '银行存款', balance: 0, statusCheck: true })

const accountModalTitle = computed(() => isAccountEdit.value ? '编辑账户' : '新增账户')

async function fetchAccounts() {
  loading.value = true
  try {
    const res = await request.get('/finance/accounts', {
      pageNum: accountPage.value.current, pageSize: accountPage.value.pageSize
    }) as any
    accountData.value = res.data?.records || []
    accountPage.value.total = res.data?.total || 0
  } catch { message.error('加载账户失败') } finally { loading.value = false }
}

async function fetchSubjects() {
  loading.value = true
  try {
    const res = await request.get('/finance/subjects') as any
    const list = res.data || []
    const buildTree = (items: any[], parentId = 0): any[] => {
      return items
        .filter((i: any) => i.parentId === parentId)
        .map((i: any) => ({ ...i, children: buildTree(items, i.id) }))
    }
    subjectData.value = buildTree(list)
  } catch { message.error('加载会计科目失败') } finally { loading.value = false }
}

function onTabChange() {
  if (activeTab.value === 'account') fetchAccounts()
  else if (activeTab.value === 'subject') fetchSubjects()
}

function handleAccountPageChange(pag: any) { accountPage.value.current = pag.current; accountPage.value.pageSize = pag.pageSize; fetchAccounts() }

function openAccountCreate() {
  isAccountEdit.value = false
  accountForm.accountName = ''; accountForm.accountNo = ''; accountForm.bankName = ''
  accountForm.accountType = '银行存款'; accountForm.balance = 0; accountForm.statusCheck = true
  accountModalVisible.value = true
}

function openAccountEdit(record: any) {
  isAccountEdit.value = true; editAccountId.value = record.id
  accountForm.accountName = record.accountName; accountForm.accountNo = record.accountNo
  accountForm.bankName = record.bankName; accountForm.accountType = record.accountType
  accountForm.balance = record.balance || 0; accountForm.statusCheck = record.status === 1
  accountModalVisible.value = true
}

async function submitAccount() {
  submitting.value = true
  try {
    const params = { ...accountForm, status: accountForm.statusCheck ? 1 : 0 }
    delete (params as any).statusCheck
    if (isAccountEdit.value) {
      await request.put(`/finance/accounts/${editAccountId.value}`, params)
      message.success('更新成功')
    } else {
      await request.post('/finance/accounts', params)
      message.success('创建成功')
    }
    accountModalVisible.value = false
    fetchAccounts()
  } catch { message.error('操作失败') } finally { submitting.value = false }
}

async function deleteAccount(id: number) {
  try {
    await request.delete(`/finance/accounts/${id}`)
    message.success('删除成功')
    fetchAccounts()
  } catch { message.error('删除失败') }
}

onMounted(() => { fetchAccounts() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
</style>
