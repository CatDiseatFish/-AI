import api from './index'

/**
 * 上传 API
 */
export const uploadApi = {
  /**
   * 通用文件上传接口
   * @param file 文件对象
   * @param type 文件类型 (image/video/audio)
   * @returns 上传成功后的文件URL
   */
  async upload(file: File, type: 'image' | 'video' | 'audio' = 'image'): Promise<{ url: string }> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', type)

    return api.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
