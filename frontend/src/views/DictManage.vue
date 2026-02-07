<template>
  <a-card title="字典管理">
    <a-row :gutter="16">
      <a-col :span="8">
        <a-card size="small" title="字典类型">
          <a-space style="margin-bottom: 12px;">
            <a-input v-model:value="typeSearch" placeholder="搜索类型" style="width: 150px;" />
            <a-button type="primary" @click="loadTypes">刷新</a-button>
          </a-space>
          <a-list
            size="small"
            :data-source="filteredTypes"
            :loading="loadingTypes"
            @click="selectType"
          >
            <template #renderItem="{ item }">
              <a-list-item
                :class="{ active: selectedType === item.dictType }"
                style="cursor: pointer;"
              >
                <a-list-item-meta>
                  <template #title>{{ item.dictName }}</template>
                  <template #description>{{ item.dictType }}</template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
      <a-col :span="16">
        <a-card size="small" title="字典项" v-if="selectedType">
          <a-space style="margin-bottom: 12px;">
            <a-select v-model:value="statusFilter" placeholder="状态" style="width: 100px;" allow-clear @change="loadItems">
              <a-select-option :value="1">启用</a-select-option>
              <a-select-option :value="0">禁用</a-select-option>
            </a-select>
            <a-button type="primary" @click="handleCreateItem" v-if="selectedType">新建项</a-button>
          </a-space>
          <a-table
            :columns="itemColumns"
            :data-source="items"
            :loading="loadingItems"
            :pagination="false"
            row-key="id"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 1 ? 'green' : 'red'">
                  {{ record.status === 1 ? '启用' : '禁用' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="handleEditItem(record)">编辑</a-button>
                  <a-button
                    v-if="record.status === 0"
                    type="link"
                    size="small"
                    @click="handleEnableItem(record)"
                  >启用</a-button>
                  <a-button
                    v-else
                    type="link"
                    size="small"
                    @click="handleDisableItem(record)"
                  >禁用</a-button>
                  <a-popconfirm title="确定删除吗？" @confirm="handleDeleteItem(record)">
                    <a-button type="link" size="small" danger>删除</a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
        <a-empty v-else description="请选择字典类型" />
      </a-col>
    </a-row>

    <a-modal
      v-model:open="itemModalOpen"
      :title="itemForm.id ? '编辑字典项' : '新建字典项'"
      @ok="handleItemOk"
      @cancel="itemModalOpen = false"
    >
      <a-form :model="itemForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="字典编码" name="code" :rules="[{ required: true, message: '请输入字典编码' }]">
          <a-input v-model:value="itemForm.code" :disabled="!!itemForm.id" />
        </a-form-item>
        <a-form-item label="字典名称" name="name" :rules="[{ required: true, message: '请输入字典名称' }]">
          <a-input v-model:value="itemForm.name" />
        </a-form-item>
        <a-form-item label="排序" name="sort">
          <a-input-number v-model:value="itemForm.sort" :min="0" style="width: 100%;" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="itemForm.status">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { message } from 'ant-design-vue'

interface DictType {
  dictType: string
  dictCode: string
  dictName: string
  sort: number
  status: number
}

interface DictItem {
  id: string
  dictType: string
  dictCode: string
  dictName: string
  sort: number
  status: number
}

const types = ref<DictType[]>([])
const items = ref<DictItem[]>([])
const loadingTypes = ref(false)
const loadingItems = ref(false)
const typeSearch = ref('')
const selectedType = ref<string>()
const statusFilter = ref<number>()
const itemModalOpen = ref(false)

const itemForm = reactive({
  id: '',
  code: '',
  name: '',
  sort: 0,
  status: 1
})

const filteredTypes = computed(() => {
  if (!typeSearch.value) return types.value
  const search = typeSearch.value.toLowerCase()
  return types.value.filter(t =>
    t.dictType.toLowerCase().includes(search) ||
    t.dictName.toLowerCase().includes(search)
  )
})

const itemColumns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 150 },
  { title: '编码', dataIndex: 'dictCode', key: 'dictCode', width: 100 },
  { title: '名称', dataIndex: 'dictName', key: 'dictName', width: 150 },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 180 }
]

const loadTypes = async () => {
  loadingTypes.value = true
  try {
    const res = await fetch('/api/dicts/types')
    const data = await res.json()
    if (data.code === 0) {
      types.value = data.data
    } else {
      message.error(data.message || '加载字典类型失败')
    }
  } catch (e) {
    message.error('加载字典类型失败')
  } finally {
    loadingTypes.value = false
  }
}

const selectType = (type: DictType) => {
  selectedType.value = type.dictType
}

const loadItems = async () => {
  if (!selectedType.value) return
  loadingItems.value = true
  try {
    const url = statusFilter.value !== undefined
      ? `/api/dicts/${selectedType.value}/items?status=${statusFilter.value}`
      : `/api/dicts/${selectedType.value}/items`
    const res = await fetch(url)
    const data = await res.json()
    if (data.code === 0) {
      items.value = data.data
    } else {
      message.error(data.message || '加载字典项失败')
    }
  } catch (e) {
    message.error('加载字典项失败')
  } finally {
    loadingItems.value = false
  }
}

watch(selectedType, () => {
  if (selectedType.value) {
    loadItems()
  } else {
    items.value = []
  }
})

const handleCreateItem = () => {
  Object.assign(itemForm, { id: '', code: '', name: '', sort: 0, status: 1 })
  itemModalOpen.value = true
}

const handleEditItem = (record: DictItem) => {
  const code = record.dictCode
  Object.assign(itemForm, {
    id: record.id,
    code,
    name: record.dictName,
    sort: record.sort,
    status: record.status
  })
  itemModalOpen.value = true
}

const handleItemOk = async () => {
  try {
    if (!selectedType.value) return
    const url = itemForm.id
      ? `/api/dicts/${selectedType.value}/items/${itemForm.code}`
      : `/api/dicts/${selectedType.value}/items`
    const method = itemForm.id ? 'PUT' : 'POST'
    const body = itemForm.id
      ? { name: itemForm.name, sort: itemForm.sort, status: itemForm.status }
      : { type: selectedType.value, code: itemForm.code, name: itemForm.name, sort: itemForm.sort, status: itemForm.status }

    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
    const data = await res.json()
    if (data.code === 0) {
      message.success(itemForm.id ? '更新成功' : '创建成功')
      itemModalOpen.value = false
      loadItems()
    } else {
      message.error(data.message || '操作失败')
    }
  } catch (e) {
    message.error('操作失败')
  }
}

const handleDeleteItem = async (record: DictItem) => {
  try {
    const url = `/api/dicts/${record.dictType}/items/${record.dictCode}`
    const res = await fetch(url, { method: 'DELETE' })
    const data = await res.json()
    if (data.code === 0) {
      message.success('删除成功')
      loadItems()
    } else {
      message.error(data.message || '删除失败')
    }
  } catch (e) {
    message.error('删除失败')
  }
}

const handleEnableItem = async (record: DictItem) => {
  try {
    const url = `/api/dicts/${record.dictType}/items/${record.dictCode}/enable`
    const res = await fetch(url, { method: 'PUT' })
    const data = await res.json()
    if (data.code === 0) {
      message.success('启用成功')
      loadItems()
    } else {
      message.error(data.message || '启用失败')
    }
  } catch (e) {
    message.error('启用失败')
  }
}

const handleDisableItem = async (record: DictItem) => {
  try {
    const url = `/api/dicts/${record.dictType}/items/${record.dictCode}/disable`
    const res = await fetch(url, { method: 'PUT' })
    const data = await res.json()
    if (data.code === 0) {
      message.success('禁用成功')
      loadItems()
    } else {
      message.error(data.message || '禁用失败')
    }
  } catch (e) {
    message.error('禁用失败')
  }
}

loadTypes()
</script>

<style scoped>
.active {
  background-color: #e6f7ff;
}
</style>
