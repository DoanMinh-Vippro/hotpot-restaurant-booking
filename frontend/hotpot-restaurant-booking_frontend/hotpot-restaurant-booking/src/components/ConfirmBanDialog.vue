<script setup lang="ts">
interface Ban {
  idBan: number
  tenBan: string
  tenKhuVuc: string
  loaiBan: string
}

interface CheckBanResult {
  trangThai: string
  message: string
  canGhep: boolean
  tongSucChua: number
  dsBan: Ban[]
}

defineProps<{
  show: boolean
  result: CheckBanResult | null
}>()

const emit = defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()
</script>

<template>
  <Teleport to="body">
    <div v-if="show && result" class="overlay">
      <div class="dialog">
        <!-- HEADER (Đứng yên) -->
        <div class="header" :class="result.trangThai === 'KHONG_CO_BAN' ? 'error' : 'success'">
          <h2>
            {{ result.trangThai === 'KHONG_CO_BAN' ? 'Không tìm thấy bàn' : 'Đề xuất bàn' }}
          </h2>
        </div>

        <!-- CONTENT -->
        <div class="content">
          <p class="message">
            {{ result.message }}
          </p>

          <template v-if="result.trangThai !== 'KHONG_CO_BAN'">
            <!-- TABLE CONTAINER (Phần có scroll và giữ cố định header của bảng) -->
            <div class="table-container">
              <table class="table">
                <thead>
                  <tr>
                    <th>Bàn</th>
                    <th>Khu vực</th>
                    <th>Loại bàn</th>
                  </tr>
                </thead>

                <tbody>
                  <tr v-for="ban in result.dsBan" :key="ban.idBan">
                    <td>{{ ban.tenBan }}</td>
                    <td>{{ ban.tenKhuVuc }}</td>
                    <td>{{ ban.loaiBan }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- CAPACITY (Đứng yên) -->
            <div class="capacity">
              Tổng sức chứa:
              <strong>{{ result.tongSucChua }}</strong>
            </div>
          </template>
        </div>

        <!-- FOOTER (Đứng yên) -->
        <div class="footer">
          <template v-if="result.trangThai === 'KHONG_CO_BAN'">
            <button class="btn btn-confirm" @click="emit('cancel')">Đã hiểu</button>
          </template>

          <template v-else>
            <button class="btn btn-cancel" @click="emit('cancel')">Hủy Đặt</button>

            <button class="btn btn-confirm" @click="emit('confirm')">Đồng ý</button>
          </template>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
/* ================= OVERLAY ================= */

.overlay {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(8, 8, 8, 0.72);
  backdrop-filter: blur(10px);
  animation: fadeIn 0.3s ease;
  z-index: 9999;
}

/* ================= DIALOG ================= */

.dialog {
  width: 760px;
  max-width: 94%;
  max-height: 90vh; /* Giới hạn chiều cao popup */
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 22px;
  background: linear-gradient(180deg, #fffefb 0%, #faf6ef 100%);
  border: 1px solid rgba(212, 175, 55, 0.35);
  box-shadow:
    0 35px 80px rgba(0, 0, 0, 0.45),
    inset 0 1px rgba(255, 255, 255, 0.7);
  animation: popup 0.3s ease;
}

/* ================= HEADER (Đứng yên) ================= */

.header {
  position: relative;
  padding: 26px 34px;
  overflow: hidden;
  flex-shrink: 0;
}

.header::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #111, #252525);
}

.header::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, #7d5b11, #f7d777, #7d5b11);
}

.header.error::before {
  background: linear-gradient(135deg, #4d1111, #7f1d1d);
}

.header h2 {
  position: relative;
  z-index: 2;
  margin: 0;
  color: #f7df95;
  font-size: 27px;
  font-weight: 700;
  letter-spacing: 1px;
}

/* ================= CONTENT ================= */

.content {
  padding: 30px;
  overflow-y: auto; /* Cho phép cuộn phần nội dung bên trong */
  flex-grow: 1;
}

.message {
  background: #fffcf5;
  border: 1px solid #e8d8ac;
  border-left: 6px solid #d4af37;
  border-radius: 14px;
  padding: 18px 22px;
  line-height: 1.8;
  color: #5b4820;
  font-size: 15px;
  margin-bottom: 24px;
}

/* ================= TABLE CONTAINER (Phần scroll bảng) ================= */

.table-container {
  max-height: 250px; /* Chiều cao tối đa của vùng chứa bảng */
  overflow-y: auto;
  border-radius: 16px;
  border: 1px solid #e6d6ac;
}

.table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  background: white;
}

.table thead {
  position: sticky;
  top: 0;
  z-index: 2;
  background: linear-gradient(180deg, #2c2c2c, #181818);
}

.table th {
  color: #f7d777;
  padding: 16px;
  font-weight: 600;
  font-size: 15px;
  letter-spacing: 0.5px;
}

.table td {
  padding: 16px;
  text-align: center;
  color: #4d4d4d;
  border-top: 1px solid #f1ead9;
}

.table tbody tr {
  transition: 0.25s;
}

.table tbody tr:nth-child(odd) {
  background: #fffdfa;
}

.table tbody tr:nth-child(even) {
  background: #fbf7ef;
}

.table tbody tr:hover {
  background: #fff4d8;
  transform: scale(1.005);
}

/* ================= CAPACITY (Đứng yên) ================= */

.capacity {
  margin-top: 24px;
  padding: 18px 22px;
  border-radius: 14px;
  background: linear-gradient(90deg, #fff9ea, #fffdf7);
  border: 1px solid #ead8a4;
  color: #6b531c;
  font-size: 15px;
  flex-shrink: 0;
}

.capacity strong {
  color: #b8860b;
  font-size: 22px;
  margin-left: 6px;
}

/* ================= FOOTER (Đứng yên) ================= */

.footer {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  padding: 24px 30px;
  background: #f8f5ef;
  border-top: 1px solid #ebe0c4;
  flex-shrink: 0;
}

/* ================= BUTTON ================= */

.btn {
  min-width: 155px;
  padding: 13px 22px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  transition: 0.25s;
}

/* Nút phụ */

.btn-cancel {
  background: #222;
  color: #f6d878;
  border: 1px solid #3d3d3d;
}

.btn-cancel:hover {
  background: #111;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.25);
  transform: translateY(-2px);
}

/* Nút chính */

.btn-confirm {
  background: linear-gradient(135deg, #b88921, #e7cb74, #c4972d);
  color: #332100;
  border: 1px solid rgba(255, 255, 255, 0.25);
  position: relative;
  overflow: hidden;
}

.btn-confirm::before {
  content: '';
  position: absolute;
  top: 0;
  left: -120%;
  width: 60%;
  height: 100%;
  background: rgba(255, 255, 255, 0.45);
  transform: skewX(-25deg);
}

.btn-confirm:hover::before {
  left: 150%;
  transition: 0.7s;
}

.btn-confirm:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(212, 175, 55, 0.45);
}

/* ================= SCROLL ================= */

::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-thumb {
  background: #c6a857;
  border-radius: 10px;
}

/* ================= ANIMATION ================= */

@keyframes popup {
  from {
    opacity: 0;
    transform: translateY(-25px) scale(0.93);
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
</style>
