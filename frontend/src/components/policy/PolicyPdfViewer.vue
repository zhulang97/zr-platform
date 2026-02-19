<template>
  <div class="pdf-viewer">
    <a-card size="small" class="viewer-card">
      <template #title>
        <div class="viewer-header">
          <span>PDF预览</span>
          <a-space>
            <a-button
              v-if="!isConverted && !converting"
              type="primary"
              size="small"
              @click="convertPdf"
              :disabled="!isPdf"
            >
              <picture-outlined /> 转换为图片
            </a-button>
            <span v-if="converting" class="converting-text">
              <loading-outlined /> 转换中...
            </span>
            <span v-if="isConverted" class="converted-text">
              <check-circle-outlined /> 已转换
            </span>
          </a-space>
        </div>
      </template>
      
      <!-- 非PDF提示 -->
      <a-alert
        v-if="!isPdf"
        message="非PDF文件"
        description="该文件不是PDF格式，无法使用图片预览功能。请直接查看文本内容。"
        type="info"
        show-icon
      />
      
      <!-- 需要转换提示 -->
      <a-alert
        v-else-if="!isConverted && !converting"
        message="需要转换"
        description="PDF文件需要转换为图片才能预览。点击上方按钮开始转换。"
        type="warning"
        show-icon
      />
      
      <!-- 转换进度 -->
      <div v-else-if="converting" class="converting-progress">
        <a-progress :percent="conversionProgress" status="active" />
        <p>正在将PDF转换为图片，请稍候...</p>
      </div>
      
      <!-- 图片预览 -->
      <div v-else-if="isConverted && imageUrls.length > 0" class="image-preview">
        <div class="preview-toolbar">
          <a-space>
            <a-button size="small" @click="zoomOut">
              <zoom-out-outlined />
            </a-button>
            <span>{{ Math.round(scale * 100) }}%</span>
            <a-button size="small" @click="zoomIn">
              <zoom-in-outlined />
            </a-button>
            <a-divider type="vertical" />
            <span>第 {{ currentPage }} / {{ imageUrls.length }} 页</span>
            <a-divider type="vertical" />
            <a-button size="small" :disabled="currentPage <= 1" @click="prevPage">
              <left-outlined />
            </a-button>
            <a-button size="small" :disabled="currentPage >= imageUrls.length" @click="nextPage">
              <right-outlined />
            </a-button>
          </a-space>
        </div>
        
        <div class="image-container" ref="imageContainer">
          <img
            v-for="(url, index) in imageUrls"
            :key="index"
            :src="url"
            :alt="`第${index + 1}页`"
            class="preview-image"
            :style="{ 
              transform: `scale(${scale})`,
              display: currentPage === index + 1 ? 'block' : 'none'
            }"
            @load="onImageLoad"
          />
        </div>
        
        <!-- 缩略图列表 -->
        <div class="thumbnail-list">
          <div
            v-for="(url, index) in imageUrls"
            :key="index"
            :class="['thumbnail', { active: currentPage === index + 1 }]"
            @click="currentPage = index + 1"
          >
            <img :src="url" :alt="`第${index + 1}页`" />
            <span class="page-number">{{ index + 1 }}</span>
          </div>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PictureOutlined,
  LoadingOutlined,
  CheckCircleOutlined,
  ZoomOutOutlined,
  ZoomInOutlined,
  LeftOutlined,
  RightOutlined
} from '@ant-design/icons-vue'
import { PolicyApi } from '../../api/policy'

const props = defineProps<{
  policyId: string
  isPdf: boolean
}>()

const isConverted = ref(false)
const converting = ref(false)
const conversionProgress = ref(0)
const imageUrls = ref<string[]>([])
const pageCount = ref(0)
const currentPage = ref(1)
const scale = ref(1)
const loadedImages = ref(0)

// 获取PDF预览信息
const loadPreviewInfo = async () => {
  if (!props.isPdf || !props.policyId) return
  
  try {
    const resp = await PolicyApi.getPdfPreviewInfo(props.policyId)
    const data = resp.data.data
    
    isConverted.value = data.isConverted
    pageCount.value = data.pageCount
    
    if (data.isConverted && data.imageUrls) {
      imageUrls.value = data.imageUrls
    }
  } catch (error) {
    console.error('加载PDF预览信息失败', error)
  }
}

// 转换PDF为图片
const convertPdf = async () => {
  if (!props.isPdf || !props.policyId) return
  
  converting.value = true
  conversionProgress.value = 0
  loadedImages.value = 0
  
  try {
    // 模拟进度
    const progressInterval = setInterval(() => {
      if (conversionProgress.value < 80) {
        conversionProgress.value += Math.random() * 15
      }
    }, 500)
    
    const resp = await PolicyApi.convertPdfToImages(props.policyId)
    
    clearInterval(progressInterval)
    conversionProgress.value = 100
    
    imageUrls.value = resp.data.data
    isConverted.value = true
    pageCount.value = imageUrls.value.length
    currentPage.value = 1
    
    message.success(`PDF已成功转换为${imageUrls.value.length}张图片`)
  } catch (error: any) {
    message.error('转换失败：' + (error?.response?.data?.message || '请重试'))
  } finally {
    converting.value = false
  }
}

// 图片加载完成
const onImageLoad = () => {
  loadedImages.value++
}

// 缩放控制
const zoomIn = () => {
  if (scale.value < 3) {
    scale.value += 0.2
  }
}

const zoomOut = () => {
  if (scale.value > 0.4) {
    scale.value -= 0.2
  }
}

// 翻页
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const nextPage = () => {
  if (currentPage.value < imageUrls.value.length) {
    currentPage.value++
  }
}

// 监听policyId变化
watch(() => props.policyId, (newVal) => {
  if (newVal) {
    loadPreviewInfo()
  }
}, { immediate: true })

onMounted(() => {
  if (props.policyId && props.isPdf) {
    loadPreviewInfo()
  }
})
</script>

<style scoped>
.pdf-viewer {
  height: 100%;
}
.viewer-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.viewer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.converting-text {
  color: #1890ff;
}
.converted-text {
  color: #52c41a;
}
.converting-progress {
  padding: 40px;
  text-align: center;
}
.image-preview {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.preview-toolbar {
  padding: 12px;
  background: #f5f5f5;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: center;
}
.image-container {
  flex: 1;
  overflow: auto;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 20px;
  background: #f0f0f0;
}
.preview-image {
  max-width: 100%;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  transition: transform 0.2s;
  transform-origin: center top;
}
.thumbnail-list {
  display: flex;
  gap: 8px;
  padding: 12px;
  background: #f5f5f5;
  border-top: 1px solid #e8e8e8;
  overflow-x: auto;
  max-height: 100px;
}
.thumbnail {
  position: relative;
  cursor: pointer;
  border: 2px solid transparent;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
}
.thumbnail.active {
  border-color: #1890ff;
}
.thumbnail img {
  width: 60px;
  height: 80px;
  object-fit: cover;
}
.thumbnail .page-number {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.6);
  color: white;
  text-align: center;
  font-size: 12px;
  padding: 2px 0;
}
</style>
