<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useEditorStore } from '@/stores/editor'
import { jobApi } from '@/api/job'
import type { JobVO } from '@/types/api'

const emit = defineEmits<{
  close: []
}>()

const editorStore = useEditorStore()
const filterType = ref<'all' | 'image' | 'video'>('all')
const sortBy = ref<'date' | 'type'>('date')
const loading = ref(false)
const historyRecords = ref<JobVO[]>([])

const loadHistory = async () => {
  if (!editorStore.projectId) return
  loading.value = true
  try {
    const response = await jobApi.getUserJobs({
      page: 1,
      size: 100,
      projectId: editorStore.projectId,
    })
    historyRecords.value = response.records || []
  } catch (error) {
    console.error('[HistoryPanel] \u52a0\u8f7d\u5386\u53f2\u8bb0\u5f55\u5931\u8d25:', error)
    historyRecords.value = []
  } finally {
    loading.value = false
  }
}

const parseMeta = (metaJson?: string | null) => {
  if (!metaJson) return null
  try {
    return JSON.parse(metaJson) as Record<string, any>
  } catch (error) {
    console.warn('[HistoryPanel] metaJson\u89e3\u6790\u5931\u8d25:', error)
    return null
  }
}

const getJobCategory = (jobType: string) => {
  if (jobType.includes('VIDEO')) return 'video'
  if (jobType.includes('IMG')) return 'image'
  return 'other'
}

const formatDuration = (seconds: number) => {
  if (!Number.isFinite(seconds) || seconds < 0) return '--:--'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

const formatDateTime = (value?: string | null) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  const ss = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${hh}:${mm}:${ss}`
}

const getTargetLabel = (record: JobVO) => {
  const meta = parseMeta(record.metaJson)
  const targetType = meta?.targetType as string | undefined
  const targetId = meta?.targetId as number | undefined
  const targetIds = Array.isArray(meta?.targetIds) ? meta?.targetIds as number[] : null

  const formatShot = (id: number) => {
    const shot = editorStore.shots.find(s => s.id === id)
    if (shot?.shotNo != null) return `\u5206\u955c#${shot.shotNo}`
    return `\u5206\u955c#${id}`
  }

  const formatByType = (type: string, id: number) => {
    if (type === 'shot') return formatShot(id)
    if (type === 'character') {
      const character = editorStore.characters?.find(c => c.id === id)
      return character?.characterName ? `\u89d2\u8272 ${character.characterName}` : `\u89d2\u8272#${id}`
    }
    if (type === 'scene') {
      const scene = editorStore.scenes?.find(s => s.id === id)
      return scene?.sceneName ? `\u573a\u666f ${scene.sceneName}` : `\u573a\u666f#${id}`
    }
    if (type === 'prop') {
      const prop = editorStore.props?.find(p => p.id === id)
      return prop?.propName ? `\u9053\u5177 ${prop.propName}` : `\u9053\u5177#${id}`
    }
    return id ? `\u7d20\u6750#${id}` : '-'
  }

  if (targetIds && targetIds.length > 0) {
    const first = targetIds[0]
    const label = targetType ? formatByType(targetType, first) : formatShot(first)
    return targetIds.length > 1 ? `${label} \u7b49${targetIds.length}\u9879` : label
  }
  if (targetType && targetId != null) {
    return formatByType(targetType, targetId)
  }

  if (record.jobType.includes('SHOT')) return '\u5206\u955c'
  if (record.jobType.includes('CHAR')) return '\u89d2\u8272'
  if (record.jobType.includes('SCENE')) return '\u573a\u666f'
  if (record.jobType.includes('PROP')) return '\u9053\u5177'
  if (record.jobType.includes('VIDEO')) return '\u5206\u955c'

  return '-'
}

const getModelLabel = (record: JobVO) => {
  const meta = parseMeta(record.metaJson)
  return meta?.model || '\u9ed8\u8ba4'
}

const getTypeLabel = (record: JobVO) => {
  const category = getJobCategory(record.jobType)
  if (category === 'video') return '\u89c6\u9891'
  if (category === 'image') return '\u56fe\u7247'
  if (record.jobType.includes('TEXT')) return '\u6587\u672c'
  return '\u5176\u4ed6'
}

const getStatusLabel = (status: JobVO['status']) => {
  switch (status) {
    case 'PENDING':
      return '\u7b49\u5f85\u4e2d'
    case 'RUNNING':
      return '\u8fdb\u884c\u4e2d'
    case 'SUCCEEDED':
      return '\u5df2\u5b8c\u6210'
    case 'FAILED':
      return '\u5931\u8d25'
    case 'CANCELED':
      return '\u5df2\u53d6\u6d88'
    default:
      return status
  }
}

const getWaitTime = (record: JobVO) => {
  const createdAt = record.createdAt ? new Date(record.createdAt).getTime() : 0
  const finishedAt = record.finishedAt ? new Date(record.finishedAt).getTime() : Date.now()
  const seconds = Math.max(0, Math.floor((finishedAt - createdAt) / 1000))
  return formatDuration(seconds)
}

const filteredRecords = computed(() => {
  let records = historyRecords.value

  if (filterType.value !== 'all') {
    records = records.filter(r => getJobCategory(r.jobType) === filterType.value)
  }

  if (sortBy.value === 'date') {
    records = [...records].sort((a, b) =>
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )
  } else if (sortBy.value === 'type') {
    records = [...records].sort((a, b) => a.jobType.localeCompare(b.jobType))
  }

  return records
})

onMounted(() => {
  loadHistory()
})
</script>

<template>
  <div class="flex flex-col h-full bg-bg-elevated">
    <div class="flex items-center gap-3 border-b border-border-default px-4 py-3">
      <button
        @click="$emit('close')"
        class="p-1.5 rounded hover:bg-bg-hover transition-colors"
        title="&#x8fd4;&#x56de;"
      >
        <svg class="w-5 h-5 text-text-secondary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
        </svg>
      </button>
      <h3 class="text-text-primary text-base font-medium">&#x5386;&#x53f2;&#x8bb0;&#x5f55;</h3>
    </div>

    <div class="flex items-center justify-between px-4 py-3 border-b border-border-default">
      <div class="flex items-center gap-2">
        <button
          @click="filterType = 'all'"
          :class="[
            'px-4 py-2 rounded text-sm font-medium transition-all',
            filterType === 'all'
              ? 'bg-bg-hover text-white'
              : 'text-text-tertiary hover:bg-bg-subtle'
          ]"
        >
          &#x5168;&#x90e8;
        </button>
        <button
          @click="filterType = 'image'"
          :class="[
            'px-4 py-2 rounded text-sm font-medium transition-all',
            filterType === 'image'
              ? 'bg-bg-hover text-white'
              : 'text-text-tertiary hover:bg-bg-subtle'
          ]"
        >
          &#x56fe;&#x7247;
        </button>
        <button
          @click="filterType = 'video'"
          :class="[
            'px-4 py-2 rounded text-sm font-medium transition-all',
            filterType === 'video'
              ? 'bg-bg-hover text-white'
              : 'text-text-tertiary hover:bg-bg-subtle'
          ]"
        >
          &#x89c6;&#x9891;
        </button>
      </div>

      <div class="flex items-center gap-2">
        <select
          v-model="sortBy"
          class="px-3 py-2 bg-bg-subtle border border-border-default rounded text-text-primary text-xs focus:outline-none focus:border-gray-900/50 cursor-pointer"
        >
          <option value="date" class="bg-bg-elevated">&#x6309;&#x65e5;&#x671f;&#x6392;&#x5e8f;</option>
          <option value="type" class="bg-bg-elevated">&#x6309;&#x7c7b;&#x578b;&#x6392;&#x5e8f;</option>
        </select>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto px-4 py-4">
      <div v-if="loading" class="text-center py-20">
        <p class="text-text-tertiary text-sm">&#x52a0;&#x8f7d;&#x4e2d;...</p>
      </div>
      <div v-else-if="filteredRecords.length === 0" class="text-center py-20">
        <svg class="w-16 h-16 mx-auto mb-4 text-text-disabled" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"></path>
        </svg>
        <p class="text-text-tertiary text-sm">&#x6682;&#x65e0;&#x5386;&#x53f2;&#x8bb0;&#x5f55;</p>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full text-left text-xs text-text-tertiary">
          <thead class="text-text-secondary">
            <tr>
              <th class="py-2 pr-4">&#x7f16;&#x53f7;</th>
              <th class="py-2 pr-4">&#x6240;&#x5c5e;&#x5206;&#x955c;/&#x7d20;&#x6750;</th>
              <th class="py-2 pr-4">&#x7c7b;&#x578b;</th>
              <th class="py-2 pr-4">&#x6a21;&#x578b;</th>
              <th class="py-2 pr-4">&#x521b;&#x5efa;&#x65f6;&#x95f4;</th>
              <th class="py-2 pr-4">&#x7b49;&#x5f85;&#x65f6;&#x95f4;</th>
              <th class="py-2">&#x72b6;&#x6001;</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="record in filteredRecords"
              :key="record.id"
              class="border-t border-border-default text-text-primary"
            >
              <td class="py-2 pr-4">{{ record.id }}</td>
              <td class="py-2 pr-4">{{ getTargetLabel(record) }}</td>
              <td class="py-2 pr-4">{{ getTypeLabel(record) }}</td>
              <td class="py-2 pr-4">{{ getModelLabel(record) }}</td>
              <td class="py-2 pr-4">{{ formatDateTime(record.createdAt) }}</td>
              <td class="py-2 pr-4">{{ getWaitTime(record) }}</td>
              <td class="py-2">{{ getStatusLabel(record.status) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="px-4 py-3 border-t border-border-default">
      <p class="text-[#FF6B9D] text-xs text-center">
        &#x672a;&#x88ab;&#x4f7f;&#x7528;&#x7684;&#x751f;&#x6210;&#x8bb0;&#x5f55;&#x4ec5;&#x4fdd;&#x7559;7&#x5929;&#xff0c;&#x8bf7;&#x53ca;&#x65f6;&#x4e0b;&#x8f7d;
      </p>
    </div>
  </div>
</template>
