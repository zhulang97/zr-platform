import { http } from './http'

export interface PolicyUploadResponse {
  policyId: number
  uploadUrl: string
  ossKey: string
}

export interface PolicyDocument {
  policyId: number
  title: string
  fileName: string
  fileType: string
  fileSize: number
  contentLength: number
  ossUrl: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface PolicyConditions {
  title?: string
  districtIds?: number[]
  disabilityCategories?: string[]
  disabilityLevels?: string[]
  hasCar?: boolean
  hasMedicalSubsidy?: boolean
  hasPensionSubsidy?: boolean
  hasBlindCard?: boolean
  ageMin?: number
  ageMax?: number
  explanation?: string
}

export interface ConditionDiff {
  field: string
  fieldName: string
  oldValue: any
  newValue: any
  diffType: 'ADDED' | 'REMOVED' | 'MODIFIED'
}

export interface PolicyAnalysisResult {
  analysisId: number
  version: number
  isLatest: boolean
  conditions: PolicyConditions
  explanation: string
  analyzedSegments: number
  totalSegments: number
  createdAt: string
  diffs?: ConditionDiff[]
}

export interface PolicyDocumentVO extends PolicyDocument {
  latestVersion?: number
  latestExplanation?: string
}

export interface PolicyQueryRequest {
  analysisId: number
  conditions?: PolicyConditions
  pageNo?: number
  pageSize?: number
}

export interface PolicyQueryResult {
  policyId: number
  policyTitle: string
  version: number
  conditions: PolicyConditions
  total: number
  items: any[]
}

export const PolicyApi = {
  // 获取OSS上传URL
  getUploadUrl(fileName: string, fileType: string) {
    return http.post<PolicyUploadResponse>('/api/policies/upload-url', { fileName, fileType })
  },

  // 确认上传完成
  confirmUpload(policyId: number) {
    return http.post(`/api/policies/${policyId}/confirm`)
  },

  // 分析政策
  analyzePolicy(policyId: number, content?: string) {
    return http.post<PolicyAnalysisResult>(`/api/policies/${policyId}/analyze`, { content })
  },

  // 根据政策查询人员
  queryPersons(request: PolicyQueryRequest) {
    return http.post<PolicyQueryResult>('/api/policies/query', request)
  },

  // 获取政策列表
  getPolicyList() {
    return http.get<PolicyDocumentVO[]>('/api/policies')
  },

  // 获取政策详情
  getPolicyDetail(policyId: number) {
    return http.get<PolicyDocument>(`/api/policies/${policyId}`)
  },

  // 获取政策所有版本
  getPolicyVersions(policyId: number) {
    return http.get<PolicyAnalysisResult[]>(`/api/policies/${policyId}/versions`)
  },

  // 更新政策标题
  updateTitle(policyId: number, title: string) {
    return http.put(`/api/policies/${policyId}/title`, { title })
  },

  // 删除政策
  deletePolicy(policyId: number) {
    return http.delete(`/api/policies/${policyId}`)
  },

  // 获取PDF预览信息
  getPdfPreviewInfo(policyId: number) {
    return http.get<{
      isPdf: boolean
      isConverted: boolean
      pageCount: number
      imageUrls?: string[]
      message?: string
    }>(`/api/policies/${policyId}/preview`)
  },

  // 转换PDF为图片
  convertPdfToImages(policyId: number) {
    return http.post<string[]>(`/api/policies/${policyId}/convert`)
  }
}
