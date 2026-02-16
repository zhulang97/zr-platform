import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from './stores/auth'

const Login = () => import('./views/Login.vue')
const Shell = () => import('./views/Shell.vue')

const Query = () => import('./views/Query.vue')
const Person = () => import('./views/Person.vue')
const Anomaly = () => import('./views/Anomaly.vue')
const Stats = () => import('./views/Stats.vue')
const Importing = () => import('./views/Importing.vue')
const ModuleConfig = () => import('./views/import/ModuleConfig.vue')
const Sys = () => import('./views/Sys.vue')
const AIChat = () => import('./views/AIChat.vue')
const PolicySearch = () => import('./views/PolicySearch.vue')
const Dashboard = () => import('./views/Dashboard.vue')

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    {
      path: '/',
      component: Shell,
      children: [
        { path: '', redirect: '/home' },
        { path: 'home', component: Dashboard, meta: { perm: 'menu:home:view' } },
        { path: 'person/:id?', component: Person, meta: { perm: 'menu:person:view' } },
        { path: 'anomaly', component: Anomaly, meta: { perm: 'menu:anomaly:view' } },
        { path: 'stats', component: Stats, meta: { perm: 'menu:stats:view' } },
        { path: 'import', component: Importing, meta: { perm: 'menu:import:view' } },
        { path: 'import/config', component: ModuleConfig, meta: { perm: 'menu:import:view' } },
        { path: 'ai', component: AIChat, meta: { perm: 'menu:ai:view' } },
        { path: 'policy', component: PolicySearch, meta: { perm: 'menu:policy:view' } },
        { path: 'sys/:tab?', component: Sys, meta: { perm: 'menu:sys:view' } }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.path === '/login') return true
  
  if (!auth.accessToken) {
    return { path: '/login' }
  }
  
  if (!auth.loaded) {
    try {
      await auth.loadMe()
    } catch {
      auth.clear()
      return { path: '/login' }
    }
  }
  
  if (!auth.me) {
    auth.clear()
    return { path: '/login' }
  }
  
  const perm = (to.meta?.perm as string | undefined)
  if (perm && !auth.hasPerm(perm)) {
    return { path: '/home' }
  }
  return true
})
