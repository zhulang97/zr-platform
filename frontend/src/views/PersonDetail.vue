<template>
  <a-card>
    <template #title>
      <a-space>
        <a-button @click="goBack">
          <LeftOutlined /> 返回
        </a-button>
        <span style="font-weight: 600;">{{ basic?.name || '' }} 的个人档案</span>
      </a-space>
    </template>
    <template #extra>
      <a-button type="primary" @click="openFieldConfig">
        <SettingOutlined /> 配置展示字段
      </a-button>
    </template>

    <a-spin :spinning="loading">
      <a-alert v-if="!loading && !detail" type="warning" message="未找到该人员信息" style="margin-bottom: 16px;" />

      <div v-if="detail" class="detail-content">
        <!-- 基本信息 -->
        <div class="info-card">
          <div class="card-title"><UserOutlined /> 基本信息</div>
          <div class="card-body">
            <div class="info-row">
              <div class="info-item"><span class="label">姓名</span><span class="value">{{ basic?.name || '-' }}</span></div>
              <div class="info-item"><span class="label">性别</span><span class="value">{{ basic?.gender || '-' }}</span></div>
              <div class="info-item"><span class="label">年龄</span><span class="value">{{ basic?.age || '-' }}</span></div>
            </div>
            <div class="info-row">
              <div class="info-item"><span class="label">身份证</span><span class="value id-card">{{ maskIdCard(basic?.idCard) }}</span></div>
              <div class="info-item"><span class="label">电话</span><span class="value">{{ basic?.phone || '-' }}</span></div>
            </div>
            <div class="info-row">
              <div class="info-item full"><span class="label">地址</span><span class="value">{{ basic?.contactAddress || '-' }}</span></div>
            </div>
          </div>
        </div>

        <!-- 残疾信息 -->
        <div class="info-card">
          <div class="card-title"><MedicineBoxOutlined /> 残疾信息</div>
          <div class="card-body">
            <div class="info-row">
              <div class="info-item"><span class="label">残疾类别</span><span class="value">{{ disability?.category || '-' }}</span></div>
              <div class="info-item"><span class="label">残疾等级</span><span class="value">{{ disability?.level || '-' }}</span></div>
              <div class="info-item"><span class="label">残疾证号</span><span class="value">{{ maskCertNo(disability?.certNo) }}</span></div>
            </div>
          </div>
        </div>

        <!-- 业务模块 -->
        <a-collapse v-model:activeKey="activeCategories" v-if="modules" class="module-collapse">
          <a-collapse-panel v-for="(items, category) in modules" :key="category">
            <template #header>
              <span class="category-header">{{ category }}</span>
              <a-badge :count="items.length" :number-style="{ backgroundColor: '#52c41a' }" style="margin-left: 8px;" />
            </template>
            <div class="module-list">
              <div v-for="item in items" :key="item.moduleCode" class="module-row">
                <span class="module-name">{{ item.moduleName }}</span>
                <span class="module-values">
                  <template v-if="item.fieldValues && item.fieldValues.length > 0">
                    <span v-for="(fv, idx) in item.fieldValues" :key="idx" class="field-value-item">
                      {{ fv.name }}: {{ fv.value || '-' }}
                    </span>
                  </template>
                  <template v-else>
                    <span class="no-data">无详细数据</span>
                  </template>
                </span>
                <a-tag color="green">有数据</a-tag>
              </div>
            </div>
          </a-collapse-panel>
        </a-collapse>

        <a-empty v-if="Object.keys(modules || {}).length === 0" description="暂无业务数据" />
      </div>
    </a-spin>

    <!-- 字段配置弹窗 -->
    <a-modal
      v-model:open="configVisible"
      title="配置展示字段"
      width="500px"
      :footer="null"
    >
      <a-spin :spinning="configLoading">
        <a-select
          v-model:value="selectedModuleCode"
          style="width: 100%; margin-bottom: 16px;"
          placeholder="选择模块"
          @change="loadModuleFields"
        >
          <a-select-option v-for="m in moduleList" :key="m.moduleCode" :value="m.moduleCode">
            {{ m.moduleName }}
          </a-select-option>
        </a-select>

        <div v-if="moduleFields.length > 0" class="field-config-list">
          <div class="field-config-header">
            <span class="col-name">字段名</span>
            <span class="col-display">展示</span>
          </div>
          <div v-for="field in moduleFields" :key="field.id" class="field-config-row">
            <span class="col-name">{{ field.fieldName }}</span>
            <span class="col-display">
              <a-switch v-model:checked="field.isDisplayChecked" size="small" />
            </span>
          </div>
          <div style="margin-top: 16px; text-align: right;">
            <a-button type="primary" @click="saveFieldConfig" :loading="saveLoading">保存配置</a-button>
          </div>
        </div>
      </a-spin>
    </a-modal>
  </a-card>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { http } from '../api/http'
import { message } from 'ant-design-vue'
import { LeftOutlined, SettingOutlined, UserOutlined, MedicineBoxOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const detail = ref<any>(null)
const activeCategories = ref<string[]>([])

const configVisible = ref(false)
const configLoading = ref(false)
const saveLoading = ref(false)
const moduleList = ref<any[]>([])
const selectedModuleCode = ref('')
const moduleFields = ref<any[]>([])

const basic = computed(() => detail.value?.basic)
const disability = computed(() => detail.value?.disability)
const modules = computed(() => detail.value?.modules)

function maskIdCard(idCard: string | undefined) {
  if (!idCard) return '-'
  return idCard.substring(0, 6) + '********' + idCard.substring(14)
}

function maskCertNo(certNo: string | undefined) {
  if (!certNo) return '-'
  if (certNo.length <= 8) return '********'
  return certNo.substring(0, 4) + '****' + certNo.substring(certNo.length - 4)
}

function goBack() {
  router.push('/person-profile')
}

async function openFieldConfig() {
  configVisible.value = true
  configLoading.value = true
  try {
    const resp = await http.get('/api/import/config/modules')
    moduleList.value = resp.data.data || []
  } catch (e: any) {
    message.error('加载模块列表失败')
  } finally {
    configLoading.value = false
  }
}

async function loadModuleFields() {
  if (!selectedModuleCode.value) return
  configLoading.value = true
  try {
    const resp = await http.get(`/api/import/config/modules/${selectedModuleCode.value}/fields`)
    moduleFields.value = (resp.data.data || []).map((f: any) => ({
      ...f,
      isDisplayChecked: f.isDisplay !== 0
    }))
  } catch (e: any) {
    message.error('加载字段列表失败')
  } finally {
    configLoading.value = false
  }
}

async function saveFieldConfig() {
  saveLoading.value = true
  try {
    const updates = moduleFields.value.map(f => ({
      id: f.id,
      isDisplay: f.isDisplayChecked ? 1 : 0
    }))
    
    await http.put(`/api/import/config/modules/${selectedModuleCode.value}/fields/display`, { fields: updates })
    message.success('保存成功')
    loadDetail()
  } catch (e: any) {
    message.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

async function loadDetail() {
  const idCard = route.params.idCard as string
  if (!idCard || idCard.length < 5) return
  
  loading.value = true
  try {
    const resp = await http.get(`/api/persons/by-idcard/${idCard}/full-detail`)
    detail.value = resp.data.data
    
    if (detail.value?.modules) {
      activeCategories.value = Object.keys(detail.value.modules)
    }
  } catch (e: any) {
    message.error('加载失败: ' + (e?.response?.data?.message || ''))
  } finally {
    loading.value = false
  }
}

watch(() => route.params.idCard, loadDetail)
onMounted(loadDetail)
</script>

<style scoped>
.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.card-title {
  padding: 12px 16px;
  font-weight: 600;
  font-size: 14px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-body {
  padding: 16px;
}

.info-row {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 150px;
}

.info-item.full {
  flex: 1;
}

.label {
  color: #8c8c8c;
  font-size: 13px;
  white-space: nowrap;
}

.value {
  color: #262626;
  font-size: 14px;
  font-weight: 500;
}

.id-card {
  font-family: monospace;
  letter-spacing: 1px;
}

.module-collapse {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.module-collapse :deep(.ant-collapse-item) {
  border-bottom: 1px solid #f0f0f0;
}

.module-collapse :deep(.ant-collapse-header) {
  padding: 14px 16px !important;
  font-weight: 500;
}

.category-header {
  font-size: 14px;
}

.module-list {
  padding: 8px 0;
}

.module-row {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  gap: 16px;
  border-bottom: 1px solid #f5f5f5;
}

.module-row:last-child {
  border-bottom: none;
}

.module-name {
  width: 140px;
  flex-shrink: 0;
  font-weight: 500;
  color: #262626;
}

.module-values {
  flex: 1;
  color: #595959;
  font-size: 13px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.field-value-item {
  white-space: nowrap;
}

.no-data {
  color: #999;
}

.field-config-list {
  border: 1px solid #f0f0f0;
  border-radius: 4px;
}

.field-config-header {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  background: #fafafa;
  font-weight: 500;
  font-size: 13px;
}

.field-config-row {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-top: 1px solid #f0f0f0;
}

.col-name { flex: 1; }
.col-display { width: 60px; text-align: center; }
</style>
