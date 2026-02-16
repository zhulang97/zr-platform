<template>
  <div class="history-detail">
    <div class="detail-header">
      <a-space>
        <a-button @click="goBack">
          <LeftOutlined />
          返回
        </a-button>
      </a-space>
      <span class="title">导入详情</span>
      <div></div>
    </div>

    <a-spin :spinning="loading">
      <template v-if="batch">
        <a-card class="info-card" size="small">
          <a-descriptions :column="3" bordered size="small">
            <a-descriptions-item label="批次ID">{{ batch.batchId }}</a-descriptions-item>
            <a-descriptions-item label="模块">{{ batch.moduleName || batch.moduleCode }}</a-descriptions-item>
            <a-descriptions-item label="策略">{{ batch.strategy === 'FULL_REPLACE' ? '全量替换' : '身份证号合并' }}</a-descriptions-item>
            <a-descriptions-item label="文件名">{{ batch.fileName }}</a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="getStatusColor(batch.status)">{{ getStatusText(batch.status) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="导入时间">{{ formatDate(batch.createdAt) }}</a-descriptions-item>
            <a-descriptions-item label="总条数">{{ batch.totalRows }}</a-descriptions-item>
            <a-descriptions-item label="成功">
              <span style="color: #52c41a">{{ batch.successRows }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="失败">
              <span :style="{ color: batch.failedRows > 0 ? '#ff4d4f' : '#999' }">{{ batch.failedRows }}</span>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card class="rows-card" size="small">
          <template #title>
            <div class="card-title">
              <span>数据明细</span>
              <a-space>
                <a-select v-model:value="rowStatusFilter" style="width: 120px" @change="loadRows">
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="SUCCESS">成功</a-select-option>
                  <a-select-option value="ERROR">失败</a-select-option>
                </a-select>
                <a-button v-if="batch.failedRows > 0" @click="exportErrors">
                  <DownloadOutlined />
                  导出失败数据
                </a-button>
              </a-space>
            </div>
          </template>

          <a-table
            :columns="dynamicColumns"
            :data-source="rows"
            :pagination="rowPagination"
            :loading="rowsLoading"
            row-key="rowId"
            :scroll="tableScroll"
            @change="handleRowTableChange"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="getRowStatusColor(record.validateStatus)">
                  {{ getRowStatusText(record.validateStatus) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'error'">
                <span class="error-text">{{ record.errorMsg || '-' }}</span>
              </template>
              <template v-else-if="column.dataIndex && column.dataIndex !== 'rowNo'">
                <span class="data-cell">{{ record.rawData?.[column.dataIndex] || '-' }}</span>
              </template>
            </template>
          </a-table>
        </a-card>
      </template>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { LeftOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import { http } from '@/api/http'
import dayjs from 'dayjs'

interface Props {
  batchId: number
  onBack: () => void
}

const props = defineProps<Props>()

const loading = ref(false)
const rowsLoading = ref(false)
const batch = ref<any>(null)
const rows = ref<any[]>([])
const rowStatusFilter = ref('')
const allHeaders = ref<string[]>([])

const rowPagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const dynamicColumns = computed(() => {
  const cols: any[] = [
    { title: '行号', dataIndex: 'rowNo', width: 80, fixed: 'left' as const },
    { title: '状态', key: 'status', width: 100, fixed: 'left' as const }
  ]
  
  allHeaders.value.forEach(header => {
    cols.push({
      title: header,
      dataIndex: header,
      width: 150,
      ellipsis: true
    })
  })
  
  cols.push({
    title: '错误信息',
    key: 'error',
    width: 250,
    fixed: 'right' as const
  })
  
  return cols
})

const tableScroll = computed(() => {
  let totalWidth = 180 + 250
  allHeaders.value.forEach(() => {
    totalWidth += 150
  })
  return { x: Math.max(totalWidth, 1200) }
})

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    'PENDING': 'default',
    'UPLOADED': 'default',
    'MAPPED': 'blue',
    'VALIDATED': 'blue',
    'RUNNING': 'processing',
    'COMPLETED': 'success',
    'FAILED': 'error'
  }
  return colors[status] || 'default'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'PENDING': '待处理',
    'UPLOADED': '已上传',
    'MAPPED': '已映射',
    'VALIDATED': '已验证',
    'RUNNING': '进行中',
    'COMPLETED': '已完成',
    'FAILED': '失败'
  }
  return texts[status] || status
}

const getRowStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    'SUCCESS': 'success',
    'ERROR': 'error',
    'PENDING': 'default'
  }
  return colors[status] || 'default'
}

const getRowStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'SUCCESS': '成功',
    'ERROR': '失败',
    'PENDING': '待验证'
  }
  return texts[status] || status
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss')
}

const loadBatch = async () => {
  loading.value = true
  try {
    const resp = await http.get(`/api/import/batches/${props.batchId}`)
    batch.value = resp.data.data
  } catch {
    message.error('加载批次信息失败')
  } finally {
    loading.value = false
  }
}

const loadRows = async () => {
  rowsLoading.value = true
  try {
    const resp = await http.get(`/api/import/batches/${props.batchId}/rows`, {
      params: {
        page: rowPagination.current - 1,
        size: rowPagination.pageSize,
        status: rowStatusFilter.value
      }
    })
    const data = resp.data.data
    rows.value = data?.content || []
    rowPagination.total = data?.totalElements || 0
    
    if (rows.value.length > 0) {
      const headers = new Set<string>()
      rows.value.forEach(row => {
        if (row.rawData) {
          Object.keys(row.rawData).forEach(key => {
            headers.add(key)
          })
        }
      })
      allHeaders.value = Array.from(headers)
    }
  } catch {
    message.error('加载数据明细失败')
  } finally {
    rowsLoading.value = false
  }
}

const handleRowTableChange = (pag: any) => {
  rowPagination.current = pag.current
  rowPagination.pageSize = pag.pageSize
  loadRows()
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
    message.success('失败数据导出成功')
  } catch {
    message.error('导出失败')
  }
}

const goBack = () => {
  props.onBack()
}

onMounted(() => {
  loadBatch()
  loadRows()
})
</script>

<style scoped>
.history-detail {
  padding: 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 24px;
  background: #fafafa;
  border-radius: 8px;
}

.detail-header .title {
  font-size: 16px;
  font-weight: 600;
}

.info-card {
  margin-bottom: 16px;
}

.rows-card {
  margin-bottom: 16px;
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.raw-data {
  max-height: 400px;
  overflow-y: auto;
  font-size: 12px;
  margin: 0;
}

.error-text {
  color: #ff4d4f;
  font-size: 12px;
  word-break: break-all;
}

.data-cell {
  font-size: 12px;
  color: #333;
}
</style>
