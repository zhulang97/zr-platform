<template>
  <div class="policy-search-page">
    <a-page-header title="政策找人" sub-title="上传政策文件，AI自动分析条件并查找符合条件的人员">
      <template #extra>
        <a-button type="primary" @click="showUpload = true">
          <upload-outlined /> 上传政策
        </a-button>
      </template>
    </a-page-header>
    
    <div class="main-content">
      <a-row :gutter="24">
        <!-- 左侧：历史政策列表 -->
        <a-col :span="6">
          <PolicyHistoryList
            ref="historyListRef"
            :refresh-trigger="refreshTrigger"
            @select="handlePolicySelect"
          />
        </a-col>
        
        <!-- 中间：政策预览和分析 -->
        <a-col :span="10">
          <div v-if="!currentPolicy" class="empty-state">
            <a-empty description="请选择或上传政策文件">
              <template #extra>
                <a-button type="primary" @click="showUpload = true">
                  上传政策
                </a-button>
              </template>
            </a-empty>
          </div>
          
          <div v-else class="policy-detail">
            <a-card :title="currentPolicy.title" size="small">
              <template #extra>
                <a-space>
                  <a-tag color="blue">v{{ currentVersion }}</a-tag>
                  <a-button 
                    type="primary" 
                    size="small"
                    :loading="analyzing"
                    @click="analyzeCurrentPolicy"
                  >
                    <search-outlined /> 分析政策
                  </a-button>
                </a-space>
              </template>
              
              <!-- 政策信息 -->
              <div class="policy-meta">
                <span><file-outlined /> {{ currentPolicy.fileName }}</span>
                <span>{{ formatFileSize(currentPolicy.fileSize) }}</span>
                <span>{{ formatTime(currentPolicy.createdAt) }}</span>
              </div>
              
              <!-- PDF预览 -->
              <PolicyPdfViewer
                v-if="currentPolicy && isPdfFile"
                :policy-id="currentPolicy.policyId"
                :is-pdf="isPdfFile"
                style="margin-top: 16px; height: 500px;"
              />
              
              <!-- 分析进度 -->
              <div v-if="analyzing" class="analysis-progress">
                <a-progress
                  :percent="analysisProgress"
                  status="active"
                  :format="() => `分析中...(${analyzedSegments}/${totalSegments})`"
                />
              </div>
            </a-card>
            
            <!-- 分析结果 -->
            <a-card v-if="currentAnalysis" title="AI分析结果" size="small" style="margin-top: 16px;">
              <div class="analysis-result">
                <div class="result-header">
                  <h4>提取的条件</h4>
                  <a-button type="link" size="small" @click="showConditionEditor = true">
                    <edit-outlined /> 编辑条件
                  </a-button>
                </div>
                
                <div class="conditions-display">
                  <div v-if="currentAnalysis.conditions?.districtIds?.length" class="condition-tag">
                    <span class="label">户籍：</span>
                    <a-tag v-for="id in currentAnalysis.conditions.districtIds" :key="id">
                      {{ getDistrictName(id) }}
                    </a-tag>
                  </div>
                  
                  <div v-if="currentAnalysis.conditions?.disabilityCategories?.length" class="condition-tag">
                    <span class="label">残疾类别：</span>
                    <a-tag v-for="cat in currentAnalysis.conditions.disabilityCategories" :key="cat">
                      {{ getCategoryName(cat) }}
                    </a-tag>
                  </div>
                  
                  <div v-if="currentAnalysis.conditions?.disabilityLevels?.length" class="condition-tag">
                    <span class="label">残疾等级：</span>
                    <a-tag v-for="level in currentAnalysis.conditions.disabilityLevels" :key="level">
                      {{ level }}级
                    </a-tag>
                  </div>
                  
                  <div class="condition-tag">
                    <span class="label">补贴要求：</span>
                    <a-tag v-if="currentAnalysis.conditions?.hasCar" color="green">残车补贴</a-tag>
                    <a-tag v-if="currentAnalysis.conditions?.hasMedicalSubsidy" color="green">医疗补贴</a-tag>
                    <a-tag v-if="currentAnalysis.conditions?.hasPensionSubsidy" color="green">养老补贴</a-tag>
                    <a-tag v-if="currentAnalysis.conditions?.hasBlindCard" color="green">盲人证</a-tag>
                  </div>
                </div>
                
                <a-divider />
                
                <div class="result-actions">
                  <a-button 
                    type="primary" 
                    size="large"
                    block
                    :loading="querying"
                    @click="queryPersons"
                  >
                    <search-outlined /> 查询符合条件人员
                  </a-button>
                </div>
              </div>
            </a-card>
          </div>
        </a-col>
        
        <!-- 右侧：条件编辑和版本 -->
        <a-col :span="8">
          <div v-if="currentAnalysis">
            <PolicyConditionEditor
              :conditions="editableConditions"
              @update="handleConditionsUpdate"
              @save="handleConditionsSave"
            />
          </div>
        </a-col>
      </a-row>
    </div>
    
    <!-- 上传弹窗 -->
    <a-modal
      v-model:open="showUpload"
      title="上传政策文件"
      width="800px"
      :footer="null"
    >
      <PolicyUploader
        ref="uploaderRef"
        @upload-success="handleUploadSuccess"
        @analyze="handleManualAnalyze"
      />
    </a-modal>
    
    <!-- 查询结果弹窗 -->
    <a-modal
      v-model:open="showResults"
      title="查询结果"
      width="1200px"
      :footer="null"
    >
      <PolicyQueryResult
        :result="queryResult"
        @close="showResults = false"
      />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  UploadOutlined,
  SearchOutlined,
  FileOutlined,
  EditOutlined
} from '@ant-design/icons-vue'
import PolicyHistoryList from '../components/policy/PolicyHistoryList.vue'
import PolicyUploader from '../components/policy/PolicyUploader.vue'
import PolicyConditionEditor from '../components/policy/PolicyConditionEditor.vue'
import PolicyQueryResult from '../components/policy/PolicyQueryResult.vue'
import PolicyPdfViewer from '../components/policy/PolicyPdfViewer.vue'
import { PolicyApi, type PolicyDocumentVO, type PolicyAnalysisResult, type PolicyConditions, type PolicyQueryResult as QueryResultType } from '../api/policy'
import dayjs from 'dayjs'
import { computed } from 'vue'

const historyListRef = ref()
const uploaderRef = ref()
const showUpload = ref(false)
const showResults = ref(false)
const refreshTrigger = ref(0)

const currentPolicy = ref<PolicyDocumentVO | null>(null)
const currentAnalysis = ref<PolicyAnalysisResult | null>(null)
const editableConditions = ref<PolicyConditions | null>(null)
const currentVersion = ref(0)

const analyzing = ref(false)
const analysisProgress = ref(0)
const analyzedSegments = ref(0)
const totalSegments = ref(0)

const querying = ref(false)
const queryResult = ref<QueryResultType | null>(null)

// 判断当前政策是否是PDF文件
const isPdfFile = computed(() => {
  if (!currentPolicy.value) return false
  const fileType = currentPolicy.value.fileType
  const fileName = currentPolicy.value.fileName || ''
  return fileType === 'application/pdf' || fileName.toLowerCase().endsWith('.pdf')
})

const handlePolicySelect = async (policy: PolicyDocumentVO) => {
  currentPolicy.value = policy
  // 加载最新版本
  try {
    const resp = await PolicyApi.getPolicyVersions(policy.policyId)
    const versions = resp.data.data
    if (versions.length > 0) {
      currentAnalysis.value = versions[0]
      editableConditions.value = versions[0].conditions
      currentVersion.value = versions[0].version
    }
  } catch (error) {
    console.error('加载版本失败', error)
  }
}

const handleUploadSuccess = async (data: any) => {
  showUpload.value = false
  refreshTrigger.value++
  // 自动选择新上传的政策
  await handlePolicySelect({
    policyId: data.policyId,
    title: data.file?.name || '未命名政策',
    fileName: data.file?.name,
    fileType: data.file?.type,
    fileSize: data.file?.size,
    ossUrl: data.uploadUrl,
    status: 'ACTIVE',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  } as PolicyDocumentVO)
  
  // 自动开始分析
  setTimeout(() => {
    analyzeCurrentPolicy()
  }, 500)
}

const handleManualAnalyze = async (policyId: string, content?: string) => {
  showUpload.value = false
  analyzing.value = true
  try {
    const resp = await PolicyApi.analyzePolicy(policyId, content)
    currentAnalysis.value = resp.data.data
    editableConditions.value = resp.data.data.conditions
    currentVersion.value = resp.data.data.version
    message.success('分析完成')
    refreshTrigger.value++
  } catch (error) {
    message.error('分析失败')
  } finally {
    analyzing.value = false
  }
}

const analyzeCurrentPolicy = async () => {
  if (!currentPolicy.value) return
  
  analyzing.value = true
  analysisProgress.value = 0
  analyzedSegments.value = 0
  totalSegments.value = Math.ceil((currentPolicy.value.contentLength || 1000) / 4000)
  
  try {
    // 模拟进度
    const progressInterval = setInterval(() => {
      if (analysisProgress.value < 90) {
        analysisProgress.value += Math.random() * 10
        analyzedSegments.value = Math.floor(analysisProgress.value / 100 * totalSegments.value)
      }
    }, 500)
    
    const resp = await PolicyApi.analyzePolicy(currentPolicy.value.policyId)
    
    clearInterval(progressInterval)
    analysisProgress.value = 100
    analyzedSegments.value = totalSegments.value
    
    currentAnalysis.value = resp.data.data
    editableConditions.value = resp.data.data.conditions
    currentVersion.value = resp.data.data.version
    
    message.success('分析完成！')
    refreshTrigger.value++
  } catch (error) {
    message.error('分析失败，请重试')
  } finally {
    analyzing.value = false
  }
}

const handleConditionsUpdate = (conditions: PolicyConditions) => {
  editableConditions.value = conditions
}

const handleConditionsSave = (conditions: PolicyConditions) => {
  editableConditions.value = conditions
  message.success('条件已更新')
}

const queryPersons = async () => {
  if (!currentAnalysis.value || !editableConditions.value) {
    message.warning('请先分析政策')
    return
  }
  
  querying.value = true
  try {
    const resp = await PolicyApi.queryPersons({
      analysisId: currentAnalysis.value.analysisId,
      conditions: editableConditions.value,
      pageNo: 1,
      pageSize: 20
    })
    queryResult.value = resp.data.data
    showResults.value = true
  } catch (error) {
    message.error('查询失败')
  } finally {
    querying.value = false
  }
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
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const getDistrictName = (id: number) => {
  const map: Record<number, string> = {
    310115: '浦东新区', 310101: '黄浦区', 310106: '静安区',
    310104: '徐汇区', 310105: '长宁区', 310107: '普陀区',
    310109: '虹口区', 310110: '杨浦区', 310112: '闵行区',
    310113: '宝山区', 310114: '嘉定区', 310116: '金山区',
    310117: '松江区', 310118: '青浦区', 310120: '奉贤区',
    310151: '崇明区'
  }
  return map[id] || id
}

const getCategoryName = (cat: string) => {
  const map: Record<string, string> = {
    LIMB: '肢体', VISION: '视力', HEARING: '听力',
    SPEECH: '言语', INTELLECTUAL: '智力', MENTAL: '精神'
  }
  return map[cat] || cat
}
</script>

<style scoped>
.policy-search-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.main-content {
  flex: 1;
  padding: 24px;
  overflow: hidden;
}
.main-content :deep(.ant-row) {
  height: 100%;
}
.main-content :deep(.ant-col) {
  height: 100%;
}
.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.policy-detail {
  height: 100%;
  overflow-y: auto;
}
.policy-meta {
  color: #666;
  font-size: 12px;
}
.policy-meta span {
  margin-right: 16px;
}
.analysis-progress {
  margin-top: 16px;
}
.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.result-header h4 {
  margin: 0;
}
.conditions-display {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.condition-tag {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.condition-tag .label {
  color: #666;
  min-width: 80px;
}
.result-actions {
  margin-top: 24px;
}
</style>
