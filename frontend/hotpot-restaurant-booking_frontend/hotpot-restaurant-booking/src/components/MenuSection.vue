<script setup lang="ts">
import { ref, onMounted } from 'vue'
import ComBoApi from '../api/ComBoApi'
import MonApi from '../api/MonApi'
import type { Combo } from '../api/ComBoApi'
import type { Mon } from '../api/MonApi'

// Khai báo trạng thái danh sách và Tab điều hướng
const monItems = ref<Mon[]>([])
const comboItems = ref<Combo[]>([])
const activeTab = ref<'mon-le' | 'combo'>('mon-le')
const loading = ref(false)

// 1. Biến quản lý phân trang cho Món lẻ
const pageNoMon = ref(0)
const pageSizeMon = ref(5)
const totalPagesMon = ref(0)

// 2. Biến quản lý phân trang cho Combo
const pageNoCombo = ref(0)
const pageSizeCombo = ref(5)
const totalPagesCombo = ref(0)

// Ảnh phôi mặc định sang trọng dành cho món lẻ không có ảnh
const anhMacDinhMonLe = 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?q=80'

// Hàm tải danh sách Món lẻ theo trang
const fetchMonByPage = async (pageMucTieu: number) => {
  pageNoMon.value = pageMucTieu
  loading.value = true
  try {
    const resMon = await MonApi.phanTrangMon(pageNoMon.value, pageSizeMon.value)
    const responseData = resMon.data as any

    if (responseData && responseData.content) {
      monItems.value = responseData.content.filter((m: Mon) => m.trangThai === 0)
      totalPagesMon.value = responseData.totalPages || 0
    } else {
      const dsMon = Array.isArray(responseData) ? responseData : []
      monItems.value = dsMon.filter((m: Mon) => m.trangThai === 0)
      totalPagesMon.value = 1
    }
  } catch (error) {
    console.error('Lỗi khi tải trang món lẻ:', error)
  } finally {
    loading.value = false
  }
}

// Hàm tải danh sách Combo theo trang
const fetchComboByPage = async (pageMucTieu: number) => {
  pageNoCombo.value = pageMucTieu
  loading.value = true
  try {
    const resCombo = await ComBoApi.phanTrangComBo(pageNoCombo.value, pageSizeCombo.value)
    const responseData = resCombo.data as any

    if (responseData && responseData.content) {
      comboItems.value = responseData.content.filter((cb: Combo) => cb.trangThai === 1)
      totalPagesCombo.value = responseData.totalPages || 0
    } else {
      const dsCombo = Array.isArray(responseData) ? responseData : []
      comboItems.value = dsCombo.filter((cb: Combo) => cb.trangThai === 1)
      totalPagesCombo.value = 1
    }
  } catch (error) {
    console.error('Lỗi khi tải trang combo:', error)
  } finally {
    loading.value = false
  }
}

// Hàm nạp lần đầu khi load component
const fetchThucDonTongHop = async () => {
  await Promise.all([fetchMonByPage(0), fetchComboByPage(0)])
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
        <div v-if="activeTab === 'mon-le'">
          <div class="menu-grid">
            <div v-for="mon in monItems" :key="mon.idMon" class="menu-card animate-fade">
              <div class="menu-img">
                <img :src="anhMacDinhMonLe" :alt="mon.tenMon" />
              </div>
              <div class="menu-info">
                <div class="header">
                  <span class="name">{{ mon.tenMon }}</span>
                  <span class="badge-danh-muc" v-if="mon.loaiDanhMuc">{{ mon.loaiDanhMuc }}</span>
                  <div class="dots"></div>
                  <div class="price-box">
                    <template v-if="mon.soTienDuocGiam > 0">
                      <div class="gia-goc">
                        {{ Number(mon.donGiaHienTai).toLocaleString('vi-VN') }}đ
                      </div>

                      <div class="gia-giam">
                        {{ Number(mon.giaSauGiam).toLocaleString('vi-VN') }}đ
                      </div>
                    </template>

                    <template v-else>
                      <div class="gia-thuong">
                        {{ Number(mon.donGiaHienTai).toLocaleString('vi-VN') }}đ
                      </div>
                    </template>

                  </div>
                </div>
                <p class="desc">
                  Món ăn tươi ngon đặc sản, được chế biến chuẩn vị từ đầu bếp nhà hàng.
                </p>
              </div>
            </div>
          </div>

          <div v-if="monItems.length === 0" class="menu-trong">
            Hiện tại danh mục món lẻ đang được cập nhật, vui lòng quay lại sau!
          </div>

          <div class="phan-trang animate-fade">
            <button class="btn-trang" :disabled="pageNoMon === 0" @click="fetchMonByPage(0)">
              &laquo;
            </button>
            <button
              class="btn-trang"
              :disabled="pageNoMon === 0"
              @click="fetchMonByPage(pageNoMon - 1)"
            >
              &#9664;
            </button>
            <span class="thong-tin-trang"
              >Trang <span class="so-trang-noi-bat">{{ pageNoMon + 1 }}</span> /
              {{ totalPagesMon }}</span
            >
            <button
              class="btn-trang vàng"
              :disabled="pageNoMon >= totalPagesMon - 1"
              @click="fetchMonByPage(pageNoMon + 1)"
            >
              &#9654;
            </button>
            <button
              class="btn-trang"
              :disabled="pageNoMon >= totalPagesMon - 1"
              @click="fetchMonByPage(totalPagesMon - 1)"
            >
              &raquo;
            </button>
          </div>
        </div>

        <div v-if="activeTab === 'combo'">
          <div class="menu-grid">
            <div v-for="cb in comboItems" :key="cb.idCombo" class="menu-card animate-fade">
              <div class="menu-img">
                <img
                  :src="
                    cb.hinhAnh ? `http://localhost:8080/uploads/${cb.hinhAnh}` : anhMacDinhMonLe
                  "
                  :alt="cb.tenCombo"
                />
              </div>
              <div class="menu-info">
                <div class="header">
                  <span class="name tags-combo">{{ cb.tenCombo }}</span>
                  <div class="dots"></div>
                  <span class="price">{{ Number(cb.giaCombo).toLocaleString('vi-VN') }}đ</span>
                </div>
                <p class="desc">
                  Gói ẩm thực tiết kiệm kết hợp, phù hợp đi nhóm đông người hoặc gia đình.
                </p>
              </div>
            </div>
          </div>

          <div v-if="comboItems.length === 0" class="menu-trong">
            Hiện tại nhà hàng đang cập nhật các gói Combo mới, vui lòng quay lại sau!
          </div>

          <div class="phan-trang animate-fade">
            <button class="btn-trang" :disabled="pageNoCombo === 0" @click="fetchComboByPage(0)">
              &laquo;
            </button>
            <button
              class="btn-trang"
              :disabled="pageNoCombo === 0"
              @click="fetchComboByPage(pageNoCombo - 1)"
            >
              &#9664;
            </button>
            <span class="thong-tin-trang"
              >Trang <span class="so-trang-noi-bat">{{ pageNoCombo + 1 }}</span> /
              {{ totalPagesCombo }}</span
            >
            <button
              class="btn-trang vàng"
              :disabled="pageNoCombo >= totalPagesCombo - 1"
              @click="fetchComboByPage(pageNoCombo + 1)"
            >
              &#9654;
            </button>
            <button
              class="btn-trang"
              :disabled="pageNoCombo >= totalPagesCombo - 1"
              @click="fetchComboByPage(totalPagesCombo - 1)"
            >
              &raquo;
            </button>
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
  align-items: center;
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

.menu-loading,
.menu-trong {
  grid-column: 1 / -1;
  text-align: center;
  color: #888;
  padding: 40px;
  font-style: italic;
}
.menu-loading {
  color: #f8d46a;
}

.animate-fade {
  animation: fadeIn 0.4s ease-in-out forwards;
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* --- CSS CỤM BỘ NÚT PHÂN TRANG THEO ẢNH MẪU --- */
.phan-trang {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 50px;
}

.btn-trang {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #888;
  padding: 10px 16px;
  border-radius: 12px;
  font-size: 0.85rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

/* Kiểu định dạng cho nút có mũi tên đang được chọn hoặc hoạt động tốt */
.btn-trang.vàng {
  color: #f8d46a;
  border-color: rgba(248, 212, 106, 0.2);
  background: rgba(248, 212, 106, 0.05);
}

.btn-trang:hover:not(:disabled) {
  border-color: #f8d46a;
  color: #f8d46a;
  background: rgba(248, 212, 106, 0.08);
}

.btn-trang:disabled {
  border-color: rgba(255, 255, 255, 0.03);
  color: rgba(255, 255, 255, 0.1);
  background: transparent;
  cursor: not-allowed;
}

.thong-tin-trang {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.95rem;
  letter-spacing: 0.5px;
  margin: 0 8px;
}

.so-trang-noi-bat {
  color: #f8d46a;
  font-weight: 600;
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
.price-box {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.gia-goc {
  text-decoration: line-through;
  color: red;
  font-size: 0.85rem;
}

.gia-giam {
  color: #c5a059;
  font-weight: bold;
  font-size: 1rem;
}
.gia-thuong {
  color: #c5a059;
  font-weight: bold;
  font-size: 1rem;
}
</style>
