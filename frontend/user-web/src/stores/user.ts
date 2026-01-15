import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import type { UserVO, UpdateProfileRequest } from '@/types/api'

interface UserState {
  token: string
  user: UserVO | null
  points: number
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('token') || '',
    user: null,
    points: 0,
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    userName: (state) => state.user?.nickname || '用户',
    userAvatar: (state) => state.user?.avatarUrl || '',
  },

  actions: {
    async login(phone: string, code: string, inviteCode?: string) {
      const data = await authApi.login({ phone, code, inviteCode })
      this.token = data.token
      // 后端登录返回扁平结构，转换为 UserVO
      this.user = {
        userId: data.userId,
        nickname: data.nickname,
        avatarUrl: data.avatarUrl,
        status: 1,
        balance: data.balance,
        createdAt: '',
      }
      this.points = data.balance
      localStorage.setItem('token', data.token)
    },

    logout() {
      this.token = ''
      this.user = null
      this.points = 0
      localStorage.removeItem('token')
    },

    async fetchProfile() {
      const user = await authApi.getProfile()
      this.user = user
      this.points = user.balance
    },

    async updateProfile(data: UpdateProfileRequest) {
      const updatedUser = await authApi.updateProfile(data)
      if (updatedUser) {
        this.user = updatedUser
      } else {
        // 如果后端返回空，重新获取
        await this.fetchProfile()
      }
    },

    setPoints(points: number) {
      this.points = points
    },
  },
})
