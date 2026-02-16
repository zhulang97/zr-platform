import { defineStore } from 'pinia'
import { http, getStoredToken, setStoredToken } from '../api/http'

type MenuNode = {
  permCode: string
  name: string
  path: string | null
  sort: number
  children: MenuNode[]
}

type MeResponse = {
  userId: number
  username: string
  displayName: string
  permissions: string[]
  menus: MenuNode[]
  districtScope: number[] | null
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: getStoredToken() as string,
    loaded: false,
    me: null as MeResponse | null
  }),
  actions: {
    async login(username: string, password: string) {
      const resp = await http.post('/api/auth/login', { username, password })
      this.accessToken = resp.data.data.accessToken
      setStoredToken(this.accessToken)
      this.loaded = false
      await this.loadMe()
    },
    async refresh() {
      const resp = await http.post('/api/auth/refresh')
      this.accessToken = resp.data.data.accessToken
      setStoredToken(this.accessToken)
    },
    async loadMe() {
      try {
        const resp = await http.get('/api/auth/me')
        if (resp.data?.success && resp.data?.data) {
          this.me = resp.data.data
          this.loaded = true
        } else {
          this.clear()
        }
      } catch {
        this.clear()
      }
    },
    async logout() {
      try {
        await http.post('/api/auth/logout')
      } catch {
      }
      this.clear()
    },
    clear() {
      this.accessToken = ''
      this.loaded = false
      this.me = null
      setStoredToken('')
    },
    hasPerm(code: string) {
      return !!this.me?.permissions?.includes(code)
    }
  }
})