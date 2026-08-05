<script setup lang="ts">
import { computed, nextTick, ref, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/AuthStore'
import {
  addChatMessage,
  getChatMessages,
  getConversationUnreadCount,
  markConversationMessagesAsRead,
  prepareChatAttachment,
  removeChatMessage,
  updateChatMessage,
  type ChatAttachment,
  type ChatMessage,
} from '@/utils/chatStorage'

const authStore = useAuthStore()
const messages = ref<ChatMessage[]>([])
const selectedConversationKey = ref<string>('')
const replyText = ref('')
const replyAttachments = ref<ChatAttachment[]>([])
const editingMessageId = ref<string | null>(null)
const editMessageText = ref('')
const editAttachments = ref<ChatAttachment[]>([])
const activeContextMessageId = ref<string | null>(null)
const replyTextRef = ref<HTMLTextAreaElement | null>(null)
const showEmojiPicker = ref(false)
const emojiOptions = ['😀', '😄', '😆', '😊', '😎', '😍', '🥰', '🤩', '👍', '🎉', '🍜', '🥳', '😮', '😂', '💬']

const conversations = computed(() => {
  const map = new Map<string, ChatMessage>()
  for (const message of messages.value) {
    const existing = map.get(message.conversationKey)
    if (!existing || Date.parse(message.createdAt) > Date.parse(existing.createdAt)) {
      map.set(message.conversationKey, message)
    }
  }

  return [...map.entries()]
    .map(([conversationKey, latest]) => ({
      conversationKey,
      customerName: latest.customerName || 'Khách hàng',
      customerPhone: latest.customerPhone || 'Khách vãng lai',
      preview: latest.text || (latest.attachments?.length ? 'Đính kèm media' : 'Chưa có tin nhắn'),
      latestAt: latest.createdAt,
      unreadCount: getConversationUnreadCount(conversationKey, 'customer'),
    }))
    .sort((a, b) => Date.parse(b.latestAt) - Date.parse(a.latestAt))
})

const selectedConversationMessages = computed(() => {
  if (!selectedConversationKey.value) return []
  return [...messages.value]
    .filter((message) => message.conversationKey === selectedConversationKey.value)
    .sort((a, b) => Date.parse(a.createdAt) - Date.parse(b.createdAt))
})
const threadMessagesRef = ref<HTMLElement | null>(null)

const scrollThreadToBottom = () => {
  nextTick(() => {
    requestAnimationFrame(() => {
      const element = threadMessagesRef.value
      if (!element) return
      element.scrollTop = element.scrollHeight
    })
  })
}

const loadMessages = () => {
  messages.value = getChatMessages()
  if (!selectedConversationKey.value && conversations.value.length > 0) {
    const firstConversation = conversations.value[0]
    if (firstConversation) {
      selectedConversationKey.value = firstConversation.conversationKey
    }
  }
  if (selectedConversationKey.value) {
    markConversationMessagesAsRead(selectedConversationKey.value, 'customer')
    messages.value = getChatMessages()
  }
  scrollThreadToBottom()
}

const selectConversation = (conversationKey: string) => {
  selectedConversationKey.value = conversationKey
  editingMessageId.value = null
  replyText.value = ''
  replyAttachments.value = []
  markConversationMessagesAsRead(conversationKey, 'customer')
  messages.value = getChatMessages()
  scrollThreadToBottom()
}

const insertEmoji = (emoji: string) => {
  const textarea = replyTextRef.value
  if (!textarea) {
    replyText.value += emoji
    return
  }

  const start = textarea.selectionStart ?? replyText.value.length
  const end = textarea.selectionEnd ?? replyText.value.length
  const before = replyText.value.slice(0, start)
  const after = replyText.value.slice(end)

  replyText.value = `${before}${emoji}${after}`
  showEmojiPicker.value = false

  nextTick(() => {
    textarea.focus()
    const cursor = start + emoji.length
    textarea.setSelectionRange(cursor, cursor)
  })
}

const onReplyFilesChanged = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files || [])
  if (!files.length) return

  const prepared = await Promise.all(files.map((file) => prepareChatAttachment(file)))
  replyAttachments.value = [...replyAttachments.value, ...prepared]
  input.value = ''
}

const resetReplyComposer = () => {
  replyText.value = ''
  replyAttachments.value = []
  showEmojiPicker.value = false
}

const commitReplyMessage = () => {
  const selected = conversations.value.find((item) => item.conversationKey === selectedConversationKey.value)
  if (!selected) return

  const text = replyText.value.trim()
  const attachmentPayload = replyAttachments.value.map((item) => ({ ...item }))
  if (!text && !attachmentPayload.length) return

  addChatMessage({
    id: `staff_${Date.now()}_${Math.random().toString(16).slice(2, 8)}`,
    conversationKey: selectedConversationKey.value,
    customerKey: selectedConversationKey.value,
    customerName: selected.customerName,
    customerPhone: selected.customerPhone,
    sender: 'staff',
    senderName: authStore.accountName || 'Nhân viên',
    text,
    attachments: attachmentPayload,
    createdAt: new Date().toISOString(),
  })

  resetReplyComposer()
  loadMessages()
  scrollThreadToBottom()
}

const onEditFilesChanged = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files || [])
  if (!files.length) return

  const prepared = await Promise.all(files.map((file) => prepareChatAttachment(file)))
  editAttachments.value = [...editAttachments.value, ...prepared]
  input.value = ''
}

const sendReply = () => {
  const text = replyText.value.trim()
  if (!text && !replyAttachments.value.length) return

  commitReplyMessage()
}

const beginEdit = (message: ChatMessage) => {
  editingMessageId.value = message.id
  editMessageText.value = message.text
  editAttachments.value = message.attachments?.map((item) => ({ ...item })) || []
  activeContextMessageId.value = null
}

const toggleMessageContext = (message: ChatMessage) => {
  if (message.sender !== 'staff') return
  activeContextMessageId.value = activeContextMessageId.value === message.id ? null : message.id
}

const saveEdit = (messageId: string) => {
  updateChatMessage(messageId, {
    text: editMessageText.value.trim(),
    attachments: editAttachments.value.map((item) => ({ ...item })),
  })
  editingMessageId.value = null
  editMessageText.value = ''
  editAttachments.value = []
  loadMessages()
}

const deleteMessage = (messageId: string) => {
  removeChatMessage(messageId)
  activeContextMessageId.value = null
  editingMessageId.value = null
  loadMessages()
}

watch(
  () => selectedConversationMessages.value.length,
  () => {
    scrollThreadToBottom()
  },
)

onMounted(() => {
  loadMessages()
  window.addEventListener('storage', loadMessages)
  window.setInterval(() => loadMessages(), 1500)
})
</script>

<template>
  <div class="message-page-shell">
    <div class="message-list-panel">
      <div class="panel-header">
        <h2>Tin nhắn</h2>
        <span>{{ conversations.length }} hội thoại</span>
      </div>

      <div class="conversation-list">
        <button
          v-for="conversation in conversations"
          :key="conversation.conversationKey"
          class="conversation-item"
          :class="{ active: selectedConversationKey === conversation.conversationKey, unread: conversation.unreadCount > 0 }"
          @click="selectConversation(conversation.conversationKey)"
        >
          <div class="conversation-name-row">
            <div class="conversation-name-wrap">
              <span v-if="conversation.unreadCount > 0" class="conversation-dot" aria-hidden="true"></span>
              <div class="conversation-name">{{ conversation.customerName }}</div>
            </div>
            <span v-if="conversation.unreadCount" class="conversation-badge">{{ conversation.unreadCount }}</span>
          </div>
          <div class="conversation-phone">{{ conversation.customerPhone }}</div>
          <div class="conversation-preview">{{ conversation.preview }}</div>
        </button>
      </div>
    </div>

    <div class="thread-panel">
      <div v-if="selectedConversationMessages.length" ref="threadMessagesRef" class="thread-messages">
        <div
          v-for="message in selectedConversationMessages"
          :key="message.id"
          class="thread-row"
          :class="message.sender === 'staff' ? 'staff' : 'customer'"
        >
          <div class="thread-card-wrapper" @click="toggleMessageContext(message)">
            <div class="thread-card">
              <div class="thread-meta">
                <span>{{ message.senderName }}</span>
                <span>{{ new Date(message.createdAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) }}</span>
              </div>

            <template v-if="editingMessageId === message.id">
              <textarea v-model="editMessageText" rows="3" class="edit-textarea"></textarea>
              <div class="media-grid">
                <template v-for="attachment in editAttachments" :key="`${message.id}-${attachment.name}`">
                  <img v-if="attachment.type === 'image'" :src="attachment.url" :alt="attachment.name" class="message-media" loading="lazy" />
                  <video v-else :src="attachment.url" controls playsinline preload="metadata" class="message-media"></video>
                </template>
              </div>
              <div class="edit-actions">
                <label class="mini-upload">
                  <input type="file" multiple accept="image/*,video/*" @change="onEditFilesChanged" />
                  Thêm media
                </label>
                <button class="btn-save" @click="saveEdit(message.id)">Lưu</button>
              </div>
            </template>

            <template v-else>
              <p v-if="message.text" class="thread-text">{{ message.text }}</p>
              <div v-if="message.attachments?.length" class="media-grid">
                <template v-for="attachment in message.attachments" :key="`${message.id}-${attachment.name}`">
                  <img v-if="attachment.type === 'image'" :src="attachment.url" :alt="attachment.name" class="message-media" loading="lazy" />
                  <video v-else :src="attachment.url" controls playsinline preload="metadata" class="message-media"></video>
                </template>
              </div>
            </template>

              <div v-if="message.sender === 'staff' && activeContextMessageId === message.id" class="thread-context-menu">
                <button @click.stop="beginEdit(message)">Sửa</button>
                <button @click.stop="deleteMessage(message.id)">Xóa</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="thread-empty">Chưa có hội thoại nào.</div>

      <div class="reply-box">
        <div class="reply-input-row">
          <textarea ref="replyTextRef" v-model="replyText" rows="3" placeholder="Nhập phản hồi cho khách hàng..." class="reply-input"></textarea>
          <div class="emoji-picker-wrap">
            <button class="emoji-toggle" @click="showEmojiPicker = !showEmojiPicker" title="Chèn emoji">😊</button>
            <div v-if="showEmojiPicker" class="emoji-picker">
              <button v-for="emoji in emojiOptions" :key="emoji" class="emoji-option" @click="insertEmoji(emoji)">{{ emoji }}</button>
            </div>
          </div>
        </div>
        <div class="reply-attachment-list" v-if="replyAttachments.length">
          <span v-for="(attachment, index) in replyAttachments" :key="`${attachment.name}-${index}`" class="attachment-pill">
            {{ attachment.name }}
          </span>
        </div>
        <div class="reply-actions">
          <label class="mini-upload">
            <input type="file" multiple accept="image/*,video/*" @change="onReplyFilesChanged" />
            Đính kèm phản hồi
          </label>
          <button class="btn-send" @click="sendReply">Gửi phản hồi</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.message-page-shell {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 18px;
  padding: 18px;
  min-height: calc(100vh - 60px);
}

.message-list-panel,
.thread-panel {
  background: rgba(255, 248, 234, 0.96);
  border: 1px solid #e6d2aa;
  border-radius: 18px;
  box-shadow: 0 10px 24px rgba(103, 72, 32, 0.06);
}

.panel-header {
  padding: 16px 14px;
  border-bottom: 1px solid #ebd8b0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h2 {
  margin: 0;
  color: #7f4c1e;
}

.conversation-list {
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 120px);
  overflow: auto;
}

.conversation-item {
  border: none;
  border-bottom: 1px solid #f0dfb7;
  background: transparent;
  text-align: left;
  padding: 14px;
  cursor: pointer;
}

.conversation-item.active,
.conversation-item:hover {
  background: #f8ead1;
}

.conversation-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.conversation-name-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.conversation-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ff4d4f;
  box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.18);
  flex-shrink: 0;
}

.conversation-name {
  font-weight: 800;
  color: #6b4728;
}

.conversation-item.unread .conversation-preview {
  color: #7d4a15;
  font-weight: 700;
}

.conversation-badge {
  min-width: 22px;
  height: 22px;
  border-radius: 999px;
  background: #ff4d4f;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  font-size: 0.72rem;
  font-weight: 800;
}

.conversation-phone,
.conversation-preview {
  font-size: 0.78rem;
  color: #8b683b;
}

.thread-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.thread-messages {
  flex: 1;
  overflow: auto;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.thread-row {
  display: flex;
}

.thread-row.customer {
  justify-content: flex-end;
}

.thread-row.staff {
  justify-content: flex-start;
}

.thread-card-wrapper {
  position: relative;
  width: min(78%, 500px);
}

.thread-card {
  padding: 10px 12px;
  border-radius: 18px;
  border: 1px solid #ead6aa;
  background: #fff;
  box-shadow: 0 4px 12px rgba(109, 77, 25, 0.08);
}

.thread-row.customer .thread-card {
  background: linear-gradient(135deg, #f6ddb1, #f1cf8f);
}

.thread-context-menu {
  position: absolute;
  right: 0;
  top: -42px;
  display: flex;
  gap: 6px;
  background: rgba(71, 43, 12, 0.94);
  color: #fff;
  padding: 6px;
  border-radius: 10px;
}

.thread-context-menu button {
  border: none;
  background: transparent;
  color: #fff;
  cursor: pointer;
  font-size: 0.72rem;
}

.thread-meta {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 0.72rem;
  color: #8d6a3a;
  margin-bottom: 6px;
}

.thread-text {
  margin: 0;
  white-space: pre-wrap;
  color: #4a3118;
  font-size: 0.9rem;
}

.media-grid {
  display: grid;
  gap: 6px;
  margin-top: 6px;
}

.message-media {
  width: 100%;
  max-height: 200px;
  object-fit: cover;
  border-radius: 10px;
}

.thread-actions,
.edit-actions,
.reply-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  align-items: center;
}

.btn-action,
.btn-save,
.btn-send,
.mini-upload {
  border: none;
  border-radius: 10px;
  padding: 6px 10px;
  cursor: pointer;
  font-size: 0.78rem;
}

.btn-action,
.btn-save {
  background: #f4ddab;
  color: #7a4b1c;
}

.btn-danger {
  background: #f4c7c7;
  color: #8a2c2c;
}

.btn-send {
  background: linear-gradient(135deg, #8f572b, #dbab53);
  color: #fff;
}

.mini-upload {
  background: #f6ead0;
  color: #7a4b1c;
}

.mini-upload input {
  display: none;
}

.reply-box {
  border-top: 1px solid #ead8af;
  padding: 14px;
  background: #fffaf1;
}

.reply-input,
.edit-textarea {
  width: 100%;
  border: 1px solid #e7d7ad;
  border-radius: 10px;
  padding: 8px 10px;
  box-sizing: border-box;
  resize: vertical;
}

.reply-attachment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 6px;
}

.attachment-pill {
  font-size: 0.72rem;
  background: #f4e5c9;
  color: #6e451f;
  padding: 3px 8px;
  border-radius: 999px;
}

.thread-empty {
  padding: 18px;
  color: #8b683b;
}
</style>
