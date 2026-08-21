<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import ThongKeApi from '@/api/ThongKeApi'
import { useShiftStore } from '@/stores/ShiftStore'
import RevenueChart from './RevenueChart.vue'
import DepositStatusChart from './DepositStatusChart.vue'
import KhuVucChart from './KhuVucChart.vue'
import GioCaoDiemChart from './GioCaoDiemChart.vue'

const shiftStore = useShiftStore()

const dashboard = ref<any>({})

const topMon = ref<any[]>([])
const topNhanVien = ref<any[]>([])
const tienCoc = ref<any[]>([])
const trangThaiCoc = ref<any[]>([])

const ngay = ref<any[]>([])
const thang = ref<any[]>([])
const nam = ref<any[]>([])
const from = ref("");
const to = ref("");
const khuVuc = ref<any[]>([])
const doanhThuGio = ref<any[]>([])
const topKhachHang = ref<any[]>([])
const khuyenMai = ref<any[]>([])
const danhMuc = ref<any[]>([])
const hieuSuatBan = ref<any[]>([])

const mode = ref<'ngay' | 'thang' | 'nam'>('thang')
const modes = ['ngay', 'thang', 'nam'] as const

// =========================
// BỘ LỌC THỜI GIAN
// =========================

const today = new Date()

const formatDate = (date: Date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

const fromDate = ref(
  `${today.getFullYear()}-01-01`
)

const toDate = ref(
  formatDate(today)
)

const selectedPeriod = ref('thisYear')

const loading = ref(false)
const exporting = ref(false)

// =========================
// CLOCK
// =========================

const now = ref(new Date())
const timeRefresh = ref('')

const updateClock = () => {
  now.value = new Date()

  timeRefresh.value = now.value.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

// =========================
// LOAD STATISTICS
// =========================

const loadStatistics = async () => {

  if (!fromDate.value || !toDate.value) {
    alert('Vui lòng chọn đầy đủ từ ngày và đến ngày')
    return
  }

  if (fromDate.value > toDate.value) {
    alert('Từ ngày không được lớn hơn đến ngày')
    return
  }

  loading.value = true

  try {

    const from = fromDate.value
    const to = toDate.value

    const [
      db,
      mon,
      nv,
      dNgay,
      dThang,
      dNam,
      coc,
      ttCoc,
      kv,
      dGio,
      kh,
      km,
      dm,
      ban
    ] = await Promise.all([

      // Dashboard
      ThongKeApi.dashboard(from, to),

      // Top món
      ThongKeApi.topMon(0, 5, from, to),

      // Top nhân viên
      ThongKeApi.topNhanVien(from, to),

      // Doanh thu
      ThongKeApi.theoNgay(from, to),
      ThongKeApi.theoThang(from, to),
      ThongKeApi.theoNam(from, to),

      // Tiền cọc
      ThongKeApi.tienCocTheoNgay(from, to),

      // Trạng thái cọc
      ThongKeApi.trangThaiCoc(from, to),

      // Khu vực
      ThongKeApi.doanhThuTheoKhuVuc(from, to),

      // Giờ cao điểm
      ThongKeApi.doanhThuTheoGio(from, to),

      // Khách hàng
      ThongKeApi.topKhachHangThanThiet(from, to),

      // Khuyến mãi
      ThongKeApi.hieuQuaKhuyenMai(from, to),

      // Danh mục
      ThongKeApi.doanhThuTheoDanhMuc(from, to),

      // Hiệu suất bàn
      ThongKeApi.hieuSuatBan(from, to),
    ])

    dashboard.value = db?.data || {}

    topMon.value = mon?.data || []
    topNhanVien.value = nv?.data || []

    ngay.value = dNgay?.data || []
    thang.value = dThang?.data || []
    nam.value = dNam?.data || []

    tienCoc.value = coc?.data || []
    trangThaiCoc.value = ttCoc?.data || []

    khuVuc.value = kv?.data || []
    doanhThuGio.value = dGio?.data || []

    topKhachHang.value = kh?.data || []
    khuyenMai.value = km?.data || []

    danhMuc.value = dm?.data || []
    hieuSuatBan.value = ban?.data || []

  } catch (err) {

    console.error('LOAD STATISTICS ERROR:', err)

    alert('Không thể tải dữ liệu thống kê')

  } finally {

    loading.value = false

  }
}
const handlePeriodChange = () => {
  const today = new Date()

  switch (selectedPeriod.value) {

    case 'today': {
      fromDate.value = formatDate(today)
      toDate.value = formatDate(today)
      break
    }

    case '7days': {
      const from = new Date(today)
      from.setDate(today.getDate() - 6)

      fromDate.value = formatDate(from)
      toDate.value = formatDate(today)
      break
    }

    case '30days': {
      const from = new Date(today)
      from.setDate(today.getDate() - 29)

      fromDate.value = formatDate(from)
      toDate.value = formatDate(today)
      break
    }

    case 'thisMonth': {
      const from = new Date(
        today.getFullYear(),
        today.getMonth(),
        1
      )

      fromDate.value = formatDate(from)
      toDate.value = formatDate(today)
      break
    }

    case 'lastMonth': {
      const from = new Date(
        today.getFullYear(),
        today.getMonth() - 1,
        1
      )

      const to = new Date(
        today.getFullYear(),
        today.getMonth(),
        0
      )

      fromDate.value = formatDate(from)
      toDate.value = formatDate(to)
      break
    }

    case 'thisYear': {
      const from = new Date(
        today.getFullYear(),
        0,
        1
      )

      fromDate.value = formatDate(from)
      toDate.value = formatDate(today)
      break
    }

    case 'custom':
      return
  }

  loadStatistics()
}
// =========================
// ĐỔI MODE BIỂU ĐỒ
// =========================

const changeMode = (newMode: 'ngay' | 'thang' | 'nam') => {
  mode.value = newMode
}
// =========================
// EXPORT EXCEL
// =========================

const exportExcel = async () => {
  try {
    exporting.value = true

    const response = await ThongKeApi.exportExcel(
      fromDate.value,
      toDate.value
    )

    const blob = new Blob(
      [response.data],
      {
        type: "application/vnd.openxml  formats-officedocument.spreadsheetml.sheet"
      }
    )

    const url = window.URL.createObjectURL(blob)

    const link = document.createElement("a")
    link.href = url
    link.download = `Bao-cao-thong-ke-${fromDate.value}-${toDate.value}.xlsx`

    document.body.appendChild(link)
    link.click()

    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

  } catch (error) {
    console.error("Lỗi xuất Excel:", error)
    alert("Không thể xuất file Excel")
  } finally {
    exporting.value = false
  }
}

// =========================
// CHART DATA
// =========================

const totalQuyHienTai = computed(() => {
  const openingCash = Number(shiftStore.currentShift?.openingCash || 0)
  const revenue = Number(shiftStore.invoiceRevenue || 0)
  const income = Number(shiftStore.cashIncome + shiftStore.transferIncome + shiftStore.electronicIncome + shiftStore.otherIncome)
  const expense = Number(shiftStore.cashExpense + shiftStore.transferExpense + shiftStore.electronicExpense + shiftStore.otherExpense)

  return openingCash + revenue + income - expense
})

const chartData = computed(() => {

  let data: any[] = []

  if (mode.value === 'ngay') {
    data = ngay.value
  }
  else if (mode.value === 'nam') {
    data = nam.value
  }
  else {
    data = thang.value
  }

  return (data || []).map((i) => {

    let thoiGian = ''

    if (mode.value === 'ngay') {

      thoiGian = i?.thoiGian
        ? i.thoiGian.slice(5)
        : ''

    }
    else if (mode.value === 'thang') {

      thoiGian = i?.thoiGian || ''

    }
    else if (mode.value === 'nam') {

      thoiGian = i?.thoiGian
        ? i.thoiGian.toString().slice(0, 4)
        : ''

    }

    return {
      thoiGian,

      tongDoanhThu:
        i?.tongDoanhThu ||
        i?.doanhThu ||
        i?.tongTien ||
        0,
    }

  })

})

// =========================
// ON MOUNT
// =========================

onMounted(() => {

  loadStatistics()

  updateClock()

  setInterval(updateClock, 1000)

})
</script>

<template>
  <div class="thong-ke-page">
    <!-- =========================
         KPI CARDS
    ========================== -->
    <div class="kpi-grid">
      <!-- TIỀN MẶT -->
      <div class="kpi kpi-orange">
        <div class="kpi-icon">💵</div>

        <div class="kpi-content">
          <span class="kpi-label">Doanh thu tiền mặt</span>

          <span class="kpi-value">
            {{ Number(dashboard.doanhThuTienMat || 0).toLocaleString() }} đ
          </span>
        </div>
      </div>


      <!-- CHUYỂN KHOẢN -->
      <div class="kpi kpi-blue">
        <div class="kpi-icon">🏦</div>

        <div class="kpi-content">
          <span class="kpi-label">Doanh thu chuyển khoản</span>

          <span class="kpi-value">
            {{ Number(dashboard.doanhThuChuyenKhoan || 0).toLocaleString() }} đ
          </span>
        </div>
      </div>


      <!-- TỔNG HÓA ĐƠN -->
      <div class="kpi kpi-purple">
        <div class="kpi-icon">🧾</div>

        <div class="kpi-content">
          <span class="kpi-label">Tổng hóa đơn</span>

          <span class="kpi-value">
            {{ dashboard.tongHoaDon || 0 }}
          </span>
        </div>
      </div>


      <!-- KHÁCH HÀNG -->
      <div class="kpi kpi-teal">
        <div class="kpi-icon">👥</div>

        <div class="kpi-content">
          <span class="kpi-label">Khách hàng</span>

          <span class="kpi-value">
            {{ dashboard.tongKhachHang || 0 }}
          </span>
        </div>
      </div>


      <!-- TIỀN CỌC -->
      <div class="kpi kpi-orange">
        <div class="kpi-icon">💳</div>

        <div class="kpi-content">
          <span class="kpi-label">Tiền cọc</span>

          <span class="kpi-value">
            {{ Number(dashboard.tongTienCoc || 0).toLocaleString() }} đ
          </span>
        </div>
      </div>


      <!-- ĐÃ CỌC -->
      <div class="kpi kpi-teal">
        <div class="kpi-icon">✅</div>

        <div class="kpi-content">
          <span class="kpi-label">Đã cọc</span>

          <span class="kpi-value">
            {{ dashboard.soDonDaCoc || 0 }}
          </span>
        </div>
      </div>


      <!-- CHƯA CỌC -->
      <div class="kpi kpi-red">
        <div class="kpi-icon">⏳</div>

        <div class="kpi-content">
          <span class="kpi-label">Chưa cọc</span>

          <span class="kpi-value">
            {{ dashboard.soDonChuaCoc || 0 }}
          </span>
        </div>
      </div>

      <!-- TỔNG QUỸ HIỆN TẠI -->
      <div class="kpi kpi-green">
        <div class="kpi-icon">💰</div>

        <div class="kpi-content">
          <span class="kpi-label">Tổng quỹ hiện tại</span>

          <span class="kpi-value">
            {{ Number(totalQuyHienTai || 0).toLocaleString() }} đ
          </span>
        </div>
      </div>

    </div>
<!-- BỘ LỌC -->
<div class="period-filter">
  <select
    v-model="selectedPeriod"
    @change="handlePeriodChange"
  >
    <option value="today">Hôm nay</option>
    <option value="7days">7 ngày qua</option>
    <option value="30days">30 ngày qua</option>
    <option value="thisMonth">Tháng này</option>
    <option value="lastMonth">Tháng trước</option>
    <option value="thisYear">Năm nay</option>
    <option value="custom">Tùy chỉnh</option>
  </select>

  <template v-if="selectedPeriod === 'custom'">
    <input
      v-model="fromDate"
      type="date"
    />

    <span>→</span>

    <input
      v-model="toDate"
      type="date"
    />

    <button
      class="btn-filter"
      @click="loadStatistics"
      :disabled="loading"
    >
      🔍
    </button>
  </template>

  <button
    class="btn-excel"
    @click="exportExcel"
    :disabled="loading || exporting"
  >
    📥 Excel
  </button>
</div>

    <!-- =========================
         MAIN LAYOUT
    ========================== -->
    <div class="main-grid">

      <!-- =========================
           CỘT TRÁI
      ========================== -->
      <div class="col-left">


        <!-- DOANH THU -->
        <div class="card">

          <div class="card-header">

            <h3>📊 Doanh thu</h3>

            <div class="filter-chip">

              <button
                v-for="m in modes"
                :key="m"
                @click="changeMode(m)"
                :class="{ active: mode === m }"
              >
                {{
                  m === "ngay"
                    ? "Ngày"
                    : m === "thang"
                      ? "Tháng"
                      : "Năm"
                }}
              </button>

            </div>

          </div>

          <div class="card-body">

            <RevenueChart
              :key="mode + chartData.length"
              :data="chartData"
              :mode="mode"
            />

          </div>

        </div>


        <!-- GIỜ CAO ĐIỂM -->
        <div class="card">

          <div class="card-header">
            <h3>⏰ Doanh thu theo khung giờ</h3>
          </div>

          <div class="card-body">

            <GioCaoDiemChart
              :data="doanhThuGio"
            />

          </div>

        </div>


        <!-- KHU VỰC -->
        <div class="card">

          <div class="card-header">
            <h3>📍 Doanh thu theo khu vực</h3>
          </div>

          <div class="card-body">

            <KhuVucChart
              :data="khuVuc"
            />

          </div>

        </div>


        <!-- DANH MỤC -->
        <div class="card">

          <div class="card-header">
            <h3>🍲 Doanh thu theo danh mục</h3>
          </div>

          <div class="card-body">

            <div
              class="list-row"
              v-for="(dm, idx) in danhMuc"
              :key="dm.danhMuc"
            >

              <span class="rank">
                {{ idx + 1 }}
              </span>

              <span class="name">
                {{ dm.danhMuc }}
              </span>

              <span class="count">
                {{ dm.soLuongBan }} món
              </span>

              <span class="amount">
                {{ Number(dm.tongThu || 0).toLocaleString() }} đ
              </span>

            </div>

            <div
              v-if="danhMuc.length === 0"
              class="empty"
            >
              Chưa có dữ liệu
            </div>

          </div>

        </div>

      </div>


      <!-- =========================
           CỘT PHẢI
      ========================== -->
      <div class="col-right">


        <!-- TRẠNG THÁI CỌC -->
        <div class="card">

          <div class="card-header">
            <h3>📌 Trạng thái cọc</h3>
          </div>

          <div class="card-body">

            <DepositStatusChart
              :data="trangThaiCoc"
            />

          </div>

        </div>


        <!-- TOP MÓN -->
        <div class="card">

          <div class="card-header">
            <h3>🔥 Top món bán chạy</h3>
          </div>

          <div class="card-body">

            <div
              class="list-row"
              v-for="(m, idx) in topMon"
              :key="m.tenMon"
            >

              <span class="rank">
                {{ idx + 1 }}
              </span>

              <span class="name">
                {{ m.tenMon }}
              </span>

              <span class="count">
                {{ m.soLuongBan }} món
              </span>

            </div>

            <div
              v-if="topMon.length === 0"
              class="empty"
            >
              Chưa có dữ liệu
            </div>

          </div>

        </div>


        <!-- TOP NHÂN VIÊN -->
        <div class="card">

          <div class="card-header">
            <h3>👨‍🍳 Top nhân viên</h3>
          </div>

          <div class="card-body">

            <div
              class="list-row"
              v-for="(nv, idx) in topNhanVien"
              :key="nv.tenNhanVien"
            >

              <span class="rank">
                {{ idx + 1 }}
              </span>

              <span class="name">
                {{ nv.tenNhanVien }}
              </span>

              <span class="amount">
                {{ Number(nv.tongDoanhThu || 0).toLocaleString() }} đ
              </span>

            </div>

            <div
              v-if="topNhanVien.length === 0"
              class="empty"
            >
              Chưa có dữ liệu
            </div>

          </div>

        </div>


        <!-- TOP KHÁCH HÀNG -->
        <div class="card">

          <div class="card-header">
            <h3>⭐ Khách hàng VIP</h3>
          </div>

          <div class="card-body">

            <div
              class="list-row"
              v-for="(kh, idx) in topKhachHang"
              :key="kh.soDienThoai"
            >

              <span class="rank">
                {{ idx + 1 }}
              </span>

              <span class="name">
                {{ kh.tenKhachHang }}
              </span>

              <span class="amount">
                {{ Number(kh.tongChiTieu || 0).toLocaleString() }} đ
              </span>

            </div>

            <div
              v-if="topKhachHang.length === 0"
              class="empty"
            >
              Chưa có dữ liệu
            </div>

          </div>

        </div>


        <!-- KHUYẾN MÃI -->
        <div class="card">

          <div class="card-header">
            <h3>🎫 Hiệu quả khuyến mãi</h3>
          </div>

          <div class="card-body">

            <div
              class="list-row"
              v-for="km in khuyenMai"
              :key="km.maGiamGia"
            >

              <span class="tag">
                {{ km.maGiamGia }}
              </span>

              <span class="name">
                {{ km.soLanSuDung }} lần
              </span>

              <span class="discount">
                -{{ Number(km.tongTienDaGiam || 0).toLocaleString() }} đ
              </span>

            </div>

            <div
              v-if="khuyenMai.length === 0"
              class="empty"
            >
              Chưa có dữ liệu
            </div>

          </div>

        </div>


        <!-- HIỆU SUẤT BÀN -->
        <div class="card">

          <div class="card-header">
            <h3>🪑 Hiệu suất bàn</h3>
          </div>

          <div class="card-body">

            <div
              class="list-row"
              v-for="ban in hieuSuatBan"
              :key="ban.tenBan"
            >

              <span class="name">
                {{ ban.tenBan }}
              </span>

              <span class="count">
                {{ ban.soLanPhucVu }} lần
              </span>

              <span class="amount">
                {{ Number(ban.tongDoanhThu || 0).toLocaleString() }} đ
              </span>

            </div>

            <div
              v-if="hieuSuatBan.length === 0"
              class="empty"
            >
              Chưa có dữ liệu
            </div>

          </div>

        </div>

      </div>

    </div>

  </div>
</template>

<style scoped>
/* ========== RESET & FONT ========== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.pos-dashboard {
  font-family:
    'SF Pro Display',
    'Inter',
    -apple-system,
    sans-serif;
  background: #f0f2f5;
  min-height: 100vh;
  padding: 20px 24px;
  color: #1e293b;
}

/* ========== TOP BAR ========== */
.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 16px 24px;
  border-radius: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 36px;
}

.logo-area h1 {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.subtitle {
  font-size: 12px;
  color: #94a3b8;
}

.right-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.date {
  font-size: 13px;
  color: #64748b;
  text-transform: capitalize;
}

.clock {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

/* ========== KPI GRID ========== */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
  margin-bottom: 20px;
}

.kpi {
  background: #fff;
  padding: 16px 18px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: transform 0.15s;
}

.kpi:hover {
  transform: translateY(-2px);
}

.kpi-icon {
  font-size: 32px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
}

.kpi-green .kpi-icon {
  background: #dcfce7;
}
.kpi-blue .kpi-icon {
  background: #dbeafe;
}
.kpi-purple .kpi-icon {
  background: #ede9fe;
}
.kpi-orange .kpi-icon {
  background: #ffedd5;
}
.kpi-teal .kpi-icon {
  background: #ccfbf1;
}
.kpi-red .kpi-icon {
  background: #fee2e2;
}

.kpi-content {
  display: flex;
  flex-direction: column;
}

.kpi-label {
  font-size: 11px;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.kpi-value {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}

/* ========== MAIN GRID 2 CỘT ========== */
.main-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 18px;
}

/* ========== CARD ========== */
.card {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  margin-bottom: 18px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
}

.card-header h3 {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.card-body {
  padding: 16px 20px;
}

/* ========== FILTER CHIP ========== */
.filter-chip {
  display: flex;
  gap: 6px;
  background: #f1f5f9;
  border-radius: 10px;
  padding: 3px;
}

.filter-chip button {
  padding: 6px 14px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-chip button.active {
  background: #fff;
  color: #0f172a;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
}

/* ========== LIST ROW ========== */
.list-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f8fafc;
}

.list-row:last-child {
  border-bottom: none;
}

.rank {
  width: 24px;
  height: 24px;
  background: #f1f5f9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
}

.name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
}

.count {
  font-size: 13px;
  color: #64748b;
}

.amount {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
}

.tag {
  background: #ede9fe;
  color: #7c3aed;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.discount {
  font-size: 13px;
  font-weight: 600;
  color: #ef4444;
}

.empty {
  text-align: center;
  padding: 20px;
  color: #94a3b8;
  font-size: 13px;
}

/* ========== RESPONSIVE ========== */
@media (max-width: 1200px) {
  .kpi-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .main-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .topbar {
    flex-direction: column;
    gap: 10px;
  }

  .right-info {
    align-items: center;
  }
}
.period-filter {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  margin: 16px 0 20px;
}

.period-filter select {
  height: 40px;
  min-width: 160px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
  font-size: 14px;
  outline: none;
  cursor: pointer;
}

.period-filter input {
  height: 40px;
  padding: 0 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.period-filter select:focus,
.period-filter input:focus {
  border-color: #409eff;
}

.period-filter .btn-excel {
  height: 40px;
  padding: 0 15px;
  border: none;
  border-radius: 8px;
  background: #198754;
  color: white;
  font-weight: 600;
  cursor: pointer;
}

.period-filter .btn-excel:hover {
  background: #157347;
}

.period-filter .btn-filter {
  height: 40px;
  width: 42px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
</style>
