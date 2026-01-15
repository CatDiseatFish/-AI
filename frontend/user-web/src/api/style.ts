import api from './index'
import type { StylePresetVO } from '@/types/api'

export const styleApi = {
  /**
   * Get all enabled style presets (no auth required)
   */
  async getStylePresets(): Promise<StylePresetVO[]> {
    return api.get('/style-presets')
  },
}
