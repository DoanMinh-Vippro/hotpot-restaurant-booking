import ApiClient from './ApiClient'

export interface BackendNotification {
  id: string
  title: string
  message: string
  createdAt: string
  read: boolean
  targetKhachHangId?: number | null
  targetStaff?: boolean
}

const NotificationApi = {
  getAll() {
    return ApiClient.get<BackendNotification[]>('/api/thong-bao')
  },
}

export default NotificationApi