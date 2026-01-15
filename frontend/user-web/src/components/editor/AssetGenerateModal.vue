<script setup lang="ts">
// {{CODE-Cycle-Integration:
//   Task_ID: [#Phase6-Step6-AssetGenerate]
//   Timestamp: 2026-01-03T13:48:18+08:00
//   Phase: [D-Develop]
//   Context-Analysis: "Creating AssetGenerateModal for triggering single asset generation. Shows form with prompt, aspectRatio, model, and estimated cost. Uses Mochiani dark design with Aether rounded corners."
//   Principle_Applied: "Pixel-Perfect-Mandate, Aether-Aesthetics, KISS"
// }}
// {{START_MODIFICATIONS}}

import { ref, computed } from 'vue'

interface Props {
  show: boolean
  assetType: 'shot_image' | 'video' | 'character' | 'scene'
  assetId: number
  onClose: () => void
  onConfirm: (params: GenerateParams) => void
}

interface GenerateParams {
  aspectRatio: '1:1' | '16:9' | '9:16' | '21:9'
  model?: string
  prompt?: string
}

const props = defineProps<Props>()

// Form state
const form = ref<GenerateParams>({
  aspectRatio: '21:9', // Default aspect ratio
  model: undefined,
  prompt: undefined,
})

// Asset type display name
const assetTypeLabel = computed(() => {
  switch (props.assetType) {
    case 'shot_image':
      return '分镜图'
    case 'video':
      return '视频'
    case 'character':
      return '角色画像'
    case 'scene':
      return '场景画像'
    default:
      return '资产'
  }
})

// Estimated cost (placeholder - should come from backend pricing rules)
const estimatedCost = computed(() => {
  const baseCost = props.assetType === 'video' ? 100 : 50
  return baseCost
})

const handleConfirm = () => {
  props.onConfirm({
    aspectRatio: form.value.aspectRatio,
    model: form.value.model || undefined,
    prompt: form.value.prompt || undefined,
  })
}

const handleClose = () => {
  // Reset form
  form.value = {
    aspectRatio: '21:9',
    model: undefined,
    prompt: undefined,
  }
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
        class="bg-[#1E2025] border border-white/10 rounded-2xl w-full max-w-md shadow-2xl"
        @click.stop
      >
        <!-- Header -->
        <div class="flex items-center justify-between px-6 py-4 border-b border-white/10">
          <h2 class="text-lg font-bold text-white">
            生成{{ assetTypeLabel }}
          </h2>
          <button
            class="p-1.5 rounded-lg hover:bg-white/10 transition-colors"
            @click="handleClose"
          >
            <svg class="w-5 h-5 text-white/60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        <!-- Form Body -->
        <div class="px-6 py-5 space-y-4">
          <!-- Aspect Ratio -->
          <div>
            <label class="block text-sm font-medium text-white/80 mb-2">
              画幅比例 <span class="text-red-400">*</span>
            </label>
            <div class="grid grid-cols-4 gap-2">
              <button
                v-for="ratio in ['1:1', '16:9', '9:16', '21:9']"
                :key="ratio"
                :class="[
                  'px-3 py-2 text-sm font-medium rounded-2xl border-2 transition-all',
                  form.aspectRatio === ratio
                    ? 'bg-[#00FFCC]/10 border-[#00FFCC] text-[#00FFCC]'
                    : 'bg-white/5 border-white/10 text-white/60 hover:border-white/20',
                ]"
                @click="form.aspectRatio = ratio as '1:1' | '16:9' | '9:16' | '21:9'"
              >
                {{ ratio }}
              </button>
            </div>
          </div>

          <!-- Model Selection -->
          <div>
            <label class="block text-sm font-medium text-white/80 mb-2">
              模型选择 <span class="text-white/40 text-xs">(可选)</span>
            </label>
            <select
              v-model="form.model"
              class="w-full px-4 py-2.5 bg-[#191A1E] border border-white/10 rounded-2xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-[#00FFCC]/50"
            >
              <option :value="undefined">默认模型</option>
              <option value="jimeng-4.5">即梦 4.5</option>
              <option value="gemini-3-pro-image-preview">Gemini 3 Pro Image</option>
            </select>
          </div>

          <!-- Custom Prompt -->
          <div>
            <label class="block text-sm font-medium text-white/80 mb-2">
              自定义提示词 <span class="text-white/40 text-xs">(可选)</span>
            </label>
            <textarea
              v-model="form.prompt"
              rows="3"
              class="w-full px-4 py-2.5 bg-[#191A1E] border border-white/10 rounded-2xl text-white text-sm resize-none focus:outline-none focus:ring-2 focus:ring-[#00FFCC]/50"
              placeholder="留空则使用系统默认提示词..."
            ></textarea>
            <p class="mt-1.5 text-xs text-white/40">
              提示：留空将根据剧本内容自动生成提示词
            </p>
          </div>

          <!-- Estimated Cost -->
          <div class="bg-white/5 border border-white/10 rounded-2xl px-4 py-3 flex items-center justify-between">
            <span class="text-sm text-white/60">预估消耗</span>
            <div class="flex items-center gap-1.5">
              <svg class="w-4 h-4 text-[#00FFCC]" viewBox="0 0 1024 1024" fill="currentColor">
                <path d="M224 800c0 9.6 3.2 44.8 6.4 54.4 6.4 48-48 76.8-48 76.8s80 41.6 147.2 0S464 796.8 368 736c-22.4-12.8-41.6-19.2-57.6-19.2-51.2 0-83.2 44.8-86.4 83.2z m336-124.8l-32 51.2c-51.2 51.2-83.2 32-83.2 32 25.6 67.2 0 112-12.8 128 25.6 6.4 51.2 9.6 80 9.6 54.4 0 102.4-9.6 150.4-32 3.2 0 3.2-3.2 3.2-3.2 22.4-16 12.8-35.2 6.4-44.8-9.6-12.8-12.8-25.6-12.8-41.6 0-54.4 60.8-99.2 137.6-99.2h22.4c12.8 0 38.4 9.6 48-25.6 0-3.2 0-3.2 3.2-6.4 0-3.2 3.2-6.4 3.2-6.4 6.4-16 6.4-16 6.4-19.2 9.6-35.2 16-73.6 16-115.2 0-105.6-41.6-198.4-108.8-268.8C704 396.8 560 675.2 560 675.2z m-336-256c0-28.8 22.4-51.2 51.2-51.2 28.8 0 51.2 22.4 51.2 51.2 0 28.8-22.4 51.2-51.2 51.2-28.8 0-51.2-22.4-51.2-51.2z m96-134.4c0-22.4 19.2-41.6 41.6-41.6 22.4 0 41.6 19.2 41.6 41.6 0 22.4-19.2 41.6-41.6 41.6-22.4 0-41.6-19.2-41.6-41.6zM457.6 208c0-12.8 12.8-25.6 25.6-25.6s25.6 12.8 25.6 25.6-12.8 25.6-25.6 25.6-25.6-12.8-25.6-25.6zM128 505.6C128 592 153.6 672 201.6 736c28.8-60.8 112-60.8 124.8-60.8-16-51.2 16-99.2 16-99.2l316.8-422.4c-48-19.2-99.2-32-150.4-32-211.2-3.2-380.8 169.6-380.8 384z"></path>
              </svg>
              <span class="text-base font-bold text-[#00FFCC]">{{ estimatedCost }}</span>
              <span class="text-sm text-white/60">积分</span>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-white/10">
          <button
            class="px-5 py-2 text-sm font-medium text-white/70 bg-white/5 rounded-full hover:bg-white/10 transition-colors"
            @click="handleClose"
          >
            取消
          </button>
          <button
            class="px-5 py-2 text-sm font-bold text-black bg-[#00FFCC] rounded-full hover:bg-[#21FFF3] transition-colors flex items-center gap-2"
            @click="handleConfirm"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.5">
              <path d="m12 3-1.9 5.8-5.8 1.9 5.8 1.9L12 21l1.9-5.8 5.8-1.9-5.8-1.9L12 3z"></path>
              <path d="M5 3v4"></path>
              <path d="M19 17v4"></path>
              <path d="M3 5h4"></path>
              <path d="M17 19h4"></path>
            </svg>
            <span>开始生成</span>
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
</style>
