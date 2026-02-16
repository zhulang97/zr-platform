<template>
  <div class="ai-assistant-container" :style="containerStyle">
    <!-- 悬浮按钮 -->
    <Transition name="fab-bounce">
      <div 
        v-if="!isOpen" 
        class="fab-button" 
        @mousedown="startDrag"
        @click="handleClick"
      >
        <div class="fab-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="currentColor" opacity="0.8"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="fab-glow"></div>
        <div class="fab-pulse"></div>
      </div>
    </Transition>

    <!-- 聊天面板 -->
    <Transition name="panel-slide">
      <div v-if="isOpen" class="assistant-panel">
        <!-- 头部 -->
        <div class="panel-header">
          <div class="header-left">
            <div class="ai-avatar">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" fill="url(#avatarGradient)"/>
                <path d="M8 12C8 9.79086 9.79086 8 12 8C14.2091 8 16 9.79086 16 12C16 13.3807 15.4132 14.6269 14.5156 15.5156" stroke="white" stroke-width="1.5" stroke-linecap="round"/>
                <circle cx="9" cy="11" r="1" fill="white"/>
                <circle cx="15" cy="11" r="1" fill="white"/>
                <defs>
                  <linearGradient id="avatarGradient" x1="2" y1="2" x2="22" y2="22">
                    <stop stop-color="#667eea"/>
                    <stop offset="1" stop-color="#764ba2"/>
                  </linearGradient>
                </defs>
              </svg>
            </div>
            <div class="header-info">
              <span class="ai-name">智联助手</span>
              <span class="ai-status">
                <span class="status-dot"></span>
                在线
              </span>
            </div>
          </div>
          <div class="header-right">
            <!-- 缩放控制 -->
            <div class="zoom-controls">
              <a-tooltip title="放大">
                <button class="zoom-btn" @click="zoomIn">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="11" cy="11" r="8"/>
                    <path d="M21 21l-4.35-4.35M11 8v6M8 11h6"/>
                  </svg>
                </button>
              </a-tooltip>
              <span class="zoom-level">{{ fontSize }}px</span>
              <a-tooltip title="缩小">
                <button class="zoom-btn" @click="zoomOut">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="11" cy="11" r="8"/>
                    <path d="M21 21l-4.35-4.35M8 11h6"/>
                  </svg>
                </button>
              </a-tooltip>
            </div>
            <a-tooltip title="收起">
              <button class="close-btn" @click="toggleOpen">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 6L6 18M6 6l12 12"/>
                </svg>
              </button>
            </a-tooltip>
          </div>
        </div>

        <!-- 消息区域 -->
        <div class="messages-container" ref="messagesContainer">
          <TransitionGroup name="message" tag="div" class="messages-list">
            <div 
              v-for="(msg, idx) in messages" 
              :key="idx" 
              class="message-item"
              :class="msg.role"
              :style="{ fontSize: fontSize + 'px' }"
            >
              <div class="message-avatar">
                <svg v-if="msg.role === 'user'" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="url(#msgGradient)">
                  <circle cx="12" cy="12" r="10"/>
                  <path d="M8 12h8M12 8v8" stroke="white" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </div>
              <div class="message-content">
                <div class="message-bubble" :style="{ fontSize: fontSize + 'px' }">
                  {{ msg.text }}
                </div>
                <div class="message-time">{{ msg.time }}</div>
              </div>
            </div>
          </TransitionGroup>
          <div v-if="loading" class="loading-indicator">
            <span class="loading-dot"></span>
            <span class="loading-dot"></span>
            <span class="loading-dot"></span>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-container">
          <div class="input-wrapper">
            <input 
              v-model="input" 
              @keydown.enter="send"
              placeholder="试试说：帮我查浦东新区的异常记录"
              class="ai-input"
              :style="{ fontSize: fontSize + 'px' }"
            />
            <button class="send-btn" @click="send" :disabled="!input.trim() || loading">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/>
              </svg>
            </button>
          </div>
          <div class="input-tips">
            💡 支持智能查询、页面跳转、数据筛选
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { http } from '../../api/http'
import { useAIController, AIResponse } from '../../composables/useAIController'

type Msg = { role: string; text: string; time: string }

const input = ref('')
const loading = ref(false)
const isOpen = ref(false)
const fontSize = ref(14)
const messagesContainer = ref<HTMLElement | null>(null)
const route = useRoute()
const { handleAIResponse } = useAIController()

// 拖拽相关
const position = ref({ x: 24, y: window.innerHeight - 88 })
const isDragging = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const hasDragged = ref(false)

const containerStyle = computed(() => ({
  right: `${position.value.x}px`,
  bottom: `${position.value.y}px`,
  left: 'auto',
  top: 'auto'
}))

const messages = ref<Msg[]>([
  { 
    role: 'assistant', 
    text: '你好！我是智联助手，可以帮你：\n• 查询残疾人数据\n• 跳转到指定页面\n• 筛选和过滤数据\n• 分析统计数据\n\n试试说"帮我查浦东新区二级肢体残疾"或"跳转到统计页面"',
    time: getTime()
  }
])

function getTime() {
  return new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function toggleOpen() {
  isOpen.value = !isOpen.value
}

function zoomIn() {
  if (fontSize.value < 20) fontSize.value += 2
}

function zoomOut() {
  if (fontSize.value > 10) fontSize.value -= 2
}

function startDrag(e: MouseEvent) {
  isDragging.value = true
  hasDragged.value = false
  dragStart.value = {
    x: e.clientX,
    y: e.clientY
  }
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

function onDrag(e: MouseEvent) {
  if (!isDragging.value) return
  
  const deltaX = dragStart.value.x - e.clientX
  const deltaY = dragStart.value.y - e.clientY
  
  if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
    hasDragged.value = true
  }
  
  const newX = Math.max(24, Math.min(window.innerWidth - 100, position.value.x + deltaX))
  const newY = Math.max(100, Math.min(window.innerHeight - 100, position.value.y + deltaY))
  
  position.value = { x: newX, y: newY }
  dragStart.value = { x: e.clientX, y: e.clientY }
}

function stopDrag() {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

function handleClick() {
  if (!hasDragged.value) {
    toggleOpen()
  }
}

async function send() {
  const text = input.value.trim()
  if (!text) return
  
  messages.value.push({ role: 'user', text, time: getTime() })
  input.value = ''
  loading.value = true
  
  await nextTick()
  scrollToBottom()
  
  try {
    const resp = await http.post('/api/assistant/chat', { 
      text,
      currentPage: route.path.replace('/', '') || 'home'
    })
    
    const data = resp.data?.data
    
    if (data) {
      const aiResponse: AIResponse = {
        action: data.action || 'ANSWER_ONLY',
        dsl: data.dsl,
        explanation: data.explanation,
        navigation: data.navigation,
        targetPage: data.targetPage
      }
      
      await handleAIResponse(aiResponse)
      
      const answerText = data.explanation || data.answer || '已完成操作'
      messages.value.push({ role: 'assistant', text: answerText, time: getTime() })
    } else {
      messages.value.push({ role: 'assistant', text: '抱歉，无法理解您的请求', time: getTime() })
    }
  } catch (error: any) {
    messages.value.push({ 
      role: 'assistant', 
      text: '请求失败：' + (error?.response?.data?.message || '请稍后重试'),
      time: getTime()
    })
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
})
</script>

<style scoped>
.ai-assistant-container {
  position: fixed;
  z-index: 9999;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 悬浮按钮 */
.fab-button {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  cursor: grab;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    0 4px 15px rgba(102, 126, 234, 0.4),
    0 0 30px rgba(102, 126, 234, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
}

.fab-button:active {
  cursor: grabbing;
}

.fab-button:hover {
  transform: scale(1.1);
  box-shadow: 
    0 6px 25px rgba(102, 126, 234, 0.5),
    0 0 50px rgba(102, 126, 234, 0.3);
}

.fab-icon {
  width: 32px;
  height: 32px;
  color: white;
  z-index: 1;
}

.fab-glow {
  position: absolute;
  inset: -2px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  filter: blur(10px);
  opacity: 0.5;
}

.fab-pulse {
  position: absolute;
  inset: -5px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.2); opacity: 0; }
}

/* 面板 */
.assistant-panel {
  width: 400px;
  height: 560px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.15),
    0 0 1px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

/* 头部 */
.panel-header {
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.ai-avatar svg {
  width: 100%;
  height: 100%;
}

.header-info {
  display: flex;
  flex-direction: column;
}

.ai-name {
  color: white;
  font-size: 16px;
  font-weight: 600;
}

.ai-status {
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #52c41a;
  border-radius: 50%;
  animation: blink 2s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.zoom-controls {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 4px 8px;
}

.zoom-btn {
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.2s;
}

.zoom-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.zoom-btn svg {
  width: 16px;
  height: 16px;
}

.zoom-level {
  color: white;
  font-size: 12px;
  min-width: 28px;
  text-align: center;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: rotate(90deg);
}

.close-btn svg {
  width: 18px;
  height: 18px;
}

/* 消息区域 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f8f9fc;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 10px;
  max-width: 90%;
}

.message-item.user {
  flex-direction: row-reverse;
  align-self: flex-end;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  color: #666;
}

.message-avatar svg {
  width: 20px;
  height: 20px;
}

.message-item.user .message-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-item.user .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message-time {
  font-size: 11px;
  color: #999;
  padding: 0 4px;
}

.message-item.user .message-time {
  text-align: right;
}

/* 加载动画 */
.loading-indicator {
  display: flex;
  gap: 6px;
  padding: 12px 16px;
}

.loading-dot {
  width: 8px;
  height: 8px;
  background: #667eea;
  border-radius: 50%;
  animation: loadingBounce 1.4s ease-in-out infinite;
}

.loading-dot:nth-child(2) { animation-delay: 0.2s; }
.loading-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes loadingBounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

/* 输入区域 */
.input-container {
  padding: 16px;
  background: white;
  border-top: 1px solid #f0f0f0;
}

.input-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
  background: #f5f7fc;
  border-radius: 24px;
  padding: 8px 12px;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.input-wrapper:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.ai-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  color: #333;
  line-height: 1.5;
}

.ai-input::placeholder {
  color: #999;
}

.send-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.1);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn svg {
  width: 18px;
  height: 18px;
}

.input-tips {
  margin-top: 10px;
  font-size: 12px;
  color: #999;
  text-align: center;
}

/* 动画 */
.fab-bounce-enter-active {
  animation: fabIn 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.fab-bounce-leave-active {
  animation: fabOut 0.3s ease-in;
}

@keyframes fabIn {
  0% { transform: scale(0) rotate(-180deg); opacity: 0; }
  100% { transform: scale(1) rotate(0); opacity: 1; }
}

@keyframes fabOut {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(0); opacity: 0; }
}

.panel-slide-enter-active {
  animation: panelIn 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.panel-slide-leave-active {
  animation: panelOut 0.3s ease-in;
}

@keyframes panelIn {
  0% { transform: scale(0.8) translateY(20px); opacity: 0; }
  100% { transform: scale(1) translateY(0); opacity: 1; }
}

@keyframes panelOut {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(0.8) translateY(20px); opacity: 0; }
}

.message-enter-active {
  animation: msgIn 0.3s ease-out;
}

@keyframes msgIn {
  0% { transform: translateY(10px); opacity: 0; }
  100% { transform: translateY(0); opacity: 1; }
}
</style>
