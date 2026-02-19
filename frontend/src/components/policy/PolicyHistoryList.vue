<template>
  <div class="policy-history-list">
    <div class="list-header">
      <h3>历史政策</h3>
      <a-input-search
        v-model:value="searchText"
        placeholder="搜索政策..."
        style="width: 200px"
      />
    </div>
    
    <a-list
      :data-source="filteredList"
      :loading="loading"
      class="policy-list"
    >
      <template #renderItem="{ item }">
        <a-list-item
          :class="['policy-item', { active: selectedId === item.policyId }]"
          @click="selectPolicy(item)"
        >
          <a-list-item-meta>
            <template #title>
              <div class="policy-title">
                <span>{{ item.title }}</span>
                <a-tag v-if="item.latestVersion" color="blue">v{{ item.latestVersion }}</a-tag>
              </div>
            </template>
            <template #description>
              <div class="policy-info">
                <span>{{ formatFileSize(item.fileSize) }}</span>
                <span>{{ formatTime(item.createdAt) }}</span>
              </div>
            </template>
          </a-list-item-meta>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { PolicyApi, type PolicyDocumentVO } from '../../api/policy'
import dayjs from 'dayjs'

const emit = defineEmits<{
  (e: 'select', policy: PolicyDocumentVO): void
}>()

const props = defineProps<{
  refreshTrigger?: number
}>()

const loading = ref(false)
const policyList = ref<PolicyDocumentVO[]>([])
const searchText = ref('')
const selectedId = ref<string | null>(null)

const filteredList = computed(() => {
  if (!searchText.value) return policyList.value
  const search = searchText.value.toLowerCase()
  return policyList.value.filter(item => 
    item.title.toLowerCase().includes(search)
  )
})

const loadPolicyList = async () => {
  loading.value = true
  try {
    const resp = await PolicyApi.getPolicyList()
    policyList.value = resp.data.data
  } catch (error) {
    message.error('加载政策列表失败')
  } finally {
    loading.value = false
  }
}

const selectPolicy = (policy: PolicyDocumentVO) => {
  selectedId.value = policy.policyId
  emit('select', policy)
}

const formatFileSize = (bytes?: number) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  return dayjs(time).format('MM-DD HH:mm')
}

onMounted(loadPolicyList)
watch(() => props.refreshTrigger, loadPolicyList)

defineExpose({ loadPolicyList })
</script>

<style scoped>
.policy-history-list {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.list-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}
.list-header h3 {
  margin: 0 0 12px 0;
}
.policy-list {
  flex: 1;
  overflow-y: auto;
}
.policy-item {
  cursor: pointer;
  padding: 12px 16px;
  transition: all 0.3s;
}
.policy-item:hover {
  background: #f5f5f5;
}
.policy-item.active {
  background: #e6f7ff;
  border-left: 3px solid #1890ff;
}
.policy-title {
  display: flex;
  align-items: center;
  gap: 8px;
}
.policy-info {
  color: #999;
  font-size: 12px;
}
.policy-info span {
  margin-right: 12px;
}
</style>
