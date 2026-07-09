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
        <div class="header">
          <h2>Đề xuất bàn</h2>
        </div>

        <div class="content">
          <p class="message">
            {{ result.message }}
          </p>

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

          <div class="capacity">
            Tổng sức chứa: <strong>{{ result.tongSucChua }}</strong>
          </div>
        </div>

        <div class="footer">
          <button class="btn btn-cancel" @click="emit('cancel')">Không đồng ý</button>

          <button class="btn btn-confirm" @click="emit('confirm')">Đồng ý</button>
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
  z-index: 9999;
}

.dialog {
  width: 600px;
  max-width: 95%;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 35px rgba(0, 0, 0, 0.2);
}

.header {
  padding: 18px 24px;
  border-bottom: 1px solid #ececec;
}

.header h2 {
  margin: 0;
  font-size: 22px;
}

.content {
  padding: 20px 24px;
}

.message {
  margin-bottom: 18px;
  line-height: 1.6;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  border: 1px solid #e5e5e5;
  padding: 10px;
  text-align: center;
}

.table th {
  background: #f7f7f7;
}

.capacity {
  margin-top: 18px;
  font-size: 15px;
}

.footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 18px 24px;
  border-top: 1px solid #ececec;
}

.btn {
  border: none;
  padding: 10px 22px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel {
  background: #e5e7eb;
}

.btn-confirm {
  background: #16a34a;
  color: white;
}

.btn-confirm:hover {
  background: #15803d;
}
</style>
