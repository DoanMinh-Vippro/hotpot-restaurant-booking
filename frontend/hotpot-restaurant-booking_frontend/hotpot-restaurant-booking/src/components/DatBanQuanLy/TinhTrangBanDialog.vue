<script setup lang="ts">
import { ref, watch } from 'vue'
import DatBanQuanLy from '@/api/DatBanQuanLy'

interface BanResponse {
  idBan: number
  tenBan: string
  loaiBan: string
  sucChua: number
  idKhuVuc: number
  tenKhuVuc: string
  trangThai: string
}

interface TinhTrangBanResponse {
  soBanConLai: number
  tongSucChua: number

  soBanDangSuDung: number
  soBanDaDat: number
  soBanTrong: number

  dsBanDangSuDung: BanResponse[]
  dsBanDaDat: BanResponse[]
  dsBanTrong: BanResponse[]
}

const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits<{
  close: []
}>()

const loading = ref(false)
const errorMessage = ref('')

const result = ref<TinhTrangBanResponse | null>(null)

// Thời gian muốn kiểm tra
const thoiGianKiemTra = ref('')

// ======================================================
// Lấy thời gian hiện tại theo format datetime-local
// ======================================================

const getCurrentDateTimeLocal = () => {
  const now = new Date()

  return (
    `${now.getFullYear()}-` +
    `${String(now.getMonth() + 1).padStart(2, '0')}-` +
    `${String(now.getDate()).padStart(2, '0')}T` +
    `${String(now.getHours()).padStart(2, '0')}:` +
    `${String(now.getMinutes()).padStart(2, '0')}`
  )
}

// ======================================================
// Gọi API kiểm tra tình trạng bàn theo giờ
// ======================================================

const loadTinhTrangBan = async () => {
  if (!thoiGianKiemTra.value) {
    errorMessage.value = 'Vui lòng chọn thời gian cần kiểm tra.'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const thoiGianDenDuKien = `${thoiGianKiemTra.value}:00`

    const res = await DatBanQuanLy.tinhTrangBan(thoiGianDenDuKien)

    // Backend hiện tại trả về List<DTOBanResponse>
    const dsBan: BanResponse[] = res.data

    const dsBanDangSuDung = dsBan.filter((ban) => ban.trangThai === 'DANG_SU_DUNG')

    const dsBanDaDat = dsBan.filter((ban) => ban.trangThai === 'DA_DAT')

    const dsBanTrong = dsBan.filter((ban) => ban.trangThai === 'TRONG')

    const tongSucChua = dsBanTrong.reduce((tong, ban) => tong + (ban.sucChua || 0), 0)

    result.value = {
      soBanConLai: dsBanTrong.length,
      tongSucChua,

      soBanDangSuDung: dsBanDangSuDung.length,
      soBanDaDat: dsBanDaDat.length,
      soBanTrong: dsBanTrong.length,

      dsBanDangSuDung,
      dsBanDaDat,
      dsBanTrong,
    }
  } catch (error) {
    console.error('Lỗi lấy tình trạng bàn:', error)

    errorMessage.value = 'Không thể lấy tình trạng bàn tại thời gian này.'

    result.value = null
  } finally {
    loading.value = false
  }
}

// ======================================================
// Khi mở popup
// ======================================================

watch(
  () => props.show,
  (show) => {
    if (show) {
      // Mặc định chọn giờ hiện tại
      thoiGianKiemTra.value = getCurrentDateTimeLocal()

      // Chưa gọi API ngay
      result.value = null
      errorMessage.value = ''
    }
  },
)

// ======================================================
// Đóng popup
// ======================================================

const closeDialog = () => {
  emit('close')
}
</script>

<template>
  <div v-if="props.show" class="dialog-overlay">
    <div class="dialog-container">
      <!-- ================= HEADER ================= -->
      <div class="dialog-header">
        <div>
          <h3>Kiểm tra tình trạng bàn</h3>

          <p>Kiểm tra tình trạng bàn theo thời gian</p>
        </div>

        <button type="button" class="btn-close" @click="closeDialog">×</button>
      </div>

      <!-- ================= BODY ================= -->
      <div class="dialog-body">
        <!-- ================= CHỌN THỜI GIAN ================= -->
        <div class="time-check-box">
          <div class="time-check-content">
            <label class="time-label"> Thời gian kiểm tra </label>

            <span class="time-description"> Chọn thời điểm muốn kiểm tra tình trạng các bàn </span>
          </div>

          <div class="time-check-action">
            <input v-model="thoiGianKiemTra" type="datetime-local" class="time-input" />

            <button type="button" class="btn-check" :disabled="loading" @click="loadTinhTrangBan">
              {{ loading ? 'Đang kiểm tra...' : 'Kiểm tra' }}
            </button>
          </div>
        </div>

        <!-- ================= LOADING ================= -->
        <div v-if="loading" class="loading-box">
          <div class="loading-spinner"></div>

          <span> Đang kiểm tra tình trạng bàn... </span>
        </div>

        <!-- ================= ERROR ================= -->
        <div v-else-if="errorMessage" class="error-box">
          <div class="error-icon">!</div>

          <span>
            {{ errorMessage }}
          </span>
        </div>

        <!-- ================= DATA ================= -->
        <template v-else-if="result">
          <!-- ================= THỐNG KÊ ================= -->
          <div class="summary-grid">
            <!-- ĐANG SỬ DỤNG -->
            <div class="summary-card used">
              <div class="summary-icon">●</div>

              <div class="summary-info">
                <span class="summary-label"> Đang sử dụng </span>

                <strong>
                  {{ result.soBanDangSuDung }}
                </strong>

                <small> bàn </small>
              </div>
            </div>

            <!-- ĐÃ ĐẶT -->
            <div class="summary-card booked">
              <div class="summary-icon">●</div>

              <div class="summary-info">
                <span class="summary-label"> Đã đặt </span>

                <strong>
                  {{ result.soBanDaDat }}
                </strong>

                <small> bàn </small>
              </div>
            </div>

            <!-- BÀN TRỐNG -->
            <div class="summary-card empty">
              <div class="summary-icon">●</div>

              <div class="summary-info">
                <span class="summary-label"> Bàn trống </span>

                <strong>
                  {{ result.soBanTrong }}
                </strong>

                <small> bàn </small>
              </div>
            </div>
          </div>

          <!-- ================= TỔNG SỨC CHỨA ================= -->
          <div class="capacity-box">
            <div class="capacity-left">
              <span class="capacity-title"> Tổng sức chứa bàn trống </span>

              <span class="capacity-description"> Khả năng phục vụ tại thời điểm đã chọn </span>
            </div>

            <div class="capacity-value">
              {{ result.tongSucChua }}

              <span> người </span>
            </div>
          </div>

          <!-- ================= DANH SÁCH BÀN ================= -->
          <div class="table-section">
            <!-- ==================================================
                 ĐANG SỬ DỤNG
            =================================================== -->
            <details class="table-group">
              <summary class="group-header">
                <div class="group-title used-title">
                  <span class="status-dot used-dot"></span>

                  <span> Đang sử dụng </span>
                </div>

                <div class="group-right">
                  <span class="group-count"> {{ result.dsBanDangSuDung?.length || 0 }} bàn </span>

                  <span class="expand-icon"> › </span>
                </div>
              </summary>

              <div class="table-content">
                <div v-if="result.dsBanDangSuDung?.length" class="table-list">
                  <div v-for="ban in result.dsBanDangSuDung" :key="ban.idBan" class="table-item">
                    <div class="table-main">
                      <strong>
                        {{ ban.tenBan }}
                      </strong>

                      <span>
                        {{ ban.tenKhuVuc }}
                      </span>
                    </div>

                    <div class="table-capacity">{{ ban.sucChua }} người</div>
                  </div>
                </div>

                <div v-else class="empty-list">Không có bàn đang sử dụng</div>
              </div>
            </details>

            <!-- ==================================================
                 ĐÃ ĐẶT
            =================================================== -->
            <details class="table-group">
              <summary class="group-header">
                <div class="group-title booked-title">
                  <span class="status-dot booked-dot"></span>

                  <span> Đã đặt </span>
                </div>

                <div class="group-right">
                  <span class="group-count"> {{ result.dsBanDaDat?.length || 0 }} bàn </span>

                  <span class="expand-icon"> › </span>
                </div>
              </summary>

              <div class="table-content">
                <div v-if="result.dsBanDaDat?.length" class="table-list">
                  <div v-for="ban in result.dsBanDaDat" :key="ban.idBan" class="table-item">
                    <div class="table-main">
                      <strong>
                        {{ ban.tenBan }}
                      </strong>

                      <span>
                        {{ ban.tenKhuVuc }}
                      </span>
                    </div>

                    <div class="table-capacity">{{ ban.sucChua }} người</div>
                  </div>
                </div>

                <div v-else class="empty-list">Không có bàn đã đặt</div>
              </div>
            </details>

            <!-- ==================================================
                 BÀN TRỐNG
            =================================================== -->
            <details class="table-group">
              <summary class="group-header">
                <div class="group-title empty-title">
                  <span class="status-dot empty-dot"></span>

                  <span> Bàn trống </span>
                </div>

                <div class="group-right">
                  <span class="group-count"> {{ result.dsBanTrong?.length || 0 }} bàn </span>

                  <span class="expand-icon"> › </span>
                </div>
              </summary>

              <div class="table-content">
                <div v-if="result.dsBanTrong?.length" class="table-list">
                  <div v-for="ban in result.dsBanTrong" :key="ban.idBan" class="table-item">
                    <div class="table-main">
                      <strong>
                        {{ ban.tenBan }}
                      </strong>

                      <span>
                        {{ ban.tenKhuVuc }}
                      </span>
                    </div>

                    <div class="table-capacity">{{ ban.sucChua }} người</div>
                  </div>
                </div>

                <div v-else class="empty-list">Không có bàn trống</div>
              </div>
            </details>
          </div>
        </template>
      </div>

      <!-- ================= FOOTER ================= -->
      <div class="dialog-footer">
        <button type="button" class="btn-close-dialog" @click="closeDialog">Đóng</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ======================================================
   OVERLAY
====================================================== */

.dialog-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;

  display: flex;
  align-items: center;
  justify-content: center;

  padding: 20px;

  background: rgba(35, 30, 20, 0.45);
  backdrop-filter: blur(5px);
}

/* ======================================================
   CONTAINER
====================================================== */

.dialog-container {
  width: 100%;
  max-width: 1050px;
  height: 90vh;
  max-height: 90vh;

  display: flex;
  flex-direction: column;

  overflow: hidden;

  background: #f7f5f0;

  border: 1px solid #d8d0c1;
  border-radius: 16px;

  box-shadow:
    0 25px 70px rgba(0, 0, 0, 0.25),
    0 5px 20px rgba(0, 0, 0, 0.08);
}

/* ======================================================
   HEADER
====================================================== */

.dialog-header {
  flex-shrink: 0;

  display: flex;
  align-items: center;
  justify-content: space-between;

  padding: 20px 26px;

  background: #fffdf9;

  border-bottom: 1px solid #e3ddd2;
}

.dialog-header h3 {
  margin: 0;

  color: #5a4427;

  font-size: 24px;
  font-weight: 700;

  letter-spacing: 0.5px;
}

.dialog-header p {
  margin: 5px 0 0;

  color: #8c8172;

  font-size: 13px;
}

/* ======================================================
   CLOSE
====================================================== */

.btn-close {
  width: 38px;
  height: 38px;

  display: flex;
  align-items: center;
  justify-content: center;

  border: 1px solid #ddd5c8;
  border-radius: 10px;

  background: #f6f3ed;
  color: #777;

  font-size: 25px;
  line-height: 1;

  cursor: pointer;

  transition: 0.2s;
}

.btn-close:hover {
  color: #6a4f27;
  border-color: #c9a961;
  background: #fffaf0;
  transform: translateY(-1px);
}

/* ======================================================
   BODY
====================================================== */

.dialog-body {
  flex: 1;

  min-height: 0;

  overflow-y: auto;

  padding: 22px 26px;

  background: #f7f5f0;
}

.dialog-body::-webkit-scrollbar {
  width: 7px;
}

.dialog-body::-webkit-scrollbar-track {
  background: #eeeae2;
}

.dialog-body::-webkit-scrollbar-thumb {
  background: #c8c0b3;
  border-radius: 10px;
}

.dialog-body::-webkit-scrollbar-thumb:hover {
  background: #aa8b4d;
}

/* ======================================================
   TIME CHECK
====================================================== */

.time-check-box {
  display: flex;
  align-items: center;
  justify-content: space-between;

  gap: 20px;

  margin-bottom: 18px;
  padding: 16px 18px;

  background: #ffffff;

  border: 1px solid #e0dbd2;
  border-radius: 12px;

  box-shadow: 0 3px 10px rgba(80, 65, 40, 0.04);
}

.time-check-content {
  display: flex;
  flex-direction: column;

  gap: 4px;

  min-width: 0;
}

.time-label {
  color: #5a4427;

  font-size: 14px;
  font-weight: 700;
}

.time-description {
  color: #8c8378;

  font-size: 12px;
}

.time-check-action {
  display: flex;
  align-items: center;

  gap: 10px;

  flex-shrink: 0;
}

.time-input {
  height: 40px;

  padding: 0 11px;

  color: #4d4337;

  background: #faf9f6;

  border: 1px solid #d8d2c8;
  border-radius: 8px;

  outline: none;

  font-family: inherit;
  font-size: 13px;

  transition: 0.2s;
}

.time-input:focus {
  border-color: #c29a4b;

  background: #fffdf8;

  box-shadow: 0 0 0 3px rgba(194, 154, 75, 0.1);
}

.btn-check {
  height: 40px;

  padding: 0 18px;

  border: none;
  border-radius: 8px;

  background: linear-gradient(135deg, #b88b3f, #d5b363);

  color: #ffffff;

  font-size: 13px;
  font-weight: 700;

  cursor: pointer;

  transition: 0.2s;

  box-shadow: 0 4px 10px rgba(160, 120, 45, 0.15);
}

.btn-check:hover:not(:disabled) {
  transform: translateY(-1px);

  background: linear-gradient(135deg, #a77b32, #cbaa5b);

  box-shadow: 0 6px 15px rgba(160, 120, 45, 0.2);
}

.btn-check:disabled {
  opacity: 0.6;

  cursor: not-allowed;
}

/* ======================================================
   LOADING
====================================================== */

.loading-box {
  min-height: 250px;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  gap: 15px;

  color: #81796d;

  font-size: 14px;
}

.loading-spinner {
  width: 38px;
  height: 38px;

  border: 3px solid #ddd7cd;
  border-top-color: #b9975b;

  border-radius: 50%;

  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ======================================================
   ERROR
====================================================== */

.error-box {
  min-height: 220px;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  gap: 12px;

  color: #c44e4e;

  font-size: 14px;
}

.error-icon {
  width: 42px;
  height: 42px;

  display: flex;
  align-items: center;
  justify-content: center;

  border: 1px solid #e1aaaa;
  border-radius: 50%;

  background: #fff1f1;

  font-size: 22px;
  font-weight: bold;
}

/* ======================================================
   SUMMARY
====================================================== */

.summary-grid {
  display: grid;

  grid-template-columns: repeat(3, minmax(0, 1fr));

  gap: 14px;

  margin-bottom: 16px;
}

.summary-card {
  display: flex;
  align-items: center;

  gap: 12px;

  padding: 16px;

  background: #ffffff;

  border: 1px solid #e1ddd5;
  border-radius: 12px;

  box-shadow: 0 3px 10px rgba(80, 65, 40, 0.05);

  transition: 0.2s;
}

.summary-card:hover {
  transform: translateY(-1px);

  box-shadow: 0 5px 14px rgba(80, 65, 40, 0.08);
}

.summary-icon {
  font-size: 14px;
}

.summary-info {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;

  gap: 5px;
}

.summary-label {
  width: 100%;

  color: #81796d;

  font-size: 12px;
}

.summary-info strong {
  color: #4b4033;

  font-size: 25px;
}

.summary-info small {
  color: #8d8579;
}

.used .summary-icon {
  color: #d45b5b;
}

.booked .summary-icon {
  color: #c69b3a;
}

.empty .summary-icon {
  color: #4dac78;
}

/* ======================================================
   CAPACITY
====================================================== */

.capacity-box {
  display: flex;
  align-items: center;
  justify-content: space-between;

  margin-bottom: 20px;

  padding: 17px 20px;

  background: #fffaf0;

  border: 1px solid #e4d4ad;
  border-radius: 12px;

  box-shadow: 0 3px 10px rgba(120, 90, 30, 0.04);
}

.capacity-left {
  display: flex;
  flex-direction: column;

  gap: 4px;
}

.capacity-title {
  color: #765b2d;

  font-size: 14px;
  font-weight: 700;
}

.capacity-description {
  color: #918575;

  font-size: 12px;
}

.capacity-value {
  color: #a17b32;

  font-size: 25px;
  font-weight: 700;
}

.capacity-value span {
  color: #81796d;

  font-size: 13px;
  font-weight: 400;
}

/* ======================================================
   TABLE SECTION
====================================================== */

.table-section {
  display: flex;
  flex-direction: column;

  gap: 10px;

  min-height: 0;
}

/* ======================================================
   TABLE GROUP
   MẶC ĐỊNH THU GỌN
====================================================== */

.table-group {
  display: flex;
  flex-direction: column;

  min-height: 0;

  overflow: hidden;

  background: #ffffff;

  border: 1px solid #ddd8cf;
  border-radius: 12px;

  box-shadow: 0 3px 10px rgba(70, 55, 35, 0.04);

  /*
   * Không ép max-height ở đây.
   * Header vẫn nhỏ gọn, danh sách chỉ xuất hiện
   * khi template của m bật/hiển thị nó.
   */
}

/* ======================================================
   GROUP HEADER
====================================================== */

.group-header {
  flex-shrink: 0;

  display: flex;
  align-items: center;
  justify-content: space-between;

  min-height: 52px;

  padding: 13px 16px;

  background: #f1eee8;

  border-bottom: 1px solid #ded9d0;

  cursor: pointer;

  user-select: none;

  transition: 0.2s;
}

.group-header:hover {
  background: #ebe6dc;
}

.group-title {
  display: flex;
  align-items: center;

  gap: 9px;

  font-size: 14px;
  font-weight: 700;
}

.status-dot {
  width: 8px;
  height: 8px;

  flex-shrink: 0;

  border-radius: 50%;
}

.used-dot {
  background: #d95d5d;

  box-shadow: 0 0 7px rgba(217, 93, 93, 0.35);
}

.booked-dot {
  background: #c89b35;

  box-shadow: 0 0 7px rgba(200, 155, 53, 0.35);
}

.empty-dot {
  background: #4caf7b;

  box-shadow: 0 0 7px rgba(76, 175, 123, 0.35);
}

.used-title {
  color: #b54c4c;
}

.booked-title {
  color: #96701f;
}

.empty-title {
  color: #347e59;
}

.group-count {
  color: #888077;

  font-size: 12px;
}

/* ======================================================
   TABLE LIST
   Khi mở nhóm, list chỉ chiếm một vùng cố định
   và tự cuộn.
====================================================== */

.table-list {
  display: grid;

  grid-template-columns: repeat(2, minmax(0, 1fr));

  gap: 8px;

  padding: 12px;

  max-height: 230px;

  overflow-y: auto;

  background: #ffffff;
}

/* ======================================================
   TABLE LIST SCROLLBAR
====================================================== */

.table-list::-webkit-scrollbar {
  width: 6px;
}

.table-list::-webkit-scrollbar-track {
  background: #f1eee8;

  border-radius: 10px;
}

.table-list::-webkit-scrollbar-thumb {
  background: #c7c0b5;

  border-radius: 10px;
}

.table-list::-webkit-scrollbar-thumb:hover {
  background: #a98a4c;
}

/* ======================================================
   TABLE ITEM
====================================================== */

.table-item {
  display: flex;
  align-items: center;
  justify-content: space-between;

  min-width: 0;

  min-height: 52px;

  padding: 10px 13px;

  background: #faf9f6;

  border: 1px solid #dfdbd3;
  border-radius: 8px;

  transition: 0.2s;
}

.table-item:hover {
  border-color: #c8a65b;

  background: #fffdf7;

  transform: translateY(-1px);

  box-shadow: 0 3px 8px rgba(100, 75, 35, 0.06);
}

/* ======================================================
   TABLE INFO
====================================================== */

.table-main {
  display: flex;
  flex-direction: column;

  gap: 3px;

  min-width: 0;
}

.table-main strong {
  overflow: hidden;

  color: #403a33;

  font-size: 14px;
  font-weight: 700;

  white-space: nowrap;
  text-overflow: ellipsis;
}

.table-main span {
  overflow: hidden;

  color: #898178;

  font-size: 11px;

  white-space: nowrap;
  text-overflow: ellipsis;
}

.table-capacity {
  flex-shrink: 0;

  margin-left: 10px;

  color: #a07c32;

  font-size: 12px;
  font-weight: 700;
}

/* ======================================================
   EMPTY LIST
====================================================== */

.empty-list {
  display: flex;
  align-items: center;
  justify-content: center;

  min-height: 60px;

  padding: 15px;

  color: #9a9287;

  text-align: center;

  font-size: 13px;
}

/* ======================================================
   FOOTER
====================================================== */

.dialog-footer {
  flex-shrink: 0;

  display: flex;
  justify-content: flex-end;

  padding: 15px 26px;

  background: #fffdf9;

  border-top: 1px solid #e1ddd5;
}

.btn-close-dialog {
  min-width: 110px;
  height: 42px;

  padding: 0 22px;

  border: none;
  border-radius: 10px;

  background: linear-gradient(135deg, #b88b3f, #d8b96a);

  color: #ffffff;

  font-size: 14px;
  font-weight: 700;

  cursor: pointer;

  transition: 0.2s;

  box-shadow: 0 4px 12px rgba(160, 120, 45, 0.2);
}

.btn-close-dialog:hover {
  transform: translateY(-1px);

  background: linear-gradient(135deg, #a77b32, #cbaa5b);

  box-shadow: 0 7px 18px rgba(160, 120, 45, 0.25);
}

/* ======================================================
   RESPONSIVE
====================================================== */

@media (max-width: 900px) {
  .dialog-container {
    max-width: 95vw;
  }

  .summary-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .time-check-box {
    align-items: flex-start;
    flex-direction: column;
  }

  .time-check-action {
    width: 100%;
  }

  .time-input {
    flex: 1;
  }
}

@media (max-width: 700px) {
  .dialog-overlay {
    padding: 12px;
  }

  .dialog-container {
    width: 100%;
    max-width: none;

    height: 94vh;
    max-height: 94vh;
  }

  .dialog-header {
    padding: 18px;
  }

  .dialog-header h3 {
    font-size: 20px;
  }

  .dialog-body {
    padding: 18px;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .capacity-box {
    align-items: flex-start;
    flex-direction: column;

    gap: 10px;
  }

  .table-list {
    grid-template-columns: 1fr;

    max-height: 260px;
  }

  .time-check-action {
    flex-direction: column;

    align-items: stretch;
  }

  .time-input,
  .btn-check {
    width: 100%;
  }

  .dialog-footer {
    padding: 14px 18px;
  }
}

@media (max-width: 450px) {
  .dialog-header {
    padding: 15px;
  }

  .dialog-header h3 {
    font-size: 18px;
  }

  .dialog-header p {
    font-size: 11px;
  }

  .dialog-body {
    padding: 15px;
  }

  .summary-card {
    padding: 14px;
  }

  .capacity-box {
    padding: 15px;
  }

  .group-header {
    padding: 12px;
  }

  .table-list {
    padding: 10px;

    max-height: 230px;
  }

  .dialog-footer {
    padding: 12px 15px;
  }
}
</style>
