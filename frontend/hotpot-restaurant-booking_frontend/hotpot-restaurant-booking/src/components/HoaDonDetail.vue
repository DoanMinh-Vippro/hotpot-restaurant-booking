<script setup lang="ts">
import type { HoaDon, HoaDonChiTiet } from '../api/HoaDonApi'

const props = defineProps<{
  selectedHoaDon: HoaDon | undefined
  chiTiets: HoaDonChiTiet[]
  loading: boolean
}>()

const getCurrentOperatorName = () =>
  localStorage.getItem('tenDangNhap')?.trim() || 'Admin'

const normalizeInvoiceEmployee = (invoice?: HoaDon) =>
  invoice?.tenNhanVien?.trim() || getCurrentOperatorName()

const normalizeOrderItem = (item: HoaDonChiTiet) => ({
  ...item,
  orderedBy: item.orderedBy?.trim() || getCurrentOperatorName(),
  orderedAt: item.orderedAt || new Date().toISOString(),
})

const detailItems = () => (props.chiTiets || []).map(normalizeOrderItem)

const formatCurrency = (value: number | string | null) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(Number(value ?? 0))

const formatInvoiceTableLabel = (invoice?: HoaDon) => {
  const names: string[] = []

  if (Array.isArray(invoice?.dsBan)) {
    invoice.dsBan.forEach((ban: any) => {
      const name = String(ban?.tenBan || ban?.name || ban?.ten || '').trim()
      if (name && !names.includes(name)) names.push(name)
    })
  }

  if (names.length === 0) {
    const raw = String(invoice?.tenBan || '').trim()
    if (raw) {
      const splitNames = raw.split(/[;,]/).map((item: string) => item.trim()).filter(Boolean)
      splitNames.forEach((name: string) => {
        if (name && !names.includes(name)) names.push(name)
      })
    }
  }

  if (names.length > 0) return `${names.join(', ')} (${names.length} bàn)`
  return invoice?.loaiBan || `Bàn ${invoice?.idBan ?? '-'}`
}

const formatDateTime = (value: string | number[] | null | undefined) => {
  if (!value) return 'Chưa xuất'

  let date: Date
  if (Array.isArray(value)) {
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
</script>

<template>
  <section class="chi-tiet-hoa-don" aria-label="Chi tiết hóa đơn">
    <template v-if="selectedHoaDon">
      <div class="dau-chi-tiet">
        <div>
          <p class="tieu-le">{{ invoiceStatusLabel(selectedHoaDon.trangThaiHoaDon) }}</p>
          <h2>{{ selectedHoaDon.maHoaDon }}</h2>
          <span>{{ formatDateTime(selectedHoaDon.thoiGianXuat) }}</span>
        </div>
        <div class="hop-tong-tien">
          <span>Tổng tiền</span>
          <strong>{{ formatCurrency(selectedHoaDon.tongTien) }}</strong>
        </div>
      </div>

      <div class="luoi-thong-tin">
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
          <strong>{{ formatInvoiceTableLabel(selectedHoaDon) }}</strong>
        </div>
        <div>
          <span>Giờ vào bàn</span>
          <strong>{{ formatDateTime(selectedHoaDon.gioVaoBan || selectedHoaDon.thoiGianXuat) }}</strong>
        </div>
        <div>
          <span>Giờ rời bàn</span>
          <strong>{{ formatDateTime(selectedHoaDon.gioRoiBan) }}</strong>
        </div>
        <div>
          <span>Nhân viên</span>
          <strong>{{ normalizeInvoiceEmployee(selectedHoaDon) }}</strong>
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

      <div class="luoi-so-tien">
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

      <div class="bao-bang">
        <div class="tieu-de-panel">
          <h2>Chi tiết món</h2>
          <span v-if="loading">Đang tải...</span>
        </div>

        <table>
          <colgroup>
            <col style="width: 120px" />
            <col style="width: 280px" />
            <col style="width: 80px" />
            <col style="width: 110px" />
            <col style="width: 90px" />
            <col style="width: 120px" />
            <col style="width: 150px" />
            <col style="width: 130px" />
          </colgroup>
          <thead>
            <tr>
              <th>Mã</th>
              <th>Món / combo</th>
              <th>Số lượng</th>
              <th>Đơn giá</th>
              <th>Giảm</th>
              <th>Thành tiền</th>
              <th>Nhân viên order</th>
              <th>Giờ order</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in detailItems()" :key="item.idHoaDonChiTiet">
              <td class="ma-cell">{{ item.maHoaDonChiTiet }}</td>
              <td class="mon-cell">
                <div class="ten-mon">{{ itemName(item) }}</div>
                <template v-if="item.comboItems?.length">
                  <div class="mon-combo">Gồm: {{ item.comboItems.join(', ') }}</div>
                </template>
              </td>
              <td class="so-luong-cell">{{ item.soLuong ?? 0 }}</td>
              <td class="gia-cell">{{ formatCurrency(item.giaBanTaiThoiDiem) }}</td>
              <td class="giam-cell">{{ formatCurrency(item.tienGiamGiaMon) }}</td>
              <td class="thanh-tien-cell">{{ formatCurrency(item.thanhTien) }}</td>
              <td class="nhan-vien-cell">{{ item.orderedBy }}</td>
              <td class="gio-order-cell">{{ formatDateTime(item.orderedAt) }}</td>
            </tr>
          </tbody>
        </table>

        <p v-if="!loading && chiTiets.length === 0" class="trang-trong">
          Hóa đơn này chưa có chi tiết món.
        </p>
      </div>
    </template>

    <p v-else class="trang-trong">Chọn một hóa đơn để xem chi tiết.</p>
  </section>
</template>

<style scoped>
.chi-tiet-hoa-don {
  border: 1px solid #e6d2aa;
  background: rgba(255, 248, 234, 0.96);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 14px;
  color: #5f3d22;
  box-shadow: 0 10px 24px rgba(103, 72, 32, 0.06);
  width: 100%;
  box-sizing: border-box;
}

.tieu-de-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.tieu-de-panel h2 {
  margin: 0;
  font-size: 1rem;
  letter-spacing: 0;
  color: #8b5e34;
}

.tieu-de-panel span {
  color: #8f6b46;
  font-size: 0.82rem;
}

.dau-chi-tiet {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.dau-chi-tiet .tieu-le {
  margin: 0 0 8px;
  color: #8b5e34;
  font-size: 0.75rem;
  font-weight: 700;
  text-align: left;
  text-transform: uppercase;
}

.dau-chi-tiet h2 {
  margin: 0;
  font-size: clamp(1.8rem, 4vw, 3rem);
  letter-spacing: 0;
  color: #5f3d22;
}

.dau-chi-tiet span {
  color: #8f6b46;
}

.hop-tong-tien {
  min-width: 170px;
  border-radius: 12px;
  padding: 14px 16px;
  background: #d8a85c;
  color: #3d2814;
  text-align: right;
}

.hop-tong-tien span {
  display: block;
  margin-bottom: 6px;
  color: rgba(61, 40, 20, 0.72);
  font-size: 0.78rem;
}

.hop-tong-tien strong {
  font-size: 1.45rem;
}

.luoi-thong-tin,
.luoi-so-tien {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.luoi-thong-tin div,
.luoi-so-tien div {
  border-radius: 10px;
  padding: 12px 13px;
  background: #fff8ea;
  border: 1px solid #e6d2aa;
}

.luoi-thong-tin span,
.luoi-so-tien span {
  display: block;
  margin-bottom: 6px;
  color: #8f6b46;
  font-size: 0.78rem;
}

.mon-combo {
  margin-top: 6px;
  color: #8f6b46;
  font-size: 0.85rem;
  line-height: 1.3;
}

.bao-bang {
  overflow-x: auto;
  overflow-y: hidden;
  width: 100%;
}

table {
  width: 100%;
  min-width: 820px;
  border-collapse: separate;
  border-spacing: 0;
  table-layout: fixed;
}

th,
td {
  border-bottom: 1px solid #efe0c1;
  padding: 11px 8px;
  text-align: left;
  vertical-align: top;
}

th {
  color: #8b5e34;
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.02em;
  background: #fff9ee;
}

td {
  color: #5f3d22;
  font-size: 0.85rem;
  line-height: 1.35;
  overflow-wrap: anywhere;
  word-break: break-word;
  white-space: normal;
}

.ma-cell {
  width: 120px;
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.so-luong-cell,
.gia-cell,
.giam-cell,
.thanh-tien-cell {
  white-space: nowrap;
}

.mon-cell,
.nhan-vien-cell,
.gio-order-cell {
  white-space: normal;
}

.ten-mon {
  min-width: 0;
}

.mon-combo {
  margin-top: 5px;
  color: #8f6b46;
  font-size: 0.82rem;
  line-height: 1.3;
}

tbody tr:hover {
  background: rgba(216, 168, 92, 0.05);
}

.trang-trong {
  color: #8f6b46;
  margin: 14px 0;
}

@media (max-width: 900px) {
  .dau-chi-tiet {
    flex-direction: column;
  }

  .hop-tong-tien {
    width: 100%;
    box-sizing: border-box;
    text-align: left;
  }

  .luoi-thong-tin,
  .luoi-so-tien {
    grid-template-columns: 1fr;
  }
}
</style>
