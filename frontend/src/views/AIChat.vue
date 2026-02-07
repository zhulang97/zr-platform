<template>
  <a-card title="AI 智能助手">
    <a-row :gutter="16">
      <a-col :span="12">
        <a-card size="small" title="对话历史">
          <div style="height: 500px; overflow-y: auto;">
            <div v-for="(msg, index) in messages" :key="index" style="margin-bottom: 16px;">
              <div v-if="msg.role === 'user'" style="text-align: right;">
                <a-tag color="blue">您</a-tag>
                <div style="background: #e6f7ff; padding: 8px 12px; border-radius: 8px; display: inline-block; max-width: 70%;">
                  {{ msg.content }}
                </div>
              </div>
              <div v-else style="text-align: left;">
                <a-tag color="green">AI 助手</a-tag>
                <div style="background: #f6ffed; padding: 8px 12px; border-radius: 8px; display: inline-block; max-width: 70%;">
                  <div v-html="formatAIResponse(msg.content)"></div>
                </div>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card size="small" title="相似信息">
          <a-empty v-if="similarDocs.length === 0" description="无相似信息" />
          <a-list v-else size="small" :data-source="similarDocs" style="max-height: 500px; overflow-y: auto;">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #title>
                    <a-tag :color="getDocTypeColor(item.type)">{{ item.type }}</a-tag>
                    {{ item.content }}
                  </template>
                  <template #description>
                    相似度: {{ (item.score * 100).toFixed(1) }}%
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>

        <a-card size="small" title="异常案例库" style="margin-top: 16px;">
          <a-space style="margin-bottom: 12px;">
            <a-button type="primary" @click="handleShowCaseModal">新建案例</a-button>
            <a-button @click="handleLoadCases">刷新</a-button>
          </a-space>
          <a-list v-if="cases.length > 0" size="small" :data-source="cases" style="max-height: 300px; overflow-y: auto;">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #title>{{ item.title }}</template>
                  <template #description>
                    <a-space>
                      <span>类型: {{ item.anomalyType }}</span>
                      <a-tag :color="getSeverityColor(item.severity)">{{ getSeverityText(item.severity) }}</a-tag>
                      <span>{{ formatTime(item.createdAt) }}</span>
                    </a-space>
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a-button type="link" size="small" @click="handleViewCase(item)">查看</a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
          <a-empty v-else description="暂无案例" />
        </a-card>
      </a-col>
    </a-row>

    <a-input-search
      v-model:value="query"
      placeholder="输入问题或搜索关键词..."
      enter-button
      size="large"
      @search="handleSearch"
      @search-button-click="handleSearch"
      style="margin-top: 16px;"
      :loading="loading"
    />

    <a-modal
      v-model:open="caseModalOpen"
      title="新建异常案例"
      width="600px"
      @ok="handleCreateCase"
      @cancel="caseModalOpen = false"
    >
      <a-form :model="caseForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="标题" name="title" :rules="[{ required: true, message: '请输入标题' }]">
          <a-input v-model:value="caseForm.title" />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="caseForm.description" :rows="4" />
        </a-form-item>
        <a-form-item label="异常类型" name="anomalyType">
          <a-input v-model:value="caseForm.anomalyType" placeholder="如：overdue_payment" />
        </a-form-item>
        <a-form-item label="严重程度" name="severity">
          <a-select v-model:value="caseForm.severity">
            <a-select-option :value="1">高</a-select-option>
            <a-select-option :value="2">中</a-select-option>
            <a-select-option :value="3">低</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="caseDetailModalOpen"
      title="案例详情"
      width="700px"
      :footer="null"
    >
      <a-descriptions bordered :column="1" v-if="currentCase">
        <a-descriptions-item label="案例ID">{{ currentCase.caseId }}</a-descriptions-item>
        <a-descriptions-item label="标题">{{ currentCase.title }}</a-descriptions-item>
        <a-descriptions-item label="异常类型">{{ currentCase.anomalyType }}</a-descriptions-item>
        <a-descriptions-item label="严重程度">
          <a-tag :color="getSeverityColor(currentCase.severity)">{{ getSeverityText(currentCase.severity) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="描述">{{ currentCase.description }}</a-descriptions-item>
        <a-descriptions-item label="处理方案">{{ currentCase.resolution }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ formatTime(currentCase.createdAt) }}</a-descriptions-item>
        <a-descriptions-item label="解决时间">{{ currentCase.resolvedAt ? formatTime(currentCase.resolvedAt) : '未解决' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </a-card>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

interface Message {
  role: 'user' | 'assistant'
  content: string
}

interface VectorDoc {
  id: number
  type: string
  content: string
  score: number
}

interface AnomalyCase {
  caseId: number
  title: string
  description?: string
  anomalyType?: string
  severity: number
  resolution?: string
  createdAt: string
  resolvedAt?: string
}

const messages = ref<Message[]>([])
const similarDocs = ref<VectorDoc[]>([])
const cases = ref<AnomalyCase[]>([])
const loading = ref(false)
const query = ref('')

const caseModalOpen = ref(false)
const caseDetailModalOpen = ref(false)
const currentCase = ref<AnomalyCase | null>(null)

const caseForm = reactive({
  title: '',
  description: '',
  anomalyType: '',
  severity: 2
})

const formatAIResponse = (text: string) => {
  return text
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
}

const getDocTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    person: 'blue',
    anomaly_case: 'orange'
  }
  return colors[type] || 'default'
}

const getSeverityColor = (severity: number) => {
  const colors: Record<number, string> = {
    1: 'red',
    2: 'orange',
    3: 'blue'
  }
  return colors[severity] || 'default'
}

const getSeverityText = (severity: number) => {
  const texts: Record<number, string> = {
    1: '高',
    2: '中',
    3: '低'
  }
  return texts[severity] || '未知'
}

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const handleSearch = async () => {
  if (!query.value.trim()) return

  loading.value = true
  try {
    const res = await fetch('/api/ai/search', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query: query.value, limit: 5 })
    })
    const data = await res.json()

    if (data.code === 0) {
      similarDocs.value = data.data || []

      messages.value.push({ role: 'user', content: query.value })

      const chatRes = await fetch('/api/ai/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query: query.value, personId: null })
      })
      const chatData = await chatRes.json()
      if (chatData.code === 0) {
        messages.value.push({ role: 'assistant', content: chatData.data })
      }
    } else {
      message.error(data.message || '搜索失败')
    }
  } catch (e) {
    message.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const handleShowCaseModal = () => {
  Object.assign(caseForm, { title: '', description: '', anomalyType: '', severity: 2 })
  caseModalOpen.value = true
}

const handleCreateCase = async () => {
  try {
    const res = await fetch('/api/ai/cases', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(caseForm)
    })
    const data = await res.json()
    if (data.code === 0) {
      message.success('创建成功')
      caseModalOpen.value = false
      handleLoadCases()
    } else {
      message.error(data.message || '创建失败')
    }
  } catch (e) {
    message.error('创建失败')
  }
}

const handleLoadCases = async () => {
  try {
    const res = await fetch('/api/ai/cases')
    const data = await res.json()
    if (data.code === 0) {
      cases.value = data.data || []
    } else {
      message.error(data.message || '加载失败')
    }
  } catch (e) {
    message.error('加载失败')
  }
}

const handleViewCase = (item: AnomalyCase) => {
  currentCase.value = item
  caseDetailModalOpen.value = true
}

handleLoadCases()
</script>
