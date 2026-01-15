<script setup lang="ts">
import { useInviteStore } from '@/stores/invite'

const inviteStore = useInviteStore()

const handleCopyLink = async () => {
  try {
    await inviteStore.copyInviteLink()
    window.$message?.success('邀请链接已复制到剪贴板')
  } catch (error: any) {
    console.error('Copy invite link error:', error)
    window.$message?.error(error.message || '复制失败')
  }
}
</script>

<template>
  <div class="p-6 rounded-2xl bg-white/5 border border-white/10">
    <h3 class="text-white font-medium mb-4">我的邀请码</h3>

    <div class="space-y-4">
      <!-- Invite Code Display -->
      <div class="p-4 rounded-2xl bg-white/5 border border-white/10">
        <div class="text-center">
          <div class="text-white/40 text-sm mb-2">邀请码</div>
          <div class="text-mochi-cyan text-3xl font-bold tracking-wider">
            {{ inviteStore.inviteInfo?.code || '---' }}
          </div>
        </div>
      </div>

      <!-- Reward Info -->
      <div class="grid grid-cols-2 gap-3">
        <div class="p-3 rounded-2xl bg-white/5 border border-white/10">
          <div class="text-white/40 text-xs mb-1">被邀请人获得</div>
          <div class="text-white text-lg font-medium">
            {{ inviteStore.inviteInfo?.rewardPoints || 0 }} 积分
          </div>
        </div>
        <div class="p-3 rounded-2xl bg-white/5 border border-white/10">
          <div class="text-white/40 text-xs mb-1">邀请人获得</div>
          <div class="text-mochi-cyan text-lg font-medium">
            {{ inviteStore.inviteInfo?.inviterRewardPoints || 0 }} 积分
          </div>
        </div>
      </div>

      <!-- Usage Stats -->
      <div class="p-3 rounded-2xl bg-white/5 border border-white/10">
        <div class="text-white/40 text-xs mb-1">使用次数</div>
        <div class="text-white text-sm">
          {{ inviteStore.inviteInfo?.usedCount || 0 }}
          <span v-if="inviteStore.inviteInfo?.maxUses" class="text-white/40">
            / {{ inviteStore.inviteInfo.maxUses }}
          </span>
          <span v-else class="text-white/40">/ 无限制</span>
        </div>
      </div>

      <!-- Copy Link Button -->
      <button
        class="w-full px-6 py-3 rounded-2xl bg-mochi-cyan text-mochi-bg font-medium text-sm hover:bg-mochi-cyan/90 transition-all"
        @click="handleCopyLink"
      >
        复制邀请链接
      </button>
    </div>
  </div>
</template>
