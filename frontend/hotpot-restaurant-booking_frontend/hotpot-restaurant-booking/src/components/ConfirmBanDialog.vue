<script setup lang="ts">
import { computed } from 'vue'

/* =========================================================
   INTERFACE
========================================================= */

interface Ban {
  idBan: number
  tenBan: string
  tenKhuVuc: string
  loaiBan: string
  sucChua?: number
  idKhuVuc?: number
  trangThai?: string
}

interface CheckBanResult {
  trangThai: string
  message: string
  canGhep: boolean
  tongSucChua: number
  dsBan: Ban[]
  dsBanTrong?: Ban[] | null
  soNguoi?: number
  soNguoiKhach?: number
}

/* =========================================================
   PROPS
========================================================= */

const props = defineProps<{
  show: boolean
  result: CheckBanResult | null

  dsBanTrong?: Ban[] | null
  dsBanDeXuat?: Ban[] | null

  /*
   * Danh sách ID bàn đang được Form cha chọn.
   */
  banDangChon: number[]

  /*
   * Số người lấy TRỰC TIẾP từ Form.
   *
   * Ví dụ:
   * Form = 9 người
   * => Dialog nhận soNguoi = 9
   */
  soNguoi: number
}>()

/* =========================================================
   EMIT
========================================================= */

const emit = defineEmits<{
  (e: 'chon-ban', ban: Ban): void
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

/* =========================================================
   DANH SÁCH BÀN GỐC
========================================================= */

const danhSachBanGoc = computed<Ban[]>(() => {
  /*
   * Ưu tiên dsBanTrong từ Form.
   */
  if (props.dsBanTrong && props.dsBanTrong.length > 0) {
    return props.dsBanTrong
  }

  /*
   * Nếu không có thì lấy từ result.
   */
  return props.result?.dsBan ?? []
})

/* =========================================================
   ID BÀN ĐƯỢC HỆ THỐNG ĐỀ XUẤT
========================================================= */

const idBanDeXuat = computed<Set<number>>(() => {
  return new Set((props.dsBanDeXuat ?? []).map((ban) => ban.idBan))
})

/* =========================================================
   DANH SÁCH BÀN HIỂN THỊ
========================================================= */

const danhSachBanHienThi = computed<Ban[]>(() => {
  return [...danhSachBanGoc.value].sort((a, b) => {
    const aUuTien = idBanDeXuat.value.has(a.idBan)
    const bUuTien = idBanDeXuat.value.has(b.idBan)

    /*
     * Bàn đề xuất nằm trên cùng.
     */
    if (aUuTien && !bUuTien) {
      return -1
    }

    if (!aUuTien && bUuTien) {
      return 1
    }

    /*
     * Cùng mức ưu tiên thì sort theo ID.
     */
    return a.idBan - b.idBan
  })
})

/* =========================================================
   SỐ NGƯỜI KHÁCH
========================================================= */

const soNguoiKhach = computed<number>(() => {
  /*
   * QUAN TRỌNG:
   * Số người lấy từ Form, không phụ thuộc response check bàn.
   *
   * Form 9 người => ở đây chắc chắn là 9.
   */
  return Number(props.soNguoi) || 0
})

/* =========================================================
   CÓ BÀN ĐANG CHỌN KHÔNG?
========================================================= */

const coBanDangChon = computed<boolean>(() => {
  return props.banDangChon.length > 0
})

/* =========================================================
   KIỂM TRA MỘT BÀN CÓ ĐANG ĐƯỢC CHỌN KHÔNG
========================================================= */

const isBanDangChon = (ban: Ban): boolean => {
  return props.banDangChon.includes(ban.idBan)
}

/* =========================================================
   TỔNG SỨC CHỨA CÁC BÀN ĐANG CHỌN
========================================================= */

const tongSucChuaDangChon = computed<number>(() => {
  return danhSachBanGoc.value
    .filter((ban) => props.banDangChon.includes(ban.idBan))
    .reduce((tong, ban) => {
      return tong + Number(ban.sucChua || 0)
    }, 0)
})

/* =========================================================
   ĐÃ ĐỦ SỨC CHỨA CHƯA?
========================================================= */

const daDuSucChua = computed<boolean>(() => {
  /*
   * Nếu chưa có số người hợp lệ thì không coi là đủ.
   *
   * Tránh trường hợp:
   * 0 >= 0
   * => true
   *
   * Đây chính là nguyên nhân UI từng hiện 0 người rồi loạn.
   */
  if (soNguoiKhach.value <= 0) {
    return false
  }

  return tongSucChuaDangChon.value >= soNguoiKhach.value
})

/* =========================================================
   BÀN ĐÃ CHỌN CÓ ĐỦ MỘT MÌNH KHÔNG?
========================================================= */

const banDuSucChua = (ban: Ban): boolean => {
  const sucChua = Number(ban.sucChua || 0)

  if (soNguoiKhach.value <= 0) {
    return false
  }

  return sucChua >= soNguoiKhach.value
}

/* =========================================================
   XÁC ĐỊNH BÀN CÓ BỊ KHÓA KHÔNG
========================================================= */

const banBiKhoa = (ban: Ban): boolean => {
  /*
   * =======================================================
   * TRƯỜNG HỢP 1:
   * BÀN ĐANG ĐƯỢC CHỌN
   *
   * LUÔN cho phép click.
   *
   * Mục đích:
   * Khi đã đủ sức chứa, người dùng vẫn phải bỏ bàn
   * được nếu muốn đổi lựa chọn.
   * =======================================================
   */
  if (isBanDangChon(ban)) {
    return false
  }

  /*
   * =======================================================
   * TRƯỜNG HỢP 2:
   * CHƯA ĐỦ SỨC CHỨA
   *
   * Tất cả bàn chưa chọn đều được chọn tiếp.
   *
   * Ví dụ:
   *
   * Khách: 10
   * Đã chọn: bàn 6
   *
   * => 6 / 10
   * => bàn 2, bàn 4, bàn 6... chưa chọn đều được click.
   * =======================================================
   */
  if (!daDuSucChua.value) {
    return false
  }

  /*
   * =======================================================
   * TRƯỜNG HỢP 3:
   * ĐÃ ĐỦ SỨC CHỨA
   *
   * Khóa toàn bộ bàn CHƯA chọn.
   *
   * Ví dụ:
   *
   * Khách: 10
   * Đã chọn:
   * Bàn 6 + Bàn 4 = 10
   *
   * => khóa toàn bộ bàn còn lại.
   *
   * Bàn 6 và bàn 4 vẫn click được để bỏ chọn.
   * =======================================================
   */
  return true
}

/* =========================================================
   CLICK CHỌN / BỎ CHỌN BÀN
========================================================= */

const handleChonBan = (ban: Ban): void => {
  /*
   * Nếu bàn đang bị khóa thì không làm gì.
   */
  if (banBiKhoa(ban)) {
    return
  }

  /*
   * Việc thêm / bỏ ID được xử lý ở Form cha.
   */
  emit('chon-ban', ban)
}

/* =========================================================
   BÀN ƯU TIÊN
========================================================= */

const laBanUuTien = (ban: Ban): boolean => {
  return idBanDeXuat.value.has(ban.idBan)
}

/* =========================================================
   KHÔNG CÓ BÀN
========================================================= */

const khongCoBan = computed<boolean>(() => {
  return props.result?.trangThai === 'KHONG_CO_BAN' || danhSachBanHienThi.value.length === 0
})

/* =========================================================
   TÊN CÁC BÀN ĐANG CHỌN
========================================================= */

const tenBanDangChon = computed<string>(() => {
  if (!coBanDangChon.value) {
    return ''
  }

  return danhSachBanGoc.value
    .filter((ban) => props.banDangChon.includes(ban.idBan))
    .map((ban) => ban.tenBan)
    .join(' + ')
})

/* =========================================================
   SỐ BÀN ĐANG CHỌN
========================================================= */

const soBanDangChon = computed<number>(() => {
  return props.banDangChon.length
})

/* =========================================================
   CÓ THỂ XÁC NHẬN KHÔNG?
========================================================= */

const coTheXacNhan = computed<boolean>(() => {
  /*
   * Phải:
   * 1. Có ít nhất một bàn.
   * 2. Có số người hợp lệ.
   * 3. Tổng sức chứa >= số người.
   */
  return (
    props.banDangChon.length > 0 &&
    soNguoiKhach.value > 0 &&
    tongSucChuaDangChon.value >= soNguoiKhach.value
  )
})
</script>

<template>
  <Teleport to="body">
    <div v-if="show && result" class="overlay">
      <div class="dialog">
        <!-- =================================================
             HEADER
        ================================================== -->

        <div class="header" :class="{ error: khongCoBan }">
          <div class="header-content">
            <div class="header-icon">
              {{ khongCoBan ? '⚠️' : '🪑' }}
            </div>

            <div>
              <h2>
                {{ khongCoBan ? 'Không tìm thấy bàn' : 'Chọn bàn đặt trước' }}
              </h2>

              <p v-if="!khongCoBan">
                Chọn một hoặc nhiều bàn để đủ chỗ cho
                {{ soNguoiKhach }} người.
              </p>

              <p v-else>Rất tiếc, hiện tại không có bàn phù hợp.</p>
            </div>
          </div>
        </div>

        <!-- =================================================
             CONTENT
        ================================================== -->

        <div class="content">
          <!-- =================================================
               MESSAGE
          ================================================== -->

          <div v-if="result.message" class="message" :class="{ 'message-error': khongCoBan }">
            <span class="message-icon">
              {{ khongCoBan ? '⚠️' : '💡' }}
            </span>

            <span>
              {{ result.message }}
            </span>
          </div>

          <!-- =================================================
               KHÔNG CÓ BÀN
          ================================================== -->

          <div v-if="khongCoBan" class="empty-state">
            <div class="empty-icon">🪑</div>

            <h3>Không có bàn phù hợp</h3>

            <p>Không tìm thấy bàn trống đáp ứng yêu cầu của bạn tại thời gian đã chọn.</p>

            <div class="empty-capacity">
              <span> Tổng sức chứa có thể nhận </span>

              <strong> {{ result.tongSucChua || 0 }} người </strong>
            </div>
          </div>

          <!-- =================================================
               CÓ BÀN
          ================================================== -->

          <template v-else>
            <!-- =================================================
                 GUIDE
            ================================================== -->

            <div class="selection-guide">
              <div class="guide-icon">☝️</div>

              <div class="guide-content">
                <strong> Chọn một hoặc nhiều bàn </strong>

                <span>
                  Chọn bàn cho đến khi đủ sức chứa. Khi tổng sức chứa đạt số người cần đặt, các bàn
                  chưa chọn sẽ tự động bị khóa. Bàn có nhãn
                  <b class="guide-highlight"> Ưu tiên </b>
                  là bàn được hệ thống đề xuất.
                </span>
              </div>
            </div>

            <!-- =================================================
                 SUMMARY
            ================================================== -->

            <div class="summary-bar">
              <div class="summary-item">
                <span class="summary-label"> Bàn đang trống </span>

                <strong>
                  {{ danhSachBanHienThi.length }}
                </strong>
              </div>

              <div class="summary-divider"></div>

              <div class="summary-item">
                <span class="summary-label"> Sức chứa tối đa </span>

                <strong> {{ result.tongSucChua || 0 }} người </strong>
              </div>

              <div class="summary-divider"></div>

              <div class="summary-item selected-summary">
                <span class="summary-label"> Đã chọn </span>

                <strong v-if="coBanDangChon"> {{ soBanDangChon }} bàn </strong>

                <strong v-else class="not-selected"> Chưa chọn </strong>
              </div>
            </div>

            <!-- =================================================
                 SELECTED INFO
            ================================================== -->

            <div v-if="coBanDangChon" class="selected-info">
              <div class="selected-info-row">
                <span class="selected-info-label"> 🪑 Bàn đã chọn </span>

                <strong>
                  {{ tenBanDangChon }}
                </strong>
              </div>

              <div class="selected-info-row">
                <span class="selected-info-label"> 👥 Tổng sức chứa </span>

                <strong> {{ tongSucChuaDangChon }} / {{ soNguoiKhach }} người </strong>
              </div>

              <div
                class="capacity-status"
                :class="{
                  success: daDuSucChua,
                  warning: !daDuSucChua,
                }"
              >
                <span v-if="daDuSucChua"> ✓ Đã đủ sức chứa, có thể xác nhận đặt bàn </span>

                <span v-else> ⚠️ Chưa đủ sức chứa, hãy chọn thêm bàn </span>
              </div>
            </div>

            <!-- =================================================
                 SECTION TITLE
            ================================================== -->

            <div class="section-title">
              <div class="section-title-left">
                <span class="section-line"></span>

                <span> Tất cả bàn đang trống </span>
              </div>

              <span class="section-count"> {{ danhSachBanHienThi.length }} bàn </span>
            </div>

            <!-- =================================================
                 BAN GRID
            ================================================== -->

            <div class="ban-grid">
              <button
                v-for="ban in danhSachBanHienThi"
                :key="ban.idBan"
                type="button"
                class="ban-card"
                :class="{
                  'is-selected': isBanDangChon(ban),

                  'is-locked': banBiKhoa(ban) && !isBanDangChon(ban),

                  'is-recommended': laBanUuTien(ban),

                  'is-not-enough': !banDuSucChua(ban),
                }"
                :disabled="banBiKhoa(ban) && !isBanDangChon(ban)"
                @click="handleChonBan(ban)"
              >
                <!-- PRIORITY -->

                <div v-if="laBanUuTien(ban)" class="priority-badge">⭐ Ưu tiên</div>

                <!-- STATUS -->

                <div class="ban-status">
                  <!-- ĐANG CHỌN -->

                  <span v-if="isBanDangChon(ban)" class="status-selected"> ✓ Đang chọn </span>

                  <!-- BỊ KHÓA -->

                  <span v-else-if="banBiKhoa(ban)" class="status-locked"> 🔒 Đã đủ bàn </span>

                  <!-- CÒN ĐƯỢC CHỌN -->

                  <span v-else class="status-available"> ● Đang trống </span>
                </div>

                <!-- ICON -->

                <div class="ban-icon">🪑</div>

                <!-- NAME -->

                <div class="ban-name">
                  {{ ban.tenBan }}
                </div>

                <!-- INFO -->

                <div class="ban-info">
                  <div class="info-row">
                    <span> 📍 </span>

                    <span>
                      {{ ban.tenKhuVuc }}
                    </span>
                  </div>

                  <div class="info-row">
                    <span> 👥 </span>

                    <span> {{ ban.sucChua ?? '--' }} người </span>
                  </div>

                  <div class="info-row">
                    <span> 🏷️ </span>

                    <span>
                      {{ ban.loaiBan }}
                    </span>
                  </div>
                </div>

                <!-- SELECTED CHECK -->

                <div v-if="isBanDangChon(ban)" class="selected-check">✓</div>
              </button>
            </div>

            <!-- =================================================
                 CHƯA CHỌN
            ================================================== -->

            <div v-if="!coBanDangChon" class="validation-hint">
              <span> ⚠️ </span>

              <span> Vui lòng chọn ít nhất một bàn trước khi xác nhận. </span>
            </div>

            <!-- =================================================
                 CHƯA ĐỦ
            ================================================== -->

            <div v-else-if="!daDuSucChua" class="validation-hint warning">
              <span> ⚠️ </span>

              <span>
                Tổng sức chứa các bàn đang chọn là
                <strong>
                  {{ tongSucChuaDangChon }}
                </strong>
                / {{ soNguoiKhach }} người. Hãy chọn thêm bàn.
              </span>
            </div>

            <!-- =================================================
                 ĐỦ
            ================================================== -->

            <div v-else class="selected-banner">
              <div class="selected-banner-icon">✓</div>

              <div class="selected-banner-content">
                <span> Bạn đang chọn {{ soBanDangChon }} bàn </span>

                <strong>
                  {{ tenBanDangChon }}
                </strong>

                <small>
                  Tổng sức chứa {{ tongSucChuaDangChon }} người, đủ cho {{ soNguoiKhach }} người.
                  Các bàn khác đã được khóa. Nhấn vào bàn đang chọn để bỏ bàn.
                </small>
              </div>
            </div>
          </template>
        </div>

        <!-- =================================================
             FOOTER
        ================================================== -->

        <div class="footer">
          <button type="button" class="btn btn-cancel" @click="emit('cancel')">Hủy đặt</button>

          <button v-if="khongCoBan" type="button" class="btn btn-confirm" @click="emit('cancel')">
            Đã hiểu
          </button>

          <button
            v-else
            type="button"
            class="btn btn-confirm"
            :disabled="!coTheXacNhan"
            @click="emit('confirm')"
          >
            <span v-if="coTheXacNhan"> ✓ Xác nhận {{ soBanDangChon }} bàn </span>

            <span v-else-if="!coBanDangChon"> Chọn bàn để tiếp tục </span>

            <span v-else> Chọn thêm bàn </span>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
/* =========================================================
   OVERLAY
========================================================= */

.overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;

  display: flex;
  justify-content: center;
  align-items: center;

  padding: 25px;

  background: rgba(5, 5, 5, 0.78);
  backdrop-filter: blur(12px);

  animation: fadeIn 0.25s ease;
}

/* =========================================================
   DIALOG
========================================================= */

.dialog {
  width: 1050px;
  max-width: 96vw;
  max-height: 92vh;

  display: flex;
  flex-direction: column;

  overflow: hidden;

  background: #f8f5ee;

  border: 1px solid rgba(212, 175, 55, 0.45);
  border-radius: 22px;

  box-shadow:
    0 35px 90px rgba(0, 0, 0, 0.55),
    0 0 0 1px rgba(255, 255, 255, 0.08);

  animation: popup 0.3s ease;
}

/* =========================================================
   HEADER
========================================================= */

.header {
  position: relative;

  flex-shrink: 0;

  padding: 25px 32px;

  background: linear-gradient(135deg, #101010 0%, #202020 55%, #111111 100%);

  border-bottom: 1px solid rgba(212, 175, 55, 0.25);
}

.header::after {
  content: '';

  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;

  height: 3px;

  background: linear-gradient(90deg, #76550e, #f4d675, #76550e);
}

.header.error {
  background: linear-gradient(135deg, #351010, #641717);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 54px;
  height: 54px;

  flex-shrink: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  background: rgba(212, 175, 55, 0.1);

  border: 1px solid rgba(212, 175, 55, 0.35);
  border-radius: 14px;

  font-size: 27px;
}

.header h2 {
  margin: 0 0 5px;

  color: #f5da88;

  font-size: 25px;
  font-weight: 700;

  letter-spacing: 0.5px;
}

.header p {
  margin: 0;

  color: #999;

  font-size: 13px;
}

/* =========================================================
   CONTENT
========================================================= */

.content {
  flex: 1;

  min-height: 0;

  overflow-y: auto;

  padding: 25px 30px;
}

/* =========================================================
   MESSAGE
========================================================= */

.message {
  display: flex;
  align-items: flex-start;
  gap: 11px;

  margin-bottom: 20px;
  padding: 14px 17px;

  background: #fffaf0;

  border: 1px solid #ead8aa;
  border-left: 4px solid #d4af37;

  border-radius: 10px;

  color: #604b1e;

  font-size: 14px;
  line-height: 1.6;
}

.message-error {
  background: #fff4f4;

  border-color: #e7b9b9;
  border-left-color: #c53030;

  color: #762626;
}

.message-icon {
  flex-shrink: 0;

  font-size: 17px;
}

/* =========================================================
   GUIDE
========================================================= */

.selection-guide {
  display: flex;
  align-items: center;
  gap: 13px;

  margin-bottom: 18px;
  padding: 15px 18px;

  background: linear-gradient(135deg, #fffaf0, #fffdf8);

  border: 1px solid #ead9af;
  border-radius: 12px;
}

.guide-icon {
  width: 40px;
  height: 40px;

  flex-shrink: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  background: #f8edc9;

  border-radius: 10px;

  font-size: 19px;
}

.guide-content {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.guide-content strong {
  color: #4a3a18;

  font-size: 14px;
}

.guide-content span {
  color: #837653;

  font-size: 12px;
  line-height: 1.5;
}

.guide-highlight {
  color: #a77b13;
}

/* =========================================================
   SUMMARY
========================================================= */

.summary-bar {
  display: flex;
  align-items: stretch;

  margin-bottom: 18px;

  background: #ffffff;

  border: 1px solid #e5dcc9;
  border-radius: 13px;

  overflow: hidden;
}

.summary-item {
  flex: 1;

  display: flex;
  flex-direction: column;
  justify-content: center;

  gap: 4px;

  padding: 13px 18px;
}

.summary-label {
  color: #8d877a;

  font-size: 11px;

  text-transform: uppercase;

  letter-spacing: 0.7px;
}

.summary-item strong {
  color: #473b23;

  font-size: 17px;
}

.summary-divider {
  width: 1px;

  margin: 10px 0;

  background: #e7dfcf;
}

.selected-summary strong {
  color: #ad7d13;
}

.selected-summary .not-selected {
  color: #999;

  font-size: 15px;
}

/* =========================================================
   SELECTED INFO
========================================================= */

.selected-info {
  margin-bottom: 22px;

  padding: 15px 17px;

  background: #fff9e7;

  border: 1px solid #e4cf8e;

  border-radius: 12px;
}

.selected-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;

  gap: 20px;

  padding: 5px 0;

  color: #6f6245;

  font-size: 13px;
}

.selected-info-row strong {
  color: #8e6915;

  font-size: 13px;

  text-align: right;
}

.selected-info-label {
  flex-shrink: 0;
}

.capacity-status {
  margin-top: 10px;
  padding-top: 10px;

  border-top: 1px solid #eadcb8;

  font-size: 12px;
  font-weight: 600;
}

.capacity-status.success {
  color: #31805a;
}

.capacity-status.warning {
  color: #ad6e13;
}

/* =========================================================
   SECTION TITLE
========================================================= */

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;

  margin-bottom: 13px;
}

.section-title-left {
  display: flex;
  align-items: center;
  gap: 9px;

  color: #393329;

  font-size: 15px;
  font-weight: 700;
}

.section-line {
  width: 4px;
  height: 19px;

  background: #c49a32;

  border-radius: 5px;
}

.section-count {
  padding: 5px 10px;

  background: #eee8db;

  border-radius: 20px;

  color: #746b5a;

  font-size: 11px;
  font-weight: 600;
}

/* =========================================================
   BAN GRID
========================================================= */

.ban-grid {
  display: grid;

  grid-template-columns: repeat(3, minmax(0, 1fr));

  gap: 14px;

  padding: 2px 2px 5px;
}

/* =========================================================
   BAN CARD
========================================================= */

.ban-card {
  position: relative;

  min-height: 230px;

  display: flex;
  flex-direction: column;
  align-items: stretch;

  padding: 17px;

  background: #ffffff;

  border: 1px solid #ded8cb;
  border-radius: 15px;

  color: #3e392f;

  cursor: pointer;

  text-align: left;

  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease,
    opacity 0.2s ease;
}

.ban-card:hover:not(:disabled) {
  transform: translateY(-3px);

  border-color: #d1a936;

  box-shadow: 0 10px 25px rgba(80, 62, 20, 0.13);
}

/* =========================================================
   LOCKED
========================================================= */

.ban-card.is-locked {
  opacity: 0.48;

  cursor: not-allowed;

  background: #f0eee9;

  border-color: #d7d3ca;

  transform: none !important;

  box-shadow: none !important;
}

.ban-card.is-locked .ban-icon {
  filter: grayscale(1);
}

/* =========================================================
   RECOMMENDED
========================================================= */

.ban-card.is-recommended {
  border-color: #d5b65b;

  background: linear-gradient(145deg, #fffdf7, #fff9e8);
}

/* =========================================================
   SELECTED
========================================================= */

.ban-card.is-selected {
  border: 2px solid #c2931e;

  background: linear-gradient(145deg, #fff9df, #fff4c9);

  box-shadow: 0 8px 25px rgba(184, 139, 25, 0.24);

  transform: translateY(-2px);
}

/*
 * Bàn đã chọn vẫn phải click được.
 */
.ban-card.is-selected:hover {
  cursor: pointer;

  transform: translateY(-3px);

  box-shadow: 0 12px 28px rgba(184, 139, 25, 0.3);
}

/* =========================================================
   NOT ENOUGH
========================================================= */

.ban-card.is-not-enough:not(.is-selected) {
  border-style: dashed;
}

/* =========================================================
   PRIORITY
========================================================= */

.priority-badge {
  position: absolute;

  top: 12px;
  right: 12px;

  padding: 5px 8px;

  background: #f5e4a9;

  border: 1px solid #dfc56d;
  border-radius: 20px;

  color: #806013;

  font-size: 10px;
  font-weight: 700;

  letter-spacing: 0.3px;
}

/* =========================================================
   STATUS
========================================================= */

.ban-status {
  min-height: 18px;

  display: flex;
  align-items: center;

  margin-bottom: 7px;

  font-size: 10px;
  font-weight: 700;
}

.status-available {
  color: #31805a;
}

.status-selected {
  color: #9a7211;
}

.status-locked {
  color: #898989;
}

/* =========================================================
   ICON
========================================================= */

.ban-icon {
  width: 46px;
  height: 46px;

  display: flex;
  align-items: center;
  justify-content: center;

  margin-bottom: 9px;

  background: #f2ecdc;

  border-radius: 12px;

  font-size: 22px;
}

.ban-card.is-selected .ban-icon {
  background: #ead697;
}

/* =========================================================
   NAME
========================================================= */

.ban-name {
  margin-bottom: 10px;

  color: #2f2a21;

  font-size: 18px;
  font-weight: 800;
}

/* =========================================================
   INFO
========================================================= */

.ban-info {
  display: flex;
  flex-direction: column;
  gap: 6px;

  padding-top: 10px;

  border-top: 1px solid #eee8dc;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 7px;

  color: #777063;

  font-size: 11px;
}

.info-row span:first-child {
  width: 17px;

  text-align: center;
}

/* =========================================================
   SELECTED CHECK
========================================================= */

.selected-check {
  position: absolute;

  right: 13px;
  bottom: 13px;

  width: 27px;
  height: 27px;

  display: flex;
  align-items: center;
  justify-content: center;

  background: #c2931e;

  border-radius: 50%;

  color: #fff;

  font-size: 15px;
  font-weight: 800;
}

/* =========================================================
   VALIDATION
========================================================= */

.validation-hint {
  display: flex;
  align-items: center;
  gap: 9px;

  margin-top: 17px;
  padding: 11px 14px;

  background: #fff8e7;

  border: 1px solid #ead49b;
  border-radius: 9px;

  color: #80672b;

  font-size: 12px;
}

.validation-hint.warning {
  background: #fff6e8;

  border-color: #e6c57c;

  color: #9a6918;
}

/* =========================================================
   SELECTED BANNER
========================================================= */

.selected-banner {
  display: flex;
  align-items: center;
  gap: 12px;

  margin-top: 17px;
  padding: 13px 15px;

  background: #f8f0d4;

  border: 1px solid #dec879;
  border-radius: 10px;
}

.selected-banner-icon {
  width: 35px;
  height: 35px;

  flex-shrink: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  background: #c2931e;

  border-radius: 50%;

  color: white;

  font-weight: 800;
}

.selected-banner-content {
  display: flex;
  flex-direction: column;
  gap: 2px;

  min-width: 0;
}

.selected-banner-content span {
  color: #8c7741;

  font-size: 11px;
}

.selected-banner-content strong {
  color: #634b12;

  font-size: 14px;

  line-height: 1.5;
}

.selected-banner-content small {
  margin-top: 2px;

  color: #9a8960;

  font-size: 10px;
}

/* =========================================================
   EMPTY STATE
========================================================= */

.empty-state {
  padding: 35px 20px;

  text-align: center;
}

.empty-icon {
  width: 70px;
  height: 70px;

  display: flex;
  align-items: center;
  justify-content: center;

  margin: 0 auto 15px;

  background: #eee5df;

  border-radius: 20px;

  font-size: 31px;
}

.empty-state h3 {
  margin: 0 0 7px;

  color: #4a3939;

  font-size: 20px;
}

.empty-state p {
  max-width: 500px;

  margin: 0 auto 20px;

  color: #877878;

  font-size: 13px;

  line-height: 1.6;
}

.empty-capacity {
  max-width: 350px;

  display: flex;
  justify-content: space-between;
  align-items: center;

  margin: auto;
  padding: 13px 16px;

  background: #fff;

  border: 1px solid #e3d7d0;
  border-radius: 10px;

  color: #82756e;

  font-size: 12px;
}

.empty-capacity strong {
  color: #7f4e4e;

  font-size: 15px;
}

/* =========================================================
   FOOTER
========================================================= */

.footer {
  flex-shrink: 0;

  display: flex;
  justify-content: flex-end;
  align-items: center;

  gap: 12px;

  padding: 18px 25px;

  background: #f1ede5;

  border-top: 1px solid #ded6c7;
}

/* =========================================================
   BUTTON
========================================================= */

.btn {
  min-width: 155px;

  height: 46px;

  padding: 0 20px;

  border-radius: 10px;

  font-size: 13px;
  font-weight: 700;

  cursor: pointer;

  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    opacity 0.2s ease;
}

.btn:disabled {
  opacity: 0.45;

  cursor: not-allowed;

  transform: none !important;

  box-shadow: none !important;
}

.btn-cancel {
  background: #272727;

  border: 1px solid #3d3d3d;

  color: #f0d47d;
}

.btn-cancel:hover {
  background: #171717;

  transform: translateY(-2px);

  box-shadow: 0 7px 18px rgba(0, 0, 0, 0.2);
}

.btn-confirm {
  background: linear-gradient(135deg, #b88921, #e7cb74, #c4972d);

  border: 1px solid rgba(255, 255, 255, 0.3);

  color: #332100;
}

.btn-confirm:not(:disabled):hover {
  transform: translateY(-2px);

  box-shadow: 0 10px 25px rgba(180, 137, 31, 0.35);
}

/* =========================================================
   SCROLLBAR
========================================================= */

.content::-webkit-scrollbar {
  width: 7px;
}

.content::-webkit-scrollbar-track {
  background: transparent;
}

.content::-webkit-scrollbar-thumb {
  background: #c7ad67;

  border-radius: 10px;
}

.content::-webkit-scrollbar-thumb:hover {
  background: #a7893e;
}

/* =========================================================
   ANIMATION
========================================================= */

@keyframes popup {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.97);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

/* =========================================================
   RESPONSIVE
========================================================= */

@media (max-width: 900px) {
  .ban-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .summary-bar {
    flex-wrap: wrap;
  }

  .summary-item {
    min-width: 30%;
  }
}

@media (max-width: 600px) {
  .overlay {
    padding: 10px;
  }

  .dialog {
    max-width: 100%;
    max-height: 96vh;

    border-radius: 16px;
  }

  .header {
    padding: 19px;
  }

  .content {
    padding: 18px;
  }

  .header h2 {
    font-size: 20px;
  }

  .header p {
    font-size: 11px;
  }

  .ban-grid {
    grid-template-columns: 1fr;
  }

  .summary-bar {
    flex-direction: column;
  }

  .summary-divider {
    width: auto;
    height: 1px;

    margin: 0 12px;
  }

  .selected-info-row {
    flex-direction: column;
    align-items: flex-start;

    gap: 3px;
  }

  .selected-info-row strong {
    text-align: left;
  }

  .footer {
    flex-direction: column-reverse;

    padding: 15px;
  }

  .btn {
    width: 100%;
  }
}
</style>
