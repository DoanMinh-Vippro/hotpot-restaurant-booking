<script setup lang="ts">
import { ref, watch } from 'vue'

import DatBanQuanLyApi from '@/api/DatBanQuanLy'
import { paymentApi } from '@/api/PaymentApi'
import ComBoInDatBan from '@/components/ComBoInDatBan.vue'
import PaymentCashDialog from '@/components/PaymentCashDialog.vue'

import { searchKhachHang } from '@/api/khachhang'

const props = defineProps<{
  visible: boolean
  dsBanTrong: any[]
}>()

const emit = defineEmits(['close', 'refresh', 'check-ban'])
type PaymentMethod = 'TIEN_MAT' | 'VNPAY' | 'CHUA_THANH_TOAN'

const showPayment = ref(false)

const showCashDialog = ref(false)

const paymentData = ref({
  qrUrl: '',
  amount: 0,
  content: '',
})

const paymentTimer: ReturnType<typeof setInterval> | null = null

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('vi-VN').format(value)
}
// ===========================
// DANH SÁCH KHÁCH TÌM ĐƯỢC
// ===========================

const dsKhachHang = ref<any[]>([])

const showDanhSachKhach = ref(false)

const khachHang = ref<any>(null)

// ===========================
// COMBO
// ===========================

const selectedCombo = ref<any[]>([])

// ===========================
// FORM
// ===========================

const form = ref({
  idKhachHang: null as number | null,
  tenKhachHang: '',
  sdtKhachHang: '',

  soNguoi: 2,

  thoiGianDenDuKien: '',

  dsBan: [] as number[],

  dsCombo: [] as any[],

  soTienCoc: 0,

  trangThaiCoc: 'CHUA_COC',

  phuongThucThanhToan: 'CHUA_THANH_TOAN' as PaymentMethod,
  ghiChu: '',

  trangThai: 'CHO_XAC_NHAN',
})
// ===========================
// CHỌN COMBO
// ===========================

const handleComboSelection = (comboList: any[]) => {
  selectedCombo.value = comboList ?? []

  form.value.dsCombo = comboList ?? []

  tinhTienCoc()
}

// ===========================
// CHỌN BÀN
// ===========================
const banToiUu = ref<number[]>([])

const isSelectedBan = (id: number) => {
  return form.value.dsBan.includes(id)
}

const tongSucChuaDaChon = () => {
  return props.dsBanTrong
    .filter((b) => form.value.dsBan.includes(b.idBan))
    .reduce((sum, b) => sum + b.sucChua, 0)
}

const timToHopToiUu = () => {
  const ds = [...props.dsBanTrong].sort((a, b) => b.sucChua - a.sucChua)

  let ketQua: any[] | null = null
  let tongTotNhat = Number.MAX_SAFE_INTEGER

  const deQuy = (index: number, toHop: any[], tong: number) => {
    // Đã đủ chỗ
    if (tong >= form.value.soNguoi) {
      if (
        ketQua == null ||
        // Ít bàn hơn
        toHop.length < ketQua.length ||
        // Cùng số bàn nhưng dư ít hơn
        (toHop.length === ketQua.length && tong < tongTotNhat)
      ) {
        ketQua = [...toHop]
        tongTotNhat = tong
      }

      return
    }

    if (index >= ds.length) return

    // =====================
    // CẮT NHÁNH
    // =====================

    if (ketQua && toHop.length >= ketQua.length) {
      return
    }

    // Chọn bàn hiện tại
    deQuy(index + 1, [...toHop, ds[index]], tong + ds[index].sucChua)

    // Bỏ qua bàn hiện tại
    deQuy(index + 1, toHop, tong)
  }

  deQuy(0, [], 0)

  banToiUu.value = ketQua?.map((b) => b.idBan) ?? []

  console.log('TỔ HỢP TỐI ƯU', banToiUu.value)
}

watch(
  [() => props.dsBanTrong, () => form.value.soNguoi],
  () => {
    timToHopToiUu()
  },
  {
    deep: true,
    immediate: true,
  },
)

const canSelectBan = (ban: any) => {
  if (isSelectedBan(ban.idBan)) {
    return true
  }

  if (tongSucChuaDaChon() >= form.value.soNguoi) {
    return false
  }

  return banToiUu.value.includes(ban.idBan)
}

const toggleBan = (ban: any) => {
  if (!canSelectBan(ban)) return

  const index = form.value.dsBan.indexOf(ban.idBan)

  if (index >= 0) {
    form.value.dsBan.splice(index, 1)
  } else {
    form.value.dsBan.push(ban.idBan)
  }
}
// ===========================
// TÍNH TIỀN CỌC
// ===========================
const tinhTienCoc = () => {
  if (form.value.dsCombo.length === 0) {
    form.value.soTienCoc = 0

    form.value.trangThaiCoc = 'CHUA_COC'

    form.value.phuongThucThanhToan = 'CHUA_THANH_TOAN'

    return
  }

  const tongTienCombo = form.value.dsCombo.reduce(
    (tong, combo) => tong + Number(combo.giaCombo) * Number(combo.soLuong ?? 1),
    0,
  )

  form.value.soTienCoc = Math.round(tongTienCombo * 0.3)

  form.value.trangThaiCoc = 'CHUA_COC'
}

// ===========================
// KIỂM TRA BÀN
// ===========================

const checkBan = () => {
  if (!form.value.thoiGianDenDuKien) {
    alert('Vui lòng chọn thời gian đến')
    return
  }

  emit('check-ban', {
    thoiGianDenDuKien: form.value.thoiGianDenDuKien,
    soNguoi: form.value.soNguoi,
  })
}
const timKhachHang = async (keyword: string) => {
  if (!keyword.trim()) {
    dsKhachHang.value = []
    showDanhSachKhach.value = false
    return
  }

  try {
    const res = await searchKhachHang(keyword)

    dsKhachHang.value = res.data ?? []

    showDanhSachKhach.value = dsKhachHang.value.length > 0
  } catch (e) {
    dsKhachHang.value = []
    showDanhSachKhach.value = false
  }
}
const chonKhachHang = (kh: any) => {
  form.value.idKhachHang = kh.idKhachHang
  form.value.tenKhachHang = kh.tenKhachHang
  form.value.sdtKhachHang = kh.soDienThoai
  dsKhachHang.value = []
  showDanhSachKhach.value = false
}

watch(
  () => form.value.sdtKhachHang,
  (value) => {
    if (khachHang.value && value !== khachHang.value.soDienThoai) {
      form.value.idKhachHang = null
      khachHang.value = null
    }

    if (value.length >= 3) {
      timKhachHang(value)
    } else {
      dsKhachHang.value = []
      showDanhSachKhach.value = false
    }
  },
)

const taoPayload = () => ({
  idKhachHang: form.value.idKhachHang,
  tenKhachHang: form.value.tenKhachHang,

  sdtKhachHang: form.value.sdtKhachHang,

  soNguoi: form.value.soNguoi,

  thoiGianDenDuKien: form.value.thoiGianDenDuKien,

  dsBan: form.value.dsBan,

  dsCombo: form.value.dsCombo,

  soTienCoc: form.value.soTienCoc,

  trangThaiCoc: form.value.trangThaiCoc,

  phuongThucThanhToan: form.value.phuongThucThanhToan,

  ghiChu: form.value.ghiChu,

  trangThai: 'CHO_XAC_NHAN',
})

const taoDon = async () => {
  await DatBanQuanLyApi.add(taoPayload())

  close()

  emit('refresh')
}

const datBan = async () => {
  if (!form.value.sdtKhachHang) {
    alert('Chưa nhập số điện thoại')
    return
  }

  if (form.value.dsBan.length === 0) {
    alert('Chưa có bàn')
    return
  }
  if (form.value.soTienCoc === 0) {
    await taoDon()
    return
  }
  if (form.value.phuongThucThanhToan === 'TIEN_MAT') {
    showCashDialog.value = true
    return
  }
  if (form.value.phuongThucThanhToan === 'VNPAY') {
    const res = await paymentApi.createVNPayPayment(taoPayload())

    window.location.href = res.data.paymentUrl

    return
  }
}

const confirmCashPayment = async () => {
  form.value.trangThaiCoc = 'DA_COC'

  await taoDon()

  showCashDialog.value = false
}

const closeCashDialog = () => {
  showCashDialog.value = false
}

const resetForm = () => {
  dsKhachHang.value = []

  showDanhSachKhach.value = false

  khachHang.value = null

  selectedCombo.value = []

  form.value = {
    idKhachHang: null,
    tenKhachHang: '',
    sdtKhachHang: '',

    soNguoi: 2,

    thoiGianDenDuKien: '',

    dsBan: [],

    dsCombo: [],

    soTienCoc: 0,

    trangThaiCoc: 'CHUA_COC',

    phuongThucThanhToan: 'CHUA_THANH_TOAN',

    ghiChu: '',

    trangThai: 'CHO_XAC_NHAN',
  }
}

const close = () => {
  resetForm()
  showCashDialog.value = false
  showPayment.value = false
  emit('close')
}

watch(
  () => props.visible,
  (visible) => {
    if (!visible) {
      resetForm()
    }
  },
)

watch(
  // dùng cho emit check bàn
  () => [form.value.thoiGianDenDuKien, form.value.soNguoi],
  () => {
    if (form.value.thoiGianDenDuKien) {
      checkBan()
    }
  },
)
</script>

<template>
  <div v-if="visible" class="dialog-overlay">
    <div class="dialog">
      <div class="dialog-body">
        <div class="left-panel">
          <!-- CARD KHÁCH HÀNG -->
          <div class="card">
            <h3>Khách hàng</h3>

            <div class="field">
              <label>Số điện thoại</label>
              <div class="search-box">
                <input v-model="form.sdtKhachHang" placeholder="Nhập số điện thoại..." />

                <div v-if="showDanhSachKhach" class="search-result">
                  <div
                    v-for="kh in dsKhachHang"
                    :key="kh.idKhachHang"
                    class="customer-item"
                    @click="chonKhachHang(kh)"
                  >
                    <strong>{{ kh.tenKhachHang }}</strong>
                    <span>{{ kh.soDienThoai }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="row">
              <div class="field">
                <label>Tên khách</label>

                <input v-model="form.tenKhachHang" placeholder="Nhập tên khách" />
              </div>

              <div class="field">
                <label>Số điện thoại</label>

                <input v-model="form.sdtKhachHang" placeholder="Nhập số điện thoại" />

                <div v-if="showDanhSachKhach" class="search-result">
                  <div
                    v-for="kh in dsKhachHang"
                    :key="kh.idKhachHang"
                    class="customer-item"
                    @click="chonKhachHang(kh)"
                  >
                    <strong>{{ kh.tenKhachHang }}</strong>

                    <span>{{ kh.soDienThoai }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- ================= THÔNG TIN ĐẶT BÀN ================= -->

          <div class="card">
            <div class="card-header">
              <h3>Thông tin đặt bàn</h3>
            </div>

            <div class="row">
              <div class="field">
                <label>Số người</label>

                <input type="number" min="1" v-model.number="form.soNguoi" />
              </div>

              <div class="field flex2">
                <label>Thời gian đến</label>

                <input type="datetime-local" v-model="form.thoiGianDenDuKien" />
              </div>
            </div>
          </div>

          <!-- ================= BÀN PHÙ HỢP ================= -->

          <div class="card">
            <div class="card-header">
              <h3>Bàn phù hợp</h3>

              <span>{{ dsBanTrong.length }} bàn</span>
            </div>

            <div v-if="!form.thoiGianDenDuKien" class="empty">
              Chọn thời gian đến, hệ thống sẽ tự tìm bàn trống.
            </div>

            <div v-else-if="dsBanTrong.length === 0" class="empty">Không có bàn trống phù hợp.</div>

            <div v-else class="table-grid">
              <div
                v-for="ban in dsBanTrong"
                :key="ban.idBan"
                class="table-card"
                :class="{
                  active: isSelectedBan(ban.idBan),
                  disabled: !canSelectBan(ban),
                }"
                @click="toggleBan(ban)"
              >
                <div class="table-name">
                  {{ ban.tenBan }}
                </div>

                <div class="table-info">
                  <span>{{ ban.loaiBan }}</span>

                  <span>{{ ban.sucChua }} người</span>
                </div>

                <div class="table-area">
                  {{ ban.tenKhuVuc }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="right-panel">
          <!-- ================= COMBO ================= -->

          <div class="card">
            <div class="card-header">
              <h3>Combo</h3>

              <span>{{ selectedCombo.length }} đã chọn</span>
            </div>

            <ComBoInDatBan v-model="selectedCombo" @selectedCombo="handleComboSelection" />
          </div>

          <!-- ================= THANH TOÁN CỌC ================= -->

          <div class="card">
            <div class="card-header">
              <h3>Thanh toán cọc</h3>
            </div>

            <div class="field">
              <label>Tiền cọc</label>

              <input :value="formatCurrency(form.soTienCoc)" readonly />
            </div>

            <div class="field">
              <label>Phương thức thanh toán</label>

              <select v-model="form.phuongThucThanhToan" :disabled="form.soTienCoc === 0">
                <option value="CHUA_THANH_TOAN">Chưa thanh toán</option>

                <option value="TIEN_MAT">Tiền mặt</option>

                <option value="CHUYEN_KHOAN">Chuyển khoản</option>

                <option value="VNPAY">VNPay</option>
              </select>
            </div>

            <div v-if="form.soTienCoc === 0" class="info-text">
              Không chọn combo thì không cần đặt cọc.
            </div>
          </div>

          <!-- ================= GHI CHÚ ================= -->

          <div class="card">
            <div class="card-header">
              <h3>Ghi chú</h3>
            </div>

            <div class="field">
              <textarea
                v-model="form.ghiChu"
                rows="6"
                placeholder="Nhập ghi chú cho đơn đặt bàn..."
              />
            </div>
          </div>
        </div>
      </div>

      <div class="dialog-footer">
        <button class="btn-cancel" @click="close">Hủy</button>

        <button class="btn-save" @click="datBan">Tạo đơn đặt bàn</button>
      </div>
    </div>
  </div>
  <PaymentCashDialog
    :show="showCashDialog"
    :amount="form.soTienCoc"
    @confirm="confirmCashPayment"
    @close="closeCashDialog"
  />
</template>

<style scoped>
/* ================= OVERLAY ================= */

.dialog-overlay {
  position: fixed;
  inset: 0;

  background: rgba(55, 42, 25, 0.45);

  display: flex;
  justify-content: center;
  align-items: flex-start;

  overflow-y: auto;

  padding: 32px 20px;

  z-index: 9999;
}

/* ================= MAIN DIALOG ================= */

.dialog {
  width: 1180px;
  max-width: 95vw;

  background: #faf7f1;

  border-radius: 20px;

  display: flex;
  flex-direction: column;

  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.18);

  border: 1px solid rgba(185, 151, 91, 0.25);

  overflow: hidden;
}

/* ================= HEADER ================= */

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  padding: 18px 26px;

  background: #fff;

  border-bottom: 1px solid #eee2d2;
}

.dialog-header h2 {
  margin: 0;

  color: #4a3824;

  font-size: 22px;

  font-weight: 700;
}

.btn-close {
  width: 36px;
  height: 36px;

  border-radius: 50%;

  border: none;

  background: #f5eee2;

  color: #76552d;

  font-size: 24px;

  cursor: pointer;

  transition: 0.2s;
}

.btn-close:hover {
  background: #c28d2c;

  color: white;
}

/* ================= BODY ================= */

.dialog-body {
  display: grid;

  grid-template-columns: 1.8fr 1fr;

  gap: 18px;

  padding: 22px;
}

/* ================= PANEL ================= */

.left-panel,
.right-panel {
  display: flex;

  flex-direction: column;

  gap: 16px;
}

/* ================= CARD ================= */

.card {
  background: white;

  border-radius: 16px;

  padding: 16px 18px;

  border: 1px solid #eee3d3;

  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.04);
}

/* ================= CARD HEADER ================= */

.card-header {
  display: flex;

  justify-content: space-between;

  align-items: center;

  margin-bottom: 14px;
}

.card-header h3 {
  margin: 0;

  color: #4a3824;

  font-size: 16px;

  font-weight: 700;
}

.card-header span {
  color: #a27f45;

  background: #faf1df;

  padding: 4px 10px;

  border-radius: 20px;

  font-size: 12px;
}

/* ================= FORM ================= */

.row {
  display: flex;

  gap: 14px;
}

.field {
  position: relative;

  display: flex;

  flex-direction: column;

  flex: 1;

  gap: 6px;
}

.flex2 {
  flex: 2;
}

.field label {
  color: #6d5739;

  font-size: 13px;

  font-weight: 600;
}

.field input,
.field select,
.field textarea {
  width: 100%;

  background: #fffdf9;

  border: 1px solid #dfd2bd;

  border-radius: 10px;

  padding: 9px 12px;

  font-size: 14px;

  color: #4a3824;

  outline: none;

  transition: 0.2s;

  box-sizing: border-box;
}

.field input:focus,
.field select:focus,
.field textarea:focus {
  border-color: #b9975b;

  box-shadow: 0 0 0 3px rgba(185, 151, 91, 0.15);
}

.field textarea {
  resize: vertical;

  min-height: 90px;
}
/* ================= SEARCH KHÁCH HÀNG ================= */

.search-result {
  position: absolute;

  top: calc(100% + 4px);

  left: 0;

  right: 0;

  background: #fff;

  border: 1px solid #dfd2bd;

  border-radius: 12px;

  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.12);

  max-height: 220px;

  overflow-y: auto;

  z-index: 50;
}

.customer-item {
  display: flex;

  justify-content: space-between;

  align-items: center;

  padding: 10px 14px;

  cursor: pointer;

  transition: 0.2s;
}

.customer-item:hover {
  background: #faf1df;
}

.customer-item strong {
  color: #4a3824;

  font-size: 14px;
}

.customer-item span {
  color: #8b7658;

  font-size: 13px;
}
.search-box {
  position: relative;
}

/* ================= TABLE GRID ================= */

.table-grid {
  display: grid;

  grid-template-columns: repeat(3, 1fr);

  gap: 12px;
}

.table-card {
  border: 1px solid #eadbc4;

  border-radius: 12px;

  padding: 12px;

  background: #fffaf2;

  cursor: pointer;

  transition: 0.2s;
}

.table-card:hover {
  transform: translateY(-2px);

  border-color: #b9975b;
}

.table-card.active {
  border: 2px solid #b9975b;

  background: #fff3d8;
}

.table-name {
  font-weight: 700;

  color: #4a3824;
}

.table-info {
  display: flex;

  justify-content: space-between;

  margin-top: 8px;

  font-size: 13px;
}

.table-area {
  margin-top: 8px;

  color: #927653;

  font-size: 12px;
}

.table-card.disabled {
  opacity: 0.35;
  pointer-events: none;
  filter: grayscale(100%);
  cursor: not-allowed;
}

.table-card.active {
  border: 2px solid #c28d2c;
  background: #fff8ea;
}

/* ================= EMPTY ================= */

.empty {
  padding: 25px;

  text-align: center;

  color: #98856b;

  background: #faf7f1;

  border-radius: 12px;
}

/* ================= FOOTER ================= */

.dialog-footer {
  display: flex;

  justify-content: flex-end;

  gap: 12px;

  padding: 0 22px 22px;
}

.btn-cancel,
.btn-save {
  border: none;

  padding: 11px 24px;

  border-radius: 12px;

  cursor: pointer;

  font-weight: 600;

  transition: 0.2s;
}

.btn-cancel {
  background: #eee7dc;

  color: #69553a;
}

.btn-cancel:hover {
  background: #ddd1bd;
}

.btn-save {
  background: #b9975b;

  color: white;
}

.btn-save:hover {
  background: #a27f45;

  transform: translateY(-1px);
}

/* ================= RESPONSIVE ================= */

@media (max-width: 1000px) {
  .dialog-body {
    grid-template-columns: 1fr;
  }

  .table-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .row {
    flex-direction: column;
  }

  .table-grid {
    grid-template-columns: 1fr;
  }
}
</style>
