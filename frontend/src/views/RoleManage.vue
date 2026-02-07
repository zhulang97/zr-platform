<template>
  <a-card title="角色管理">
    <a-space style="margin-bottom: 16px;">
      <a-input v-model:value="searchForm.roleCodeLike" placeholder="角色编码" style="width: 200px;" />
      <a-select v-model:value="searchForm.status" placeholder="状态" style="width: 120px;" allow-clear>
        <a-select-option :value="1">启用</a-select-option>
        <a-select-option :value="0">禁用</a-select-option>
      </a-select>
      <a-button type="primary" @click="handleSearch">查询</a-button>
      <a-button @click="handleReset">重置</a-button>
      <a-button type="primary" @click="handleCreate">新建角色</a-button>
    </a-space>

    <a-table
      :columns="columns"
      :data-source="roles"
      :loading="loading"
      :pagination="false"
      row-key="roleId"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">{{ record.status === 1 ? '启用' : '禁用' }}</a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
            <a-button type="link" size="small" @click="handleGrantPermissions(record)">分配权限</a-button>
            <a-popconfirm title="确定删除吗？" @confirm="handleDelete(record.roleId)">
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal
      v-model:open="editModalOpen"
      :title="editForm.roleId ? '编辑角色' : '新建角色'"
      @ok="handleEditOk"
      @cancel="editModalOpen = false"
    >
      <a-form :model="editForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="角色编码" name="roleCode" :rules="[{ required: true, message: '请输入角色编码' }]">
          <a-input v-model:value="editForm.roleCode" :disabled="!!editForm.roleId" />
        </a-form-item>
        <a-form-item label="角色名称" name="name" :rules="[{ required: true, message: '请输入角色名称' }]">
          <a-input v-model:value="editForm.name" />
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
      v-model:open="permissionModalOpen"
      title="分配权限"
      width="600px"
      @ok="handleGrantOk"
      @cancel="permissionModalOpen = false"
    >
      <a-checkbox-group v-model:value="selectedPermissions">
        <a-row :gutter="[16, 8]">
          <a-col :span="12" v-for="perm in permissions" :key="perm.permId">
            <a-checkbox :value="perm.permId">{{ perm.name }} ({{ perm.permCode }})</a-checkbox>
          </a-col>
        </a-row>
      </a-checkbox-group>
    </a-modal>
  </a-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'

interface Role {
  roleId: number
  roleCode: string
  name: string
  status: number
  createdAt: string
}

interface Permission {
  permId: number
  permCode: string
  name: string
}

const roles = ref<Role[]>([])
const permissions = ref<Permission[]>([])
const selectedPermissions = ref<number[]>([])
const loading = ref(false)

const searchForm = reactive({
  roleCodeLike: '',
  status: undefined as number | undefined
})

const editForm = reactive({
  roleId: undefined as number | undefined,
  roleCode: '',
  name: '',
  status: 1
})

const editModalOpen = ref(false)
const permissionModalOpen = ref(false)
const currentRoleId = ref<number>()

const columns = [
  { title: '角色ID', dataIndex: 'roleId', key: 'roleId', width: 100 },
  { title: '角色编码', dataIndex: 'roleCode', key: 'roleCode', width: 150 },
  { title: '角色名称', dataIndex: 'name', key: 'name', width: 150 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 200 }
]

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await fetch('/api/roles/search', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(searchForm)
    })
    const data = await res.json()
    if (data.code === 0) {
      roles.value = data.data
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
  searchForm.roleCodeLike = ''
  searchForm.status = undefined
  handleSearch()
}

const handleCreate = () => {
  Object.assign(editForm, { roleId: undefined, roleCode: '', name: '', status: 1 })
  editModalOpen.value = true
}

const handleEdit = (record: Role) => {
  Object.assign(editForm, {
    roleId: record.roleId,
    roleCode: record.roleCode,
    name: record.name,
    status: record.status
  })
  editModalOpen.value = true
}

const handleEditOk = async () => {
  try {
    const url = editForm.roleId ? `/api/roles/${editForm.roleId}` : '/api/roles'
    const method = editForm.roleId ? 'PUT' : 'POST'
    const body = editForm.roleId
      ? { name: editForm.name, status: editForm.status }
      : { roleCode: editForm.roleCode, name: editForm.name, status: editForm.status }

    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
    const data = await res.json()
    if (data.code === 0) {
      message.success(editForm.roleId ? '更新成功' : '创建成功')
      editModalOpen.value = false
      handleSearch()
    } else {
      message.error(data.message || '操作失败')
    }
  } catch (e) {
    message.error('操作失败')
  }
}

const handleDelete = async (roleId: number) => {
  try {
    const res = await fetch(`/api/roles/${roleId}`, { method: 'DELETE' })
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

const handleGrantPermissions = async (record: Role) => {
  currentRoleId.value = record.roleId
  loading.value = true
  try {
    const [permRes, rolePermRes] = await Promise.all([
      fetch('/api/permissions/all'),
      fetch(`/api/roles/${record.roleId}/permissions`)
    ])
    const permData = await permRes.json()
    const rolePermData = await rolePermRes.json()

    if (permData.code === 0) {
      permissions.value = permData.data
    }
    if (rolePermData.code === 0) {
      selectedPermissions.value = rolePermData.data.map((p: any) => p.permId)
    }
    permissionModalOpen.value = true
  } catch (e) {
    message.error('加载权限失败')
  } finally {
    loading.value = false
  }
}

const handleGrantOk = async () => {
  try {
    const res = await fetch(`/api/roles/${currentRoleId.value}/permissions`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ permissionIds: selectedPermissions.value })
    })
    const data = await res.json()
    if (data.code === 0) {
      message.success('分配成功')
      permissionModalOpen.value = false
    } else {
      message.error(data.message || '分配失败')
    }
  } catch (e) {
    message.error('分配失败')
  }
}

onMounted(() => {
  handleSearch()
})
</script>
