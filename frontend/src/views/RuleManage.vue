<template>
  <a-card title="规则管理">
    <a-space style="margin-bottom: 16px;">
      <a-input v-model:value="searchForm.ruleCodeLike" placeholder="规则编码" style="width: 200px;" />
      <a-input v-model:value="searchForm.ruleType" placeholder="规则类型" style="width: 150px;" />
      <a-select v-model:value="searchForm.severity" placeholder="严重程度" style="width: 120px;" allow-clear>
        <a-select-option :value="1">高</a-select-option>
        <a-select-option :value="2">中</a-select-option>
        <a-select-option :value="3">低</a-select-option>
      </a-select>
      <a-select v-model:value="searchForm.enabled" placeholder="状态" style="width: 120px;" allow-clear>
        <a-select-option :value="1">启用</a-select-option>
        <a-select-option :value="0">禁用</a-select-option>
      </a-select>
      <a-button type="primary" @click="handleSearch">查询</a-button>
      <a-button @click="handleReset">重置</a-button>
      <a-button type="primary" @click="handleCreate">新建规则</a-button>
      <a-button @click="handleExecuteAll" :loading="executingAll">执行全部</a-button>
    </a-space>

    <a-table
      :columns="columns"
      :data-source="rules"
      :loading="loading"
      :pagination="false"
      row-key="ruleId"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'enabled'">
          <a-tag :color="record.enabled === 1 ? 'green' : 'red'">{{ record.enabled === 1 ? '启用' : '禁用' }}</a-tag>
        </template>
        <template v-if="column.key === 'severity'">
          <a-tag :color="getSeverityColor(record.severity)">{{ getSeverityText(record.severity) }}</a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
            <a-button type="link" size="small" @click="handleExecute(record)" :loading="record.executing">执行</a-button>
            <a-button
              v-if="record.enabled === 0"
              type="link"
              size="small"
              @click="handleEnable(record)"
            >启用</a-button>
            <a-button
              v-else
              type="link"
              size="small"
              @click="handleDisable(record)"
            >禁用</a-button>
            <a-popconfirm title="确定删除吗？" @confirm="handleDelete(record.ruleId)">
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal
      v-model:open="editModalOpen"
      :title="editForm.ruleId ? '编辑规则' : '新建规则'"
      width="700px"
      @ok="handleEditOk"
      @cancel="editModalOpen = false"
    >
      <a-form :model="editForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="规则编码" name="ruleCode" :rules="[{ required: true, message: '请输入规则编码' }]">
          <a-input v-model:value="editForm.ruleCode" :disabled="!!editForm.ruleId" />
        </a-form-item>
        <a-form-item label="规则名称" name="name" :rules="[{ required: true, message: '请输入规则名称' }]">
          <a-input v-model:value="editForm.name" />
        </a-form-item>
        <a-form-item label="规则类型" name="ruleType">
          <a-input v-model:value="editForm.ruleType" placeholder="如：age_over_80" />
        </a-form-item>
        <a-form-item label="规则条件" name="ruleCondition">
          <a-select v-model:value="editForm.ruleCondition" allow-clear>
            <a-select-option value="age_over_80">年龄大于80岁</a-select-option>
            <a-select-option value="high_disability_level">高残疾等级</a-select-option>
            <a-select-option value="has_caregiver">有护理人员</a-select-option>
            <a-select-option value="lives_alone">独居</a-select-option>
            <a-select-option value="recent_hospitalization">近期住院</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="参数" name="params">
          <a-textarea v-model:value="editForm.params" :rows="3" placeholder="JSON格式参数" />
        </a-form-item>
        <a-form-item label="严重程度" name="severity">
          <a-select v-model:value="editForm.severity">
            <a-select-option :value="1">高</a-select-option>
            <a-select-option :value="2">中</a-select-option>
            <a-select-option :value="3">低</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="优先级" name="priority">
          <a-input-number v-model:value="editForm.priority" :min="1" :max="10" style="width: 100%;" />
        </a-form-item>
        <a-form-item label="状态" name="enabled">
          <a-select v-model:value="editForm.enabled">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Cron表达式" name="cronExpr">
          <a-input v-model:value="editForm.cronExpr" placeholder="如：0 0 2 * * ?" />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="editForm.description" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'

interface Rule {
  ruleId: number
  ruleCode: string
  name: string
  description?: string
  ruleType?: string
  ruleCondition?: string
  params?: string
  severity: number
  priority: number
  enabled: number
  cronExpr?: string
  lastExecutedAt?: string
  executing?: boolean
}

const rules = ref<Rule[]>([])
const loading = ref(false)
const executingAll = ref(false)

const searchForm = reactive({
  ruleCodeLike: '',
  ruleType: '',
  severity: undefined as number | undefined,
  enabled: undefined as number | undefined
})

const editForm = reactive({
  ruleId: undefined as number | undefined,
  ruleCode: '',
  name: '',
  description: '',
  ruleType: '',
  ruleCondition: '',
  params: '',
  severity: 1,
  priority: 5,
  enabled: 1,
  cronExpr: ''
})

const editModalOpen = ref(false)

const columns = [
  { title: '规则ID', dataIndex: 'ruleId', key: 'ruleId', width: 80 },
  { title: '规则编码', dataIndex: 'ruleCode', key: 'ruleCode', width: 150 },
  { title: '规则名称', dataIndex: 'name', key: 'name', width: 150 },
  { title: '规则类型', dataIndex: 'ruleType', key: 'ruleType', width: 120 },
  { title: '严重程度', dataIndex: 'severity', key: 'severity', width: 100 },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 80 },
  { title: '状态', dataIndex: 'enabled', key: 'enabled', width: 80 },
  { title: '上次执行', dataIndex: 'lastExecutedAt', key: 'lastExecutedAt', width: 160 },
  { title: '操作', key: 'action', width: 250 }
]

const getSeverityColor = (severity: number) => {
  const colors: Record<number, string> = {
    1: 'red',
    2: 'orange',
    3: 'blue'
  }
  return colors[severity] || 'default'
}

const getSeverityText = (severity: number) => {
  const texts: Record<number, string> = {
    1: '高',
    2: '中',
    3: '低'
  }
  return texts[severity] || '未知'
}

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await fetch('/api/rules/search', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(searchForm)
    })
    const data = await res.json()
    if (data.code === 0) {
      rules.value = data.data
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
  searchForm.ruleCodeLike = ''
  searchForm.ruleType = ''
  searchForm.severity = undefined
  searchForm.enabled = undefined
  handleSearch()
}

const handleCreate = () => {
  Object.assign(editForm, {
    ruleId: undefined,
    ruleCode: '',
    name: '',
    description: '',
    ruleType: '',
    ruleCondition: '',
    params: '',
    severity: 1,
    priority: 5,
    enabled: 1,
    cronExpr: ''
  })
  editModalOpen.value = true
}

const handleEdit = (record: Rule) => {
  Object.assign(editForm, {
    ruleId: record.ruleId,
    ruleCode: record.ruleCode,
    name: record.name,
    description: record.description || '',
    ruleType: record.ruleType || '',
    ruleCondition: record.ruleCondition || '',
    params: record.params || '',
    severity: record.severity,
    priority: record.priority,
    enabled: record.enabled,
    cronExpr: record.cronExpr || ''
  })
  editModalOpen.value = true
}

const handleEditOk = async () => {
  try {
    const url = editForm.ruleId ? `/api/rules/${editForm.ruleId}` : '/api/rules'
    const method = editForm.ruleId ? 'PUT' : 'POST'
    const body = editForm.ruleId
      ? {
          name: editForm.name,
          description: editForm.description,
          ruleType: editForm.ruleType,
          ruleCondition: editForm.ruleCondition,
          params: editForm.params,
          severity: editForm.severity,
          priority: editForm.priority,
          enabled: editForm.enabled,
          cronExpr: editForm.cronExpr
        }
      : {
          ruleCode: editForm.ruleCode,
          name: editForm.name,
          description: editForm.description,
          ruleType: editForm.ruleType,
          ruleCondition: editForm.ruleCondition,
          params: editForm.params,
          severity: editForm.severity,
          priority: editForm.priority,
          enabled: editForm.enabled,
          cronExpr: editForm.cronExpr
        }

    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
    const data = await res.json()
    if (data.code === 0) {
      message.success(editForm.ruleId ? '更新成功' : '创建成功')
      editModalOpen.value = false
      handleSearch()
    } else {
      message.error(data.message || '操作失败')
    }
  } catch (e) {
    message.error('操作失败')
  }
}

const handleDelete = async (ruleId: number) => {
  try {
    const res = await fetch(`/api/rules/${ruleId}`, { method: 'DELETE' })
    const data = await res.json()
    if (data.code === 0) {
      message.success('删除成功')
      handleSearch()
    } else {
      message.error(data.message || '删除失败')
    }
  } catch (e) {
    message.error('删除失败')
  }
}

const handleEnable = async (record: Rule) => {
  try {
    const res = await fetch(`/api/rules/${record.ruleId}/enable`, { method: 'PUT' })
    const data = await res.json()
    if (data.code === 0) {
      message.success('启用成功')
      handleSearch()
    } else {
      message.error(data.message || '启用失败')
    }
  } catch (e) {
    message.error('启用失败')
  }
}

const handleDisable = async (record: Rule) => {
  try {
    const res = await fetch(`/api/rules/${record.ruleId}/disable`, { method: 'PUT' })
    const data = await res.json()
    if (data.code === 0) {
      message.success('禁用成功')
      handleSearch()
    } else {
      message.error(data.message || '禁用失败')
    }
  } catch (e) {
    message.error('禁用失败')
  }
}

const handleExecute = async (record: Rule) => {
  try {
    record.executing = true
    const res = await fetch(`/api/rules/${record.ruleId}/execute`, { method: 'POST' })
    const data = await res.json()
    if (data.code === 0) {
      message.success('执行成功')
      handleSearch()
    } else {
      message.error(data.message || '执行失败')
    }
  } catch (e) {
    message.error('执行失败')
  } finally {
    record.executing = false
  }
}

const handleExecuteAll = async () => {
  try {
    executingAll.value = true
    const res = await fetch('/api/rules/execute-all', { method: 'POST' })
    const data = await res.json()
    if (data.code === 0) {
      message.success('执行成功')
      handleSearch()
    } else {
      message.error(data.message || '执行失败')
    }
  } catch (e) {
    message.error('执行失败')
  } finally {
    executingAll.value = false
  }
}

onMounted(() => {
  handleSearch()
})
</script>
