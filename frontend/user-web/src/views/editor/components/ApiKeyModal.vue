<script setup lang="ts">
import { ref, watch } from 'vue'
const props = defineProps<{
  visible: boolean
  hasApiKey?: boolean
}>()

const emit = defineEmits<{
  close: []
  saved: []
}>()

const apiKey = ref('')
const submitting = ref(false)

watch(
  () => props.visible,
  (value) => {
    if (value) {
      apiKey.value = ''
    }
  }
)

const handleClose = () => {
  if (!submitting.value) {
    emit('close')
  }
}

const handleSave = async () => {
  if (submitting.value) return
  const trimmed = apiKey.value.trim()

  if (!trimmed && props.hasApiKey) {
    const confirmed = window.confirm('未输入密钥将清除已保存的密钥，确定继续吗？')
    if (!confirmed) return
  }

  submitting.value = true
  try {
    if (trimmed) {
      localStorage.setItem('ai_api_key', trimmed)
      window.$message?.success('API密钥已保存')
    } else {
      localStorage.removeItem('ai_api_key')
      window.$message?.success('API密钥已清除')
    }
    emit('saved')
  } catch (error: any) {
    window.$message?.error(error.message || '保存失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div
    v-if="visible"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
    @click.self="handleClose"
  >
    <div class="bg-bg-elevated border border-border-default rounded w-[520px] max-w-[90vw] p-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-text-primary text-base font-semibold">设置API密钥</h3>
        <button
          class="p-2 rounded hover:bg-bg-hover transition-colors"
          :disabled="submitting"
          @click="handleClose"
        >
          <svg class="w-5 h-5 text-text-secondary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <div class="mb-4">
        <label class="text-xs font-bold text-text-secondary mb-2 block">API密钥</label>
        <input
          v-model="apiKey"
          type="password"
          placeholder="输入你的API密钥（留空可清除）"
          class="w-full px-4 py-3 bg-bg-subtle border border-border-default rounded text-text-primary placeholder-text-tertiary focus:outline-none focus:border-gray-900/50"
          :disabled="submitting"
          autocomplete="off"
        >
        <p class="text-text-tertiary text-xs mt-2">
          密钥仅用于本账号全部生成请求。留空并保存可清除已设置的密钥。
        </p>
      </div>

      <div class="flex items-center justify-end gap-3">
        <button
          class="px-4 py-2 rounded border border-border-default text-text-secondary hover:bg-bg-subtle transition-colors text-sm"
          :disabled="submitting"
          @click="handleClose"
        >
          取消
        </button>
        <button
          class="px-4 py-2 rounded bg-gray-900 text-white font-medium hover:bg-gray-700 transition-colors text-sm disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="submitting"
          @click="handleSave"
        >
          {{ submitting ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>
  </div>
</template>
