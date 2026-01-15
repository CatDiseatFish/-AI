import api from './index'
import type {
  ProjectPropVO,
  PropLibraryVO,
  PropCategoryVO,
  CreatePropDTO,
  UpdatePropDTO,
  GeneratePropDTO,
  CreatePropLibraryDTO,
  UpdatePropLibraryDTO,
  CreatePropCategoryDTO,
  UpdatePropCategoryDTO,
  AddPropToProjectDTO,
} from '@/types/api'

export const propApi = {
  // ============== Project Props ==============

  /**
   * Get all props for a project
   */
  async getProjectProps(projectId: number): Promise<ProjectPropVO[]> {
    return api.get(`/projects/${projectId}/props`)
  },

  /**
   * Get single prop
   */
  async getProp(projectId: number, propId: number): Promise<ProjectPropVO> {
    return api.get(`/projects/${projectId}/props/${propId}`)
  },

  /**
   * Create new prop (add from library to project)
   */
  async addPropToProject(projectId: number, data: AddPropToProjectDTO): Promise<ProjectPropVO> {
    return api.post(`/projects/${projectId}/props`, data)
  },

  /**
   * Update prop
   */
  async updateProp(
    projectId: number,
    propId: number,
    data: UpdatePropDTO
  ): Promise<ProjectPropVO> {
    return api.put(`/projects/${projectId}/props/${propId}`, data)
  },

  /**
   * Delete prop from project
   */
  async deleteProp(projectId: number, propId: number): Promise<void> {
    return api.delete(`/projects/${projectId}/props/${propId}`)
  },

  /**
   * Generate prop image
   */
  async generateProp(
    projectId: number,
    propId: number,
    data: GeneratePropDTO
  ): Promise<{ jobId: number }> {
    return api.post(`/projects/${projectId}/generate/prop/${propId}`, data)
  },

  // ============== Prop Library ==============

  /**
   * Get library props (optionally filtered by category)
   */
  async getLibraryProps(categoryId?: number): Promise<PropLibraryVO[]> {
    const params = categoryId ? { categoryId } : {}
    return api.get('/library/props', { params })
  },

  /**
   * Get all prop categories
   */
  async getPropCategories(): Promise<PropCategoryVO[]> {
    return api.get('/library/props/categories')
  },

  /**
   * Create prop from library
   */
  async createFromLibrary(projectId: number, libraryPropId: number): Promise<ProjectPropVO> {
    return api.post(`/projects/${projectId}/props`, {
      libraryPropId: libraryPropId,
    })
  },

  /**
   * Create library prop
   */
  async createLibraryProp(data: CreatePropLibraryDTO): Promise<PropLibraryVO> {
    return api.post('/library/props', data)
  },

  /**
   * Update library prop
   */
  async updateLibraryProp(propId: number, data: UpdatePropLibraryDTO): Promise<void> {
    return api.put(`/library/props/${propId}`, data)
  },

  /**
   * Delete library prop
   */
  async deleteLibraryProp(propId: number): Promise<void> {
    return api.delete(`/library/props/${propId}`)
  },

  // ============== Prop Categories ==============

  /**
   * Create prop category
   */
  async createCategory(data: CreatePropCategoryDTO): Promise<PropCategoryVO> {
    return api.post('/library/props/categories', data)
  },

  /**
   * Update (rename) prop category
   */
  async updateCategory(categoryId: number, data: UpdatePropCategoryDTO): Promise<void> {
    return api.put(`/library/props/categories/${categoryId}`, data)
  },

  /**
   * Delete prop category
   */
  async deleteCategory(categoryId: number): Promise<void> {
    return api.delete(`/library/props/categories/${categoryId}`)
  },
}
