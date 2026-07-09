<script setup lang="ts">
import { onMounted, ref } from 'vue'

import BanApi from '@/api/BanApi'
import { getAllKhuVuc } from '@/api/khuvuc'
import PopupListDatBan from '@/components/PopupListDatBan.vue'
import DatBanQLTab from '@/components/DatBanQLTab.vue'
import DatBanQLListBan from '@/components/DatBanQLListBan.vue'
import DatBanPopupCheck from '@/components/DatBanPopupCheck.vue'
import ThanhToan from '@/components/ThanhToan.vue'
import HoaDonApi from '@/api/HoaDonApi'
import DatBanQuanLyApi from '@/api/DatBanQuanLy'

// chuyển màn hình từ quản lý bàn sang màn thanh toán của bàn được chọn
const manHinhHienTai = ref('danhSachBan')
const moManHinhthanhToan = async () => {
  showPopup.value = false
  if (banDangChon.value) {
    await markBanDangSuDung(banDangChon.value)
  }
  manHinhHienTai.value = 'thanhToan'
}

/**
 * biến theo dõi để mở popup đặt bàn list (trạng thái đã xác nhận)
 */
const showPopupDaXacNhan = ref(false)

/**
 * DANH SÁCH BÀN
 */
const danhSachBan = ref<any[]>([])

/**
 * DANH SÁCH KHU VỰC
 *
 * Tạm thời hard code
 * Sau này gọi API
 */
const danhSachKhuVuc = ref<any[]>([])

/**
 * KHU VỰC ĐANG CHỌN
 */
const idKhuVucDangChon = ref<number | null>(null)

/**
 * BÀN ĐANG CHỌN
 */
const banDangChon = ref<any | null>(null)

const datBanDangChon = ref<any | null>(null)
/**
 * HIỂN THỊ POPUP
 */
const showPopup = ref(false)

/**
 * LOAD DANH SÁCH BÀN
 */
const loadBan = async () => {
  const [banRes, reservationRes] = await Promise.all([BanApi.getAll(), DatBanQuanLyApi.getAll()])
  const rawBan = Array.isArray(banRes?.data) ? banRes.data : []
  const reservations = Array.isArray(reservationRes?.data) ? reservationRes.data : []

  const reservationByBanId = new Map<number, any>()
  reservations.forEach((reservation: any) => {
    if (!reservation?.idBan) return
    const banId = Number(reservation.idBan)
    const existing = reservationByBanId.get(banId)
    const currentTime = reservation?.thoiGianDenDuKien ? new Date(reservation.thoiGianDenDuKien).getTime() : (reservation?.ngayDat ? new Date(reservation.ngayDat).getTime() : 0)
    const existingTime = existing?.thoiGianDenDuKien ? new Date(existing.thoiGianDenDuKien).getTime() : (existing?.ngayDat ? new Date(existing.ngayDat).getTime() : 0)
    if (!existing || currentTime > existingTime) {
      reservationByBanId.set(banId, reservation)
    }
  })

  danhSachBan.value = rawBan.map((ban: any) => {
    const reservation = reservationByBanId.get(Number(ban.idBan))
    let nextStatus = ban.trangThai

    if (reservation?.trangThai === 'DA_XAC_NHAN') {
      nextStatus = 'DA_DAT'
    } else if (reservation?.trangThai === 'DA_NHAN_BAN') {
      nextStatus = 'DANG_SU_DUNG'
    } else if (['HOAN_THANH', 'DA_HUY'].includes(reservation?.trangThai)) {
      nextStatus = 'TRONG'
    }

    return { ...ban, trangThai: nextStatus }
  })
}

/**
 * LOAD KHU VỰC
 */
const loadKhuVuc = async () => {
  const re = await getAllKhuVuc()
  danhSachKhuVuc.value = re.data
}

/**
 * CHUYỂN TAB KHU VỰC
 */
const handleChangeTab = (idKhuVuc: number) => {
  idKhuVucDangChon.value = idKhuVuc
}

/**
 * CLICK 1 LẦN VÀO BÀN
 */
const handleSelectBan = (ban: any) => {
  banDangChon.value = ban
}

/**
 * DOUBLE CLICK VÀO BÀN
 *
 * => mở popup kiểm tra đặt bàn
 */
const moPopupDatBan = async (ban: any) => {
  banDangChon.value = ban

  try {
    // 0 là hóa đơn đang hoạt động
    const res = await HoaDonApi.findByBanAndStatus(ban.idBan, 0)

    if (res.data) {
      datBanDangChon.value = null
      manHinhHienTai.value = 'thanhToan'
      return
    }
  } catch (error) {
    // Không có hóa đơn -> hiện popup kiểm tra đặt bàn
  }

  showPopup.value = true
}

/**
 * ĐÓNG POPUP
 */
const dongPopup = () => {
  showPopup.value = false
}

const markBanDangSuDung = async (ban: any) => {
  if (!ban?.idBan) return

  try {
    await BanApi.update(ban.idBan, { trangThai: 'DANG_SU_DUNG' })
    const targetIndex = danhSachBan.value.findIndex((item: any) => Number(item.idBan) === Number(ban.idBan))
    if (targetIndex >= 0) {
      danhSachBan.value[targetIndex] = { ...danhSachBan.value[targetIndex], trangThai: 'DANG_SU_DUNG' }
    }
    banDangChon.value = { ...ban, trangThai: 'DANG_SU_DUNG' }
    // Reload bàn after update to ensure UI reflects changes
    await loadBan()
  } catch (error) {
    console.warn('Không thể cập nhật trạng thái bàn:', error)
  }
}

//
const moDanhSachDatBan = () => {
  // đóng popup hỏi

  showPopup.value = false

  // mở popup danh sách

  showPopupDaXacNhan.value = true
}

/////////////
const chonDatBan = (datBan: any) => {
  datBanDangChon.value = datBan

  showPopupDaXacNhan.value = false

  manHinhHienTai.value = 'thanhToan'
}

////
const quayVeDanhSachBan = async () => {
  manHinhHienTai.value = 'danhSachBan'

  await loadBan()
}

onMounted(async () => {
  await loadBan()
  await loadKhuVuc()
})
</script>

<template>
  <div class="ban-hang-view">
    <template v-if="manHinhHienTai === 'danhSachBan'">
      <!-- TAB KHU VỰC -->
      <DatBanQLTab :listKhuVuc="danhSachKhuVuc" @change="handleChangeTab">
        <!-- NỘI DUNG TAB -->
        <template #default="{ idKhuVuc }">
          <DatBanQLListBan
            :danhSachBan="danhSachBan"
            :idKhuVuc="idKhuVuc"
            @select="handleSelectBan"
            @openDetail="moPopupDatBan"
          />
        </template>
      </DatBanQLTab>

      <!-- POPUP KIỂM TRA ĐẶT BÀN -->
      <DatBanPopupCheck
        v-if="showPopup"
        :ban="banDangChon"
        @close="dongPopup"
        @coDonDatBan="moDanhSachDatBan"
        @khongCoDonDatBan="moManHinhthanhToan"
      />

      <PopupListDatBan
        v-if="showPopupDaXacNhan"
        @close="showPopupDaXacNhan = false"
        @chonDatBan="chonDatBan"
      />
    </template>

    <ThanhToan
      v-if="manHinhHienTai === 'thanhToan'"
      :ban="banDangChon"
      :datBan="datBanDangChon"
      @quayLai="quayVeDanhSachBan"
    />
  </div>
</template>

<style scoped>
.ban-hang-view {
  height: 100vh;
  background: linear-gradient(135deg, #f9efe0 0%, #f4e4c6 100%);
  padding: 12px;
  box-sizing: border-box;
  overflow: hidden;
  color: #5f3d22;
}
</style>
