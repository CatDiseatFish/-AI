<script setup lang="ts">
// {{CODE-Cycle-Integration:
//   Task_ID: [#问题1]
//   Timestamp: 2026-01-03T08:00:00+08:00
//   Phase: [D-Develop]
//   Context-Analysis: "Creating FolderCard component for homepage folder display. Following ProjectCard pattern with folder-specific design."
//   Principle_Applied: "Pixel-Perfect-Mandate, Aether-Aesthetics, DRY"
// }}
// {{START_MODIFICATIONS}}

import { computed } from 'vue'
import type { FolderVO } from '@/types/api'
import NeonTag from '@/components/base/NeonTag.vue'

interface Props {
  folder: FolderVO
}

const props = defineProps<Props>()

const emit = defineEmits<{
  click: []
  edit: []
  delete: []
}>()

// Format date to relative time
const formatDate = (date: string) => {
  const now = new Date()
  const then = new Date(date)
  const diffMs = now.getTime() - then.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins}分钟前`
  if (diffHours < 24) return `${diffHours}小时前`
  if (diffDays < 7) return `${diffDays}天前`

  return then.toLocaleDateString('zh-CN')
}

const folderImage = computed(() => {
  return props.folder.coverUrl || null
})

const projectCount = computed(() => {
  return props.folder.projectCount || 0
})
</script>

<template>
  <div
    class="group relative bg-white/5 border border-white/10 rounded-2xl overflow-hidden hover:bg-white/10 hover:border-mochi-cyan/50 transition-all cursor-pointer"
    @click="$emit('click')"
  >
    <!-- Folder visual -->
    <div class="relative aspect-video bg-gradient-to-br from-[#26272c] to-[#1a1b1f] overflow-hidden flex items-center justify-center">
      <!-- If cover image exists -->
      <img
        v-if="folderImage"
        :src="folderImage"
        :alt="folder.name"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
      >

      <!-- Default folder icon -->
      <div v-else class="flex flex-col items-center gap-3">
        <svg class="w-16 h-16 text-mochi-cyan/40 group-hover:text-mochi-cyan/60 transition-colors" fill="currentColor" viewBox="0 0 24 24">
          <path d="M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z" />
        </svg>
        <span class="text-white/20 text-xs font-medium">{{ projectCount }} 个项目</span>
      </div>

      <!-- Overlay on hover -->
      <div class="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity">
        <div class="absolute bottom-3 left-3 right-3 flex gap-2">
          <button
            class="flex-1 px-3 py-2 rounded-xl bg-mochi-cyan text-mochi-bg text-xs font-medium hover:brightness-110 transition-all"
            @click.stop="$emit('click')"
          >
            打开文件夹
          </button>
          <button
            class="px-3 py-2 rounded-xl bg-white/10 text-white text-xs hover:bg-white/20 transition-all backdrop-blur-sm"
            @click.stop="$emit('edit')"
          >
            编辑
          </button>
          <button
            class="px-3 py-2 rounded-xl bg-white/10 text-white text-xs hover:bg-white/20 transition-all backdrop-blur-sm"
            @click.stop="$emit('delete')"
          >
            删除
          </button>
        </div>
      </div>
    </div>

    <!-- Folder info -->
    <div class="p-4">
      <h3 class="text-white font-medium text-sm mb-2 truncate">{{ folder.name }}</h3>

      <div class="flex items-center justify-between text-xs">
        <span class="text-white/40">{{ formatDate(folder.createdAt) }}</span>
        <NeonTag
          :label="`${projectCount} 项目`"
          variant="cyan"
        />
      </div>
    </div>
  </div>
</template>
