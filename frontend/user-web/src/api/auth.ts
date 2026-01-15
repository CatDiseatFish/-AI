import api from './index'
import type { SendCodeRequest, LoginRequest, LoginResponse, UserVO, UpdateProfileRequest } from '@/types/api'

export const authApi = {
  /**
   * Send verification code
   */
  sendCode(data: SendCodeRequest): Promise<void> {
    return api.post('/auth/phone/send-code', data)
  },

  /**
   * Phone login
   */
  login(data: LoginRequest): Promise<LoginResponse> {
    return api.post('/auth/phone/login', data)
  },

  /**
   * Get user profile
   */
  getProfile(): Promise<UserVO> {
    return api.get('/user/profile')
  },

  /**
   * Update user profile
   */
  updateProfile(data: UpdateProfileRequest): Promise<UserVO | null> {
    return api.put('/user/profile', data)
  },

  /**
   * Upload avatar
   */
  uploadAvatar(file: File): Promise<string> {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
