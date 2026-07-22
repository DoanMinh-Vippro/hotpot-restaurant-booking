<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  visible: boolean
  reservation: any | null
  dsBanTrong: any[]
}>()

const emit = defineEmits(['close', 'save'])
const selectedTables = ref<number[]>([])
const banToiUu = ref<number[]>([])
const canSave = () => selectedTables.value.length > 0
const hetBanPhuHop = () => banToiUu.value.length === 0
const dsBanHienTai = () => props.reservation?.dsBan?.map((b: any) => b.idBan) ?? []

const tongSucChuaDaChon = () => {
  return props.dsBanTrong
    .filter((b) => selectedTables.value.includes(b.idBan))
    .reduce((sum, b) => sum + b.sucChua, 0)
}

const sucChuaToiUu = () => {
  if (banToiUu.value.length === 0) return 0

  const ban = props.dsBanTrong.find((b) => b.idBan === banToiUu.value[0])

  return ban?.sucChua ?? 0
}

const timToHopToiUu = () => {
  const soNguoi = props.reservation.soNguoi

  const ds = props.dsBanTrong
    .filter((b) => !dsBanHienTai().includes(b.idBan))
    .sort((a, b) => a.sucChua - b.sucChua)

  // ===========================
  // B1. Bàn đơn vừa đủ
  // ===========================
  const banDonVua = ds.find((b) => b.sucChua === soNguoi)

  if (banDonVua) {
    banToiUu.value = [banDonVua.idBan]
    return
  }

  // ===========================
  // B2. Bàn đơn dư tối đa 1 ghế
  // ===========================
  const banDonDu1 = ds.find((b) => b.sucChua === soNguoi + 1)

  if (banDonDu1) {
    banToiUu.value = [banDonDu1.idBan]
    return
  }

  // ===========================
  // B3 + B4. Ghép bàn
  // ===========================

  let ketQua: any[] | null = null
  let tongTotNhat = Number.MAX_SAFE_INTEGER

  const deQuy = (index: number, toHop: any[], tong: number) => {
    if (tong >= soNguoi && tong <= soNguoi + 1) {
      if (
        ketQua == null ||
        tong < tongTotNhat ||
        (tong === tongTotNhat && toHop.length < ketQua.length)
      ) {
        ketQua = [...toHop]
        tongTotNhat = tong
      }

      return
    }

    if (tong > soNguoi + 1) return

    if (index >= ds.length) return

    deQuy(index + 1, [...toHop, ds[index]], tong + ds[index].sucChua)

    deQuy(index + 1, toHop, tong)
  }

  deQuy(0, [], 0)

  if (ketQua) {
    const candidate: any[] = ketQua ? [...ketQua] : []
    banToiUu.value = candidate.map((b: any) => b.idBan)
    return
  }

  // ===========================
  // B5. Lấy bàn đơn nhỏ nhất còn lại
  // ===========================
  const banLonNhatConLai = ds.find((b) => b.sucChua > soNguoi)

  if (banLonNhatConLai) {
    banToiUu.value = [banLonNhatConLai.idBan]
    return
  }

  // ===========================
  // B6. Hết bàn
  // ===========================
  banToiUu.value = []
}

watch(
  () => [props.dsBanTrong, props.reservation],
  () => {
    if (props.reservation) {
      timToHopToiUu()
    }
  },
  {
    deep: true,
    immediate: true,
  },
)

const canSelectBan = (ban: any) => {
  // Đã chọn thì luôn bỏ được
  if (selectedTables.value.includes(ban.idBan)) {
    return true
  }
  // Đủ người rồi thì khóa các bàn còn lại
  if (tongSucChuaDaChon() >= props.reservation.soNguoi) {
    return false
  }
  // Chưa chọn bàn nào
  if (selectedTables.value.length === 0) {
    // Nếu tổ hợp tối ưu chỉ có 1 bàn
    if (banToiUu.value.length === 1) {
      // Cho phép chọn mọi bàn cùng sức chứa
      return ban.sucChua === sucChuaToiUu()
    }

    // Nếu tổ hợp tối ưu nhiều bàn
    return banToiUu.value.includes(ban.idBan)
  }
  // Đã chọn ít nhất 1 bàn
  return banToiUu.value.includes(ban.idBan)
}

const toggleBan = (ban: any) => {
  if (!canSelectBan(ban)) return

  const index = selectedTables.value.indexOf(ban.idBan)

  if (index >= 0) {
    selectedTables.value.splice(index, 1)
  } else {
    selectedTables.value.push(ban.idBan)
  }
}

watch(
  () => props.reservation,
  (value) => {
    if (!value) {
      selectedTables.value = []
      return
    }

    selectedTables.value = []
  },
  {
    immediate: true,
  },
)

const close = () => {
  emit('close')
}

const save = () => {
  if (!canSave() || hetBanPhuHop()) return

  emit('save', {
    dsBan: selectedTables.value,
  })
}

const formatDateTime = (dateTime?: string) => {
  if (!dateTime) return ''

  const date = new Date(dateTime)

  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="overlay" @click.self="close">
      <div class="dialog">
        <div class="dialog-header">
          <div>
            <h2>Đổi bàn</h2>
            <p>Đơn #{{ reservation?.idDatBan }}</p>
          </div>

          <button class="close-btn" @click="close">✕</button>
        </div>

        <div class="content">
          <div class="info-box">
            <div>
              <label>Khách hàng</label>
              <strong>{{ reservation?.tenKhachHang }}</strong>
            </div>

            <div>
              <label>Số người</label>
              <strong>{{ reservation?.soNguoi }}</strong>
            </div>

            <div>
              <label>Giờ đến</label>
              <strong>{{ formatDateTime(reservation?.thoiGianDenDuKien) }}</strong>
            </div>
          </div>

          <h3>Chọn bàn mới</h3>

          <div v-if="dsBanTrong.length === 0" class="warning-text">
            Không còn bàn trống trong khung giờ này.
          </div>

          <div v-else-if="hetBanPhuHop()" class="warning-text">
            Không còn tổ hợp bàn phù hợp cho {{ reservation?.soNguoi }} khách.
          </div>
          <div class="table-grid">
            <div
              v-for="ban in dsBanTrong"
              :key="ban.idBan"
              class="table-card"
              :class="{
                active: selectedTables.includes(ban.idBan),
                disabled: !canSelectBan(ban),
              }"
              @click="toggleBan(ban)"
            >
              <div class="table-name">
                {{ ban.tenBan }}
              </div>

              <div class="table-capacity">{{ ban.sucChua }} người</div>
            </div>
          </div>
        </div>

        <div class="dialog-footer">
          <button class="btn cancel" @click="close">Huỷ</button>

          <button class="btn save" :disabled="!canSave() || hetBanPhuHop()" @click="save">
            Xác nhận đổi bàn
          </button>
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

.warning-text {
  margin: 16px 0;
  padding: 14px;
  border-radius: 10px;
  background: #fff3cd;
  border: 1px solid #ffe08a;
  color: #8a6d3b;
  text-align: center;
  font-weight: 600;
}

.table-card.disabled {
  opacity: 0.35;
  pointer-events: none;
  filter: grayscale(100%);
  cursor: not-allowed;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.dialog {
  width: 900px;
  max-width: 95vw;
  max-height: 90vh;
  overflow-y: auto;
  background: #f8f5ef;
  border-radius: 18px;
  border: 1px solid #e6d8bb;
  box-shadow: 0 18px 45px rgba(0, 0, 0, 0.18);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 28px;
  border-bottom: 1px solid #e7dcc8;
}

.dialog-header h2 {
  margin: 0;
  color: #3b3124;
}

.dialog-header p {
  margin-top: 5px;
  color: #887861;
}

.close-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 10px;
  background: #ece3d3;
  cursor: pointer;
  transition: 0.25s;
}

.close-btn:hover {
  background: #d7c8ad;
}

.content {
  padding: 26px;
}

.info-box {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  margin-bottom: 28px;
}

.info-box div {
  background: white;
  border: 1px solid #ece1cc;
  border-radius: 12px;
  padding: 18px;
}

.info-box label {
  display: block;
  color: #9d8d72;
  font-size: 13px;
  margin-bottom: 6px;
  text-transform: uppercase;
}

.info-box strong {
  font-size: 17px;
  color: #2e2b27;
}

h3 {
  margin-bottom: 18px;
  color: #6b5536;
}

.table-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 18px;
}

.table-card {
  background: white;
  border: 2px solid #e6dac4;
  border-radius: 14px;
  padding: 18px;
  cursor: pointer;
  transition: 0.25s;
  text-align: center;
}

.table-card:hover {
  transform: translateY(-4px);

  border-color: #c6a96d;

  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);
}

.table-card.active {
  background: #c8a15b;

  color: white;

  border-color: #c8a15b;
}

.table-name {
  font-size: 18px;

  font-weight: 700;

  margin-bottom: 8px;
}

.table-capacity {
  font-size: 14px;

  opacity: 0.85;
}

.dialog-footer {
  display: flex;

  justify-content: flex-end;

  gap: 12px;

  padding: 22px 28px;

  border-top: 1px solid #e7dcc8;
}

.btn {
  border: none;

  border-radius: 10px;

  padding: 12px 22px;

  font-weight: 600;

  cursor: pointer;

  transition: 0.25s;
}

.cancel {
  background: #d9d9d9;
}

.cancel:hover {
  background: #c5c5c5;
}

.save {
  background: #c6a15b;

  color: white;
}

.save:hover {
  background: #b48d45;
}

@media (max-width: 768px) {
  .info-box {
    grid-template-columns: 1fr;
  }

  .table-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .dialog-footer {
    flex-wrap: wrap;
  }

  .btn {
    flex: 1;
  }
}
</style>
