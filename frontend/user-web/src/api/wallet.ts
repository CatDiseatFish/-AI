import api from './index'
import type { WalletVO, TransactionVO, PageResult } from '@/types/api'

export const walletApi = {
  /**
   * Get current wallet balance
   */
  async getBalance(): Promise<WalletVO> {
    const data = await api.get('/wallet') as any
    console.log('[WalletAPI] Raw balance response:', data)

    // Backend may not return frozenBalance, default to 0
    const result = {
      balance: data.balance || 0,
      frozenBalance: data.frozenBalance || 0,
    }
    console.log('[WalletAPI] Mapped balance:', result)
    return result
  },

  /**
   * Get transaction history with pagination
   */
  async getTransactionHistory(params: {
    page: number
    size: number
  }): Promise<PageResult<TransactionVO>> {
    const result = await api.get('/wallet/transactions', { params }) as PageResult<any>
    console.log('[WalletAPI] Raw transactions response:', result)

    // Log first transaction to see structure
    if (result.records && result.records.length > 0) {
      console.log('[WalletAPI] First transaction raw:', result.records[0])
    }

    // Map backend transaction records, add defaults for missing fields
    const mapped = {
      ...result,
      records: (result.records || []).map((item: any) => ({
        id: item.id,
        type: item.type || 'UNKNOWN',
        amount: item.amount || 0,
        balance: item.balanceAfter || 0,  // Backend field is balanceAfter
        description: item.description || item.remark || '(无描述)',
        createdAt: item.createdAt || item.createTime || new Date().toISOString(),
        bizType: item.bizType,  // Include bizType for description formatting
        metaJson: item.metaJson,  // Include metaJson for parsing job details
      })) as TransactionVO[]
    }

    console.log('[WalletAPI] Mapped transactions:', mapped.records.slice(0, 2))
    return mapped
  },
}
