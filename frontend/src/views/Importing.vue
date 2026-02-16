<template>
  <div class="import-page">
    <a-card :bordered="false" class="main-card">
      <template #title>
        <div class="card-header">
          <span class="title">数据导入治理</span>
          <a-space>
            <a-button type="link" @click="router.push('/import/config')">
              <SettingOutlined />
              模块配置
            </a-button>
            <a-button type="link" @click="showHistory ? backToImport() : (showHistory = !showHistory)" v-if="!viewingDetail">
              <HistoryOutlined />
              {{ showHistory ? '返回导入' : '历史记录' }}
            </a-button>
          </a-space>
        </div>
      </template>

      <!-- 历史记录详情 -->
      <template v-if="viewingDetail">
        <HistoryDetail 
          :batch-id="selectedBatchId" 
          :on-back="backFromDetail" 
        />
      </template>

      <!-- 历史记录列表 -->
      <template v-else-if="showHistory">
        <HistoryPanel @view="viewBatch" @retry="retryBatch" />
      </template>

      <!-- 导入流程 -->
      <template v-else>
        <!-- 步骤条 -->
        <div class="steps-container">
          <a-steps :current="currentStep" size="small">
            <a-step title="选择模块" />
            <a-step title="上传文件" />
            <a-step title="字段映射" />
            <a-step title="预览确认" />
            <a-step title="执行导入" />
          </a-steps>
        </div>

        <!-- 步骤内容 -->
        <div class="step-content">
          <!-- 步骤1: 选择模块 -->
          <Step1SelectModule 
            v-if="currentStep === 0"
            v-model:selectedModules="selectedModules"
            @next="goNext"
          />

          <!-- 步骤2: 上传文件 -->
          <Step2UploadFile
            v-if="currentStep === 1"
            :modules="selectedModules"
            v-model:files="uploadedFiles"
            v-model:strategy="importStrategy"
            @prev="goPrev"
            @next="goNext"
          />

          <!-- 步骤3: 字段映射 -->
          <Step3FieldMapping
            v-if="currentStep === 2"
            :batch-id="currentBatchId"
            :module="currentModule"
            @prev="goPrev"
            @next="goNext"
          />

          <!-- 步骤4: 预览确认 -->
          <Step4Preview
            v-if="currentStep === 3"
            :batch-id="currentBatchId"
            :module="currentModule"
            @prev="goPrev"
            @next="goNext"
          />

          <!-- 步骤5: 执行导入 -->
          <Step5Execute
            v-if="currentStep === 4"
            :batch-id="currentBatchId"
            :module="currentModule"
            :strategy="importStrategy"
            @done="onImportDone"
          />
        </div>
      </template>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { HistoryOutlined, SettingOutlined } from '@ant-design/icons-vue'
import Step1SelectModule from './import/Step1SelectModule.vue'
import Step2UploadFile from './import/Step2UploadFile.vue'
import Step3FieldMapping from './import/Step3FieldMapping.vue'
import Step4Preview from './import/Step4Preview.vue'
import Step5Execute from './import/Step5Execute.vue'
import HistoryPanel from './import/HistoryPanel.vue'
import HistoryDetail from './import/HistoryDetail.vue'

const router = useRouter()
const currentStep = ref(0)
const showHistory = ref(false)
const viewingDetail = ref(false)
const selectedModules = ref<string[]>([])
const uploadedFiles = ref<any[]>([])
const importStrategy = ref('ID_CARD_MERGE')
const currentBatchId = ref<number | null>(null)
const currentModule = ref<string>('')
const selectedBatchId = ref<number>(0)

const goNext = (data?: any) => {
  if (currentStep.value === 1 && data) {
    currentBatchId.value = data.batchId
    currentModule.value = data.moduleCode
  }
  currentStep.value++
}

const goPrev = () => {
  currentStep.value--
}

const onImportDone = () => {
  showHistory.value = true
}

const viewBatch = (batch: any) => {
  selectedBatchId.value = batch.batchId
  viewingDetail.value = true
}

const retryBatch = (batch: any) => {
  currentModule.value = batch.moduleCode
  currentBatchId.value = batch.batchId
  showHistory.value = false
  currentStep.value = 2
}

const backFromDetail = () => {
  viewingDetail.value = false
  selectedBatchId.value = 0
}

const backToImport = () => {
  showHistory.value = false
  currentStep.value = 0
  currentBatchId.value = null
  currentModule.value = ''
  selectedModules.value = []
  uploadedFiles.value = []
}
</script>

<style scoped>
.import-page {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.main-card {
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.steps-container {
  padding: 20px 40px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 24px;
}

.step-content {
  min-height: 400px;
}
</style>
