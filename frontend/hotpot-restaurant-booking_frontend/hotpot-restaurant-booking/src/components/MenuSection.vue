<script setup lang="ts">
import { ref, onMounted } from 'vue'
import ComBoApi from '../api/ComBoApi' // Import API Combo hệ thống
import MonApi from '../api/MonApi'     // Import API Món ăn hệ thống của bạn
import type { Combo } from '../api/ComBoApi'
import type { Mon } from '../api/MonApi'

// Khai báo trạng thái danh sách và Tab điều hướng
const monItems = ref<Mon[]>([])
const comboItems = ref<Combo[]>([])
const activeTab = ref<'mon-le' | 'combo'>('mon-le')
const loading = ref(false)

// Ảnh phôi mặc định sang trọng dành cho món lẻ không có ảnh
const anhMacDinhMonLe = 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80'

const fetchThucDonTongHop = async () => {
  loading.value = true
  try {
    // 1. Gọi API lấy danh sách Món lẻ thực tế từ Backend
    const resMon = await MonApi.hienThiMon()
    // Lọc lấy các món đang ở trạng thái "Còn bán" (trangThai === 0)
    monItems.value = (resMon.data || []).filter((m: Mon) => m.trangThai === 0)

    // 2. Gọi API lấy danh sách Gói Combo thực tế từ Backend
    const resCombo = await ComBoApi.hienThiComBo()
    // Lọc lấy các combo đang ở trạng thái "Còn bán" (trangThai === 1)
    comboItems.value = (resCombo.data || []).filter((cb: Combo) => cb.trangThai === 1)

  } catch (error) {
    console.error('Hệ thống gặp lỗi khi tải dữ liệu thực đơn công khai:', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchThucDonTongHop)
</script>

<template>
  <section id="menu" class="menu-section">
    <div class="menu-bg-overlay"></div>

    <div class="menu-content-wrapper">
      <div class="section-header">
        <p class="subtitle">ẨM THỰC</p>
        <h2>THỰC ĐƠN ĐẶC SẮC</h2>

        <div class="menu-tabs">
          <button 
            class="tab-btn" 
            :class="{ active: activeTab === 'mon-le' }" 
            @click="activeTab = 'mon-le'"
          >
            Món Lẻ Thực Đơn
          </button>
          <button 
            class="tab-btn" 
            :class="{ active: activeTab === 'combo' }" 
            @click="activeTab = 'combo'"
          >
            Gói Combo Ưu Đãi
          </button>
        </div>
      </div>

      <div v-if="loading" class="menu-loading">
        <span class="loading-icon">⏳</span> Đang nạp danh sách thực đơn nhà hàng...
      </div>

      <div v-else>
        <div v-if="activeTab === 'mon-le'" class="menu-grid">
          <div v-for="mon in monItems" :key="mon.idMon" class="menu-card animate-fade">
            <div class="menu-img">
              <img :src="anhMacDinhMonLe" :alt="mon.tenMon" />
            </div>
            <div class="menu-info">
              <div class="header">
                <span class="name">{{ mon.tenMon }}</span>
                <span class="badge-danh-muc" v-if="mon.loaiDanhMuc">{{ mon.loaiDanhMuc }}</span>
                <div class="dots"></div>
                <span class="price">{{ Number(mon.donGiaHienTai).toLocaleString('vi-VN') }}đ</span>
              </div>
              <p class="desc">Món ăn tươi ngon đặc sản, được chế biến chuẩn vị từ đầu bếp nhà hàng.</p>
            </div>
          </div>

          <div v-if="monItems.length === 0" class="menu-trong">
            Hiện tại danh mục món lẻ đang được cập nhật, vui lòng quay lại sau!
          </div>
        </div>

        <div v-if="activeTab === 'combo'" class="menu-grid">
          <div v-for="cb in comboItems" :key="cb.idCombo" class="menu-card animate-fade">
            <div class="menu-img">
              <img 
                :src="cb.hinhAnh ? `http://localhost:8080/uploads/${cb.hinhAnh}` : anhMacDinhMonLe" 
                :alt="cb.tenCombo" 
              />
            </div>
            <div class="menu-info">
              <div class="header">
                <span class="name tags-combo">{{ cb.tenCombo }}</span>
                <div class="dots"></div>
                <span class="price">{{ Number(cb.giaCombo).toLocaleString('vi-VN') }}đ</span>
              </div>
              <p class="desc">Gói ẩm thực tiết kiệm kết hợp, phù hợp đi nhóm đông người hoặc gia đình.</p>
            </div>
          </div>

          <div v-if="comboItems.length === 0" class="menu-trong">
            Hiện tại nhà hàng đang cập nhật các gói Combo mới, vui lòng quay lại sau!
          </div>
        </div>
      </div>

    </div>
  </section>
</template>

<style scoped>
.menu-section {
  padding: 120px 10%;
  position: relative;
  color: #fff;
  background-image: url('https://images.unsplash.com/photo-1556910103-1c02745aae4d?q=80');
  background-attachment: fixed;
  background-position: center center;
  background-repeat: no-repeat;
  background-size: cover;
  will-change: background-position;
  overflow: hidden;
}

.menu-bg-overlay {
  position: absolute;
  inset: 0;
  background: rgba(10, 10, 10, 0.92);
  z-index: 1;
}

.menu-content-wrapper {
  position: relative;
  z-index: 2;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}
.section-header h2 {
  font-size: 3rem;
  font-family: 'Playfair Display', serif;
  margin-top: 10px;
  letter-spacing: 2px;
}
.subtitle {
  color: #c5a059;
  letter-spacing: 5px;
  font-size: 0.8rem;
}

/* THANH ĐIỀU HƯỚNG TAB */
.menu-tabs {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 35px;
}
.tab-btn {
  background: transparent;
  border: 1px solid rgba(197, 160, 89, 0.3);
  color: rgba(255, 255, 255, 0.6);
  padding: 10px 24px;
  font-size: 0.9rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s ease;
}
.tab-btn:hover {
  color: #c5a059;
  border-color: #c5a059;
}
.tab-btn.active {
  background: #c5a059;
  color: #101010;
  border-color: #c5a059;
  box-shadow: 0 4px 15px rgba(197, 160, 89, 0.3);
}

.menu-grid {
  display: grid;
  grid-template-cols: 1fr 1fr;
  gap: 50px 80px;
  max-width: 1400px;
  margin: 0 auto;
}
.menu-card {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}
.menu-img {
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.4);
  border-radius: 4px;
}
.menu-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.menu-card:hover .menu-img img {
  transform: scale(1.08);
}
.menu-info {
  flex: 1;
}
.header {
  display: flex;
  align-items: center; /* Giúp tên món, tag danh mục và dấu chấm thẳng hàng */
}
.name {
  font-size: 1rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  white-space: nowrap;
}
.tags-combo {
  color: #f8d46a;
}
/* Nhãn danh mục nhỏ gọn đính kèm bên cạnh tên món lẻ */
.badge-danh-muc {
  font-size: 0.7rem;
  background: rgba(197, 160, 89, 0.15);
  color: #c5a059;
  padding: 2px 8px;
  border-radius: 4px;
  margin-left: 8px;
  white-space: nowrap;
}
.dots {
  flex-grow: 1;
  border-bottom: 1px dotted rgba(197, 160, 89, 0.4);
  margin: 0 10px;
}
.price {
  color: #c5a059;
  font-weight: bold;
  white-space: nowrap;
}
.desc {
  font-size: 0.85rem;
  color: #aaa;
  font-style: italic;
  margin-top: 8px;
}

.menu-loading, .menu-trong {
  grid-column: 1 / -1;
  text-align: center;
  color: #888;
  padding: 40px;
  font-style: italic;
}
.menu-loading {
  color: #f8d46a;
}

/* Hiệu ứng mượt fade-in khi switch tab */
.animate-fade {
  animation: fadeIn 0.4s ease-in-out forwards;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 992px) {
  .menu-grid {
    grid-template-cols: 1fr;
    gap: 40px;
  }
  .menu-tabs {
    flex-direction: column;
    align-items: center;
    gap: 10px;
  }
  .tab-btn {
    width: 80%;
    max-width: 300px;
  }
}
</style>