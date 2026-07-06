import ApiClient from './ApiClient'
import type { PaymentResponse } from '@/types/payment'

export const paymentApi = {
  createPayment(data: any) {
    return ApiClient.post<PaymentResponse>('/api/payment/create', data)
  },

  checkPaymentStatus(content: string) {
    return ApiClient.get<boolean>('/api/payment/status', {
      params: {
        content,
      },
    })
  },

  //vnpay
  createVNPayPayment(data: any) {
    return ApiClient.post('/api/payment/vnpay/create', data)
  },
}
