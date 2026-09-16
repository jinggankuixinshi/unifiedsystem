<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  DashboardOutlined,
  SettingOutlined,
  ToolOutlined,
  CarOutlined,
  ShoppingCartOutlined,
  DollarOutlined,
  BellOutlined,
  UserOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import { useMessageStore } from '@/stores/message'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const messageStore = useMessageStore()

const collapsed = ref(false)
const selectedKeys = ref<string[]>([route.path])
const openKeys = ref<string[]>([`/${route.path.split('/')[1]}`])
let ws: WebSocket | null = null

function connectWebSocket() {
  const token = localStorage.getItem('token')
  if (!token) return
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  ws = new WebSocket(`${protocol}//${window.location.host}/ws/notification?token=${token}`)
  ws.onmessage = (e) => {
    try {
      const data = JSON.parse(e.data)
      if (data.type === 'new_message') {
        messageStore.incrementUnread()
      } else if (data.type === 'kicked') {
        userStore.setToken('')
        router.push('/login')
      }
    } catch {}
  }
  ws.onopen = () => { messageStore.wsConnected = true }
  ws.onclose = () => { messageStore.wsConnected = false }
}

function navigate(key: string) {
  router.push(key)
}

async function handleLogout() {
  await userStore.logout()
  ws?.close()
  router.push('/login')
}

onMounted(() => {
  connectWebSocket()
  messageStore.fetchUnreadCount()
})

onUnmounted(() => {
  ws?.close()
})
</script>

<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider v-model:collapsed="collapsed" collapsible>
      <div class="logo">
        <span v-if="!collapsed">UnifiedSystem</span>
        <span v-else>US</span>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        v-model:openKeys="openKeys"
        mode="inline"
        theme="dark"
        @click="({ key }) => navigate(key)"
      >
        <a-menu-item key="/dashboard">
          <template #icon><DashboardOutlined /></template>
          <span>工作台</span>
        </a-menu-item>
        <a-sub-menu key="/system">
          <template #icon><SettingOutlined /></template>
          <template #title>系统管理</template>
          <a-menu-item key="/system/user">用户管理</a-menu-item>
          <a-menu-item key="/system/department">部门管理</a-menu-item>
          <a-menu-item key="/system/role">角色管理</a-menu-item>
          <a-menu-item key="/system/resource">权限管理</a-menu-item>
          <a-menu-item key="/system/message">消息中心</a-menu-item>
          <a-menu-item key="/system/dict">字典管理</a-menu-item>
          <a-menu-item key="/system/audit">审计日志</a-menu-item>
          <a-menu-item key="/system/attendance">考勤管理</a-menu-item>
          <a-menu-item key="/system/workflow">审批工作台</a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="/production">
          <template #icon><ToolOutlined /></template>
          <template #title>生产管理</template>
          <a-menu-item key="/production/product">产品与BOM</a-menu-item>
          <a-menu-item key="/production/plan">计划与排期</a-menu-item>
          <a-menu-item key="/production/order">工单管理</a-menu-item>
          <a-menu-item key="/production/purchase">采购申请</a-menu-item>
          <a-menu-item key="/production/warehouse">生产仓库</a-menu-item>
          <a-menu-item key="/production/quality">质检管理</a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="/logistics">
          <template #icon><CarOutlined /></template>
          <template #title>物流管理</template>
          <a-menu-item key="/logistics/warehouse">物流仓库</a-menu-item>
          <a-menu-item key="/logistics/transfer">调拨管理</a-menu-item>
          <a-menu-item key="/logistics/picking">拣货管理</a-menu-item>
          <a-menu-item key="/logistics/shipping">发货管理</a-menu-item>
          <a-menu-item key="/logistics/quality">物流质检</a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="/sales">
          <template #icon><ShoppingCartOutlined /></template>
          <template #title>销售管理</template>
          <a-menu-item key="/sales/customer">客户管理</a-menu-item>
          <a-menu-item key="/sales/order">销售报单</a-menu-item>
          <a-menu-item key="/sales/price">价格管理</a-menu-item>
          <a-menu-item key="/sales/contract">合同管理</a-menu-item>
          <a-menu-item key="/sales/aftersale">售后服务</a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="/finance">
          <template #icon><DollarOutlined /></template>
          <template #title>财务管理</template>
          <a-menu-item key="/finance/account">账户与科目</a-menu-item>
          <a-menu-item key="/finance/voucher">记账凭证</a-menu-item>
          <a-menu-item key="/finance/receivable">应收账款</a-menu-item>
          <a-menu-item key="/finance/payable">应付账款</a-menu-item>
          <a-menu-item key="/finance/expense">费用报销</a-menu-item>
          <a-menu-item key="/finance/budget">预算管理</a-menu-item>
          <a-menu-item key="/finance/salary">工资管理</a-menu-item>
          <a-menu-item key="/finance/report">财务报表</a-menu-item>
        </a-sub-menu>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <a-layout-header class="header">
        <div class="header-left">
          <component
            :is="collapsed ? MenuUnfoldOutlined : MenuFoldOutlined"
            class="trigger"
            @click="collapsed = !collapsed"
          />
        </div>
        <div class="header-right">
          <a-badge :count="messageStore.unreadCount" :overflow-count="99">
            <BellOutlined class="header-icon" />
          </a-badge>
          <a-dropdown>
            <a-space class="user-info">
              <UserOutlined />
              <span>{{ userStore.userInfo?.realName || '用户' }}</span>
            </a-space>
            <template #overlay>
              <a-menu>
                <a-menu-item key="logout" @click="handleLogout">
                  <LogoutOutlined /> 退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<style scoped lang="less">
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}
.header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.trigger {
  font-size: 18px;
  cursor: pointer;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}
.header-icon {
  font-size: 18px;
  cursor: pointer;
}
.user-info {
  cursor: pointer;
}
.content {
  margin: 24px;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  min-height: 280px;
}
</style>
