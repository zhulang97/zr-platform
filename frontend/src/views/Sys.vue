<template>
  <a-card>
    <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
      <a-tab-pane key="users" tab="用户管理">
        <UserManage />
      </a-tab-pane>
      <a-tab-pane key="roles" tab="角色管理">
        <RoleManage />
      </a-tab-pane>
      <a-tab-pane key="permissions" tab="权限管理">
        <PermissionManage />
      </a-tab-pane>
      <a-tab-pane key="rules" tab="规则管理">
        <RuleManage />
      </a-tab-pane>
      <a-tab-pane key="audit" tab="审计日志">
        <AuditQuery />
      </a-tab-pane>
      <a-tab-pane key="dict" tab="字典管理">
        <DictManage />
      </a-tab-pane>
    </a-tabs>
  </a-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import UserManage from './UserManage.vue'
import RoleManage from './RoleManage.vue'
import PermissionManage from './PermissionManage.vue'
import RuleManage from './RuleManage.vue'
import AuditQuery from './AuditQuery.vue'
import DictManage from './DictManage.vue'

const route = useRoute()
const router = useRouter()
const activeTab = ref(route.params.tab as string || 'users')

const handleTabChange = (key: string) => {
  router.push({ path: '/sys', params: { tab: key } })
}

onMounted(() => {
  activeTab.value = (route.params.tab as string) || 'users'
})
</script>
