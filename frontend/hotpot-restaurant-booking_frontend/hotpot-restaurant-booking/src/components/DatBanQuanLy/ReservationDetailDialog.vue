<script setup lang="ts">
const props = defineProps<{
  visible: boolean
  reservation: any | null
}>()

const emit = defineEmits(['close', 'doiBan', 'doiGio', 'xacNhan', 'checkIn'])

const close = () => {
  emit('close')
}

const doiBan = () => {
  emit('doiBan', props.reservation)
}

const doiGio = () => {
  emit('doiGio', props.reservation)
}

const xacNhan = () => {
  emit('xacNhan', props.reservation)
}

const checkIn = () => {
  emit('checkIn', props.reservation)
}

const formatMoney = (value: number) => {
  if (!value) return '0 ₫'

  return value.toLocaleString('vi-VN') + ' ₫'
}

const isChoXacNhan = () => props.reservation?.trangThai === 'CHO_XAC_NHAN'
const isDaXacNhan = () => props.reservation?.trangThai === 'DA_XAC_NHAN'
const canEdit = () =>
  props.reservation?.trangThai === 'CHO_XAC_NHAN' || props.reservation?.trangThai === 'DA_XAC_NHAN'

const getTrangThaiText = (tt: string) => {
  switch (tt) {
    case 'CHO_XAC_NHAN':
      return 'Chờ xác nhận'
    case 'DA_XAC_NHAN':
      return 'Đã xác nhận'
    case 'DA_NHAN_BAN':
      return 'Đã nhận bàn'
    case 'HOAN_THANH':
      return 'Hoàn thành'
    case 'DA_HUY':
      return 'Đã hủy'
    default:
      return tt
  }
}

const getTrangThaiCocText = (tt: string) => {
  switch (tt) {
    case 'CHUA_COC':
      return 'Chưa cọc'
    case 'DA_COC':
      return 'Đã cọc'
    default:
      return tt
  }
}

const getPhuongThucText = (pt: string) => {
  switch (pt) {
    case 'TIEN_MAT':
      return 'Tiền mặt'
    case 'CHUYEN_KHOAN':
      return 'Chuyển khoản'
    case 'VNPAY':
      return 'VNPay'
    case 'CHUA_THANH_TOAN':
      return 'Chưa thanh toán'
    default:
      return pt
  }
}

const formatDateTime = (value?: string) => {
  if (!value) return '-'

  const date = new Date(value)

  return date.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="overlay" @click.self="close">
      <div class="dialog">
        <!-- Header -->
        <div class="dialog-header">
          <div>
            <h2>Chi tiết đặt bàn</h2>
            <p>Mã đơn #{{ reservation?.idDatBan }}</p>
          </div>

          <button class="close-btn" @click="close">✕</button>
        </div>

        <!-- Nội dung -->
        <div class="content">
          <!-- Khách hàng -->
          <div class="card">
            <h3>Thông tin khách hàng</h3>

            <div class="grid">
              <div class="item">
                <label>Khách hàng</label>
                <span>{{ reservation?.tenKhachHang || 'Khách lẻ' }}</span>
              </div>

              <div class="item">
                <label>SĐT</label>
                <span>{{ reservation?.sdtKhachHang || '-' }}</span>
              </div>

              <div class="item">
                <label>Số người</label>
                <span>{{ reservation?.soNguoi }}</span>
              </div>

              <div class="item">
                <label>Ghi chú</label>
                <span>{{ reservation?.ghiChu || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- Đặt bàn -->
          <div class="card">
            <h3>Thông tin đặt bàn</h3>

            <div class="grid">
              <div class="item">
                <label>Ngày đặt</label>
                <span>{{ reservation?.ngayDat }}</span>
              </div>

              <div class="item">
                <label>Giờ đặt</label>
                <span>{{ reservation?.gioDat }}</span>
              </div>

              <div class="item">
                <label>Thời gian đến</label>
                <span>{{ formatDateTime(reservation?.thoiGianDenDuKien) }}</span>
              </div>

              <div class="item">
                <label>Tiền cọc</label>
                <span>{{ formatMoney(reservation?.soTienCoc) }}</span>
              </div>

              <div class="item">
                <label>Trạng thái</label>
                <span>{{ getTrangThaiText(reservation?.trangThai) }}</span>
              </div>

              <div class="item">
                <label>Trạng thái cọc</label>
                <span>{{ getTrangThaiCocText(reservation?.trangThaiCoc) }}</span>
              </div>

              <div class="item">
                <label>Phương thức thanh toán</label>
                <span>{{ getPhuongThucText(reservation?.phuongThucThanhToan) }}</span>
              </div>
            </div>
          </div>

          <!-- Danh sách bàn -->
          <div class="card">
            <h3>Bàn đã chọn</h3>

            <div class="table-list">
              <span v-for="ban in reservation?.dsBan" :key="ban.idBan" class="table-chip">
                {{ ban.tenBan }}
              </span>
            </div>
          </div>

          <!-- Danh sách combo -->
          <div class="card">
            <h3>Combo đặt trước</h3>

            <div v-if="reservation?.dsCombo?.length" class="combo-list">
              <div v-for="combo in reservation.dsCombo" :key="combo.idCombo" class="combo-item">
                <strong>{{ combo.tenCombo }}</strong>

                <span>x{{ combo.soLuong }}</span>
              </div>
            </div>

            <div v-else class="empty">Không có combo đặt trước</div>
          </div>
        </div>

        <!-- Footer -->
        <div class="dialog-footer">
          <button v-if="isChoXacNhan()" class="btn green" @click="xacNhan">Xác nhận</button>

          <button v-if="isDaXacNhan()" class="btn blue" @click="checkIn">Check-in</button>

          <button v-if="canEdit()" class="btn gold" @click="doiBan">Đổi bàn</button>

          <button v-if="canEdit()" class="btn brown" @click="doiGio">Đổi giờ</button>

          <button class="btn gray" @click="close">Đóng</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);

  display: flex;
  justify-content: center;
  align-items: center;

  z-index: 999;
}

.dialog {
  width: 900px;
  max-width: 95vw;
  max-height: 90vh;

  overflow-y: auto;

  background: #f8f5ef;
  border-radius: 18px;

  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.18);

  border: 1px solid #e5d7b8;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  padding: 24px 30px;

  border-bottom: 1px solid #e7dcc5;
}

.dialog-header h2 {
  margin: 0;
  font-size: 24px;
  color: #2f2a24;
}

.dialog-header p {
  margin-top: 6px;
  color: #8b7b63;
  font-size: 14px;
}

.close-btn {
  width: 40px;
  height: 40px;

  border: none;

  background: #ece4d3;

  border-radius: 10px;

  cursor: pointer;

  transition: 0.25s;
}

.close-btn:hover {
  background: #d9ccb2;
}

.content {
  padding: 24px;

  display: flex;

  flex-direction: column;

  gap: 22px;
}

.card {
  background: white;

  border-radius: 14px;

  border: 1px solid #e7dcc5;

  padding: 22px;
}

.card h3 {
  margin: 0 0 18px;

  color: #715f44;

  font-size: 18px;
}

.grid {
  display: grid;

  grid-template-columns: repeat(2, 1fr);

  gap: 18px;
}

.item {
  display: flex;

  flex-direction: column;

  gap: 6px;
}

.item label {
  color: #8f846d;

  font-size: 13px;

  text-transform: uppercase;

  letter-spacing: 0.8px;
}

.item span {
  font-size: 16px;

  color: #2d2d2d;

  font-weight: 600;
}

.table-list {
  display: flex;

  flex-wrap: wrap;

  gap: 12px;
}

.table-chip {
  background: #efe5d2;

  color: #6b5536;

  padding: 10px 18px;

  border-radius: 999px;

  font-weight: 600;
}

.combo-list {
  display: flex;

  flex-direction: column;

  gap: 12px;
}

.combo-item {
  display: flex;

  justify-content: space-between;

  padding: 14px 18px;

  background: #faf8f4;

  border-radius: 10px;

  border: 1px solid #ece2ce;
}

.empty {
  text-align: center;

  color: #999;

  padding: 18px;
}

.dialog-footer {
  display: flex;

  justify-content: flex-end;

  gap: 12px;

  padding: 24px 30px;

  border-top: 1px solid #e7dcc5;
}

.btn {
  border: none;

  padding: 12px 22px;

  border-radius: 10px;

  cursor: pointer;

  font-weight: 600;

  transition: 0.25s;
}

.green {
  background: #4caf50;
  color: white;
}

.green:hover {
  background: #419846;
}

.blue {
  background: #1976d2;
  color: white;
}

.blue:hover {
  background: #1565c0;
}

.gold {
  background: #c9a96a;
  color: white;
}

.gold:hover {
  background: #b8924d;
}

.brown {
  background: #8d6e63;
  color: white;
}

.brown:hover {
  background: #795548;
}

.gray {
  background: #d8d8d8;
}

.gray:hover {
  background: #c7c7c7;
}

@media (max-width: 900px) {
  .grid {
    grid-template-columns: 1fr;
  }

  .dialog {
    width: 96%;
  }

  .dialog-footer {
    flex-wrap: wrap;
  }

  .btn {
    flex: 1;
  }
}
</style>
