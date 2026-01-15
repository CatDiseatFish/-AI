import api from './index'
import type { FolderVO, CreateFolderDTO, UpdateFolderDTO } from '@/types/api'

export const folderApi = {
  /**
   * Get all folders for current user
   */
  async getFolders(): Promise<FolderVO[]> {
    return api.get('/folders')
  },

  /**
   * Create new folder
   */
  async createFolder(data: CreateFolderDTO): Promise<FolderVO> {
    return api.post('/folders', data)
  },

  /**
   * Update folder
   */
  async updateFolder(id: number, data: UpdateFolderDTO): Promise<FolderVO> {
    return api.put(`/folders/${id}`, data)
  },

  /**
   * Delete folder
   */
  async deleteFolder(id: number): Promise<void> {
    return api.delete(`/folders/${id}`)
  },
}
