<template>
  <div class="step1-container">
    <div class="layout">
      <!-- 左侧分类 -->
      <div class="category-panel">
        <div class="panel-title">模块分类</div>
        <a-menu
          v-model:selectedKeys="selectedCategory"
          mode="inline"
          class="category-menu"
        >
          <a-menu-item key="all">
            <AppstoreOutlined />
            <span>全部模块</span>
          </a-menu-item>
          <a-menu-item v-for="cat in categories" :key="cat">
            <FolderOutlined />
            <span>{{ cat }}</span>
            <span class="count">{{ getCategoryCount(cat) }}</span>
          </a-menu-item>
        </a-menu>
      </div>

      <!-- 右侧模块列表 -->
      <div class="module-panel">
        <div class="panel-header">
          <a-input-search
            v-model:value="searchKeyword"
            placeholder="搜索模块名称或字段..."
            class="search-input"
          />
          <span class="selected-count">
            已选 {{ selectedModules.length }} 个模块
          </span>
        </div>

        <div class="module-grid">
          <div
            v-for="module in filteredModules"
            :key="module.code"
            class="module-card"
            :class="{ selected: selectedModules.includes(module.code), disabled: !module.hasPermission }"
            @click="toggleModule(module)"
          >
            <div class="module-header">
              <a-checkbox 
                :checked="selectedModules.includes(module.code)"
                :disabled="!module.hasPermission"
              />
              <span class="module-name">{{ module.name }}</span>
            </div>
            <div class="module-info">
              <span class="info-item">
                <FieldStringOutlined /> {{ module.fieldCount }} 个字段
              </span>
            </div>
            <a-tag v-if="!module.hasPermission" color="red" size="small">无权限</a-tag>
          </div>
        </div>

        <div class="actions">
          <a-button @click="$emit('prev')">取消</a-button>
          <a-button type="primary" :disabled="selectedModules.length === 0" @click="$emit('next')">
            下一步
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { 
  AppstoreOutlined, 
  FolderOutlined, 
  FieldStringOutlined 
} from '@ant-design/icons-vue'
import { http } from '@/api/http'

interface Module {
  code: string
  name: string
  category: string
  fieldCount: number
  hasPermission: boolean
}

const props = defineProps<{
  selectedModules: string[]
}>()

const emit = defineEmits<{
  'update:selectedModules': [value: string[]]
  'next': []
  'prev': []
}>()

const modules = ref<Module[]>([])
const categories = ref<string[]>([])
const selectedCategory = ref(['all'])
const searchKeyword = ref('')
const loading = ref(false)

const filteredModules = computed(() => {
  let result = modules.value
  
  if (selectedCategory.value[0] !== 'all') {
    result = result.filter(m => m.category === selectedCategory.value[0])
  }
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(m => 
      m.name.toLowerCase().includes(keyword) ||
      m.code.toLowerCase().includes(keyword)
    )
  }
  
  return result
})

const getCategoryCount = (category: string) => {
  return modules.value.filter(m => m.category === category).length
}

const toggleModule = (module: Module) => {
  if (!module.hasPermission) {
    message.warning('您没有该模块的导入权限')
    return
  }
  
  const newSelected = [...props.selectedModules]
  const index = newSelected.indexOf(module.code)
  
  if (index > -1) {
    newSelected.splice(index, 1)
  } else {
    newSelected.push(module.code)
  }
  
  emit('update:selectedModules', newSelected)
}

const loadModules = async () => {
  loading.value = true
  try {
    const resp = await http.get('/api/import/modules')
    modules.value = resp.data.data || []
    categories.value = [...new Set(modules.value.map(m => m.category))]
  } catch (e: any) {
    message.error('加载模块列表失败')
    // 模拟数据
    modules.value = [
      { code: 'disabled_cert', name: '残疾人认证', category: '基本保障', fieldCount: 20, hasPermission: true },
      { code: 'disabled_mgmt', name: '残疾管理', category: '基本保障', fieldCount: 19, hasPermission: true },
      { code: 'rehab_subsidy', name: '康复医疗/器材补助', category: '医疗康复', fieldCount: 9, hasPermission: true },
      { code: 'pension_subsidy', name: '养老补助', category: '基本保障', fieldCount: 8, hasPermission: true },
      { code: 'employment', name: '就业', category: '就业培训', fieldCount: 24, hasPermission: false },
      { code: 'vocational_training', name: '职业技能培训', category: '就业培训', fieldCount: 9, hasPermission: true },
    ]
    categories.value = [...new Set(modules.value.map(m => m.category))]
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadModules()
})
</script>

<style scoped>
.step1-container {
  height: 100%;
}

.layout {
  display: flex;
  gap: 20px;
  height: 100%;
}

.category-panel {
  width: 200px;
  flex-shrink: 0;
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 12px;
}

.category-menu {
  border: none;
  background: transparent;
}

.category-menu :deep(.ant-menu-item) {
  margin: 4px 0;
  border-radius: 6px;
}

.category-menu .count {
  margin-left: auto;
  background: #e6f7ff;
  padding: 0 8px;
  border-radius: 10px;
  font-size: 12px;
  color: #1890ff;
}

.module-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.search-input {
  width: 300px;
}

.selected-count {
  color: #1890ff;
  font-weight: 500;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
  flex: 1;
  overflow-y: auto;
}

.module-card {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.module-card:hover:not(.disabled) {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

.module-card.selected {
  border-color: #1890ff;
  background: #e6f7ff;
}

.module-card.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.module-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.module-name {
  font-weight: 500;
  color: #262626;
}

.module-info {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #8c8c8c;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
