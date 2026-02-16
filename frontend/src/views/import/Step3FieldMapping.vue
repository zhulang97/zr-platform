<template>
  <div class="step3-container">
    <div class="mapping-header">
      <span class="title">字段映射</span>
      <a-space>
        <a-button @click="autoMap" :loading="autoMapping">
          <ThunderboltOutlined />
          自动匹配
        </a-button>
        <a-button type="primary" ghost @click="downloadTemplate">
          <DownloadOutlined />
          下载模板
        </a-button>
      </a-space>
    </div>

    <a-alert
      v-if="missingRequired.length > 0"
      type="warning"
      show-icon
      class="missing-alert"
    >
      <template #message>
        必填字段未映射: {{ missingRequired.join(', ') }}
      </template>
    </a-alert>

    <div class="mapping-table">
      <div class="table-header">
        <div class="col col-excel">Excel列名</div>
        <div class="col col-sample">示例数据</div>
        <div class="col col-system">系统字段</div>
        <div class="col col-status">状态</div>
      </div>
      
      <div class="table-body">
        <div 
          v-for="mapping in mappings" 
          :key="mapping.excelColumn" 
          class="table-row"
        >
          <div class="col col-excel">
            <FileExcelOutlined class="excel-icon" />
            {{ mapping.excelColumn }}
          </div>
          <div class="col col-sample">
            <span v-for="(val, idx) in mapping.sampleValues" :key="idx" class="sample-tag">
              {{ val }}
            </span>
          </div>
          <div class="col col-system">
            <a-select
              v-model:value="mapping.systemField"
              placeholder="选择系统字段"
              allow-clear
              show-search
              :filter-option="filterOption"
              class="field-select"
              @change="onMappingChange(mapping)"
            >
              <a-select-option 
                v-for="field in systemFields" 
                :key="field.fieldCode"
                :value="field.fieldCode"
              >
                {{ field.fieldName }}
                <span v-if="field.required" class="required-tag">必填</span>
              </a-select-option>
            </a-select>
          </div>
          <div class="col col-status">
            <a-tag v-if="mapping.matched" color="success">已匹配</a-tag>
            <a-tag v-else-if="mapping.systemField" color="processing">手动映射</a-tag>
            <a-tag v-else color="default">未映射</a-tag>
          </div>
        </div>
      </div>
    </div>

    <div class="actions">
      <a-button @click="$emit('prev')">上一步</a-button>
      <a-button type="primary" @click="handleNext">
        下一步
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { ThunderboltOutlined, DownloadOutlined, FileExcelOutlined } from '@ant-design/icons-vue'
import { http } from '@/api/http'

interface Mapping {
  excelColumn: string
  systemField: string
  fieldName: string
  matched: boolean
  confidence: number
  sampleValues: string[]
}

interface SystemField {
  fieldCode: string
  fieldName: string
  dataType: string
  required: boolean
}

const props = defineProps<{
  batchId: number | null
  module: string
}>()

const emit = defineEmits<{
  'next': []
  'prev': []
}>()

const mappings = ref<Mapping[]>([])
const systemFields = ref<SystemField[]>([])
const autoMapping = ref(false)
const missingRequired = computed(() => {
  const requiredFields = systemFields.value.filter(f => f.required).map(f => f.fieldName)
  const mappedFields = mappings.value.filter(m => m.systemField).map(m => {
    const field = systemFields.value.find(f => f.fieldCode === m.systemField)
    return field?.fieldName
  })
  return requiredFields.filter(f => !mappedFields.includes(f))
})

const filterOption = (input: string, option: any) => {
  return option.children[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

const loadMappingData = async () => {
  try {
    const resp = await http.get(`/api/import/batches/${props.batchId}/mapping`)
    mappings.value = resp.data.data?.mappings || []
    systemFields.value = resp.data.data?.systemFields || []
  } catch {
    // 模拟数据
    mappings.value = [
      { excelColumn: '姓名', systemField: '', fieldName: '', matched: false, confidence: 0, sampleValues: ['张三', '李四', '王五'] },
      { excelColumn: '证件号码', systemField: '', fieldName: '', matched: false, confidence: 0, sampleValues: ['310105199001011234', '310105199002022345'] },
      { excelColumn: '性别', systemField: '', fieldName: '', matched: false, confidence: 0, sampleValues: ['男', '女', '男'] },
    ]
    systemFields.value = [
      { fieldCode: 'name', fieldName: '姓名', dataType: 'STRING', required: true },
      { fieldCode: 'id_card', fieldName: '证件号', dataType: 'STRING', required: true },
      { fieldCode: 'gender', fieldName: '性别', dataType: 'STRING', required: false },
    ]
  }
}

const autoMap = async () => {
  autoMapping.value = true
  try {
    await http.post(`/api/import/batches/${props.batchId}/auto-map`)
    await loadMappingData()
    message.success('自动匹配完成')
  } catch {
    // 模拟自动匹配
    mappings.value.forEach(m => {
      const match = systemFields.value.find(f => 
        f.fieldName === m.excelColumn || 
        f.fieldCode.toLowerCase() === m.excelColumn.toLowerCase()
      )
      if (match) {
        m.systemField = match.fieldCode
        m.fieldName = match.fieldName
        m.matched = true
      }
    })
    message.success('自动匹配完成')
  } finally {
    autoMapping.value = false
  }
}

const onMappingChange = (mapping: Mapping) => {
  const field = systemFields.value.find(f => f.fieldCode === mapping.systemField)
  mapping.fieldName = field?.fieldName || ''
  mapping.matched = false
}

const downloadTemplate = async () => {
  try {
    const resp = await http.get(`/api/import/modules/${props.module}/template`, { responseType: 'blob' })
    const url = URL.createObjectURL(new Blob([resp.data]))
    const a = document.createElement('a')
    a.href = url
    a.download = `${props.module}_template.xlsx`
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    message.info('模板下载功能开发中')
  }
}

const handleNext = () => {
  if (missingRequired.value.length > 0) {
    message.warning('请先映射必填字段')
    return
  }
  emit('next')
}

onMounted(() => {
  loadMappingData()
})
</script>

<style scoped>
.step3-container {
  max-width: 1000px;
  margin: 0 auto;
}

.mapping-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.mapping-header .title {
  font-size: 15px;
  font-weight: 600;
}

.missing-alert {
  margin-bottom: 16px;
}

.mapping-table {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.table-header {
  display: flex;
  background: #fafafa;
  padding: 12px 16px;
  font-weight: 500;
  border-bottom: 1px solid #e8e8e8;
}

.table-row {
  display: flex;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  align-items: center;
}

.table-row:last-child {
  border-bottom: none;
}

.table-row:hover {
  background: #fafafa;
}

.col {
  padding: 0 8px;
}

.col-excel { width: 20%; display: flex; align-items: center; gap: 8px; }
.col-sample { width: 35%; }
.col-system { width: 30%; }
.col-status { width: 15%; }

.excel-icon { color: #52c41a; }

.sample-tag {
  display: inline-block;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 4px;
  color: #666;
}

.field-select {
  width: 100%;
}

.required-tag {
  font-size: 10px;
  color: #ff4d4f;
  margin-left: 4px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
