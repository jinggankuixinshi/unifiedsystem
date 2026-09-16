import axios from 'axios'
import type { AxiosResponse } from 'axios'
import { message } from 'ant-design-vue'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

const pendingMap = new Map<string, AbortController>()

function buildKey(config: any): string {
  const params = config.params ? JSON.stringify(config.params) : ''
  const data = config.data && typeof config.data !== 'string'
    ? JSON.stringify(config.data)
    : (config.data || '')
  return `${config.method}_${config.url}_${params}_${data}`
}

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    const key = buildKey(config)
    ;(config as any).customKey = key
    if (pendingMap.has(key)) {
      pendingMap.get(key)?.abort()
      pendingMap.delete(key)
    }
    const controller = new AbortController()
    config.signal = controller.signal
    pendingMap.set(key, controller)
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const key = (response.config as any).customKey
    if (key) pendingMap.delete(key)
    const data = response.data
    if (data.code !== 200) {
      const errMsg = data.message || data.msg
      if (data.code === 401) {
        localStorage.removeItem('token')
        window.location.hash = '#/login'
        return Promise.reject({ code: 401, message: errMsg || '登录已过期' })
      }
      message.error(errMsg || '请求失败')
      return Promise.reject({ code: data.code, message: errMsg })
    }
    return data
  },
  (error) => {
    if (axios.isCancel(error)) {
      return Promise.reject({ code: -1, message: '请求已取消' })
    }
    const key = (error.config as any)?.customKey
    if (key) pendingMap.delete(key)
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.hash = '#/login'
      return Promise.reject({ code: 401, message: '登录已过期' })
    }
    const serverMsg = error.response?.data?.message || error.response?.data?.msg
    message.error(serverMsg || error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default service
