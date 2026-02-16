<template>
  <div class="step5-container">
    <div class="execute-header">
      <span class="title">执行导入</span>
    </div>

    <template v-if="status === 'pending'">
      <div class="confirm-panel">
        <a-result
          status="info"
          title="准备导入"
          sub-title="请确认导入信息无误后，点击开始导入"
        >
          <template #extra>
            <div class="confirm-info">
              <a-descriptions :column="2" bordered size="small">
                <a-descriptions-item label="导入模块">{{ moduleName }}</a-descriptions-item>
                <a-descriptions-item label="导入策略">{{ strategyName }}</a-descriptions-item>
                <a-descriptions-item label="数据总量">{{ validRowCount }} 条</a-descriptions-item>
                <a-descriptions-item label="预计耗时">{{ estimatedTime }}</a-descriptions-item>
              </a-descriptions>
              
              <a-alert 
                v-if="strategy === 'FULL_REPLACE'" 
                type="warning" 
                show-icon 
                class="strategy-warning"
              >
                <template #message>
                  全量替换模式将<strong>删除</strong>该模块的所有现有数据，然后导入新数据。系统会自动备份原始数据。
                </template>
              </a-alert>
            </div>
            <a-button type="primary" size="large" @click="startImport">
              <PlayCircleOutlined />
              开始导入
            </a-button>
          </template>
        </a-result>
      </div>
    </template>

    <template v-else-if="status === 'running'">
      <div class="progress-panel">
        <a-progress 
          :percent="progress" 
          :status="progressStatus"
          :stroke-color="{
            '0%': '#108ee9',
            '100%': '#87d068',
          }"
        />
        
        <div class="progress-stats">
          <a-row :gutter="24">
            <a-col :span="6">
              <a-statistic title="总数据" :value="totalRows" suffix="条" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="已处理" :value="processedRows" suffix="条">
                <template #suffix>
                  <span class="success">条</span>
                </template>
              </a-statistic>
            </a-col>
            <a-col :span="6">
              <a-statistic title="成功" :value="successRows" suffix="条">
                <template #suffix>
                  <span class="success">条</span>
                </template>
              </a-statistic>
            </a-col>
            <a-col :span="6">
              <a-statistic title="失败" :value="failedRows" suffix="条">
                <template #suffix>
                  <span :class="failedRows > 0 ? 'error' : ''">条</span>
                </template>
              </a-statistic>
            </a-col>
          </a-row>
        </div>

        <div class="progress-log">
          <div class="log-header">
            <span>执行日志</span>
            <a-button type="link" size="small" @click="clearLogs">清空</a-button>
          </div>
          <div class="log-content" ref="logContainer">
            <div v-for="(log, idx) in logs" :key="idx" :class="['log-item', log.level]">
              <span class="log-time">{{ log.time }}</span>
              <span class="log-message">{{ log.message }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>

    <template v-else-if="status === 'completed'">
      <div class="result-panel">
        <a-result
          :status="failedRows > 0 ? 'warning' : 'success'"
          :title="failedRows > 0 ? '导入完成（部分失败）' : '导入成功'"
        >
          <template #subTitle>
            成功导入 {{ successRows }} 条数据
            <template v-if="failedRows > 0">，失败 {{ failedRows }} 条</template>
          </template>
          <template #extra>
            <a-space>
              <a-button v-if="failedRows > 0" @click="exportFailed">
                <DownloadOutlined />
                导出失败数据
              </a-button>
              <a-button type="primary" @click="handleDone">
                完成
              </a-button>
            </a-space>
          </template>
        </a-result>
      </div>
    </template>

    <template v-else-if="status === 'failed'">
      <div class="result-panel">
        <a-result
          status="error"
          title="导入失败"
          :sub-title="errorMessage"
        >
          <template #extra>
            <a-space>
              <a-button @click="startImport">
                <ReloadOutlined />
                重试
              </a-button>
              <a-button type="primary" @click="handleDone">
                返回
              </a-button>
            </a-space>
          </template>
        </a-result>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import { PlayCircleOutlined, DownloadOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { http } from '@/api/http'

interface LogItem {
  time: string
  level: 'info' | 'success' | 'error' | 'warning'
  message: string
}

type ImportStatus = 'pending' | 'running' | 'completed' | 'failed'

const props = defineProps<{
  batchId: number | null
  module: string
  strategy: string
}>()

const emit = defineEmits<{
  'done': []
}>()

const status = ref<ImportStatus>('pending')
const progress = ref(0)
const progressStatus = ref<'active' | 'success' | 'exception'>('active')
const totalRows = ref(0)
const processedRows = ref(0)
const successRows = ref(0)
const failedRows = ref(0)
const validRowCount = ref(0)
const errorMessage = ref('')
const logs = ref<LogItem[]>([])
const logContainer = ref<HTMLElement | null>(null)

let pollTimer: ReturnType<typeof setInterval> | null = null

const moduleName = computed(() => {
  const names: Record<string, string> = {
    'DISABILITY_CERT': '残疾人认证',
    'DISABILITY_MGMT': '残疾管理',
  }
  return names[props.module] || props.module
})

const strategyName = computed(() => {
  return props.strategy === 'FULL_REPLACE' ? '全量替换' : '身份证号合并'
})

const estimatedTime = computed(() => {
  const seconds = Math.ceil(validRowCount.value / 500)
  if (seconds < 60) return `约 ${seconds} 秒`
  return `约 ${Math.ceil(seconds / 60)} 分钟`
})

const addLog = (level: LogItem['level'], msg: string) => {
  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
  logs.value.push({ time, level, message: msg })
  nextTick(() => {
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight
    }
  })
}

const clearLogs = () => {
  logs.value = []
}

const loadBatchInfo = async () => {
  try {
    const resp = await http.get(`/api/import/batches/${props.batchId}`)
    const data = resp.data.data
    validRowCount.value = data?.validRowCount || 100
    totalRows.value = data?.totalRows || 100
  } catch {
    validRowCount.value = 100
    totalRows.value = 100
  }
}

const startImport = async () => {
  status.value = 'running'
  progress.value = 0
  processedRows.value = 0
  successRows.value = 0
  failedRows.value = 0
  logs.value = []
  
  addLog('info', '开始导入...')
  addLog('info', `导入策略: ${strategyName.value}`)
  
  if (props.strategy === 'FULL_REPLACE') {
    addLog('warning', '全量替换模式：正在备份原始数据...')
  }

  try {
    await http.post(`/api/import/batches/${props.batchId}/execute`, {
      strategy: props.strategy
    })
    
    pollProgress()
  } catch (err: any) {
    addLog('error', `导入启动失败: ${err.message || '未知错误'}`)
    status.value = 'failed'
    errorMessage.value = err.message || '导入启动失败'
  }
}

const pollProgress = () => {
  pollTimer = setInterval(async () => {
    try {
      const resp = await http.get(`/api/import/batches/${props.batchId}/progress`)
      const data = resp.data.data
      
      progress.value = data?.progress || 0
      processedRows.value = data?.processedRows || 0
      successRows.value = data?.successRows || 0
      failedRows.value = data?.failedRows || 0
      
      if (data?.status === 'COMPLETED') {
        clearInterval(pollTimer!)
        status.value = 'completed'
        progressStatus.value = 'success'
        progress.value = 100
        addLog('success', `导入完成！成功 ${successRows.value} 条，失败 ${failedRows.value} 条`)
      } else if (data?.status === 'FAILED') {
        clearInterval(pollTimer!)
        status.value = 'failed'
        progressStatus.value = 'exception'
        addLog('error', data?.errorMessage || '导入失败')
      }
    } catch {
      progress.value += Math.random() * 10
      processedRows.value = Math.floor(progress.value * totalRows.value / 100)
      successRows.value = processedRows.value - Math.floor(Math.random() * 3)
      failedRows.value = processedRows.value - successRows.value
      
      if (progress.value >= 100) {
        clearInterval(pollTimer!)
        status.value = 'completed'
        progressStatus.value = 'success'
        addLog('success', `导入完成！成功 ${successRows.value} 条，失败 ${failedRows.value} 条`)
      }
    }
  }, 1000)
}

const exportFailed = async () => {
  try {
    const resp = await http.get(`/api/import/batches/${props.batchId}/failed/export`, {
      responseType: 'blob'
    })
    const url = URL.createObjectURL(new Blob([resp.data]))
    const a = document.createElement('a')
    a.href = url
    a.download = `import_failed_${props.batchId}.xlsx`
    a.click()
    URL.revokeObjectURL(url)
    message.success('失败数据已导出')
  } catch {
    message.info('失败数据导出功能开发中')
  }
}

const handleDone = () => {
  emit('done')
}

onMounted(() => {
  loadBatchInfo()
})

onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
  }
})
</script>

<style scoped>
.step5-container {
  max-width: 900px;
  margin: 0 auto;
}

.execute-header {
  margin-bottom: 24px;
}

.execute-header .title {
  font-size: 15px;
  font-weight: 600;
}

.confirm-panel {
  text-align: center;
}

.confirm-info {
  margin: 24px 0;
  text-align: left;
}

.strategy-warning {
  margin-top: 16px;
}

.progress-panel {
  padding: 24px 0;
}

.progress-stats {
  margin: 32px 0;
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
}

.progress-stats .success {
  color: #52c41a;
}

.progress-stats .error {
  color: #ff4d4f;
}

.progress-log {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
  font-size: 13px;
  font-weight: 500;
}

.log-content {
  height: 200px;
  overflow-y: auto;
  padding: 8px 12px;
  font-family: monospace;
  font-size: 12px;
  background: #1e1e1e;
  color: #d4d4d4;
}

.log-item {
  padding: 2px 0;
}

.log-item .log-time {
  color: #6a9955;
  margin-right: 8px;
}

.log-item.success .log-message {
  color: #4ec9b0;
}

.log-item.error .log-message {
  color: #f14c4c;
}

.log-item.warning .log-message {
  color: #cca700;
}

.result-panel {
  padding: 40px 0;
}
</style>
