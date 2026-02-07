<template>
  <a-card title="权限管理">
    <a-space style="margin-bottom: 16px;">
      <a-input v-model:value="searchForm.permCodeLike" placeholder="权限编码" style="width: 200px;" />
      <a-select v-model:value="searchForm.permType" placeholder="权限类型" style="width: 120px;" allow-clear>
        <a-select-option value="menu">菜单</a-select-option>
        <a-select-option value="api">API</a-select-option>
        <a-select-option value="data">数据</a-select-option>
      </a-select>
      <a-select v-model:value="searchForm.status" placeholder="状态" style="width: 120px;" allow-clear>
        <a-select-option :value="1">启用</a-select-option>
        <a-select-option :value="0">禁用</a-select-option>
      </a-select>
      <a-button type="primary" @click="handleSearch">查询</a-button>
      <a-button @click="handleReset">重置</a-button>
      <a-button type="primary" @click="handleCreate">新建权限</a-button>
    </a-space>

    <a-table
      :columns="columns"
      :data-source="permissions"
      :loading="loading"
      :pagination="false"
      row-key="permId"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'permType'">
          <a-tag :color="getPermTypeColor(record.permType)">{{ record.permType }}</a-tag>
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">{{ record.status === 1 ? '启用' : '禁用' }}</a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
            <a-popconfirm title="确定删除吗？" @confirm="handleDelete(record.permId)">
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal
      v-model:open="editModalOpen"
      :title="editForm.permId ? '编辑权限' : '新建权限'"
      @ok="handleEditOk"
      @cancel="editModalOpen = false"
    >
      <a-form :model="editForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="权限编码" name="permCode" :rules="[{ required: true, message: '请输入权限编码' }]">
          <a-input v-model:value="editForm.permCode" :disabled="!!editForm.permId" />
        </a-form-item>
        <a-form-item label="权限类型" name="permType" :rules="[{ required: true, message: '请选择权限类型' }]">
          <a-select v-model:value="editForm.permType">
            <a-select-option value="menu">菜单</a-select-option>
            <a-select-option value="api">API</a-select-option>
            <a-select-option value="data">数据</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="权限名称" name="name" :rules="[{ required: true, message: '请输入权限名称' }]">
          <a-input v-model:value="editForm.name" />
        </a-form-item>
        <a-form-item label="路径" name="path">
          <a-input v-model:value="editForm.path" placeholder="如：/api/users" />
        </a-form-item>
        <a-form-item label="方法" name="method">
          <a-select v-model:value="editForm.method" allow-clear>
            <a-select-option value="GET">GET</a-select-option>
            <a-select-option value="POST">POST</a-select-option>
            <a-select-option value="PUT">PUT</a-select-option>
            <a-select-option value="DELETE">DELETE</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="父权限" name="parentId">
          <a-select v-model:value="editForm.parentId" allow-clear show-search>
            <a-select-option v-for="perm in parentPermissions" :key="perm.permId" :value="perm.permId">
              {{ perm.name }} ({{ perm.permCode }})
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="排序" name="sort">
          <a-input-number v-model:value="editForm.sort" :min="0" style="width: 100%;" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="editForm.status">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'

interface Permission {
  permId: number
  permCode: string
  permType: string
  name: string
  path?: string
  method?: string
  parentId?: number
  sort: number
  status: number
}

const permissions = ref<Permission[]>([])
const loading = ref(false)

const searchForm = reactive({
  permCodeLike: '',
  permType: undefined as string | undefined,
  status: undefined as number | undefined
})

const editForm = reactive({
  permId: undefined as number | undefined,
  permCode: '',
  permType: 'api',
  name: '',
  path: '',
  method: '',
  parentId: undefined as number | undefined,
  sort: 0,
  status: 1
})

const editModalOpen = ref(false)

const columns = [
  { title: '权限ID', dataIndex: 'permId', key: 'permId', width: 100 },
  { title: '权限编码', dataIndex: 'permCode', key: 'permCode', width: 180 },
  { title: '权限类型', dataIndex: 'permType', key: 'permType', width: 100 },
  { title: '权限名称', dataIndex: 'name', key: 'name', width: 150 },
  { title: '路径', dataIndex: 'path', key: 'path', width: 150 },
  { title: '方法', dataIndex: 'method', key: 'method', width: 80 },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 150 }
]

const parentPermissions = computed(() => {
  return permissions.value.filter(p => p.permType === 'menu')
})

const getPermTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    menu: 'blue',
    api: 'green',
    data: 'orange'
  }
  return colors[type] || 'default'
}

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await fetch('/api/permissions/search', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(searchForm)
    })
    const data = await res.json()
    if (data.code === 0) {
      permissions.value = data.data
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
  searchForm.permCodeLike = ''
  searchForm.permType = undefined
  searchForm.status = undefined
  handleSearch()
}

const handleCreate = () => {
  Object.assign(editForm, {
    permId: undefined,
    permCode: '',
    permType: 'api',
    name: '',
    path: '',
    method: '',
    parentId: undefined,
    sort: 0,
    status: 1
  })
  editModalOpen.value = true
}

const handleEdit = (record: Permission) => {
  Object.assign(editForm, {
    permId: record.permId,
    permCode: record.permCode,
    permType: record.permType,
    name: record.name,
    path: record.path || '',
    method: record.method || '',
    parentId: record.parentId,
    sort: record.sort,
    status: record.status
  })
  editModalOpen.value = true
}

const handleEditOk = async () => {
  try {
    const url = editForm.permId ? `/api/permissions/${editForm.permId}` : '/api/permissions'
    const method = editForm.permId ? 'PUT' : 'POST'
    const body = editForm.permId
      ? {
          name: editForm.name,
          path: editForm.path,
          method: editForm.method,
          parentId: editForm.parentId,
          sort: editForm.sort,
          status: editForm.status
        }
      : {
          permCode: editForm.permCode,
          permType: editForm.permType,
          name: editForm.name,
          path: editForm.path,
          method: editForm.method,
          parentId: editForm.parentId,
          sort: editForm.sort,
          status: editForm.status
        }

    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
    const data = await res.json()
    if (data.code === 0) {
      message.success(editForm.permId ? '更新成功' : '创建成功')
      editModalOpen.value = false
      handleSearch()
    } else {
      message.error(data.message || '操作失败')
    }
  } catch (e) {
    message.error('操作失败')
  }
}

const handleDelete = async (permId: number) => {
  try {
    const res = await fetch(`/api/permissions/${permId}`, { method: 'DELETE' })
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

onMounted(() => {
  handleSearch()
})
</script>
