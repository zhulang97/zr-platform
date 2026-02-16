<template>
  <div class="step4-container">
    <div class="preview-header">
      <span class="title">数据预览与校验</span>
      <a-space>
        <a-tag v-if="validationResult" :color="validationResult.valid ? 'success' : 'error'">
          {{ validationResult.valid ? '校验通过' : '校验不通过' }}
        </a-tag>
        <span class="stats">
          共 {{ totalRows }} 条数据，
          <span class="success">有效 {{ validRows }}</span>
          <span v-if="invalidRows > 0" class="error">，无效 {{ invalidRows }}</span>
        </span>
      </a-space>
    </div>

    <a-alert
      v-if="validationResult && !validationResult.valid"
      type="error"
      show-icon
      class="error-alert"
    >
      <template #message>
        发现 {{ invalidRows }} 条数据存在问题，请检查后重试
      </template>
    </a-alert>

    <a-tabs v-model:activeKey="activeTab" class="preview-tabs">
      <a-tab-pane key="valid" :tab="`有效数据 (${validRows})`">
        <a-table
          :columns="previewColumns"
          :data-source="validData"
          :pagination="{ pageSize: 10 }"
          :scroll="{ x: 1200 }"
          size="small"
          :loading="loading"
          row-key="_rowId"
        />
      </a-tab-pane>
      
      <a-tab-pane key="invalid" :tab="`无效数据 (${invalidRows})`" v-if="invalidRows > 0">
        <a-table
          :columns="errorColumns"
          :data-source="invalidData"
          :pagination="{ pageSize: 10 }"
          :scroll="{ x: 1200 }"
          size="small"
          :loading="loading"
          row-key="_rowId"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'errors'">
              <a-tooltip v-for="(err, idx) in record._errors" :key="idx">
                <template #title>{{ err }}</template>
                <a-tag color="error" class="error-tag">{{ err.substring(0, 20) }}...</a-tag>
              </a-tooltip>
            </template>
          </template>
        </a-table>
      </a-tab-pane>
    </a-tabs>

    <div class="actions">
      <a-space>
        <a-button @click="$emit('prev')">上一步</a-button>
        <a-button v-if="invalidRows > 0" @click="exportErrors">
          <DownloadOutlined />
          导出错误数据
        </a-button>
      </a-space>
      <a-button 
        type="primary" 
        :disabled="!canProceed"
        @click="handleNext"
      >
        确认导入
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { DownloadOutlined } from '@ant-design/icons-vue'
import { http } from '@/api/http'

interface PreviewRow {
  _rowId: number
  _valid: boolean
  _errors?: string[]
  [key: string]: any
}

interface ValidationResult {
  valid: boolean
  totalRows: number
  validRows: number
  invalidRows: number
  errors: string[]
}

const props = defineProps<{
  batchId: number | null
  module: string
}>()

const emit = defineEmits<{
  'next': []
  'prev': []
}>()

const loading = ref(false)
const activeTab = ref('valid')
const previewData = ref<PreviewRow[]>([])
const validationResult = ref<ValidationResult | null>(null)
const previewColumns = ref<any[]>([])
const errorColumns = ref<any[]>([])

const totalRows = computed(() => validationResult.value?.totalRows || 0)
const validRows = computed(() => validationResult.value?.validRows || 0)
const invalidRows = computed(() => validationResult.value?.invalidRows || 0)
const validData = computed(() => previewData.value.filter(r => r._valid))
const invalidData = computed(() => previewData.value.filter(r => !r._valid))
const canProceed = computed(() => validationResult.value?.valid || validRows.value > 0)

const loadPreviewData = async () => {
  loading.value = true
  try {
    const resp = await http.get(`/api/import/batches/${props.batchId}/preview`)
    const data = resp.data.data
    
    // 从 validation 读取统计信息
    validationResult.value = data?.validation || null
    
    // 从 rows 读取详细数据
    previewData.value = data?.rows || []
    
    if (data?.columns) {
      previewColumns.value = data.columns.map((col: string) => ({
        title: col,
        dataIndex: col,
        key: col,
        ellipsis: true,
        width: 150
      }))
      errorColumns.value = [
        ...previewColumns.value,
        { title: '错误信息', key: 'errors', width: 200, fixed: 'right' }
      ]
    } else if (previewData.value.length > 0) {
      // 从数据推断列名
      const firstRow = previewData.value[0]
      const cols = Object.keys(firstRow).filter(k => !k.startsWith('_'))
      previewColumns.value = cols.map(col => ({
        title: col,
        dataIndex: col,
        key: col,
        ellipsis: true,
        width: 150
      }))
      errorColumns.value = [
        ...previewColumns.value,
        { title: '错误信息', key: 'errors', width: 200, fixed: 'right' }
      ]
    }
  } catch (e) {
    console.error('Preview load failed:', e)
    message.error('加载预览数据失败')
  } finally {
    loading.value = false
  }
}

const exportErrors = async () => {
  try {
    const resp = await http.get(`/api/import/batches/${props.batchId}/errors/export`, {
      responseType: 'blob'
    })
    const url = URL.createObjectURL(new Blob([resp.data]))
    const a = document.createElement('a')
    a.href = url
    a.download = `import_errors_${props.batchId}.xlsx`
    a.click()
    URL.revokeObjectURL(url)
    message.success('错误数据已导出')
  } catch {
    message.info('错误数据导出功能开发中')
  }
}

const handleNext = () => {
  if (!canProceed.value) {
    message.warning('没有有效数据可导入')
    return
  }
  emit('next')
}

onMounted(() => {
  loadPreviewData()
})
</script>

<style scoped>
.step4-container {
  max-width: 1200px;
  margin: 0 auto;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.preview-header .title {
  font-size: 15px;
  font-weight: 600;
}

.preview-header .stats {
  font-size: 13px;
  color: #666;
}

.preview-header .success {
  color: #52c41a;
}

.preview-header .error {
  color: #ff4d4f;
}

.error-alert {
  margin-bottom: 16px;
}

.preview-tabs {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.error-tag {
  margin-right: 4px;
  cursor: pointer;
}

.actions {
  display: flex;
  justify-content: space-between;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
