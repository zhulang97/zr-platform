<template>
  <a-space direction="vertical" style="width: 100%;" :size="16">
    <a-card>
      <a-space>
        <a-button @click="goBack">返回</a-button>
        <span style="font-weight: 600;">一人一档</span>
      </a-space>
    </a-card>

    <a-card title="基础信息" :loading="loading">
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="姓名">{{ detail?.nameMasked ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="身份证号">{{ detail?.idNoMasked ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="残疾证号">{{ detail?.disabilityCardNo ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="残疾类别/等级">
          {{ detail?.disabilityCategoryName ?? detail?.disabilityCategoryCode ?? '-' }} /
          {{ detail?.disabilityLevelName ?? detail?.disabilityLevelCode ?? '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="发证日期">{{ detail?.issueDate ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="残疾证状态">
          <a-tag :color="statusColor(detail?.cardStatusCode)">{{ detail?.cardStatusName ?? detail?.cardStatusCode ?? '-' }}</a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="业务信息" :loading="loading">
      <div style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px;">
        <a-card size="small" title="残车">
          <a-descriptions :column="1" size="small" bordered>
            <a-descriptions-item label="是否持有">
              <a-tag :color="boolColor(boolVal(biz?.hasCar))">{{ boolText(boolVal(biz?.hasCar)) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="车牌号">{{ biz?.plateNo ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="年检状态">{{ biz?.annualInspectionStatus ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="人车是否分离">
              <a-tag v-if="personCarSeparated" color="red">是</a-tag>
              <a-tag v-else color="green">否</a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card size="small" title="医疗补助">
          <a-descriptions :column="1" size="small" bordered>
            <a-descriptions-item label="是否享受">
              <a-tag :color="boolColor(boolVal(biz?.medicalEnabled))">{{ boolText(boolVal(biz?.medicalEnabled)) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="当前状态">{{ biz?.medicalStatus ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="最近发放时间">{{ asText(biz?.medicalLastPayDate) }}</a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card size="small" title="养老补助">
          <a-descriptions :column="1" size="small" bordered>
            <a-descriptions-item label="是否享受">
              <a-tag :color="boolColor(boolVal(biz?.pensionEnabled))">{{ boolText(boolVal(biz?.pensionEnabled)) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="当前状态">{{ biz?.pensionStatus ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="最近发放时间">{{ asText(biz?.pensionLastPayDate) }}</a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card size="small" title="盲人证">
          <a-descriptions :column="1" size="small" bordered>
            <a-descriptions-item label="是否持有">
              <a-tag :color="boolColor(!!biz?.blindCardNo)">{{ biz?.blindCardNo ? '是' : '否' }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="状态">{{ biz?.blindStatus ?? '-' }}</a-descriptions-item>
          </a-descriptions>
        </a-card>
      </div>
      <div style="margin-top: 8px; color: #999; font-size: 12px;">
        说明：本页所有字段均为展示用途，实际业务数据不可由助手直接修改。
      </div>
    </a-card>

    <a-card title="异常与风险提示" :loading="loading">
      <a-alert
        v-if="(risks?.length ?? 0) === 0"
        type="success"
        show-icon
        message="未发现异常"
      />
      <a-table
        v-else
        :dataSource="risks"
        :columns="riskColumns"
        :pagination="false"
        rowKey="anomalyId"
      />
    </a-card>

    <a-drawer v-model:open="anomalyOpen" title="异常详情" width="520">
      <template #extra>
        <a-space>
          <a-button size="small" @click="anomalyOpen = false">关闭</a-button>
        </a-space>
      </template>
      <a-spin :spinning="anomalyLoading">
        <a-descriptions bordered size="small" :column="1">
          <a-descriptions-item label="异常类型">
            {{ anomalyDetail?.anomalyTypeName ?? anomalyDetail?.anomalyType ?? '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="规则">
            {{ anomalyDetail?.ruleName ?? anomalyDetail?.ruleCode ?? '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="严重程度">{{ anomalyDetail?.severity ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="anomalyDetail?.status === 'UNHANDLED' ? 'red' : 'green'">{{ anomalyDetail?.status ?? '-' }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="命中时间">{{ anomalyDetail?.hitTime ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="处理说明">{{ anomalyDetail?.handleNote ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="处理时间">{{ anomalyDetail?.handledAt ?? '-' }}</a-descriptions-item>
        </a-descriptions>

        <a-card size="small" title="命中证据" style="margin-top: 12px;">
          <pre style="margin:0; white-space: pre-wrap; word-break: break-word;">{{ formatJson(anomalyDetail?.snapshotJson) }}</pre>
        </a-card>

        <a-divider />
        <div v-perm="'anomaly:handle'">
          <a-space direction="vertical" style="width: 100%;">
            <a-select v-model:value="handleStatus" :options="handleStatusOptions" />
            <a-textarea v-model:value="handleNote" placeholder="处理说明" :rows="3" />
            <a-button type="primary" :loading="handleSubmitting" @click="submitHandle">提交处理</a-button>
          </a-space>
        </div>
      </a-spin>
    </a-drawer>
  </a-space>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { http } from '../api/http'
import { router } from '../router'

const route = useRoute()

const loading = ref(false)
const detail = ref<any | null>(null)
const biz = ref<any | null>(null)
const risks = ref<any[]>([])

const anomalyOpen = ref(false)
const anomalyLoading = ref(false)
const anomalyDetail = ref<any | null>(null)
const handleStatus = ref('VERIFIED')
const handleNote = ref('')
const handleSubmitting = ref(false)

const handleStatusOptions = [
  { value: 'UNHANDLED', label: '未处理' },
  { value: 'VERIFIED', label: '已核实' },
  { value: 'HANDLED', label: '已处理' }
]

const personId = computed(() => {
  const v = route.params.id
  return v ? String(v) : ''
})

const personCarSeparated = computed(() => {
  return boolVal(biz.value?.personCarSeparated)
})

const riskColumns = [
  {
    title: '异常类型',
    dataIndex: 'anomalyType',
    customRender: ({ record }: any) => {
      const text = record.anomalyTypeName ?? typeLabel(record.anomalyType)
      return record.status === 'UNHANDLED'
        ? h('span', { style: 'color:#cf1322;font-weight:600;' }, text)
        : text
    }
  },
  { title: '状态', dataIndex: 'status' },
  { title: '命中时间', dataIndex: 'hitTime' }
]

riskColumns.push({
  title: '操作',
  key: 'action',
  customRender: ({ record }: any) =>
    h(
      'a',
      {
        onClick: (e: Event) => {
          e.preventDefault()
          openAnomaly(record?.anomalyId)
        }
      },
      '查看'
    )
})

function typeLabel(code: string) {
  if (!code) return '-'
  const m: Record<string, string> = {
    PERSON_CAR_SEPARATION: '人车分离',
    NO_CARD_SUBSIDY: '无证补贴',
    CANCELLED_CARD_SUBSIDY: '注销仍补贴',
    DUPLICATE_SUBSIDY: '重复补贴'
  }
  return m[code] ?? code
}

function asText(v: any) {
  if (v == null || v === '') return '-'
  return String(v)
}

function boolVal(v: any) {
  if (v === true || v === 'true') return true
  if (v === false || v === 'false') return false
  if (typeof v === 'number') return v === 1
  if (typeof v === 'string') return v === '1'
  return false
}

function boolText(v: boolean) {
  return v ? '是' : '否'
}

function boolColor(v: boolean) {
  return v ? 'green' : 'default'
}

function statusColor(status?: string | null) {
  if (!status) return 'default'
  if (status === 'NORMAL') return 'green'
  if (status === 'CANCELLED') return 'red'
  if (status === 'EXPIRED') return 'orange'
  return 'default'
}

async function load() {
  const idCard = personId.value
  if (!idCard || idCard.length < 5) return
  
  loading.value = true
  try {
    const [idx, d, b, r] = await Promise.all([
      http.get(`/api/persons/by-idcard/${idCard}`).catch(() => ({ data: { data: null } })),
      http.get(`/api/persons/${idCard}`).catch(() => ({ data: { data: null } })),
      http.get(`/api/persons/${idCard}/biz`).catch(() => ({ data: { data: null } })),
      http.get(`/api/persons/${idCard}/risks`).catch(() => ({ data: { data: [] } }))
    ])
    
    const personIndex = idx.data?.data
    const personDetail = d.data?.data
    
    if (personIndex) {
      detail.value = {
        nameMasked: personIndex.name ? personIndex.name.substring(0, 1) + '**' : null,
        idNoMasked: personIndex.idCard ? personIndex.idCard.substring(0, 6) + '********' + personIndex.idCard.substring(14) : null,
        disabilityCategoryName: personIndex.disabilityCategory,
        disabilityLevelName: personIndex.disabilityLevel,
        disabilityCardNo: null,
        issueDate: null,
        cardStatusCode: personIndex.locationStatus,
        cardStatusName: personIndex.locationStatus === 'SUCCESS' ? '正常' : '待定位'
      }
      biz.value = b.data?.data || {}
      risks.value = r.data?.data ?? []
    } else if (personDetail) {
      detail.value = personDetail
      biz.value = b.data?.data || {}
      risks.value = r.data?.data ?? []
    } else {
      message.warning('未找到该人员信息')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

async function openAnomaly(anomalyId: number) {
  if (!anomalyId) return
  anomalyOpen.value = true
  anomalyLoading.value = true
  handleNote.value = ''
  try {
    const resp = await http.get(`/api/anomalies/${anomalyId}`)
    anomalyDetail.value = resp.data.data
    handleStatus.value = anomalyDetail.value?.status === 'HANDLED' ? 'HANDLED' : 'VERIFIED'
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? '加载异常详情失败')
  } finally {
    anomalyLoading.value = false
  }
}

function formatJson(v: any) {
  if (!v) return '-'
  try {
    const obj = typeof v === 'string' ? JSON.parse(v) : v
    return JSON.stringify(obj, null, 2)
  } catch {
    return String(v)
  }
}

async function submitHandle() {
  if (!anomalyDetail.value?.anomalyId) return
  handleSubmitting.value = true
  try {
    await http.post(`/api/anomalies/${anomalyDetail.value.anomalyId}/status`, {
      status: handleStatus.value,
      note: handleNote.value
    })
    message.success('已提交')
    anomalyOpen.value = false
    await load()
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? '提交失败')
  } finally {
    handleSubmitting.value = false
  }
}

function goBack() {
  router.back()
}

onMounted(load)
watch(personId, load)
</script>
