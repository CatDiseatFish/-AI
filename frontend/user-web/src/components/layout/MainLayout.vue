<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { walletApi } from '@/api/wallet'
import NavSidebar from './NavSidebar.vue'

const userStore = useUserStore()

// Fetch user profile and wallet balance on layout mount
onMounted(async () => {
  try {
    await userStore.fetchProfile()
    const walletData = await walletApi.getBalance()
    userStore.setPoints(walletData.balance)
  } catch (error) {
    console.error('[MainLayout] Failed to load user data:', error)
    userStore.setPoints(0)
  }
})
</script>

<template>
  <div class="flex h-screen bg-mochi-bg overflow-hidden">
    <!-- Sidebar -->
    <NavSidebar />

    <!-- Main content area -->
    <main class="flex-1 overflow-y-auto">
      <slot />
    </main>
  </div>
</template>
