<template>
  <div class="step2-container">
    <div class="upload-section">
      <div class="section-title">
        <UploadOutlined />
        上传文件
      </div>
      
      <a-upload-dragger
        v-model:file-list="fileList"
        :multiple="true"
        :accept="'.xlsx,.xls,.csv'"
        :before-upload="beforeUpload"
        class="upload-dragger"
      >
        <p class="ant-upload-drag-icon">
          <InboxOutlined />
        </p>
        <p class="ant-upload-text">点击或拖拽文件到此区域上传</p>
        <p class="ant-upload-hint">
          支持 Excel (.xlsx, .xls) 和 CSV 文件，单个文件不超过 10MB
        </p>
      </a-upload-dragger>

      <div class="file-list" v-if="fileList.length > 0">
        <div class="list-title">已上传文件 ({{ fileList.length }})</div>
        <div class="file-item" v-for="(file, index) in fileList" :key="index">
          <div class="file-info">
            <FileExcelOutlined class="file-icon" />
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">{{ formatSize(file.size || 0) }}</span>
          </div>
          <a-button type="link" danger size="small" @click="removeFile(index)">
            <DeleteOutlined />
          </a-button>
        </div>
      </div>
    </div>

    <div class="strategy-section">
      <div class="section-title">
        <SettingOutlined />
        导入策略
      </div>
      
      <a-radio-group v-model:value="localStrategy" class="strategy-group">
        <a-radio value="FULL_REPLACE" class="strategy-item">
          <div class="strategy-content">
            <div class="strategy-name">全部覆盖</div>
            <div class="strategy-desc">删除该模块原有数据，再导入新数据</div>
          </div>
        </a-radio>
        <a-radio value="ID_CARD_MERGE" class="strategy-item">
          <div class="strategy-content">
            <div class="strategy-name">同证件号覆盖</div>
            <div class="strategy-desc">根据"证件号"字段判断，存在则覆盖，不存在则追加</div>
          </div>
        </a-radio>
      </a-radio-group>

      <a-alert
        type="info"
        show-icon
        class="strategy-tip"
      >
        <template #message>
          <strong>证件号规则说明：</strong>
          身份证号 = 证件号 (18位)；残疾证号去掉最后2位 = 身份证号
        </template>
      </a-alert>
    </div>

    <div class="actions">
      <a-button @click="$emit('prev')">上一步</a-button>
      <a-button 
        type="primary" 
        :disabled="fileList.length === 0" 
        :loading="uploading"
        @click="handleUpload"
      >
        开始上传
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadProps } from 'ant-design-vue'
import {
  UploadOutlined,
  InboxOutlined,
  FileExcelOutlined,
  DeleteOutlined,
  SettingOutlined
} from '@ant-design/icons-vue'
import { http } from '@/api/http'

const props = defineProps<{
  modules: string[]
  files: any[]
  strategy: string
}>()

const emit = defineEmits<{
  'update:files': [value: any[]]
  'update:strategy': [value: string]
  'next': [data: { batchId: number; moduleCode: string }]
  'prev': []
}>()

const fileList = ref<any[]>([])
const localStrategy = ref(props.strategy)
const uploading = ref(false)

watch(localStrategy, (val) => {
  emit('update:strategy', val)
})

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isValidType = file.name.endsWith('.xlsx') || file.name.endsWith('.xls') || file.name.endsWith('.csv')
  if (!isValidType) {
    message.error('只支持 Excel 或 CSV 文件')
    return false
  }
  
  const isValidSize = file.size / 1024 / 1024 < 10
  if (!isValidSize) {
    message.error('文件大小不能超过 10MB')
    return false
  }
  
  return false
}

const removeFile = (index: number) => {
  fileList.value.splice(index, 1)
}

const formatSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

const handleUpload = async () => {
  if (props.modules.length === 0) {
    message.warning('请先选择导入模块')
    return
  }
  
  if (fileList.value.length === 0) {
    message.warning('请上传文件')
    return
  }

  uploading.value = true
  
  try {
    const formData = new FormData()
    formData.append('file', fileList.value[0].originFileObj || fileList.value[0])
    formData.append('strategy', localStrategy.value)
    
    const resp = await http.post(`/api/import/modules/${props.modules[0]}/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    const batchId = resp.data.data?.batchId
    message.success('上传成功')
    emit('next', { batchId, moduleCode: props.modules[0] })
  } catch (e: any) {
    message.error(e?.response?.data?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.step2-container {
  max-width: 800px;
  margin: 0 auto;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-section {
  margin-bottom: 32px;
}

.upload-dragger {
  margin-bottom: 16px;
}

.file-list {
  background: #fafafa;
  border-radius: 8px;
  padding: 12px;
}

.list-title {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #fff;
  border-radius: 6px;
  margin-bottom: 8px;
}

.file-item:last-child {
  margin-bottom: 0;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  color: #52c41a;
  font-size: 18px;
}

.file-name {
  color: #262626;
}

.file-size {
  color: #8c8c8c;
  font-size: 12px;
}

.strategy-section {
  margin-bottom: 32px;
}

.strategy-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.strategy-item {
  display: flex;
  padding: 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  margin: 0 !important;
}

.strategy-item:has(.ant-radio-checked) {
  border-color: #1890ff;
  background: #e6f7ff;
}

.strategy-content {
  margin-left: 8px;
}

.strategy-name {
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
}

.strategy-desc {
  font-size: 12px;
  color: #8c8c8c;
}

.strategy-tip {
  margin-top: 16px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
