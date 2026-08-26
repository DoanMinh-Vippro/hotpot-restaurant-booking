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
  if (!formKhuVuc.value.maKhuVuc || !formKhuVuc.value.tenKhuVuc) {
    alert('Vui lòng nhập đầy đủ Mã và Tên khu vực!')
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
  currentId.value = item.idKhuVuc
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
            <tr v-for="item in danhSachKhuVuc" :key="item.idKhuVuc">
              <td style="text-align: center; color: #888">{{ item.idKhuVuc }}</td>
              <td class="highlight-text">{{ item.maKhuVuc }}</td>
              <td class="highlight-text">{{ item.tenKhuVuc }}</td>
              <td>{{ item.moTa || '---' }}</td>
              <td style="text-align: center">
                <button
                  class="btn-status"
                  :class="item.trangThai === 1 || item.trangThai === true ? 'active' : 'locked'"
                  @click="handleDoiTrangThai(item.idKhuVuc)"
                >
                  {{
                    item.trangThai === 1 || item.trangThai === true ? '● Hoạt động' : '○ Đang khóa'
                  }}
                </button>
              </td>
              <td style="text-align: center">
                <button class="btn-action view" @click="handleViewDetail(item)">Xem</button>
                <button class="btn-action edit" @click="handleEdit(item)">Sửa</button>
                <button class="btn-action delete" @click="handleXoa(item.idKhuVuc)">Xóa</button>
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
                <td class="val">#{{ selectedKhuVuc.idKhuVuc }}</td>
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
        </div>
        <div class="modal-footer" style="padding: 15px; text-align: right">
          <button class="btn-gray" @click="closeDetailModal">Đóng lại</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.khu-vuc-page {
  padding: 25px;
  background: linear-gradient(135deg, #f9efe0 0%, #f4e4c6 100%);
  min-height: 100vh;
  color: #5f3d22;
  font-family: Arial, sans-serif;
}

h2 {
  color: #8b5e34;
  font-size: 28px;
  font-weight: bold;
}
.line-break {
  border: none;
  border-top: 1px solid #e6d2aa;
  margin: 20px 0 25px;
}

.search-section {
  display: flex;
  gap: 10px;
  margin-bottom: 25px;
}
.search-section input,
.select-classic {
  background-color: #fffaf1;
  color: #5f3d22;
  border: 1px solid #e6d2aa;
  padding: 10px;
  border-radius: 6px;
  min-width: 220px;
}

.main-layout {
  display: flex;
  gap: 25px;
  align-items: flex-start;
  flex-wrap: wrap;
}
.form-container {
  flex: 1;
  min-width: 350px;
  background: rgba(255, 248, 234, 0.95);
  padding: 20px;
  border: 1px solid #e6d2aa;
  border-radius: 12px;
}
.table-container {
  flex: 2;
  overflow: hidden;
  background: rgba(255, 248, 234, 0.95);
  border-radius: 12px;
}

.form-title {
  color: #8b5e34;
  margin-bottom: 20px;
}
.form-row {
  margin-bottom: 16px;
}
.form-label {
  display: block;
  margin-bottom: 6px;
  font-weight: bold;
}
.form-row input,
.form-row textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 10px;
  border: 1px solid #e6d2aa;
  border-radius: 6px;
}

.btn-yellow {
  background: #ffc107;
  color: #000;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
}
.btn-submit {
  background: #4b7c45;
  color: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
}
.btn-gray {
  background: #888;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
}

.table-classic {
  width: 100%;
  border-collapse: collapse;
}
.table-classic th {
  background: #f3dfb4;
  color: #8b5e34;
  padding: 12px;
  text-align: left;
}
.table-classic td {
  padding: 12px;
  border-bottom: 1px solid #efe0c1;
}

.btn-status {
  border: none;
  border-radius: 30px;
  padding: 6px 14px;
  cursor: pointer;
  font-weight: bold;
}
.active {
  background: #4b7c45;
  color: white;
}
.locked {
  background: #c98b3e;
  color: white;
}

.btn-action {
  border: none;
  border-radius: 999px;
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

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
}
.modal-content {
  background: #fffaf1;
  padding: 20px;
  border-radius: 8px;
  width: 500px;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.text-loading {
  text-align: center;
  padding: 40px;
  font-weight: bold;
}
</style>
