<template>
  <a-card title="数据导入与治理">
    <a-space direction="vertical" style="width: 100%;" :size="16">
      <a-card size="small" title="导入操作">
        <a-space>
          <a-select v-model:value="importType" style="width: 200px;" :options="importTypeOptions" />
          <a-button @click="downloadTemplate" :loading="templateLoading">下载模板</a-button>
          <a-upload
            :customRequest="handleUpload"
            :showUploadList="false"
            accept=".xlsx,.xls,.csv"
          >
            <a-button type="primary" :loading="uploading">上传文件</a-button>
          </a-upload>
        </a-space>
      </a-card>

      <a-card size="small" title="导入批次" v-if="batches.length > 0">
        <a-table :dataSource="batches" :columns="batchColumns" :pagination="false" rowKey="batchId">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'action'">
              <a-space>
                <a-button size="small" @click="validateBatch(record.batchId)">校验</a-button>
                <a-button 
                  size="small" 
                  type="primary" 
                  @click="showCommitModal(record.batchId)"
                  :disabled="record.status !== 'VALIDATED'"
                >
                  提交
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>

      <a-card size="small" title="校验结果" v-if="validationResult">
        <a-alert
          :type="validationResult.status === 'VALID' ? 'success' : 'error'"
          :message="`总计: ${validationResult.total} 条, 错误: ${validationResult.errors} 条`"
        />
        <a-list
          v-if="validationResult.errorMessages?.length > 0"
          size="small"
          :dataSource="validationResult.errorMessages"
          style="margin-top: 12px; maxHeight: 200px; overflow: auto;"
        >
          <template #renderItem="{ item }">
            <a-list-item>
              <a-typography-text type="danger">{{ item }}</a-typography-text>
            </a-list-item>
          </template>
        </a-list>
      </a-card>
    </a-space>

    <a-modal v-model:open="commitModalOpen" title="提交导入" @ok="commitBatch">
      <a-space direction="vertical" style="width: 100%;">
        <a-radio-group v-model:value="commitStrategy">
          <a-radio value="MARK_ERROR">标记错误并继续</a-radio>
          <a-radio value="SKIP">跳过错误</a-radio>
          <a-radio value="ABORT">遇到错误中止</a-radio>
        </a-radio-group>
      </a-space>
    </a-modal>
  </a-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadRequestOption } from 'ant-design-vue/es/upload/interface'
import { http } from '../api/http'

const importType = ref('person')
const templateLoading = ref(false)
const uploading = ref(false)
const batches = ref<any[]>([])
const validationResult = ref<any>(null)
const commitModalOpen = ref(false)
const commitBatchId = ref<number | null>(null)
const commitStrategy = ref('MARK_ERROR')

const importTypeOptions = [
  { label: '人员信息', value: 'person' },
  { label: '残疾证信息', value: 'disability_card' },
  { label: '补贴信息', value: 'benefit' }
]

const batchColumns = [
  { title: '批次ID', dataIndex: 'batchId' },
  { title: '类型', dataIndex: 'type' },
  { title: '文件名', dataIndex: 'fileName' },
  { title: '状态', dataIndex: 'status' },
  { title: '上传时间', dataIndex: 'createdAt' },
  { title: '操作', key: 'action' }
]

async function downloadTemplate() {
  templateLoading.value = true
  try {
    const resp = await http.get(`/api/import/templates/${importType.value}`)
    const { filename, content } = resp.data.data
    const blob = new Blob([content], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.click()
    URL.revokeObjectURL(url)
    message.success('模板已下载')
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? '下载失败')
  } finally {
    templateLoading.value = false
  }
}

async function handleUpload(options: UploadRequestOption) {
  const { file, onSuccess, onError } = options
  uploading.value = true
  
  const formData = new FormData()
  formData.append('type', importType.value)
  formData.append('file', file as Blob)
  
  try {
    const resp = await http.post('/api/import/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const { batchId } = resp.data.data
    message.success(`上传成功，批次ID: ${batchId}`)
    batches.value.unshift({
      batchId,
      type: importType.value,
      fileName: (file as File).name,
      status: 'UPLOADED',
      createdAt: new Date().toLocaleString()
    })
    onSuccess?.(resp.data)
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? '上传失败')
    onError?.(e)
  } finally {
    uploading.value = false
  }
}

async function validateBatch(batchId: number) {
  try {
    const resp = await http.get(`/api/import/${batchId}/validate`)
    validationResult.value = resp.data.data
    
    const batch = batches.value.find(b => b.batchId === batchId)
    if (batch) {
      batch.status = resp.data.data.status === 'VALID' ? 'VALIDATED' : 'INVALID'
    }
    
    message.success('校验完成')
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? '校验失败')
  }
}

function showCommitModal(batchId: number) {
  commitBatchId.value = batchId
  commitModalOpen.value = true
}

async function commitBatch() {
  if (!commitBatchId.value) return
  
  try {
    const resp = await http.post(`/api/import/${commitBatchId.value}/commit`, {
      strategy: commitStrategy.value
    })
    const result = resp.data.data
    message.success(`提交完成: 成功 ${result.success} 条, 失败 ${result.failed} 条`)
    
    const batch = batches.value.find(b => b.batchId === commitBatchId.value)
    if (batch) {
      batch.status = 'COMMITTED'
    }
    
    commitModalOpen.value = false
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? '提交失败')
  }
}

onMounted(() => {
  // 加载历史批次
})
</script>