import api from './index'
import type {
  ProjectVO,
  CreateProjectDTO,
  UpdateProjectDTO,
  PageResult,
} from '@/types/api'

export const projectApi = {
  /**
   * Get paginated project list
   */
  async getProjects(params: {
    page: number
    size: number
    keyword?: string
    folderId?: number | null
  }): Promise<PageResult<ProjectVO>> {
    return api.get('/projects', { params })
  },

  /**
   * Get project by ID
   */
  async getProject(id: number): Promise<ProjectVO> {
    return api.get(`/projects/${id}`)
  },

  /**
   * Create new project
   */
  async createProject(data: CreateProjectDTO): Promise<ProjectVO> {
    return api.post('/projects', data)
  },

  /**
   * Update project
   */
  async updateProject(id: number, data: UpdateProjectDTO): Promise<ProjectVO> {
    return api.put(`/projects/${id}`, data)
  },

  /**
   * Delete project
   */
  async deleteProject(id: number): Promise<void> {
    return api.delete(`/projects/${id}`)
  },

  /**
   * Move project to folder (uses standard update endpoint)
   */
  async moveToFolder(id: number, folderId: number | null): Promise<void> {
    return api.put(`/projects/${id}`, { folderId })
  },

  /**
   * Parse project text to generate storyboard shots
   */
  async parseProjectText(id: number, rawText: string): Promise<void> {
    return api.post(`/projects/${id}/parse`, { rawText })
  },
}
