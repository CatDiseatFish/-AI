import api from './index'
import type {
  ToolboxGenerateRequest,
  ToolboxGenerateResponse,
  ToolboxHistoryVO,
} from '@/types/api'

export const toolboxApi = {
  /**
   * Generate content (text/image/video) in toolbox
   * Unified endpoint for all generation types
   * Image/Video generation may take time to initialize, so use extended timeout
   */
  async generate(data: ToolboxGenerateRequest): Promise<ToolboxGenerateResponse> {
    // Use extended timeout (3 minutes) for generation initialization
    // TEXT: synchronous, may take 30-60s for long responses
    // IMAGE: async initialization may take 30-60s (then polls)
    // VIDEO: async initialization may take 30-60s (then polls)
    const timeout = 180000 // 180 seconds (3 minutes)

    return api.post('/toolbox/generate', data, { timeout })
  },

  /**
   * Get toolbox generation history (last 7 days)
   */
  async getHistory(params?: { page?: number; size?: number }): Promise<ToolboxHistoryVO[]> {
    const response = (await api.get('/toolbox/history', {
      params: {
        page: params?.page || 1,
        size: params?.size || 100, // Get all recent records
      },
    })) as { records?: any[] }

    // Backend returns PageResult, extract records
    const records = response.records || []

    // Debug: log raw backend response to identify field names
    if (records.length > 0) {
      console.log('[ToolboxAPI] Raw history item fields:', Object.keys(records[0]))
      console.log('[ToolboxAPI] Sample raw item:', JSON.stringify(records[0], null, 2))
    }

    // Map backend fields to frontend ToolboxHistoryVO
    // Backend uses 'resultContent' but frontend expects 'text'
    // Backend may use 'inputPrompt' or 'prompt' for the prompt field
    return records.map((item: any) => ({
      ...item,
      text: item.resultContent || item.text || null,  // Map resultContent -> text
      resultUrl: item.resultUrl || null,
      prompt: item.inputPrompt || item.prompt || null,  // Try inputPrompt first, then prompt
    })) as ToolboxHistoryVO[]
  },

  /**
   * Delete history record
   */
  async deleteHistory(id: number): Promise<void> {
    return api.delete(`/toolbox/history/${id}`)
  },

  /**
   * Save history item to asset library
   */
  async saveToAssets(id: number): Promise<void> {
    console.log('[ToolboxAPI] Saving to assets, historyId:', id)
    return api.post('/toolbox/save', { historyId: id })
  },
}
