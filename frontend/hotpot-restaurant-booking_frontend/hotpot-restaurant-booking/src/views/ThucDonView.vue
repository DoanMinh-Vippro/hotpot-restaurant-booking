<script setup lang="ts">
import { ref } from 'vue' // Bổ sung ref để quản lý trạng thái Tab
import { useRouter } from 'vue-router'

// Import 3 view hiện tại vào như các component
import MonView from './MonView.vue'
import ComBoView from './ComBoView.vue'

const router = useRouter()

// Trạng thái Tab hiện tại ('mon' hoặc 'combo')
const activeTab = ref<'mon' | 'combo'>('mon')

// Hàm xử lý quay về trang chủ (Khớp name: 'home' trong router/index.ts)
const quayVeTrangChu = () => {
  router.push({ name: 'home' })
}
</script>

<template>
  <div class="thuc-don-tong">
    <div class="thanh-top-wrapper">
      <button class="nut-trang-chu" @click="quayVeTrangChu">
        🏠 Quay về Trang chủ
      </button>

      <div class="quan-ly-tabs">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'mon' }"
          @click="activeTab = 'mon'"
        >
          🍔 Quản lý Món ăn
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'combo' }"
          @click="activeTab = 'combo'"
        >
          🍱 Quản lý Gói Combo
        </button>
      </div>
    </div>

    <div class="tab-content-container">
      <transition name="fade" mode="out-in">
        <section v-if="activeTab === 'mon'" key="mon" class="menu-section animate-fade">
          <MonView />
        </section>

        <section v-else-if="activeTab === 'combo'" key="combo" class="menu-section animate-fade">
          <ComBoView />
        </section>
      </transition>
    </div>
  </div>
</template>

<style scoped>
.thuc-don-tong {
  background: #0f0f0f;
  min-height: 100vh;
  padding-top: 100px; /* Bù lại khoảng trống cho Navbar */
  padding-bottom: 50px;
}

/* Khu vực bao bọc nút trang chủ và thanh Tab */
.thanh-top-wrapper {
  max-width: 1400px;
  margin: 0 auto 30px;
  padding: 0 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

/* Thiết kế nút trang chủ mượt mà theo tông Dark Mode */
.nut-trang-chu {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #c7c7c7;
  padding: 10px 20px;
  border-radius: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.nut-trang-chu:hover {
  background: rgba(197, 160, 89, 0.15); /* Đồng bộ tông vàng mờ #c5a059 của bạn */
  border-color: #c5a059;
  color: #c5a059;
}

/* Cụm Tabs quản lý chỉnh chu */
.quan-ly-tabs {
  display: flex;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  padding: 6px;
  border-radius: 16px;
}

.tab-btn {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.6);
  padding: 10px 24px;
  font-size: 0.95rem;
  font-weight: 600;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.tab-btn:hover {
  color: #c5a059;
}

.tab-btn.active {
  background: #c5a059;
  color: #101010;
  box-shadow: 0 4px 12px rgba(197, 160, 89, 0.2);
}

.tab-content-container {
  max-width: 1400px;
  margin: 0 auto;
}

.menu-section {
  padding: 0 10px;
}

/* Hiệu ứng chuyển động mượt mà khi đổi Tab */
.animate-fade {
  animation: fadeIn 0.3s ease-in-out forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive tối ưu trên màn hình nhỏ */
@media (max-width: 768px) {
  .thanh-top-wrapper {
    flex-direction: column;
    align-items: stretch;
    padding: 0 16px;
  }
  .quan-ly-tabs {
    flex-direction: column;
  }
  .nut-trang-chu {
    text-align: center;
  }
}
</style>