<template>
  <a-layout style="min-height: 100vh;">
    <a-layout-sider collapsible v-model:collapsed="collapsed">
      <div style="height: 56px; display: flex; align-items: center; padding: 0 16px; color: #fff; font-weight: 600;">
        智联助残
      </div>
      <a-menu theme="dark" mode="inline" :selectedKeys="[selectedKey]" @click="onMenu">
        <a-menu-item key="/home">统一查询</a-menu-item>
        <a-menu-item key="/person">一人一档</a-menu-item>
        <a-menu-item key="/anomaly" v-perm="'menu:anomaly:view'">异常管理</a-menu-item>
        <a-menu-item key="/stats" v-perm="'menu:stats:view'">统计看板</a-menu-item>
        <a-menu-item key="/import" v-perm="'menu:import:view'">导入治理</a-menu-item>
        <a-menu-item key="/sys" v-perm="'menu:sys:view'">系统管理</a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <a-layout-header style="background: #fff; display: flex; justify-content: space-between; align-items: center;">
        <div>
          <span style="font-weight: 600;">智联助残数据平台</span>
        </div>
        <div>
          <a-space>
            <span style="color: #666;">{{ auth.me?.displayName }}</span>
            <a-button size="small" @click="onLogout">退出</a-button>
          </a-space>
        </div>
      </a-layout-header>
      <a-layout-content style="padding: 16px;">
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

const auth = useAuthStore()
const route = useRoute()
const collapsed = ref(false)

const selectedKey = computed(() => {
  const p = route.path
  if (p.startsWith('/person')) return '/person'
  return p
})

function onMenu(info: any) {
  router.push(info.key)
}

async function onLogout() {
  await auth.logout()
  router.push('/login')
}
</script>
