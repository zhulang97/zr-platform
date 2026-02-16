import { defineStore } from 'pinia'
import { emitter } from '../utils/eventBus'

export interface QueryFilters {
  nameLike: string
  idNo: string
  disabilityCardNo: string
  districtIds: string[]
  streetIds: string[]
  disabilityCategories: string[]
  disabilityLevels: string[]
  hasCar: boolean | null
  hasMedicalSubsidy: boolean | null
  hasPensionSubsidy: boolean | null
  hasBlindCard: boolean | null
}

export interface AnomalyFilters {
  nameLike: string
  idNo: string
  disabilityCardNo: string
  anomalyType: string
  status: string
  pageNo: number
  pageSize: number
}

export interface StatsFilters {
  districtIds: string[]
}

export const useFilterStore = defineStore('filters', {
  state: () => ({
    // 各页面筛选器状态
    queryFilters: {
      nameLike: '',
      idNo: '',
      disabilityCardNo: '',
      districtIds: [],
      streetIds: [],
      disabilityCategories: [],
      disabilityLevels: [],
      hasCar: null,
      hasMedicalSubsidy: null,
      hasPensionSubsidy: null,
      hasBlindCard: null
    } as QueryFilters,
    
    anomalyFilters: {
      nameLike: '',
      idNo: '',
      disabilityCardNo: '',
      anomalyType: '',
      status: '',
      pageNo: 1,
      pageSize: 20
    } as AnomalyFilters,
    
    statsFilters: {
      districtIds: []
    } as StatsFilters
  }),

  actions: {
    setFilters(page: string, filters: Record<string, any>) {
      console.log(`Setting filters for ${page}:`, filters)
      
      switch (page) {
        case 'query':
        case 'home':
          this.queryFilters = { ...this.queryFilters, ...this.convertFilters(filters, 'query') }
          break
        case 'anomaly':
          this.anomalyFilters = { ...this.anomalyFilters, ...this.convertFilters(filters, 'anomaly') }
          break
        case 'stats':
          this.statsFilters = { ...this.statsFilters, ...this.convertFilters(filters, 'stats') }
          break
      }
      
      // 触发页面刷新
      this.triggerPageRefresh(page)
    },

    convertFilters(filters: Record<string, any>, page: string): Record<string, any> {
      const converted: Record<string, any> = {}
      
      for (const [key, value] of Object.entries(filters)) {
        if (value === null || value === undefined) continue
        
        // 转换数组格式
        if (Array.isArray(value)) {
          converted[key] = value.map(v => String(v))
        } else if (typeof value === 'boolean') {
          converted[key] = value
        } else {
          converted[key] = value
        }
      }
      
      return converted
    },

    triggerPageRefresh(page: string) {
      console.log(`Triggering refresh for ${page}`)
      emitter.emit(`refresh:${page}` as any)
    },

    clearFilters(page: string) {
      switch (page) {
        case 'query':
        case 'home':
          this.queryFilters = {
            nameLike: '',
            idNo: '',
            disabilityCardNo: '',
            districtIds: [],
            streetIds: [],
            disabilityCategories: [],
            disabilityLevels: [],
            hasCar: null,
            hasMedicalSubsidy: null,
            hasPensionSubsidy: null,
            hasBlindCard: null
          }
          break
        case 'anomaly':
          this.anomalyFilters = {
            nameLike: '',
            idNo: '',
            disabilityCardNo: '',
            anomalyType: '',
            status: '',
            pageNo: 1,
            pageSize: 20
          }
          break
        case 'stats':
          this.statsFilters = {
            districtIds: []
          }
          break
      }
      this.triggerPageRefresh(page)
    }
  }
})
