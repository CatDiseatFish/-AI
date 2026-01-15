import api from './index'
import type { ExportRequest, ExportResponse } from '@/types/api'

export const exportApi = {
  /**
   * 提交导出任务
   */
  async submitExportTask(projectId: number, request: ExportRequest): Promise<ExportResponse> {
    return api.post(`/projects/${projectId}/export`, request)
  },

  /**
   * 下载导出文件
   */
  getDownloadUrl(jobId: number): string {
    const baseURL = api.defaults.baseURL || '/api'
    const token = localStorage.getItem('token')
    return `${baseURL}/exports/${jobId}/download?token=${token}`
  },

  /**
   * 触发文件下载
   */
  downloadExportFile(jobId: number): void {
    const url = this.getDownloadUrl(jobId)
    const link = document.createElement('a')
    link.href = url
    link.download = `project_export_${jobId}.zip`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  },
}
