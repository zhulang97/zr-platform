<template>
  <a-card title="用户管理">
    <a-space style="margin-bottom: 16px;">
      <a-input v-model:value="searchForm.usernameLike" placeholder="用户名" style="width: 200px;" />
      <a-select v-model:value="searchForm.status" placeholder="状态" style="width: 120px;" allow-clear>
        <a-select-option :value="1">启用</a-select-option>
        <a-select-option :value="0">禁用</a-select-option>
      </a-select>
      <a-button type="primary" @click="handleSearch">查询</a-button>
      <a-button @click="handleReset">重置</a-button>
      <a-button type="primary" @click="handleCreate">新建用户</a-button>
    </a-space>

    <a-table
      :columns="columns"
      :data-source="users"
      :loading="loading"
      :pagination="false"
      row-key="userId"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">{{ record.status === 1 ? '启用' : '禁用' }}</a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
            <a-button type="link" size="small" @click="handleDataScope(record)">数据范围</a-button>
            <a-button type="link" size="small" @click="handleResetPassword(record)">重置密码</a-button>
            <a-popconfirm title="确定禁用吗？" v-if="record.status === 1" @confirm="handleDisable(record.userId)">
              <a-button type="link" size="small" danger>禁用</a-button>
            </a-popconfirm>
            <a-popconfirm title="确定启用吗？" v-else @confirm="handleEnable(record.userId)">
              <a-button type="link" size="small">启用</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal
      v-model:open="editModalOpen"
      :title="editForm.userId ? '编辑用户' : '新建用户'"
      @ok="handleEditOk"
      @cancel="editModalOpen = false"
    >
      <a-form :model="editForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="用户名" name="username" :rules="[{ required: true, message: '请输入用户名' }]">
          <a-input v-model:value="editForm.username" :disabled="!!editForm.userId" />
        </a-form-item>
        <a-form-item label="显示名称" name="displayName" :rules="[{ required: true, message: '请输入显示名称' }]">
          <a-input v-model:value="editForm.displayName" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="editForm.phone" />
        </a-form-item>
        <a-form-item label="角色" name="roleId" :rules="[{ required: true, message: '请选择角色' }]">
          <a-select v-model:value="editForm.roleId">
            <a-select-option v-for="role in roles" :key="role.roleId" :value="role.roleId">
              {{ role.name }} ({{ role.roleCode }})
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="editForm.status">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="dataScopeModalOpen"
      title="设置数据范围"
      width="600px"
      @ok="handleDataScopeOk"
      @cancel="dataScopeModalOpen = false"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="区域范围">
          <a-select
            v-model:value="selectedDistricts"
            mode="multiple"
            placeholder="请选择区域"
            :options="districtOptions"
            :field-names="{ label: 'name', value: 'id' }"
            show-search
            style="width: 100%;"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'

interface User {
  userId: number
  username: string
  displayName: string
  phone?: string
  status: number
  createdAt: string
}

interface Role {
  roleId: number
  roleCode: string
  name: string
}

const users = ref<User[]>([])
const roles = ref<Role[]>([])
const districts = ref<any[]>([])
const loading = ref(false)

const searchForm = reactive({
  usernameLike: '',
  status: undefined as number | undefined
})

const editForm = reactive({
  userId: undefined as number | undefined,
  username: '',
  displayName: '',
  phone: '',
  roleId: undefined as number | undefined,
  status: 1
})

const editModalOpen = ref(false)
const dataScopeModalOpen = ref(false)
const currentUserId = ref<number>()
const selectedDistricts = ref<number[]>([])

const columns = [
  { title: '用户ID', dataIndex: 'userId', key: 'userId', width: 100 },
  { title: '用户名', dataIndex: 'username', key: 'username', width: 150 },
  { title: '显示名称', dataIndex: 'displayName', key: 'displayName', width: 150 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 280 }
]

const districtOptions = computed(() => {
  return districts.value.map(d => ({ id: d.id, name: d.name }))
})

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await fetch('/api/users/search', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ...searchForm, pageNo: 1, pageSize: 1000 })
    })
    const data = await res.json()
    if (data.code === 0) {
      users.value = data.data.items || data.data
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
  searchForm.usernameLike = ''
  searchForm.status = undefined
  handleSearch()
}

const handleCreate = async () => {
  await loadRoles()
  Object.assign(editForm, { userId: undefined, username: '', displayName: '', phone: '', roleId: undefined, status: 1 })
  editModalOpen.value = true
}

const handleEdit = async (record: User) => {
  await loadRoles()
  Object.assign(editForm, {
    userId: record.userId,
    username: record.username,
    displayName: record.displayName,
    phone: record.phone || '',
    roleId: undefined as number | undefined,
    status: record.status
  })
  editModalOpen.value = true
}

const handleEditOk = async () => {
  try {
    const url = editForm.userId ? `/api/users/${editForm.userId}` : '/api/users'
    const method = editForm.userId ? 'PUT' : 'POST'
    const body = editForm.userId
      ? { displayName: editForm.displayName, phone: editForm.phone, status: editForm.status }
      : {
          username: editForm.username,
          displayName: editForm.displayName,
          phone: editForm.phone,
          roleId: editForm.roleId,
          districtIds: []
        }

    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
    const data = await res.json()
    if (data.code === 0) {
      message.success(editForm.userId ? '更新成功' : '创建成功')
      editModalOpen.value = false
      handleSearch()
    } else {
      message.error(data.message || '操作失败')
    }
  } catch (e) {
    message.error('操作失败')
  }
}

const handleDisable = async (userId: number) => {
  try {
    const res = await fetch(`/api/users/${userId}/disable`, { method: 'PUT' })
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

const handleEnable = async (userId: number) => {
  try {
    const res = await fetch(`/api/users/${userId}/disable`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: 1 })
    })
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

const handleResetPassword = async (record: User) => {
  try {
    const res = await fetch(`/api/users/${record.userId}/reset-password`, { method: 'PUT' })
    const data = await res.json()
    if (data.code === 0) {
      message.success(`密码已重置：${data.data.tempPassword}`)
    } else {
      message.error(data.message || '重置失败')
    }
  } catch (e) {
    message.error('重置失败')
  }
}

const loadRoles = async () => {
  try {
    const res = await fetch('/api/roles')
    const data = await res.json()
    if (data.code === 0) {
      roles.value = data.data
    }
  } catch (e) {
    console.error('加载角色失败', e)
  }
}

const loadDistricts = async () => {
  try {
    const res = await fetch('/api/dicts/districts')
    const data = await res.json()
    if (data.code === 0) {
      districts.value = data.data
    }
  } catch (e) {
    console.error('加载区域失败', e)
  }
}

const handleDataScope = async (record: User) => {
  currentUserId.value = record.userId
  await loadDistricts()
  selectedDistricts.value = []
  dataScopeModalOpen.value = true
}

const handleDataScopeOk = async () => {
  try {
    const res = await fetch(`/api/users/${currentUserId.value}/data-scope`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ districtIds: selectedDistricts.value })
    })
    const data = await res.json()
    if (data.code === 0) {
      message.success('设置成功')
      dataScopeModalOpen.value = false
    } else {
      message.error(data.message || '设置失败')
    }
  } catch (e) {
    message.error('设置失败')
  }
}

onMounted(() => {
  handleSearch()
})
</script>
