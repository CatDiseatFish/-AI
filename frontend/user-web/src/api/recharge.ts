import api from './index'
import type {
  RechargeProductVO,
  ExchangeRuleVO,
  CreateOrderRequest,
  NativeOrderVO,
  OrderStatusVO,
} from '@/types/api'

export const rechargeApi = {
  /**
   * 获取充值套餐列表
   */
  async getProducts(): Promise<RechargeProductVO[]> {
    return api.get('/recharge/products')
  },

  /**
   * 获取兑换规则
   */
  async getExchangeRules(): Promise<ExchangeRuleVO[]> {
    return api.get('/recharge/exchange-rules')
  },

  /**
   * 创建充值订单
   */
  async createOrder(data: CreateOrderRequest): Promise<NativeOrderVO> {
    return api.post('/recharge/orders', data)
  },

  /**
   * 查询订单状态
   */
  async getOrderStatus(orderNo: string): Promise<OrderStatusVO> {
    return api.get(`/recharge/orders/${orderNo}`)
  },
}
