<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useAuthStore } from '@/stores/AuthStore'
import {
  addChatMessage,
  getChatMessages,
  getCustomerConversationKey,
  getUnreadChatCount,
  markConversationMessagesAsRead,
  prepareChatAttachment,
  removeChatMessage,
  updateChatMessage,
  type ChatAttachment,
  type ChatMessage,
} from '@/utils/chatStorage'

const authStore = useAuthStore()
const isOpen = ref(false)
const draft = ref('')
const attachments = ref<ChatAttachment[]>([])
const messageList = ref<ChatMessage[]>([])
const editingMessageId = ref<string | null>(null)
const activeContextMessageId = ref<string | null>(null)
const draftInputRef = ref<HTMLTextAreaElement | null>(null)
const showEmojiPicker = ref(false)
const emojiOptions = ['😀', '😄', '😆', '😊', '😎', '😍', '🥰', '🤩', '👍', '🎉', '🍜', '🥳', '😮', '😂', '💬']
let refreshTimer: number | null = null

const guestSeed = (() => {
  let seed = sessionStorage.getItem('restaurant_chat_guest_seed')
  if (!seed) {
    seed = `guest_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`
    sessionStorage.setItem('restaurant_chat_guest_seed', seed)
  }
  return seed
})()

const currentCustomerKey = computed(() => {
  if (authStore.khachHangId != null) {
    return `customer_${authStore.khachHangId}`
  }

  return `guest_${guestSeed}`
})

const customerProfile = computed(() => ({
  khachHangId: authStore.khachHangId,
  soDienThoai: authStore.customerInfo?.soDienThoai || '',
  email: authStore.customerInfo?.email || '',
  tenKhachHang: authStore.tenKhachHang || 'Khách hàng',
  maKhachHang: authStore.customerInfo?.maKhachHang || '',
}))

const currentConversationKey = computed(() => getCustomerConversationKey(customerProfile.value))
const chatBodyRef = ref<HTMLElement | null>(null)

const scrollChatToBottom = () => {
  nextTick(() => {
    requestAnimationFrame(() => {
      const element = chatBodyRef.value
      if (!element) return
      element.scrollTop = element.scrollHeight
    })
  })
}

const loadChat = () => {
  const messages = getChatMessages().filter((message) => message.conversationKey === currentConversationKey.value)
  messageList.value = [...messages].sort(
    (a, b) => Date.parse(a.createdAt) - Date.parse(b.createdAt),
  )
  scrollChatToBottom()
}

const hasUnreadMessages = computed(() => getUnreadChatCount(currentConversationKey.value) > 0)

const getTodayDateKey = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const ensureWelcomeMessage = () => {
  const today = getTodayDateKey()
  const welcomeStorageKey = `welcomed_date_${currentCustomerKey.value}`
  const alreadyWelcomedToday = localStorage.getItem(welcomeStorageKey) === today

  if (alreadyWelcomedToday) return

  const welcomeMessage: ChatMessage = {
    id: `welcome_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
    conversationKey: currentConversationKey.value,
    customerKey: currentCustomerKey.value,
    customerName: customerProfile.value.tenKhachHang || 'Khách hàng',
    customerPhone: customerProfile.value.soDienThoai || 'Khách vãng lai',
    sender: 'staff',
    senderName: 'Nhà hàng Cái Bang',
    text: 'Xin chào quý khách! Nhà hàng Lẩu Ếch Cái Bang chúc quý khách một ngày mới tốt lành và ngon miệng!',
    attachments: [],
    createdAt: new Date().toISOString(),
    isRead: false,
  }

  addChatMessage(welcomeMessage)
  localStorage.setItem(welcomeStorageKey, today)
  loadChat()
}

const onFileSelected = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files || [])

  if (!files.length) return

  const prepared = await Promise.all(files.map((file) => prepareChatAttachment(file)))
  attachments.value = [...attachments.value, ...prepared]
  input.value = ''
}

const appendMessageWithAttachments = (nextMessage: ChatMessage) => {
  addChatMessage(nextMessage)
  loadChat()
  scrollChatToBottom()
}

const removeAttachment = (index: number) => {
  attachments.value = attachments.value.filter((_, itemIndex) => itemIndex !== index)
}

const resetComposer = () => {
  draft.value = ''
  attachments.value = []
  showEmojiPicker.value = false
}

const insertEmoji = (emoji: string) => {
  const textarea = draftInputRef.value
  if (!textarea) {
    draft.value += emoji
    return
  }

  const start = textarea.selectionStart ?? draft.value.length
  const end = textarea.selectionEnd ?? draft.value.length
  const before = draft.value.slice(0, start)
  const after = draft.value.slice(end)

  draft.value = `${before}${emoji}${after}`
  showEmojiPicker.value = false

  nextTick(() => {
    textarea.focus()
    const cursor = start + emoji.length
    textarea.setSelectionRange(cursor, cursor)
  })
}

const sendMessage = () => {
  const text = draft.value.trim()
  const attachmentPayload = attachments.value.map((item) => ({ ...item }))
  if (!text && !attachmentPayload.length) return

  if (editingMessageId.value) {
    updateChatMessage(editingMessageId.value, {
      text,
      attachments: attachmentPayload,
    })
    editingMessageId.value = null
    activeContextMessageId.value = null
    resetComposer()
    loadChat()
    scrollChatToBottom()
    return
  }

  const message: ChatMessage = {
    id: `chat_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
    conversationKey: currentConversationKey.value,
    customerKey: currentCustomerKey.value,
    customerName: customerProfile.value.tenKhachHang || 'Khách hàng',
    customerPhone: customerProfile.value.soDienThoai || 'Khách vãng lai',
    sender: 'customer',
    senderName: customerProfile.value.tenKhachHang || 'Khách hàng',
    text,
    attachments: attachmentPayload,
    createdAt: new Date().toISOString(),
    isRead: false,
  }

  appendMessageWithAttachments(message)
  resetComposer()
}

const startEditMessage = (message: ChatMessage) => {
  editingMessageId.value = message.id
  draft.value = message.text
  attachments.value = (message.attachments || []).map((item) => ({ ...item }))
  activeContextMessageId.value = null
}

const deleteOwnMessage = (messageId: string) => {
  removeChatMessage(messageId)
  activeContextMessageId.value = null
  editingMessageId.value = null
  resetComposer()
  loadChat()
}

const toggleMessageContext = (message: ChatMessage) => {
  if (message.sender !== 'customer') return
  activeContextMessageId.value = activeContextMessageId.value === message.id ? null : message.id
}

const onWidgetOpen = () => {
  isOpen.value = true
  loadChat()
  ensureWelcomeMessage()
  markConversationMessagesAsRead(currentConversationKey.value)
  loadChat()
  scrollChatToBottom()
}

watch(
  () => messageList.value.length,
  () => {
    scrollChatToBottom()
  },
)

onMounted(() => {
  loadChat()
  refreshTimer = window.setInterval(() => {
    loadChat()
  }, 1500)
  window.addEventListener('storage', loadChat)
})

onUnmounted(() => {
  if (refreshTimer) window.clearInterval(refreshTimer)
  window.removeEventListener('storage', loadChat)
})
</script>

<template>
  <div v-if="authStore.isAuthenticated && authStore.isUser" class="chat-widget-shell">
    <button v-if="!isOpen" class="chat-toggle" @click="onWidgetOpen" title="Chat trực tuyến">
      <span class="chat-badge">💬</span>
      <span v-if="hasUnreadMessages" class="chat-badge-dot"></span>
    </button>

    <div v-if="isOpen" class="chat-panel">
      <div class="chat-header">
        <div>
          <div class="chat-title">Chat trực tuyến</div>
          <div class="chat-subtitle">Hỗ trợ nhanh từ nhà hàng</div>
        </div>
        <button class="chat-close" @click="isOpen = false">×</button>
      </div>

      <div ref="chatBodyRef" class="chat-body">
        <div v-if="messageList.length" class="chat-message-list">
          <div
            v-for="message in messageList"
            :key="message.id"
            class="chat-message-row"
            :class="message.sender === 'customer' ? 'customer' : 'staff'"
          >
            <div class="chat-message-card-wrapper" @click="toggleMessageContext(message)">
              <div class="chat-message-card">
                <div class="chat-message-meta">
                  <span>{{ message.senderName }}</span>
                  <span>{{ new Date(message.createdAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) }}</span>
                </div>
                <p v-if="message.text" class="chat-message-text">{{ message.text }}</p>
                <div v-if="message.attachments?.length" class="chat-media-grid">
                  <template v-for="attachment in message.attachments" :key="`${message.id}-${attachment.name}`">
                    <img v-if="attachment.type === 'image'" :src="attachment.url" :alt="attachment.name" class="chat-media" loading="lazy" />
                    <video v-else :src="attachment.url" controls playsinline preload="metadata" class="chat-media"></video>
                  </template>
                </div>
              </div>

              <div v-if="message.sender === 'customer' && activeContextMessageId === message.id" class="chat-context-menu">
                <button @click.stop="startEditMessage(message)">Sửa</button>
                <button @click.stop="deleteOwnMessage(message.id)">Xóa</button>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="chat-empty">Bắt đầu trò chuyện với nhà hàng.</div>
      </div>

      <div class="chat-editor">
        <div class="chat-editor-top">
          <textarea ref="draftInputRef" v-model="draft" rows="3" :placeholder="editingMessageId ? 'Chỉnh sửa tin nhắn...' : 'Nhập tin nhắn...'" class="chat-input"></textarea>
          <div class="emoji-picker-wrap">
            <button class="emoji-toggle" @click="showEmojiPicker = !showEmojiPicker" title="Chèn emoji">😊</button>
            <div v-if="showEmojiPicker" class="emoji-picker">
              <button v-for="emoji in emojiOptions" :key="emoji" class="emoji-option" @click="insertEmoji(emoji)">{{ emoji }}</button>
            </div>
          </div>
        </div>
        <div class="chat-attachment-list" v-if="attachments.length">
          <div v-for="(attachment, index) in attachments" :key="`${attachment.name}-${index}`" class="attachment-chip">
            <span>{{ attachment.name }}</span>
            <button @click="removeAttachment(index)">×</button>
          </div>
        </div>
        <div class="chat-actions">
          <label class="upload-btn">
            <input type="file" multiple accept="image/*,video/*" @change="onFileSelected" />
            Đính kèm
          </label>
          <button class="send-btn" @click="sendMessage">{{ editingMessageId ? 'Cập nhật' : 'Gửi' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-widget-shell {
  position: fixed;
  right: 18px;
  bottom: 18px;
  z-index: 1200;
}

.chat-toggle {
  position: relative;
  border: none;
  width: 62px;
  height: 62px;
  border-radius: 50%;
  background: linear-gradient(135deg, #9f6a2e, #d1a665);
  color: #fff;
  box-shadow: 0 10px 30px rgba(129, 78, 11, 0.35);
  cursor: pointer;
}

.chat-badge {
  font-size: 1.5rem;
}

.chat-badge-dot {
  position: absolute;
  top: 5px;
  right: 2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #ff3b30;
  border: 2px solid #fff;
}

.chat-panel {
  width: 360px;
  max-height: 74vh;
  background: rgba(255, 248, 234, 0.98);
  border: 1px solid #e4c78b;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 15px 45px rgba(65, 38, 8, 0.18);
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  background: linear-gradient(135deg, #9a6336, #d5a85a);
  color: #fff;
}

.chat-title {
  font-weight: 800;
}

.chat-subtitle {
  font-size: 0.72rem;
  opacity: 0.9;
}

.chat-close {
  background: transparent;
  border: none;
  color: #fff;
  font-size: 1.4rem;
  cursor: pointer;
}

.chat-body {
  max-height: 340px;
  overflow: auto;
  padding: 12px;
  background: #fffaf1;
}

.chat-message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-message-row {
  display: flex;
}

.chat-message-row.customer {
  justify-content: flex-end;
}

.chat-message-row.staff {
  justify-content: flex-start;
}

.chat-message-card-wrapper {
  position: relative;
  max-width: 84%;
}

.chat-message-card {
  background: #fff;
  border: 1px solid #efdfbb;
  border-radius: 18px;
  padding: 10px 12px;
  box-shadow: 0 4px 12px rgba(116, 76, 27, 0.08);
}

.chat-message-row.customer .chat-message-card {
  background: linear-gradient(135deg, #f4dca9, #efd39a);
}

.chat-message-row.staff .chat-message-card {
  background: #fff;
}

.chat-context-menu {
  position: absolute;
  right: 0;
  top: -42px;
  display: flex;
  gap: 6px;
  background: rgba(59, 37, 8, 0.95);
  color: #fff;
  padding: 6px;
  border-radius: 10px;
  z-index: 2;
}

.chat-context-menu button {
  border: none;
  background: transparent;
  color: #fff;
  cursor: pointer;
  font-size: 0.72rem;
}

.chat-message-meta {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 0.66rem;
  color: #8d6a3a;
  margin-bottom: 4px;
}

.chat-message-text {
  margin: 0;
  white-space: pre-wrap;
  color: #4a3118;
  font-size: 0.83rem;
}

.chat-media-grid {
  margin-top: 8px;
  display: grid;
  gap: 6px;
}

.chat-media {
  width: 100%;
  border-radius: 10px;
  object-fit: cover;
  max-height: 160px;
}

.chat-empty {
  text-align: center;
  color: #8f6b46;
  font-size: 0.82rem;
  padding: 14px 0;
}

.chat-editor-top {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.emoji-picker-wrap {
  position: relative;
  flex-shrink: 0;
}

.emoji-toggle {
  border: 1px solid #e8d5a6;
  background: #fff;
  border-radius: 10px;
  width: 38px;
  height: 38px;
  cursor: pointer;
}

.emoji-picker {
  position: absolute;
  right: 0;
  bottom: 46px;
  width: 220px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 6px;
  background: #fff;
  border: 1px solid #e9cf98;
  border-radius: 12px;
  padding: 8px;
  box-shadow: 0 10px 22px rgba(95, 62, 13, 0.16);
}

.emoji-option {
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 1.15rem;
}

.chat-attachment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px 0 0;
}

.attachment-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 999px;
  background: #f4e4c6;
  color: #56361a;
  font-size: 0.72rem;
}

.attachment-chip button {
  border: none;
  background: transparent;
  cursor: pointer;
}

.chat-editor {
  padding: 12px;
  border-top: 1px solid #ead8af;
  background: #fffaf1;
}

.chat-input {
  width: 100%;
  border: 1px solid #e8d5a6;
  border-radius: 14px;
  padding: 10px 12px;
  resize: none;
  box-sizing: border-box;
  min-height: 52px;
  background: #fff;
}

.chat-actions {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
  margin-top: 10px;
}

.upload-btn,
.send-btn {
  border: none;
  border-radius: 12px;
  cursor: pointer;
  padding: 9px 14px;
  font-weight: 700;
}

.upload-btn {
  background: #f6ead0;
  color: #7b4e1f;
}

.upload-btn input {
  display: none;
}

.send-btn {
  background: linear-gradient(135deg, #9a6336, #d5a85a);
  color: #fff;
}
</style>
