<template>
  <div class="dashboard-page">
    <!-- 顶部统计栏 -->
    <div class="stats-bar">
      <div class="stat-card" v-for="(stat, index) in statsData" :key="stat.title">
        <div class="stat-icon" :style="{ background: stat.gradient }">
          <component :is="stat.icon" />
        </div>
        <div class="stat-content">
          <div class="stat-value" :style="{ color: stat.color }">
            <span class="value-number">{{ animatedValues[index] }}</span>
            <span v-if="stat.suffix" class="stat-suffix">{{ stat.suffix }}</span>
          </div>
          <div class="stat-title">{{ stat.title }}</div>
          <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
            <ArrowUpOutlined v-if="stat.trend > 0" />
            <ArrowDownOutlined v-else />
            {{ Math.abs(stat.trend) }}%
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区 -->
    <div class="main-content">
      <!-- 地图区域 -->
      <div class="map-section" :class="{ 'fullscreen': isFullscreen }">
        <div class="section-header">
          <div class="header-left">
            <GlobalOutlined class="header-icon" />
            <span class="header-title">长宁区残疾人分布地图</span>
          </div>
          <div class="header-right">
            <a-input-search
              v-model:value="searchAddress"
              placeholder="搜索地址或姓名"
              class="search-input"
              @search="onSearch"
              enter-button
            />
            <a-button @click="refreshData" :loading="isRefreshing">
              <ReloadOutlined />
              刷新
            </a-button>
            <a-button @click="resetMap">
              <CompassOutlined />
              重置
            </a-button>
            <a-button type="primary" @click="toggleFullscreen">
              <component :is="isFullscreen ? ShrinkOutlined : ExpandOutlined" />
              {{ isFullscreen ? '退出' : '全屏' }}
            </a-button>
          </div>
        </div>
        <div class="map-wrapper">
          <div id="map-container" class="map-container"></div>
          <!-- 图例 -->
          <div class="map-legend">
            <div class="legend-items">
              <div v-for="item in legendItems" :key="item.type" class="legend-item">
                <span class="legend-dot" :style="{ backgroundColor: item.color }"></span>
                <span class="legend-label">{{ item.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧边栏 -->
      <div class="sidebar-section">
        <!-- 实时动态 -->
        <div class="sidebar-card">
          <div class="card-title">
            <ClockCircleOutlined class="card-icon" />
            <span>实时动态</span>
            <span class="live-badge"></span>
          </div>
          <div class="activity-list">
            <div v-for="(item, idx) in recentActivities" :key="idx" class="activity-item">
              <div class="activity-icon" :style="{ backgroundColor: item.color + '20', color: item.color }">
                <component :is="item.icon" />
              </div>
              <div class="activity-content">
                <div class="activity-title">{{ item.title }}</div>
                <div class="activity-desc">{{ item.description }}</div>
                <div class="activity-time">{{ item.time }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分布统计 -->
        <div class="sidebar-card">
          <div class="card-title">
            <PieChartOutlined class="card-icon" />
            <span>残疾类别分布</span>
          </div>
          <div id="category-chart" class="chart-container"></div>
        </div>

        <!-- 街道统计 -->
        <div class="sidebar-card">
          <div class="card-title">
            <TrophyOutlined class="card-icon" />
            <span>街道统计 TOP5</span>
          </div>
          <div class="street-list">
            <div v-for="(item, index) in streetStats" :key="item.name" class="street-item">
              <span class="rank-num" :class="{ 'top3': index < 3 }">{{ index + 1 }}</span>
              <div class="street-info">
                <span class="street-name">{{ item.name }}</span>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: `${(item.count / 1256) * 100}%` }"></div>
                </div>
              </div>
              <span class="street-count">{{ item.count }}人</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import * as echarts from 'echarts'
import {
  UserOutlined,
  PlusCircleOutlined,
  WarningOutlined,
  EnvironmentOutlined,
  GlobalOutlined,
  ReloadOutlined,
  CompassOutlined,
  ExpandOutlined,
  ShrinkOutlined,
  ClockCircleOutlined,
  PieChartOutlined,
  TrophyOutlined,
  NotificationOutlined,
  SyncOutlined,
  CheckCircleOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined
} from '@ant-design/icons-vue'
import { emitter } from '../utils/eventBus'
import { http } from '../api/http'

let map: any = null
let markers: any[] = []
let infoWindow: any = null

const isFullscreen = ref(false)
const isRefreshing = ref(false)

const animatedValues = ref([0, 0, 0, 0])

const statsData = [
  {
    title: '持证残疾人总数',
    value: 12568,
    icon: UserOutlined,
    color: '#667eea',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    trend: 5.2
  },
  {
    title: '今日新增',
    value: 8,
    icon: PlusCircleOutlined,
    color: '#10b981',
    gradient: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
    trend: 12.8
  },
  {
    title: '异常提醒',
    value: 23,
    icon: WarningOutlined,
    color: '#f43f5e',
    gradient: 'linear-gradient(135deg, #f43f5e 0%, #e11d48 100%)',
    trend: -3.5
  },
  {
    title: '覆盖街道/居委',
    value: 185,
    suffix: '个',
    icon: EnvironmentOutlined,
    color: '#f59e0b',
    gradient: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
    trend: 2.1
  }
]

const animateNumbers = () => {
  const duration = 2000
  const steps = 60
  const interval = duration / steps
  
  statsData.forEach((stat, index) => {
    const target = stat.value
    const increment = target / steps
    let step = 0
    
    const timer = setInterval(() => {
      step++
      animatedValues.value[index] = Math.min(Math.round(increment * step), target)
      if (step >= steps) {
        clearInterval(timer)
        animatedValues.value[index] = target
      }
    }, interval)
  })
}

const searchAddress = ref('')

const legendItems = [
  { type: 'LIMB', label: '肢体残疾', color: '#f43f5e' },
  { type: 'VISION', label: '视力残疾', color: '#667eea' },
  { type: 'HEARING', label: '听力残疾', color: '#10b981' },
  { type: 'SPEECH', label: '言语残疾', color: '#f59e0b' },
  { type: 'INTELLECTUAL', label: '智力残疾', color: '#8b5cf6' },
  { type: 'MENTAL', label: '精神残疾', color: '#ec4899' }
]

const recentActivities = ref([
  { title: '新增持证人员', description: '张三，新华街道，二级肢体残疾', time: '10分钟前', icon: PlusCircleOutlined, color: '#10b981' },
  { title: '异常提醒', description: '发现人车分离异常，请及时核实', time: '25分钟前', icon: NotificationOutlined, color: '#f43f5e' },
  { title: '信息更新', description: '李四地址变更已确认', time: '1小时前', icon: SyncOutlined, color: '#667eea' },
  { title: '补贴审核', description: '本月残车补贴审核完成', time: '2小时前', icon: CheckCircleOutlined, color: '#8b5cf6' }
])

const streetStats = ref([
  { name: '新华街道', count: 1256 },
  { name: '江苏路街道', count: 1145 },
  { name: '华阳路街道', count: 1089 },
  { name: '周家桥街道', count: 956 },
  { name: '天山路街道', count: 892 }
])

const mockPersons = [
  { id: 1, name: '张**', address: '长宁路100号', longitude: 121.4035, latitude: 31.2198, category: 'LIMB', level: '二级', district: '新华街道', benefits: ['残车', '医疗'] },
  { id: 2, name: '李**', address: '定西路200号', longitude: 121.4142, latitude: 31.2185, category: 'VISION', level: '一级', district: '新华街道', benefits: ['盲人证', '养老'] },
  { id: 3, name: '王**', address: '愚园路300号', longitude: 121.4221, latitude: 31.2215, category: 'HEARING', level: '三级', district: '江苏路街道', benefits: ['医疗'] },
  { id: 4, name: '刘**', address: '武夷路400号', longitude: 121.4089, latitude: 31.2156, category: 'LIMB', level: '一级', district: '华阳路街道', benefits: ['残车', '医疗', '养老'] },
  { id: 5, name: '陈**', address: '虹桥路500号', longitude: 121.3978, latitude: 31.2034, category: 'MENTAL', level: '二级', district: '周家桥街道', benefits: ['医疗'] },
  { id: 6, name: '杨**', address: '天山路600号', longitude: 121.3956, latitude: 31.2123, category: 'SPEECH', level: '四级', district: '天山路街道', benefits: [] },
  { id: 7, name: '赵**', address: '仙霞路700号', longitude: 121.3856, latitude: 31.2089, category: 'INTELLECTUAL', level: '二级', district: '仙霞新村街道', benefits: ['医疗', '养老'] },
  { id: 8, name: '黄**', address: '虹桥路800号', longitude: 121.4012, latitude: 31.2056, category: 'LIMB', level: '三级', district: '程家桥街道', benefits: ['残车'] }
]

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  nextTick(() => {
    if (map) map.resize()
  })
}

const initMap = () => {
  if (typeof AMap === 'undefined') {
    message.error('地图加载失败')
    initDemoMap()
    return
  }
  
  map = new AMap.Map('map-container', {
    zoom: 13,
    center: [121.385, 31.205],
    viewMode: '2D',
    mapStyle: 'amap://styles/normal',
    zooms: [12, 18],
    dragEnable: true,
    zoomEnable: true
  })
  
  AMap.plugin('AMap.DistrictSearch', () => {
    const districtSearch = new AMap.DistrictSearch({
      level: 'district',
      showbiz: false,
      extensions: 'all'
    })
    
    districtSearch.search('长宁区', (status: any, result: any) => {
      if (status === 'complete' && result.districtList[0]) {
        const district = result.districtList[0]
        if (district.boundaries && district.boundaries.length > 0) {
          const boundary = district.boundaries[0]
          addMaskOverlay(boundary)
          addDistrictBoundary(boundary)
        }
      }
    })
  })
  
  addMarkers()
  map.addControl(new AMap.Scale())
  map.addControl(new AMap.ToolBar({ position: 'LB' }))
}

const addMaskOverlay = (boundary: number[][]) => {
  const outer = [
    [-180, 90],
    [180, 90],
    [180, -90],
    [-180, -90],
    [-180, 90]
  ]
  
  const maskPolygon = new AMap.Polygon({
    path: [outer, boundary],
    strokeColor: '#667eea',
    strokeWeight: 3,
    strokeOpacity: 1,
    fillColor: '#f0f2f5',
    fillOpacity: 0.9
  })
  
  map.add(maskPolygon)
}

const addDistrictBoundary = (boundary: number[][]) => {
  const polygon = new AMap.Polygon({
    path: boundary,
    strokeColor: '#667eea',
    strokeWeight: 3,
    strokeOpacity: 0.8,
    fillColor: '#667eea',
    fillOpacity: 0.05,
    strokeStyle: 'solid'
  })
  map.add(polygon)
}

const addMarkers = () => {
  markers.forEach(marker => map.remove(marker))
  markers = []
  
  mockPersons.forEach(person => {
    const categoryInfo = legendItems.find(item => item.type === person.category)
    const color = categoryInfo?.color || '#667eea'
    
    const marker = new AMap.Marker({
      position: [person.longitude, person.latitude],
      title: person.name,
      icon: new AMap.Icon({
        size: new AMap.Size(24, 34),
        imageSize: new AMap.Size(24, 34),
        image: createMarkerIcon(color)
      }),
      extData: person
    })
    
    marker.on('click', () => showInfoWindow(person))
    map.add(marker)
    markers.push(marker)
  })
}

const createMarkerIcon = (color: string) => {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="34" viewBox="0 0 24 34">
      <path fill="${color}" stroke="#fff" stroke-width="2"
        d="M12 0C5.373 0 0 5.373 0 12c0 9 12 22 12 22s12-13 12-22c0-6.627-5.373-12-12-12z"/>
      <circle fill="#fff" cx="12" cy="12" r="5"/>
    </svg>
  `
  return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg)
}

const showInfoWindow = (person: any) => {
  const categoryInfo = legendItems.find(item => item.type === person.category)
  
  const content = `
    <div style="padding: 16px; min-width: 220px;">
      <h4 style="margin: 0 0 12px 0; font-size: 16px; color: #667eea; border-bottom: 1px solid #eee; padding-bottom: 8px;">
        ${person.name}
      </h4>
      <div style="margin-bottom: 6px; color: #666;"><span style="color: #999;">地址：</span>${person.address}</div>
      <div style="margin-bottom: 6px; color: #666;"><span style="color: #999;">街道：</span>${person.district}</div>
      <div style="margin-bottom: 6px; color: #666;"><span style="color: #999;">残疾类别：</span><span style="color: ${categoryInfo?.color}; font-weight: 600;">${categoryInfo?.label}</span></div>
      <div style="margin-bottom: 6px; color: #666;"><span style="color: #999;">残疾等级：</span>${person.level}</div>
      <div style="color: #666;"><span style="color: #999;">享受补贴：</span>${person.benefits.length > 0 ? person.benefits.join('、') : '无'}</div>
    </div>
  `
  
  infoWindow = new AMap.InfoWindow({
    content: content,
    offset: new AMap.Pixel(0, -34)
  })
  
  infoWindow.open(map, [person.longitude, person.latitude])
}

const onSearch = () => {
  if (!searchAddress.value) {
    message.warning('请输入搜索内容')
    return
  }
  
  const found = mockPersons.find(p => 
    p.address.includes(searchAddress.value) || 
    p.name.includes(searchAddress.value)
  )
  
  if (found) {
    map.setZoomAndCenter(16, [found.longitude, found.latitude])
    showInfoWindow(found)
  } else {
    message.info('未找到匹配的人员')
  }
}

const refreshData = () => {
  isRefreshing.value = true
  setTimeout(() => {
    isRefreshing.value = false
    message.success('数据已更新')
  }, 1500)
}

const resetMap = () => {
  if (map) {
    map.setZoomAndCenter(13, [121.385, 31.205])
    if (infoWindow) infoWindow.close()
  }
}

const initCharts = () => {
  const categoryChart = echarts.init(document.getElementById('category-chart'))
  categoryChart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { 
        show: true,
        formatter: '{b}\n{c}',
        fontSize: 11,
        color: '#666'
      },
      data: [
        { value: 4568, name: '肢体残疾', itemStyle: { color: '#f43f5e' } },
        { value: 2345, name: '视力残疾', itemStyle: { color: '#667eea' } },
        { value: 1987, name: '听力残疾', itemStyle: { color: '#10b981' } },
        { value: 1234, name: '言语残疾', itemStyle: { color: '#f59e0b' } },
        { value: 1456, name: '智力残疾', itemStyle: { color: '#8b5cf6' } },
        { value: 978, name: '精神残疾', itemStyle: { color: '#ec4899' } }
      ]
    }]
  })
  
  window.addEventListener('resize', () => categoryChart.resize())
}

const initDemoMap = () => {
  const container = document.getElementById('map-container')
  if (!container) return
  
  const chart = echarts.init(container)
  const data = mockPersons.map(p => ({
    name: p.name,
    value: [p.longitude, p.latitude],
    category: p.category
  }))
  
  chart.setOption({
    backgroundColor: '#f5f7fa',
    title: {
      text: '长宁区残疾人分布示意图',
      left: 'center',
      top: 16,
      textStyle: { color: '#666', fontSize: 14 }
    },
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => `<b>${params.name}</b>`
    },
    xAxis: { show: false, min: 121.376, max: 121.436 },
    yAxis: { show: false, min: 31.194, max: 31.244 },
    grid: { left: 10, right: 10, top: 50, bottom: 10 },
    series: [{
      type: 'scatter',
      symbolSize: 12,
      data: data,
      itemStyle: {
        color: (params: any) => {
          const cat = legendItems.find(l => l.type === params.data.category)
          return cat?.color || '#667eea'
        }
      }
    }]
  })
}

onMounted(async () => {
  animateNumbers()
  
  try {
    const configResp = await http.get('/api/config/amap')
    const data = configResp.data?.data || configResp.data
    
    if (!data || !data.key || data.key === 'your-amap-key' || data.key.length < 10) {
      initDemoMap()
      initCharts()
      return
    }
    
    if (data.securityCode) {
      window._AMapSecurityConfig = { securityJsCode: data.securityCode }
    }
    
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${data.key}&plugin=AMap.Scale,AMap.ToolBar`
    script.onload = () => initMap()
    script.onerror = () => initDemoMap()
    document.head.appendChild(script)
  } catch {
    initDemoMap()
  }
  
  nextTick(() => initCharts())
  
  emitter.on('map:zoomTo', (data: any) => {
    if (map && data.longitude && data.latitude) {
      map.setZoomAndCenter(data.zoom || 16, [data.longitude, data.latitude])
    }
  })
  
  emitter.on('map:filter', (filters: any) => {
    console.log('Map filter:', filters)
  })
  
  emitter.on('map:showPerson', (data: any) => {
    if (data.personId && markers.length > 0) {
      const targetMarker = markers.find(m => m.getExtData().id === data.personId)
      if (targetMarker) {
        const person = targetMarker.getExtData()
        map.setZoomAndCenter(16, [person.longitude, person.latitude])
        showInfoWindow(person)
      }
    }
  })
})

onUnmounted(() => {
  emitter.off('map:zoomTo')
  emitter.off('map:filter')
  emitter.off('map:showPerson')
  if (map) map.destroy()
})
</script>

<style scoped>
.dashboard-page {
  padding: 20px;
  min-height: calc(100vh - 64px);
  background: #f0f2f5;
}

/* 统计卡片 */
.stats-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 4px;
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stat-suffix {
  font-size: 14px;
  font-weight: 500;
  opacity: 0.7;
}

.stat-title {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.stat-trend {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
}

.stat-trend.up {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
}

.stat-trend.down {
  color: #f43f5e;
  background: rgba(244, 63, 94, 0.1);
}

/* 主内容区 */
.main-content {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 16px;
  flex: 1;
}

/* 地图区域 */
.map-section {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.map-section.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  border-radius: 0;
}

.section-header {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafafa;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 18px;
  color: #667eea;
}

.header-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-input {
  width: 200px;
}

.map-wrapper {
  position: relative;
  flex: 1;
  min-height: 400px;
}

.map-container {
  width: 100%;
  height: 100%;
}

.map-legend {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}

.legend-items {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

/* 侧边栏 */
.sidebar-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sidebar-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.card-title {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fafafa;
}

.card-icon {
  color: #667eea;
}

.live-badge {
  margin-left: auto;
  width: 8px;
  height: 8px;
  background: #10b981;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 活动列表 */
.activity-list {
  padding: 8px;
}

.activity-item {
  padding: 10px 12px;
  display: flex;
  gap: 12px;
  border-radius: 8px;
  transition: background 0.2s;
  cursor: pointer;
}

.activity-item:hover {
  background: #f5f5f5;
}

.activity-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-title {
  font-weight: 600;
  color: #1a1a2e;
  font-size: 13px;
  margin-bottom: 2px;
}

.activity-desc {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-time {
  font-size: 11px;
  color: #bfbfbf;
}

/* 图表 */
.chart-container {
  height: 180px;
  padding: 8px;
}

/* 街道统计 */
.street-list {
  padding: 8px;
}

.street-item {
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-radius: 8px;
  transition: background 0.2s;
  cursor: pointer;
}

.street-item:hover {
  background: #f5f5f5;
}

.rank-num {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #8c8c8c;
  flex-shrink: 0;
}

.rank-num.top3 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.street-info {
  flex: 1;
  min-width: 0;
}

.street-name {
  font-size: 13px;
  color: #1a1a2e;
  display: block;
  margin-bottom: 4px;
}

.progress-bar {
  height: 4px;
  background: #f0f0f0;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
  transition: width 1s ease-out;
}

.street-count {
  font-size: 14px;
  font-weight: 600;
  color: #667eea;
}

/* 响应式 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
  }
  
  .sidebar-section {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .stats-bar {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .sidebar-section {
    grid-template-columns: 1fr;
  }
}
</style>
