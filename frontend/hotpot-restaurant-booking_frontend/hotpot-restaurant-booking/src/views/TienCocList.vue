<script setup lang="ts">
import { ref, onMounted } from 'vue'
// Import thêm useRouter từ vue-router để xử lý điều hướng trang chủ
import { useRouter } from 'vue-router'
import { 
  getAllTienCoc, 
  getTienCocByTrangThai, 
  getTienCocByKhachHang, 
  getTongTienDaThu 
} from '../api/coc'

const router = useRouter()

// Các biến quản lý dữ liệu
const tatCaDieuDuLieuGoc = ref<any[]>([]) // Bản gốc để lọc offline siêu tốc
const danhSachTienCoc = ref<any[]>([])    // Bảng hiển thị
const chiTietCoc = ref<any>(null)
const tongTienDaThu = ref<number>(0)
const loading = ref(true)

// Biến phục vụ bộ lọc
const filterTrangThai = ref<string>('all')
const searchKeyword = ref<string>('') // Đổi tên cho rộng nghĩa (tìm cả tên, id, sdt)

  // Hàm xử lý nhảy về trang chủ
const goToHome = () => {
  router.push('/') // Trả về trang chính mặc định của hệ thống
}

// 1. Hàm tải dữ liệu ban đầu
const loadInitialData = async () => {
  loading.value = true
  try {
    const resList = await getAllTienCoc()
    tatCaDieuDuLieuGoc.value = resList.data || []
    danhSachTienCoc.value = resList.data || []

    const resTong = await getTongTienDaThu()
    tongTienDaThu.value = typeof resTong.data === 'number' ? resTong.data : (resTong.data?.tongTienCocDaThu || resTong.data?.tongTien || 0)
  } catch (error) {
    console.error('Lỗi khi tải dữ liệu tiền cọc:', error)
    danhSachTienCoc.value = []
  } finally {
    loading.value = false
  }
}

// 2. Xử lý bộ lọc trạng thái (Lọc trực tiếp theo DTO cực chuẩn)
const handleFilterTrangThai = () => {
  searchKeyword.value = '' 
  chiTietCoc.value = null
  
  if (filterTrangThai.value === 'all') {
    danhSachTienCoc.value = [...tatCaDieuDuLieuGoc.value]
    return
  }

  const statusTarget = parseInt(filterTrangThai.value)
  danhSachTienCoc.value = tatCaDieuDuLieuGoc.value.filter(item => item.trangThaiCoc === statusTarget)
}

// 3. Xử lý tìm kiếm thông minh: Gõ ID, Tên hay SĐT đều tìm ra tuốt!
const handleSearch = () => {
  filterTrangThai.value = 'all'
  chiTietCoc.value = null

  if (!searchKeyword.value.trim()) {
    danhSachTienCoc.value = [...tatCaDieuDuLieuGoc.value]
    return
  }

  const kw = searchKeyword.value.trim().toLowerCase()
  
  danhSachTienCoc.value = tatCaDieuDuLieuGoc.value.filter(item => {
    const matchId = item.id?.toString() === kw
    const matchMaKH = item.maKhachHang?.toString().toLowerCase() === kw
    const matchTen = item.tenKhachHang?.toLowerCase().includes(kw)
    const matchSdt = item.soDienThoai?.includes(kw)
    return matchId || matchMaKH || matchTen || matchSdt
  })
}

const xemChiTiet = (item: any) => {
  chiTietCoc.value = item
}

const handleReset = () => {
  filterTrangThai.value = 'all'
  searchKeyword.value = ''
  chiTietCoc.value = null
  loadInitialData()
}

onMounted(() => {
  loadInitialData()
})
</script>

<template>
  <div class="coc-page">
    <div class="coc-header">
      <div class="header-left">

      <h2>👑 BÁO CÁO & QUẢN LÝ TIỀN CỌC</h2>
      <div class="revenue-badge">
        <span class="lbl">TỔNG TIỀN ĐÃ THU:</span>
        <span class="val">{{ tongTienDaThu.toLocaleString() }}đ</span>
      </div>
    </div>
          
    <button class="btn-back-home" @click="goToHome">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          <polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
        Quay về trang chủ
      </button>
    </div>
    <hr class="line-break" />

    <div class="filter-panel">
      <div class="filter-group">
        <label>Lọc Theo Trạng Thái Cọc:</label>
        <select v-model="filterTrangThai" @change="handleFilterTrangThai">
          <option value="all">-- Tất cả trạng thái --</option>
          <option value="1">Đã đặt cọc</option>
          <option value="0">Chưa có tiền</option>
          <option value="2">Đã hoàn trả</option>
        </select>
      </div>

      <div class="filter-group">
        <label>Tìm kiếm thông tin khách hàng:</label>
        <div class="search-input-wrapper">
          <input 
            v-model="searchKeyword" 
            type="text" 
            placeholder="Nhập Tên, SĐT hoặc Mã..." 
            @keyup.enter="handleSearch"
          />
          <button class="btn-yellow" @click="handleSearch">Tìm</button>
        </div>
      </div>

      <div class="filter-group" style="margin-bottom: 0; padding-top: 22px;">
        <button class="btn-gray" @click="handleReset">Làm mới bộ lọc</button>
      </div>
    </div>

    <div v-if="loading" class="text-loading">🔄 Đang tải danh sách tiền cọc từ CocResponse DTO...</div>

    <div v-else class="main-layout">
      <div class="table-container" :style="{ flex: chiTietCoc ? '1.3' : '2' }">
        <table class="table-classic">
          <thead>
            <tr>
              <th style="width: 60px; text-align: center;">Mã ĐB</th>
              <th>Khách Hàng</th>
              <th>Số Điện Thoại</th>
              <th>Số Tiền Cọc</th>
              <th style="width: 140px; text-align: center;">Trạng Thái Cọc</th>
              <th style="width: 130px; text-align: center;">Hành Động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in danhSachTienCoc" :key="item.id">
              <td style="text-align: center; color: #888;">{{ item.id }}</td>
              <td>{{ item.tenKhachHang || 'Khách vãng lai' }}</td>
              <td>{{ item.soDienThoai || '---' }}</td>
              <td class="money-text">{{ (item.soTienCoc || 0).toLocaleString() }}đ</td>
              <td style="text-align: center;">
                <span class="status-tag" :class="item.trangThaiCoc === 1 ? 'success' : item.trangThaiCoc === 2 ? 'info' : 'warning'">
                  {{ item.trangThaiCocText || (item.trangThaiCoc === 1 ? 'Đã cọc' : item.trangThaiCoc === 2 ? 'Đã hoàn' : 'Chưa cọc') }}
                </span>
              </td>
              <td style="text-align: center;">
                <button class="btn-view" @click="xemChiTiet(item)">Xem chi tiết</button>
              </td>
            </tr>
            <tr v-if="danhSachTienCoc.length === 0">
              <td colspan="6" style="text-align: center; color: #999; padding: 30px 0;">
                📭 Không tìm thấy phiếu đặt bàn có cọc nào.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="chiTietCoc" class="detail-panel">
        <div class="detail-card">
          <div class="detail-header">
            <span>📝 Chi Tiết Phiếu Cọc #{{ chiTietCoc.id }}</span>
            <button class="close-x" @click="chiTietCoc = null">×</button>
          </div>
          <div class="detail-body">
            <div class="detail-row"><strong>Mã Đặt Bàn (ID):</strong> <span>{{ chiTietCoc.id }}</span></div>
            <div class="detail-row"><strong>Mã Khách Hàng:</strong> <span>{{ chiTietCoc.maKhachHang || '---' }}</span></div>
            <div class="detail-row"><strong>Tên Khách Hàng:</strong> <span>{{ chiTietCoc.tenKhachHang || '---' }}</span></div>
            <div class="detail-row"><strong>Số Điện Thoại:</strong> <span>{{ chiTietCoc.soDienThoai || '---' }}</span></div>
            <div class="detail-row"><strong>Thời gian đặt:</strong> <span>{{ chiTietCoc.ngayDat }} | {{ chiTietCoc.gioDat }}</span></div>
            <div class="detail-row"><strong>Số lượng khách:</strong> <span>{{ chiTietCoc.soNguoi }} người</span></div>
            <div class="detail-row">
              <strong>Số Tiền Cọc:</strong> 
              <span class="money-text" style="font-size: 16px;">{{ (chiTietCoc.soTienCoc || 0).toLocaleString() }}đ</span>
            </div>
            <div class="detail-row">
              <strong>Phương thức thanh toán:</strong> 
              <span style="color: #ffc107; font-weight: bold;">{{ chiTietCoc.phuongThucThanhToanText || 'Chưa xác định' }}</span>
            </div>
            <div class="detail-row">
              <strong>Trạng Thái Cọc:</strong> 
              <span class="status-tag" :class="chiTietCoc.trangThaiCoc === 1 ? 'success' : chiTietCoc.trangThaiCoc === 2 ? 'info' : 'warning'">
                {{ chiTietCoc.trangThaiCocText || 'Chưa cọc' }}
              </span>
            </div>
            <div class="detail-row" style="border: none;">
              <strong>Ghi chú đơn:</strong>
              <div class="note-box">{{ chiTietCoc.ghiChu || 'Không có ghi chú' }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Giữ nguyên phần CSS đẹp mắt phía dưới */
.coc-page { padding: 25px; background-color: #121212; min-height: 100vh; font-family: Arial, sans-serif; color: #ffffff; }

/* CẬP NHẬT: Chia không gian flexbox cho thanh tiêu đề */
.coc-header { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 15px; }
.header-left { display: flex; align-items: center; gap: 15px; flex-wrap: wrap; }

/* THÊM MỚI: Style nút quay về trang chủ */
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
.btn-back-home svg { transition: transform 0.2s ease; }
.btn-back-home:hover svg { transform: scale(1.1); }

.coc-header { display: flex; align-items: center; flex-wrap: wrap; gap: 15px; }
h2 { color: #ffc107; margin: 0; font-weight: bold; }
.revenue-badge { background-color: #1e2b22; border: 1px solid #28a745; padding: 10px 18px; border-radius: 6px; }
.revenue-badge .lbl { font-weight: bold; color: #adb5bd; margin-right: 8px; }
.revenue-badge .val { color: #28a745; font-weight: bold; font-size: 18px; }
.line-break { border: 0; border-top: 1px solid #333; margin-bottom: 25px; margin-top: 15px; }
.filter-panel { background-color: #1a1a1a; padding: 15px 20px; border: 1px solid #333; border-radius: 6px; display: flex; gap: 25px; flex-wrap: wrap; margin-bottom: 25px; align-items: center; }
.filter-group { display: flex; flex-direction: column; gap: 6px; }
.filter-group label { font-size: 13px; color: #aaa; font-weight: bold; }
.filter-group select { background-color: #2b3035; color: white; border: 1px solid #555; padding: 8px 12px; border-radius: 4px; width: 200px; }
.search-input-wrapper { display: flex; gap: 8px; }
.search-input-wrapper input { background-color: #2b3035; color: white; border: 1px solid #555; padding: 8px 12px; border-radius: 4px; width: 200px; }
.main-layout { display: flex; gap: 25px; flex-wrap: wrap; align-items: flex-start; }
.table-container { min-width: 500px; transition: all 0.3s ease; }
.text-loading { color: #ffc107; font-weight: bold; padding: 15px 0; }
.table-classic { width: 100%; border-collapse: collapse; background-color: #1a1a1a; border: 1px solid #333; }
.table-classic th { background-color: #2a2a2a; color: #ffc107; padding: 12px 10px; text-align: left; border-bottom: 2px solid #444; font-size: 14px; }
.table-classic td { padding: 12px 10px; border-bottom: 1px solid #2a2a2a; font-size: 14px; }
.table-classic tr:hover { background-color: #222; }
.money-text { font-weight: bold; color: #dc3545; }
.status-tag { padding: 3px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; display: inline-block; }
.status-tag.success { background-color: #28a745; color: white; }
.status-tag.warning { background-color: #ffc107; color: #000; }
.status-tag.info { background-color: #17a2b8; color: white; }
.detail-panel { flex: 0.8; min-width: 350px; position: sticky; top: 20px; }
.detail-card { background-color: #1a1a1a; border: 1px solid #17a2b8; border-radius: 6px; overflow: hidden; }
.detail-header { background-color: #17a2b8; color: white; padding: 12px 15px; font-weight: bold; display: flex; justify-content: space-between; align-items: center; }
.close-x { background: transparent; border: none; color: white; font-size: 20px; cursor: pointer; line-height: 1; }
.detail-body { padding: 15px; }
.detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #2a2a2a; font-size: 14px; }
.detail-row strong { color: #adb5bd; }
.note-box { background-color: #252525; border: 1px solid #333; padding: 8px; border-radius: 4px; margin-top: 8px; color: #ccc; font-size: 13px; word-break: break-all; }
button { padding: 6px 14px; border-radius: 4px; font-weight: bold; cursor: pointer; border: none; }
.btn-yellow { background-color: #ffc107; color: #000; }
.btn-gray { background-color: #555; color: #fff; }
.btn-view { background-color: #17a2b8; color: white; font-size: 12px; width: 100%; padding: 5px 0; }
.btn-view:hover { opacity: 0.9; }
</style>