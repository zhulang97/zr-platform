<template>
  <div style="position: fixed; right: 16px; bottom: 16px; width: 360px; z-index: 999;">
    <a-card size="small" title="智能助手" :bodyStyle="{ padding: '12px' }">
      <div style="height: 180px; overflow: auto; border: 1px solid #eee; padding: 8px; border-radius: 6px;">
        <div v-for="(m, idx) in messages" :key="idx" style="margin-bottom: 8px;">
          <div style="font-size: 12px; color: #888;">{{ m.role }}</div>
          <div style="white-space: pre-wrap;">{{ m.text }}</div>
        </div>
      </div>
      <a-space style="margin-top: 8px; width: 100%;" direction="vertical">
        <a-input v-model:value="input" @pressEnter="send" placeholder="例如：帮我看下浦东新区二级肢体残疾、享受残车补贴的人" />
        <a-button type="primary" block @click="send" :loading="loading">发送</a-button>
      </a-space>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { http } from '../../api/http'

type Msg = { role: string; text: string }

const input = ref('')
const loading = ref(false)
const messages = ref<Msg[]>([
  { role: 'system', text: '我可以帮你把自然语言转换为查询条件，并解释结果。' }
])

async function send() {
  const text = input.value.trim()
  if (!text) return
  messages.value.push({ role: 'user', text })
  input.value = ''
  loading.value = true
  try {
    const resp = await http.post('/api/assistant/chat', { text })
    const answer = resp.data?.data?.answer ?? '...'
    messages.value.push({ role: 'assistant', text: answer })
  } finally {
    loading.value = false
  }
}
</script>
