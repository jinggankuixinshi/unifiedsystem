import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/api/request'
import type { UserInfo } from '@/api/modules/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])

  async function fetchUserInfo(): Promise<UserInfo> {
    const res = await request.get('/auth/user-info') as any
    userInfo.value = res.data
    roles.value = res.data?.roles || []
    permissions.value = res.data?.permissions || ['*']
    return res.data
  }

  function setToken(val: string) {
    token.value = val
    if (val) {
      localStorage.setItem('token', val)
    } else {
      localStorage.removeItem('token')
    }
  }

  function hasPermission(perm: string): boolean {
    return permissions.value.includes(perm) || permissions.value.includes('*')
  }

  async function logout() {
    try { await request.post('/auth/logout') } catch {}
    token.value = ''
    userInfo.value = null
    roles.value = []
    permissions.value = []
    localStorage.removeItem('token')
  }

  return { token, userInfo, roles, permissions, fetchUserInfo, setToken, hasPermission, logout }
})
