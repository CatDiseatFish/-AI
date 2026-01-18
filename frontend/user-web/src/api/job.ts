import api from './index'
import type { JobVO, PageResult } from '@/types/api'

export const jobApi = {
  /**
   * Get job status by ID
   */
  async getJobStatus(jobId: number): Promise<JobVO> {
    return api.get(`/jobs/${jobId}`)
  },

  /**
   * Get user's job history with pagination
   */
  async getUserJobs(params: {
    page: number
    size: number
    status?: 'PENDING' | 'RUNNING' | 'SUCCEEDED' | 'FAILED' | 'CANCELED'
    jobType?: string
    projectId?: number
  }): Promise<PageResult<JobVO>> {
    return api.get('/jobs', { params })
  },

  /**
   * Cancel a job (if possible)
   */
  async cancelJob(jobId: number): Promise<void> {
    return api.post(`/jobs/${jobId}/cancel`)
  },
}

/**
 * Poll job status until completion or failure
 * @param jobId - Job ID to poll
 * @param onProgress - Callback for progress updates
 * @param interval - Polling interval in milliseconds (default: 3000)
 * @returns Final job result
 */
export async function pollJobStatus(
  jobId: number,
  onProgress?: (job: JobVO) => void,
  interval: number = 3000
): Promise<JobVO> {
  return new Promise((resolve, reject) => {
    const poll = async () => {
      try {
        const job = await jobApi.getJobStatus(jobId)

        // Notify progress callback
        if (onProgress) {
          onProgress(job)
        }

        // Check if job is finished
        // Backend may return 'SUCCEEDED' or 'COMPLETED'
        if (job.status === 'SUCCEEDED' || job.status === 'COMPLETED') {
          resolve(job)
        } else if (job.status === 'FAILED') {
          reject(new Error(job.errorMessage || 'Job failed'))
        } else {
          // Continue polling
          setTimeout(poll, interval)
        }
      } catch (error) {
        reject(error)
      }
    }

    poll()
  })
}
