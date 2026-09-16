export interface Result<T = any> {
  code: number
  msg: string
  data: T
}

export interface PageResult<T = any> {
  total: number
  pageNum: number
  pageSize: number
  list: T[]
}

export interface PageData<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
