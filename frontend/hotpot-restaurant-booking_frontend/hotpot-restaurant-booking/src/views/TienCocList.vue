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
.coc-page { padding: 25px; background: linear-gradient(135deg, #f9efe0 0%, #f4e4c6 100%); min-height: 100vh; font-family: Arial, sans-serif; color: #5f3d22; }
.coc-header { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 15px; }
.header-left { display: flex; align-items: center; gap: 15px; flex-wrap: wrap; }
.btn-back-home {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #4b7c45, #6d9b5d);
  color: #ffffff;
  border: 1px solid #4b7c45;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-back-home:hover {
  background: linear-gradient(135deg, #3f693b, #5a8550);
  border-color: #d8a85c;
  color: #fffaf1;
}
.btn-back-home svg { transition: transform 0.2s ease; }
.btn-back-home:hover svg { transform: scale(1.1); }
.coc-header { display: flex; align-items: center; flex-wrap: wrap; gap: 15px; }
h2 { color: #8b5e34; margin: 0; font-weight: bold; }
.revenue-badge { background: rgba(255, 248, 234, 0.95); border: 1px solid #e6d2aa; padding: 10px 18px; border-radius: 10px; }
.revenue-badge .lbl { font-weight: bold; color: #8f6b46; margin-right: 8px; }
.revenue-badge .val { color: #4b7c45; font-weight: bold; font-size: 18px; }
.line-break { border: 0; border-top: 1px solid #e6d2aa; margin-bottom: 25px; margin-top: 15px; }
.filter-panel { background: rgba(255, 248, 234, 0.95); padding: 15px 20px; border: 1px solid #e6d2aa; border-radius: 12px; display: flex; gap: 25px; flex-wrap: wrap; margin-bottom: 25px; align-items: center; box-shadow: 0 8px 18px rgba(103, 72, 32, 0.06); }
.filter-group { display: flex; flex-direction: column; gap: 6px; }
.filter-group label { font-size: 13px; color: #8f6b46; font-weight: bold; }
.filter-group select { background-color: #fffaf1; color: #5f3d22; border: 1px solid #e6d2aa; padding: 8px 12px; border-radius: 6px; width: 200px; }
.search-input-wrapper { display: flex; gap: 8px; }
.search-input-wrapper input { background-color: #fffaf1; color: #5f3d22; border: 1px solid #e6d2aa; padding: 8px 12px; border-radius: 6px; width: 200px; }
.main-layout { display: flex; gap: 25px; flex-wrap: wrap; align-items: flex-start; }
.table-container { min-width: 500px; transition: all 0.3s ease; }
.text-loading { color: #c98b3e; font-weight: bold; padding: 15px 0; }
.table-classic { width: 100%; border-collapse: collapse; background-color: #fffaf1; border: 1px solid #e6d2aa; }
.table-classic th { background-color: #f3dfb4; color: #8b5e34; padding: 12px 10px; text-align: left; border-bottom: 2px solid #e6d2aa; font-size: 14px; }
.table-classic td { padding: 12px 10px; border-bottom: 1px solid #efe0c1; font-size: 14px; color: #5f3d22; }
.table-classic tr:hover { background-color: #fef4de; }
.money-text { font-weight: bold; color: #dc3545; }
.status-tag { padding: 3px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; display: inline-block; }
.status-tag.success { background-color: #4b7c45; color: white; }
.status-tag.warning { background: linear-gradient(135deg, #d8a85c, #f1cf87); color: #3d2814; }
.status-tag.info { background-color: #5f7fb8; color: white; }
.detail-panel { flex: 0.8; min-width: 350px; position: sticky; top: 20px; }
.detail-card { background: rgba(255, 248, 234, 0.98); border: 1px solid #e6d2aa; border-radius: 6px; overflow: hidden; box-shadow: 0 8px 18px rgba(103, 72, 32, 0.06); }
.detail-header { background: linear-gradient(135deg, #d8a85c, #f1cf87); color: #3d2814; padding: 12px 15px; font-weight: bold; display: flex; justify-content: space-between; align-items: center; }
.close-x { background: transparent; border: none; color: #3d2814; font-size: 20px; cursor: pointer; line-height: 1; }
.detail-body { padding: 15px; }
.detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #efe0c1; font-size: 14px; }
.detail-row strong { color: #8f6b46; }
.note-box { background-color: #fff8ea; border: 1px solid #e6d2aa; padding: 8px; border-radius: 4px; margin-top: 8px; color: #5f3d22; font-size: 13px; word-break: break-all; }
button { padding: 6px 14px; border-radius: 4px; font-weight: bold; cursor: pointer; border: none; }
.btn-yellow { background: linear-gradient(135deg, #d8a85c, #f1cf87); color: #3d2814; }
.btn-gray { background: #fff3d3; color: #8b5e34; border: 1px solid #e6d2aa; }
.btn-view { background: linear-gradient(135deg, #4b7c45, #6d9b5d); color: white; font-size: 12px; width: 100%; padding: 5px 0; }
.btn-view:hover { opacity: 0.9; }
</style>