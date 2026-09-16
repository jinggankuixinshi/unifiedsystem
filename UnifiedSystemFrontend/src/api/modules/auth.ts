import request from '@/api/request'

export interface LoginParams {
  username: string
  password: string
}

export interface UserInfo {
  userId: number
  username: string
  realName: string
  deptId: number
  deptName: string
  roles: string[]
  permissions: string[]
}

export function login(params: LoginParams) {
  return request.post('/auth/login', params) as Promise<any>
}

export function logout() {
  return request.post('/auth/logout')
}
