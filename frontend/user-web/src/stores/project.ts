import { defineStore } from 'pinia'
import { projectApi, folderApi } from '@/api/apis'
import type { ProjectVO, FolderVO, CreateProjectDTO, CreateFolderDTO } from '@/types/api'

interface ProjectState {
  projects: ProjectVO[]
  folders: FolderVO[]
  currentProject: ProjectVO | null
  loading: boolean
  currentPage: number
  pageSize: number
  total: number
  keyword: string
  selectedFolderId: number | null
}

export const useProjectStore = defineStore('project', {
  state: (): ProjectState => ({
    projects: [],
    folders: [],
    currentProject: null,
    loading: false,
    currentPage: 1,
    pageSize: 20,
    total: 0,
    keyword: '',
    selectedFolderId: null,
  }),

  getters: {
    // Group projects by folder
    projectsByFolder: (state) => {
      const grouped: Record<string, ProjectVO[]> = {
        uncategorized: [],
      }

      // Initialize folder groups
      state.folders.forEach((folder) => {
        grouped[String(folder.id)] = []
      })

      // Categorize projects
      state.projects.forEach((project) => {
        if (project.folderId) {
          const key = String(project.folderId)
          if (!grouped[key]) {
            grouped[key] = []
          }
          grouped[key].push(project)
        } else {
          grouped.uncategorized.push(project)
        }
      })

      return grouped
    },

    // Get folder by ID
    getFolderById: (state) => (id: number) => {
      return state.folders.find((f) => f.id === id)
    },
  },

  actions: {
    async fetchProjects(page = 1, keyword = '') {
      this.loading = true
      try {
        console.log('[Store] Fetching projects:', { page, keyword, folderId: this.selectedFolderId })

        const result = await projectApi.getProjects({
          page,
          size: this.pageSize,
          keyword,
          folderId: this.selectedFolderId,
        })

        console.log('[Store] API returned result:', result)

        this.projects = result.records || []
        this.total = result.total || 0
        this.currentPage = page
        this.keyword = keyword

        console.log('[Store] Projects loaded:', this.projects.length, 'total:', this.total)
      } catch (error) {
        console.error('[Store] Failed to fetch projects:', error)
        this.projects = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    async fetchFolders() {
      try {
        console.log('[Store] Fetching folders')
        const folders = await folderApi.getFolders()
        console.log('[Store] API returned folders:', folders)

        this.folders = folders || []
        console.log('[Store] Folders loaded:', this.folders.length)
      } catch (error) {
        console.error('[Store] Failed to fetch folders:', error)
        this.folders = []
      }
    },

    async createProject(data: CreateProjectDTO) {
      console.log('[Store] Creating project with data:', data)
      const project = await projectApi.createProject(data)
      console.log('[Store] API returned project:', project)
      return project
    },

    async updateProject(id: number, data: Partial<ProjectVO>) {
      const updated = await projectApi.updateProject(id, data)
      const index = this.projects.findIndex((p) => p.id === id)
      if (index !== -1) {
        this.projects[index] = updated
      }
      if (this.currentProject?.id === id) {
        this.currentProject = updated
      }
      return updated
    },

    async deleteProject(id: number) {
      await projectApi.deleteProject(id)
      this.projects = this.projects.filter((p) => p.id !== id)
      this.total--
      if (this.currentProject?.id === id) {
        this.currentProject = null
      }
    },

    async moveProjectToFolder(projectId: number, folderId: number | null) {
      await projectApi.moveToFolder(projectId, folderId)
      const project = this.projects.find((p) => p.id === projectId)
      if (project) {
        project.folderId = folderId
      }
    },

    async createFolder(data: CreateFolderDTO) {
      console.log('[Store] Creating folder with data:', data)
      const folder = await folderApi.createFolder(data)
      console.log('[Store] API returned folder:', folder)
      return folder
    },

    async updateFolder(id: number, data: Partial<FolderVO>) {
      const updated = await folderApi.updateFolder(id, data)
      const index = this.folders.findIndex((f) => f.id === id)
      if (index !== -1) {
        this.folders[index] = updated
      }
      return updated
    },

    async deleteFolder(id: number) {
      await folderApi.deleteFolder(id)
      this.folders = this.folders.filter((f) => f.id !== id)
      // Move projects in deleted folder to uncategorized
      this.projects.forEach((p) => {
        if (p.folderId === id) {
          p.folderId = null
        }
      })
    },

    async fetchProjectDetail(id: number) {
      try {
        console.log('[ProjectStore] Fetching project detail:', id)
        const project = await projectApi.getProject(id)
        this.currentProject = project
        console.log('[ProjectStore] Project detail loaded:', project)
        return project
      } catch (error) {
        console.error('[ProjectStore] Failed to fetch project detail:', error)
        this.currentProject = null
        throw error
      }
    },

    setCurrentProject(project: ProjectVO | null) {
      this.currentProject = project
    },

    setSelectedFolder(folderId: number | null) {
      this.selectedFolderId = folderId
    },
  },
})
