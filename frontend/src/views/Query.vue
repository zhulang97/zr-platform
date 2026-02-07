<template>
  <a-card title="统一数据查询">
    <div style="display: grid; grid-template-columns: 320px 1fr; gap: 16px;">
      <a-card size="small" title="筛选条件">
        <a-form layout="vertical">
          <a-form-item label="姓名(模糊)">
            <a-input v-model:value="filters.nameLike" />
          </a-form-item>
          <a-form-item label="身份证号(精确)">
            <a-input v-model:value="filters.idNo" />
          </a-form-item>
          <a-space>
            <a-button type="primary" @click="load">查询</a-button>
            <a-button v-perm="'person:export'" @click="exporting">导出</a-button>
          </a-space>
        </a-form>
      </a-card>
      <a-card size="small" title="结果">
        <a-table
          :columns="columns"
          :dataSource="rows"
          :pagination="false"
          rowKey="personId"
          :customRow="customRow"
        />
      </a-card>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { http } from '../api/http'
import { message } from 'ant-design-vue'
import { router } from '../router'

const filters = reactive({
  nameLike: '',
  idNo: '',
  pageNo: 1,
  pageSize: 20
})

const rows = ref<any[]>([])

const columns = [
  { title: '姓名', dataIndex: 'nameMasked' },
  { title: '身份证号', dataIndex: 'idNoMasked' },
  {
    title: '残疾类别/等级',
    customRender: ({ record }: any) =>
      `${record.disabilityCategoryName ?? record.disabilityCategoryCode ?? '-'} / ${record.disabilityLevelName ?? record.disabilityLevelCode ?? '-'}`
  },
  { title: '区县/街道', customRender: ({ record }: any) => `${record.district ?? '-'} / ${record.street ?? '-'}` },
  { title: '残车', dataIndex: 'hasCar' },
  { title: '医疗补助', dataIndex: 'hasMedicalSubsidy' },
  { title: '养老补助', dataIndex: 'hasPensionSubsidy' },
  { title: '异常', dataIndex: 'riskFlag' }
]

function customRow(record: any) {
  return {
    style: 'cursor: pointer',
    onClick: () => {
      if (record?.personId) router.push(`/person/${record.personId}`)
    }
  }
}

async function load() {
  const resp = await http.post('/api/persons/search', { ...filters })
  rows.value = resp.data.data.items
}

async function exporting() {
  const resp = await http.post('/api/persons/export', { ...filters }, { responseType: 'blob' })
  const blob = new Blob([resp.data], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `persons-${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
  message.success('已导出')
}
</script>
