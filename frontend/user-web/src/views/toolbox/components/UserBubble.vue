<script setup lang="ts">
import type { ConversationItem } from '@/stores/toolbox'

interface Props {
  conversation: ConversationItem
}

const props = defineProps<Props>()

// 获取类型图标SVG
const getTypeIcon = (type: string) => {
  switch (type) {
    case 'TEXT':
      return `<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
      </svg>`
    case 'IMAGE':
      return `<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>`
    case 'VIDEO':
      return `<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
      </svg>`
    default:
      return `<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>`
  }
}

// 获取类型名称
const getTypeName = (type: string) => {
  switch (type) {
    case 'TEXT':
      return '文字'
    case 'IMAGE':
      return '图片'
    case 'VIDEO':
      return '视频'
    default:
      return '未知'
  }
}
</script>

<template>
  <div class="flex justify-end mb-2">
    <div class="max-w-[70%] bg-mochi-cyan/10 border border-mochi-cyan/30 rounded-2xl p-3">
      <!-- Header: Type | Model | Ratio -->
      <div class="flex items-center gap-1.5 mb-1.5 text-xs">
        <span class="flex items-center gap-1 text-mochi-cyan">
          <span v-html="getTypeIcon(conversation.userInput?.type || '')"></span>
          <span class="font-medium">{{ getTypeName(conversation.userInput?.type || '') }}</span>
        </span>
        <span class="text-white/40">|</span>
        <span class="text-white/60">{{ conversation.userInput?.model }}</span>
        <span v-if="conversation.userInput?.aspectRatio" class="text-white/40">|</span>
        <span v-if="conversation.userInput?.aspectRatio" class="text-white/60">
          {{ conversation.userInput?.aspectRatio }}
        </span>
        <span v-if="conversation.userInput?.duration" class="text-white/40">|</span>
        <span v-if="conversation.userInput?.duration" class="text-white/60">
          {{ conversation.userInput?.duration }}秒
        </span>
      </div>

      <!-- Prompt -->
      <div class="text-white/90 text-sm leading-relaxed whitespace-pre-wrap break-words mb-1.5">
        {{ conversation.userInput?.prompt || '(无提示词)' }}
      </div>

      <!-- Estimated Cost -->
      <div class="flex items-center justify-end gap-1 text-xs text-mochi-cyan/80">
        <span>预计:</span>
        <span class="font-medium">{{ conversation.userInput?.estimatedCost }}</span>
        <span>积分</span>
      </div>
    </div>
  </div>
</template>
