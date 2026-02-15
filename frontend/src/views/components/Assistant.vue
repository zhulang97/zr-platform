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
        <a-input 
          v-model:value="input" 
          @pressEnter="send" 
          placeholder="例如：帮我查浦东新区二级肢体残疾的人，或跳转到异常管理页面" 
        />
        <a-button type="primary" block @click="send" :loading="loading">发送</a-button>
      </a-space>
      <div style="margin-top: 8px; font-size: 11px; color: #888;">
        💡 提示：支持"查询/跳转/刷新"等指令
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { http } from '../../api/http'
import { useAIController, AIResponse } from '../../composables/useAIController'

type Msg = { role: string; text: string }

const input = ref('')
const loading = ref(false)
const route = useRoute()
const { handleAIResponse } = useAIController()

const messages = ref<Msg[]>([
  { role: 'system', text: '我可以帮你查询数据、跳转页面、筛选条件等。试试说"帮我查浦东新区的异常记录"或"跳转到统计页面"' }
])

async function send() {
  const text = input.value.trim()
  if (!text) return
  messages.value.push({ role: 'user', text })
  input.value = ''
  loading.value = true
  
  try {
    const resp = await http.post('/api/assistant/chat', { 
      text,
      currentPage: route.path.replace('/', '') || 'home'
    })
    
    const data = resp.data?.data
    
    if (data) {
      // 处理结构化响应
      const aiResponse: AIResponse = {
        action: data.action || 'ANSWER_ONLY',
        dsl: data.dsl,
        explanation: data.explanation,
        navigation: data.navigation,
        targetPage: data.targetPage
      }
      
      // 执行 AI 指令
      await handleAIResponse(aiResponse)
      
      // 显示回答文本
      const answerText = data.explanation || data.answer || '已完成操作'
      messages.value.push({ role: 'assistant', text: answerText })
    } else {
      messages.value.push({ role: 'assistant', text: '抱歉，无法理解您的请求' })
    }
  } catch (error: any) {
    messages.value.push({ 
      role: 'assistant', 
      text: '请求失败：' + (error?.response?.data?.message || '请稍后重试')
    })
  } finally {
    loading.value = false
  }
}
</script>
