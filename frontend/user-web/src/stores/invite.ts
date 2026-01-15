import { defineStore } from 'pinia'
import { inviteApi } from '@/api/apis'
import type { InviteInfoVO, InviteRecordVO, InviteStatsVO, PageResult } from '@/types/api'

interface InviteState {
  inviteInfo: InviteInfoVO | null
  records: InviteRecordVO[]
  stats: InviteStatsVO | null
  loading: boolean
  recordsPage: number
  recordsTotal: number
}

export const useInviteStore = defineStore('invite', {
  state: (): InviteState => ({
    inviteInfo: null,
    records: [],
    stats: null,
    loading: false,
    recordsPage: 1,
    recordsTotal: 0,
  }),

  getters: {
    inviteCode: (state) => state.inviteInfo?.code || '',
    inviteLink: (state) => state.inviteInfo?.inviteLink || '',
    hasRecords: (state) => state.records.length > 0,
  },

  actions: {
    /**
     * Fetch user's invite information
     */
    async fetchMyInviteInfo() {
      this.loading = true
      try {
        console.log('[InviteStore] Fetching my invite info')
        this.inviteInfo = await inviteApi.getMyInviteInfo()

        // Also fetch stats to get totalInvited and totalRewardsEarned
        const stats = await inviteApi.getInviteStats()
        if (this.inviteInfo && stats) {
          this.inviteInfo.totalInvited = stats.totalInvited
          this.inviteInfo.totalRewardsEarned = stats.totalRewardsEarned
        }

        console.log('[InviteStore] Invite info loaded:', this.inviteInfo)
      } catch (error) {
        console.error('[InviteStore] Failed to fetch invite info:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * Fetch invite records with pagination
     */
    async fetchInviteRecords(page = 1, size = 10) {
      this.loading = true
      try {
        console.log('[InviteStore] Fetching invite records, page:', page)
        const result: PageResult<InviteRecordVO> = await inviteApi.getInviteRecords({
          page,
          size,
        })
        this.records = result.records || []
        this.recordsPage = result.current
        this.recordsTotal = result.total
        console.log('[InviteStore] Records loaded:', this.records.length, 'items')
      } catch (error) {
        console.error('[InviteStore] Failed to fetch records:', error)
        this.records = []
      } finally {
        this.loading = false
      }
    },

    /**
     * Fetch invite statistics
     */
    async fetchInviteStats() {
      this.loading = true
      try {
        console.log('[InviteStore] Fetching invite stats')
        this.stats = await inviteApi.getInviteStats()
        console.log('[InviteStore] Stats loaded:', this.stats)
      } catch (error) {
        console.error('[InviteStore] Failed to fetch stats:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * Copy invite link to clipboard
     */
    async copyInviteLink() {
      try {
        if (!this.inviteInfo) {
          await this.fetchMyInviteInfo()
        }

        if (this.inviteInfo) {
          await inviteApi.copyInviteLink(this.inviteInfo.code)
          console.log('[InviteStore] Invite link copied to clipboard')
        }
      } catch (error) {
        console.error('[InviteStore] Failed to copy invite link:', error)
        throw error
      }
    },

    /**
     * Reset store state
     */
    reset() {
      this.inviteInfo = null
      this.records = []
      this.stats = null
      this.loading = false
      this.recordsPage = 1
      this.recordsTotal = 0
    },
  },
})
