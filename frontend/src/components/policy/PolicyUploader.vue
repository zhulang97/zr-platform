<template>
  <div class="policy-uploader">
    <a-upload-dragger
      v-model:fileList="fileList"
      :customRequest="handleUpload"
      :beforeUpload="beforeUpload"
      :showUploadList="false"
      accept=".pdf,.doc,.docx,.txt"
      class="upload-area"
    >
      <div v-if="!uploading && !previewUrl" class="upload-placeholder">
        <p class="ant-upload-drag-icon">
          <inbox-outlined style="font-size: 48px; color: #1890ff;"></inbox-outlined>
        </p>
        <p class="ant-upload-text">点击或拖拽文件到此处上传</p>
        <p class="ant-upload-hint">
          支持 PDF、Word(.doc/.docx)、TXT 格式，文件大小不超过 10MB
        </p>
      </div>
      
      <div v-else-if="uploading" class="upload-progress">
        <a-progress
          type="circle"
          :percent="uploadProgress"
          :status="uploadProgress === 100 ? 'success' : 'active'"
        />
        <p style="margin-top: 16px;">正在上传...</p>
      </div>
      
      <div v-else class="upload-preview">
        <file-outlined style="font-size: 48px; color: #52c41a;"></file-outlined>
        <p style="margin-top: 16px; font-weight: 500;">{{ currentFile?.name }}</p>
        <p style="color: #666;">上传成功</p>
        <a-space style="margin-top: 16px;">
          <a-button type="primary" @click.stop="handleAnalyze">
            <search-outlined /> 分析政策
          </a-button>
          <a-button @click.stop="handleReupload">重新上传</a-button>
        </a-space>
      </div>
    </a-upload-dragger>
    
    <!-- 手动输入文本 -->
    <a-divider>或</a-divider>
    
    <a-textarea
      v-model:value="manualText"
      :rows="6"
      placeholder="直接粘贴政策文本内容..."
      class="manual-input"
    />
    
    <a-button 
      v-if="manualText" 
      type="primary" 
      block 
      style="margin-top: 12px;"
      @click="handleManualAnalyze"
      :loading="analyzing"
    >
      <search-outlined /> 分析文本
    </a-button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { InboxOutlined, FileOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { PolicyApi, type PolicyUploadResponse } from '../../api/policy'

const emit = defineEmits<{
  (e: 'upload-success', data: PolicyUploadResponse & { file?: File }): void
  (e: 'analyze', policyId: string, content?: string): void
}>()

const fileList = ref([])
const uploading = ref(false)
const uploadProgress = ref(0)
const currentFile = ref<File | null>(null)
const previewUrl = ref('')
const manualText = ref('')
const analyzing = ref(false)

const beforeUpload = (file: File) => {
  const isValidType = ['application/pdf', 'application/msword', 
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 
    'text/plain'].includes(file.type)
  
  if (!isValidType) {
    message.error('只支持 PDF、Word、TXT 格式的文件！')
    return false
  }
  
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    message.error('文件大小不能超过 10MB！')
    return false
  }
  
  return true
}

const handleUpload = async (options: any) => {
  const { file, onProgress, onSuccess, onError } = options
  currentFile.value = file
  uploading.value = true
  uploadProgress.value = 0
  
  try {
    // 直接通过后端上传
    const resp = await PolicyApi.uploadFile(file)
    const { policyId, uploadUrl, ossKey } = resp.data.data
    
    uploadProgress.value = 100
    previewUrl.value = uploadUrl
    uploading.value = false
    onSuccess({})
    
    message.success('文件上传成功！')
    emit('upload-success', { 
      policyId, 
      uploadUrl, 
      ossKey, 
      file 
    })
    
  } catch (error: any) {
    console.error('Upload error:', error)
    uploading.value = false
    onError(error)
    message.error('上传失败：' + (error?.response?.data?.message || error?.message || '请重试'))
  }
}

const handleAnalyze = () => {
  // 从 emit 中获取 policyId
  // 这里需要在父组件中处理
  message.info('请点击分析政策按钮')
}

const handleReupload = () => {
  fileList.value = []
  currentFile.value = null
  previewUrl.value = ''
  uploadProgress.value = 0
}

const handleManualAnalyze = async () => {
  if (!manualText.value.trim()) {
    message.warning('请输入政策文本')
    return
  }
  
  analyzing.value = true
  try {
    // 创建临时政策记录
    const resp = await PolicyApi.getUploadUrl('manual-input.txt', 'text/plain')
    const { policyId } = resp.data.data
    await PolicyApi.confirmUpload(policyId)
    
    emit('analyze', policyId, manualText.value)
  } catch (error: any) {
    message.error('创建分析任务失败')
  } finally {
    analyzing.value = false
  }
}

// 暴露方法给父组件
const getCurrentFile = () => currentFile.value
const clear = () => {
  handleReupload()
  manualText.value = ''
}

defineExpose({
  getCurrentFile,
  clear
})
</script>

<style scoped>
.policy-uploader {
  padding: 20px;
}

.upload-area {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.3s;
}

.upload-area:hover {
  border-color: #1890ff;
  background: #e6f7ff;
}

.upload-placeholder {
  padding: 40px 0;
  text-align: center;
}

.upload-progress {
  padding: 40px 0;
  text-align: center;
}

.upload-preview {
  padding: 40px 0;
  text-align: center;
}

.manual-input {
  border-radius: 8px;
}

:deep(.ant-upload-drag) {
  border: none;
  background: transparent;
}

:deep(.ant-upload-drag:hover) {
  border: none;
}
</style>
