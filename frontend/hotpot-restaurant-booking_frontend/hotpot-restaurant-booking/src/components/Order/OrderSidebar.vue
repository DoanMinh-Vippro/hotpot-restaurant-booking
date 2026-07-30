<script setup lang="ts">
import { onMounted, ref } from 'vue'
import OrderApi from '@/api/Order'
import type { OrderBan, OrderKhuVuc } from '@/types/Order'

const khuVucs = ref<OrderKhuVuc[]>([])
const loading = ref(false)
const expanded = ref<number[]>([])

const emit = defineEmits<{
  (e: 'select-ban', value: OrderBan): void
}>()

async function loadBan() {
  loading.value = true

  try {
    const res = await OrderApi.getBan()

    khuVucs.value = res.data as OrderKhuVuc[]

    expanded.value = khuVucs.value.map((i) => i.idKhuVuc)
  } finally {
    loading.value = false
  }
}

function toggleKhuVuc(id: number) {
  const index = expanded.value.indexOf(id)

  if (index >= 0) {
    expanded.value.splice(index, 1)
  } else {
    expanded.value.push(id)
  }
}

function selectBan(ban: OrderBan) {
  emit('select-ban', ban)
}

onMounted(() => {
  loadBan()
})
</script>

<template>
  <div class="sidebar">
    <div class="title">KHU VỰC</div>

    <div v-if="loading" class="loading">Đang tải danh sách bàn...</div>

    <template v-else>
      <div v-for="kv in khuVucs" :key="kv.idKhuVuc" class="khu-vuc">
        <div class="header" @click="toggleKhuVuc(kv.idKhuVuc)">
          <span class="arrow">
            {{ expanded.includes(kv.idKhuVuc) ? '▼' : '▶' }}
          </span>

          <span class="name">
            {{ kv.tenKhuVuc }}
          </span>
        </div>

        <Transition name="collapse">
          <div v-if="expanded.includes(kv.idKhuVuc)" class="list-ban">
            <div v-for="ban in kv.dsBan" :key="ban.idBan" class="ban" @click="selectBan(ban)">
              <div class="left">
                <span class="check">🍽️</span>

                <span class="ten-ban">
                  {{ ban.tenBan }}
                </span>
              </div>

              <div class="right">{{ ban.sucChua }} khách</div>
            </div>
          </div>
        </Transition>
      </div>

      <div v-if="!khuVucs.length" class="empty">Không có bàn nào đang hoạt động.</div>
    </template>
  </div>
</template>

<style scoped>
.sidebar {
  height: 100%;
  overflow-y: auto;
  background: #f7f3eb;
  padding: 16px;
}

.title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 18px;
  color: #5a4634;
}

.loading,
.empty {
  display: flex;
  justify-content: center;
  align-items: center;
  color: #8d7967;
  padding: 40px 0;
  font-size: 14px;
}

.khu-vuc {
  background: #fffdf9;
  border-radius: 18px;
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow:
    0 3px 10px rgba(84, 60, 37, 0.06),
    0 1px 2px rgba(84, 60, 37, 0.04);
}

.header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 18px;
  cursor: pointer;
  user-select: none;
  transition: 0.25s;
}

.header:hover {
  background: #f6f0e8;
}

.arrow {
  width: 18px;
  text-align: center;
  color: #8c745f;
  font-size: 13px;
}

.name {
  flex: 1;
  font-size: 16px;
  font-weight: 700;
  color: #5a4634;
}

.list-ban {
  padding: 8px 12px 14px;
}

.ban {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 14px;
  padding: 12px 14px;
  margin-top: 8px;
  cursor: pointer;
  transition: 0.2s;
}

.ban:hover {
  background: #f6f1ea;
}

.ban.active {
  background: #efe3d2;
  border: 1px solid #cfb391;
}

.left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.check {
  width: 22px;
  font-size: 17px;
  color: #8b5e34;
}

.ten-ban {
  font-weight: 600;
  color: #5a4634;
}

.right {
  font-size: 13px;
  color: #9b8772;
  font-weight: 500;
}

.ban.active .ten-ban,
.ban.active .right,
.ban.active .check {
  color: #8b5e34;
}

.collapse-enter-active,
.collapse-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}

.collapse-enter-from,
.collapse-leave-to {
  opacity: 0;
  transform: translateY(-6px);
  max-height: 0;
}

.collapse-enter-to,
.collapse-leave-from {
  opacity: 1;
  transform: translateY(0);
  max-height: 600px;
}

::-webkit-scrollbar {
  width: 7px;
}

::-webkit-scrollbar-thumb {
  background: #d7c7b5;
  border-radius: 999px;
}

::-webkit-scrollbar-thumb:hover {
  background: #bda791;
}
</style>
