// {{CODE-Cycle-Integration:
//   Task_ID: [#Phase6-Step6]
//   Timestamp: 2026-01-03T13:48:18+08:00
//   Phase: [D-Develop]
//   Context-Analysis: "Creating generation API module for batch and single generation endpoints. Maps to backend /api/projects/{pid}/generate/* endpoints."
//   Principle_Applied: "Context-First-Mandate, KISS, DRY"
// }}
// {{START_MODIFICATIONS}}

import api from './index'
import type { BatchGenerateRequest, BatchGenerateResponse } from '@/types/api'

export const generationApi = {
  /**
   * Batch generate shot images (分镜图)
   */
  async generateShotsBatch(
    projectId: number,
    request: BatchGenerateRequest,
  ): Promise<BatchGenerateResponse> {
    return api.post(`/projects/${projectId}/generate/shots`, request)
  },

  /**
   * Batch generate videos
   */
  async generateVideosBatch(
    projectId: number,
    request: BatchGenerateRequest,
  ): Promise<BatchGenerateResponse> {
    return api.post(`/projects/${projectId}/generate/videos`, request)
  },

  /**
   * Batch generate character images (角色画像)
   */
  async generateCharactersBatch(
    projectId: number,
    request: BatchGenerateRequest,
  ): Promise<BatchGenerateResponse> {
    return api.post(`/projects/${projectId}/generate/characters`, request)
  },

  /**
   * Batch generate scene images (场景画像)
   */
  async generateScenesBatch(
    projectId: number,
    request: BatchGenerateRequest,
  ): Promise<BatchGenerateResponse> {
    return api.post(`/projects/${projectId}/generate/scenes`, request)
  },

  /**
   * Generate single character image
   */
  async generateSingleCharacter(
    projectId: number,
    characterId: number,
    params: {
      aspectRatio?: '1:1' | '16:9' | '9:16' | '21:9'
      model?: string
      customPrompt?: string
      referenceImageUrl?: string
    },
  ): Promise<BatchGenerateResponse> {
    const queryParams = new URLSearchParams()
    if (params.aspectRatio) queryParams.append('aspectRatio', params.aspectRatio)
    if (params.model) queryParams.append('model', params.model)
    if (params.customPrompt) queryParams.append('customPrompt', params.customPrompt)
    if (params.referenceImageUrl) queryParams.append('referenceImageUrl', params.referenceImageUrl)
    const queryString = queryParams.toString()
    return api.post(`/projects/${projectId}/generate/character/${characterId}${queryString ? '?' + queryString : ''}`)
  },

  /**
   * Generate single scene image
   */
  async generateSingleScene(
    projectId: number,
    sceneId: number,
    params: {
      aspectRatio?: '1:1' | '16:9' | '9:16' | '21:9'
      model?: string
      customPrompt?: string
      referenceImageUrl?: string
    },
  ): Promise<BatchGenerateResponse> {
    const queryParams = new URLSearchParams()
    if (params.aspectRatio) queryParams.append('aspectRatio', params.aspectRatio)
    if (params.model) queryParams.append('model', params.model)
    if (params.customPrompt) queryParams.append('customPrompt', params.customPrompt)
    if (params.referenceImageUrl) queryParams.append('referenceImageUrl', params.referenceImageUrl)
    const queryString = queryParams.toString()
    return api.post(`/projects/${projectId}/generate/scene/${sceneId}${queryString ? '?' + queryString : ''}`)
  },

  /**
   * Generate single shot image
   */
  async generateSingleShot(
    projectId: number,
    shotId: number,
    params: {
      aspectRatio?: '1:1' | '16:9' | '9:16' | '21:9'
      model?: string
      customPrompt?: string
      referenceImageUrl?: string // 添加参考图参数
    },
  ): Promise<BatchGenerateResponse> {
    // 构建查询参数
    const queryParams = new URLSearchParams()
    if (params.aspectRatio) queryParams.append('aspectRatio', params.aspectRatio)
    if (params.model) queryParams.append('model', params.model)
    if (params.customPrompt) queryParams.append('customPrompt', params.customPrompt)
    if (params.referenceImageUrl) queryParams.append('referenceImageUrl', params.referenceImageUrl)
    const queryString = queryParams.toString()
    return api.post(`/projects/${projectId}/generate/shot/${shotId}${queryString ? '?' + queryString : ''}`)
  },

  /**
   * Generate single video
   */
  async generateSingleVideo(
    projectId: number,
    shotId: number,
    params: {
      aspectRatio?: '1:1' | '16:9' | '9:16' | '21:9'
      model?: string
    },
  ): Promise<BatchGenerateResponse> {
    return api.post(`/projects/${projectId}/generate/video/${shotId}`, params)
  },

  /**
   * Generate single prop image
   */
  async generateSingleProp(
    projectId: number,
    propId: number,
    params: {
      aspectRatio?: '1:1' | '16:9' | '9:16' | '21:9'
      model?: string
      customPrompt?: string
      referenceImageUrl?: string
    },
  ): Promise<BatchGenerateResponse> {
    const queryParams = new URLSearchParams()
    if (params.aspectRatio) queryParams.append('aspectRatio', params.aspectRatio)
    if (params.model) queryParams.append('model', params.model)
    if (params.customPrompt) queryParams.append('customPrompt', params.customPrompt)
    if (params.referenceImageUrl) queryParams.append('referenceImageUrl', params.referenceImageUrl)
    const queryString = queryParams.toString()
    return api.post(`/projects/${projectId}/generate/prop/${propId}${queryString ? '?' + queryString : ''}`)
  },
}

// {{END_MODIFICATIONS}}
