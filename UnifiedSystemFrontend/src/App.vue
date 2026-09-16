<template>
  <a-config-provider :theme="themeConfig">
    <router-view />
  </a-config-provider>
</template>

<script setup lang="ts">
import { onErrorCaptured, ref } from 'vue'

const themeConfig = {
  token: {
    colorPrimary: '#1e3a5f',
    colorSuccess: '#5b8c5a',
    colorWarning: '#d4953a',
    colorError: '#b44a4a',
    borderRadius: 4,
    fontSize: 14,
    colorText: '#4b5563',
    colorTextHeading: '#1f2937',
  }
}

const hasError = ref(false)

onErrorCaptured((err) => {
  console.error('Component error:', err)
  hasError.value = true
  return false
})
</script>
