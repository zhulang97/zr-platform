<template>
  <div class="login-page">
    <!-- 左侧装饰区 -->
    <div class="login-left">
      <div class="left-content">
        <div class="brand-section">
          <div class="brand-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h1 class="brand-title">智联助残数据平台</h1>
          <p class="brand-desc">让数据更有温度，让服务更加精准</p>
        </div>
        <div class="feature-list">
          <div class="feature-item">
            <div class="feature-icon">
              <SafetyCertificateOutlined />
            </div>
            <div class="feature-text">
              <h4>数据安全</h4>
              <p>多重加密保护，确保信息安全</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <ThunderboltOutlined />
            </div>
            <div class="feature-text">
              <h4>智能分析</h4>
              <p>AI驱动的数据洞察与预警</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <TeamOutlined />
            </div>
            <div class="feature-text">
              <h4>协同高效</h4>
              <p>多部门联动，精准服务每一位残疾人</p>
            </div>
          </div>
        </div>
      </div>
      <div class="left-footer">
        <span>© 2026 智联助残数据平台</span>
      </div>
    </div>

    <!-- 右侧登录区 -->
    <div class="login-right">
      <div class="login-card">
        <div class="login-header">
          <h2>欢迎登录</h2>
          <p>请输入您的账号信息</p>
        </div>
        
        <form @submit.prevent="onSubmit" class="login-form">
          <div class="form-item">
            <label class="form-label">用户名</label>
            <div class="input-wrapper">
              <UserOutlined class="input-icon" />
              <input 
                v-model="username" 
                type="text" 
                class="form-input" 
                placeholder="请输入用户名"
                autocomplete="username"
              />
            </div>
          </div>
          
          <div class="form-item">
            <label class="form-label">密码</label>
            <div class="input-wrapper">
              <LockOutlined class="input-icon" />
              <input 
                v-model="password" 
                :type="showPassword ? 'text' : 'password'" 
                class="form-input" 
                placeholder="请输入密码"
                autocomplete="current-password"
              />
              <button type="button" class="password-toggle" @click="showPassword = !showPassword">
                <EyeOutlined v-if="!showPassword" />
                <EyeInvisibleOutlined v-else />
              </button>
            </div>
          </div>
          
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </form>

        <div class="dev-tip">
          <InfoCircleOutlined />
          <span>开发模式: admin / Admin@12345</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { useAuthStore } from '../stores/auth'
import { router } from '../router'
import { 
  UserOutlined, 
  LockOutlined,
  SafetyCertificateOutlined,
  ThunderboltOutlined,
  TeamOutlined,
  InfoCircleOutlined,
  EyeOutlined,
  EyeInvisibleOutlined
} from '@ant-design/icons-vue'

const auth = useAuthStore()
const username = ref('admin')
const password = ref('Admin@12345')
const loading = ref(false)
const showPassword = ref(false)

async function onSubmit() {
  if (!username.value) {
    message.warning('请输入用户名')
    return
  }
  if (!password.value) {
    message.warning('请输入密码')
    return
  }
  
  loading.value = true
  try {
    await auth.login(username.value, password.value)
    await router.push('/home')
  } catch (e: any) {
    message.error(e?.response?.data?.message ?? '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: #f5f7fa;
}

/* 左侧装饰区 */
.login-left {
  width: 480px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;
}

.login-left::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%);
  pointer-events: none;
}

.login-left::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -30%;
  width: 80%;
  height: 80%;
  background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 60%);
  pointer-events: none;
}

.left-content {
  position: relative;
  z-index: 1;
}

.brand-section {
  margin-bottom: 60px;
}

.brand-icon {
  width: 72px;
  height: 72px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-bottom: 24px;
}

.brand-icon svg {
  width: 36px;
  height: 36px;
}

.brand-title {
  color: white;
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px;
  letter-spacing: 1px;
}

.brand-desc {
  color: rgba(255, 255, 255, 0.85);
  font-size: 16px;
  margin: 0;
  line-height: 1.6;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.feature-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  flex-shrink: 0;
}

.feature-text h4 {
  color: white;
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px;
}

.feature-text p {
  color: rgba(255, 255, 255, 0.75);
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
}

.left-footer {
  position: absolute;
  bottom: 24px;
  left: 60px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
}

/* 右侧登录区 */
.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.login-header p {
  font-size: 15px;
  color: #6b7280;
  margin: 0;
}

/* 表单样式 */
.login-form {
  margin-bottom: 24px;
}

.form-item {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  font-size: 16px;
  color: #9ca3af;
  z-index: 1;
}

.form-input {
  width: 100%;
  height: 48px;
  padding: 0 14px 0 42px;
  font-size: 15px;
  color: #1a1a2e;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  outline: none;
  transition: all 0.2s;
  box-sizing: border-box;
}

.form-input::placeholder {
  color: #9ca3af;
}

.form-input:hover {
  border-color: #d1d5db;
}

.form-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.password-toggle {
  position: absolute;
  right: 14px;
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  color: #9ca3af;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.password-toggle:hover {
  color: #667eea;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.4);
}

.submit-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #5a6fd6 0%, #6a4196 100%);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.dev-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fef3c7;
  border: 1px solid #fcd34d;
  border-radius: 8px;
  color: #92400e;
  font-size: 13px;
}

/* 响应式 */
@media (max-width: 900px) {
  .login-left {
    display: none;
  }
  
  .login-right {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
  
  .login-card {
    background: white;
    padding: 40px;
    border-radius: 16px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  }
}

@media (max-width: 480px) {
  .login-right {
    padding: 20px;
  }
  
  .login-card {
    padding: 30px 24px;
  }
  
  .login-header h2 {
    font-size: 24px;
  }
}
</style>
