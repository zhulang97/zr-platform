<template>
  <a-card title="一人一档">
    <a-space direction="vertical" style="width: 100%;" :size="16">
      <a-space>
        <a-input-search
          v-model:value="searchKey"
          placeholder="请输入姓名/身份证号/手机号"
          style="width: 300px"
          @search="handleSearch"
          allow-clear
        />
        <a-button type="primary" @click="handleSearch">搜索</a-button>
      </a-space>

      <a-collapse v-model:activeKey="activeKeys">
        <a-collapse-panel key="1" header="高级筛选">
          <a-space>
            <a-form-item label="街道">
              <a-select v-model:value="filters.street" placeholder="选择街道" allow-clear @change="onStreetChange" style="width: 150px">
                <a-select-option v-for="s in streets" :key="s" :value="s">{{ s }}</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="居委">
              <a-select v-model:value="filters.committee" placeholder="选择居委" allow-clear style="width: 150px">
                <a-select-option v-for="c in committees" :key="c" :value="c">{{ c }}</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="残疾类别">
              <a-select v-model:value="filters.disabilityCategory" placeholder="选择类别" allow-clear style="width: 150px">
                <a-select-option v-for="c in categories" :key="c" :value="c">{{ c }}</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="残疾等级">
              <a-select v-model:value="filters.disabilityLevel" placeholder="选择等级" allow-clear style="width: 100px">
                <a-select-option v-for="l in levels" :key="l" :value="l">{{ l }}级</a-select-option>
              </a-select>
            </a-form-item>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-collapse-panel>
      </a-collapse>

      <a-alert v-if="searched && rows.length === 0" type="warning" message="未找到匹配的人员" show-icon />

      <a-table
        v-if="rows.length > 0"
        :columns="columns"
        :data-source="rows"
        :pagination="pagination"
        :loading="loading"
        row-key="idCard"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a @click="viewDetail(record)">{{ record.name }}</a>
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
    </a-space>
  </a-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { http } from '../api/http'
import { message } from 'ant-design-vue'

const router = useRouter()

const searchKey = ref('')
const searched = ref(false)
const activeKeys = ref<string[]>([])

const filters = reactive({
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
  { title: '操作', key: 'action', width: 100 }
]

function maskIdCard(idCard: string) {
  if (!idCard) return '-'
  return idCard.substring(0, 6) + '********' + idCard.substring(14)
}

function viewDetail(record: any) {
  router.push(`/person-profile/${record.idCard}`)
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  handleSearch()
}

function onStreetChange() {
  filters.committee = ''
  loadCommittees()
}

async function loadOptions() {
  try {
    const [s, c, l] = await Promise.all([
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

async function handleSearch() {
  loading.value = true
  searched.value = true
  try {
    let name = '', idCard = ''
    
    if (searchKey.value) {
      const key = searchKey.value.trim()
      if (key.length() === 18 && /^\d{17}[\dXx]$/.test(key)) {
        idCard = key
      } else {
        name = key
      }
    }
    
    const resp = await http.post('/api/person-index/search', {
      name: name || undefined,
      idCard: idCard || undefined,
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
    message.error('搜索失败')
    rows.value = []
  } finally {
    loading.value = false
  }
}

function handleReset() {
  searchKey.value = ''
  filters.street = ''
  filters.committee = ''
  filters.disabilityCategory = ''
  filters.disabilityLevel = ''
  pagination.current = 1
  rows.value = []
  searched.value = false
}

onMounted(() => {
  loadOptions()
})
</script>
