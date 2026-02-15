import axios from 'axios'
import { useAuthStore } from '../stores/auth'

// Token 持久化存储
const TOKEN_KEY = 'zr_token'

const getStoredToken = () => {
  try {
    return localStorage.getItem(TOKEN_KEY) || ''
  } catch {
    return ''
  }
}

const setStoredToken = (token: string) => {
  try {
    if (token) {
      localStorage.setItem(TOKEN_KEY, token)
    } else {
      localStorage.removeItem(TOKEN_KEY)
    }
  } catch {
  }
}

export const http = axios.create({
  baseURL: '/',
  withCredentials: true
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (!auth.accessToken) {
    auth.accessToken = getStoredToken()
  }
  if (auth.accessToken) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

http.interceptors.response.use(
  (resp) => resp,
  async (err) => {
    const auth = useAuthStore()
    if (err?.response?.status === 401 && auth.accessToken) {
      try {
        await auth.refresh()
        err.config.headers.Authorization = `Bearer ${auth.accessToken}`
        return http.request(err.config)
      } catch {
        auth.clear()
        window.location.href = '/login'
      }
    }
    throw err
  }
)

export { getStoredToken, setStoredToken }