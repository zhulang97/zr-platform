import { useRouter, useRoute } from 'vue-router'
import { useFilterStore } from '../stores/filters'
import { emitter } from '../utils/eventBus'
import { message } from 'ant-design-vue'

export interface AIResponse {
  action: string
  dsl?: {
    intent?: string
    filters?: Record<string, any>
    targetPage?: {
      page?: string
      tab?: string
    }
    navigation?: {
      path?: string
      query?: Record<string, string>
      newTab?: boolean
    }
    // 地图联动相关
    personId?: number
    personIds?: number[]
    districtId?: number
    streetId?: number
    longitude?: number
    latitude?: number
    zoom?: number
    categoryCode?: string
    levelCode?: string
  }
  explanation?: string
  navigation?: {
    path?: string
    query?: Record<string, string>
    newTab?: boolean
  }
  targetPage?: {
    page?: string
    tab?: string
  }
}

export function useAIController() {
  const router = useRouter()
  const route = useRoute()
  const filterStore = useFilterStore()

  const handleAIResponse = async (response: AIResponse) => {
    console.log('AI Response:', response)
    
    const { action, dsl, explanation, navigation, targetPage } = response
    
    // 显示解释信息
    if (explanation) {
      message.info(explanation)
    }

    switch (action) {
      case 'NAVIGATE':
        await handleNavigate(navigation || dsl?.navigation)
        break
      case 'SET_FILTERS':
        await handleSetFilters(dsl?.targetPage?.page || 'query', dsl?.filters)
        break
      case 'NAVIGATE_AND_FILTER':
        await handleNavigateAndFilter(dsl)
        break
      case 'REFRESH':
        await handleRefresh(dsl?.targetPage?.page || route.path.replace('/', ''))
        break
      case 'SHOW_ON_MAP':
        await handleShowOnMap(dsl)
        break
      case 'ZOOM_TO_LOCATION':
        await handleZoomToLocation(dsl?.longitude, dsl?.latitude, dsl?.zoom)
        break
      case 'FILTER_MAP_MARKERS':
        await handleFilterMapMarkers(dsl)
        break
      case 'ANSWER_ONLY':
        // 仅显示文本回答，不做其他操作
        break
      default:
        console.warn('Unknown AI action:', action)
    }
  }

  const handleNavigate = async (nav?: AIResponse['navigation']) => {
    if (!nav?.path) {
      console.warn('No navigation path provided')
      return
    }

    if (nav.newTab) {
      const queryString = nav.query 
        ? '?' + new URLSearchParams(nav.query).toString()
        : ''
      window.open(`${nav.path}${queryString}`, '_blank')
    } else {
      await router.push({
        path: nav.path,
        query: nav.query
      })
    }
  }

  const handleSetFilters = async (page: string, filters?: Record<string, any>) => {
    if (!filters) {
      console.warn('No filters provided')
      return
    }

    filterStore.setFilters(page, filters)
  }

  const handleNavigateAndFilter = async (dsl?: AIResponse['dsl']) => {
    if (!dsl) {
      console.warn('No DSL provided')
      return
    }

    const targetPage = dsl.targetPage?.page || 'query'
    
    // 先设置筛选器
    if (dsl.filters) {
      filterStore.setFilters(targetPage, dsl.filters)
    }

    // 再导航到目标页面
    const pathMap: Record<string, string> = {
      'query': '/home',
      'anomaly': '/anomaly',
      'stats': '/stats',
      'sys': '/sys',
      'ai': '/ai'
    }

    const path = pathMap[targetPage] || '/home'
    
    // 如果已经在目标页面，直接刷新
    if (route.path === path) {
      emitter.emit(`refresh:${targetPage}`)
    } else {
      await router.push({ path })
    }
  }

  const handleRefresh = async (page: string) => {
    emitter.emit(`refresh:${page}`)
  }

  // 在地图上显示指定人员
  const handleShowOnMap = async (dsl?: AIResponse['dsl']) => {
    // 先导航到大屏页面
    if (route.path !== '/home') {
      await router.push({ path: '/home' })
    }
    
    // 触发地图事件
    if (dsl?.personId) {
      emitter.emit('map:showPerson', { personId: dsl.personId })
    } else if (dsl?.personIds && dsl.personIds.length > 0) {
      emitter.emit('map:showPersons', { personIds: dsl.personIds })
    }
  }

  // 缩放到指定位置
  const handleZoomToLocation = async (longitude?: number, latitude?: number, zoom?: number) => {
    if (longitude === undefined || latitude === undefined) {
      console.warn('No location provided for zoom')
      return
    }
    
    // 先导航到大屏页面
    if (route.path !== '/home') {
      await router.push({ path: '/home' })
    }
    
    // 触发地图缩放事件
    emitter.emit('map:zoomTo', { longitude, latitude, zoom: zoom || 16 })
  }

  // 筛选地图标记点
  const handleFilterMapMarkers = async (dsl?: AIResponse['dsl']) => {
    // 先导航到大屏页面
    if (route.path !== '/home') {
      await router.push({ path: '/home' })
    }
    
    // 触发地图筛选事件
    emitter.emit('map:filter', {
      districtId: dsl?.districtId,
      streetId: dsl?.streetId,
      categoryCode: dsl?.categoryCode,
      levelCode: dsl?.levelCode
    })
  }

  return {
    handleAIResponse,
    handleNavigate,
    handleSetFilters,
    handleNavigateAndFilter,
    handleRefresh,
    handleShowOnMap,
    handleZoomToLocation,
    handleFilterMapMarkers
  }
}
