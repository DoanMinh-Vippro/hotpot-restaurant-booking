<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

type HoaDon = {
  idHoaDon: number
  maHoaDon: string
  maGiaoDich: string | null
  trangThaiHoaDon: number | null
  sdtKhachHang: string | null
  tienTruocGiam: number | string | null
  tienCoc: number | string | null
  tienGiamGia: number | string | null
  tongTien: number | string | null
  thoiGianXuat: string | number[] | null
  idBan: number | null
  loaiBan: string | null
  idDatBan: number | null
  idGiamGia?: number | null
  maGiamGia?: string | null
  loaiGiam?: string | null
  idKhachHang: number | null
  tenKhachHang: string | null
  idNhanVien: number | null
  tenNhanVien: string | null
  trangThaiThanhToan: number | null
  phuongThucThanhToan: number | null
}

type HoaDonChiTiet = {
  idHoaDonChiTiet: number
  maHoaDonChiTiet: string
  tenMon: string | null
  tenCombo: string | null
  comboItems?: string[] | null
  soLuong: number | null
  giaBanTaiThoiDiem: number | string | null
  tienGiamGiaMon: number | string | null
  thanhTien: number | string | null
}

// Khi chạy dev, Vite proxy /api → http://localhost:8080, nên để mặc định rỗng.
// Trong production, đặt VITE_API_BASE_URL=https://your-server.com
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? ''

const hoaDons = ref<HoaDon[]>([])
const chiTiets = ref<HoaDonChiTiet[]>([])
const selectedId = ref<number | null>(null)
const loadingHoaDon = ref(false)
const loadingChiTiet = ref(false)
const errorMessage = ref('')

const selectedHoaDon = computed(() => hoaDons.value.find((item) => item.idHoaDon === selectedId.value))

const searchQuery = ref('')
const filteredHoaDons = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return hoaDons.value

  return hoaDons.value.filter((item) => {
    return [
      item.maHoaDon,
      item.tenKhachHang,
      item.sdtKhachHang,
      item.maGiaoDich,
    ]
      .filter(Boolean)
      .some((field) => String(field).toLowerCase().includes(q))
  })
})

const paidCount = computed(
  () => hoaDons.value.filter((item) => item.trangThaiThanhToan === 1).length,
)

const formatCurrency = (value: number | string | null) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(toNumber(value))

const formatDateTime = (value: string | number[] | null) => {
  if (!value) return 'Chưa xuất'

  let date: Date
  if (Array.isArray(value)) {
    // Spring Boot có thể serialize LocalDateTime thành array [year, month, day, hour, minute, second]
    const [
      year = 0,
      month = 1,
      day = 1,
      hour = 0,
      minute = 0,
      second = 0,
    ] = value
    date = new Date(year, month - 1, day, hour, minute, second)
  } else {
    date = new Date(value)
  }

  if (isNaN(date.getTime())) return String(value)

  return new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date)
}

const toNumber = (value: number | string | null) => Number(value ?? 0)

const invoiceStatusLabel = (status: number | null) => {
  if (status === 1) return 'Đã xuất'
  if (status === 0) return 'Nháp'
  return 'Không rõ'
}

const paymentStatusLabel = (status: number | null) => {
  if (status === 1) return 'Đã thanh toán'
  if (status === 0) return 'Chưa thanh toán'
  return 'Không rõ'
}

const paymentMethodLabel = (method: number | null) => {
  if (method === 1) return 'Tiền mặt'
  if (method === 2) return 'Chuyển khoản'
  if (method === 3) return 'Thẻ'
  return 'Chưa có'
}

const itemName = (item: HoaDonChiTiet) => item.tenMon ?? item.tenCombo ?? 'Món chưa đặt tên'

const loadHoaDons = async () => {
  loadingHoaDon.value = true
  errorMessage.value = ''

  try {
    const response = await fetch(`${apiBaseUrl}/api/hoa-don`)
    if (!response.ok) throw new Error(`HTTP ${response.status}`)

    hoaDons.value = await response.json()
    selectedId.value = hoaDons.value[0]?.idHoaDon ?? null

    if (selectedId.value) {
      await loadChiTiet(selectedId.value)
    }
  } catch (error) {
    errorMessage.value = 'Không tải được dữ liệu hóa đơn. Kiểm tra backend và đường dẫn API.'
    console.error(error)
  } finally {
    loadingHoaDon.value = false
  }
}

const loadChiTiet = async (idHoaDon: number) => {
  selectedId.value = idHoaDon
  loadingChiTiet.value = true
  errorMessage.value = ''

  try {
    const response = await fetch(`${apiBaseUrl}/api/hoa-don/${idHoaDon}/chi-tiet`)
    if (!response.ok) throw new Error(`HTTP ${response.status}`)

    chiTiets.value = await response.json()
  } catch (error) {
    chiTiets.value = []
    errorMessage.value = 'Không tải được chi tiết hóa đơn.'
    console.error(error)
  } finally {
    loadingChiTiet.value = false
  }
}

onMounted(loadHoaDons)
</script>

<template>
  <main class="invoice-page">
    <header class="invoice-header">
      <button class="ghost-button" type="button" @click="$router.push('/')">Trang chủ</button>
      <div>
        <p class="eyebrow">Quản lý thanh toán</p>
        <h1>Hóa đơn</h1>
      </div>
      <button class="primary-button" type="button" @click="loadHoaDons">Tải lại</button>
    </header>

    <section class="summary-grid">
      <div class="summary-item">
        <span>Tổng hóa đơn</span>
        <strong>{{ hoaDons.length }}</strong>
      </div>
      <div class="summary-item">
        <span>Đã thanh toán</span>
        <strong>{{ paidCount }}</strong>
      </div>
    </section>

    <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

    <section class="invoice-workspace">
      <aside class="invoice-list" aria-label="Danh sách hóa đơn">
        <div class="panel-title">
          <h2>Tìm kiếm hóa đơn</h2>
          <span v-if="loadingHoaDon">Đang tải...</span>
        </div>
        <input
          class="search-input"
          type="search"
          v-model="searchQuery"
          placeholder="Nhập mã hóa đơn, tên khách, số điện thoại..."
        />

        <button
          v-for="hoaDon in filteredHoaDons"
          :key="hoaDon.idHoaDon"
          :class="['invoice-row', { active: hoaDon.idHoaDon === selectedId }]"
          type="button"
          @click="loadChiTiet(hoaDon.idHoaDon)"
        >
          <span>
            <strong>{{ hoaDon.maHoaDon }}</strong>
            <small>{{ hoaDon.tenKhachHang ?? hoaDon.sdtKhachHang ?? 'Khách lẻ' }}</small>
          </span>
          <span class="row-right">
            <b>{{ formatCurrency(hoaDon.tongTien) }}</b>
            <small>{{ paymentStatusLabel(hoaDon.trangThaiThanhToan) }}</small>
          </span>
        </button>

        <p v-if="!loadingHoaDon && filteredHoaDons.length === 0" class="empty-state">
          Không tìm thấy hóa đơn phù hợp.
        </p>
      </aside>

      <section class="invoice-detail" aria-label="Chi tiết hóa đơn">
        <template v-if="selectedHoaDon">
          <div class="detail-head">
            <div>
              <p class="eyebrow">{{ invoiceStatusLabel(selectedHoaDon.trangThaiHoaDon) }}</p>
              <h2>{{ selectedHoaDon.maHoaDon }}</h2>
              <span>{{ formatDateTime(selectedHoaDon.thoiGianXuat) }}</span>
            </div>
            <div class="total-box">
              <span>Tổng tiền</span>
              <strong>{{ formatCurrency(selectedHoaDon.tongTien) }}</strong>
            </div>
          </div>

          <div class="info-grid">
            <div>
              <span>Khách hàng</span>
              <strong>{{ selectedHoaDon.tenKhachHang ?? 'Khách lẻ' }}</strong>
            </div>
            <div>
              <span>Số điện thoại</span>
              <strong>{{ selectedHoaDon.sdtKhachHang ?? 'Chưa có' }}</strong>
            </div>
            <div>
              <span>Bàn</span>
              <strong>{{ selectedHoaDon.loaiBan ?? `Bàn ${selectedHoaDon.idBan ?? '-'}` }}</strong>
            </div>
            <div>
              <span>Nhân viên</span>
              <strong>{{ selectedHoaDon.tenNhanVien ?? 'Chưa có' }}</strong>
            </div>
            <div>
              <span>Thanh toán</span>
              <strong>{{ paymentStatusLabel(selectedHoaDon.trangThaiThanhToan) }}</strong>
            </div>
            <div>
              <span>Phương thức</span>
              <strong>{{ paymentMethodLabel(selectedHoaDon.phuongThucThanhToan) }}</strong>
            </div>
          </div>

          <div class="amount-grid">
            <div>
              <span>Trước giảm</span>
              <strong>{{ formatCurrency(selectedHoaDon.tienTruocGiam) }}</strong>
            </div>
            <div>
              <span>Tiền cọc</span>
              <strong>{{ formatCurrency(selectedHoaDon.tienCoc) }}</strong>
            </div>
            <div>
              <span>Giảm giá</span>
              <strong>
                {{ formatCurrency(selectedHoaDon.tienGiamGia) }}
                <template v-if="selectedHoaDon.maGiamGia">
                  ({{ selectedHoaDon.maGiamGia }})
                </template>
              </strong>
            </div>
          </div>

          <div class="table-wrap">
            <div class="panel-title">
              <h2>Chi tiết món</h2>
              <span v-if="loadingChiTiet">Đang tải...</span>
            </div>

            <table>
              <thead>
                <tr>
                  <th>Mã</th>
                  <th>Món / combo</th>
                  <th>Số lượng</th>
                  <th>Đơn giá</th>
                  <th>Giảm</th>
                  <th>Thành tiền</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in chiTiets" :key="item.idHoaDonChiTiet">
                  <td>{{ item.maHoaDonChiTiet }}</td>
                  <td>
                    <div>{{ itemName(item) }}</div>
                    <template v-if="item.comboItems?.length">
                      <div class="combo-items">Gồm: {{ item.comboItems.join(', ') }}</div>
                    </template>
                  </td>
                  <td>{{ item.soLuong ?? 0 }}</td>
                  <td>{{ formatCurrency(item.giaBanTaiThoiDiem) }}</td>
                  <td>{{ formatCurrency(item.tienGiamGiaMon) }}</td>
                  <td>{{ formatCurrency(item.thanhTien) }}</td>
                </tr>
              </tbody>
            </table>

            <p v-if="!loadingChiTiet && chiTiets.length === 0" class="empty-state">
              Hóa đơn này chưa có chi tiết món.
            </p>
          </div>
        </template>

        <p v-else class="empty-state">Chọn một hóa đơn để xem chi tiết.</p>
      </section>
    </section>
  </main>
</template>

<style scoped>
.invoice-page {
  min-height: 100vh;
  padding: 32px clamp(16px, 4vw, 56px);
  color: #f7f2e9;
  background:
    linear-gradient(rgba(8, 8, 8, 0.92), rgba(8, 8, 8, 0.96)),
    url('https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=1800&q=80')
      center / cover fixed;
  font-family:
    Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.invoice-header,
.summary-grid,
.invoice-workspace,
.detail-head,
.info-grid,
.amount-grid {
  max-width: 1440px;
  margin-inline: auto;
}

.invoice-header {
  display: grid;
  grid-template-columns: 120px 1fr 120px;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
}

.invoice-header h1,
.detail-head h2,
.panel-title h2 {
  margin: 0;
  letter-spacing: 0;
}

.invoice-header h1 {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: clamp(2.1rem, 5vw, 4rem);
  text-align: center;
}

.eyebrow {
  margin: 0 0 8px;
  color: #d7b46a;
  font-size: 0.75rem;
  font-weight: 700;
  text-align: center;
  text-transform: uppercase;
}

.primary-button,
.ghost-button,
.invoice-row {
  border: 1px solid rgba(215, 180, 106, 0.45);
  color: #f7f2e9;
  cursor: pointer;
}

.primary-button,
.ghost-button {
  min-height: 42px;
  border-radius: 6px;
  padding: 0 16px;
  font-weight: 700;
}

.primary-button {
  background: #d7b46a;
  color: #16110a;
}

.ghost-button {
  background: rgba(255, 255, 255, 0.06);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}

.summary-item,
.invoice-list,
.invoice-detail {
  border: 1px solid rgba(215, 180, 106, 0.2);
  background: rgba(14, 14, 14, 0.88);
  backdrop-filter: blur(10px);
}

.summary-item {
  border-radius: 8px;
  padding: 18px;
}

.summary-item span,
.info-grid span,
.amount-grid span,
.total-box span {
  display: block;
  margin-bottom: 6px;
  color: #b7ad9c;
  font-size: 0.78rem;
}

.summary-item strong {
  font-size: clamp(1.4rem, 3vw, 2rem);
}

.invoice-workspace {
  display: grid;
  grid-template-columns: minmax(280px, 380px) 1fr;
  gap: 18px;
}

.invoice-list,
.invoice-detail {
  border-radius: 8px;
  padding: 18px;
}

.panel-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-title h2 {
  font-size: 1rem;
}

.panel-title span {
  color: #d7b46a;
  font-size: 0.82rem;
}

.combo-items {
  margin-top: 6px;
  color: #cfc3ae;
  font-size: 0.85rem;
  line-height: 1.3;
}

.invoice-list {
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 220px);
  overflow: auto;
}

.search-input {
  width: 100%;
  margin-bottom: 14px;
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.05);
  color: #f7f2e9;
}

.search-input::placeholder {
  color: rgba(247, 242, 233, 0.6);
}

.invoice-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  width: 100%;
  min-height: 76px;
  border-radius: 6px;
  margin-bottom: 10px;
  padding: 14px;
  text-align: left;
  background: rgba(255, 255, 255, 0.05);
}

.invoice-row.active {
  border-color: #d7b46a;
  background: rgba(215, 180, 106, 0.14);
}

.invoice-row strong,
.invoice-row b,
.invoice-row small {
  display: block;
}

.invoice-row small {
  margin-top: 6px;
  color: #b7ad9c;
}

.row-right {
  text-align: right;
}

.detail-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 20px;
}

.detail-head .eyebrow {
  text-align: left;
}

.detail-head h2 {
  font-size: clamp(1.8rem, 4vw, 3rem);
}

.detail-head span {
  color: #cfc3ae;
}

.total-box {
  min-width: 190px;
  border-radius: 8px;
  padding: 18px;
  background: #d7b46a;
  color: #15110b;
  text-align: right;
}

.total-box span {
  color: rgba(21, 17, 11, 0.68);
}

.total-box strong {
  font-size: 1.45rem;
}

.info-grid,
.amount-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.info-grid div,
.amount-grid div {
  border-radius: 6px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.055);
}

.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  min-width: 760px;
  border-collapse: collapse;
}

th,
td {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding: 13px 10px;
  text-align: left;
  white-space: nowrap;
}

th {
  color: #d7b46a;
  font-size: 0.78rem;
  text-transform: uppercase;
}

td {
  color: #efe8dc;
}

.error-message,
.empty-state {
  max-width: 1440px;
  margin: 14px auto;
  color: #ffd8d8;
}

.empty-state {
  color: #cfc3ae;
}

@media (max-width: 900px) {
  .invoice-header {
    grid-template-columns: 1fr;
  }

  .invoice-header h1,
  .eyebrow {
    text-align: left;
  }

  .summary-grid,
  .invoice-workspace,
  .info-grid,
  .amount-grid {
    grid-template-columns: 1fr;
  }

  .invoice-list {
    max-height: none;
  }

  .detail-head {
    flex-direction: column;
  }

  .total-box {
    width: 100%;
    box-sizing: border-box;
    text-align: left;
  }
}
</style>
