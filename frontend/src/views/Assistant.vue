<template>
  <a-drawer
    v-model:open="open"
    title="智能助手"
    width="520"
    placement="right"
  >
    <a-space direction="vertical" style="width: 100%;">
      <a-input
        v-model:value="input"
        placeholder="例如：帮我查浦东新区二级肢体残疾、享受残车补贴的人"
        @pressEnter="send"
      />
      <a-button type="primary" :loading="sending" @click="send">发送</a-button>
    </a-space>
    <a-divider />
    <a-spin :spinning="loading">
      <div v-if="response">
        <a-alert v-if="response.action === 'SET_FILTERS'" type="success" show-icon>
          <template #message>
            <span>筛选条件已自动填充（请查看查询页）</span>
          </template>
        </a-alert>
        <a-alert v-else type="info">
          <template #message>
            <div>{{ response.dsl?.intent ?? response.action }}：{{ response.explanation }}</div>
            <div v-if="response.data?.total !== undefined && response.data.total > 0">
              共找到 {{ response.data.total }} 条记录
            </div>
          </template>
        </a-alert>
      </div>
    </a-spin>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'

const open = ref(false)
const input = ref('')
const loading = ref(false)
const response = ref<any>(null)

async function send() {
  loading.value = true
  try {
    const resp = await http.post('/api/assistant/chat', { text: input.value })
    response.value = resp.data.data
    if (response.value.action === 'SET_FILTERS') {
      message.success('筛选条件已设置')
      open.value = false
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? '助手调用失败')
  } finally {
    loading.value = false
  }
}
</script>