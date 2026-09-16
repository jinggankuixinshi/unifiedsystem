import { useDebounceFn } from '@vueuse/core'
import { ref, type Ref } from 'vue'

export function useDebounce(fn: (...args: any[]) => void, delay = 300) {
  return useDebounceFn(fn, delay)
}

export function usePagination(pageSizeDefault = 10) {
  const current = ref(1)
  const pageSize = ref(pageSizeDefault)
  const total = ref(0)

  function onChange(page: number, size: number) {
    current.value = page
    pageSize.value = size
  }

  function reset() {
    current.value = 1
  }

  return { current, pageSize, total, onChange, reset }
}

export function useLoading() {
  const loading = ref(false)
  const error = ref(false)
  const errorMsg = ref('')

  function start() {
    loading.value = true
    error.value = false
  }

  function done() {
    loading.value = false
  }

  function fail(msg = '加载失败') {
    loading.value = false
    error.value = true
    errorMsg.value = msg
  }

  return { loading, error, errorMsg, start, done, fail }
}
