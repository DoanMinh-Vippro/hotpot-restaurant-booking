export type ChatSender = 'customer' | 'staff'

export interface ChatAttachment {
  type: 'image' | 'video'
  url: string
  name: string
}

export const prepareChatAttachment = async (file: File): Promise<ChatAttachment> => {
  const type = file.type.startsWith('video/') ? 'video' : 'image'
  const url = URL.createObjectURL(file)

  return {
    type,
    url,
    name: file.name || `${type}-${Date.now()}`,
  }
}

export interface ChatMessage {
  id: string
  conversationKey: string
  customerKey: string
  customerName: string
  customerPhone: string
  sender: ChatSender
  senderName: string
  text: string
  attachments: ChatAttachment[]
  createdAt: string
  editedAt?: string
  isRead?: boolean
}

const CHAT_STORAGE_KEY = 'restaurant_chat_messages'

export const getChatMessages = (): ChatMessage[] => {
  try {
    const stored = localStorage.getItem(CHAT_STORAGE_KEY)
    if (!stored) return []
    const parsed = JSON.parse(stored)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

export const saveChatMessages = (messages: ChatMessage[]) => {
  try {
    localStorage.setItem(CHAT_STORAGE_KEY, JSON.stringify(messages))
  } catch {
    // ignore storage errors
  }
}

export const addChatMessage = (message: ChatMessage) => {
  const current = getChatMessages()
  current.push(message)
  saveChatMessages(current)
  return current
}

export const updateChatMessage = (messageId: string, values: Partial<ChatMessage>) => {
  const current = getChatMessages()
  const next = current.map((message) => {
    if (message.id !== messageId) return message
    return {
      ...message,
      ...values,
      editedAt: new Date().toISOString(),
    }
  })
  saveChatMessages(next)
  return next
}

export const removeChatMessage = (messageId: string) => {
  const current = getChatMessages().filter((message) => message.id !== messageId)
  saveChatMessages(current)
  return current
}

export const markConversationMessagesAsRead = (
  conversationKey: string,
  sender?: ChatSender,
) => {
  const current = getChatMessages()
  const next = current.map((message) => {
    if (message.conversationKey !== conversationKey) return message
    if (sender && message.sender !== sender) return message
    return { ...message, isRead: true }
  })
  saveChatMessages(next)
  return next
}

export const getUnreadChatCount = (conversationKey: string, sender: ChatSender = 'staff') =>
  getChatMessages().filter(
    (message) => message.conversationKey === conversationKey && message.sender === sender && !message.isRead,
  ).length

export const getConversationUnreadCount = (conversationKey: string, sender: ChatSender = 'customer') =>
  getChatMessages().filter(
    (message) => message.conversationKey === conversationKey && message.sender === sender && !message.isRead,
  ).length

export const getCustomerConversationKey = (customer: {
  khachHangId?: number | string | null
  soDienThoai?: string | null
  email?: string | null
  tenKhachHang?: string | null
  maKhachHang?: string | null
}) => {
  const rawId = customer?.khachHangId ?? customer?.maKhachHang ?? customer?.soDienThoai ?? customer?.email
  if (rawId != null && String(rawId).trim() !== '') {
    return `customer_${String(rawId)}`
  }

  const guestSeed = `${customer?.tenKhachHang || 'guest'}-${customer?.soDienThoai || customer?.email || 'anonymous'}`
  return `guest_${guestSeed}`
}

export const getConversationPreview = (conversationKey: string) => {
  const messages = getChatMessages().filter((message) => message.conversationKey === conversationKey)
  const latest = [...messages].sort((a, b) => Date.parse(b.createdAt) - Date.parse(a.createdAt))[0]
  return latest || null
}
