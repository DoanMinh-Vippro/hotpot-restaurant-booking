<template>
  <div class="kds-container">
    <h2>MÀN HÌNH HIỂN THỊ QUẦY BẾP</h2>
    <div class="ticket-list">
      <div v-for="(ticket, index) in danhSachPhieu" :key="index" class="ticket-card">
        <h3>PHIẾU BÁO CHẾ BIẾN</h3>
        <div class="quay">[{{ ticket.tenQuay }}]</div>
        <div class="info">Mã HD: {{ ticket.maHoaDon }}</div>
        <div class="info">Bàn: {{ ticket.tenBan }}</div>
        <div class="info">Nhân viên phục vụ: {{ ticket.tenNhanVien }}</div>
        <div class="info">Giờ đặt: {{ ticket.thoiGian }}</div>
        <hr />
        <table>
          <thead>
            <tr>
              <th>Tên món / Combo</th>
              <th class="sl">SL</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(mon, mIdx) in ticket.danhSachMon" :key="mIdx">
              <td>{{ mon.tenMon }}</td>
              <td class="sl">x{{ mon.soLuong }}</td>
            </tr>
          </tbody>
        </table>
        <hr />
        <div class="footer">Vui lòng chế biến/pha chế theo thứ tự!</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

const danhSachPhieu = ref([])

onMounted(() => {
  const socket = new SockJS('http://localhost:8080/ws-print')
  const stompClient = Stomp.over(socket)

  stompClient.connect({}, () => {
    console.log('✅ Đã kết nối Màn Hình Bếp!')
    // Lắng nghe kênh của BẾP
    stompClient.subscribe('/topic/bep', (message) => {
      const data = JSON.parse(message.body)
      danhSachPhieu.value.unshift(data) // Đơn mới nổi lên đầu
    })
  })
})
</script>

<style scoped>
.kds-container {
  padding: 20px;
  background: #eef2f5;
  min-height: 100vh;
  font-family: Arial, sans-serif;
}
.ticket-list {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}
.ticket-card {
  width: 320px;
  background: #fff;
  padding: 18px;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-top: 6px solid #d32f2f;
  color: #000;
  line-height: 1.4;
}
.ticket-card h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: bold;
}
.quay {
  font-weight: bold;
  margin-bottom: 12px;
}
.info {
  margin-bottom: 6px;
  font-size: 14px;
}
hr {
  border: none;
  border-top: 1px dashed #333;
  margin: 10px 0;
}
table {
  width: 100%;
  border-collapse: collapse;
}
th,
td {
  text-align: left;
  padding: 4px 0;
  font-size: 14px;
}
th.sl,
td.sl {
  text-align: right;
  font-weight: bold;
}
.footer {
  margin-top: 10px;
  font-size: 13px;
}
</style>