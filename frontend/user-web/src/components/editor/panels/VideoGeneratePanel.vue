<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useEditorStore } from '@/stores/editor'
import api from '@/api'
import { uploadApi } from '@/api/apis'
import axios from 'axios'
import { jobApi, pollJobStatus } from '@/api/job'

// Props定义
const props = defineProps<{
  shotId: number
  shotNo: number
}>()

// Emits定义
const emit = defineEmits<{
  close: []
}>()

const editorStore = useEditorStore()

// 当前分镜数据
const currentShot = computed(() => {
  return editorStore.shots.find(s => s.id === props.shotId)
})

// 用户自定义提示词（暂未开放）
const scriptDescription = ref('')

// 视频画幅 / 时长选择
const aspectRatio = ref<'16:9' | '9:16'>('16:9')
const aspectRatioOptions = [
  { label: '16:9（横版）', value: '16:9' },
  { label: '9:16（竖版）', value: '9:16' },
]

const duration = ref(10)
const durationOptions = [10, 15]

const mapAspectRatioToSize = (value: '16:9' | '9:16') => {
  return value === '9:16' ? '720x1280' : '1280x720'
}

// 生成状态
const isGenerating = ref(false)

// 轮询进度信息
const pollingInfo = ref<{
  isPolling: boolean
  jobId: number | null
  status: string
  progress: number
  attempts: number
}>({
  isPolling: false,
  jobId: null,
  status: '',
  progress: 0,
  attempts: 0
})
const pollingStorageKey = computed(() => `video_polling_shot_${props.shotId}`)
const savePollingInfo = () => {
  try {
    localStorage.setItem(pollingStorageKey.value, JSON.stringify(pollingInfo.value))
  } catch (error) {
    console.error('[VideoGeneratePanel] 保存轮询状态失败:', error)
  }
}
const loadPollingInfo = () => {
  try {
    const stored = localStorage.getItem(pollingStorageKey.value)
    if (!stored) return null
    const parsed = JSON.parse(stored) as typeof pollingInfo.value
    if (typeof parsed.attempts !== 'number') {
      parsed.attempts = 0
    }
    return parsed
  } catch (error) {
    console.error('[VideoGeneratePanel] 读取轮询状态失败:', error)
    return null
  }
}
const clearPollingInfo = () => {
  try {
    localStorage.removeItem(pollingStorageKey.value)
  } catch (error) {
    console.error('[VideoGeneratePanel] 清理轮询状态失败:', error)
  }
}

// 视频参考图URL（拼接后的图片）
const videoReferenceUrl = ref<string>('')
const referenceImageFile = ref<File | null>(null)
const isMergingReference = ref(false)

// 当前显示的缩略图URL（待生成区域）
const referenceStorageKey = computed(() => `video_reference_shot_${props.shotId}`)
const saveVideoReference = (url: string) => {
  if (!url) return
  try {
    localStorage.setItem(referenceStorageKey.value, url)
  } catch (error) {
    console.error('[VideoGeneratePanel] 保存参考图失败:', error)
  }
}
const loadVideoReference = () => {
  try {
    const stored = localStorage.getItem(referenceStorageKey.value)
    if (stored) {
      videoReferenceUrl.value = stored
    }
  } catch (error) {
    console.error('[VideoGeneratePanel] 加载参考图失败:', error)
  }
}

const selectedHistoryId = ref<number | null>(null)
const selectedHistoryItem = computed(() => {
  if (selectedHistoryId.value !== null) {
    const matched = generationHistory.value.find(item => item.id === selectedHistoryId.value)
    if (matched) return matched
  }
  return generationHistory.value[0] || null
})
const previewImageUrl = computed(() => selectedHistoryItem.value?.thumbnailUrl || '')
const previewVideoUrl = computed(() => selectedHistoryItem.value?.videoUrl || '')
const generatedVideoThumbnail = ref('')
const showImageOverlay = ref(false)
const overlayImageUrl = ref<string>('')
const openImageOverlay = (url: string) => {
  if (!url) return
  overlayImageUrl.value = url
  showImageOverlay.value = true
}
const closeImageOverlay = () => {
  showImageOverlay.value = false
  overlayImageUrl.value = ''
}

// 备选素材（从分镜图中选择）
const availableMaterials = ref<Array<{
  id: number
  imageUrl: string
  isSelected: boolean
}>>([])  // 初始为空，从分镜图历史记录加载

// 历史记录
interface VideoHistoryItem {
  id: number
  videoUrl: string
  thumbnailUrl: string
  timestamp: string
  prompt: string
  userInput: string
  expiresAt: number
}

const generationHistory = ref<VideoHistoryItem[]>([])
const loadingHistory = ref(false)

const handleReferenceImageUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  try {
    referenceImageFile.value = file
    const result = await uploadApi.upload(file, 'image')
    videoReferenceUrl.value = result.url
    saveVideoReference(result.url)
    window.$message?.success('参考图上传成功')
  } catch (error: any) {
    console.error('[VideoGeneratePanel] 参考图上传失败:', error)
    window.$message?.error('参考图上传失败')
  } finally {
    target.value = ''
  }
}

const triggerReferenceImageInput = () => {
  const input = document.getElementById('video-reference-image-input') as HTMLInputElement
  input?.click()
}

const handleReferencePreview = () => {
  if (videoReferenceUrl.value) {
    openImageOverlay(videoReferenceUrl.value)
    return
  }
  window.$message?.info('请先上传或拼接参考图')
}

const mergeReferenceImages = async () => {
  if (isMergingReference.value) return

  const imageItems: Array<{ label: string; imageUrl: string }> = []
  const added = new Set<string>()
  const addItem = (label: string, imageUrl?: string) => {
    if (!imageUrl || imageUrl.includes('via.placeholder.com')) return
    if (added.has(imageUrl)) return
    added.add(imageUrl)
    imageItems.push({ label, imageUrl })
  }

  const shotImageUrl = currentShot.value?.shotImage?.currentUrl
    || currentShot.value?.shotImage?.thumbnailUrl
    || videoReferenceUrl.value

  if (shotImageUrl) {
    addItem('镜头参考', shotImageUrl)
  }

  if (currentShot.value?.scene?.thumbnailUrl) {
    addItem(
      currentShot.value.scene.sceneName
        ? `${currentShot.value.scene.sceneName} 场景参考`
        : '场景参考',
      currentShot.value.scene.thumbnailUrl
    )
  }

  if (currentShot.value?.characters?.length) {
    currentShot.value.characters.forEach((char) => {
      addItem(`${char.characterName || '角色'} 人物参考`, char.thumbnailUrl)
    })
  } else if (editorStore.characters?.length) {
    editorStore.characters.forEach((char) => {
      addItem(`${char.characterName || '角色'} 人物参考`, char.thumbnailUrl)
    })
  }

  if (currentShot.value?.props?.length) {
    currentShot.value.props.forEach((prop) => {
      addItem(`${prop.propName || '道具'} 道具参考`, prop.thumbnailUrl)
    })
  } else if (editorStore.props?.length) {
    editorStore.props.forEach((prop) => {
      addItem(`${prop.propName || '道具'} 道具参考`, prop.thumbnailUrl)
    })
  }

  if (imageItems.length === 0) {
    window.$message?.warning('没有可拼接的参考图')
    return
  }

  if (imageItems.length === 1) {
    videoReferenceUrl.value = imageItems[0].imageUrl
    saveVideoReference(imageItems[0].imageUrl)
    window.$message?.success('已设置首帧参考图')
    return
  }

  isMergingReference.value = true
  try {
    const response = await api.post('/utils/images/merge', { imageItems })
    if (response?.mergedImageUrl) {
      videoReferenceUrl.value = response.mergedImageUrl
      saveVideoReference(response.mergedImageUrl)
      window.$message?.success('首帧拼接完成')
    } else {
      window.$message?.error('拼接失败')
    }
  } catch (error) {
    console.error('[VideoGeneratePanel] 拼接参考图失败:', error)
    window.$message?.error('拼接失败')
  } finally {
    isMergingReference.value = false
  }
}
// 收集当前分镜的所有资源（不拼接，直接传递）
const collectAssetResources = () => {
  if (!currentShot.value) return null
  
  console.log('[VideoGeneratePanel] ========== 开始收集资源 ==========')
  
  const resources = {
    script: currentShot.value.scriptText || '',
    shotImage: null as { thumbnailUrl: string } | null,
    scene: null as { id: number; name: string; thumbnailUrl: string } | null,
    characters: [] as Array<{ id: number; name: string; thumbnailUrl: string }>,
    props: [] as Array<{ id: number; name: string; thumbnailUrl: string }>
  }
  
  // 收集剧本
  if (resources.script) {
    console.log('[VideoGeneratePanel] ✅ 剧本已收集')
  }
  
  // 收集分镜图
  if (currentShot.value.shotImage?.thumbnailUrl) {
    resources.shotImage = {
      thumbnailUrl: currentShot.value.shotImage.thumbnailUrl
    }
    console.log('[VideoGeneratePanel] ✅ 分镜图已收集')
  }
  
  // 收集场景
  if (currentShot.value.scene?.thumbnailUrl) {
    resources.scene = {
      id: currentShot.value.scene.id,
      name: currentShot.value.scene.sceneName || '场景',
      thumbnailUrl: currentShot.value.scene.thumbnailUrl
    }
    console.log('[VideoGeneratePanel] ✅ 场景已收集:', resources.scene.name)
  }
  
  // 收集角色
  if (currentShot.value.characters && currentShot.value.characters.length > 0) {
    currentShot.value.characters.forEach((char) => {
      if (char.thumbnailUrl) {
        resources.characters.push({
          id: char.id,
          name: char.characterName || '角色',
          thumbnailUrl: char.thumbnailUrl
        })
        console.log('[VideoGeneratePanel] ✅ 角色已收集:', char.characterName)
      }
    })
  }
  
  // 收集道具
  if (currentShot.value.props && currentShot.value.props.length > 0) {
    currentShot.value.props.forEach((prop) => {
      if (prop.thumbnailUrl) {
        resources.props.push({
          id: prop.id,
          name: prop.propName || '道具',
          thumbnailUrl: prop.thumbnailUrl
        })
        console.log('[VideoGeneratePanel] ✅ 道具已收集:', prop.propName)
      }
    })
  }
  
  console.log('[VideoGeneratePanel] 资源收集完成:', {
    hasScript: !!resources.script,
    hasShotImage: !!resources.shotImage,
    hasScene: !!resources.scene,
    characterCount: resources.characters.length,
    propCount: resources.props.length
  })
  
  return resources
}

// 保存历史记录到 localStorage
const saveHistory = () => {
  const key = `video_history_shot_${props.shotId}`
  localStorage.setItem(key, JSON.stringify(generationHistory.value))
  console.log('[VideoGeneratePanel] 历史记录已保存')
}

const getFallbackThumbnailUrl = () => {
  if (currentShot.value?.shotImage?.currentUrl) return currentShot.value.shotImage.currentUrl
  if (videoReferenceUrl.value) return videoReferenceUrl.value
  if (currentShot.value?.scene?.thumbnailUrl) return currentShot.value.scene.thumbnailUrl
  if (currentShot.value?.characters?.length) {
    const firstChar = currentShot.value.characters.find(char => char.thumbnailUrl)
    if (firstChar?.thumbnailUrl) return firstChar.thumbnailUrl
  }
  if (currentShot.value?.props?.length) {
    const firstProp = currentShot.value.props.find(prop => prop.thumbnailUrl)
    if (firstProp?.thumbnailUrl) return firstProp.thumbnailUrl
  }
  return 'https://via.placeholder.com/400x225'
}

const captureVideoThumbnail = (videoUrl: string): Promise<string | null> => {
  return new Promise((resolve) => {
    if (!videoUrl) {
      resolve(null)
      return
    }

    const video = document.createElement('video')
    video.crossOrigin = 'anonymous'
    video.preload = 'metadata'
    video.muted = true
    video.playsInline = true

    const cleanup = () => {
      video.src = ''
      video.load()
    }

    const handleError = () => {
      cleanup()
      resolve(null)
    }

    const capture = () => {
      if (!video.videoWidth || !video.videoHeight) {
        handleError()
        return
      }
      const canvas = document.createElement('canvas')
      canvas.width = video.videoWidth
      canvas.height = video.videoHeight
      const ctx = canvas.getContext('2d')
      if (!ctx) {
        handleError()
        return
      }
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
      try {
        const dataUrl = canvas.toDataURL('image/jpeg', 0.9)
        cleanup()
        resolve(dataUrl)
      } catch (error) {
        handleError()
      }
    }

    const handleLoaded = () => {
      if (video.duration && video.duration > 0.1) {
        try {
          video.currentTime = 0.1
        } catch (error) {
          capture()
        }
      } else {
        capture()
      }
    }

    video.addEventListener('loadeddata', handleLoaded, { once: true })
    video.addEventListener('seeked', capture, { once: true })
    video.addEventListener('error', handleError, { once: true })
    video.src = videoUrl
  })
}

const waitForResultUrl = async (jobId: number, retries: number = 3, interval: number = 1500) => {
  for (let attempt = 0; attempt < retries; attempt += 1) {
    try {
      const job = await jobApi.getJobStatus(jobId)
      if (job.resultUrl) return job.resultUrl
    } catch (error) {
      console.error('[VideoGeneratePanel] 获取结果URL失败:', error)
    }
    if (attempt < retries - 1) {
      await new Promise(resolve => setTimeout(resolve, interval))
    }
  }
  return null
}

const updateHistoryVideoUrl = async (jobId: number, resultUrl: string) => {
  const index = generationHistory.value.findIndex(item => item.id === jobId)
  if (index === -1) return
  generationHistory.value[index].videoUrl = resultUrl
  const capturedThumbnail = await captureVideoThumbnail(resultUrl)
  generationHistory.value[index].thumbnailUrl = capturedThumbnail || getFallbackThumbnailUrl()
  saveHistory()
}

const maxPollingAttempts = 120
const startPollingJobStatus = async (jobId: number, initialAttempts: number = 0) => {
  // 开始轮询
  pollingInfo.value = {
    isPolling: true,
    jobId,
    status: 'RUNNING',
    progress: 0,
    attempts: initialAttempts
  }
  savePollingInfo()
  
  try {
    const job = await pollJobStatus(jobId, (progressJob) => {
      // 更新轮询进度
      pollingInfo.value.status = progressJob.status || 'RUNNING'
      pollingInfo.value.attempts += 1
      if (progressJob.progress && progressJob.progress > 0) {
        pollingInfo.value.progress = progressJob.progress
      } else {
        const fallbackProgress = Math.min(
          95,
          Math.round((pollingInfo.value.attempts / maxPollingAttempts) * 95)
        )
        pollingInfo.value.progress = Math.max(pollingInfo.value.progress, fallbackProgress)
      }
      savePollingInfo()
      console.log('[VideoGeneratePanel] 轮询进度:', progressJob.status, progressJob.progress + '%')
    })
    
    if (job.resultUrl) {
      await updateHistoryVideoUrl(jobId, job.resultUrl)
      window.$message?.success('视频生成完成！')
    } else {
      const resultUrl = await waitForResultUrl(jobId)
      if (resultUrl) {
        await updateHistoryVideoUrl(jobId, resultUrl)
        window.$message?.success('视频生成完成！')
      } else {
        window.$message?.warning('Video ready but url missing')
      }
    }
  } catch (error) {
    console.error('[VideoGeneratePanel] poll job failed:', error)
    window.$message?.error('Video generation failed')
  } finally {
    // 结束轮询
    pollingInfo.value.isPolling = false
    clearPollingInfo()
  }
}

const hydrateHistoryVideoUrls = async () => {
  const pendingItems = generationHistory.value.filter(item => !item.videoUrl)
  if (pendingItems.length === 0) return
  for (const item of pendingItems) {
    try {
      const job = await jobApi.getJobStatus(item.id)
      if (job.resultUrl) {
        await updateHistoryVideoUrl(item.id, job.resultUrl)
      }
    } catch (error) {
      console.error('[VideoGeneratePanel] hydrate job failed:', error)
    }
  }
}

// 加载历史记录
const hydrateHistoryThumbnails = async () => {
  const items = generationHistory.value.filter(item => item.videoUrl && item.thumbnailUrl.includes('via.placeholder.com'))
  for (const item of items) {
    try {
      const capturedThumbnail = await captureVideoThumbnail(item.videoUrl)
      if (capturedThumbnail) {
        item.thumbnailUrl = capturedThumbnail
        saveHistory()
      }
    } catch (error) {
      console.error('[VideoGeneratePanel] hydrate thumbnail failed:', error)
    }
  }
}

const loadHistory = () => {
  const key = `video_history_shot_${props.shotId}`
  const stored = localStorage.getItem(key)
  if (stored) {
    try {
      const history = JSON.parse(stored) as VideoHistoryItem[]
      // 过滤掉过期的记录（7天）
      generationHistory.value = history.filter((item) => {
        return Date.now() < item.expiresAt
      })
      console.log('[VideoGeneratePanel] 加载历史记录:', generationHistory.value.length, '条')
      // 保存过滤后的结果
      if (generationHistory.value.length !== history.length) {
        saveHistory()
      }
    } catch (error) {
      console.error('[VideoGeneratePanel] 加载历史记录失败:', error)
      generationHistory.value = []
    }
  }
  hydrateHistoryVideoUrls()
  hydrateHistoryThumbnails()
}


// AI生成视频
const handleAIGenerate = async () => {
  if (!editorStore.projectId) {
    window.$message?.error('项目ID不存在')
    return
  }
  
  isGenerating.value = true
  
  try {
    // 1. 收集当前分镜的所有资源（剧本、分镜图、场景、角色、道具）
    const resources = collectAssetResources()
    
    if (!resources) {
      window.$message?.error('无法获取分镜资源')
      return
    }
    
    // 2. 构建完整的提示词：内嵌规则 + 分镜剧本
    const fixedTemplate = '根据参考图的设定，使用参考图中的角色、场景、道具，运用合理的构建分镜，合理的动作，合理的运镜，合理的环境渲染，发散你的想象力，生成保持风格一致性的2D动漫视频，要求不要字幕和BGM，没有台词时禁止说话，线条细致，人物画风保持与参考图一致，清晰不模糊，颜色鲜艳，光影效果，超清画质，电影级镜头（cinematicdvnamiccamera）,音质清晰无杂质，第一个镜头0.3秒空境，请忠实原文，不增加原文没有的内容，不减少原文包含的信息，分镜要求如下：'
    
    // 构建prompt：内嵌规则 + 分镜剧本
    let customPrompt = fixedTemplate
    if (resources.script) {
      customPrompt += resources.script
    } else {
      customPrompt += '（无剧本内容）'
    }
    
    console.log('[VideoGeneratePanel] 生成参数:', {
      shotId: props.shotId,
      customPrompt,
      size: mapAspectRatioToSize(aspectRatio.value),
      duration: duration.value,
      aspectRatio: aspectRatio.value,
      resources
    })
    
    // 3. 调用后端视频生成接口
    const response = await api.post(
      `/projects/${editorStore.projectId}/generate/shot-video/${props.shotId}`,
      {
        prompt: customPrompt,
        aspectRatio: aspectRatio.value,
        size: mapAspectRatioToSize(aspectRatio.value),
        duration: duration.value,
        referenceImageUrl: videoReferenceUrl.value || undefined,
        shotImage: resources.shotImage,
        scene: resources.scene,
        characters: resources.characters,
        props: resources.props
      }
    )
    
    console.log('[VideoGeneratePanel] 生成响应:', response)
    
    // 4. 获取返回的jobId
    const jobId = response.jobId
    
    // 使用第一个可用的图片作为缩略图（优先使用分镜图）
    let mockThumbnailUrl = 'https://via.placeholder.com/400x225'
    if (resources.shotImage?.thumbnailUrl) {
      mockThumbnailUrl = resources.shotImage.thumbnailUrl
    } else if (resources.scene?.thumbnailUrl) {
      mockThumbnailUrl = resources.scene.thumbnailUrl
    } else if (resources.characters.length > 0) {
      mockThumbnailUrl = resources.characters[0].thumbnailUrl
    } else if (resources.props.length > 0) {
      mockThumbnailUrl = resources.props[0].thumbnailUrl
    }
    
    // 5. 保存到历史记录（使用jobId）
    const newHistoryItem: VideoHistoryItem = {
      id: jobId, // 使用后端返回的jobId
      videoUrl: '', // 视频URL将在任务完成后更新
      thumbnailUrl: mockThumbnailUrl,
      timestamp: new Date().toLocaleString('zh-CN'),
      prompt: customPrompt,
      userInput: '',
      expiresAt: Date.now() + 7 * 24 * 60 * 60 * 1000 // 7天后过期
    }
    
    generationHistory.value.unshift(newHistoryItem)
    selectedHistoryId.value = jobId
    
    // 仅在未上传参考图时，用素材图做展示
    if (!videoReferenceUrl.value) {
      videoReferenceUrl.value = mockThumbnailUrl
      saveVideoReference(mockThumbnailUrl)
    }
    
    // 保存到 localStorage
    saveHistory()
    
    window.$message?.success(`视频生成任务已提交，任务ID: ${jobId}。请等待生成完成...`)
    
    startPollingJobStatus(jobId)
    
  } catch (error: any) {
    console.error('[VideoGeneratePanel] 生成失败:', error)
    window.$message?.error(error.response?.data?.message || error.message || '生成失败，请重试')
  } finally {
    isGenerating.value = false
  }
}


// 选择备选素材（切换待生成缩略图）
const handleSelectMaterial = (material: any) => {
  generatedVideoThumbnail.value = material.imageUrl
  console.log('[VideoGeneratePanel] 选择备选素材:', material.id)
}

// 点击历史记录（切换待生成缩略图）
const handleHistoryClick = (item: VideoHistoryItem) => {
  selectedHistoryId.value = item.id
  generatedVideoThumbnail.value = item.thumbnailUrl
  // 回填用户输入
  scriptDescription.value = item.userInput
  console.log('[VideoGeneratePanel] 选择历史记录:', item.id)
}

// 删除历史记录
const handleDeleteHistory = (id: number) => {
  const index = generationHistory.value.findIndex(item => item.id === id)
  if (index !== -1) {
    generationHistory.value.splice(index, 1)
    saveHistory()
    window.$message?.success('已删除历史记录')
  }
}

const handleDeletePreviewImage = () => {
  videoReferenceUrl.value = ''
  generatedVideoThumbnail.value = ''
  try {
    localStorage.removeItem(referenceStorageKey.value)
  } catch (error) {
    console.error('[VideoGeneratePanel] 删除参考图失败:', error)
  }
  window.$message?.success('已删除图片')
}

const handleDownloadPreviewImage = () => {
  if (!previewImageUrl.value) return
  try {
    const link = document.createElement('a')
    link.href = previewImageUrl.value
    link.download = `video_preview_${props.shotNo}_${Date.now()}.jpg`
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.$message?.success('开始下载')
  } catch (error) {
    console.error('下载失败:', error)
    window.$message?.error('下载失败')
  }
}

const handleCopyPreviewImage = async () => {
  if (!previewImageUrl.value) return
  try {
    window.$message?.info('正在复制...')
    const response = await fetch('/api/asset/download-from-url', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ url: previewImageUrl.value })
    })

    if (!response.ok) {
      throw new Error('下载图片失败')
    }

    const blob = await response.blob()
    await navigator.clipboard.write([
      new ClipboardItem({
        [blob.type]: blob
      })
    ])

    window.$message?.success('已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    window.$message?.error('复制失败，请尝试右键保存')
  }
}

// 下载视频
const handleDownloadVideo = async (videoUrl: string, fileName: string = '视频') => {
  if (!videoUrl) {
    window.$message?.warning('视频地址为空')
    return
  }
  try {
    const token = localStorage.getItem('token')
    const response = await axios.post(
      '/api/assets/download-from-url',
      { url: videoUrl },
      {
        responseType: 'blob',
        headers: token ? { Authorization: `Bearer ${token}` } : undefined,
      }
    )
    const blob = response.data
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = `${fileName}_${Date.now()}.mp4`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)
    window.$message?.success('下载成功')
  } catch (error) {
    console.error('[VideoGeneratePanel] 下载失败:', error)
    window.$message?.error('下载失败')
  }
}

// 应用视频到分镜
const handleApplyVideo = async (videoUrl: string) => {
  // TODO: 实现应用视频逻辑
  console.log('[VideoGeneratePanel] 应用视频:', videoUrl)
  window.$message?.info('应用视频功能开发中')
}

// 组件挂载时加载数据
onMounted(() => {
  console.log('[VideoGeneratePanel] 组件挂载, shotId:', props.shotId)
  loadHistory()
  loadVideoReference()
  const storedPolling = loadPollingInfo()
  if (storedPolling?.isPolling && storedPolling.jobId) {
    pollingInfo.value = storedPolling
    jobApi.getJobStatus(storedPolling.jobId)
      .then((job) => {
        pollingInfo.value.status = job.status || pollingInfo.value.status
        pollingInfo.value.progress = job.progress || pollingInfo.value.progress
        savePollingInfo()
        if (job.status === 'SUCCEEDED' || job.status === 'COMPLETED') {
          if (job.resultUrl) {
            updateHistoryVideoUrl(storedPolling.jobId, job.resultUrl)
          } else {
            waitForResultUrl(storedPolling.jobId)
              .then((resultUrl) => {
                if (resultUrl) {
                  updateHistoryVideoUrl(storedPolling.jobId, resultUrl)
                }
              })
              .catch(() => undefined)
          }
          clearPollingInfo()
          pollingInfo.value.isPolling = false
        } else if (job.status === 'FAILED') {
          clearPollingInfo()
          pollingInfo.value.isPolling = false
        } else {
          startPollingJobStatus(storedPolling.jobId, storedPolling.attempts || 0)
        }
      })
      .catch((error) => {
        console.error('[VideoGeneratePanel] 恢复轮询失败:', error)
      })
  }
  ;(window as any).__shot = currentShot.value
  // TODO: 加载备选素材（从分镜图历史记录）
})

watch(
  currentShot,
  (value) => {
    ;(window as any).__shot = value
  },
  { immediate: true }
)
</script>

<template>
  <div class="flex flex-col h-full bg-bg-elevated">
    <!-- 顶部导航 -->
    <div class="flex items-center justify-between px-4 py-3 border-b border-border-subtle">
      <button
        @click="$emit('close')"
        class="flex items-center gap-2 text-text-secondary hover:text-text-primary transition-colors"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
        </svg>
        <span class="text-sm font-medium">返回</span>
      </button>
      <h3 class="text-text-primary text-base font-medium">分镜 #{{ shotNo }}</h3>
    </div>

    <!-- 主内容区 -->
    <div class="flex-1 overflow-y-auto px-6 py-6">
      <!-- 视频参考图区域 -->
      <div class="mb-6">
      <div class="flex items-center justify-between mb-3">
        <h4 class="text-text-primary text-base font-medium">视频参考图</h4>
        <div class="flex items-center gap-2">
          <button
            @click="triggerReferenceImageInput"
            class="px-3 py-1.5 bg-bg-subtle rounded text-text-secondary text-xs hover:bg-bg-hover transition-colors"
          >
            上传参考图
          </button>
          <button
            @click="mergeReferenceImages"
            :disabled="isMergingReference"
            class="px-3 py-1.5 bg-bg-subtle rounded text-text-secondary text-xs hover:bg-bg-hover transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ isMergingReference ? '拼接中...' : '拼接首帧' }}
          </button>
        </div>
      </div>

        <!-- 大虚线框预览区 -->
        <div
          class="group relative w-full aspect-video rounded border-2 border-dashed border-border-default bg-bg-subtle flex items-center justify-center overflow-hidden cursor-pointer hover:bg-bg-hover transition-colors"
          @click="handleReferencePreview"
        >
          <template v-if="videoReferenceUrl">
            <img :src="videoReferenceUrl" alt="视频参考图" class="w-full h-full object-cover rounded">
            <div class="absolute top-3 right-3 flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
              <button
                @click.stop="handleDownloadPreviewImage"
                class="p-2 rounded-lg bg-gray-800 hover:bg-gray-700 transition-colors"
                title="Download"
              >
                <svg class="w-5 h-5 text-text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                </svg>
              </button>
              <button
                @click.stop="handleCopyPreviewImage"
                class="p-2 rounded-lg bg-gray-800 hover:bg-gray-700 transition-colors"
                title="Copy"
              >
                <svg class="w-5 h-5 text-text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
              </button>
              <button
                @click.stop="handleDeletePreviewImage"
                class="p-2 rounded-lg bg-gray-800 hover:bg-gray-700 transition-colors"
                title="Delete"
              >
                <svg class="w-5 h-5 text-text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                </svg>
              </button>
            </div>
          </template>
          <template v-else>
            <p class="text-text-tertiary text-sm">参考图</p>
          </template>
        </div>
        <input
          id="video-reference-image-input"
          type="file"
          accept="image/*"
          class="hidden"
          @change="handleReferenceImageUpload"
        >
      </div>

      <!-- 用户自定义内容输入框（暂不展示） -->

      <!-- 底部控制栏 -->
      <div class="flex items-center justify-between mb-6">
        <!-- 格式 / 时长选择 -->
        <div class="flex items-center gap-3">
          <select
            v-model="aspectRatio"
            class="px-4 py-2.5 bg-bg-hover border border-border-default rounded text-text-primary text-sm focus:outline-none focus:border-gray-900/50 cursor-pointer"
          >
            <option v-for="option in aspectRatioOptions" :key="option.value" :value="option.value" class="bg-bg-elevated">
              {{ option.label }}
            </option>
          </select>
          <select
            v-model="duration"
            class="px-4 py-2.5 bg-bg-hover border border-border-default rounded text-text-primary text-sm focus:outline-none focus:border-gray-900/50 cursor-pointer"
          >
            <option v-for="option in durationOptions" :key="option" :value="option" class="bg-bg-elevated">
              {{ option }} 秒
            </option>
          </select>
        </div>

        <!-- AI生成按钮 -->
        <button
          @click="handleAIGenerate"
          :disabled="isGenerating || pollingInfo.isPolling"
          class="px-6 py-2.5 bg-bg-hover border border-border-default rounded text-text-primary font-medium text-sm hover:bg-bg-subtle transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <template v-if="pollingInfo.isPolling">
            <span>生成中</span>
          </template>
          <template v-else-if="isGenerating">
            <span>生成中</span>
          </template>
          <template v-else>
            <span>AI生成</span>
          </template>
        </button>
      </div>

      <!-- 进度条 -->
      <div class="mb-6 border-t border-border-default pt-6">
        <h4 class="text-text-primary text-sm font-medium mb-3">生成进度</h4>
        <div class="w-full h-2 rounded bg-bg-subtle overflow-hidden">
          <div
            class="h-full bg-[#8B5CF6] transition-all"
            :style="{ width: `${pollingInfo.progress || 0}%` }"
          ></div>
        </div>
        <div class="mt-2 text-text-tertiary text-xs">
          {{ pollingInfo.isPolling ? `处理中 ${pollingInfo.progress || 0}%` : '待生成' }}
        </div>
      </div>

      <!-- 生成结果与历史 -->
      <div class="border-t border-border-default pt-6 mb-6">
        <div class="flex items-center justify-between mb-4">
          <h4 class="text-text-primary text-sm font-medium">生成结果与历史</h4>
        </div>
        <div class="relative w-full aspect-video rounded border-2 border-dashed border-border-default bg-bg-subtle flex items-center justify-center overflow-hidden mb-6">
          <template v-if="previewImageUrl">
            <img
              :src="previewImageUrl"
              alt="视频首帧参考"
              class="w-full h-full object-cover rounded"
              @click.stop="openImageOverlay(previewImageUrl)"
            >
          </template>
          <template v-else>
            <div class="text-center">
              <svg class="w-16 h-16 mx-auto text-text-disabled mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"></path>
              </svg>
              <p class="text-text-tertiary text-sm">暂无结果</p>
            </div>
          </template>
        </div>
        <div v-if="generationHistory.length === 0" class="text-center py-8">
          <p class="text-text-tertiary text-sm mb-2">暂无生成记录</p>
          <p class="text-[#FF6B9D] text-xs">
            未被使用的生成记录仅保疙7天，请及时下载文件
          </p>
        </div>

        <div v-else>
          <div class="grid grid-cols-4 gap-2 mb-3">
            <div
              v-for="item in generationHistory.slice(0, 8)"
              :key="item.id"
              @click="handleHistoryClick(item)"
              class="relative aspect-square rounded overflow-hidden cursor-pointer hover:ring-2 hover:ring-[#00FFCC]/50 transition-all group"
            >
              <img :src="item.thumbnailUrl" alt="历史记录" class="w-full h-full object-cover">

              <!-- 播放按钮覆盖 -->
              <div class="absolute inset-0 bg-bg-subtle flex items-center justify-center">
                <svg class="w-10 h-10 text-text-secondary" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M8 5v14l11-7z"></path>
                </svg>
              </div>
              
              <!-- 悬浮按钮 -->
              <div class="absolute top-1 right-1 opacity-0 group-hover:opacity-100 transition-opacity flex gap-1">
                <button 
                  @click.stop="handleDownloadVideo(item.videoUrl, `分镜${props.shotNo}_视频`)"
                  class="p-1.5 bg-gray-800 rounded hover:bg-gray-700 transition-colors"
                  title="下载"
                >
                  <svg class="w-3.5 h-3.5 text-text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
                  </svg>
                </button>
                <button 
                  @click.stop="handleDeleteHistory(item.id)"
                  class="p-1.5 bg-gray-800 rounded hover:bg-gray-700 transition-colors"
                  title="删除"
                >
                  <svg class="w-3.5 h-3.5 text-text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <p class="text-[#FF6B9D] text-xs text-center">
            未被使用的生成记录仅保疙7天，请及时下载文件
          </p>
        </div>
      </div>
    </div>
    <div
      v-if="showImageOverlay"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/70"
      @click="closeImageOverlay"
    >
      <div class="relative max-w-[90vw] max-h-[90vh]">
        <img
          :src="overlayImageUrl"
          alt="视频大图预览"
          class="max-w-[90vw] max-h-[90vh] object-contain rounded"
          @click.stop
        >
        <button
          class="absolute -top-3 -right-3 w-8 h-8 rounded-full bg-gray-900 text-text-primary flex items-center justify-center hover:bg-gray-700 transition-colors"
          title="关闭"
          @click="closeImageOverlay"
        >
          ✕
        </button>
      </div>
    </div>
  </div>
</template>
