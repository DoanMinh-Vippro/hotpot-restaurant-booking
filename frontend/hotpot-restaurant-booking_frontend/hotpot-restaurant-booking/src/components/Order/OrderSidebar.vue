<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import OrderApi from '@/api/Order'
import type { OrderBan, OrderKhuVuc } from '@/types/Order'

const khuVucs = ref<OrderKhuVuc[]>([])
const loading = ref(false)

// Khu vực đang chọn
const selectedKhuVuc = ref<number | null>(null)
// Bàn đang chọn
const selectedBanId = ref<number | null>(null)

const emit = defineEmits<{
  (e: 'select-ban', value: OrderBan): void
}>()

async function loadBan() {
  loading.value = true

  try {
    const res = await OrderApi.getBan()

    khuVucs.value = res.data as OrderKhuVuc[]

    // mặc định chọn khu vực đầu tiên
    if (khuVucs.value.length > 0) {
      selectedKhuVuc.value = khuVucs.value[0]!.idKhuVuc
    }
  } finally {
    loading.value = false
  }
}

// Danh sách bàn của khu vực đang chọn
const danhSachBan = computed(() => {
  return khuVucs.value.find((kv) => kv.idKhuVuc === selectedKhuVuc.value)?.dsBan || []
})

function changeKhuVuc(id: number) {
  selectedKhuVuc.value = id
}

function selectBan(ban: OrderBan) {
  selectedBanId.value = ban.idBan
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
      <!-- Tabs khu vực -->
      <div class="tabs">
        <div
          v-for="kv in khuVucs"
          :key="kv.idKhuVuc"
          class="tab"
          :class="{ active: selectedKhuVuc === kv.idKhuVuc }"
          @click="changeKhuVuc(kv.idKhuVuc)"
        >
          {{ kv.tenKhuVuc }}
        </div>
      </div>

      <!-- Grid bàn -->
      <div v-if="danhSachBan.length" class="grid">
        <div
          v-for="ban in danhSachBan"
          :key="ban.idBan"
          class="ban-card"
          :class="{ active: selectedBanId === ban.idBan }"
          @click="selectBan(ban)"
        >
          <div class="status">
            <span
              class="dot"
              :class="{
                green: ban.trangThai === 'DANG_SU_DUNG',
                orange: ban.trangThai === 'DA_DAT',
                gray: ban.trangThai === 'TRONG',
              }"
            ></span>
          </div>

          <div class="ban-name">
            {{ ban.tenBan }}
          </div>

          <div class="capacity">{{ ban.sucChua }} người</div>
        </div>
      </div>

      <div v-else class="empty">Không có bàn trong khu vực này.</div>
    </template>
  </div>
</template>

<style scoped>
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f7f3eb;
  padding: 16px;
  overflow: hidden;
  min-width: 0;
}

.title {
  font-size: 22px;
  font-weight: 700;
  color: #5a4634;
  margin-bottom: 16px;
}

/* ================= Tabs ================= */

.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
  overflow-x: auto;
  padding-bottom: 6px;

  scrollbar-width: none;
}

.tabs::-webkit-scrollbar {
  display: none;
}

.tab {
  flex-shrink: 0;
  padding: 10px 18px;
  border-radius: 999px;
  background: white;
  cursor: pointer;
  transition: 0.2s;
  font-size: 14px;
  font-weight: 600;
  color: #6b5644;
  border: 1px solid #e5d6c4;
  white-space: nowrap;
}

.tab:hover {
  background: #f4ece2;
}

.tab.active {
  background: #b7793f;
  color: white;
  border-color: #b7793f;
}

/* ================= Grid ================= */

.grid {
  flex: 1;
  overflow-y: auto;

  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 14px;

  align-content: start;

  padding-right: 4px;
}

/* ================= Card ================= */

.ban-card {
  aspect-ratio: 1 / 1;

  background: #efebe5;

  border: 2px solid #ddd2c3;

  border-radius: 18px;

  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  cursor: pointer;

  transition: all 0.22s ease;

  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.ban-card:hover {
  background: #e8e1d7;

  transform: translateY(-3px);

  border-color: #b7793f;

  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.ban-card.active {
  background: #b7793f;
  color: white;
  border-color: #a66b36;

  box-shadow: 0 12px 22px rgba(183, 121, 63, 0.3);
}

.ban-card.active .capacity {
  color: rgba(255, 255, 255, 0.85);
}

/* ================= Status ================= */

.status {
  margin-bottom: 8px;
}

.dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  display: inline-block;
}

.green {
  background: #35c759;
}

.orange {
  background: #ff9500;
}

.gray {
  background: #9e9e9e;
}

.ban-card.active .dot {
  background: white;
}

/* ================= Text ================= */

.ban-name {
  font-size: 20px;
  font-weight: 700;
}

.capacity {
  margin-top: 4px;
  font-size: 13px;
  color: #7d6b59;
}

/* ================= Empty ================= */

.loading,
.empty {
  flex: 1;

  display: flex;
  justify-content: center;
  align-items: center;

  color: #8d7967;
  font-size: 14px;
}

/* ================= Tablet ================= */

@media (max-width: 1200px) {
  .grid {
    grid-template-columns: repeat(auto-fill, minmax(105px, 1fr));
  }

  .ban-name {
    font-size: 16px;
  }

  .capacity {
    font-size: 11px;
  }
}

/* ================= Mobile ================= */

@media (max-width: 768px) {
  .sidebar {
    padding: 12px;
  }

  .title {
    font-size: 18px;
    margin-bottom: 12px;
  }

  .tabs {
    gap: 8px;
    margin-bottom: 12px;
  }

  .tab {
    padding: 8px 14px;
    font-size: 13px;
  }

  .grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .ban-card {
    aspect-ratio: 1;
    width: 80%;
    justify-self: center;
  }

  .ban-name {
    font-size: 15px;
  }

  .capacity {
    font-size: 10px;
  }

  .dot {
    width: 12px;
    height: 12px;
  }
}

/* ================= Scroll ================= */

::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-thumb {
  background: #d3c2ae;
  border-radius: 999px;
}

::-webkit-scrollbar-thumb:hover {
  background: #bca48b;
}
</style>
