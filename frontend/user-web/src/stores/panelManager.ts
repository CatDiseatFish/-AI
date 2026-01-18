import { defineStore } from 'pinia'
import { ref, nextTick } from 'vue'

export type PanelType = 'default' | 'asset-edit' | 'history' | 'shot-image-generate' | 'video-generate' | null

export interface PanelData {
  [key: string]: any
}

export const usePanelManagerStore = defineStore('panelManager', () => {
  const currentPanelType = ref<PanelType>('default')
  const panelData = ref<PanelData>({})
  const panelKey = ref(0) // 添加key强制刷新组件

  const openPanel = async (type: PanelType, data?: PanelData) => {
    const payload = { ...(data || {}) }
    // 先重置，避免同类型面板不刷新
    currentPanelType.value = null
    panelData.value = {}
    panelKey.value++

    await nextTick()

    currentPanelType.value = type
    panelData.value = payload
    panelKey.value++

    console.log('[PanelManager] Opening panel:', type, payload, 'key:', panelKey.value)
  }

  const closePanel = () => {
    console.log('[PanelManager] Closing panel, returning to default')
    currentPanelType.value = 'default'
    panelData.value = {}
    panelKey.value++ // 刷新组件
  }

  const updatePanelData = (data: PanelData) => {
    panelData.value = { ...panelData.value, ...data }
    console.log('[PanelManager] Updated panel data:', panelData.value)
  }

  return {
    currentPanelType,
    panelData,
    panelKey,
    openPanel,
    closePanel,
    updatePanelData,
  }
})
