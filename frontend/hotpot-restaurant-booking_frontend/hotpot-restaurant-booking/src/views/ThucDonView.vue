<script setup lang="ts">
import { ref } from 'vue'

import MonView from './MonView.vue'
import ComBoView from './ComBoView.vue'

const activeTab = ref<'mon' | 'combo'>('mon')
</script>

<template>
  <div class="thuc-don-tong">
    <div class="thanh-top-wrapper">
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
  background: linear-gradient(135deg, #f9efe0 0%, #f4e4c6 100%);
  min-height: 100vh;
  padding-top: 100px;
  padding-bottom: 50px;
  color: #5f3d22;
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

/* Cụm Tabs quản lý chỉnh chu */
.quan-ly-tabs {
  display: flex;
  background: rgba(255, 248, 234, 0.95);
  border: 1px solid #e6d2aa;
  padding: 6px;
  border-radius: 16px;
  box-shadow: 0 6px 16px rgba(103, 72, 32, 0.08);
}

.tab-btn {
  background: transparent;
  border: none;
  color: #8b5e34;
  padding: 10px 24px;
  font-size: 0.95rem;
  font-weight: 600;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.tab-btn:hover {
  color: #d8a85c;
}

.tab-btn.active {
  background: #d8a85c;
  color: #3d2814;
  box-shadow: 0 4px 12px rgba(103, 72, 32, 0.12);
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
}
</style>