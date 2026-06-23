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

// chuyển màn hình từ quản lý bàn sang màn thanh toán của bàn được chọn
const manHinhHienTai = ref('danhSachBan')
const moManHinhthanhToan = () => {
  showPopup.value = false
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

/**
 * HIỂN THỊ POPUP
 */
const showPopup = ref(false)

/**
 * LOAD DANH SÁCH BÀN
 */
const loadBan = async () => {
  const res = await BanApi.getAll()

  danhSachBan.value = res.data
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

  showPopup.value = true
}

/**
 * ĐÓNG POPUP
 */
const dongPopup = () => {
  showPopup.value = false
}

//
const moDanhSachDatBan = () => {
  // đóng popup hỏi

  showPopup.value = false

  // mở popup danh sách

  showPopupDaXacNhan.value = true
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

      <PopupListDatBan v-if="showPopupDaXacNhan" @close="showPopupDaXacNhan = false" />
    </template>

    <ThanhToan
      v-if="manHinhHienTai === 'thanhToan'"
      :ban="banDangChon"
      @quayLai="quayVeDanhSachBan"
    />
  </div>
</template>

<style scoped>
.ban-hang-view {
  height: 100vh;

  background: #0b0b0d;

  padding: 12px;

  box-sizing: border-box;

  overflow: hidden;
}
</style>
