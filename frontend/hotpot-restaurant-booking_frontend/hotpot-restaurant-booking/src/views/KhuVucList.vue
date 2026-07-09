<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  getAllKhuVuc,
  createKhuVuc,
  updateKhuVuc,
  deleteKhuVuc,
  changeStatusKhuVuc,
} from '../api/khuvuc'

const router = useRouter()

// Các trạng thái dữ liệu
const danhSachKhuVuc = ref<any[]>([])
const loading = ref(true)
const keyword = ref('')
const filterTrangThai = ref('ALL')

// Biến phục vụ cho Form Thêm / Sửa
const isEditing = ref(false)
const currentId = ref<number | null>(null)
const formKhuVuc = ref({
  maKhuVuc: '',
  tenKhuVuc: '',
  moTa: '',
})

// Biến điều khiển Modal
const showDetailModal = ref(false)
const selectedKhuVuc = ref<any>(null)

const goToHome = () => {
  router.push('/')
}

// 1. Tải dữ liệu
const loadData = async () => {
  loading.value = true
  try {
    const res = await getAllKhuVuc()
    const dataGoc = res.data || []

    danhSachKhuVuc.value = dataGoc.filter((item: any) => {
      const matchesKeyword =
        !keyword.value.trim() ||
        (item.tenKhuVuc || '').toLowerCase().includes(keyword.value.toLowerCase()) ||
        (item.moTa || '').toLowerCase().includes(keyword.value.toLowerCase())

      let matchesStatus = true
      const tt = item.trangThai
      if (filterTrangThai.value === 'ACTIVE') {
        matchesStatus = tt === 1 || tt === true
      } else if (filterTrangThai.value === 'LOCKED') {
        matchesStatus = tt === 0 || tt === false
      }

      return matchesKeyword && matchesStatus
    })
  } catch (error) {
    console.error('Lỗi khi tải danh sách khu vực:', error)
    danhSachKhuVuc.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

const handleSearch = () => {
  loadData()
}

const handleViewDetail = (item: any) => {
  selectedKhuVuc.value = item
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedKhuVuc.value = null
}

const handleSave = async () => {
  if (!formKhuVuc.value.maKhuVuc) {
    alert('Vui lòng nhập mã khu vực!')
    return
  }

  if (!formKhuVuc.value.tenKhuVuc) {
    alert('Vui lòng nhập tên khu vực!')
    return
  }

  try {
    const payload = {
      maKhuVuc: formKhuVuc.value.maKhuVuc,
      tenKhuVuc: formKhuVuc.value.tenKhuVuc,
      moTa: formKhuVuc.value.moTa || null,
      trangThai: 1,
    }
    if (isEditing.value && currentId.value !== null) {
      await updateKhuVuc(currentId.value, { id: currentId.value, ...payload })
      alert('Cập nhật khu vực thành công!')
    } else {
      await createKhuVuc(payload)
      alert('Thêm mới khu vực thành công!')
    }
    resetForm()
    loadData()
  } catch (error) {
    console.error('Lỗi khi lưu khu vực:', error)
    alert('Xảy ra lỗi, vui lòng kiểm tra lại!')
  }
}

const handleEdit = (item: any) => {
  isEditing.value = true
  currentId.value = item.id
  formKhuVuc.value = {
    maKhuVuc: item.maKhuVuc || '',
    tenKhuVuc: item.tenKhuVuc || '',
    moTa: item.moTa || '',
  }
}

const handleDoiTrangThai = async (id: number) => {
  try {
    await changeStatusKhuVuc(id)
    loadData()
  } catch (error) {
    console.error('Lỗi khi đổi trạng thái:', error)
    alert('Không thể thay đổi trạng thái!')
  }
}

const handleXoa = async (id: number) => {
  if (confirm('Bạn có chắc chắn muốn xóa khu vực này?')) {
    try {
      await deleteKhuVuc(id)
      alert('Xóa khu vực thành công!')
      loadData()
    } catch (error) {
      console.error('Lỗi khi xóa:', error)
      alert('Không thể xóa khu vực này!')
    }
  }
}

const resetForm = () => {
  isEditing.value = false
  currentId.value = null
  formKhuVuc.value = {
    maKhuVuc: '',
    tenKhuVuc: '',
    moTa: '',
  }
}

const resetFilter = () => {
  keyword.value = ''
  filterTrangThai.value = 'ALL'
  loadData()
}
</script>

<template>
  <div class="khu-vuc-page">
    <div class="page-header-wrapper">
      <h2>👑 QUẢN LÝ KHU VỰC NHÀ HÀNG</h2>
      <button class="btn-back-home" @click="goToHome">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
          <polyline points="9 22 9 12 15 12 15 22" />
        </svg>
        Quay về trang chủ
      </button>
    </div>

    <hr class="line-break" />

    <div class="search-section">
      <input
        v-model="keyword"
        type="text"
        placeholder="Nhập tên khu vực cần tìm..."
        @keyup.enter="handleSearch"
      />
      <select v-model="filterTrangThai" class="select-classic" @change="handleSearch">
        <option value="ALL">-- Tất cả trạng thái --</option>
        <option value="ACTIVE">Hoạt động</option>
        <option value="LOCKED">Đang khóa</option>
      </select>
      <button class="btn-yellow" @click="handleSearch">Tìm kiếm</button>
      <button class="btn-gray" @click="resetFilter">Làm mới bộ lọc</button>
    </div>

    <div class="main-layout">
      <div class="form-container">
        <h3 class="form-title">{{ isEditing ? '📝 CẬP NHẬT KHU VỰC' : '➕ THÊM KHU VỰC MỚI' }}</h3>
        <div class="form-row">
          <label class="form-label">Mã Khu Vực *</label>
          <input v-model="formKhuVuc.maKhuVuc" type="text" placeholder="Ví dụ: A, B, C..." />
        </div>
        <div class="form-row">
          <label class="form-label">Tên Khu Vực *</label>
          <input
            v-model="formKhuVuc.tenKhuVuc"
            type="text"
            placeholder="Ví dụ: Tầng 1, Sân vườn..."
          />
        </div>

        <div class="form-row" style="align-items: flex-start">
          <label class="form-label" style="padding-top: 5px">Mô Tả / Ghi Chú</label>
          <textarea
            v-model="formKhuVuc.moTa"
            rows="3"
            placeholder="Nhập mô tả khu vực..."
          ></textarea>
        </div>
        <div class="form-buttons">
          <button class="btn-submit" @click="handleSave">
            {{ isEditing ? 'Cập nhật' : 'Thêm mới' }}
          </button>
          <button class="btn-gray" @click="resetForm">Hủy bỏ</button>
        </div>
      </div>

      <div class="table-container">
        <div v-if="loading" class="text-loading">🔄 Đang tải dữ liệu...</div>
        <table v-else class="table-classic">
          <thead>
            <tr>
              <th style="width: 60px; text-align: center">ID</th>
              <th>Mã Khu Vực</th>
              <th>Tên Khu Vực</th>
              <th>Mô Tả / Ghi Chú</th>
              <th style="width: 140px; text-align: center">Trạng Thái</th>
              <th style="width: 180px; text-align: center">Hành Động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in danhSachKhuVuc" :key="item.id">
              <td style="text-align: center; color: #888">{{ item.id }}</td>
              <td class="highlight-text">{{ item.maKhuVuc }}</td>
              <td class="highlight-text">{{ item.tenKhuVuc }}</td>
              <td>{{ item.moTa || '---' }}</td>
              <td style="text-align: center">
                <button
                  class="btn-status"
                  :class="item.trangThai === 1 || item.trangThai === true ? 'active' : 'locked'"
                  @click="handleDoiTrangThai(item.id)"
                >
                  {{
                    item.trangThai === 1 || item.trangThai === true ? '● Hoạt động' : '○ Đang khóa'
                  }}
                </button>
              </td>
              <td style="text-align: center">
                <button class="btn-action view" @click="handleViewDetail(item)">Xem</button>
                <button class="btn-action edit" @click="handleEdit(item)">Sửa</button>
                <button class="btn-action delete" @click="handleXoa(item.id)">Xóa</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>🔍 CHI TIẾT PHÂN KHU: {{ selectedKhuVuc?.tenKhuVuc }}</h3>
          <span class="close-btn" @click="closeDetailModal">&times;</span>
        </div>
        <div class="modal-body" v-if="selectedKhuVuc">
          <div class="detail-section-title">📌 Thông tin Tổng Quan</div>
          <table class="detail-table">
            <tbody>
              <tr>
                <td class="lbl">Mã Hệ Thống (ID):</td>
                <td class="val">#{{ selectedKhuVuc.id }}</td>
              </tr>
              <tr>
                <td class="lbl">Mã khu vực:</td>
                <td class="val">{{ selectedKhuVuc.maKhuVuc }}</td>
              </tr>
              <tr>
                <td class="lbl">Tên Khu Vực:</td>
                <td class="val highlight-text">{{ selectedKhuVuc.tenKhuVuc }}</td>
              </tr>
              <tr>
                <td class="lbl">Mô Tả:</td>
                <td class="val text-blue">{{ selectedKhuVuc.moTa || 'Không có mô tả.' }}</td>
              </tr>
            </tbody>
          </table>
          <div class="detail-section-title">🪑 Sơ Đồ Loại Bàn</div>
          <div class="booking-history-container">
            <div
              v-for="(ban, index) in selectedKhuVuc.banList"
              :key="ban.id"
              class="booking-item-card"
            >
              <table class="detail-table style-compact">
                <tbody>
                  <tr>
                    <td class="lbl">Loại bàn:</td>
                    <td class="val gold-text">{{ ban.loaiBan }}</td>
                  </tr>
                  <tr>
                    <td class="lbl">Số lượng:</td>
                    <td class="val">
                      <span class="badge-count">{{ ban.soLuongBan }}</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-gray" @click="closeDetailModal">Đóng lại</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.khu-vuc-page {
  padding: 25px;
  background: #121212;
  min-height: 100vh;
  color: #fff;
  font-family: Arial, sans-serif;
}

.page-header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

h2 {
  color: #ffc107;
  font-size: 28px;
  font-weight: bold;
}

.line-break {
  border: none;
  border-top: 1px solid #333;
  margin: 20px 0 25px;
}

/* =======================
        BUTTON
======================= */

.btn-back-home,
.btn-yellow,
.btn-gray,
.btn-submit,
.btn-action {
  transition: 0.2s;
}

.btn-back-home {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #1f1f1f;
  color: white;
  border: 1px solid #444;
  padding: 9px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
}

.btn-back-home:hover {
  background: #333;
}

.btn-yellow {
  background: #ffc107;
  color: #000;
  border: none;
  padding: 9px 18px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
}

.btn-yellow:hover {
  opacity: 0.9;
}

.btn-gray {
  background: #555;
  color: white;
  border: none;
  padding: 9px 18px;
  border-radius: 6px;
  cursor: pointer;
}

.btn-submit {
  background: #28a745;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
}

.btn-submit:hover {
  background: #218838;
}

/* =======================
      SEARCH
======================= */

.search-section {
  display: flex;
  gap: 10px;
  margin-bottom: 25px;
  flex-wrap: wrap;
}

.search-section input,
.select-classic {
  background: #1f1f1f;
  color: white;
  border: 1px solid #444;
  border-radius: 6px;
  padding: 10px;
  min-width: 220px;
}

.search-section input:focus,
.select-classic:focus {
  outline: none;
  border-color: #ffc107;
}

/* =======================
      LAYOUT
======================= */

.main-layout {
  display: flex;
  gap: 25px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.form-container,
.table-container {
  background: #1a1a1a;
  border: 1px solid #2f2f2f;
  border-radius: 10px;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.25);
}

.form-container {
  flex: 1;
  min-width: 330px;
  padding: 22px;
}

.table-container {
  flex: 2;
  overflow: hidden;
}

.form-title {
  color: #ffc107;
  margin-bottom: 20px;
}

.form-row {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  margin-bottom: 6px;
  font-weight: bold;
  color: #ddd;
}

.form-row input,
.form-row textarea {
  width: 100%;
  box-sizing: border-box;
  background: #222;
  color: white;
  border: 1px solid #444;
  border-radius: 6px;
  padding: 10px;
}

.form-row input:focus,
.form-row textarea:focus {
  outline: none;
  border-color: #ffc107;
}

.form-buttons {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

/* =======================
      TABLE
======================= */

.table-classic {
  width: 100%;
  border-collapse: collapse;
}

.table-classic thead {
  background: #2b2b2b;
}

.table-classic th {
  color: #ffc107;
  padding: 14px;
}

.table-classic td {
  padding: 13px;
  border-top: 1px solid #2f2f2f;
}

.table-classic tbody tr {
  transition: 0.2s;
}

.table-classic tbody tr:hover {
  background: #232323;
}

.highlight-text {
  color: #ffc107;
  font-weight: bold;
}

/* =======================
      BUTTON TABLE
======================= */

.btn-action {
  border: none;
  border-radius: 5px;
  padding: 6px 12px;
  margin: 0 3px;
  cursor: pointer;
  color: white;
}

.view {
  background: #17a2b8;
}

.edit {
  background: #007bff;
}

.delete {
  background: #dc3545;
}

.btn-action:hover {
  filter: brightness(1.1);
}

/* =======================
      STATUS
======================= */

.btn-status {
  border: none;
  border-radius: 30px;
  padding: 6px 14px;
  cursor: pointer;
  font-weight: bold;
}

.active {
  background: #28a745;
  color: white;
}

.locked {
  background: #6c757d;
  color: white;
}

/* =======================
      MODAL
======================= */

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  width: 600px;
  max-width: 95%;
  background: #1c1c1c;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #444;
}

.modal-header {
  background: #2a2a2a;
  color: #ffc107;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.close-btn {
  font-size: 28px;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
}

.detail-section-title {
  margin: 15px 0 10px;
  color: #ffc107;
  font-weight: bold;
}

.detail-table {
  width: 100%;
  border-collapse: collapse;
}

.detail-table td {
  padding: 8px 5px;
}

.lbl {
  width: 180px;
  color: #9c9c9c;
}

.val {
  color: white;
}

.text-blue {
  color: #4fc3f7;
}

.gold-text {
  color: #ffc107;
}

.booking-item-card {
  border: 1px solid #333;
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 10px;
  background: #202020;
}

.badge-count {
  background: #ffc107;
  color: #000;
  padding: 3px 8px;
  border-radius: 20px;
  font-weight: bold;
}

.text-loading {
  text-align: center;
  padding: 40px;
}
</style>
