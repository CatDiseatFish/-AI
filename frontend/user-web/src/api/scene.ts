import api from './index'
import type {
  ProjectSceneVO,
  SceneLibraryVO,
  SceneCategoryVO,
  CreateSceneDTO,
  UpdateSceneDTO,
  GenerateSceneDTO,
  CreateSceneLibraryDTO,
  UpdateSceneLibraryDTO,
  CreateSceneCategoryDTO,
  UpdateSceneCategoryDTO,
} from '@/types/api'

export const sceneApi = {
  // ============== Project Scenes ==============

  /**
   * Get all scenes for a project
   */
  async getProjectScenes(projectId: number): Promise<ProjectSceneVO[]> {
    return api.get(`/projects/${projectId}/scenes`)
  },

  /**
   * Get single scene
   */
  async getScene(projectId: number, sceneId: number): Promise<ProjectSceneVO> {
    return api.get(`/projects/${projectId}/scenes/${sceneId}`)
  },

  /**
   * Create new scene
   */
  async createScene(projectId: number, data: CreateSceneDTO): Promise<ProjectSceneVO> {
    return api.post(`/projects/${projectId}/scenes`, data)
  },

  /**
   * Update scene
   */
  async updateScene(
    projectId: number,
    sceneId: number,
    data: UpdateSceneDTO
  ): Promise<ProjectSceneVO> {
    return api.put(`/projects/${projectId}/scenes/${sceneId}`, data)
  },

  /**
   * Delete scene
   */
  async deleteScene(projectId: number, sceneId: number): Promise<void> {
    return api.delete(`/projects/${projectId}/scenes/${sceneId}`)
  },

  /**
   * Generate scene image
   */
  async generateScene(
    projectId: number,
    sceneId: number,
    data: GenerateSceneDTO
  ): Promise<{ jobId: number }> {
    return api.post(`/projects/${projectId}/generate/scene/${sceneId}`, data)
  },

  // ============== Scene Library ==============

  /**
   * Get library scenes (optionally filtered by category)
   */
  async getLibraryScenes(categoryId?: number): Promise<SceneLibraryVO[]> {
    const params = categoryId ? { categoryId } : {}
    return api.get('/library/scenes', { params })
  },

  /**
   * Get all scene categories
   */
  async getSceneCategories(): Promise<SceneCategoryVO[]> {
    return api.get('/library/scenes/categories')
  },

  /**
   * Create scene from library
   */
  async createFromLibrary(projectId: number, librarySceneId: number): Promise<ProjectSceneVO> {
    return api.post(`/projects/${projectId}/scenes`, {
      sceneLibraryId: librarySceneId,
      name: '', // Will be populated from library
      description: '', // Will be populated from library
    })
  },

  /**
   * Create library scene
   */
  async createLibraryScene(data: CreateSceneLibraryDTO): Promise<SceneLibraryVO> {
    return api.post('/library/scenes', data)
  },

  /**
   * Update library scene
   */
  async updateLibraryScene(sceneId: number, data: UpdateSceneLibraryDTO): Promise<void> {
    return api.put(`/library/scenes/${sceneId}`, data)
  },

  /**
   * Delete library scene
   */
  async deleteLibraryScene(sceneId: number): Promise<void> {
    return api.delete(`/library/scenes/${sceneId}`)
  },

  // ============== Scene Categories ==============

  /**
   * Create scene category
   */
  async createCategory(data: CreateSceneCategoryDTO): Promise<SceneCategoryVO> {
    return api.post('/scene-categories', data)
  },

  /**
   * Update (rename) scene category
   */
  async updateCategory(categoryId: number, data: UpdateSceneCategoryDTO): Promise<void> {
    return api.put(`/scene-categories/${categoryId}`, data)
  },

  /**
   * Delete scene category
   */
  async deleteCategory(categoryId: number): Promise<void> {
    return api.delete(`/scene-categories/${categoryId}`)
  },
}
