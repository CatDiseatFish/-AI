// AI工具箱模型配置
// 根据后端文档 V1_DEVELOPMENT_GUIDE.md 配置

export interface ModelOption {
  code: string
  name: string
  description?: string
}

export interface TextModelOption extends ModelOption {
  costPerToken: number
}

export interface ImageModelOption extends ModelOption {
  costPerItem: number
  aspectRatios: string[]
}

export interface VideoModelOption extends ModelOption {
  costPerSecond: number
  aspectRatios: string[]
  durations: number[]
}

export interface ModelConfig {
  TEXT: TextModelOption[]
  IMAGE: ImageModelOption[]
  VIDEO: VideoModelOption[]
}

/**
 * 工具箱模型配置
 * 根据后端 V1_DEVELOPMENT_GUIDE.md 第6.1节配置
 */
export const TOOLBOX_MODELS: ModelConfig = {
  TEXT: [
    {
      code: 'gemini-3-pro-preview',
      name: 'Gemini 3 Pro',
      description: '智能文本生成',
      costPerToken: 0.002, // 2积分/1000tokens
    },
    {
      code: 'gpt-4o',
      name: 'GPT-4o',
      description: 'OpenAI GPT-4o 多模态模型',
      costPerToken: 0.003, // 3积分/1000tokens
    },
  ],
  IMAGE: [
    {
      code: 'jimeng-4.0',
      name: '即梦图片 4.0',
      description: '快速图片生成',
      costPerItem: 10,
      aspectRatios: ['1:1', '16:9', '9:16', '21:9'],
    },
    {
      code: 'jimeng-4.5',
      name: '即梦图片 4.5',
      description: '高质量图片生成',
      costPerItem: 15,
      aspectRatios: ['1:1', '16:9', '9:16', '21:9'],
    },
    {
      code: 'gemini-3-pro-image-preview',
      name: 'Gemini 3 Pro Image',
      description: '顶级质量图片',
      costPerItem: 20,
      aspectRatios: ['1:1', '16:9', '9:16', '21:9'],
    },
  ],
  VIDEO: [
    {
      code: 'sora-2',
      name: 'Sora 2',
      description: 'AI视频生成',
      costPerSecond: 5, // 5积分/秒
      aspectRatios: ['16:9', '9:16', '21:9'],
      durations: [5, 10, 15, 20], // 可选时长(秒)
    },
  ],
}

/**
 * 根据类型和模型计算预估消耗
 */
export function calculateEstimatedCost(
  type: 'TEXT' | 'IMAGE' | 'VIDEO',
  modelCode: string,
  options?: {
    promptLength?: number // 文本长度(字符)
    duration?: number // 视频时长(秒)
  }
): number {
  if (type === 'TEXT') {
    const model = TOOLBOX_MODELS.TEXT.find((m) => m.code === modelCode)
    if (!model) return 0

    // 估算: 1个中文字符 ≈ 2 tokens
    const estimatedTokens = (options?.promptLength || 100) * 2
    return Math.ceil(estimatedTokens * model.costPerToken)
  }

  if (type === 'IMAGE') {
    const model = TOOLBOX_MODELS.IMAGE.find((m) => m.code === modelCode)
    return model?.costPerItem || 0
  }

  if (type === 'VIDEO') {
    const model = TOOLBOX_MODELS.VIDEO.find((m) => m.code === modelCode)
    if (!model) return 0

    const duration = options?.duration || 5
    return duration * model.costPerSecond
  }

  return 0
}

/**
 * 获取类型的默认模型
 */
export function getDefaultModel(type: 'TEXT' | 'IMAGE' | 'VIDEO'): string {
  return TOOLBOX_MODELS[type][0]?.code || ''
}

/**
 * 获取类型的默认比例
 */
export function getDefaultAspectRatio(type: 'TEXT' | 'IMAGE' | 'VIDEO'): string {
  if (type === 'TEXT') return ''
  if (type === 'IMAGE') return '16:9'
  if (type === 'VIDEO') return '16:9'
  return ''
}

/**
 * 获取模型支持的比例列表
 */
export function getModelAspectRatios(type: 'TEXT' | 'IMAGE' | 'VIDEO', modelCode: string): string[] {
  if (type === 'TEXT') return []

  if (type === 'IMAGE') {
    const model = TOOLBOX_MODELS.IMAGE.find((m) => m.code === modelCode)
    return model?.aspectRatios || []
  }

  if (type === 'VIDEO') {
    const model = TOOLBOX_MODELS.VIDEO.find((m) => m.code === modelCode)
    return model?.aspectRatios || []
  }

  return []
}

/**
 * 获取视频模型支持的时长列表
 */
export function getVideoDurations(modelCode: string): number[] {
  const model = TOOLBOX_MODELS.VIDEO.find((m) => m.code === modelCode)
  return model?.durations || [5]
}
