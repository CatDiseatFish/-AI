<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useProjectStore } from '@/stores/project'
import type { ProjectVO, FolderVO } from '@/types/api'
import NeonTag from '@/components/base/NeonTag.vue'
import PillButton from '@/components/base/PillButton.vue'

const router = useRouter()
const projectStore = useProjectStore()

const emit = defineEmits<{
  editFolder: [folder: FolderVO]
  deleteFolder: [folder: FolderVO]
  deleteProject: [project: ProjectVO]
  moveProject: [project: ProjectVO]
}>()

// Get all projects directly (no folding)
const allProjects = computed(() => {
  return projectStore.projects
})

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

const handleEditProject = (project: ProjectVO) => {
  router.push(`/editor/${project.id}`)
}

// Helper to get folder name
const getFolderName = (folderId: number | null) => {
  if (!folderId) return '未分类'
  const folder = projectStore.getFolderById(folderId)
  return folder?.name || '未知文件夹'
}
</script>

<template>
  <div class="bg-white/5 border border-white/10 rounded-2xl overflow-hidden">
    <!-- Table header -->
    <div class="grid grid-cols-12 gap-4 px-6 py-3 bg-white/5 border-b border-white/10 text-xs font-medium text-white/60">
      <div class="col-span-5">项目名称</div>
      <div class="col-span-2">所属文件夹</div>
      <div class="col-span-2">更新时间</div>
      <div class="col-span-3 text-right">操作</div>
    </div>

    <!-- Empty state -->
    <div v-if="allProjects.length === 0" class="py-16 text-center">
      <div class="mb-4">
        <svg class="w-16 h-16 mx-auto text-white/20" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 13h6m-3-3v6m5 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
      </div>
      <p class="text-white/40 text-sm">暂无项目</p>
    </div>

    <!-- Project list (flat, no folding) -->
    <div v-else class="divide-y divide-white/5">
      <div
        v-for="project in allProjects"
        :key="project.id"
        class="grid grid-cols-12 gap-4 px-6 py-4 hover:bg-white/5 transition-all"
      >
        <!-- Project name -->
        <div class="col-span-5 flex items-center gap-3">
          <div class="w-12 h-12 rounded-lg bg-mochi-surface-l1 overflow-hidden flex-shrink-0">
            <img
              v-if="project.coverUrl"
              :src="project.coverUrl"
              :alt="project.title || project.name"
              class="w-full h-full object-cover"
            >
            <div v-else class="w-full h-full flex items-center justify-center text-white/20">
              <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
              </svg>
            </div>
          </div>
          <div class="min-w-0 flex-1">
            <p class="text-white text-sm font-medium truncate">{{ project.title || project.name }}</p>
            <p v-if="project.description" class="text-white/40 text-xs truncate">
              {{ project.description }}
            </p>
          </div>
        </div>

        <!-- Folder name -->
        <div class="col-span-2 flex items-center">
          <NeonTag :label="getFolderName(project.folderId)" variant="cyan" />
        </div>

        <!-- Update time -->
        <div class="col-span-2 flex items-center">
          <span class="text-white/60 text-sm">{{ formatDate(project.updatedAt) }}</span>
        </div>

        <!-- Actions -->
        <div class="col-span-3 flex items-center justify-end gap-2">
          <PillButton
            label="编辑"
            variant="primary"
            @click="handleEditProject(project)"
          />
          <PillButton
            label="移动"
            variant="ghost"
            @click="$emit('moveProject', project)"
          />
          <button
            class="p-2 rounded-lg text-white/60 hover:bg-mochi-pink/20 hover:text-mochi-pink transition-all"
            @click="$emit('deleteProject', project)"
          >
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
