<template>
  <a-layout class="app-layout">
    <!-- 侧边栏 -->
    <a-layout-sider 
      collapsible 
      v-model:collapsed="collapsed" 
      :trigger="null"
      class="app-sider"
      :width="240"
    >
      <!-- Logo -->
      <div class="sider-logo">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <transition name="fade">
          <span v-if="!collapsed" class="logo-text">智联助残</span>
        </transition>
      </div>
      
      <!-- 菜单 -->
      <a-menu 
        mode="inline" 
        :selectedKeys="[selectedKey]" 
        @click="onMenu"
        class="app-menu"
      >
        <a-menu-item key="/home">
          <template #icon><HomeOutlined /></template>
          <span>首页</span>
        </a-menu-item>
        <a-menu-item key="/data-overview">
          <template #icon><DatabaseOutlined /></template>
          <span>数据总览</span>
        </a-menu-item>
        <a-menu-item key="/person-profile">
          <template #icon><UserOutlined /></template>
          <span>一人一档</span>
        </a-menu-item>
        <a-menu-item key="/person">
          <template #icon><IdcardOutlined /></template>
          <span>一人一档(旧)</span>
        </a-menu-item>
        <a-menu-item key="/anomaly" v-perm="'menu:anomaly:view'">
          <template #icon><WarningOutlined /></template>
          <span>异常管理</span>
        </a-menu-item>
        <a-menu-item key="/policy" v-perm="'menu:policy:view'">
          <template #icon><FileTextOutlined /></template>
          <span>政策找人</span>
        </a-menu-item>
        <a-menu-item key="/stats" v-perm="'menu:stats:view'">
          <template #icon><BarChartOutlined /></template>
          <span>统计看板</span>
        </a-menu-item>
        <a-menu-item key="/import" v-perm="'menu:import:view'">
          <template #icon><UploadOutlined /></template>
          <span>导入治理</span>
        </a-menu-item>
        <a-menu-item key="/sys" v-perm="'menu:sys:view'">
          <template #icon><SettingOutlined /></template>
          <span>系统管理</span>
        </a-menu-item>
      </a-menu>
      
      <!-- 折叠按钮 -->
      <div class="collapse-trigger" @click="collapsed = !collapsed">
        <MenuFoldOutlined v-if="!collapsed" />
        <MenuUnfoldOutlined v-else />
      </div>
    </a-layout-sider>

    <!-- 主内容区 -->
    <a-layout class="app-main">
      <!-- 顶部导航 -->
      <a-layout-header class="app-header">
        <div class="header-left">
          <h1 class="header-title">智联助残数据平台</h1>
        </div>
        <div class="header-right">
          <div class="header-actions">
            <a-tooltip title="消息通知">
              <a-badge :count="3" :offset="[-2, 2]">
                <div class="action-btn">
                  <BellOutlined />
                </div>
              </a-badge>
            </a-tooltip>
          </div>
          
          <a-divider type="vertical" class="header-divider" />
          
          <!-- 用户信息 -->
          <a-dropdown :trigger="['click']">
            <div class="user-info">
              <a-avatar :size="36" class="user-avatar">
                <template #icon><UserOutlined /></template>
              </a-avatar>
              <div class="user-detail">
                <span class="user-name">{{ userName }}</span>
                <span class="user-role">管理员</span>
              </div>
              <DownOutlined class="dropdown-arrow" />
            </div>
            <template #overlay>
              <a-menu class="user-dropdown">
                <a-menu-item key="profile">
                  <UserOutlined />
                  <span>个人中心</span>
                </a-menu-item>
                <a-menu-item key="settings">
                  <SettingOutlined />
                  <span>账户设置</span>
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" @click="onLogout">
                  <LogoutOutlined />
                  <span>退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      
      <!-- 内容区 -->
      <a-layout-content class="app-content">
        <router-view />
        <Assistant />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { router } from '../router'
import Assistant from './components/Assistant.vue'
import {
  HomeOutlined,
  UserOutlined,
  WarningOutlined,
  FileTextOutlined,
  BarChartOutlined,
  UploadOutlined,
  SettingOutlined,
  BellOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DownOutlined,
  DatabaseOutlined,
  IdcardOutlined
} from '@ant-design/icons-vue'

const auth = useAuthStore()
const route = useRoute()
const collapsed = ref(false)

const selectedKey = computed(() => {
  const p = route.path
  if (p.startsWith('/person')) return '/person'
  if (p.startsWith('/data-overview')) return '/data-overview'
  return p
})

const userName = computed(() => {
  return auth.me?.displayName || auth.me?.username || '用户'
})

function onMenu(info: any) {
  router.push(info.key)
}

async function onLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<style scoped>
/* ========== 布局 ========== */
.app-layout {
  min-height: 100vh;
  background: #f0f2f5;
}

/* ========== 侧边栏 ========== */
.app-sider {
  background: #001529 !important;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
  position: relative;
}

.app-sider :deep(.ant-layout-sider-children) {
  display: flex;
  flex-direction: column;
}

/* Logo */
.sider-logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.logo-icon svg {
  width: 20px;
  height: 20px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 菜单 */
.app-menu {
  flex: 1;
  border-right: none !important;
  background: transparent !important;
  padding: 8px 0;
}

.app-menu :deep(.ant-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 4px 8px;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.65);
  transition: all 0.3s;
}

.app-menu :deep(.ant-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.app-menu :deep(.ant-menu-item-selected) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.app-menu :deep(.ant-menu-item-selected)::after {
  display: none;
}

.app-menu :deep(.ant-menu-item .anticon) {
  font-size: 16px;
}

/* 折叠按钮 */
.collapse-trigger {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.45);
  cursor: pointer;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s;
}

.collapse-trigger:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

/* ========== 主内容区 ========== */
.app-main {
  background: transparent;
}

/* ========== 顶部导航 ========== */
.app-header {
  height: 64px;
  background: #fff !important;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.action-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: #5f6368;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #f5f5f5;
  color: #667eea;
}

.header-divider {
  height: 24px;
  margin: 0 8px;
  border-color: #e8e8e8;
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 8px;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.2s;
}

.user-info:hover {
  background: #f5f5f5;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.user-detail {
  display: flex;
  flex-direction: column;
  min-width: 60px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
  line-height: 1.3;
}

.user-role {
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1.3;
}

.dropdown-arrow {
  font-size: 10px;
  color: #8c8c8c;
  margin-left: 4px;
}

/* 下拉菜单 */
.user-dropdown {
  padding: 4px;
  border-radius: 8px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.user-dropdown :deep(.ant-dropdown-menu-item) {
  border-radius: 6px;
  padding: 8px 16px;
  color: #5f6368;
}

.user-dropdown :deep(.ant-dropdown-menu-item:hover) {
  background: #f5f5f5;
  color: #667eea;
}

.user-dropdown :deep(.ant-dropdown-menu-item .anticon) {
  margin-right: 8px;
}

.user-dropdown :deep(.ant-dropdown-menu-item-divider) {
  margin: 4px 0;
}

/* ========== 内容区 ========== */
.app-content {
  padding: 0;
  overflow: auto;
  min-height: calc(100vh - 64px);
}

/* ========== 滚动条 ========== */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
