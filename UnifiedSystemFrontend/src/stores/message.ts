import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/api/request'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)
  const wsConnected = ref(false)

  function setUnreadCount(count: number) {
    unreadCount.value = count
  }

  async function fetchUnreadCount() {
    try {
      const res = await request.get('/messages/unread-count') as any
      unreadCount.value = res.data?.unreadCount || 0
    } catch {}
  }

  function incrementUnread() {
    unreadCount.value++
  }

  return { unreadCount, wsConnected, setUnreadCount, fetchUnreadCount, incrementUnread }
})
