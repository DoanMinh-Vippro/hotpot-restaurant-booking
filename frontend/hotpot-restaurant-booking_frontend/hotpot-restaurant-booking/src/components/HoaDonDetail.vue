<script setup lang="ts">
import type { HoaDon, HoaDonChiTiet } from '../api/HoaDonApi'

defineProps<{
  selectedHoaDon: HoaDon | undefined
  chiTiets: HoaDonChiTiet[]
  loading: boolean
}>()

const formatCurrency = (value: number | string | null) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(Number(value ?? 0))

const formatDateTime = (value: string | number[] | null) => {
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
                  <div class="mon-combo">Gồm: {{ item.comboItems.join(', ') }}</div>
                </template>
              </td>
              <td>{{ item.soLuong ?? 0 }}</td>
              <td>{{ formatCurrency(item.giaBanTaiThoiDiem) }}</td>
              <td>{{ formatCurrency(item.tienGiamGiaMon) }}</td>
              <td>{{ formatCurrency(item.thanhTien) }}</td>
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
  border: 1px solid rgba(215, 180, 106, 0.2);
  background: rgba(14, 14, 14, 0.88);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  padding: 18px;
  color: #f7f2e9;
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
}

.tieu-de-panel span {
  color: #d7b46a;
  font-size: 0.82rem;
}

.dau-chi-tiet {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 20px;
}

.dau-chi-tiet .tieu-le {
  margin: 0 0 8px;
  color: #d7b46a;
  font-size: 0.75rem;
  font-weight: 700;
  text-align: left;
  text-transform: uppercase;
}

.dau-chi-tiet h2 {
  margin: 0;
  font-size: clamp(1.8rem, 4vw, 3rem);
  letter-spacing: 0;
}

.dau-chi-tiet span {
  color: #cfc3ae;
}

.hop-tong-tien {
  min-width: 190px;
  border-radius: 8px;
  padding: 18px;
  background: #d7b46a;
  color: #15110b;
  text-align: right;
}

.hop-tong-tien span {
  display: block;
  margin-bottom: 6px;
  color: rgba(21, 17, 11, 0.68);
  font-size: 0.78rem;
}

.hop-tong-tien strong {
  font-size: 1.45rem;
}

.luoi-thong-tin,
.luoi-so-tien {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.luoi-thong-tin div,
.luoi-so-tien div {
  border-radius: 6px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.055);
}

.luoi-thong-tin span,
.luoi-so-tien span {
  display: block;
  margin-bottom: 6px;
  color: #b7ad9c;
  font-size: 0.78rem;
}

.mon-combo {
  margin-top: 6px;
  color: #cfc3ae;
  font-size: 0.85rem;
  line-height: 1.3;
}

.bao-bang {
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

.trang-trong {
  color: #cfc3ae;
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
