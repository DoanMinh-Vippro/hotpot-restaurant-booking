<script setup lang="ts">
import { ref, onMounted } from 'vue'
// Import thêm useRouter từ vue-router để xử lý điều hướng
import { useRouter } from 'vue-router'
// Đường dẫn tương đối từ thư mục views ra src rồi vào api
import { 
  getAllKhachHang, 
  createKhachHang, 
  updateKhachHang, 
  deleteKhachHang
} from '../api/khachhang'

const router = useRouter()

// Trạng thái dữ liệu danh sách
const danhSachKhachHang = ref<any[]>([])
const loading = ref(true)
const keyword = ref('')
const filterTrangThai = ref('ALL') // Bộ lọc trạng thái hồ sơ: 'ALL', 'ACTIVE', 'INACTIVE'

// Biến điều khiển Form nhập liệu (Thêm/Sửa)
const isEditing = ref(false)
const currentId = ref<number | null>(null)
const formKhachHang = ref({
  tenKhachHang: '',
  soDienThoai: '',
  email: ''
})

// === BIẾN ĐIỀU KHIỂN XEM CHI TIẾT (MODAL) ===
const showDetailModal = ref(false)
const selectedKhachHang = ref<any>(null) // Lưu trữ thông tin khách hàng đang được chọn xem
  
// Hàm điều hướng quay lại trang chủ
const goToHome = () => {
  router.push('/') // Bạn sửa lại thành '/' nếu route trang chủ của bạn đặt là dấu gạch chéo nhé
}

// 1. Hàm tải danh sách khách hàng và lọc dữ liệu
const loadData = async () => {
  loading.value = true
  try {
    const res = await getAllKhachHang()
    const dataGoc = res.data || []

    // Xử lý bộ lọc dựa trên trường trạng thái hoạt động hoặc từ khóa
    danhSachKhachHang.value = dataGoc.filter((kh: any) => {
      const matchesKeyword = !keyword.value.trim() || 
        (kh.tenKhachHang || '').toLowerCase().includes(keyword.value.toLowerCase()) ||
        (kh.soDienThoai || '').includes(keyword.value)

      let matchesStatus = true
      const tt = kh.trangThai

      if (filterTrangThai.value === 'ACTIVE') {
        matchesStatus = tt === true || tt === 1 || String(kh.trangThaiText).includes('Còn')
      } else if (filterTrangThai.value === 'INACTIVE') {
        matchesStatus = tt === false || tt === 0 || String(kh.trangThaiText).includes('Hết')
      }

      return matchesKeyword && matchesStatus
    })
  } catch (error) {
    console.error('Lỗi khi tải danh sách khách hàng:', error)
    danhSachKhachHang.value = []
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

// 2. Hàm kích hoạt bật Modal Xem chi tiết đặt bàn
const handleViewDetail = (item: any) => {
  selectedKhachHang.value = item
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedKhachHang.value = null
}

// 3. Hàm xử lý nút Lưu (Thêm mới hoặc Sửa)
const handleSave = async () => {
  if (!formKhachHang.value.tenKhachHang || !formKhachHang.value.soDienThoai) {
    alert('Vui lòng nhập đầy đủ Tên và Số điện thoại!')
    return
  }

  try {
    if (isEditing.value && currentId.value !== null) {
      const updatePayload = {
        id: currentId.value,
        tenKhachHang: formKhachHang.value.tenKhachHang,
        soDienThoai: formKhachHang.value.soDienThoai,
        email: formKhachHang.value.email || null
      }
      await updateKhachHang(currentId.value, updatePayload)
      alert('Cập nhật thông tin khách hàng thành công!')
    } else {
      const createPayload = {
        tenKhachHang: formKhachHang.value.tenKhachHang,
        soDienThoai: formKhachHang.value.soDienThoai,
        email: formKhachHang.value.email || null
      }
      await createKhachHang(createPayload)
      alert('Thêm khách hàng mới thành công!')
    }
    resetForm()
    loadData()
  } catch (error) {
    console.error('Lỗi khi thao tác dữ liệu:', error)
    alert('Không thể lưu dữ liệu!')
  }
}

// 4. Hàm kích hoạt chế độ sửa
const handleEdit = (item: any) => {
  isEditing.value = true
  currentId.value = item.id
  formKhachHang.value = {
    tenKhachHang: item.tenKhachHang || '',
    soDienThoai: item.soDienThoai || '',
    email: item.email || ''
  }
}

// 5. Hàm xóa khách hàng
const handleXoa = async (id: number) => {
  if (confirm('Bạn có chắc chắn muốn xóa khách hàng này?')) {
    try {
      await deleteKhachHang(id)
      alert('Xóa khách hàng thành công!')
      loadData()
    } catch (error) {
      console.error('Lỗi khi xóa khách hàng:', error)
      alert('Không thể xóa khách hàng này!')
    }
  }
}

const resetForm = () => {
  isEditing.value = false
  currentId.value = null
  formKhachHang.value = { tenKhachHang: '', soDienThoai: '', email: '' }
}

const resetFilter = () => {
  keyword.value = ''
  filterTrangThai.value = 'ALL'
  loadData()
}

// Hàm hỗ trợ format tiền tệ VNĐ chuẩn chỉ
const formatCurrency = (value: any) => {
  if (value === undefined || value === null || value === '') return '0 đ'
  return Number(value).toLocaleString('vi-VN') + ' đ'
}

// Hàm format chuỗi thời gian đến dự kiến sang giao diện dễ nhìn
const formatDateTime = (dateTimeStr: string) => {
  if (!dateTimeStr) return '---'
  const date = new Date(dateTimeStr)
  if (isNaN(date.getTime())) return dateTimeStr
  return `${date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} ngày ${date.toLocaleDateString('vi-VN')}`
}
</script>

<template>
  <div class="khach-hang-page">
    <div class="page-header-wrapper">
    <h2>👑 QUẢN LÝ DANH SÁCH KHÁCH HÀNG</h2>
          
    <button class="btn-back-home" @click="goToHome">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          <polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
        Quay về trang chủ
      </button>
    </div>
    
    <hr class="line-break" />

    <div class="search-section">
      <input 
        v-model="keyword" 
        type="text" 
        placeholder="Tìm tên hoặc số điện thoại..."
        @keyup.enter="handleSearch"
      />
      
      <select v-model="filterTrangThai" class="select-classic" @change="handleSearch">
        <option value="ALL">-- Tất cả trạng thái --</option>
        <option value="ACTIVE">Còn hoạt động</option>
        <option value="INACTIVE">Hết HĐ</option>
      </select>

      <button class="btn-yellow" @click="handleSearch">Tìm kiếm</button>
      <button class="btn-gray" @click="resetFilter">Làm mới</button>
    </div>

    <div class="form-container">
      <h3 class="form-title">{{ isEditing ? '📝 CẬP NHẬT THÔNG TIN' : '➕ THÊM KHÁCH HÀNG' }}</h3>
      
      <div class="form-row">
        <label class="form-label">Tên Khách Hàng *</label>
        <input v-model="formKhachHang.tenKhachHang" type="text" placeholder="Nhập tên khách hàng" />
      </div>

      <div class="form-row">
        <label class="form-label">Số Điện Thoại *</label>
        <input v-model="formKhachHang.soDienThoai" type="text" placeholder="Nhập số điện thoại" />
      </div>

      <div class="form-row">
        <label class="form-label">Email (Nếu có)</label>
        <input v-model="formKhachHang.email" type="email" placeholder="viethung@gmail.com" />
      </div>

      <div class="form-buttons">
        <button class="btn-submit" @click="handleSave">
          {{ isEditing ? 'Cập nhật' : 'Thêm mới' }}
        </button>
        <button class="btn-gray" @click="resetForm">Hủy bỏ</button>
      </div>
    </div>
      
    <div class="table-container">
      <div v-if="loading" class="text-loading">
        🔄 Đang tải dữ liệu từ Backend Spring Boot...
      </div>

      <table v-else class="table-classic">
        <thead>
          <tr>
            <th style="width: 60px; text-align: center;">ID</th>
            <th style="width: 100px;">Mã KH</th>
            <th>Tên Khách Hàng</th>
            <th style="width: 150px;">Số Điện Thoại</th>
            <th>Email</th>
            <th style="width: 160px; text-align: center;">Trạng Thái</th>
            <th style="width: 200px; text-align: center;">Hành Động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in danhSachKhachHang" :key="item.id">
            <td style="text-align: center; color: #888;">{{ item.id }}</td>
            <td style="font-weight: bold; color: #aaa;">{{ item.maKhachHang }}</td>
            <td class="highlight-text">{{ item.tenKhachHang }}</td>
            <td>{{ item.soDienThoai }}</td>
            <td style="color: #599fff;">{{ item.email || '---' }}</td>
            
            <td style="text-align: center; font-weight: bold;">
              <span :class="item.trangThai ? 'status-green' : 'status-red'">
                {{ item.trangThai ? 'Còn hoạt động' : 'Hết HĐ' }}
              </span>
            </td>

            <td style="text-align: center;">
              <button class="btn-action view" @click="handleViewDetail(item)">Xem</button>
              <button class="btn-action edit" @click="handleEdit(item)">Sửa</button>
              <button class="btn-action delete" @click="handleXoa(item.id)">Xóa</button>
            </td>
          </tr>
          <tr v-if="danhSachKhachHang.length === 0">
            <td colspan="7" style="text-align: center; color: #999; padding: 30px 0;">
              📭 Không có dữ liệu khách hàng trùng khớp.
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>🔍 CHI TIẾT HỒ SƠ KHÁCH HÀNG (#{{ selectedKhachHang?.maKhachHang }})</h3>
          <span class="close-btn" @click="closeDetailModal">&times;</span>
        </div>
        
        <div class="modal-body" v-if="selectedKhachHang">
          <div class="detail-section-title">👤 Thông Tin Cá Nhân</div>
          <table class="detail-table">
            <tr>
              <td class="lbl">Tên Khách Hàng:</td>
              <td class="val highlight-text">{{ selectedKhachHang.tenKhachHang }}</td>
            </tr>
            <tr>
              <td class="lbl">Số Điện Thoại:</td>
              <td class="val gold-text">{{ selectedKhachHang.soDienThoai }}</td>
            </tr>
            <tr>
              <td class="lbl">Địa Chỉ Email:</td>
              <td class="val text-blue">{{ selectedKhachHang.email || 'Chưa cung cấp' }}</td>
            </tr>
            <tr>
              <td class="lbl">Giới Tính / Địa Chỉ:</td>
              <td class="val">
                {{ selectedKhachHang.gioiTinh ? 'Nam' : 'Nữ' }} — {{ selectedKhachHang.diaChi || '---' }}
              </td>
            </tr>
            <tr>
              <td class="lbl">Trạng Thái Hồ Sơ:</td>
              <td class="val">
                <span :class="selectedKhachHang.trangThai ? 'status-green' : 'status-red'" style="font-weight: bold;">
                  {{ selectedKhachHang.trangThai ? 'Còn hoạt động' : 'Hết HĐ' }}
                </span>
              </td>
            </tr>
            <tr v-if="selectedKhachHang.taiKhoan">
              <td class="lbl">Tài Khoản Hệ Thống:</td>
              <td class="val" style="color: #20c997; font-weight: bold;">
                {{ selectedKhachHang.taiKhoan.tenDangNhap }} (Mã: {{ selectedKhachHang.taiKhoan.maTaiKhoan }})
              </td>
            </tr>
          </table>

          <div class="detail-section-title">🍲 Lịch Sử Đặt Bàn & Tiền Cọc</div>
          
          <div v-if="!selectedKhachHang.datBanList || selectedKhachHang.datBanList.length === 0" class="no-booking-text">
            📭 Khách hàng này chưa từng đặt bàn trên hệ thống.
          </div>
          
          <div v-else class="booking-history-container">
            <div v-for="(booking, index) in selectedKhachHang.datBanList" :key="booking.id" class="booking-item-card">
              <div class="booking-item-header">
                <span>Đơn Đặt Bàn #{{ booking.id }} (Lần {{ Number(index) + 1 }})</span>
                <span :class="booking.trangThai === 1 ? 'status-green' : 'status-red'" style="font-size: 11px;">
                  ● {{ booking.trangThaiText || (booking.trangThai === 1 ? 'Đã xác nhận' : 'Chờ xử lý') }}
                </span>
              </div>
              
              <table class="detail-table style-compact">
                <tr>
                  <td class="lbl">Thời Gian Đến:</td>
                  <td class="val text-blue">{{ formatDateTime(booking.thoiGianDenDuKien) }}</td>
                </tr>
                <tr>
                  <td class="lbl">Ngày / Giờ Đặt:</td>
                  <td class="val">{{ booking.ngayDat }} lúc {{ booking.gioDat }}</td>
                </tr>
                <tr>
                  <td class="lbl">Số Lượng Người:</td>
                  <td class="val"><span class="badge-count">{{ booking.soNguoi }} Người</span></td>
                </tr>
                <tr>
                  <td class="lbl">Vị Trí Bàn / Ghi Chú:</td>
                  <td class="val">{{ booking.tenBan || 'Tự động xếp' }} <span v-if="booking.ghiChu" style="color:#aaa;">— "{{ booking.ghiChu }}"</span></td>
                </tr>
                <tr>
                  <td class="lbl">Thông Tin Tiền Cọc:</td>
                  <td class="val gold-text">
                    {{ formatCurrency(booking.soTienCoc) }} 
                    <span :class="booking.trangThaiCoc === 1 ? 'status-green' : 'status-red'" style="font-size: 12px; margin-left: 5px;">
                      ({{ booking.trangThaiCoc === 1 ? 'Đã cọc' : 'Chưa cọc' }})
                    </span>
                  </td>
                </tr>
                <tr>
                  <td class="lbl">Thanh Toán Qua:</td>
                  <td class="val">
                    <span class="badge-method" v-if="booking.phuongThucThanhToan">{{ booking.phuongThucThanhToan }}</span>
                    <span v-else class="badge-method gray">Chưa chọn</span>
                    <span v-if="booking.maGiaoDich" class="code-text" style="margin-left: 10px;">ID: {{ booking.maGiaoDich }}</span>
                  </td>
                </tr>
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
.khach-hang-page {
  padding: 20px;
  background-color: #121212;
  min-height: 100vh;
  font-family: Arial, sans-serif;
  color: #ffffff;
}

/* THÊM MỚI: Flexbox CSS cho vùng header của trang */
.page-header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

/* THÊM MỚI: Định dạng nút bấm Quay về trang chủ */
.btn-back-home {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: #1e1e1e;
  color: #ffffff;
  border: 1px solid #444;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-back-home:hover {
  background-color: #2a2a2a;
  border-color: #ffc107;
  color: #ffc107;
}

.btn-back-home svg {
  transition: transform 0.2s ease;
}

.btn-back-home:hover svg {
  transform: scale(1.1);
}

h2 { color: #ffc107; margin: 0; }
h2 { color: #ffc107; margin-top: 0; }
.line-break { border: 0; border-top: 1px solid #333; margin-bottom: 20px; }

/* Tìm kiếm & Select */
.search-section { margin-bottom: 25px; display: flex; align-items: center; }
.search-section input { background-color: #222; color: #fff; border: 1px solid #555; padding: 7px 12px; border-radius: 4px; width: 250px; margin-right: 10px; }
.select-classic { background-color: #222; color: #fff; border: 1px solid #555; padding: 7px 12px; border-radius: 4px; margin-right: 10px; cursor: pointer; }

/* Khung Form */
.form-container { background-color: #1a1a1a; padding: 20px; border: 1px solid #333; border-radius: 6px; width: 450px; margin-bottom: 30px; }
.form-title { color: #ffc107; margin-top: 0; margin-bottom: 15px; font-size: 15px; }
.form-row { margin-bottom: 12px; display: flex; align-items: center; }
.form-label { width: 140px; font-size: 14px; color: #ccc; }
.form-row input { flex: 1; background-color: #2b3035; color: #fff; border: 1px solid #555; padding: 6px 10px; border-radius: 4px; }
.form-buttons { margin-left: 140px; display: flex; gap: 10px; }

.status-green { color: #198754; }
.status-red { color: #dc3545; }

/* Bảng */
.table-classic { width: 100%; border-collapse: collapse; background-color: #1a1a1a; border: 1px solid #333; }
.table-classic th { background-color: #2a2a2a; color: #ffc107; padding: 10px; text-align: left; border-bottom: 2px solid #444; }
.table-classic td { padding: 10px; border-bottom: 1px solid #2a2a2a; }
.table-classic tr:hover { background-color: #222; }
.highlight-text { font-weight: bold; color: #ffc107; }

/* Nút bấm */
button { padding: 6px 14px; border-radius: 4px; font-weight: bold; cursor: pointer; border: none; margin-right: 5px; }
.btn-yellow { background-color: #ffc107; color: #000; }
.btn-gray { background-color: #555; color: #fff; }
.btn-submit { background-color: #198754; color: #fff; }
.btn-action { padding: 3px 8px; font-size: 12px; background: transparent; border: 1px solid; margin-right: 5px; }

.view { border-color: #0d6efd; color: #0d6efd; }
.view:hover { background: #0d6efd; color: #fff; }
.edit { border-color: #ffc107; color: #ffc107; }
.edit:hover { background: #ffc107; color: #000; }
.delete { border-color: #dc3545; color: #dc3545; }
.delete:hover { background: #dc3545; color: #fff; }

/* ================= MODAL XEM CHI TIẾT LỊCH SỬ LỒNG NHAU ================= */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.75);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal-content {
  background-color: #1a1a1a;
  border: 1px solid #c5a059;
  border-radius: 8px;
  width: 550px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.5);
  overflow: hidden;
}

.modal-header {
  background-color: #2a2a2a;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #333;
}

.modal-header h3 { margin: 0; color: #ffc107; font-size: 14px; letter-spacing: 1px; }
.close-btn { color: #aaa; font-size: 24px; font-weight: bold; cursor: pointer; }
.close-btn:hover { color: #fff; }
.modal-body { padding: 15px 20px; max-height: 70vh; overflow-y: auto; }

.detail-section-title {
  color: #ffc107;
  font-size: 13px;
  font-weight: bold;
  letter-spacing: 1px;
  margin-top: 15px;
  margin-bottom: 8px;
  padding-bottom: 3px;
  border-bottom: 1px dashed #333;
}

.detail-table { width: 100%; border-collapse: collapse; margin-bottom: 10px; }
.detail-table td { padding: 7px 5px; border-bottom: 1px solid #2a2a2a; }
.detail-table .lbl { width: 150px; color: #aaa; font-size: 13px; }
.detail-table .val { color: #fff; font-size: 13px; }

/* Thẻ bọc danh sách đơn đặt bàn lặp */
.booking-history-container {
  max-height: 280px;
  overflow-y: auto;
  padding-right: 5px;
}

.no-booking-text {
  color: #777;
  font-style: italic;
  padding: 15px 0;
  text-align: center;
  font-size: 13px;
}

.booking-item-card {
  background-color: #222;
  border: 1px solid #333;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 12px;
}

.booking-item-header {
  display: flex;
  justify-content: space-between;
  font-weight: bold;
  color: #ffc107;
  font-size: 12px;
  border-bottom: 1px solid #444;
  padding-bottom: 5px;
  margin-bottom: 5px;
}

.style-compact td { padding: 4px 5px !important; border-bottom: none !important; }
.style-compact .lbl { width: 140px !important; }

/* Trực quan */
.badge-count { background-color: #ffc107; color: #000; padding: 1px 5px; border-radius: 3px; font-size: 11px; font-weight: bold; }
.badge-method { background-color: #0d6efd; color: #fff; padding: 1px 5px; border-radius: 3px; font-size: 10px; font-weight: bold; }
.badge-method.gray { background-color: #555; }
.code-text { font-family: 'Courier New', Courier, monospace; color: #20c997; font-weight: bold; font-size: 11px; }
.gold-text { color: #c5a059; font-weight: bold; }
.text-blue { color: #599fff; }

.modal-footer { padding: 10px 20px; background-color: #2a2a2a; display: flex; justify-content: flex-end; border-top: 1px solid #333; }
</style>