<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import NotificationApi from '@/api/NotificationApi'
import { useAuthStore } from '@/stores/AuthStore'

const emit = defineEmits(['close'])
const authStore = useAuthStore()

const notifications = ref<any[]>([])
let refreshTimer: number | null = null

const matchesTarget = (notification: any) => {
  if (authStore.isAdmin) return true
  if (notification?.targetStaff) return false

  const currentId = authStore.customerInfo?.khachHangId
  const currentPhone = authStore.customerInfo?.soDienThoai
  const targetId = notification?.targetKhachHangId
  const targetPhone = notification?.targetPhone || notification?.targetKhachHangPhone || notification?.targetPhone

  if (!currentId && !currentPhone) return true
  if (targetId != null && currentId != null && Number(targetId) === Number(currentId)) return true
  if (targetPhone && currentPhone && String(targetPhone).trim() === String(currentPhone).trim()) return true
  if (targetId == null && targetPhone == null) return true
  return false
}

const load = async () => {
  if (!authStore.isAuthenticated) {
    notifications.value = []
    return
  }

  try {
    const response = await NotificationApi.getAll()
    const items = Array.isArray(response?.data) ? response.data : []
    notifications.value = items.filter(matchesTarget).map((item: any) => ({
      ...item,
      time: item.createdAt || item.time,
      read: Boolean(item.read),
      eventKey: item.id,
    }))
  } catch (error) {
    notifications.value = []
  }
}

onMounted(() => {
  void load()
  refreshTimer = window.setInterval(() => {
    void load()
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
  }
})

const markRead = (notification: any) => {
  if (!notification) return
  notification.read = true
}

const markAllRead = () => {
  notifications.value = notifications.value.map((n: any) => ({ ...n, read: true }))
}

const visibleNotifications = computed(() => notifications.value)

const close = () => emit('close')
</script>

<template>
  <div class="notification-panel">
    <div class="panel-header">
      <strong>Thông báo</strong>
      <div class="panel-actions">
        <button class="btn-small" @click="markAllRead">Đánh dấu đã đọc</button>
        <button class="btn-small close" @click="$emit('close')">Đóng</button>
      </div>
    </div>
    <div v-if="visibleNotifications.length === 0" class="empty">Không có thông báo</div>
    <div v-else class="notif-list">
      <div v-for="(n, i) in visibleNotifications" :key="i" class="notif-item" :class="{ unread: !n.read }" @click="markRead(n)">
        <div class="notif-title">{{ n.title }}</div>
        <div class="notif-body">{{ n.message }}</div>
        <div class="notif-meta">{{ n.time ? new Date(n.time).toLocaleString() : '' }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.notification-panel {
  position: absolute;
  right: 20px;
  top: 64px;
  width: 320px;
  background: #fffaf1;
  border: 1px solid #e4c78b;
  padding: 10px;
  border-radius: 8px;
  z-index: 2001;
  box-shadow: 0 6px 18px rgba(103,72,32,0.16);
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.panel-actions { display:flex; gap:6px }
.btn-small { background: transparent; border: 1px solid #d8b66c; color:#6b4728; padding:4px 8px; border-radius:4px; cursor:pointer }
.btn-small.close { border-color: #b96d3d; color: #b96d3d }
.empty { color:#8f6b46; padding:10px; text-align:center }
.notif-list { max-height: 300px; overflow-y: auto }
.notif-item { padding:8px; border-bottom:1px solid #efe0be; cursor:pointer }
.notif-item.unread { background: rgba(216,168,92,0.12) }
.notif-title { color:#8b5e34; font-weight:700 }
.notif-body { color:#6b4728; font-size:13px }
.notif-meta { color:#8f6b46; font-size:11px; margin-top:6px }
</style>
