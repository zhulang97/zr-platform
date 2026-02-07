<template>
  <a-card title="审计日志查询">
    <a-form layout="inline" style="margin-bottom: 16px;">
      <a-form-item label="用户ID">
        <a-input-number v-model:value="searchForm.userId" placeholder="用户ID" style="width: 120px;" />
      </a-form-item>
      <a-form-item label="操作">
        <a-input v-model:value="searchForm.actionLike" placeholder="操作" style="width: 150px;" />
      </a-form-item>
      <a-form-item label="资源">
        <a-input v-model:value="searchForm.resourceLike" placeholder="资源" style="width: 200px;" />
      </a-form-item>
      <a-form-item label="结果码">
        <a-input v-model:value="searchForm.resultCode" placeholder="结果码" style="width: 100px;" />
      </a-form-item>
      <a-form-item label="开始时间">
        <a-date-picker v-model:value="searchForm.startAt" show-time format="YYYY-MM-DD HH:mm:ss" style="width: 180px;" />
      </a-form-item>
      <a-form-item label="结束时间">
        <a-date-picker v-model:value="searchForm.endAt" show-time format="YYYY-MM-DD HH:mm:ss" style="width: 180px;" />
      </a-form-item>
      <a-form-item>
        <a-space>
          <a-button type="primary" @click="handleSearch" :loading="loading">查询</a-button>
          <a-button @click="handleReset">重置</a-button>
          <a-button @click="handleExport" :loading="exporting">导出</a-button>
        </a-space>
      </a-form-item>
    </a-form>

    <a-table
      :columns="columns"
      :data-source="logs"
      :loading="loading"
      :pagination="{ pageSize: 50 }"
      row-key="id"
      :scroll="{ y: 500 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'createdAt'">
          {{ formatTime(record.createdAt) }}
        </template>
        <template v-if="column.key === 'resultCode'">
          <a-tag :color="record.resultCode === 'OK' ? 'green' : 'red'">{{ record.resultCode }}</a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-button type="link" size="small" @click="handleViewDetail(record)">详情</a-button>
        </template>
      </template>
    </a-table>

    <a-modal
      v-model:open="detailModalOpen"
      title="审计日志详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions bordered :column="1" v-if="currentLog">
        <a-descriptions-item label="日志ID">{{ currentLog.id }}</a-descriptions-item>
        <a-descriptions-item label="用户ID">{{ currentLog.userId }}</a-descriptions-item>
        <a-descriptions-item label="操作">{{ currentLog.action }}</a-descriptions-item>
        <a-descriptions-item label="资源">{{ currentLog.resource }}</a-descriptions-item>
        <a-descriptions-item label="结果码">
          <a-tag :color="currentLog.resultCode === 'OK' ? 'green' : 'red'">{{ currentLog.resultCode }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="耗时(ms)">{{ currentLog.costMs }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ formatTime(currentLog.createdAt) }}</a-descriptions-item>
        <a-descriptions-item label="IP地址">{{ currentLog.ip }}</a-descriptions-item>
        <a-descriptions-item label="User Agent">{{ currentLog.ua }}</a-descriptions-item>
        <a-descriptions-item label="请求内容">
          <pre style="max-height: 300px; overflow: auto;">{{ currentLog.requestClob }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </a-card>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

interface AuditLog {
  id: number
  userId: number
  action: string
  resource?: string
  requestClob?: string
  resultCode: string
  costMs?: number
  createdAt: string
  ip?: string
  ua?: string
}

const logs = ref<AuditLog[]>([])
const loading = ref(false)
const exporting = ref(false)
const detailModalOpen = ref(false)
const currentLog = ref<AuditLog | null>(null)

const searchForm = reactive({
  userId: undefined as number | undefined,
  actionLike: '',
  resourceLike: '',
  resultCode: '',
  startAt: null as any,
  endAt: null as any
})

const columns = [
  { title: '日志ID', dataIndex: 'id', key: 'id', width: 100 },
  { title: '用户ID', dataIndex: 'userId', key: 'userId', width: 100 },
  { title: '操作', dataIndex: 'action', key: 'action', width: 120 },
  { title: '资源', dataIndex: 'resource', key: 'resource', width: 200 },
  { title: '结果码', dataIndex: 'resultCode', key: 'resultCode', width: 100 },
  { title: '耗时(ms)', dataIndex: 'costMs', key: 'costMs', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 160 },
  { title: 'IP', dataIndex: 'ip', key: 'ip', width: 120 },
  { title: '操作', key: 'action', width: 80 }
]

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const handleSearch = async () => {
  loading.value = true
  try {
    const params = {
      userId: searchForm.userId,
      actionLike: searchForm.actionLike || undefined,
      resourceLike: searchForm.resourceLike || undefined,
      resultCode: searchForm.resultCode || undefined,
      startAt: searchForm.startAt ? searchForm.startAt.toISOString() : undefined,
      endAt: searchForm.endAt ? searchForm.endAt.toISOString() : undefined
    }

    const res = await fetch('/api/audit/search', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(params)
    })
    const data = await res.json()
    if (data.code === 0) {
      logs.value = data.data
    } else {
      message.error(data.message || '查询失败')
    }
  } catch (e) {
    message.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.userId = undefined
  searchForm.actionLike = ''
  searchForm.resourceLike = ''
  searchForm.resultCode = ''
  searchForm.startAt = null
  searchForm.endAt = null
  handleSearch()
}

const handleViewDetail = (record: AuditLog) => {
  currentLog.value = record
  detailModalOpen.value = true
}

const handleExport = async () => {
  try {
    exporting.value = true
    const params = {
      userId: searchForm.userId,
      actionLike: searchForm.actionLike || undefined,
      resourceLike: searchForm.resourceLike || undefined,
      resultCode: searchForm.resultCode || undefined,
      startAt: searchForm.startAt ? searchForm.startAt.toISOString() : undefined,
      endAt: searchForm.endAt ? searchForm.endAt.toISOString() : undefined
    }

    const res = await fetch('/api/audit/search', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(params)
    })
    const data = await res.json()
    if (data.code === 0) {
      const csv = convertToCSV(data.data)
      downloadCSV(csv, 'audit_logs.csv')
      message.success('导出成功')
    } else {
      message.error(data.message || '导出失败')
    }
  } catch (e) {
    message.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const convertToCSV = (data: AuditLog[]) => {
  const headers = ['ID', '用户ID', '操作', '资源', '结果码', '耗时', '创建时间', 'IP']
  const rows = data.map(log => [
    log.id,
    log.userId,
    log.action,
    log.resource || '',
    log.resultCode,
    log.costMs || 0,
    formatTime(log.createdAt),
    log.ip || ''
  ])
  return [headers, ...rows].map(row => row.join(',')).join('\n')
}

const downloadCSV = (csv: string, filename: string) => {
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename
  link.click()
}
</script>
