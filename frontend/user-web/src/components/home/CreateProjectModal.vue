<script setup lang="ts">
import { ref, computed } from 'vue'

const props = defineProps<{
  show: boolean
  loading?: boolean
  folderId?: number | null
}>()

const emit = defineEmits<{
  close: []
  confirm: [data: {
    name: string
    folderId?: number
    rawText: string
  }]
}>()

const projectName = ref('')
const storyText = ref('')

// Char count
const charCount = computed(() => storyText.value.length)
const maxChars = 5000

const handleConfirm = () => {
  if (!projectName.value.trim()) {
    alert('请输入项目名称')
    return
  }

  emit('confirm', {
    name: projectName.value.trim(),
    folderId: props.folderId || undefined,
    rawText: storyText.value,
  })
}

const handleClose = () => {
  // Reset form
  projectName.value = ''
  storyText.value = ''
  emit('close')
}
</script>

<template>
  <!-- Modal Overlay -->
  <Transition
    enter-active-class="transition-opacity duration-200"
    leave-active-class="transition-opacity duration-200"
    enter-from-class="opacity-0"
    leave-to-class="opacity-0"
  >
    <div
      v-if="show"
      class="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4"
      @click.self="handleClose"
    >
      <!-- Modal Container -->
      <div class="bg-[#1a1b1f] w-[500px] max-h-[90vh] rounded-xl flex flex-col shadow-2xl">
        <!-- Header -->
        <div class="relative flex items-center justify-center py-4 border-b border-white/5">
          <h2 class="text-[16px] font-medium text-[#ddd]">创建作品</h2>
          <button
            class="absolute right-5 top-4 text-white/60 hover:text-white transition-colors"
            @click="handleClose"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <!-- Body -->
        <div class="flex flex-col gap-6 p-8">
          <!-- Project Name -->
          <div>
            <div class="text-[14px] text-white/60 mb-2">项目名称</div>
            <input
              v-model="projectName"
              type="text"
              placeholder="请输入项目名称"
              class="w-full bg-[#26272c] border-none rounded-lg px-3 py-2.5 text-white text-[14px] outline-none placeholder-[#555] focus:ring-1 focus:ring-[#00e5bf]"
            />
          </div>

          <!-- Story Script -->
          <div class="flex flex-col">
            <div class="text-[14px] text-white/60 mb-2">故事文案（选填）</div>
            <div class="bg-[#26272c] rounded-lg p-3 flex flex-col">
              <textarea
                v-model="storyText"
                placeholder="输入你的故事文案..."
                class="h-32 bg-transparent border-none text-white text-[14px] leading-[1.5] resize-none outline-none placeholder-[#555]"
                :maxlength="maxChars"
              ></textarea>
              <div class="flex items-center justify-end mt-2 pt-2 border-t border-white/5">
                <span class="text-[12px] text-[#555]">{{ charCount }} / {{ maxChars }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="flex items-center justify-center gap-4 px-8 pb-6">
          <button
            class="px-8 py-2.5 bg-[#00e5bf] text-[#1a1b1f] text-[14px] font-medium rounded-full hover:bg-[#00d4b0] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="loading"
            @click="handleConfirm"
          >
            {{ loading ? '创建中...' : '创建' }}
          </button>
          <button
            class="px-8 py-2.5 bg-transparent border border-[#555] text-[#ddd] text-[14px] font-medium rounded-full hover:border-[#777] hover:bg-white/5 transition-colors"
            @click="handleClose"
          >
            取消
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
/* Custom scrollbar for style grid */
.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}
.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}
.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #3f4148;
  border-radius: 2px;
}
.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>
