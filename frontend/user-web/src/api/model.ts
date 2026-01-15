// {{CODE-Cycle-Integration:
//   Task_ID: [#T-BACKEND-MODEL-API]
//   Timestamp: 2026-01-04T15:55:39+08:00
//   Phase: [D-Develop]
//   Context-Analysis: "Creating frontend API for fetching AI models from backend. Follows styleApi pattern."
//   Principle_Applied: "KISS, DRY, API-First"
// }}
// {{START_MODIFICATIONS}}
import api from './index'

/**
 * AI模型VO接口
 */
export interface ModelVO {
  id: number
  code: string
  name: string
  type: 'LANGUAGE' | 'IMAGE' | 'VIDEO'
  provider: string
  enabled: number
}

/**
 * AI模型API
 */
export const modelApi = {
  /**
   * 根据类型获取AI模型列表
   * @param type 模型类型: LANGUAGE, IMAGE, VIDEO
   * @returns 模型列表
   */
  async getModels(type: 'LANGUAGE' | 'IMAGE' | 'VIDEO'): Promise<ModelVO[]> {
    return api.get('/ai-models', { params: { type } })
  }
}

// {{END_MODIFICATIONS}}
