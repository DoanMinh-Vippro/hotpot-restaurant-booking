<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import HoaDonApi from '@/api/HoaDonApi'
import BanApi from '@/api/BanApi'
import DatBanQuanLyApi from '@/api/DatBanQuanLy'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBooking = () => {
  router.push('/dat-ban')
}

/**
 * Sau khi VNPay redirect về trang thành công, backend đã cập nhật trạng thái hóa đơn.
 * Ở đây cố gắng tìm hóa đơn đã thanh toán gần nhất có idBan và cập nhật trạng thái bàn về 'TRONG'.
 * Nếu hóa đơn liên quan đến đơn đặt bàn (idDatBan) thì gọi API hoàn thành đơn đặt bàn.
 */
const markTablesEmptyAfterPayment = async () => {
  try {
    const res = await HoaDonApi.getDanhSach()
    const bills = res.data || []

    // Lọc các hóa đơn đã thanh toán và có idBan
    const paidWithBan = bills.filter((b: any) => Number(b.trangThaiThanhToan) === 1 && b.idBan)

    if (!paidWithBan.length) return

    // Lấy hóa đơn gần nhất (idHoaDon lớn nhất)
    paidWithBan.sort((a: any, b: any) => (b.idHoaDon || 0) - (a.idHoaDon || 0))
    const latest = paidWithBan[0]

    const banId = latest.idBan
    const datBanId = latest.idDatBan

    if (banId) {
      try {
        await BanApi.update(banId, {
          loaiBan: latest.loaiBan ?? null,
          tenBan: latest.tenBan ?? null,
          idKhuVuc: latest.idKhuVuc ?? null,
          trangThai: 'TRONG',
        })
      } catch (updateErr) {
        console.warn('Không thể cập nhật trạng thái bàn:', updateErr)
      }
    }

    if (datBanId) {
        try {
          await DatBanQuanLyApi.hoanThanh(datBanId)
        } catch (resErr) {
          console.warn('Không thể hoàn thành đơn đặt bàn liên quan:', resErr)
        }
      }

      // Notify opener window (if payment opened a new window) so POS can react immediately
      try {
        if (window && (window.opener as any) && !(window.opener as any).closed) {
          ;(window.opener as any).postMessage(
            {
              type: 'payment-complete',
              idBan: banId,
              billId: latest?.idHoaDon ?? null,
              trangThai: 'TRONG',
            },
            '*',
          )
        }
      } catch (postErr) {
        console.warn('Không thể postMessage tới cửa sổ cha:', postErr)
      }

      // Redirect current tab to Ban Hàng with query so that BanHang's openPendingTarget can handle it
      try {
        void router.push({
          name: 'ban-hang',
          query: {
            pendingTableId: String(banId || ''),
            pendingBillId: String(latest?.idHoaDon ?? ''),
            pendingDatBanId: String(datBanId || ''),
          },
        })
      } catch (navErr) {
        // ignore
      }
    }
  } catch (err) {
    console.warn('Lỗi khi kiểm tra hóa đơn sau khi thanh toán:', err)
  }
}

onMounted(() => {
  // Thực hiện cập nhật không chặn UI
  void markTablesEmptyAfterPayment()
})
</script>

<template>
  <div class="payment-page">
    <div class="payment-card">
      <div class="success-icon">✓</div>

      <h1>Thanh toán thành công</h1>

      <p class="message">
        Đơn đặt bàn của bạn đã được ghi nhận. Nhà hàng sẽ xác nhận và chuẩn bị bàn cho bạn.
      </p>

      <div class="info-box">
        <p>
          <strong>Trạng thái:</strong>
          <span> Đã thanh toán</span>
        </p>

        <p>
          <strong>Thanh toán:</strong>
          <span> VNPay / Chuyển khoản</span>
        </p>
      </div>

      <div class="actions">
        <button class="btn-home" @click="goHome">Về trang chủ</button>

        <button class="btn-booking" @click="goBooking">Đặt bàn mới</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.payment-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;

  background: #f5f5f5;
}

.payment-card {
  width: 420px;

  background: white;

  padding: 40px;

  border-radius: 16px;

  text-align: center;

  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);
}

.success-icon {
  width: 80px;
  height: 80px;

  margin: 0 auto 20px;

  border-radius: 50%;

  background: #22c55e;

  color: white;

  font-size: 45px;

  display: flex;
  justify-content: center;
  align-items: center;
}

h1 {
  margin-bottom: 15px;

  color: #16a34a;
}

.message {
  color: #555;

  line-height: 1.6;
}

.info-box {
  margin-top: 25px;

  padding: 15px;

  background: #f8fafc;

  border-radius: 10px;

  text-align: left;
}

.info-box p {
  margin: 8px 0;
}

.actions {
  display: flex;

  gap: 15px;

  margin-top: 30px;

  justify-content: center;
}

button {
  padding: 12px 22px;

  border: none;

  border-radius: 8px;

  cursor: pointer;

  font-size: 14px;
}

.btn-home {
  background: #333;

  color: white;
}

.btn-booking {
  background: #ef4444;

  color: white;
}

button:hover {
  opacity: 0.85;
}
</style>
