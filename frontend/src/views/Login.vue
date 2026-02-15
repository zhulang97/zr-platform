<template>
  <div style="min-height: 100vh; display: grid; place-items: center; background: #f6f7fb; padding: 24px;">
    <a-card title="智联助残数据平台" style="width: 360px;">
      <a-form layout="vertical" :model="{ username, password }" @finish="onSubmit">
        <a-form-item label="用户名" name="username" :rules="[{ required: true, message: '请输入用户名' }]">
          <a-input v-model:value="username" autocomplete="username" />
        </a-form-item>
        <a-form-item label="密码" name="password" :rules="[{ required: true, message: '请输入密码' }]">
          <a-input-password v-model:value="password" autocomplete="current-password" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" block :loading="loading">登录</a-button>
        </a-form-item>
      </a-form>
      <div style="margin-top: 12px; color: #666; font-size: 12px;">
        Dev default: admin / Admin@12345
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { useAuthStore } from '../stores/auth'
import { router } from '../router'

const auth = useAuthStore()
const username = ref('admin')
const password = ref('Admin@12345')
const loading = ref(false)

async function onSubmit() {
  console.log('开始登录...', username.value)
  loading.value = true
  try {
    await auth.login(username.value, password.value)
    console.log('登录成功，跳转到首页')
    await router.push('/home')
  } catch (e: any) {
    console.error('登录失败:', e)
    message.error(e?.response?.data?.message ?? '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
