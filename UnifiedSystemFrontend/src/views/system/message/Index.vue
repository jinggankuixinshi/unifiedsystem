<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">消息中心</h2>
      <a-button size="small" @click="markAllRead">全部已读</a-button>
    </div>
    <a-card class="content-card">
      <a-spin :spinning="loading">
        <a-list :data-source="messages" :pagination="{ current, pageSize, total, onChange: onPageChange }">
          <template #renderItem="{ item }">
            <a-list-item :class="{ unread: !item.isRead }" @click="markRead(item)">
              <a-list-item-meta>
                <template #title>
                  <a-badge v-if="!item.isRead" status="processing" :offset="[-6, 3]" />
                  {{ item.title }}
                </template>
                <template #description>
                  <div>{{ item.content }}</div>
                  <div class="msg-time">{{ item.createTime }}</div>
                </template>
              </a-list-item-meta>
            </a-list-item>
          </template>
          <template #empty>
            <a-empty description="暂无消息" />
          </template>
        </a-list>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import request from '@/api/request'
import { useMessageStore } from '@/stores/message'

const messageStore = useMessageStore()
const loading = ref(false)
const messages = ref<any[]>([])
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function fetchMessages() {
  loading.value = true
  try {
    const res = await request.get('/messages', { params: { pageNum: current.value, pageSize: pageSize.value } }) as any
    messages.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { } finally { loading.value = false }
}

function onPageChange(page: number) {
  current.value = page; fetchMessages()
}

async function markRead(item: any) {
  if (item.isRead) return
  try {
    await request.put(`/messages/${item.id}/read`)
    item.isRead = 1
    messageStore.fetchUnreadCount()
  } catch { }
}

async function markAllRead() {
  try {
    await request.put('/messages/read-all')
    messages.value.forEach(m => m.isRead = 1)
    messageStore.setUnreadCount(0)
    message.success('已全部标记为已读')
  } catch { }
}

onMounted(() => { fetchMessages(); messageStore.fetchUnreadCount() })
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';
.unread {
  background: rgba(30, 58, 95, 0.03);
  cursor: pointer;
}
.msg-time {
  color: #9ca3af;
  font-size: 12px;
}
</style>
