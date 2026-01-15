/**
 * Centralized API exports
 * Import all APIs from a single location for convenience
 */

export { authApi } from './auth'
export { projectApi } from './project'
export { folderApi } from './folder'
export { shotApi } from './shot'
export { characterApi } from './character'
export { sceneApi } from './scene'
export { propApi } from './prop'
export { assetApi } from './asset'
export { generationApi } from './generation'
export { jobApi, pollJobStatus } from './job'
export { toolboxApi } from './toolbox'
export { inviteApi } from './invite'
export { walletApi } from './wallet'
export { styleApi } from './style'
export { uploadApi } from './upload'

// Re-export the base api instance for custom requests
export { default as api } from './index'
