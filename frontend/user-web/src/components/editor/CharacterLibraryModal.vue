<script setup lang="ts">
// {{CODE-Cycle-Integration:
//   Task_ID: [#CharacterLibrary-Modal]
//   Timestamp: 2026-01-04
//   Phase: [D-Develop]
//   Context-Analysis: "Creating CharacterLibraryModal for selecting characters from library to add to project. Shows categorized characters with search, supports selecting and adding to project."
//   Principle_Applied: "Pixel-Perfect-Mandate, Aether-Aesthetics-User-Approved, Context-First-Mandate"
// }}
// {{START_MODIFICATIONS}}

import { ref, computed, onMounted } from 'vue'
import { characterApi } from '@/api/character'
import { useEditorStore } from '@/stores/editor'
import type { CharacterLibraryVO, CharacterCategoryVO } from '@/types/api'

interface Props {
  projectId: number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  close: []
  added: []
}>()

const editorStore = useEditorStore()

// State
const loading = ref(false)
const categories = ref<CharacterCategoryVO[]>([])
const libraryCharacters = ref<CharacterLibraryVO[]>([])
const selectedCategoryId = ref<number | null>(null)
const searchQuery = ref('')
const showCreateCategory = ref(false)
const newCategoryName = ref('')
const creatingCategory = ref(false)

// Load data
onMounted(async () => {
  loading.value = true
  try {
    const [categoriesData, charactersData] = await Promise.all([
      characterApi.getCharacterCategories(),
      characterApi.getLibraryCharacters(),
    ])
    categories.value = categoriesData
    libraryCharacters.value = charactersData
  } catch (error: any) {
    window.$message?.error(error.message || '加载角色库失败')
  } finally {
    loading.value = false
  }
})

// Filtered characters
const filteredCharacters = computed(() => {
  let chars = libraryCharacters.value

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

// Add character to project
const addingId = ref<number | null>(null)
const handleAddCharacter = async (character: CharacterLibraryVO) => {
  addingId.value = character.id
  try {
    await characterApi.createFromLibrary(props.projectId, character.id)
    // Refresh project characters
    await editorStore.fetchCharacters()
    window.$message?.success(`已添加角色：${character.name}`)
    emit('added')
  } catch (error: any) {
    window.$message?.error(error.message || '添加角色失败')
  } finally {
    addingId.value = null
  }
}

// Create category
const handleCreateCategory = async () => {
  if (!newCategoryName.value.trim()) {
    window.$message?.warning('请输入分类名称')
    return
  }
  creatingCategory.value = true
  try {
    const newCategory = await characterApi.createCategory({ name: newCategoryName.value.trim() })
    categories.value.push(newCategory)
    newCategoryName.value = ''
    showCreateCategory.value = false
    window.$message?.success('分类创建成功')
  } catch (error: any) {
    window.$message?.error(error.message || '创建分类失败')
  } finally {
    creatingCategory.value = false
  }
}

// Close modal
const handleClose = () => {
  emit('close')
}

// Change category
const showCategorySelect = ref<number | null>(null)
const changingCategoryId = ref<number | null>(null)

const handleShowCategorySelect = (characterId: number) => {
  showCategorySelect.value = characterId
}

const handleChangeCategory = async (character: CharacterLibraryVO, categoryId: number | null) => {
  changingCategoryId.value = character.id
  try {
    await characterApi.updateLibraryCharacter(character.id, {
      name: character.name,
      description: character.description || '',
      categoryId: categoryId
    })
    // Update local data
    const index = libraryCharacters.value.findIndex(c => c.id === character.id)
    if (index !== -1) {
      libraryCharacters.value[index].categoryId = categoryId
    }
    window.$message?.success('分类修改成功')
    showCategorySelect.value = null
  } catch (error: any) {
    window.$message?.error(error.message || '修改分类失败')
  } finally {
    changingCategoryId.value = null
  }
}

// Delete thumbnail
const deletingThumbnailId = ref<number | null>(null)
const handleDeleteThumbnail = async (character: CharacterLibraryVO) => {
  if (!character.thumbnailUrl) return
  
  if (!confirm('确定要删除这张图片吗？')) return
  
  deletingThumbnailId.value = character.id
  try {
    await characterApi.updateLibraryCharacter(character.id, {
      name: character.name,
      description: character.description || '',
      categoryId: character.categoryId,
      thumbnailUrl: null
    })
    // Update local data
    const index = libraryCharacters.value.findIndex(c => c.id === character.id)
    if (index !== -1) {
      libraryCharacters.value[index].thumbnailUrl = null
    }
    window.$message?.success('图片删除成功')
  } catch (error: any) {
    window.$message?.error(error.message || '删除图片失败')
  } finally {
    deletingThumbnailId.value = null
  }
}

// Delete character
const deletingCharacterId = ref<number | null>(null)
const handleDeleteCharacter = async (character: CharacterLibraryVO) => {
  console.log('[CharacterLibraryModal] 删除角色:', character.name, 'shots:', editorStore.shots.length)
  
  // 检查角色是否被分镜绑定（通过名字判断）
  const boundShotNames: string[] = []
  for (const shot of editorStore.shots) {
    if (shot.characters && shot.characters.length > 0) {
      const hasSameName = shot.characters.some(c => c.characterName === character.name)
      if (hasSameName) {
        boundShotNames.push(`分镜${shot.shotNo}`)
      }
    }
  }
  
  console.log('[CharacterLibraryModal] 绑定的分镜:', boundShotNames)
  
  if (boundShotNames.length > 0) {
    window.$message?.error(`角色「${character.name}」已被绑定到${boundShotNames.join('、')}，请先在分镜表中解绑后再删除`)
    return
  }
  
  // 确认删除
  const confirmed = window.confirm(`确定要删除角色「${character.name}」吗？此操作不可恢复。`)
  if (!confirmed) {
    console.log('[CharacterLibraryModal] 用户取消删除')
    return
  }
  
  deletingCharacterId.value = character.id
  try {
    await characterApi.deleteLibraryCharacter(character.id)
    // Remove from local data
    libraryCharacters.value = libraryCharacters.value.filter(c => c.id !== character.id)
    // 清除该角色的本地图片历史记录
    editorStore.clearLocalImageHistoryByName(character.name)
    // 刷新项目角色列表（同步角色库的删除）
    await editorStore.fetchCharacters()
    window.$message?.success('角色删除成功')
  } catch (error: any) {
    console.error('[CharacterLibraryModal] 删除失败:', error)
    window.$message?.error(error.message || '删除角色失败')
  } finally {
    deletingCharacterId.value = null
  }
}
</script>

<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm" @click.self="handleClose">
    <!-- Modal Container -->
    <div class="bg-[#1E2025] border border-white/10 rounded-2xl w-[900px] max-h-[80vh] flex flex-col shadow-2xl" @click.stop>
      <!-- Header -->
      <div class="flex items-center justify-between px-6 py-4 border-b border-white/10">
        <h2 class="text-lg font-bold text-white">从角色库中选择</h2>
        <button
          class="p-2 rounded-lg text-white/60 hover:bg-white/5 hover:text-white transition-colors"
          @click="handleClose"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Content -->
      <div class="flex flex-1 overflow-hidden">
        <!-- Left Sidebar: Categories -->
        <div class="w-48 border-r border-white/10 p-4 overflow-y-auto">
          <h3 class="text-xs font-bold text-white/60 mb-3">分类</h3>
          <div class="space-y-1">
            <!-- All Category -->
            <button
              class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors"
              :class="selectedCategoryId === null ? 'bg-[#00FFCC]/20 text-[#00FFCC]' : 'text-white/80 hover:bg-white/5'"
              @click="selectedCategoryId = null"
            >
              全部
            </button>

            <!-- Category List -->
            <button
              v-for="category in categories"
              :key="category.id"
              class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors"
              :class="selectedCategoryId === category.id ? 'bg-[#00FFCC]/20 text-[#00FFCC]' : 'text-white/80 hover:bg-white/5'"
              @click="selectedCategoryId = category.id"
            >
              {{ category.name }}
            </button>

            <!-- Add Category Button -->
            <div class="mt-3 pt-3 border-t border-white/10">
              <div v-if="showCreateCategory" class="space-y-2">
                <input
                  v-model="newCategoryName"
                  type="text"
                  placeholder="分类名称..."
                  class="w-full px-3 py-2 bg-white/5 border border-white/10 rounded-lg text-white text-sm placeholder-white/40 focus:outline-none focus:border-[#00FFCC]/50"
                  @keyup.enter="handleCreateCategory"
                  @keyup.esc="showCreateCategory = false"
                >
                <div class="flex gap-2">
                  <button
                    class="flex-1 px-3 py-1.5 bg-[#00FFCC] text-black text-xs font-medium rounded-lg hover:bg-[#00FFCC]/80 transition-colors disabled:opacity-50"
                    :disabled="creatingCategory"
                    @click="handleCreateCategory"
                  >
                    {{ creatingCategory ? '创建中...' : '确定' }}
                  </button>
                  <button
                    class="px-3 py-1.5 bg-white/10 text-white/80 text-xs rounded-lg hover:bg-white/20 transition-colors"
                    @click="showCreateCategory = false; newCategoryName = ''"
                  >
                    取消
                  </button>
                </div>
              </div>
              <button
                v-else
                class="w-full text-left px-3 py-2 rounded-lg text-sm text-white/60 hover:bg-white/5 hover:text-white transition-colors flex items-center gap-2"
                @click="showCreateCategory = true"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                </svg>
                添加分类
              </button>
            </div>
          </div>
        </div>

        <!-- Right Content: Characters -->
        <div class="flex-1 flex flex-col overflow-hidden">
          <!-- Search Bar -->
          <div class="px-6 py-4 border-b border-white/10">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索角色名称或描述..."
              class="w-full px-4 py-2 bg-white/5 border border-white/10 rounded-lg text-white placeholder-white/40 focus:outline-none focus:border-[#00FFCC]/50"
            >
          </div>

          <!-- Character Grid -->
          <div class="flex-1 overflow-y-auto px-6 py-4">
            <!-- Loading State -->
            <div v-if="loading" class="flex items-center justify-center h-64">
              <div class="w-8 h-8 border-2 border-mochi-cyan border-t-transparent rounded-full animate-spin"></div>
            </div>

            <!-- Character Cards -->
            <div v-else-if="filteredCharacters.length > 0" class="grid grid-cols-4 gap-4">
              <div
                v-for="character in filteredCharacters"
                :key="character.id"
                class="bg-white/5 border border-white/10 rounded-xl p-3 hover:bg-white/10 transition-all group"
              >
                <!-- Thumbnail -->
                <div class="aspect-square rounded-lg overflow-hidden mb-2 relative">
                  <img
                    v-if="character.thumbnailUrl"
                    :src="character.thumbnailUrl"
                    :alt="character.name"
                    class="w-full h-full object-cover"
                  >
                  <div
                    v-else
                    class="w-full h-full bg-black/20 flex items-center justify-center"
                  >
                    <svg class="w-12 h-12 text-white/30" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                      <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                  </div>

                  <!-- Action Overlay -->
                  <div class="absolute inset-0 bg-black/60 flex flex-col items-center justify-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    <!-- Category Select Dropdown -->
                    <div v-if="showCategorySelect === character.id" class="w-full px-2">
                      <div class="bg-[#2A2D35] rounded-lg p-2 space-y-1 max-h-32 overflow-y-auto">
                        <button
                          class="w-full text-left px-2 py-1.5 rounded text-xs transition-colors"
                          :class="character.categoryId === null ? 'bg-[#00FFCC]/20 text-[#00FFCC]' : 'text-white/80 hover:bg-white/10'"
                          :disabled="changingCategoryId === character.id"
                          @click="handleChangeCategory(character, null)"
                        >
                          未分类
                        </button>
                        <button
                          v-for="cat in categories"
                          :key="cat.id"
                          class="w-full text-left px-2 py-1.5 rounded text-xs transition-colors"
                          :class="character.categoryId === cat.id ? 'bg-[#00FFCC]/20 text-[#00FFCC]' : 'text-white/80 hover:bg-white/10'"
                          :disabled="changingCategoryId === character.id"
                          @click="handleChangeCategory(character, cat.id)"
                        >
                          {{ cat.name }}
                        </button>
                      </div>
                      <button
                        class="w-full mt-2 px-3 py-1.5 rounded-lg border border-white/20 text-white/60 text-xs hover:bg-white/5"
                        @click="showCategorySelect = null"
                      >
                        取消
                      </button>
                    </div>
                    <!-- Action Buttons -->
                    <template v-else>
                      <div class="flex flex-col gap-2">
                        <button
                          class="px-3 py-1.5 rounded-lg border border-white/30 text-white/80 text-xs hover:bg-white/10 transition-colors"
                          @click.stop="handleShowCategorySelect(character.id)"
                        >
                          {{ character.categoryId ? '修改分类' : '添加到分类' }}
                        </button>
                        <!-- 删除角色按钮 -->
                        <button
                          class="px-3 py-1.5 rounded-lg border border-red-400/50 text-red-400 text-xs hover:bg-red-400/10 transition-colors disabled:opacity-50"
                          :disabled="deletingCharacterId === character.id"
                          @click.stop="handleDeleteCharacter(character)"
                        >
                          {{ deletingCharacterId === character.id ? '删除中...' : '删除角色' }}
                        </button>
                      </div>
                    </template>
                  </div>
                </div>

                <!-- Character Info -->
                <h4 class="text-sm font-medium text-white truncate mb-1">{{ character.name }}</h4>
                <p class="text-xs text-white/60 line-clamp-2 h-8">{{ character.description || '暂无描述' }}</p>
              </div>
            </div>

            <!-- Empty State -->
            <div v-else class="flex flex-col items-center justify-center h-64 text-white/40">
              <svg class="w-16 h-16 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <p class="text-sm">{{ searchQuery ? '未找到匹配的角色' : '角色库为空' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-white/10">
        <button
          class="px-4 py-2 rounded-lg border border-white/20 text-white/80 hover:bg-white/5 transition-colors text-sm"
          @click="handleClose"
        >
          关闭
        </button>
      </div>
    </div>
  </div>
</template>

// {{END_MODIFICATIONS}}
