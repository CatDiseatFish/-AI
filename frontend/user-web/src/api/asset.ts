import api from './index'
import type { AssetVersionVO, SetCurrentVersionRequest } from '@/types/api'

export const assetApi = {
  /**
   * Get all versions for an asset
   */
  async getAssetVersions(assetId: number): Promise<AssetVersionVO[]> {
    return api.get(`/assets/${assetId}/versions`)
  },

  /**
   * Upload new asset version
   */
  async uploadNewVersion(assetId: number, file: File): Promise<AssetVersionVO> {
    const formData = new FormData()
    formData.append('file', file)

    return api.post(`/assets/${assetId}/versions/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  /**
   * Upload new asset version from URL
   */
  async uploadFromUrl(assetId: number, imageUrl: string): Promise<AssetVersionVO> {
    return api.post(`/assets/${assetId}/versions/upload-from-url`, null, {
      params: {
        imageUrl
      }
    })
  },

  /**
   * Set current version
   */
  async setCurrentVersion(assetId: number, request: SetCurrentVersionRequest): Promise<void> {
    return api.put(`/assets/${assetId}/current`, request)
  },

  /**
   * Download asset
   */
  async downloadAsset(url: string): Promise<Blob> {
    const response = await api.get(url, {
      responseType: 'blob',
    })

    return response as unknown as Blob
  },
}
