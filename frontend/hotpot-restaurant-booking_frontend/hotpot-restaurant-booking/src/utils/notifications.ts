export type NotificationPayload = {
  key: string
  title: string
  message: string
  targetKhachHangId?: number | null
  targetKhachHangPhone?: string | null
  targetStaff?: boolean
}

const notificationKeysStorage = 'notification-event-keys'

const readNotifications = (): any[] => {
  try {
    const value = JSON.parse(localStorage.getItem('notifications') || '[]')
    return Array.isArray(value) ? value : []
  } catch {
    return []
  }
}

const readEventKeys = (): Set<string> => {
  try {
    const value = JSON.parse(localStorage.getItem(notificationKeysStorage) || '[]')
    return new Set(Array.isArray(value) ? value : [])
  } catch {
    return new Set()
  }
}

export const addNotificationOnce = (payload: NotificationPayload): boolean => {
  const eventKeys = readEventKeys()
  if (eventKeys.has(payload.key)) return false

  const notifications = readNotifications()
  notifications.unshift({
    title: payload.title,
    message: payload.message,
    targetKhachHangId: payload.targetKhachHangId,
    targetKhachHangPhone: payload.targetKhachHangPhone,
    targetStaff: payload.targetStaff,
    eventKey: payload.key,
    time: new Date().toISOString(),
    read: false,
  })

  eventKeys.add(payload.key)
  localStorage.setItem('notifications', JSON.stringify(notifications))
  localStorage.setItem(notificationKeysStorage, JSON.stringify([...eventKeys]))
  window.dispatchEvent(new Event('notification-created'))
  return true
}
