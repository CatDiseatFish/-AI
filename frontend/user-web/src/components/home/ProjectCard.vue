<script setup lang="ts">
import { computed } from 'vue'
import type { ProjectVO } from '@/types/api'
import NeonTag from '@/components/base/NeonTag.vue'

interface Props {
  project: ProjectVO
}

const props = defineProps<Props>()

const emit = defineEmits<{
  click: []
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

const coverImage = computed(() => {
  return props.project.coverUrl || 'https://via.placeholder.com/400x225/1E2025/00FFCC?text=No+Cover'
})

const projectTitle = computed(() => {
  return props.project.title || props.project.name
})
</script>

<template>
  <div
    class="group relative bg-white/5 border border-white/10 rounded-2xl overflow-hidden hover:bg-white/10 hover:border-mochi-cyan/50 transition-all cursor-pointer"
    @click="$emit('click')"
  >
    <!-- Cover image -->
    <div class="relative aspect-video bg-mochi-surface-l1 overflow-hidden">
      <img
        :src="coverImage"
        :alt="projectTitle"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
      >

      <!-- Overlay on hover -->
      <div class="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity">
        <div class="absolute bottom-3 left-3 right-3 flex gap-2">
          <button
            class="flex-1 px-3 py-2 rounded-xl bg-mochi-cyan text-mochi-bg text-xs font-medium hover:brightness-110 transition-all"
            @click.stop="$emit('click')"
          >
            编辑项目
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

    <!-- Project info -->
    <div class="p-4">
      <h3 class="text-white font-medium text-sm mb-2 truncate">{{ projectTitle }}</h3>

      <div class="flex items-center justify-between text-xs">
        <span class="text-white/40">{{ formatDate(project.updatedAt) }}</span>
        <NeonTag
          v-if="project.shotCount"
          :label="`${project.shotCount} 分镜`"
          variant="cyan"
        />
      </div>
    </div>
  </div>
</template>
