import axios from 'axios'
import { useAuthStore } from '../stores/auth'

export const http = axios.create({
  baseURL: '/',
  withCredentials: true
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
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
