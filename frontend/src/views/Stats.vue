<template>
  <a-card title="统计分析看板">
    <a-space direction="vertical" style="width:100%;" :size="16">
      <a-space>
        <span style="font-weight:600;">统计口径：</span>
        <a-select v-model:value="statDistrictIds" mode="multiple" :options="districtOptions" placeholder="选择区县" style="width: 240px;" />
        <a-button type="primary" @click="loadAll">刷新</a-button>
      </a-space>

      <a-row :gutter="16">
        <a-col :span="6">
          <a-card size="small" title="持证残疾人总数">
            <a-statistic :value="overview?.totalPersons ?? 0" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card size="small" title="残车覆盖率">
            <a-statistic :value="coverageStats.car ?? 0" suffix="人" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card size="small" title="医疗补助覆盖率">
            <a-statistic :value="coverageStats.medical ?? 0" suffix="人" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card size="small" title="养老补助覆盖率">
            <a-statistic :value="coverageStats.pension ?? 0" suffix="人" />
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-card title="残疾类别分布">
            <div ref="catChartRef" style="height: 320px;"></div>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="残疾等级分布">
            <div ref="levelChartRef" style="height: 320px;"></div>
          </a-card>
        </a-col>
      </a-row>

      <a-card title="各区县分布">
        <div ref="districtChartRef" style="height: 320px;"></div>
      </a-card>

      <div style="margin-top: 12px; text-align: right;" v-perm="'stats:export'">
        <a-button @click="exportStats" :loading="exporting">导出统计</a-button>
      </div>
    </a-space>
  </a-card>
</template>

<script setup lang="ts">
import { onMounted, ref, nextTick } from 'vue'
import * as echarts from 'echarts'
import { http } from '../api/http'
import { message } from 'ant-design-vue'

const statDistrictIds = ref<number[]>([])
const overview = ref<any>(null)
const coverageStats = ref<any>({})
const categories = ref<any[]>([])
const levels = ref<any[]>([])
const districtItems = ref<any[]>([])
const exporting = ref(false)

const districtOptions = ref<any[]>([])

const catChartRef = ref<HTMLDivElement>()
const levelChartRef = ref<HTMLDivElement>()
const districtChartRef = ref<HTMLDivElement>()

function initCharts() {
  if (catChartRef.value) {
    const c = echarts.init(catChartRef.value)
    c.setOption({
      tooltip: { trigger: 'item', formatter: '{a}: {c} 人' },
      xAxis: { type: 'category', data: categories.value.map(i => i.name) },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar',
        data: categories.value.map(i => ({ value: i.count, name: i.name }))
      }]
    })
  }

  if (levelChartRef.value) {
    const l = echarts.init(levelChartRef.value)
    l.setOption({
      tooltip: { trigger: 'item', formatter: '{a}: {c} 人' },
      xAxis: { type: 'category', data: levels.value.map(i => i.name) },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar',
        data: levels.value.map(i => ({ value: i.count, name: i.name }))
      }]
    })
  }

  if (districtChartRef.value) {
    const d = echarts.init(districtChartRef.value)
    d.setOption({
      tooltip: { trigger: 'item', formatter: '{a}: {c} 人' },
      xAxis: { type: 'category', data: districtItems.value.map(i => i.name) },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar',
        data: districtItems.value.map(i => ({ value: i.count, name: i.name }))
      }]
    })
  }
}

async function loadAll() {
  try {
    const payload = statDistrictIds.value.length ? { districtIds: statDistrictIds.value } : {}
    const [ov, dist, cov] = await Promise.all([
      http.post('/api/stats/overview', payload),
      http.post('/api/stats/by-district', payload),
      http.post('/api/stats/subsidy-coverage', payload)
    ])
    overview.value = ov.data.data
    districtItems.value = dist.data.data.items ?? []
    coverageStats.value = cov.data.data.coverage ?? {}

    const distResp = await http.post('/api/stats/disability-distribution', payload)
    categories.value = distResp.data.data.categories ?? []
    levels.value = distResp.data.data.levels ?? []

    await nextTick()
    initCharts()
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? '加载统计看板失败')
  }
}

async function loadDistricts() {
  try {
    const resp = await http.get('/api/dicts/districts')
    districtOptions.value = resp.data.data.map((d: any) => ({
      label: d.name,
      value: d.id
    }))
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? '加载区县失败')
  }
}

async function exportStats() {
  exporting.value = true
  try {
    const payload = statDistrictIds.value.length ? { districtIds: statDistrictIds.value } : {}
    const resp = await http.post('/api/stats/export', payload, { responseType: 'blob' })
    const blob = new Blob([resp.data], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `stats-${new Date().toISOString().slice(0, 10)}.csv`
    a.click()
    URL.revokeObjectURL(url)
    message.success('已导出')
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? '导出统计失败')
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  loadDistricts()
  loadAll()
})
</script>