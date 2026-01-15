// {{CODE-Cycle-Integration:
//   Task_ID: [#Phase9.5C-ModelConfig]
//   Timestamp: 2026-01-03T16:00:00+08:00
//   Phase: [D-Develop]
//   Context-Analysis: "Created modelConfig store for managing image and video generation model settings. Implements localStorage persistence."
//   Principle_Applied: "KISS, DRY, YAGNI"
// }}
// {{START_MODIFICATIONS}}

import { defineStore } from 'pinia'

/**
 * Model configuration for different generation types
 */
interface ModelConfig {
  languageModel: string          // 语言模型
  characterImageModel: string    // 角色画像生成
  sceneImageModel: string        // 场景画像生成
  shotImageModel: string         // 分镜画面生成
  videoModel: string             // 视频生成
}

interface ModelConfigState extends ModelConfig {
  // Future: Add more model types (audio, 3D, etc.)
}

const STORAGE_KEY = 'yuanmeng_model_config'

/**
 * Default model configuration
 */
const DEFAULT_CONFIG: ModelConfig = {
  languageModel: 'gemini-3-pro-preview',         // 语言模型默认
  characterImageModel: 'jimeng-4.5',             // 角色画像默认
  sceneImageModel: 'jimeng-4.5',                 // 场景画像默认
  shotImageModel: 'jimeng-4.5',                  // 分镜画面默认
  videoModel: 'sora-2',                          // 视频生成默认
}

/**
 * Load configuration from localStorage
 */
const loadConfig = (): ModelConfig => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      const parsed = JSON.parse(stored)
      return { ...DEFAULT_CONFIG, ...parsed }
    }
  } catch (error) {
    console.error('[ModelConfigStore] Failed to load config:', error)
  }
  return { ...DEFAULT_CONFIG }
}

/**
 * Save configuration to localStorage
 */
const saveConfig = (config: ModelConfig) => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(config))
    console.log('[ModelConfigStore] Config saved:', config)
  } catch (error) {
    console.error('[ModelConfigStore] Failed to save config:', error)
  }
}

export const useModelConfigStore = defineStore('modelConfig', {
  state: (): ModelConfigState => ({
    ...loadConfig(),
  }),

  getters: {
    /**
     * Get current language model
     */
    currentLanguageModel: (state): string => state.languageModel,

    /**
     * Get current character image generation model
     */
    currentCharacterImageModel: (state): string => state.characterImageModel,

    /**
     * Get current scene image generation model
     */
    currentSceneImageModel: (state): string => state.sceneImageModel,

    /**
     * Get current shot image generation model
     */
    currentShotImageModel: (state): string => state.shotImageModel,

    /**
     * Get current video generation model
     */
    currentVideoModel: (state): string => state.videoModel,

    /**
     * Get all configuration as an object
     */
    allConfig: (state): ModelConfig => ({
      languageModel: state.languageModel,
      characterImageModel: state.characterImageModel,
      sceneImageModel: state.sceneImageModel,
      shotImageModel: state.shotImageModel,
      videoModel: state.videoModel,
    }),
  },

  actions: {
    /**
     * Update all model configuration at once
     */
    updateConfig(config: Partial<ModelConfig>) {
      console.log('[ModelConfigStore] Updating config:', config)
      if (config.languageModel !== undefined) {
        this.languageModel = config.languageModel
      }
      if (config.characterImageModel !== undefined) {
        this.characterImageModel = config.characterImageModel
      }
      if (config.sceneImageModel !== undefined) {
        this.sceneImageModel = config.sceneImageModel
      }
      if (config.shotImageModel !== undefined) {
        this.shotImageModel = config.shotImageModel
      }
      if (config.videoModel !== undefined) {
        this.videoModel = config.videoModel
      }
      saveConfig(this.allConfig)
    },

    /**
     * Reset to default configuration
     */
    resetToDefault() {
      console.log('[ModelConfigStore] Resetting to default')
      this.languageModel = DEFAULT_CONFIG.languageModel
      this.characterImageModel = DEFAULT_CONFIG.characterImageModel
      this.sceneImageModel = DEFAULT_CONFIG.sceneImageModel
      this.shotImageModel = DEFAULT_CONFIG.shotImageModel
      this.videoModel = DEFAULT_CONFIG.videoModel
      saveConfig(this.allConfig)
    },
  },
})

// {{END_MODIFICATIONS}}
