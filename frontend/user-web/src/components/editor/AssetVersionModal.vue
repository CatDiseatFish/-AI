<script setup lang="ts">
// {{CODE-Cycle-Integration:
//   Task_ID: [#Phase6-Step6-AssetVersion]
//   Timestamp: 2026-01-03T13:48:18+08:00
//   Phase: [D-Develop]
//   Context-Analysis: "Creating AssetVersionModal for viewing and switching asset versions. Shows thumbnail grid with current version highlighted. Uses Mochiani dark design with Aether rounded corners."
//   Principle_Applied: "Pixel-Perfect-Mandate, Aether-Aesthetics, KISS"
// }}
// {{START_MODIFICATIONS}}

import { ref, computed, onMounted } from 'vue'
import type { AssetVersionVO } from '@/types/api'

interface Props {
  show: boolean
  assetId: number | null
  versions: AssetVersionVO[]
  onClose: () => void
  onSwitchVersion: (versionId: number) => void
  onDownload: (url: string) => void
}

const props = defineProps<Props>()

// Loading state
const loading = ref(false)

// Current version
const currentVersion = computed(() => {
  return props.versions.find((v) => v.isCurrent)
})

// Sort versions by versionNo descending (latest first)
const sortedVersions = computed(() => {
  return [...props.versions].sort((a, b) => b.versionNo - a.versionNo)
})

// Format date
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// Handle version switch
const handleSwitchVersion = async (versionId: number) => {
  if (currentVersion.value?.id === versionId) return
  loading.value = true
  try {
    await props.onSwitchVersion(versionId)
  } finally {
    loading.value = false
  }
}

// Handle download
const handleDownload = (url: string) => {
  props.onDownload(url)
}

// Handle close
const handleClose = () => {
  props.onClose()
}
</script>

<template>
  <!-- Modal Backdrop -->
  <Transition name="fade">
    <div
      v-if="show"
      class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4"
      @click="handleClose"
    >
      <!-- Modal Content -->
      <div
        class="bg-[#1E2025] border border-white/10 rounded-2xl w-full max-w-4xl max-h-[80vh] shadow-2xl flex flex-col"
        @click.stop
      >
        <!-- Header -->
        <div class="flex items-center justify-between px-6 py-4 border-b border-white/10 flex-shrink-0">
          <div>
            <h2 class="text-lg font-bold text-white">
              版本历史
            </h2>
            <p class="text-xs text-white/40 mt-0.5">
              共 {{ versions.length }} 个版本
            </p>
          </div>
          <button
            class="p-1.5 rounded-lg hover:bg-white/10 transition-colors"
            @click="handleClose"
          >
            <svg class="w-5 h-5 text-white/60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        <!-- Body -->
        <div class="flex-1 overflow-y-auto p-6">
          <!-- Empty State -->
          <div v-if="versions.length === 0" class="flex flex-col items-center justify-center h-full py-16">
            <svg class="w-16 h-16 text-white/20 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 13h6m-3-3v6m5 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
            </svg>
            <p class="text-white/40 text-sm">暂无版本记录</p>
          </div>

          <!-- Version Grid -->
          <div v-else class="grid grid-cols-2 md:grid-cols-3 gap-4">
            <div
              v-for="version in sortedVersions"
              :key="version.id"
              :class="[
                'relative bg-[#191A1E] border-2 rounded-2xl overflow-hidden cursor-pointer transition-all group',
                version.isCurrent
                  ? 'border-[#00FFCC] shadow-lg shadow-[#00FFCC]/20'
                  : 'border-white/10 hover:border-white/20',
                loading ? 'pointer-events-none opacity-50' : '',
              ]"
              @click="handleSwitchVersion(version.id)"
            >
              <!-- Image -->
              <div class="aspect-video bg-black/40 flex items-center justify-center overflow-hidden">
                <img
                  :src="version.url"
                  :alt="`版本 ${version.versionNo}`"
                  class="w-full h-full object-cover"
                  loading="lazy"
                >
              </div>

              <!-- Info -->
              <div class="p-3">
                <div class="flex items-center justify-between mb-1.5">
                  <span class="text-sm font-bold text-white">
                    版本 {{ version.versionNo }}
                  </span>
                  <div
                    :class="[
                      'px-2 py-0.5 text-[10px] font-bold rounded-full',
                      version.source === 'AI' ? 'bg-purple-500/20 text-purple-300' : 'bg-blue-500/20 text-blue-300',
                    ]"
                  >
                    {{ version.source }}
                  </div>
                </div>
                <p class="text-xs text-white/40">
                  {{ formatDate(version.createdAt) }}
                </p>

                <!-- Current Badge -->
                <div
                  v-if="version.isCurrent"
                  class="absolute top-2 right-2 bg-[#00FFCC] text-black text-[10px] font-bold px-2 py-1 rounded-full"
                >
                  当前版本
                </div>

                <!-- Download Button -->
                <button
                  class="absolute bottom-2 right-2 p-1.5 bg-white/10 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity hover:bg-white/20"
                  @click.stop="handleDownload(version.url)"
                  title="下载"
                >
                  <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
                  </svg>
                </button>
              </div>

              <!-- Prompt Info removed - not showing system template -->
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="flex items-center justify-between px-6 py-4 border-t border-white/10 flex-shrink-0">
          <div class="text-xs text-white/40">
            点击版本卡片切换为当前版本
          </div>
          <button
            class="px-5 py-2 text-sm font-medium text-white/70 bg-white/5 rounded-full hover:bg-white/10 transition-colors"
            @click="handleClose"
          >
            关闭
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
