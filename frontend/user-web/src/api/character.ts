import api from './index'
import type {
  ProjectCharacterVO,
  CharacterLibraryVO,
  CharacterCategoryVO,
  CreateCharacterDTO,
  UpdateCharacterDTO,
  GenerateCharacterDTO,
  CreateCharacterLibraryDTO,
  UpdateCharacterLibraryDTO,
  CreateCharacterCategoryDTO,
  UpdateCharacterCategoryDTO,
} from '@/types/api'

export const characterApi = {
  // ============== Project Characters ==============

  /**
   * Get all characters for a project
   */
  async getProjectCharacters(projectId: number): Promise<ProjectCharacterVO[]> {
    return api.get(`/projects/${projectId}/characters`)
  },

  /**
   * Get single character
   */
  async getCharacter(projectId: number, characterId: number): Promise<ProjectCharacterVO> {
    return api.get(`/projects/${projectId}/characters/${characterId}`)
  },

  /**
   * Create new character
   */
  async createCharacter(projectId: number, data: CreateCharacterDTO): Promise<ProjectCharacterVO> {
    return api.post(`/projects/${projectId}/characters`, data)
  },

  /**
   * Update character
   */
  async updateCharacter(
    projectId: number,
    characterId: number,
    data: UpdateCharacterDTO
  ): Promise<ProjectCharacterVO> {
    return api.put(`/projects/${projectId}/characters/${characterId}`, data)
  },

  /**
   * Delete character
   */
  async deleteCharacter(projectId: number, characterId: number): Promise<void> {
    return api.delete(`/projects/${projectId}/characters/${characterId}`)
  },

  /**
   * Generate character image
   */
  async generateCharacter(
    projectId: number,
    characterId: number,
    data: GenerateCharacterDTO
  ): Promise<{ jobId: number }> {
    return api.post(`/projects/${projectId}/generate/character/${characterId}`, data)
  },

  // ============== Character Library ==============

  /**
   * Get library characters (optionally filtered by category)
   */
  async getLibraryCharacters(categoryId?: number): Promise<CharacterLibraryVO[]> {
    const params = categoryId ? { categoryId } : {}
    return api.get('/library/characters', { params })
  },

  /**
   * Get all character categories
   */
  async getCharacterCategories(): Promise<CharacterCategoryVO[]> {
    return api.get('/library/characters/categories')
  },

  /**
   * Create character from library
   */
  async createFromLibrary(projectId: number, libraryCharacterId: number): Promise<ProjectCharacterVO> {
    return api.post(`/projects/${projectId}/characters`, {
      characterLibraryId: libraryCharacterId,
      name: '', // Will be populated from library
      description: '', // Will be populated from library
    })
  },

  /**
   * Create library character
   */
  async createLibraryCharacter(data: CreateCharacterLibraryDTO): Promise<CharacterLibraryVO> {
    return api.post('/library/characters', data)
  },

  /**
   * Update library character
   */
  async updateLibraryCharacter(
    characterId: number,
    data: UpdateCharacterLibraryDTO
  ): Promise<void> {
    return api.put(`/library/characters/${characterId}`, data)
  },

  /**
   * Delete library character
   */
  async deleteLibraryCharacter(characterId: number): Promise<void> {
    return api.delete(`/library/characters/${characterId}`)
  },

  /**
   * Upload library character thumbnail
   */
  async uploadLibraryCharacterThumbnail(characterId: number, file: File): Promise<{ url: string }> {
    const formData = new FormData()
    formData.append('file', file)
    return api.post(`/library/characters/${characterId}/thumbnail`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  // ============== Character Categories ==============

  /**
   * Create character category
   */
  async createCategory(data: CreateCharacterCategoryDTO): Promise<CharacterCategoryVO> {
    return api.post('/character-categories', data)
  },

  /**
   * Update (rename) character category
   */
  async updateCategory(categoryId: number, data: UpdateCharacterCategoryDTO): Promise<void> {
    return api.put(`/character-categories/${categoryId}`, data)
  },

  /**
   * Delete character category
   */
  async deleteCategory(categoryId: number): Promise<void> {
    return api.delete(`/character-categories/${categoryId}`)
  },
}
