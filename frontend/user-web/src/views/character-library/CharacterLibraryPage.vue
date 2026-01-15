<script setup lang="ts">
// {{CODE-Cycle-Integration:
//   Task_ID: [#CharacterLibrary-CRUD]
//   Timestamp: 2026-01-04
//   Phase: [D-Develop]
//   Context-Analysis: "Updated CharacterLibraryPage to include full CRUD operations with CreateEditCharacterModal integration."
//   Principle_Applied: "Pixel-Perfect-Mandate, Aether-Aesthetics-User-Approved, Context-First-Mandate"
// }}
// {{START_MODIFICATIONS}}

import { ref, computed, onMounted } from 'vue'
import { characterApi } from '@/api/character'
import type { CharacterLibraryVO, CharacterCategoryVO } from '@/types/api'
import CreateEditCharacterModal from '@/components/character-library/CreateEditCharacterModal.vue'

// State
const loading = ref(false)
const categories = ref<CharacterCategoryVO[]>([])
const characters = ref<CharacterLibraryVO[]>([])
const selectedCategoryId = ref<number | null>(null)
const searchQuery = ref('')

// Load data
onMounted(async () => {
  await loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const [categoriesData, charactersData] = await Promise.all([
      characterApi.getCharacterCategories(),
      characterApi.getLibraryCharacters(),
    ])
    categories.value = categoriesData
    characters.value = charactersData
  } catch (error: any) {
    window.$message?.error(error.message || '加载角色库失败')
  } finally {
    loading.value = false
  }
}

// Filtered characters
const filteredCharacters = computed(() => {
  let chars = characters.value

  // Filter by category
  if (selectedCategoryId.value !== null) {
    chars = chars.filter((c) => c.categoryId === selectedCategoryId.value)
  }

  // Filter by search query
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    chars = chars.filter(
      (c) =>
        c.name.toLowerCase().includes(query) ||
        (c.description && c.description.toLowerCase().includes(query))
    )
  }

  return chars
})

// View character details
const selectedCharacter = ref<CharacterLibraryVO | null>(null)
const showDetailModal = ref(false)

// Create/Edit modal
const showCreateEditModal = ref(false)
const editingCharacter = ref<CharacterLibraryVO | null>(null)

const handleCharacterClick = (character: CharacterLibraryVO) => {
  selectedCharacter.value = character
  showDetailModal.value = true
}

const handleCloseDetail = () => {
  showDetailModal.value = false
  selectedCharacter.value = null
}

// Create/Edit handlers
const handleCreate = () => {
  editingCharacter.value = null
  showCreateEditModal.value = true
}

const handleEdit = (character: CharacterLibraryVO) => {
  editingCharacter.value = character
  showCreateEditModal.value = true
  showDetailModal.value = false
}

const handleCreateEditSaved = async () => {
  showCreateEditModal.value = false
  editingCharacter.value = null
  await loadData()
}

const handleCloseCreateEdit = () => {
  showCreateEditModal.value = false
  editingCharacter.value = null
}

// Delete handler
const deleting = ref(false)
const uploading = ref(false)

const handleDelete = async (character: CharacterLibraryVO) => {
  if (deleting.value) return

  const confirmed = confirm(`确定要删除角色"${character.name}"吗？此操作不可恢复。`)
  if (!confirmed) return

  deleting.value = true
  try {
    await characterApi.deleteLibraryCharacter(character.id)
    window.$message?.success('角色删除成功')
    showDetailModal.value = false
    selectedCharacter.value = null
    await loadData()
  } catch (error: any) {
    window.$message?.error(error.message || '删除角色失败')
  } finally {
    deleting.value = false
  }
}

// Upload thumbnail handler
const handleUploadThumbnail = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file || !selectedCharacter.value) return

  uploading.value = true
  try {
    const result = await characterApi.uploadLibraryCharacterThumbnail(selectedCharacter.value.id, file)
    // 更新当前选中的角色缩略图
    selectedCharacter.value.thumbnailUrl = result.url
    // 更新列表中的角色
    const charIndex = characters.value.findIndex(c => c.id === selectedCharacter.value?.id)
    if (charIndex !== -1) {
      characters.value[charIndex].thumbnailUrl = result.url
    }
    window.$message?.success('图片上传成功')
  } catch (error: any) {
    window.$message?.error(error.message || '图片上传失败')
  } finally {
    uploading.value = false
    // 清除input值，允许重复上传同一文件
    target.value = ''
  }
}

// Delete thumbnail handler
const deletingThumbnail = ref(false)
const handleDeleteThumbnail = async () => {
  if (!selectedCharacter.value || !selectedCharacter.value.thumbnailUrl) return
  
  if (!confirm('确定要删除这张图片吗？')) return
  
  deletingThumbnail.value = true
  try {
    // 调用 API 删除缩略图（更新为 null）
    await characterApi.updateLibraryCharacter(selectedCharacter.value.id, {
      name: selectedCharacter.value.name,
      description: selectedCharacter.value.description || '',
      categoryId: selectedCharacter.value.categoryId,
      thumbnailUrl: null
    })
    
    // 更新当前选中的角色缩略图
    selectedCharacter.value.thumbnailUrl = null
    // 更新列表中的角色
    const charIndex = characters.value.findIndex(c => c.id === selectedCharacter.value?.id)
    if (charIndex !== -1) {
      characters.value[charIndex].thumbnailUrl = null
    }
    
    window.$message?.success('图片删除成功')
  } catch (error: any) {
    window.$message?.error(error.message || '图片删除失败')
  } finally {
    deletingThumbnail.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#0D0E12] text-white">
    <!-- Page Header -->
    <div class="border-b border-white/10 bg-[#1E2025]">
      <div class="max-w-[1600px] mx-auto px-8 py-6">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold mb-2">角色库</h1>
            <p class="text-sm text-white/60">管理和浏览所有可用角色</p>
          </div>
          <button
            class="px-4 py-2 rounded-2xl bg-[#00FFCC] text-black font-medium hover:bg-[#00FFCC]/80 transition-colors"
            @click="handleCreate"
          >
            + 创建角色
          </button>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-[1600px] mx-auto px-8 py-6">
      <div class="flex gap-6">
        <!-- Left Sidebar: Categories & Search -->
        <div class="w-64 flex-shrink-0">
          <div class="bg-white/5 border border-white/10 rounded-2xl p-4 sticky top-6">
            <!-- Search -->
            <div class="mb-6">
              <label class="text-xs font-bold text-white/60 mb-2 block">搜索</label>
              <input
                v-model="searchQuery"
                type="text"
                placeholder="角色名称或描述..."
                class="w-full px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-sm text-white placeholder-white/40 focus:outline-none focus:border-[#00FFCC]/50"
              >
            </div>

            <!-- Categories -->
            <div>
              <label class="text-xs font-bold text-white/60 mb-2 block">分类</label>
              <div class="space-y-1">
                <!-- All Category -->
                <button
                  class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors"
                  :class="selectedCategoryId === null ? 'bg-[#00FFCC]/20 text-[#00FFCC] font-medium' : 'text-white/80 hover:bg-white/5'"
                  @click="selectedCategoryId = null"
                >
                  全部角色
                  <span class="float-right text-white/40">{{ characters.length }}</span>
                </button>

                <!-- Category List -->
                <button
                  v-for="category in categories"
                  :key="category.id"
                  class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors"
                  :class="selectedCategoryId === category.id ? 'bg-[#00FFCC]/20 text-[#00FFCC] font-medium' : 'text-white/80 hover:bg-white/5'"
                  @click="selectedCategoryId = category.id"
                >
                  {{ category.name }}
                  <span class="float-right text-white/40">
                    {{ characters.filter(c => c.categoryId === category.id).length }}
                  </span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Content: Character Grid -->
        <div class="flex-1">
          <!-- Results Header -->
          <div class="flex items-center justify-between mb-6">
            <p class="text-sm text-white/60">
              共 <span class="text-[#00FFCC] font-medium">{{ filteredCharacters.length }}</span> 个角色
            </p>
            <!-- Future: Sort/Filter Options -->
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="flex items-center justify-center h-96">
            <div class="w-12 h-12 border-2 border-mochi-cyan border-t-transparent rounded-full animate-spin"></div>
          </div>

          <!-- Character Grid -->
          <div v-else-if="filteredCharacters.length > 0" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
            <div
              v-for="character in filteredCharacters"
              :key="character.id"
              class="bg-white/5 border border-white/10 rounded-2xl p-4 hover:bg-white/10 hover:border-[#00FFCC]/30 transition-all cursor-pointer group"
              @click="handleCharacterClick(character)"
            >
              <!-- Character Info (no thumbnail in list view) -->
              <div class="flex items-start gap-3">
                <!-- Avatar icon -->
                <div class="w-10 h-10 rounded-full bg-white/10 flex items-center justify-center flex-shrink-0">
                  <svg class="w-5 h-5 text-white/40" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                </div>
                <div class="flex-1 min-w-0">
                  <h3 class="text-sm font-medium text-white mb-1 truncate">{{ character.name }}</h3>
                  <p class="text-xs text-white/60 line-clamp-2">
                    {{ character.description || '暂无描述' }}
                  </p>
                </div>
              </div>
              
              <!-- Category Tag (if exists) -->
              <div v-if="character.categoryId" class="mt-3">
                <span class="bg-[#00FFCC]/20 text-[#00FFCC] px-2 py-0.5 rounded-full text-xs font-medium">
                  {{ categories.find(c => c.id === character.categoryId)?.name || '未分类' }}
                </span>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-else class="flex flex-col items-center justify-center h-96 text-white/40">
            <svg class="w-20 h-20 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <p class="text-lg font-medium mb-2">
              {{ searchQuery ? '未找到匹配的角色' : '角色库为空' }}
            </p>
            <p class="text-sm">
              {{ searchQuery ? '尝试使用不同的关键词搜索' : '开始创建你的第一个角色' }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Character Detail Modal -->
    <div
      v-if="showDetailModal && selectedCharacter"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
      @click.self="handleCloseDetail"
    >
      <div class="bg-[#1E2025] border border-white/10 rounded-2xl w-[600px] max-h-[80vh] flex flex-col shadow-2xl" @click.stop>
        <!-- Modal Header -->
        <div class="flex items-center justify-between px-6 py-4 border-b border-white/10">
          <h2 class="text-lg font-bold text-white">角色详情</h2>
          <button
            class="p-2 rounded-lg text-white/60 hover:bg-white/5 hover:text-white transition-colors"
            @click="handleCloseDetail"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- Modal Content -->
        <div class="flex-1 overflow-y-auto p-6">
          <!-- Image -->
          <div class="aspect-square rounded-xl overflow-hidden mb-6 bg-black/20 relative group">
            <img
              v-if="selectedCharacter.thumbnailUrl"
              :src="selectedCharacter.thumbnailUrl"
              :alt="selectedCharacter.name"
              class="w-full h-full object-cover"
            >
            <div
              v-else
              class="w-full h-full flex items-center justify-center"
            >
              <svg class="w-32 h-32 text-white/20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </div>
            
            <!-- 删除按钮（右上角，有图片时显示） -->
            <button
              v-if="selectedCharacter.thumbnailUrl"
              @click="handleDeleteThumbnail"
              :disabled="deletingThumbnail"
              class="absolute top-3 right-3 p-2 rounded-lg bg-red-500/80 hover:bg-red-500 transition-colors opacity-0 group-hover:opacity-100 z-10"
              title="删除图片"
            >
              <svg v-if="!deletingThumbnail" class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
              </svg>
              <svg v-else class="w-5 h-5 text-white animate-spin" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            </button>
            
            <!-- 上传按钮覆盖层 -->
            <label
              class="absolute inset-0 flex flex-col items-center justify-center bg-black/60 cursor-pointer transition-opacity"
              :class="selectedCharacter.thumbnailUrl ? 'opacity-0 group-hover:opacity-100' : 'opacity-100'"
            >
              <input
                type="file"
                accept="image/*"
                class="hidden"
                :disabled="uploading"
                @change="handleUploadThumbnail"
              >
              <svg v-if="!uploading" class="w-10 h-10 text-white/80 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path>
              </svg>
              <svg v-else class="w-10 h-10 text-white/80 mb-2 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <span class="text-white/80 text-sm">{{ uploading ? '上传中...' : '点击上传图片' }}</span>
            </label>
          </div>

          <!-- Info -->
          <div class="space-y-4">
            <div>
              <label class="text-xs font-bold text-white/60 mb-1 block">角色名称</label>
              <p class="text-white">{{ selectedCharacter.name }}</p>
            </div>

            <div v-if="selectedCharacter.categoryId">
              <label class="text-xs font-bold text-white/60 mb-1 block">分类</label>
              <span class="inline-block bg-[#00FFCC]/20 text-[#00FFCC] px-3 py-1 rounded-full text-sm font-medium">
                {{ categories.find(c => c.id === selectedCharacter?.categoryId)?.name || '未分类' }}
              </span>
            </div>

            <div>
              <label class="text-xs font-bold text-white/60 mb-1 block">描述</label>
              <p class="text-white/80 whitespace-pre-wrap">{{ selectedCharacter.description || '暂无描述' }}</p>
            </div>

            <div>
              <label class="text-xs font-bold text-white/60 mb-1 block">创建时间</label>
              <p class="text-white/60 text-sm">{{ new Date(selectedCharacter.createdAt).toLocaleString('zh-CN') }}</p>
            </div>
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="flex items-center justify-between gap-3 px-6 py-4 border-t border-white/10">
          <button
            class="px-4 py-2 rounded-2xl border border-red-400/50 text-red-400 hover:bg-red-400/10 transition-colors text-sm"
            :disabled="deleting"
            @click="handleDelete(selectedCharacter!)"
          >
            {{ deleting ? '删除中...' : '删除' }}
          </button>
          <div class="flex items-center gap-3">
            <button
              class="px-4 py-2 rounded-2xl border border-white/20 text-white/80 hover:bg-white/5 transition-colors text-sm"
              @click="handleCloseDetail"
            >
              关闭
            </button>
            <button
              class="px-4 py-2 rounded-2xl bg-[#00FFCC] text-black font-medium hover:bg-[#00FFCC]/80 transition-colors text-sm"
              @click="handleEdit(selectedCharacter!)"
            >
              编辑
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Character Modal -->
    <CreateEditCharacterModal
      v-if="showCreateEditModal"
      :character="editingCharacter"
      :categories="categories"
      @close="handleCloseCreateEdit"
      @saved="handleCreateEditSaved"
    />
  </div>
</template>

// {{END_MODIFICATIONS}}
