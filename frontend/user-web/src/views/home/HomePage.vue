<script setup lang="ts">
// {{CODE-Cycle-Integration:
//   Task_ID: [#问题1]
//   Timestamp: 2026-01-03T08:05:00+08:00
//   Phase: [D-Develop]
//   Context-Analysis: "Refactoring HomePage to show folders as card grid. Adding folder selection logic and updating CreateProjectModal integration."
//   Principle_Applied: "Context-First-Mandate, Aether-Aesthetics, Verification-Mindset"
// }}
// {{START_MODIFICATIONS}}

import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useProjectStore } from '@/stores/project'
import { useUserStore } from '@/stores/user'
import { walletApi, projectApi } from '@/api/apis'
import type { ProjectVO, FolderVO } from '@/types/api'
import GlassCard from '@/components/base/GlassCard.vue'
import PillButton from '@/components/base/PillButton.vue'
import ProjectCard from '@/components/home/ProjectCard.vue'
import FolderCard from '@/components/home/FolderCard.vue'
import ProjectTable from '@/components/home/ProjectTable.vue'
import CreateProjectModal from '@/components/home/CreateProjectModal.vue'
import FolderModal from '@/components/home/FolderModal.vue'
import MoveProjectModal from '@/components/home/MoveProjectModal.vue'

const router = useRouter()
const projectStore = useProjectStore()
const userStore = useUserStore()

const searchKeyword = ref('')
const showCreateProjectModal = ref(false)
const showCreateFolderModal = ref(false)
const showEditFolderModal = ref(false)
const showMoveProjectModal = ref(false)
const editingFolder = ref<FolderVO | null>(null)  // For folder edit modal
const selectedProject = ref<ProjectVO | null>(null)
const loading = ref(false)

onMounted(async () => {
  // Fetch user profile and wallet balance
  try {
    await Promise.all([
      userStore.fetchProfile(),
      projectStore.fetchProjects(),
      projectStore.fetchFolders(),
    ])

    // Fetch wallet balance separately (optional, may not be implemented yet)
    fetchWalletBalance().catch(() => {
      // Silently fail if wallet endpoint doesn't exist
      console.warn('Wallet API not available')
    })
  } catch (error) {
    console.error('Failed to fetch initial data:', error)
  }
})

const fetchWalletBalance = async () => {
  try {
    const wallet = await walletApi.getBalance()
    userStore.setPoints(wallet.balance)
  } catch (error) {
    console.error('Failed to fetch wallet balance:', error)
  }
}

const handleSearch = async () => {
  await projectStore.fetchProjects(1, searchKeyword.value)
}

const handleCreateProject = async (data: {
  name: string
  folderId?: number
  rawText: string
}) => {
  loading.value = true
  try {
    console.log('[HomePage] Creating project with data:', data)

    // Create project with simplified fields (use default aspectRatio)
    const project = await projectStore.createProject({
      name: data.name,
      folderId: data.folderId,
      rawText: data.rawText,
      aspectRatio: '16:9',  // Default aspect ratio
      styleCode: '',  // Empty style code
    })

    console.log('[HomePage] Project created:', project)

    if (!project || !project.id) {
      throw new Error('项目创建失败：返回数据无效')
    }

    showCreateProjectModal.value = false

    // Refresh project list after creation
    await projectStore.fetchProjects()

    // Navigate to editor
    router.push(`/editor/${project.id}`)
  } catch (error: any) {
    console.error('[HomePage] Create project error:', error)
    alert(error.message || '创建项目失败')
  } finally {
    loading.value = false
  }
}

const handleCreateFolder = async (name: string) => {
  loading.value = true
  try {
    await projectStore.createFolder({ name })
    showCreateFolderModal.value = false

    // Refresh folder list after creation
    await projectStore.fetchFolders()
  } catch (error: any) {
    alert(error.message || '创建文件夹失败')
  } finally {
    loading.value = false
  }
}

const handleEditFolder = (folder: FolderVO) => {
  editingFolder.value = folder
  showEditFolderModal.value = true
}

const handleUpdateFolder = async (name: string) => {
  if (!editingFolder.value) return

  loading.value = true
  try {
    await projectStore.updateFolder(editingFolder.value.id, { name })
    showEditFolderModal.value = false
    editingFolder.value = null

    // Refresh folder list to ensure UI is updated
    await projectStore.fetchFolders()
  } catch (error: any) {
    alert(error.message || '更新文件夹失败')
  } finally {
    loading.value = false
  }
}

const handleDeleteFolder = async (folder: FolderVO) => {
  if (!confirm(`确定要删除文件夹"${folder.name}"吗？文件夹内的项目将移至未分类。`)) {
    return
  }

  try {
    await projectStore.deleteFolder(folder.id)
  } catch (error: any) {
    alert(error.message || '删除文件夹失败')
  }
}

const handleDeleteProject = async (project: ProjectVO) => {
  if (!confirm(`确定要删除项目"${project.title}"吗？此操作不可撤销。`)) {
    return
  }

  try {
    await projectStore.deleteProject(project.id)
  } catch (error: any) {
    alert(error.message || '删除项目失败')
  }
}

const handleMoveProject = (project: ProjectVO) => {
  selectedProject.value = project
  showMoveProjectModal.value = true
}

const handleConfirmMoveProject = async (folderId: number | null) => {
  if (!selectedProject.value) return

  loading.value = true
  try {
    await projectStore.moveProjectToFolder(selectedProject.value.id, folderId)
    showMoveProjectModal.value = false
    selectedProject.value = null

    // Refresh project list to reflect the folder change
    await projectStore.fetchProjects()
  } catch (error: any) {
    alert(error.message || '移动项目失败')
  } finally {
    loading.value = false
  }
}

const handleEditProject = (project: ProjectVO) => {
  router.push(`/editor/${project.id}`)
}

// Folder selection logic
const selectedFolder = computed(() => {
  if (!projectStore.selectedFolderId) return null
  return projectStore.getFolderById(projectStore.selectedFolderId)
})

const handleFolderClick = async (folder: FolderVO) => {
  projectStore.setSelectedFolder(folder.id)
  await projectStore.fetchProjects(1, searchKeyword.value)
}

const handleShowAllProjects = async () => {
  projectStore.setSelectedFolder(null)
  await projectStore.fetchProjects(1, searchKeyword.value)
}

// {{END_MODIFICATIONS}}

</script>

<template>
  <div class="p-8 space-y-8">
      <!-- Header with actions -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-white mb-2">我的项目</h1>
          <p class="text-white/60 text-sm">
            共 {{ projectStore.total }} 个项目 · {{ projectStore.folders.length }} 个文件夹
          </p>
        </div>

        <div class="flex gap-3">
          <PillButton
            label="新建文件夹"
            variant="secondary"
            icon='<svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" /></svg>'
            @click="showCreateFolderModal = true"
          />
          <button
            class="flex items-center gap-2 px-6 py-2 rounded-full bg-gradient-to-r from-mochi-cyan to-mochi-blue text-mochi-bg font-medium text-sm hover:shadow-neon-cyan transition-all"
            @click="showCreateProjectModal = true"
          >
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            新建项目
          </button>
        </div>
      </div>

      <!-- Search bar -->
      <GlassCard padding="p-4">
        <div class="flex gap-3">
          <div class="flex-1 flex items-center gap-3 px-4 py-2 rounded-xl bg-white/5 border border-white/10 focus-within:border-mochi-cyan transition-all">
            <svg class="w-5 h-5 text-white/40" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索项目..."
              class="flex-1 bg-transparent text-white placeholder-white/30 focus:outline-none text-sm"
              @keydown.enter="handleSearch"
            >
          </div>
          <PillButton
            label="搜索"
            variant="primary"
            @click="handleSearch"
          />
        </div>
      </GlassCard>

      <!-- Folder grid -->
      <div v-if="projectStore.folders.length > 0 && !selectedFolder">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-xl font-semibold text-white">我的文件夹</h2>
          <span class="text-white/40 text-sm">{{ projectStore.folders.length }} 个文件夹</span>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <FolderCard
            v-for="folder in projectStore.folders"
            :key="folder.id"
            :folder="folder"
            @click="handleFolderClick(folder)"
            @edit="handleEditFolder(folder)"
            @delete="handleDeleteFolder(folder)"
          />
        </div>
      </div>

      <!-- All projects table -->
      <div>
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-3">
            <button
              v-if="selectedFolder"
              class="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-white/5 border border-white/10 text-white/60 hover:bg-white/10 hover:text-white transition-all text-sm"
              @click="handleShowAllProjects"
            >
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
              </svg>
              返回所有项目
            </button>
            <h2 class="text-xl font-semibold text-white">
              {{ selectedFolder ? `${selectedFolder.name} 中的项目` : '所有项目' }}
            </h2>
          </div>
        </div>
        <ProjectTable
          @edit-folder="handleEditFolder"
          @delete-folder="handleDeleteFolder"
          @delete-project="handleDeleteProject"
          @move-project="handleMoveProject"
        />
      </div>

    <!-- Modals -->
    <CreateProjectModal
      :show="showCreateProjectModal"
      :loading="loading"
      :folder-id="projectStore.selectedFolderId"
      @close="showCreateProjectModal = false"
      @confirm="handleCreateProject"
    />

    <FolderModal
      :show="showCreateFolderModal"
      :loading="loading"
      title="新建文件夹"
      @close="showCreateFolderModal = false"
      @confirm="handleCreateFolder"
    />

    <FolderModal
      :show="showEditFolderModal"
      :loading="loading"
      title="编辑文件夹"
      :folder-name="editingFolder?.name || ''"
      @close="showEditFolderModal = false"
      @confirm="handleUpdateFolder"
    />

    <MoveProjectModal
      :show="showMoveProjectModal"
      :loading="loading"
      :project="selectedProject"
      @close="showMoveProjectModal = false"
      @confirm="handleConfirmMoveProject"
    />
  </div>
</template>
