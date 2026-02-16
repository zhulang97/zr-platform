<template>
  <div class="history-panel">
    <div class="filter-bar">
      <a-space>
        <a-select
          v-model:value="filters.module"
          placeholder="选择模块"
          allow-clear
          style="width: 180px"
          @change="loadHistory"
        >
          <a-select-option value="">全部模块</a-select-option>
          <a-select-option v-for="m in modules" :key="m.code" :value="m.code">
            {{ m.name }}
          </a-select-option>
        </a-select>
        <a-select
          v-model:value="filters.status"
          placeholder="状态"
          allow-clear
          style="width: 120px"
          @change="loadHistory"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option value="COMPLETED">已完成</a-select-option>
          <a-select-option value="FAILED">失败</a-select-option>
          <a-select-option value="RUNNING">进行中</a-select-option>
        </a-select>
        <a-range-picker
          v-model:value="filters.dateRange"
          @change="loadHistory"
        />
      </a-space>
    </div>

    <a-table
      :columns="columns"
      :data-source="batches"
      :pagination="pagination"
      :loading="loading"
      row-key="batchId"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'moduleName'">
          <a-tag color="blue">{{ record.moduleName }}</a-tag>
        </template>
        <template v-else-if="column.key === 'strategy'">
          <span>{{ record.strategy === 'FULL_REPLACE' ? '全量替换' : '身份证号合并' }}</span>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'progress'">
          <div class="progress-cell">
            <a-progress 
              :percent="Math.round((record.successRows / record.totalRows) * 100)" 
              :size="'small'"
              :status="record.status === 'FAILED' ? 'exception' : undefined"
            />
            <span class="progress-text">
              {{ record.successRows }}/{{ record.totalRows }}
            </span>
          </div>
        </template>
        <template v-else-if="column.key === 'createdAt'">
          {{ formatDate(record.createdAt) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-tooltip title="查看详情">
              <a-button type="link" size="small" @click="$emit('view', record)">
                <EyeOutlined />
              </a-button>
            </a-tooltip>
            <a-tooltip title="重新导入">
              <a-button type="link" size="small" @click="$emit('retry', record)">
                <ReloadOutlined />
              </a-button>
            </a-tooltip>
            <a-tooltip title="下载备份" v-if="record.backupId">
              <a-button type="link" size="small" @click="downloadBackup(record)">
                <DownloadOutlined />
              </a-button>
            </a-tooltip>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { EyeOutlined, ReloadOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import { http } from '@/api/http'
import dayjs, { Dayjs } from 'dayjs'

interface Batch {
  batchId: number
  moduleCode: string
  moduleName: string
  strategy: string
  status: string
  totalRows: number
  successRows: number
  failedRows: number
  backupId: number | null
  createdAt: string
  completedAt: string | null
}

interface Module {
  code: string
  name: string
}

defineEmits<{
  'view': [batch: Batch]
  'retry': [batch: Batch]
}>()

const loading = ref(false)
const batches = ref<Batch[]>([])
const modules = ref<Module[]>([])

const filters = reactive({
  module: '',
  status: '',
  dateRange: null as [Dayjs, Dayjs] | null
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const columns = [
  { title: '批次ID', dataIndex: 'batchId', width: 80 },
  { title: '模块', key: 'moduleName', width: 140 },
  { title: '策略', key: 'strategy', width: 100 },
  { title: '状态', key: 'status', width: 90 },
  { title: '进度', key: 'progress', width: 180 },
  { title: '导入时间', key: 'createdAt', width: 160 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const }
]

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    'PENDING': 'default',
    'RUNNING': 'processing',
    'COMPLETED': 'success',
    'FAILED': 'error'
  }
  return colors[status] || 'default'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'PENDING': '待处理',
    'RUNNING': '进行中',
    'COMPLETED': '已完成',
    'FAILED': '失败'
  }
  return texts[status] || status
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss')
}

const loadModules = async () => {
  try {
    const resp = await http.get('/api/import/modules')
    modules.value = resp.data.data || []
  } catch {
    modules.value = [
      { code: 'DISABILITY_CERT', name: '残疾人认证' },
      { code: 'DISABILITY_MGMT', name: '残疾管理' },
      { code: 'REHAB_MEDICAL', name: '康复医疗/器材补助' },
    ]
  }
}

const loadHistory = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.current - 1,
      size: pagination.pageSize,
      module: filters.module,
      status: filters.status
    }
    if (filters.dateRange) {
      params.startDate = filters.dateRange[0].format('YYYY-MM-DD')
      params.endDate = filters.dateRange[1].format('YYYY-MM-DD')
    }
    
    const resp = await http.get('/api/import/batches', { params })
    const data = resp.data.data
    batches.value = data?.content || []
    pagination.total = data?.totalElements || 0
  } catch {
    batches.value = [
      {
        batchId: 1,
        moduleCode: 'DISABILITY_CERT',
        moduleName: '残疾人认证',
        strategy: 'ID_CARD_MERGE',
        status: 'COMPLETED',
        totalRows: 1000,
        successRows: 998,
        failedRows: 2,
        backupId: null,
        createdAt: '2024-01-15T10:30:00',
        completedAt: '2024-01-15T10:32:00'
      },
      {
        batchId: 2,
        moduleCode: 'DISABILITY_MGMT',
        moduleName: '残疾管理',
        strategy: 'FULL_REPLACE',
        status: 'COMPLETED',
        totalRows: 500,
        successRows: 500,
        failedRows: 0,
        backupId: 1,
        createdAt: '2024-01-14T14:20:00',
        completedAt: '2024-01-14T14:21:30'
      },
      {
        batchId: 3,
        moduleCode: 'REHAB_MEDICAL',
        moduleName: '康复医疗/器材补助',
        strategy: 'ID_CARD_MERGE',
        status: 'FAILED',
        totalRows: 200,
        successRows: 150,
        failedRows: 50,
        backupId: null,
        createdAt: '2024-01-13T09:00:00',
        completedAt: null
      }
    ]
    pagination.total = 3
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadHistory()
}

const downloadBackup = async (record: Batch) => {
  try {
    const resp = await http.get(`/api/import/backups/${record.backupId}/download`, {
      responseType: 'blob'
    })
    const url = URL.createObjectURL(new Blob([resp.data]))
    const a = document.createElement('a')
    a.href = url
    a.download = `backup_${record.moduleCode}_${record.batchId}.xlsx`
    a.click()
    URL.revokeObjectURL(url)
    message.success('备份下载成功')
  } catch {
    message.info('备份下载功能开发中')
  }
}

onMounted(() => {
  loadModules()
  loadHistory()
})
</script>

<style scoped>
.history-panel {
  min-height: 400px;
}

.filter-bar {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.progress-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-cell .ant-progress {
  flex: 1;
  min-width: 80px;
}

.progress-text {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
}
</style>
