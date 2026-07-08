<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BanApi from '@/api/BanApi'
import DatBanQuanLyApi from '@/api/DatBanQuanLy'
import ComBoInDatBan from '@/components/ComBoInDatBan.vue'

type ReservationStatus = 'CHO_XAC_NHAN' | 'DA_XAC_NHAN' | 'DA_NHAN_BAN' | 'HOAN_THANH' | 'DA_HUY'
type ViewMode = 'management' | 'walkin'

type TabItem = {
  key: ReservationStatus
  label: string
  showFilter: boolean
}

const router = useRouter()
const reservations = ref<any[]>([])
const activeTab = ref<ReservationStatus>('DA_XAC_NHAN')
const selectedReservation = ref<any | null>(null)
const filterDate = ref('')
const filterMonth = ref('')
const searchTerm = ref('')
const sortOption = ref('newest')
const viewMode = ref<ViewMode>('management')
const selectedComboId = ref<number | null>(null)
const tableDepositAmount = 100000
const comboDepositRate = 0.3

const tabs: TabItem[] = [
  { key: 'DA_XAC_NHAN', label: 'Đã xác nhận', showFilter: true },
  { key: 'CHO_XAC_NHAN', label: 'Chờ xác nhận', showFilter: false },
  { key: 'DA_NHAN_BAN', label: 'Đã nhận bàn', showFilter: false },
  { key: 'HOAN_THANH', label: 'Hoàn thành', showFilter: true },
  { key: 'DA_HUY', label: 'Đã huỷ', showFilter: true },
]

const statusLabels: Record<ReservationStatus, string> = {
  CHO_XAC_NHAN: 'Chờ xác nhận',
  DA_XAC_NHAN: 'Đã xác nhận',
  DA_NHAN_BAN: 'Đã nhận bàn',
  HOAN_THANH: 'Hoàn thành',
  DA_HUY: 'Đã huỷ',
}

const statusOptions: ReservationStatus[] = ['CHO_XAC_NHAN', 'DA_XAC_NHAN', 'DA_NHAN_BAN', 'HOAN_THANH', 'DA_HUY']

const walkInForm = ref({
  tenKhachHang: '',
  idkhachHang: null as number | null,
  sdtKhachHang: '',
  soNguoi: 2,
  thoiGianDenDuKien: '',
  soTienCoc: tableDepositAmount,
  trangThaiCoc: 'CHUA_COC',
  phuongThucThanhToan: 'TIEN_MAT',
  ghiChu: '',
})

const normalizeStatus = (value: any) => {
  if (!value) return ''
  if (typeof value === 'string') return value
  if (typeof value === 'object' && 'name' in value) return String(value.name)
  return String(value)
}

const formatDateValue = (value: any) => {
  if (!value) return '—'
  if (typeof value === 'string') return value.slice(0, 10)
  if (value instanceof Date) return value.toISOString().slice(0, 10)
  if (typeof value === 'object' && value.year != null) {
    return `${value.year}-${String(value.monthValue || value.month || 1).padStart(2, '0')}-${String(value.dayOfMonth || value.day || 1).padStart(2, '0')}`
  }
  return String(value)
}

const currentTabLabel = computed(() => tabs.find((tab) => tab.key === activeTab.value)?.label || 'Đặt bàn')
const showMonthFilter = computed(() => activeTab.value === 'DA_HUY')

const visibleReservations = computed(() => {
  const keyword = searchTerm.value.trim().toLowerCase()

  return reservations.value
    .filter((item) => normalizeStatus(item.trangThai) === activeTab.value)
    .filter((item) => {
      const dateValue = formatDateValue(item.ngayDat)
      const monthValue = dateValue.slice(0, 7)
      const matchesDate = !filterDate.value || dateValue === filterDate.value
      const matchesMonth = !filterMonth.value || monthValue === filterMonth.value
      return matchesDate && matchesMonth
    })
    .filter((item) => {
      if (!keyword) return true
      const haystack = [
        item.tenKhachHang,
        item.sdtKhachHang,
        item.idDatBan,
        item.ghiChu,
        statusLabels[normalizeStatus(item.trangThai) as ReservationStatus],
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
      return haystack.includes(keyword)
    })
    .sort((a, b) => {
      const aDate = a.ngayDat ? new Date(a.ngayDat).getTime() : 0
      const bDate = b.ngayDat ? new Date(b.ngayDat).getTime() : 0

      if (sortOption.value === 'newest') return bDate - aDate
      if (sortOption.value === 'oldest') return aDate - bDate
      if (sortOption.value === 'deposit_desc') return Number(b.soTienCoc || 0) - Number(a.soTienCoc || 0)
      if (sortOption.value === 'deposit_asc') return Number(a.soTienCoc || 0) - Number(b.soTienCoc || 0)
      return Number(b.soNguoi || 0) - Number(a.soNguoi || 0)
    })
})

const loadData = async () => {
  try {
    const response = await DatBanQuanLyApi.getAll()
    reservations.value = Array.isArray(response?.data) ? response.data : []
  } catch (error) {
    console.error('Lỗi khi tải danh sách đặt bàn:', error)
  }
}

const openDetail = async (reservation: any) => {
  try {
    const response = await DatBanQuanLyApi.findById(reservation.idDatBan)
    selectedReservation.value = response?.data || reservation
  } catch (error) {
    console.error('Lỗi lấy chi tiết đặt bàn:', error)
    selectedReservation.value = reservation
  }
}

const getAvailableStatusOptions = (reservation: any) => {
  const currentStatus = normalizeStatus(reservation?.trangThai)

  switch (currentStatus) {
    case 'CHO_XAC_NHAN':
      return ['CHO_XAC_NHAN', 'DA_XAC_NHAN', 'DA_HUY'] as ReservationStatus[]
    case 'DA_XAC_NHAN':
      return ['DA_XAC_NHAN', 'DA_NHAN_BAN', 'DA_HUY'] as ReservationStatus[]
    case 'DA_NHAN_BAN':
      return ['DA_NHAN_BAN', 'HOAN_THANH', 'DA_HUY'] as ReservationStatus[]
    case 'HOAN_THANH':
      return ['HOAN_THANH'] as ReservationStatus[]
    case 'DA_HUY':
      return ['DA_HUY'] as ReservationStatus[]
    default:
      return statusOptions
  }
}

const isTerminalStatus = (reservation: any) => {
  const currentStatus = normalizeStatus(reservation?.trangThai)
  return currentStatus === 'HOAN_THANH' || currentStatus === 'DA_HUY'
}

const changeStatus = async (reservation: any, newStatus: ReservationStatus) => {
  const currentStatus = normalizeStatus(reservation?.trangThai)
  if (currentStatus === newStatus) return

  const confirmed = window.confirm(`Bạn có chắc muốn đổi trạng thái đơn #${reservation.idDatBan} sang ${statusLabels[newStatus]}?`)
  if (!confirmed) return

  try {
    await DatBanQuanLyApi.update(reservation.idDatBan, { ...reservation, trangThai: newStatus })

    if (newStatus === 'DA_XAC_NHAN' && reservation.idBan) {
      await BanApi.update(reservation.idBan, { trangThai: 'DA_DAT' })
    }

    if (newStatus === 'DA_NHAN_BAN' && reservation.idBan) {
      await BanApi.update(reservation.idBan, { trangThai: 'DANG_SU_DUNG' })
    }

    if ((newStatus === 'HOAN_THANH' || newStatus === 'DA_HUY') && reservation.idBan) {
      await BanApi.update(reservation.idBan, { trangThai: 'TRONG' })
    }

    await loadData()
    activeTab.value = newStatus
  } catch (error) {
    console.error('Lỗi cập nhật trạng thái:', error)
    alert('Không thể cập nhật trạng thái. Vui lòng thử lại.')
  }
}

const handleComboSelection = (combo: any | null) => {
  if (!combo) {
    selectedComboId.value = null
    walkInForm.value.soTienCoc = tableDepositAmount
    return
  }

  selectedComboId.value = combo.idCombo ?? null
  walkInForm.value.soTienCoc = Math.round(tableDepositAmount + Number(combo.giaCombo || 0) * comboDepositRate)
}

const resetWalkInForm = () => {
  walkInForm.value = {
    tenKhachHang: '',
    idkhachHang: null,
    sdtKhachHang: '',
    soNguoi: 2,
    thoiGianDenDuKien: '',
    soTienCoc: tableDepositAmount,
    trangThaiCoc: 'CHUA_COC',
    phuongThucThanhToan: 'TIEN_MAT',
    ghiChu: '',
  }
  selectedComboId.value = null
}

const submitWalkInReservation = async () => {
  try {
    const payload = {
      idkhachHang: walkInForm.value.idkhachHang || null,
      sdtKhachHang: walkInForm.value.sdtKhachHang || '',
      soNguoi: Number(walkInForm.value.soNguoi || 1),
      trangThai: 'CHO_XAC_NHAN',
      ghiChu: walkInForm.value.ghiChu || (walkInForm.value.tenKhachHang ? `Khách: ${walkInForm.value.tenKhachHang}` : 'Đặt bàn tại quầy'),
      thoiGianDenDuKien: walkInForm.value.thoiGianDenDuKien || null,
      soTienCoc: Number(walkInForm.value.soTienCoc || 0),
      trangThaiCoc: walkInForm.value.trangThaiCoc || 'CHUA_COC',
      phuongThucThanhToan: walkInForm.value.phuongThucThanhToan || 'TIEN_MAT',
      idCombo: selectedComboId.value ?? null,
    }

    await DatBanQuanLyApi.add(payload)
    await loadData()
    activeTab.value = 'CHO_XAC_NHAN'
    viewMode.value = 'management'
    resetWalkInForm()
    alert('Đã lưu đơn đặt bàn tại quầy thành công.')
  } catch (error) {
    console.error('Lỗi lưu đặt bàn tại quầy:', error)
    alert('Không thể lưu đặt bàn tại quầy. Vui lòng kiểm tra lại dữ liệu.')
  }
}

const goHome = () => {
  router.push('/')
}

const formatCurrency = (value: number | string | null | undefined) => {
  const amount = Number(value || 0)
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)
}

onMounted(loadData)
</script>

<template>
  <div class="page-shell">
    <div class="page-top">
      <button class="back-home-btn" @click="goHome">🏠 Trang chủ</button>
      <h2 class="page-title">Quản lý đặt bàn</h2>
    </div>

    <div class="section-switcher">
      <button class="section-btn" :class="{ active: viewMode === 'management' }" @click="viewMode = 'management'">
        Quản lý đặt bàn
      </button>
      <button class="section-btn" :class="{ active: viewMode === 'walkin' }" @click="viewMode = 'walkin'">
        Đặt bàn tại quầy
      </button>
    </div>

    <div v-if="viewMode === 'management'">
      <div class="tab-bar">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-button"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="filter-row">
        <label class="filter-field">
          <span>Ngày</span>
          <input v-model="filterDate" type="date" />
        </label>
        <label v-if="showMonthFilter" class="filter-field">
          <span>Tháng</span>
          <input v-model="filterMonth" type="month" />
        </label>
        <label class="filter-field search-field">
          <span>Tìm kiếm</span>
          <input v-model="searchTerm" type="text" placeholder="Tên, SĐT, mã đơn" />
        </label>
        <label class="filter-field">
          <span>Sắp xếp</span>
          <select v-model="sortOption">
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="deposit_desc">Tiền cọc giảm dần</option>
            <option value="deposit_asc">Tiền cọc tăng dần</option>
            <option value="guest_desc">Số người giảm dần</option>
          </select>
        </label>
        <button class="clear-filter-btn" @click="filterDate = ''; filterMonth = ''; searchTerm = ''; sortOption = 'newest'">Xoá bộ lọc</button>
      </div>

      <div class="table-card">
        <div class="table-header">
          <h3>{{ currentTabLabel }}</h3>
          <span>{{ visibleReservations.length }} đơn</span>
        </div>

        <div v-if="visibleReservations.length === 0" class="empty-state">
          Không có dữ liệu trong tab này.
        </div>

        <div v-else class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Mã</th>
                <th>Khách hàng</th>
                <th>Số điện thoại</th>
                <th>Ngày đặt</th>
                <th>Giờ đặt</th>
                <th>Số người</th>
                <th>Tiền cọc</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in visibleReservations" :key="item.idDatBan">
                <td>#{{ item.idDatBan }}</td>
                <td>{{ item.tenKhachHang || '—' }}</td>
                <td>{{ item.sdtKhachHang || '—' }}</td>
                <td>{{ formatDateValue(item.ngayDat) }}</td>
                <td>{{ item.gioDat || '—' }}</td>
                <td>{{ item.soNguoi || 0 }}</td>
                <td>{{ formatCurrency(item.soTienCoc) }}</td>
                <td>
                  <template v-if="isTerminalStatus(item)">
                    <span></span>
                  </template>
                  <select v-else :value="normalizeStatus(item.trangThai)" @change="(event) => changeStatus(item, (event.target as HTMLSelectElement).value as ReservationStatus)">
                    <option v-for="status in getAvailableStatusOptions(item)" :key="status" :value="status">
                      {{ statusLabels[status] }}
                    </option>
                  </select>
                </td>
                <td>
                  <div class="action-group">
                    <button class="btn btn-detail" @click="openDetail(item)">Chi tiết</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div v-else class="walkin-card">
      <div class="walkin-header">
        <div>
          <h3>Đặt bàn tại quầy</h3>
          <p>Đơn sẽ được lưu với trạng thái chờ xác nhận và hiển thị ở tab tương ứng.</p>
        </div>
        <span class="walkin-badge">Khách vãng lai nếu không nhập tên</span>
      </div>

      <div class="walkin-grid">
        <label class="field">
          <span>Tên khách hàng</span>
          <input v-model="walkInForm.tenKhachHang" type="text" placeholder="Nhập tên nếu có" />
        </label>

        <label class="field">
          <span>Số điện thoại</span>
          <input v-model="walkInForm.sdtKhachHang" type="text" placeholder="SĐT khách hàng" />
        </label>

        <label class="field">
          <span>ID khách hàng (không bắt bu)</span>
          <input v-model.number="walkInForm.idkhachHang" type="number" placeholder="Nếu có tài khoản" />
        </label>

        <label class="field">
          <span>Số người</span>
          <input v-model.number="walkInForm.soNguoi" type="number" min="1" />
        </label>

        <label class="field">
          <span>Thời gian đến</span>
          <input v-model="walkInForm.thoiGianDenDuKien" type="datetime-local" />
        </label>

        <label class="field">
          <span>Tiền cọc</span>
          <input v-model.number="walkInForm.soTienCoc" type="number" min="0" />
        </label>

        <label class="field">
          <span>Trạng thái cọc</span>
          <select v-model="walkInForm.trangThaiCoc">
            <option value="CHUA_COC">Chưa cọc</option>
            <option value="DA_COC">Đã cọc</option>
            <option value="DA_HOAN_COC">Đã hoàn cọc</option>
            <option value="KHONG_HOAN_COC">Không hoàn cọc</option>
          </select>
        </label>

        <label class="field">
          <span>Phương thức thanh toán</span>
          <select v-model="walkInForm.phuongThucThanhToan">
            <option value="TIEN_MAT">Tiền mặt</option>
            <option value="CHUYEN_KHOAN">Chuyển khoản</option>
            <option value="VNPAY">VNPAY</option>
          </select>
        </label>
      </div>

      <label class="field full-width">
        <span>Đặt món trước</span>
        <ComBoInDatBan v-model="selectedComboId" @selectedCombo="handleComboSelection" />
      </label>

      <label class="field full-width">
        <span>Ghi chú</span>
        <textarea v-model="walkInForm.ghiChu" rows="3" placeholder="Ghi chú cho nhân viên"></textarea>
      </label>

      <div class="walkin-actions">
        <button class="btn btn-primary" @click="submitWalkInReservation">Lưu đặt bàn tại quầy</button>
        <button class="btn btn-secondary" @click="resetWalkInForm">Đặt lại</button>
      </div>
    </div>
  </div>

  <div v-if="selectedReservation" class="modal-overlay" @click.self="selectedReservation = null">
    <div class="detail-modal">
      <div class="modal-header">
        <h3>Chi tiết đặt bàn #{{ selectedReservation.idDatBan }}</h3>
        <button class="close-btn" @click="selectedReservation = null">×</button>
      </div>

      <div class="detail-grid">
        <div><span>Khách hàng</span><strong>{{ selectedReservation.tenKhachHang || '—' }}</strong></div>
        <div><span>Số điện thoại</span><strong>{{ selectedReservation.sdtKhachHang || '—' }}</strong></div>
        <div><span>Ngày đặt</span><strong>{{ formatDateValue(selectedReservation.ngayDat) }}</strong></div>
        <div><span>Giờ đặt</span><strong>{{ selectedReservation.gioDat || '—' }}</strong></div>
        <div><span>Số người</span><strong>{{ selectedReservation.soNguoi || 0 }}</strong></div>
        <div><span>Tiền cọc</span><strong>{{ formatCurrency(selectedReservation.soTienCoc) }}</strong></div>
        <div><span>Trạng thái</span><strong>{{ statusLabels[normalizeStatus(selectedReservation.trangThai) as ReservationStatus] || normalizeStatus(selectedReservation.trangThai) || '—' }}</strong></div>
        <div><span>Trạng thái cọc</span><strong>{{ selectedReservation.trangThaiCoc || '—' }}</strong></div>
        <div><span>Phương thức thanh toán</span><strong>{{ selectedReservation.phuongThucThanhToan || '—' }}</strong></div>
        <div><span>Combo</span><strong>{{ selectedReservation.tenCombo || '—' }}</strong></div>
        <div><span>Ghi chú</span><strong>{{ selectedReservation.ghiChu || '—' }}</strong></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-shell {
  padding: 18px 0 32px;
  color: #fff;
}

.page-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.page-title {
  margin: 0;
  font-size: 1.2rem;
  color: #ffd86b;
}

.back-home-btn {
  border: 1px solid rgba(212, 175, 55, 0.35);
  background: rgba(255, 255, 255, 0.06);
  color: #ffd86b;
  padding: 8px 14px;
  border-radius: 999px;
  cursor: pointer;
  font-weight: 600;
}

.back-home-btn:hover {
  background: rgba(212, 175, 55, 0.16);
}

.section-switcher {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.section-btn {
  border: 1px solid #2e2e2e;
  background: #121212;
  color: #f8e6a0;
  padding: 8px 12px;
  border-radius: 999px;
  cursor: pointer;
}

.section-btn.active {
  background: #c5a059;
  color: #111;
  border-color: #c5a059;
  font-weight: 700;
}

.tab-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}

.tab-button {
  border: 1px solid #333;
  background: #111;
  color: #e5e5e5;
  padding: 8px 12px;
  border-radius: 999px;
  cursor: pointer;
}

.tab-button.active {
  background: #c5a059;
  color: #111;
  border-color: #c5a059;
  font-weight: 700;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 14px;
  align-items: flex-end;
}

.filter-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #d8c07d;
  font-size: 0.85rem;
}

.filter-field input {
  min-width: 150px;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid #2c2c2c;
  background: #111;
  color: #fff;
}

.clear-filter-btn {
  border: 1px solid #3b3b3b;
  background: #171717;
  color: #fff;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}

.table-card {
  background: #0d0d0d;
  border: 1px solid #222;
  border-radius: 16px;
  padding: 16px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.table-header h3 {
  margin: 0;
  color: #ffd86b;
}

.table-header span {
  color: #aaa;
  font-size: 0.9rem;
}

.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  color: #fff;
}

th,
td {
  padding: 10px 8px;
  border-bottom: 1px solid #222;
  text-align: left;
  font-size: 0.9rem;
}

th {
  color: #c5a059;
  text-transform: uppercase;
  font-size: 0.72rem;
  letter-spacing: 0.06em;
}

select,
input,
textarea {
  border: 1px solid #333;
  background: #151515;
  color: #fff;
  padding: 8px 10px;
  border-radius: 8px;
  width: 100%;
  box-sizing: border-box;
}

textarea {
  min-height: 88px;
  resize: vertical;
}

.btn {
  border: 1px solid #c5a059;
  background: transparent;
  color: #c5a059;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}

.btn:hover {
  background: #c5a059;
  color: #111;
}

.btn-primary {
  background: #c5a059;
  color: #111;
  font-weight: 700;
}

.btn-secondary {
  border-color: #4a4a4a;
  color: #ddd;
}

.empty-state {
  padding: 24px;
  text-align: center;
  color: #888;
}

.walkin-card {
  background: linear-gradient(135deg, rgba(32, 25, 16, 0.96), rgba(14, 14, 14, 0.95));
  border: 1px solid rgba(197, 160, 89, 0.25);
  border-radius: 18px;
  padding: 20px;
  box-shadow: 0 14px 32px rgba(0, 0, 0, 0.28);
}

.walkin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.walkin-header h3 {
  margin: 0 0 4px;
  color: #ffd86b;
}

.walkin-header p {
  margin: 0;
  color: #c7c7c7;
  font-size: 0.92rem;
}

.walkin-badge {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(197, 160, 89, 0.16);
  color: #f3d47d;
  font-size: 0.8rem;
  white-space: nowrap;
}

.walkin-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #e7d9a8;
  font-size: 0.9rem;
}

.field.full-width {
  grid-column: 1 / -1;
  margin-top: 10px;
}

.walkin-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  z-index: 1000;
}

.detail-modal {
  width: min(620px, 100%);
  background: #121212;
  border: 1px solid #333;
  border-radius: 16px;
  padding: 18px;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.45);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.modal-header h3 {
  margin: 0;
  color: #ffd86b;
}

.close-btn {
  border: none;
  background: transparent;
  color: #fff;
  font-size: 1.3rem;
  cursor: pointer;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 16px;
}

.detail-grid > div {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px;
  border: 1px solid #232323;
  border-radius: 10px;
  background: #171717;
}

.detail-grid span {
  color: #8e8e8e;
  font-size: 0.8rem;
}

.detail-grid strong {
  color: #fff;
  font-size: 0.94rem;
}

@media (max-width: 720px) {
  .walkin-grid,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .walkin-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
