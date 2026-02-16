import { http } from './http'

export interface ImportModule {
  code: string
  name: string
  category: string
  tableName: string
  fields: ImportModuleField[]
}

export interface ImportModuleField {
  fieldCode: string
  fieldName: string
  dataType: string
  required: boolean
  description?: string
}

export interface ImportBatch {
  batchId: number
  moduleCode: string
  moduleName: string
  strategy: 'FULL_REPLACE' | 'ID_CARD_MERGE'
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED'
  totalRows: number
  validRows: number
  invalidRows: number
  successRows: number
  failedRows: number
  progress: number
  backupId: number | null
  errorMessage?: string
  createdAt: string
  completedAt?: string
}

export interface FieldMapping {
  excelColumn: string
  systemField: string
  fieldName: string
  matched: boolean
  confidence: number
  sampleValues: string[]
}

export interface MappingResponse {
  mappings: FieldMapping[]
  systemFields: ImportModuleField[]
}

export interface PreviewResponse {
  columns: string[]
  rows: PreviewRow[]
  validation: ValidationResult
}

export interface PreviewRow {
  _rowId: number
  _valid: boolean
  _errors?: string[]
  [key: string]: any
}

export interface ValidationResult {
  valid: boolean
  totalRows: number
  validRows: number
  invalidRows: number
  errors: string[]
}

export interface ProgressResponse {
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED'
  progress: number
  totalRows: number
  processedRows: number
  successRows: number
  failedRows: number
  errorMessage?: string
}

export interface BatchListParams {
  page?: number
  size?: number
  module?: string
  status?: string
  startDate?: string
  endDate?: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const ImportApi = {
  getModules() {
    return http.get<ImportModule[]>('/api/import/modules')
  },

  getModuleDetail(moduleCode: string) {
    return http.get<ImportModule>(`/api/import/modules/${moduleCode}`)
  },

  downloadTemplate(moduleCode: string) {
    return http.get(`/api/import/modules/${moduleCode}/template`, { responseType: 'blob' })
  },

  uploadFile(moduleCode: string, file: File, strategy: string) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('strategy', strategy)
    return http.post<{ batchId: number; moduleCode: string }>(
      `/api/import/modules/${moduleCode}/upload`,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    )
  },

  getMapping(batchId: number) {
    return http.get<MappingResponse>(`/api/import/batches/${batchId}/mapping`)
  },

  autoMap(batchId: number) {
    return http.post(`/api/import/batches/${batchId}/auto-map`)
  },

  saveMapping(batchId: number, mappings: FieldMapping[]) {
    return http.post(`/api/import/batches/${batchId}/mapping`, { mappings })
  },

  getPreview(batchId: number) {
    return http.get<PreviewResponse>(`/api/import/batches/${batchId}/preview`)
  },

  getBatch(batchId: number) {
    return http.get<ImportBatch>(`/api/import/batches/${batchId}`)
  },

  executeImport(batchId: number, strategy: string) {
    return http.post(`/api/import/batches/${batchId}/execute`, { strategy })
  },

  getProgress(batchId: number) {
    return http.get<ProgressResponse>(`/api/import/batches/${batchId}/progress`)
  },

  cancelImport(batchId: number) {
    return http.post(`/api/import/batches/${batchId}/cancel`)
  },

  getBatchList(params: BatchListParams) {
    return http.get<PageResponse<ImportBatch>>('/api/import/batches', { params })
  },

  exportErrors(batchId: number) {
    return http.get(`/api/import/batches/${batchId}/errors/export`, { responseType: 'blob' })
  },

  exportFailed(batchId: number) {
    return http.get(`/api/import/batches/${batchId}/failed/export`, { responseType: 'blob' })
  },

  downloadBackup(backupId: number) {
    return http.get(`/api/import/backups/${backupId}/download`, { responseType: 'blob' })
  },

  restoreBackup(backupId: number) {
    return http.post(`/api/import/backups/${backupId}/restore`)
  }
}
