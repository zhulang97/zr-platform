<template>
  <a-card title="异常管理">
    <a-space style="margin-bottom: 16px;">
      <a-form layout="inline">
        <a-form-item label="异常类型">
          <a-select v-model:value="filters.anomalyType" placeholder="全部" style="width: 150px;" allow-clear>
            <a-select-option value="PERSON_CAR_SEPARATION">人车分离</a-select-option>
            <a-select-option value="NO_CARD_SUBSIDY">无证补贴</a-select-option>
            <a-select-option value="CANCELLED_CARD_SUBSIDY">注销仍补贴</a-select-option>
            <a-select-option value="DUPLICATE_SUBSIDY">重复补贴</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="filters.status" placeholder="全部" style="width: 120px;" allow-clear>
            <a-select-option value="UNHANDLED">未处理</a-select-option>
            <a-select-option value="VERIFIED">已核实</a-select-option>
            <a-select-option value="HANDLED">已处理</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="姓名">
          <a-input v-model:value="filters.nameLike" placeholder="模糊搜索" style="width: 150px;" />
        </a-form-item>
        <a-form-item label="身份证号">
          <a-input v-model:value="filters.idNo" placeholder="精确搜索" style="width: 150px;" />
        </a-form-item>
        <a-form-item label="残疾人证号">
          <a-input v-model:value="filters.disabilityCardNo" placeholder="模糊搜索" style="width: 150px;" />
        </a-form-item>
        <a-space>
          <a-button type="primary" @click="handleSearch">查询</a-button>
          <a-button @click="handleReset">重置</a-button>
          <a-button @click="handleExport" :loading="exporting">导出</a-button>
        </a-space>
      </a-form>
    </a-space>

    <a-table
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      rowKey="anomalyId"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'anomalyType'">
          <a-tag :color="getAnomalyTypeColor(record.anomalyType)">
            {{ getAnomalyTypeText(record.anomalyType) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'severity'">
          <a-tag :color="getSeverityColor(record.severity)">
            {{ getSeverityText(record.severity) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleDetail(record)">详情</a-button>
            <a-button
              v-if="record.status === 'UNHANDLED'"
              type="primary"
              size="small"
              @click="handleHandle(record)"
            >处理</a-button>
            <a-button
              v-else
              type="primary"
              size="small"
              @click="handleHandle(record)"
            >复核</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal
      v-model:open="detailModalOpen"
      title="异常详情"
      width="700px"
      :footer="null"
    >
      <a-spin :spinning="detailLoading">
        <a-descriptions bordered :column="1" v-if="currentAnomaly">
          <a-descriptions-item label="异常ID">{{ currentAnomaly.anomalyId }}</a-descriptions-item>
          <a-descriptions-item label="人员ID">{{ currentAnomaly.personId }}</a-descriptions-item>
          <a-descriptions-item label="姓名">{{ currentAnomaly.personName ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="身份证号">{{ currentAnomaly.personIdNo ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="残疾人证号">{{ currentAnomaly.disabilityCardNo ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="异常类型">
            <a-tag :color="getAnomalyTypeColor(currentAnomaly.anomalyType)">
              {{ getAnomalyTypeText(currentAnomaly.anomalyType) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="严重程度">
            <a-tag :color="getSeverityColor(currentAnomaly.severity)">
              {{ getSeverityText(currentAnomaly.severity) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(currentAnomaly.status)">
              {{ getStatusText(currentAnomaly.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="命中时间">{{ formatTime(currentAnomaly.hitTime) }}</a-descriptions-item>
          <a-descriptions-item label="处理说明">{{ currentAnomaly.handleNote ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="处理时间">{{ currentAnomaly.handledAt ? formatTime(currentAnomaly.handledAt) : '-' }}</a-descriptions-item>
          <a-descriptions-item label="处理人">{{ currentAnomaly.handlerName ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ formatTime(currentAnomaly.createdAt) }}</a-descriptions-item>
        </a-descriptions>

        <a-divider />
        <a-card size="small" title="命中证据" style="margin-top: 12px;">
          <pre style="margin:0; white-space: pre-wrap; word-break: break-word; max-height: 200px; overflow: auto;">
            {{ formatJson(currentAnomaly.snapshotJson) }}
          </pre>
        </a-card>

        <a-divider v-if="currentAnomaly.status === 'UNHANDLED'" style="margin-top: 12px;" />
        <div v-if="currentAnomaly.status === 'UNHANDLED'" v-perm="'anomaly:handle'">
          <a-form :model="handleForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
            <a-form-item label="处理结果" name="status" :rules="[{ required: true, message: '请选择处理结果' }]">
              <a-radio-group v-model:value="handleForm.status">
                <a-radio value="VERIFIED">已核实</a-radio>
                <a-radio value="HANDLED">已处理</a-radio>
              </a-radio-group>
            </a-form-item>
            <a-form-item label="处理说明" name="note" :rules="[{ required: true, message: '请输入处理说明' }]">
              <a-textarea v-model:value="handleForm.note" placeholder="请描述处理情况" :rows="3" />
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="handleStatusSubmit" :loading="submitting">
                  提交
                </a-button>
                <a-button @click="detailModalOpen = false">关闭</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </div>
      </a-spin>
    </a-modal>
  </a-card>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { http } from '../api/http'
import dayjs from 'dayjs'

interface AnomalyRow {
  anomalyId: number
  personId: number
  personName: string
  personIdNo: string
  disabilityCardNo: string
  anomalyType: string
  severity: number
  status: string
  hitTime: string
  handleNote: string
  handledAt: string
  handlerName: string
  handlerUserId: number
  snapshotJson: string
  createdAt: string
}

const loading = ref(false)
const exporting = ref(false)
const detailModalOpen = ref(false)
const detailLoading = ref(false)
const currentAnomaly = ref<AnomalyRow | null>(null)
const submitting = ref(false)

const filters = reactive({
  nameLike: '',
  idNo: '',
  disabilityCardNo: '',
  anomalyType: '',
  status: '',
  pageNo: 1,
  pageSize: 20
})

const handleForm = reactive({
  status: 'VERIFIED',
  note: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showTotal: true
})

const columns = [
  { title: '异常ID', dataIndex: 'anomalyId', key: 'anomalyId', width: 100 },
  { title: '人员ID', dataIndex: 'personId', key: 'personId', width: 100 },
  { title: '姓名', dataIndex: 'personName', key: 'personName', width: 120 },
  { title: '身份证号', dataIndex: 'personIdNo', key: 'personIdNo', width: 150 },
  { title: '残疾人证号', dataIndex: 'disabilityCardNo', key: 'disabilityCardNo', width: 150 },
  { title: '异常类型', dataIndex: 'anomalyType', key: 'anomalyType', width: 150 },
  { title: '严重程度', dataIndex: 'severity', key: 'severity', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '命中时间', dataIndex: 'hitTime', key: 'hitTime', width: 160 },
  { title: '操作', key: 'action', width: 180 }
]

function getAnomalyTypeColor(type: string) {
  const colors: Record<string, string> = {
    PERSON_CAR_SEPARATION: 'red',
    NO_CARD_SUBSIDY: 'orange',
    CANCELLED_CARD_SUBSIDY: 'volcano',
    DUPLICATE_SUBSIDY: 'magenta'
  }
  return colors[type] || 'default'
}

function getAnomalyTypeText(type: string) {
  const texts: Record<string, string> = {
    PERSON_CAR_SEPARATION: '人车分离',
    NO_CARD_SUBSIDY: '无证补贴',
    CANCELLED_CARD_SUBSIDY: '注销仍补贴',
    DUPLICATE_SUBSIDY: '重复补贴'
  }
  return texts[type] || type
}

function getStatusColor(status: string) {
  const colors: Record<string, string> = {
    UNHANDLED: 'red',
    VERIFIED: 'orange',
    HANDLED: 'green'
  }
  return colors[status] || 'default'
}

function getStatusText(status: string) {
  const texts: Record<string, string> = {
    UNHANDLED: '未处理',
    VERIFIED: '已核实',
    HANDLED: '已处理'
  }
  return texts[status] || status
}

function getSeverityColor(severity: number) {
  const colors: Record<number, string> = {
    1: 'red',
    2: 'orange',
    3: 'blue'
  }
  return colors[severity] || 'default'
}

function getSeverityText(severity: number) {
  const texts: Record<number, string> = {
    1: '高',
    2: '中',
    3: '低'
  }
  return texts[severity] || '未知'
}

function formatTime(time: string) {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

function formatJson(json: string) {
  if (!json) return '-'
  try {
    const obj = JSON.parse(json)
    return JSON.stringify(obj, null, 2)
  } catch {
    return json
  }
}

async function load() {
  loading.value = true
  try {
    const req = {
      nameLike: filters.nameLike || undefined,
      idNo: filters.idNo || undefined,
      disabilityCardNo: filters.disabilityCardNo || undefined,
      anomalyType: filters.anomalyType || undefined,
      status: filters.status || undefined,
      pageNo: filters.pageNo,
      pageSize: filters.pageSize
    }
    const resp = await http.post('/api/anomalies/search', req)
    rows.value = resp.data.data.items || []
    pagination.total = resp.data.data.total || 0
    pagination.current = filters.pageNo
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

function handleReset() {
  filters.nameLike = ''
  filters.idNo = ''
  filters.disabilityCardNo = ''
  filters.anomalyType = ''
  filters.status = ''
  filters.pageNo = 1
  load()
}

async function handleSearch() {
  filters.pageNo = 1
  load()
}

async function handleDetail(record: AnomalyRow) {
  detailModalOpen.value = true
  detailLoading.value = true
  handleForm.status = 'VERIFIED'
  handleForm.note = ''
  try {
    const resp = await http.get(`/api/anomalies/${record.anomalyId}`)
    currentAnomaly.value = resp.data.data
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? '加载异常详情失败')
  } finally {
    detailLoading.value = false
  }
}

async function handleHandle(record: AnomalyRow) {
  detailModalOpen.value = true
  detailLoading.value = true
  handleForm.status = record.status === 'UNHANDLED' ? 'VERIFIED' : 'HANDLED'
  handleForm.note = ''
  try {
    const resp = await http.get(`/api/anomalies/${record.anomalyId}`)
    currentAnomaly.value = resp.data.data
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? '加载异常详情失败')
  } finally {
    detailLoading.value = false
  }
}

async function handleStatusSubmit() {
  if (!currentAnomaly.value) return
  submitting.value = true
  try {
    await http.post(`/api/anomalies/${currentAnomaly.value.anomalyId}/status`, {
      status: handleForm.status,
      note: handleForm.note
    })
    message.success('提交成功')
    detailModalOpen.value = false
    await load()
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? '提交失败')
  } finally {
    submitting.value = false
  }
}

async function handleExport() {
  exporting.value = true
  try {
    const req = {
      nameLike: filters.nameLike || undefined,
      idNo: filters.idNo || undefined,
      disabilityCardNo: filters.disabilityCardNo || undefined,
      anomalyType: filters.anomalyType || undefined,
      status: filters.status || undefined
    }
    const resp = await http.post('/api/anomalies/export', req, { responseType: 'blob' })
    const blob = new Blob([resp.data], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `anomalies-${new Date().toISOString().slice(0, 10)}.csv`
    a.click()
    URL.revokeObjectURL(url)
    message.success('导出成功')
  } catch (e: any) {
    message.error('导出失败')
  } finally {
    exporting.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
}

load()
</script>
