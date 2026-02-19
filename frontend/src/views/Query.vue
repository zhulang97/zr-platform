<template>
  <a-card title="数据总览">
    <div style="display: grid; grid-template-columns: 320px 1fr; gap: 16px;">
      <a-card size="small" title="筛选条件">
        <a-form layout="vertical">
          <a-form-item label="姓名(模糊)">
            <a-input v-model:value="filters.name" placeholder="输入姓名" />
          </a-form-item>
          <a-form-item label="身份证号">
            <a-input v-model:value="filters.idCard" placeholder="输入身份证号" />
          </a-form-item>
          <a-form-item label="街道">
            <a-select v-model:value="filters.street" placeholder="选择街道" allow-clear @change="onStreetChange">
              <a-select-option v-for="s in streets" :key="s" :value="s">{{ s }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="居委">
            <a-select v-model:value="filters.committee" placeholder="选择居委" allow-clear :disabled="!filters.street">
              <a-select-option v-for="c in committees" :key="c" :value="c">{{ c }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="残疾类别">
            <a-select v-model:value="filters.disabilityCategory" placeholder="选择类别" allow-clear>
              <a-select-option v-for="c in categories" :key="c" :value="c">{{ c }}</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="残疾等级">
            <a-select v-model:value="filters.disabilityLevel" placeholder="选择等级" allow-clear>
              <a-select-option v-for="l in levels" :key="l" :value="l">{{ l }}级</a-select-option>
            </a-select>
          </a-form-item>
          <a-space>
            <a-button type="primary" @click="load">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form>
      </a-card>
      <a-card size="small" title="查询结果" :body-style="{ padding: '0' }">
        <a-table
          :columns="columns"
          :data-source="rows"
          :pagination="pagination"
          :loading="loading"
          row-key="idCard"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'name'">
              <a>{{ record.name }}</a>
            </template>
            <template v-else-if="column.key === 'idCard'">
              {{ maskIdCard(record.idCard) }}
            </template>
            <template v-else-if="column.key === 'category'">
              {{ record.disabilityCategory }} / {{ record.disabilityLevel }}
            </template>
            <template v-else-if="column.key === 'address'">
              {{ record.street }} / {{ record.committee }}
            </template>
            <template v-else-if="column.key === 'action'">
              <a-button type="link" size="small" @click="viewDetail(record)">查看详情</a-button>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { http } from '../api/http'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const filters = reactive({
  name: '',
  idCard: '',
  street: '',
  committee: '',
  disabilityCategory: '',
  disabilityLevel: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const rows = ref<any[]>([])
const loading = ref(false)
const streets = ref<string[]>([])
const committees = ref<string[]>([])
const categories = ref<string[]>([])
const levels = ref<string[]>([])

const columns = [
  { title: '姓名', key: 'name', width: 100 },
  { title: '身份证号', key: 'idCard', width: 150 },
  { title: '残疾类别/等级', key: 'category', width: 120 },
  { title: '街道/居委', key: 'address', width: 180 },
  { title: '联系电话', dataIndex: 'phone', width: 120 },
  { title: '操作', key: 'action', width: 100, fixed: 'right' as const }
]

function maskIdCard(idCard: string) {
  if (!idCard) return '-'
  return idCard.substring(0, 6) + '********' + idCard.substring(14)
}

function viewDetail(record: any) {
  router.push(`/person/${record.idCard}`)
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  load()
}

function onStreetChange() {
  filters.committee = ''
  loadCommittees()
}

async function loadOptions() {
  try {
    const [s, c, cat, l] = await Promise.all([
      http.get('/api/person-index/streets'),
      http.get('/api/person-index/categories'),
      http.get('/api/person-index/levels')
    ])
    streets.value = s.data.data || []
    categories.value = c.data.data || []
    levels.value = l.data.data || []
  } catch (e) {
    console.error('Failed to load options', e)
  }
}

async function loadCommittees() {
  if (!filters.street) {
    committees.value = []
    return
  }
  try {
    const resp = await http.get('/api/person-index/committees', { params: { street: filters.street } })
    committees.value = resp.data.data || []
  } catch (e) {
    console.error('Failed to load committees', e)
  }
}

async function load() {
  loading.value = true
  try {
    const resp = await http.post('/api/person-index/search', {
      name: filters.name || undefined,
      idCard: filters.idCard || undefined,
      street: filters.street || undefined,
      committee: filters.committee || undefined,
      disabilityCategory: filters.disabilityCategory || undefined,
      disabilityLevel: filters.disabilityLevel || undefined,
      pageNo: pagination.current,
      pageSize: pagination.pageSize
    })
    rows.value = resp.data.data.records || []
    pagination.total = resp.data.data.total || 0
  } catch (error: any) {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

function handleReset() {
  filters.name = ''
  filters.idCard = ''
  filters.street = ''
  filters.committee = ''
  filters.disabilityCategory = ''
  filters.disabilityLevel = ''
  pagination.current = 1
  load()
}

onMounted(() => {
  loadOptions()
  load()
})
</script>
