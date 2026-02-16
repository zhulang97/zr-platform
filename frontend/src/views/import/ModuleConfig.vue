<template>
  <div class="config-container">
    <div class="page-header">
      <h2>导入模块配置</h2>
      <a-space>
        <a-button type="primary" @click="showModuleModal()">
          <PlusOutlined /> 新建模块
        </a-button>
        <a-button @click="syncFromEnum" :loading="syncing">
          <SyncOutlined /> 从枚举同步
        </a-button>
      </a-space>
    </div>

    <div class="content-layout">
      <div class="module-list">
        <a-card title="模块列表" size="small" :loading="loading">
          <template #extra>
            <a-select v-model:value="filterCategory" placeholder="按分类筛选" style="width: 120px" allowClear>
              <a-select-option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</a-select-option>
            </a-select>
          </template>
          <a-list :data-source="filteredModules" size="small">
            <template #renderItem="{ item }">
              <a-list-item 
                :class="['module-item', { active: selectedModule?.moduleCode === item.moduleCode }]"
                @click="selectModule(item)"
              >
                <a-list-item-meta :title="item.moduleName" :description="item.moduleCode" />
                <template #actions>
                  <a-tag :color="item.isActive ? 'success' : 'default'">
                    {{ item.isActive ? '启用' : '禁用' }}
                  </a-tag>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </div>

      <div class="module-detail" v-if="selectedModule">
        <a-card size="small">
          <template #title>
            <span>{{ selectedModule.moduleName }}</span>
            <a-tag style="margin-left: 8px">{{ selectedModule.category }}</a-tag>
          </template>
          <template #extra>
            <a-space>
              <a-button size="small" @click="showModuleModal(selectedModule)">编辑</a-button>
              <a-popconfirm title="确定删除该模块?" @confirm="deleteModule(selectedModule.moduleCode)">
                <a-button size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>

          <a-descriptions :column="2" size="small">
            <a-descriptions-item label="模块代码">{{ selectedModule.moduleCode }}</a-descriptions-item>
            <a-descriptions-item label="表名">{{ selectedModule.tableName }}</a-descriptions-item>
            <a-descriptions-item label="身份证字段">{{ selectedModule.idCardField }}</a-descriptions-item>
            <a-descriptions-item label="残疾证字段">{{ selectedModule.certNoField }}</a-descriptions-item>
            <a-descriptions-item label="权限代码">{{ selectedModule.permissionCode || '-' }}</a-descriptions-item>
            <a-descriptions-item label="排序">{{ selectedModule.sortOrder }}</a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card title="字段配置" size="small" style="margin-top: 16px">
          <template #extra>
            <a-button size="small" type="primary" @click="showFieldModal()">
              <PlusOutlined /> 添加字段
            </a-button>
          </template>
          <a-table 
            :columns="fieldColumns" 
            :data-source="fields" 
            size="small" 
            :pagination="false"
            :loading="fieldsLoading"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'isRequired'">
                <a-tag :color="record.isRequired ? 'red' : 'default'">{{ record.isRequired ? '必填' : '选填' }}</a-tag>
              </template>
              <template v-if="column.key === 'isUnique'">
                <a-tag :color="record.isUnique ? 'blue' : 'default'">{{ record.isUnique ? '唯一' : '-' }}</a-tag>
              </template>
              <template v-if="column.key === 'actions'">
                <a-space>
                  <a-button type="link" size="small" @click="showFieldModal(record)">编辑</a-button>
                  <a-popconfirm title="确定删除?" @confirm="deleteField(record.id)">
                    <a-button type="link" size="small" danger>删除</a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>

      <div class="empty-state" v-else>
        <a-empty description="请选择一个模块查看详情" />
      </div>
    </div>

    <a-modal 
      v-model:open="moduleModalVisible" 
      :title="editingModule ? '编辑模块' : '新建模块'"
      @ok="saveModule"
      :confirmLoading="saving"
    >
      <a-form :model="moduleForm" :label-col="{ span: 6 }">
        <a-form-item label="模块代码" required>
          <a-input v-model:value="moduleForm.moduleCode" :disabled="!!editingModule" />
        </a-form-item>
        <a-form-item label="模块名称" required>
          <a-input v-model:value="moduleForm.moduleName" />
        </a-form-item>
        <a-form-item label="表名" required>
          <a-input v-model:value="moduleForm.tableName" />
        </a-form-item>
        <a-form-item label="分类">
          <a-input v-model:value="moduleForm.category" />
        </a-form-item>
        <a-form-item label="身份证字段">
          <a-input v-model:value="moduleForm.idCardField" placeholder="ID_CARD" />
        </a-form-item>
        <a-form-item label="残疾证字段">
          <a-input v-model:value="moduleForm.certNoField" placeholder="DISABILITY_CERT_NO" />
        </a-form-item>
        <a-form-item label="权限代码">
          <a-input v-model:value="moduleForm.permissionCode" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="moduleForm.sortOrder" :min="0" />
        </a-form-item>
        <a-form-item label="是否启用">
          <a-switch v-model:checked="moduleForm.isActive" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal 
      v-model:open="fieldModalVisible" 
      :title="editingField ? '编辑字段' : '添加字段'"
      @ok="saveField"
      :confirmLoading="saving"
    >
      <a-form :model="fieldForm" :label-col="{ span: 6 }">
        <a-form-item label="字段代码" required>
          <a-input v-model:value="fieldForm.fieldCode" />
        </a-form-item>
        <a-form-item label="字段名称" required>
          <a-input v-model:value="fieldForm.fieldName" />
        </a-form-item>
        <a-form-item label="数据库列名">
          <a-input v-model:value="fieldForm.dbColumn" />
        </a-form-item>
        <a-form-item label="数据类型">
          <a-select v-model:value="fieldForm.dataType">
            <a-select-option value="STRING">字符串</a-select-option>
            <a-select-option value="INTEGER">整数</a-select-option>
            <a-select-option value="DECIMAL">小数</a-select-option>
            <a-select-option value="DATE">日期</a-select-option>
            <a-select-option value="DATETIME">日期时间</a-select-option>
            <a-select-option value="BOOLEAN">布尔</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="必填">
          <a-switch v-model:checked="fieldForm.isRequired" />
        </a-form-item>
        <a-form-item label="唯一">
          <a-switch v-model:checked="fieldForm.isUnique" />
        </a-form-item>
        <a-form-item label="默认值">
          <a-input v-model:value="fieldForm.defaultValue" />
        </a-form-item>
        <a-form-item label="校验规则">
          <a-textarea v-model:value="fieldForm.validationRule" :rows="2" placeholder="正则表达式" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="fieldForm.sortOrder" :min="0" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SyncOutlined } from '@ant-design/icons-vue'
import { http } from '@/api/http'

interface Module {
  moduleCode: string
  moduleName: string
  tableName: string
  category: string
  idCardField: string
  certNoField: string
  sortOrder: number
  isActive: number
  permissionCode: string
  createdAt: string
}

interface Field {
  id: number
  moduleCode: string
  fieldCode: string
  fieldName: string
  dbColumn: string
  dataType: string
  isRequired: number
  isUnique: number
  defaultValue: string
  validationRule: string
  sortOrder: number
}

const loading = ref(false)
const syncing = ref(false)
const saving = ref(false)
const fieldsLoading = ref(false)
const modules = ref<Module[]>([])
const categories = ref<string[]>([])
const fields = ref<Field[]>([])
const selectedModule = ref<Module | null>(null)
const filterCategory = ref<string | null>(null)

const moduleModalVisible = ref(false)
const fieldModalVisible = ref(false)
const editingModule = ref<Module | null>(null)
const editingField = ref<Field | null>(null)

const moduleForm = ref<any>({
  moduleCode: '',
  moduleName: '',
  tableName: '',
  category: '',
  idCardField: 'ID_CARD',
  certNoField: 'DISABILITY_CERT_NO',
  permissionCode: '',
  sortOrder: 0,
  isActive: true
})

const fieldForm = ref<any>({
  fieldCode: '',
  fieldName: '',
  dbColumn: '',
  dataType: 'STRING',
  isRequired: false,
  isUnique: false,
  defaultValue: '',
  validationRule: '',
  sortOrder: 0
})

const fieldColumns = [
  { title: '字段代码', dataIndex: 'fieldCode', key: 'fieldCode', width: 100 },
  { title: '字段名称', dataIndex: 'fieldName', key: 'fieldName', width: 120 },
  { title: '数据库列名', dataIndex: 'dbColumn', key: 'dbColumn', width: 120 },
  { title: '类型', dataIndex: 'dataType', key: 'dataType', width: 80 },
  { title: '必填', key: 'isRequired', width: 60 },
  { title: '唯一', key: 'isUnique', width: 60 },
  { title: '操作', key: 'actions', width: 100 }
]

const filteredModules = computed(() => {
  if (!filterCategory.value) return modules.value
  return modules.value.filter(m => m.category === filterCategory.value)
})

const loadModules = async () => {
  loading.value = true
  try {
    const resp = await http.get('/api/import/config/modules')
    modules.value = resp.data.data || []
  } catch (e) {
    console.error('Load modules failed:', e)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const resp = await http.get('/api/import/config/categories')
    categories.value = resp.data.data || []
  } catch (e) {
    console.error('Load categories failed:', e)
  }
}

const loadFields = async (moduleCode: string) => {
  fieldsLoading.value = true
  try {
    const resp = await http.get(`/api/import/config/modules/${moduleCode}/fields`)
    fields.value = resp.data.data || []
  } catch (e) {
    console.error('Load fields failed:', e)
  } finally {
    fieldsLoading.value = false
  }
}

const selectModule = (module: Module) => {
  selectedModule.value = module
  loadFields(module.moduleCode)
}

const showModuleModal = (module?: Module) => {
  editingModule.value = module || null
  if (module) {
    moduleForm.value = { ...module, isActive: !!module.isActive }
  } else {
    moduleForm.value = {
      moduleCode: '',
      moduleName: '',
      tableName: '',
      category: '',
      idCardField: 'ID_CARD',
      certNoField: 'DISABILITY_CERT_NO',
      permissionCode: '',
      sortOrder: 0,
      isActive: true
    }
  }
  moduleModalVisible.value = true
}

const saveModule = async () => {
  saving.value = true
  try {
    const data = { ...moduleForm.value, isActive: moduleForm.value.isActive ? 1 : 0 }
    if (editingModule.value) {
      await http.put(`/api/import/config/modules/${editingModule.value.moduleCode}`, data)
    } else {
      await http.post('/api/import/config/modules', data)
    }
    message.success('保存成功')
    moduleModalVisible.value = false
    loadModules()
  } catch (e: any) {
    message.error(e.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const deleteModule = async (moduleCode: string) => {
  try {
    await http.delete(`/api/import/config/modules/${moduleCode}`)
    message.success('删除成功')
    if (selectedModule.value?.moduleCode === moduleCode) {
      selectedModule.value = null
      fields.value = []
    }
    loadModules()
  } catch (e: any) {
    message.error(e.response?.data?.message || '删除失败')
  }
}

const showFieldModal = (field?: Field) => {
  editingField.value = field || null
  if (field) {
    fieldForm.value = { 
      ...field, 
      isRequired: !!field.isRequired,
      isUnique: !!field.isUnique
    }
  } else {
    fieldForm.value = {
      fieldCode: '',
      fieldName: '',
      dbColumn: '',
      dataType: 'STRING',
      isRequired: false,
      isUnique: false,
      defaultValue: '',
      validationRule: '',
      sortOrder: fields.value.length
    }
  }
  fieldModalVisible.value = true
}

const saveField = async () => {
  if (!selectedModule.value) return
  saving.value = true
  try {
    const data = { 
      ...fieldForm.value, 
      isRequired: fieldForm.value.isRequired ? 1 : 0,
      isUnique: fieldForm.value.isUnique ? 1 : 0
    }
    if (editingField.value) {
      await http.put(`/api/import/config/fields/${editingField.value.id}`, data)
    } else {
      await http.post(`/api/import/config/modules/${selectedModule.value.moduleCode}/fields`, data)
    }
    message.success('保存成功')
    fieldModalVisible.value = false
    loadFields(selectedModule.value.moduleCode)
  } catch (e: any) {
    message.error(e.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const deleteField = async (id: number) => {
  try {
    await http.delete(`/api/import/config/fields/${id}`)
    message.success('删除成功')
    if (selectedModule.value) {
      loadFields(selectedModule.value.moduleCode)
    }
  } catch (e: any) {
    message.error(e.response?.data?.message || '删除失败')
  }
}

const syncFromEnum = async () => {
  syncing.value = true
  try {
    const resp = await http.post('/api/import/config/modules/sync-from-enum')
    const data = resp.data.data
    message.success(`同步完成: 新增 ${data.modules} 个模块`)
    loadModules()
    loadCategories()
  } catch (e: any) {
    message.error(e.response?.data?.message || '同步失败')
  } finally {
    syncing.value = false
  }
}

onMounted(() => {
  loadModules()
  loadCategories()
})
</script>

<style scoped>
.config-container {
  padding: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
}

.content-layout {
  display: flex;
  gap: 16px;
  min-height: calc(100vh - 200px);
}

.module-list {
  width: 280px;
  flex-shrink: 0;
}

.module-detail {
  flex: 1;
  min-width: 0;
}

.module-item {
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.module-item:hover {
  background-color: #f5f5f5;
}

.module-item.active {
  background-color: #e6f4ff;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  border-radius: 8px;
}
</style>
