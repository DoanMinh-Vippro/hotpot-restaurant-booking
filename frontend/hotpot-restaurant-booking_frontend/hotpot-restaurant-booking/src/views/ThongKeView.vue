<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import ThongKeApi from '@/api/ThongKeApi'
import RevenueChart from './RevenueChart.vue'
import DepositStatusChart from './DepositStatusChart.vue'
import KhuVucChart from './KhuVucChart.vue'
import GioCaoDiemChart from './GioCaoDiemChart.vue'

const dashboard = ref<any>({})
const topMon = ref<any[]>([])
const topNhanVien = ref<any[]>([])
const tienCoc = ref<any[]>([])
const trangThaiCoc = ref<any[]>([])
const ngay = ref<any[]>([])
const thang = ref<any[]>([])
const nam = ref<any[]>([])
const khuVuc = ref<any[]>([])
const doanhThuGio = ref<any[]>([])
const topKhachHang = ref<any[]>([])
const khuyenMai = ref<any[]>([])

const mode = ref<'ngay' | 'thang' | 'nam'>('thang')
const modes = ['ngay', 'thang', 'nam'] as const

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

const load = async () => {
  try {
    const [db, mon, nv, dNgay, dThang, dNam, coc, ttCoc, kv, dGio, kh, km] = await Promise.all([
      ThongKeApi.dashboard(),
      ThongKeApi.topMon(0, 5),
      ThongKeApi.topNhanVien(),
      ThongKeApi.theoNgay('2026-01-01', '2026-12-31'),
      ThongKeApi.theoThang(),
      ThongKeApi.theoNam(),
      ThongKeApi.tienCocTheoNgay(),
      ThongKeApi.trangThaiCoc(),
      ThongKeApi.doanhThuTheoKhuVuc(),
      ThongKeApi.doanhThuTheoGio(),
      ThongKeApi.topKhachHangThanThiet(),
      ThongKeApi.hieuQuaKhuyenMai(),
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
  } catch (err) {
    console.error('LOAD ERROR:', err)
  }
}

const chartData = computed(() => {
  let data: any[] = []
  if (mode.value === 'ngay') data = ngay.value
  else if (mode.value === 'nam') data = nam.value
  else data = thang.value

  return (data || []).map((i) => {
    let thoiGian = ''
    if (mode.value === 'ngay') thoiGian = i?.thoiGian ? i.thoiGian.slice(5) : ''
    else if (mode.value === 'thang') thoiGian = i?.thoiGian || ''
    else if (mode.value === 'nam') thoiGian = i?.thoiGian ? i.thoiGian.toString().slice(0, 4) : ''

    return {
      thoiGian,
      tongDoanhThu: i?.tongDoanhThu || i?.doanhThu || i?.tongTien || 0,
    }
  })
})

onMounted(() => {
  load()
  updateClock()
  setInterval(updateClock, 1000)
})
</script>

<template>
  <div class="pos-dashboard">
    <!-- ====== TOP BAR ====== -->
    <div class="topbar">
      <div class="logo-area">
        <span class="logo-icon">🍲</span>
        <div>
          <h1>Hotpot Restaurant</h1>
          <span class="subtitle">POS Dashboard · Real-time</span>
        </div>
      </div>
      <div class="right-info">
        <span class="date">{{
          now.toLocaleDateString('vi-VN', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric',
          })
        }}</span>
        <span class="clock">{{ timeRefresh }}</span>
      </div>
    </div>

    <!-- ====== KPI CARDS ====== -->
    <div class="kpi-grid">
      <div class="kpi kpi-green">
        <div class="kpi-icon">💰</div>
        <div class="kpi-content">
          <span class="kpi-label">Tổng doanh thu</span>
          <span class="kpi-value"
            >{{ Number(dashboard.tongDoanhThu || 0).toLocaleString() }} đ</span
          >
        </div>
      </div>

      <div class="kpi kpi-blue">
        <div class="kpi-icon">🧾</div>
        <div class="kpi-content">
          <span class="kpi-label">Tổng hóa đơn</span>
          <span class="kpi-value">{{ dashboard.tongHoaDon || 0 }}</span>
        </div>
      </div>

      <div class="kpi kpi-purple">
        <div class="kpi-icon">👥</div>
        <div class="kpi-content">
          <span class="kpi-label">Khách hàng</span>
          <span class="kpi-value">{{ dashboard.tongKhachHang || 0 }}</span>
        </div>
      </div>

      <div class="kpi kpi-orange">
        <div class="kpi-icon">💵</div>
        <div class="kpi-content">
          <span class="kpi-label">Tiền cọc</span>
          <span class="kpi-value">{{ Number(dashboard.tongTienCoc || 0).toLocaleString() }} đ</span>
        </div>
      </div>

      <div class="kpi kpi-teal">
        <div class="kpi-icon">✅</div>
        <div class="kpi-content">
          <span class="kpi-label">Đã cọc</span>
          <span class="kpi-value">{{ dashboard.soDonDaCoc || 0 }}</span>
        </div>
      </div>

      <div class="kpi kpi-red">
        <div class="kpi-icon">⏳</div>
        <div class="kpi-content">
          <span class="kpi-label">Chưa cọc</span>
          <span class="kpi-value">{{ dashboard.soDonChuaCoc || 0 }}</span>
        </div>
      </div>
    </div>

    <!-- ====== MAIN LAYOUT 2 CỘT ====== -->
    <div class="main-grid">
      <!-- CỘT TRÁI -->
      <div class="col-left">
        <!-- DOANH THU CHART -->
        <div class="card">
          <div class="card-header">
            <h3>📊 Doanh thu</h3>
            <div class="filter-chip">
              <button v-for="m in modes" :key="m" @click="mode = m" :class="{ active: mode === m }">
                {{ m === 'ngay' ? 'Ngày' : m === 'thang' ? 'Tháng' : 'Năm' }}
              </button>
            </div>
          </div>
          <div class="card-body">
            <RevenueChart :key="mode + chartData.length" :data="chartData" :mode="mode" />
          </div>
        </div>

        <!-- GIỜ CAO ĐIỂM -->
        <div class="card">
          <div class="card-header">
            <h3>⏰ Doanh thu theo khung giờ</h3>
          </div>
          <div class="card-body">
            <GioCaoDiemChart :data="doanhThuGio" />
          </div>
        </div>

        <!-- KHU VỰC -->
        <div class="card">
          <div class="card-header">
            <h3>📍 Doanh thu theo khu vực</h3>
          </div>
          <div class="card-body">
            <KhuVucChart :data="khuVuc" />
          </div>
        </div>
      </div>

      <!-- CỘT PHẢI -->
      <div class="col-right">
        <!-- TRẠNG THÁI CỌC -->
        <div class="card">
          <div class="card-header">
            <h3>📌 Trạng thái cọc</h3>
          </div>
          <div class="card-body">
            <DepositStatusChart :data="trangThaiCoc" />
          </div>
        </div>

        <!-- TOP MÓN -->
        <div class="card">
          <div class="card-header">
            <h3>🔥 Top món bán chạy</h3>
          </div>
          <div class="card-body">
            <div class="list-row" v-for="(m, idx) in topMon" :key="m.tenMon">
              <span class="rank">{{ idx + 1 }}</span>
              <span class="name">{{ m.tenMon }}</span>
              <span class="count">{{ m.soLuongBan }} món</span>
            </div>
            <div v-if="topMon.length === 0" class="empty">Chưa có dữ liệu</div>
          </div>
        </div>

        <!-- TOP NHÂN VIÊN -->
        <div class="card">
          <div class="card-header">
            <h3>👨‍🍳 Top nhân viên</h3>
          </div>
          <div class="card-body">
            <div class="list-row" v-for="(nv, idx) in topNhanVien" :key="nv.tenNhanVien">
              <span class="rank">{{ idx + 1 }}</span>
              <span class="name">{{ nv.tenNhanVien }}</span>
              <span class="amount">{{ Number(nv.tongDoanhThu).toLocaleString() }} đ</span>
            </div>
            <div v-if="topNhanVien.length === 0" class="empty">Chưa có dữ liệu</div>
          </div>
        </div>

        <!-- TOP KHÁCH HÀNG -->
        <div class="card">
          <div class="card-header">
            <h3>⭐ Khách hàng VIP</h3>
          </div>
          <div class="card-body">
            <div class="list-row" v-for="(kh, idx) in topKhachHang" :key="kh.soDienThoai">
              <span class="rank">{{ idx + 1 }}</span>
              <span class="name">{{ kh.tenKhachHang }}</span>
              <span class="amount">{{ Number(kh.tongChiTieu).toLocaleString() }} đ</span>
            </div>
            <div v-if="topKhachHang.length === 0" class="empty">Chưa có dữ liệu</div>
          </div>
        </div>

        <!-- KHUYẾN MÃI -->
        <div class="card">
          <div class="card-header">
            <h3>🎫 Hiệu quả khuyến mãi</h3>
          </div>
          <div class="card-body">
            <div class="list-row" v-for="km in khuyenMai" :key="km.maGiamGia">
              <span class="tag">{{ km.maGiamGia }}</span>
              <span class="name">{{ km.soLanSuDung }} lần</span>
              <span class="discount">-{{ Number(km.tongTienDaGiam).toLocaleString() }} đ</span>
            </div>
            <div v-if="khuyenMai.length === 0" class="empty">Chưa có dữ liệu</div>
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
  grid-template-columns: repeat(6, 1fr);
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
</style>
