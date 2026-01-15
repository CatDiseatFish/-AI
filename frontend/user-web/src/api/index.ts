// import axios, type { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios'
import axios from 'axios'
import type { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { API_CONFIG } from '@/constants/config'
import router from '@/router'
import type { Result } from '@/types/api'

// Create axios instance
const api: AxiosInstance = axios.create({
  baseURL: API_CONFIG.baseURL,
  timeout: API_CONFIG.timeout,
})

// Request interceptor - Add JWT token
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// Response interceptor - Handle Result wrapper & errors
api.interceptors.response.use(
  (response) => {
    const { code, data, message } = response.data as Result<unknown>

    if (code === 200) {
      return data as any // Extract data from Result wrapper
    }

    // Business error
    console.error('[API Error]', message)
    // @ts-expect-error window.$message is provided by Naive UI
    window.$message?.error(message || '操作失败')
    return Promise.reject(new Error(message))
  },
  (error: AxiosError) => {
    // HTTP error
    if (error.response?.status === 401) {
      // Token expired or invalid
      console.error('[Auth Error] Token invalid, redirecting to login')
      localStorage.removeItem('token')
      router.push('/login')
    } else {
      const message = error.message || '网络错误'
      console.error('[Network Error]', message)
      // @ts-expect-error window.$message is provided by Naive UI
      window.$message?.error(message)
    }
    return Promise.reject(error)
  }
)

export default api
