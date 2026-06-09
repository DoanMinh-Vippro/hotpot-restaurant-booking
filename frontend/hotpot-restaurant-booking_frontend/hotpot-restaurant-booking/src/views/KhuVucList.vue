
<script setup lang="ts">
import { ref, onMounted } from 'vue'
// Giữ nguyên đường dẫn tương đối gọi API đang chạy ổn định của bạn
import { 
  getAllKhuVuc, 
  createKhuVuc, 
  updateKhuVuc, 
  deleteKhuVuc, 
  changeStatusKhuVuc 
} from '../api/khuvuc'

// Các trạng thái dữ liệu
const danhSachKhuVuc = ref<any[]>([])
const loading = ref(true)
const keyword = ref('') // Biến lưu từ khóa tìm kiếm
const filterTrangThai = ref('ALL') // Biến bộ lọc trạng thái ('ALL', 'ACTIVE', 'LOCKED')

// Biến phục vụ cho Form Thêm / Sửa
const isEditing = ref(false)
const currentId = ref<number | null>(null)
const formKhuVuc = ref({
  tenKhuVuc: '',
  moTa: ''
})

// === BIẾN ĐIỀU KHIỂN MODAL XEM CHI TIẾT BÀN LỒNG NHAU ===
const showDetailModal = ref(false)
const selectedKhuVuc = ref<any>(null)

// 1. Hàm tải danh sách khu vực và kết hợp bộ lọc
const loadData = async () => {
  loading.value = true
  try {
    const res = await getAllKhuVuc()
    const dataGoc = res.data || []
    
    // Tiến hành lọc dữ liệu theo cả từ khóa và trạng thái hoạt động (khớp chuẩn số 1 và 0)
    danhSachKhuVuc.value = dataGoc.filter((item: any) => {
      const matchesKeyword = !keyword.value.trim() || 
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

const handleSearch = () => {
  loadData()
}

// 2. Hàm kích hoạt bật Modal Xem chi tiết loại bàn thuộc khu vực
const handleViewDetail = (item: any) => {
  selectedKhuVuc.value = item
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedKhuVuc.value = null
}

// 3. Hàm xử lý khi ấn nút Lưu (Đã sửa đổi Payload truyền số 1/0 đồng bộ DTO Java)
const handleSave = async () => {
  if (!formKhuVuc.value.tenKhuVuc) {
    alert('Vui lòng nhập Tên Khu Vực!')
    return
  }

  try {
    if (isEditing.value && currentId.value !== null) {
      const updatePayload = {
        id: currentId.value,
        tenKhuVuc: formKhuVuc.value.tenKhuVuc,
        moTa: formKhuVuc.value.moTa || null,
        trangThai: 1 // <--- Đổi từ true thành số 1 đồng bộ với Database nhóm bạn
      }
      await updateKhuVuc(currentId.value, updatePayload)
      alert('Cập nhật khu vực thành công!')
    } else {
      const createPayload = {
        tenKhuVuc: formKhuVuc.value.tenKhuVuc,
        moTa: formKhuVuc.value.moTa || null,
        trangThai: 1 // <--- Đổi từ true thành số 1 đồng bộ với Database nhóm bạn
      }
      await createKhuVuc(createPayload)
      alert('Thêm mới khu vực thành công!')
    }
    resetForm()
    loadData() 
  } catch (error) {
    console.error('Lỗi khi lưu khu vực:', error)
    alert('Xảy ra lỗi, vui lòng kiểm tra lại kết nối hoặc DTO Backend!')
  }
}

// 4. Hàm kích hoạt chế độ Sửa
const handleEdit = (item: any) => {
  isEditing.value = true
  currentId.value = item.id
  formKhuVuc.value = {
    tenKhuVuc: item.tenKhuVuc || '',
    moTa: item.moTa || ''
  }
}

// 5. Hàm Đổi trạng thái Hoạt động / Khóa (PATCH)
const handleDoiTrangThai = async (id: number) => {
  try {
    await changeStatusKhuVuc(id)
    loadData() 
  } catch (error) {
    console.error('Lỗi khi đổi trạng thái khu vực:', error)
    alert('Không thể thay đổi trạng thái lúc này!')
  }
}

// 6. Hàm Xóa khu vực (DELETE)
const handleXoa = async (id: number) => {
  if (confirm('Bạn có chắc chắn muốn xóa khu vực này? Tất cả bàn thuộc khu vực này có thể bị ảnh hưởng.')) {
    try {
      await deleteKhuVuc(id)
      alert('Xóa khu vực thành công!')
      loadData()
    } catch (error) {
      console.error('Lỗi khi xóa khu vực:', error)
      alert('Không thể xóa khu vực này!')
    }
  }
}

const resetForm = () => {
  isEditing.value = false
  currentId.value = null
  formKhuVuc.value = { tenKhuVuc: '', moTa: '' }
}

const resetFilter = () => {
  keyword.value = ''
  filterTrangThai.value = 'ALL'
  loadData()
}
</script>

<template>
  <div class="khu-vuc-page">
    <h2>👑 QUẢN LÝ KHU VỰC NHÀ HÀNG</h2>
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
          <label class="form-label">Tên Khu Vực *</label>
          <input v-model="formKhuVuc.tenKhuVuc" type="text" placeholder="Ví dụ: Tầng 1, Sân vườn..." />
        </div>

        <div class="form-row" style="align-items: flex-start;">
          <label class="form-label" style="padding-top: 5px;">Mô Tả / Ghi Chú</label>
          <textarea v-model="formKhuVuc.moTa" rows="3" placeholder="Nhập mô tả khu vực hoặc vị trí..."></textarea>
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
          🔄 Đang tải danh sách khu vực từ hệ thống...
        </div>

        <table v-else class="table-classic">
          <thead>
            <tr>
              <th style="width: 60px; text-align: center;">ID</th>
              <th>Tên Khu Vực</th>
              <th>Mô Tả / Ghi Chú</th>
              <th style="width: 140px; text-align: center;">Trạng Thái</th>
              <th style="width: 180px; text-align: center;">Hành Động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in danhSachKhuVuc" :key="item.id">
              <td style="text-align: center; color: #888;">{{ item.id }}</td>
              <td class="highlight-text">{{ item.tenKhuVuc }}</td>
              <td>{{ item.moTa || '---' }}</td>
              <td style="text-align: center;">
                <button 
                  class="btn-status" 
                  :class="(item.trangThai === 1 || item.trangThai === true) ? 'active' : 'locked'" 
                  @click="handleDoiTrangThai(item.id)"
                  title="Bấm vào để chuyển đổi trạng thái hoạt động"
                >
                  {{ (item.trangThai === 1 || item.trangThai === true) ? '● Hoạt động' : '○ Đang khóa' }}
                </button>
              </td>
              <td style="text-align: center;">
                <button class="btn-action view" @click="handleViewDetail(item)">Xem</button>
                <button class="btn-action edit" @click="handleEdit(item)">Sửa</button>
                <button class="btn-action delete" @click="handleXoa(item.id)">Xóa</button>
              </td>
            </tr>
            <tr v-if="danhSachKhuVuc.length === 0">
              <td colspan="5" style="text-align: center; color: #999; padding: 30px 0;">
                📭 Không tìm thấy khu vực nào phù hợp với bộ lọc.
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
          <div class="detail-section-title">📌 Informaciên Tổng Quan</div>
          <table class="detail-table">
            <tr>
              <td class="lbl">Mã Hệ Thống (ID):</td>
              <td class="val">#{{ selectedKhuVuc.id }}</td>
            </tr>
            <tr>
              <td class="lbl">Tên Khu Vực:</td>
              <td class="val highlight-text">{{ selectedKhuVuc.tenKhuVuc }}</td>
            </tr>
            <tr>
              <td class="lbl">Mô Tả Không Gian:</td>
              <td class="val text-blue">{{ selectedKhuVuc.moTa || 'Không có mô tả cụ thể.' }}</td>
            </tr>
            <tr>
              <td class="lbl">Trạng Thái Phân Khu:</td>
              <td class="val">
                <span :class="(selectedKhuVuc.trangThai === 1 || selectedKhuVuc.trangThai === true) ? 'status-green' : 'status-red'" style="font-weight: bold;">
                  {{ (selectedKhuVuc.trangThai === 1 || selectedKhuVuc.trangThai === true) ? '● Đang mở hoạt động' : '○ Đang tạm khóa' }}
                </span>
              </td>
            </tr>
          </table>

          <div class="detail-section-title">🪑 Sơ Đồ Loại Bàn Thuộc Khu Vực</div>
          
          <div v-if="!selectedKhuVuc.banList || selectedKhuVuc.banList.length === 0" class="no-booking-text">
            📭 Hiện tại phân khu này trống, chưa được thêm loại bàn nào.
          </div>
          
          <div v-else class="booking-history-container">
            <div v-for="(ban, index) in selectedKhuVuc.banList" :key="ban.id" class="booking-item-card">
              <div class="booking-item-header">
                <span>Cấu Hình Bàn Số #{{ ban.id }} (Mẫu bàn thứ {{ Number(index) + 1 }})</span>
                <span :class="ban.trangThai === 1 ? 'status-green' : 'status-red'" style="font-size: 11px;">
                  ● {{ ban.trangThai === 1 ? 'Sẵn sàng phục vụ' : 'Tạm dừng sử dụng' }}
                </span>
              </div>
              
              <table class="detail-table style-compact">
                <tr>
                  <td class="lbl">Phân Loại / Quy Cách:</td>
                  <td class="val gold-text">Bàn dành cho {{ ban.loaiBan }}</td>
                </tr>
                <tr>
                  <td class="lbl">Số Lượng Bàn Thực Tế:</td>
                  <td class="val">
                    <span class="badge-count">{{ ban.soLuongBan }} Bàn</span>
                  </td>
                </tr>
                <tr>
                  <td class="lbl">Mã Định Danh Khu Vực:</td>
                  <td class="val code-text">KhuVuc_ID: {{ ban.khuVucId }}</td>
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
.khu-vuc-page {
  padding: 25px;
  background-color: #121212;
  min-height: 100vh;
  font-family: Arial, sans-serif;
  color: #ffffff;
}

h2 { color: #ffc107; margin-top: 0; font-weight: bold; }
.line-break { border: 0; border-top: 1px solid #333; margin-bottom: 25px; }

.search-section { margin-bottom: 25px; display: flex; align-items: center; }
.search-section input { background-color: #222; color: #fff; border: 1px solid #555; padding: 8px 12px; border-radius: 4px; width: 250px; margin-right: 10px; }
.select-classic { background-color: #222; color: #fff; border: 1px solid #555; padding: 8px 12px; border-radius: 4px; margin-right: 10px; cursor: pointer; }

.main-layout { display: flex; gap: 30px; flex-wrap: wrap; }

.form-container { flex: 1; min-width: 350px; max-width: 450px; background-color: #1a1a1a; padding: 20px; border: 1px solid #333; border-radius: 6px; height: fit-content; }
.form-title { color: #ffc107; margin-top: 0; margin-bottom: 20px; font-size: 16px; }
.form-row { margin-bottom: 15px; display: flex; align-items: center; }
.form-label { width: 120px; font-size: 14px; color: #ccc; }
.form-row input, .form-row textarea { flex: 1; background-color: #2b3035; color: #fff; border: 1px solid #555; padding: 8px 12px; border-radius: 4px; font-family: Arial, sans-serif; }
.form-row textarea { resize: none; }
.form-buttons { margin-left: 120px; display: flex; gap: 10px; }

.table-container { flex: 2; min-width: 500px; }
.text-loading { color: #ffc107; font-weight: bold; padding: 10px 0; }
.table-classic { width: 100%; border-collapse: collapse; background-color: #1a1a1a; border: 1px solid #333; border-radius: 4px; }
.table-classic th { background-color: #2a2a2a; color: #ffc107; padding: 12px 10px; text-align: left; border-bottom: 2px solid #444; font-size: 14px; }
.table-classic td { padding: 12px 10px; border-bottom: 1px solid #2a2a2a; font-size: 14px; }
.table-classic tr:hover { background-color: #222; }
.highlight-text { font-weight: bold; color: #ffc107; }

button { padding: 7px 16px; border-radius: 4px; font-weight: bold; cursor: pointer; border: none; margin-right: 5px; }
.btn-yellow { background-color: #ffc107; color: #000; }
.btn-gray { background-color: #555; color: #fff; }
.btn-submit { background-color: #198754; color: #fff; }

.btn-status { padding: 4px 12px; font-size: 13px; width: 110px; text-align: center; }
.btn-status.active { background-color: #28a745; color: white; }
.btn-status.locked { background-color: #6c757d; color: white; }

.btn-action { padding: 4px 10px; font-size: 12px; background: transparent; border: 1px solid; margin-right: 5px; }
.view { border-color: #0d6efd; color: #0d6efd; }
.view:hover { background: #0d6efd; color: #fff; }
.edit { border-color: #ffc107; color: #ffc107; }
.edit:hover { background: #ffc107; color: #000; }
.delete { border-color: #dc3545; color: #dc3545; }
.delete:hover { background: #dc3545; color: #fff; }

/* MODAL */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0, 0, 0, 0.75); display: flex; justify-content: center; align-items: center; z-index: 2000; }
.modal-content { background-color: #1a1a1a; border: 1px solid #c5a059; border-radius: 8px; width: 550px; box-shadow: 0 5px 15px rgba(0,0,0,0.5); overflow: hidden; }
.modal-header { background-color: #2a2a2a; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #333; }
.modal-header h3 { margin: 0; color: #ffc107; font-size: 14px; letter-spacing: 1px; }
.close-btn { color: #aaa; font-size: 24px; font-weight: bold; cursor: pointer; }
.close-btn:hover { color: #fff; }
.modal-body { padding: 15px 20px; max-height: 70vh; overflow-y: auto; }

.detail-section-title { color: #ffc107; font-size: 13px; font-weight: bold; letter-spacing: 1px; margin-top: 15px; margin-bottom: 8px; padding-bottom: 3px; border-bottom: 1px dashed #333; }
.detail-table { width: 100%; border-collapse: collapse; margin-bottom: 10px; }
.detail-table td { padding: 7px 5px; border-bottom: 1px solid #2a2a2a; }
.detail-table .lbl { width: 160px; color: #aaa; font-size: 13px; }
.detail-table .val { color: #fff; font-size: 13px; }

.booking-history-container { max-height: 280px; overflow-y: auto; padding-right: 5px; }
.no-booking-text { color: #777; font-style: italic; padding: 15px 0; text-align: center; font-size: 13px; }
.booking-item-card { background-color: #222; border: 1px solid #333; border-radius: 4px; padding: 10px; margin-bottom: 12px; }
.booking-item-header { display: flex; justify-content: space-between; font-weight: bold; color: #ffc107; font-size: 12px; border-bottom: 1px solid #444; padding-bottom: 5px; margin-bottom: 5px; }

.style-compact td { padding: 4px 5px !important; border-bottom: none !important; }
.style-compact .lbl { width: 140px !important; }

.status-green { color: #198754; }
.status-red { color: #dc3545; }
.badge-count { background-color: #ffc107; color: #000; padding: 1px 5px; border-radius: 3px; font-size: 11px; font-weight: bold; }
.code-text { font-family: 'Courier New', Courier, monospace; color: #20c997; font-weight: bold; font-size: 11px; }
.gold-text { color: #c5a059; font-weight: bold; }
.text-blue { color: #599fff; }
.modal-footer { padding: 10px 20px; background-color: #2a2a2a; display: flex; justify-content: flex-end; border-top: 1px solid #333; }
</style>