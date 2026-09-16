<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="login-title">UnifiedSystem</h1>
      <p class="login-subtitle">企业管理系统</p>
      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        @finish="handleLogin"
      >
        <a-form-item name="username">
          <a-input
            v-model:value="formState.username"
            placeholder="用户名"
            size="large"
            :prefix="h(UserOutlined)"
          />
        </a-form-item>
        <a-form-item name="password">
          <a-input-password
            v-model:value="formState.password"
            placeholder="密码"
            size="large"
            :prefix="h(LockOutlined)"
            @keyup.enter="handleLogin"
          />
        </a-form-item>
        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            block
            :loading="submitting"
            :disabled="submitting"
          >
            登 录
          </a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { login } from '@/api/modules/auth'
import { useUserStore } from '@/stores/user'
import type { Rule } from 'ant-design-vue/es/form'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const submitting = ref(false)

const formState = reactive({
  username: '',
  password: ''
})

const rules: Record<string, Rule[]> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (submitting.value) return
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    const res = await login({ username: formState.username, password: formState.password })
    const { token, userInfo } = res.data
    userStore.setToken(token)
    userStore.userInfo = userInfo
    userStore.permissions = userInfo.permissions || ['*']
    message.success('登录成功')
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } catch (err: any) {
    message.error(err?.message || '登录失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="less">
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f2f5 0%, #e8edf2 100%);
}
.login-card {
  width: 400px;
  padding: 48px 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}
.login-title {
  text-align: center;
  font-size: 28px;
  color: #1e3a5f;
  margin-bottom: 4px;
}
.login-subtitle {
  text-align: center;
  font-size: 14px;
  color: #9ca3af;
  margin-bottom: 36px;
}
</style>
