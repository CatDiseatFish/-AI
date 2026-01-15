<script setup lang="ts">
import { ref } from 'vue'
import { useEditorStore } from '@/stores/editor'

const editorStore = useEditorStore()

// 批量生成分镜模态框
const showShotGenerateModal = ref(false)
const shotGenerateMode = ref<'ALL' | 'MISSING'>('MISSING')
const shotGenerateCount = ref(1)

// 批量生成视频模态框
const showVideoGenerateModal = ref(false)
const videoGenerateMode = ref<'ALL' | 'MISSING'>('MISSING')
const videoGenerateCount = ref(1)

// 全选
const handleSelectAll = () => {
  editorStore.selectAll()
}

// 反选
const handleInvertSelection = () => {
  const allShotIds = new Set(editorStore.shots.map(s => s.id))
  const currentSelected = new Set(editorStore.selectedShotIds)

  // 清空当前选择
  editorStore.deselectAll()

  // 选中未选中的
  allShotIds.forEach(id => {
    if (!currentSelected.has(id)) {
      editorStore.toggleShotSelection(id)
    }
  })
}

// 取消选择
const handleCancelSelection = () => {
  editorStore.deselectAll()
}

// 打开批量生成分镜模态框
const handleOpenShotGenerate = () => {
  showShotGenerateModal.value = true
}

// 确认批量生成分镜
const handleConfirmShotGenerate = async () => {
  try {
    await editorStore.batchGenerateShots({
      targetIds: Array.from(editorStore.selectedShotIds),
      mode: shotGenerateMode.value,
      countPerItem: shotGenerateCount.value,
      aspectRatio: '21:9',
    })
    showShotGenerateModal.value = false
    editorStore.deselectAll()
    window.$message?.success('批量生成分镜任务已提交')
  } catch (error: any) {
    console.error('[BatchOperationBar] Failed to batch generate shots:', error)
    window.$message?.error(error.message || '批量生成分镜失败')
  }
}

// 打开批量生成视频模态框
const handleOpenVideoGenerate = () => {
  showVideoGenerateModal.value = true
}

// 确认批量生成视频
const handleConfirmVideoGenerate = async () => {
  try {
    await editorStore.batchGenerateVideos({
      targetIds: Array.from(editorStore.selectedShotIds),
      mode: videoGenerateMode.value,
      countPerItem: videoGenerateCount.value,
      aspectRatio: '16:9',
    })
    showVideoGenerateModal.value = false
    editorStore.deselectAll()
    window.$message?.success('批量生成视频任务已提交')
  } catch (error: any) {
    console.error('[BatchOperationBar] Failed to batch generate videos:', error)
    window.$message?.error(error.message || '批量生成视频失败')
  }
}
</script>

<template>
  <!-- 底部浮动操作栏 -->
  <Transition
    enter-active-class="transition-all duration-300 ease-out"
    enter-from-class="translate-y-full opacity-0"
    enter-to-class="translate-y-0 opacity-100"
    leave-active-class="transition-all duration-200 ease-in"
    leave-from-class="translate-y-0 opacity-100"
    leave-to-class="translate-y-full opacity-0"
  >
    <div
      v-if="editorStore.hasSelection"
      class="fixed bottom-6 left-1/2 -translate-x-1/2 z-40 px-6 py-4 bg-[#1E2025] border border-white/20 rounded-2xl shadow-2xl backdrop-blur-xl"
    >
      <div class="flex items-center gap-4">
        <!-- 选中数量 -->
        <div class="flex items-center gap-2 px-4 py-2 bg-[#00FFCC]/10 border border-[#00FFCC]/30 rounded-full">
          <svg class="w-4 h-4 text-[#00FFCC]" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path>
          </svg>
          <span class="text-[#00FFCC] text-sm font-semibold">
            已选中 {{ editorStore.selectedShotIds.size }} 条
          </span>
        </div>

        <!-- 分隔线 -->
        <div class="w-px h-6 bg-white/10"></div>

        <!-- 全选按钮 -->
        <button
          class="px-4 py-2 text-sm text-white/80 bg-white/5 border border-white/10 rounded-full hover:bg-white/10 transition-colors flex items-center gap-2"
          @click="handleSelectAll"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"></path>
          </svg>
          全选
        </button>

        <!-- 反选按钮 -->
        <button
          class="px-4 py-2 text-sm text-white/80 bg-white/5 border border-white/10 rounded-full hover:bg-white/10 transition-colors flex items-center gap-2"
          @click="handleInvertSelection"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"></path>
          </svg>
          反选
        </button>

        <!-- 分隔线 -->
        <div class="w-px h-6 bg-white/10"></div>

        <!-- 批量生成分镜 -->
        <button
          class="px-4 py-2 text-sm bg-purple-500/20 text-purple-400 border border-purple-500/30 rounded-full hover:bg-purple-500/30 transition-colors flex items-center gap-2"
          @click="handleOpenShotGenerate"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.5">
            <path d="m12 3-1.9 5.8-5.8 1.9 5.8 1.9L12 21l1.9-5.8 5.8-1.9-5.8-1.9L12 3z"></path>
          </svg>
          批量生成分镜
        </button>

        <!-- 批量生成视频 -->
        <button
          class="px-4 py-2 text-sm bg-blue-500/20 text-blue-400 border border-blue-500/30 rounded-full hover:bg-blue-500/30 transition-colors flex items-center gap-2"
          @click="handleOpenVideoGenerate"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2">
            <path stroke-linecap="round" d="M15.75 10.5l4.72-4.72a.75.75 0 011.28.53v11.38a.75.75 0 01-1.28.53l-4.72-4.72M4.5 18.75h9a2.25 2.25 0 002.25-2.25v-9a2.25 2.25 0 00-2.25-2.25h-9A2.25 2.25 0 002.25 7.5v9a2.25 2.25 0 002.25 2.25z"></path>
          </svg>
          批量生成视频
        </button>

        <!-- 分隔线 -->
        <div class="w-px h-6 bg-white/10"></div>

        <!-- 取消选择 -->
        <button
          class="px-4 py-2 text-sm text-white/60 bg-white/5 border border-white/10 rounded-full hover:bg-white/10 transition-colors"
          @click="handleCancelSelection"
        >
          取消选择
        </button>
      </div>
    </div>
  </Transition>

  <!-- 批量生成分镜模态框 -->
  <Transition
    enter-active-class="transition-opacity duration-200"
    leave-active-class="transition-opacity duration-200"
    enter-from-class="opacity-0"
    leave-to-class="opacity-0"
  >
    <div
      v-if="showShotGenerateModal"
      class="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50"
      @click.self="showShotGenerateModal = false"
    >
      <div class="bg-[#1E2025] w-[500px] rounded-2xl p-6 shadow-2xl">
        <h3 class="text-white text-lg font-semibold mb-4">批量生成分镜图</h3>

        <div class="space-y-4">
          <!-- 生成模式 -->
          <div>
            <label class="block text-sm font-medium text-white/80 mb-2">生成模式</label>
            <div class="flex items-center gap-3">
              <button
                :class="[
                  'flex-1 px-4 py-2.5 rounded-xl text-sm font-medium transition-colors',
                  shotGenerateMode === 'MISSING'
                    ? 'bg-[#00FFCC]/20 text-[#00FFCC] border-2 border-[#00FFCC]'
                    : 'bg-white/5 text-white/60 border-2 border-white/10 hover:bg-white/10'
                ]"
                @click="shotGenerateMode = 'MISSING'"
              >
                缺失生成
                <span class="block text-xs text-white/40 mt-1">仅为未生成的分镜生成图片</span>
              </button>
              <button
                :class="[
                  'flex-1 px-4 py-2.5 rounded-xl text-sm font-medium transition-colors',
                  shotGenerateMode === 'ALL'
                    ? 'bg-[#00FFCC]/20 text-[#00FFCC] border-2 border-[#00FFCC]'
                    : 'bg-white/5 text-white/60 border-2 border-white/10 hover:bg-white/10'
                ]"
                @click="shotGenerateMode = 'ALL'"
              >
                全部生成
                <span class="block text-xs text-white/40 mt-1">为所有分镜重新生成图片</span>
              </button>
            </div>
          </div>

          <!-- 生成数量 -->
          <div>
            <label class="block text-sm font-medium text-white/80 mb-2">
              每条分镜生成数量
            </label>
            <input
              v-model.number="shotGenerateCount"
              type="number"
              min="1"
              max="4"
              class="w-full px-4 py-2.5 bg-[#0D0E12] border border-white/10 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-[#00FFCC]/50"
            >
            <p class="text-xs text-white/40 mt-1">建议1-4张，生成多张可供选择最佳效果</p>
          </div>

          <!-- 消耗预估 -->
          <div class="bg-white/5 border border-white/10 rounded-xl p-3">
            <p class="text-white/60 text-xs">
              💡 预计消耗：
              <span class="text-[#00FFCC] font-semibold">
                {{ editorStore.selectedShotIds.size * shotGenerateCount * 50 }} 积分
              </span>
            </p>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex items-center justify-end gap-3 mt-6">
          <button
            class="px-5 py-2 bg-white/10 text-white/60 text-sm rounded-2xl hover:bg-white/20 transition-colors"
            @click="showShotGenerateModal = false"
          >
            取消
          </button>
          <button
            class="px-5 py-2 bg-[#00FFCC] text-black font-semibold text-sm rounded-2xl hover:bg-[#21FFF3] transition-colors"
            @click="handleConfirmShotGenerate"
          >
            确认生成
          </button>
        </div>
      </div>
    </div>
  </Transition>

  <!-- 批量生成视频模态框 -->
  <Transition
    enter-active-class="transition-opacity duration-200"
    leave-active-class="transition-opacity duration-200"
    enter-from-class="opacity-0"
    leave-to-class="opacity-0"
  >
    <div
      v-if="showVideoGenerateModal"
      class="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50"
      @click.self="showVideoGenerateModal = false"
    >
      <div class="bg-[#1E2025] w-[500px] rounded-2xl p-6 shadow-2xl">
        <h3 class="text-white text-lg font-semibold mb-4">批量生成视频</h3>

        <div class="space-y-4">
          <!-- 生成模式 -->
          <div>
            <label class="block text-sm font-medium text-white/80 mb-2">生成模式</label>
            <div class="flex items-center gap-3">
              <button
                :class="[
                  'flex-1 px-4 py-2.5 rounded-xl text-sm font-medium transition-colors',
                  videoGenerateMode === 'MISSING'
                    ? 'bg-[#00FFCC]/20 text-[#00FFCC] border-2 border-[#00FFCC]'
                    : 'bg-white/5 text-white/60 border-2 border-white/10 hover:bg-white/10'
                ]"
                @click="videoGenerateMode = 'MISSING'"
              >
                缺失生成
                <span class="block text-xs text-white/40 mt-1">仅为未生成的分镜生成视频</span>
              </button>
              <button
                :class="[
                  'flex-1 px-4 py-2.5 rounded-xl text-sm font-medium transition-colors',
                  videoGenerateMode === 'ALL'
                    ? 'bg-[#00FFCC]/20 text-[#00FFCC] border-2 border-[#00FFCC]'
                    : 'bg-white/5 text-white/60 border-2 border-white/10 hover:bg-white/10'
                ]"
                @click="videoGenerateMode = 'ALL'"
              >
                全部生成
                <span class="block text-xs text-white/40 mt-1">为所有分镜重新生成视频</span>
              </button>
            </div>
          </div>

          <!-- 生成数量 -->
          <div>
            <label class="block text-sm font-medium text-white/80 mb-2">
              每条分镜生成数量
            </label>
            <input
              v-model.number="videoGenerateCount"
              type="number"
              min="1"
              max="3"
              class="w-full px-4 py-2.5 bg-[#0D0E12] border border-white/10 rounded-xl text-white text-sm focus:outline-none focus:ring-2 focus:ring-[#00FFCC]/50"
            >
            <p class="text-xs text-white/40 mt-1">建议1-3个，生成多个可供选择最佳效果</p>
          </div>

          <!-- 消耗预估 -->
          <div class="bg-white/5 border border-white/10 rounded-xl p-3">
            <p class="text-white/60 text-xs">
              💡 预计消耗：
              <span class="text-[#00FFCC] font-semibold">
                {{ editorStore.selectedShotIds.size * videoGenerateCount * 100 }} 积分
              </span>
            </p>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex items-center justify-end gap-3 mt-6">
          <button
            class="px-5 py-2 bg-white/10 text-white/60 text-sm rounded-2xl hover:bg-white/20 transition-colors"
            @click="showVideoGenerateModal = false"
          >
            取消
          </button>
          <button
            class="px-5 py-2 bg-[#00FFCC] text-black font-semibold text-sm rounded-2xl hover:bg-[#21FFF3] transition-colors"
            @click="handleConfirmVideoGenerate"
          >
            确认生成
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>
