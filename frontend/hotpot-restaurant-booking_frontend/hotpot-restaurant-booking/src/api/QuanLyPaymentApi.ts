import ApiClient from './ApiClient'
import type { PaymentResponse } from '@/types/payment'

export const quanLyPaymentApi = {
  createPayment(data: any) {
    return ApiClient.post<PaymentResponse>('/api/quan-ly-payment/create', data)
  },

  checkPaymentStatus(content: string) {
    return ApiClient.get<boolean>('/api/quan-ly-payment/status', {
      params: {
        content,
      },
    })
  },

  createVNPayPayment(data: any) {
    return ApiClient.post('/api/quan-ly-payment/vnpay/create', data)
  },
}
