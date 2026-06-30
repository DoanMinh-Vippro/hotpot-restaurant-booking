<script setup lang="ts">
import { onMounted, ref } from 'vue'
import BanApi from '@/api/BanApi'
import { getAllKhuVuc } from '@/api/khuvuc'
import BanTab from '@/components/BanTab.vue'
import BanList from '@/components/BanList.vue'
import BanDetail from '@/components/BanDetail.vue'
import AddBan from '@/components/AddBan.vue'

/**
 * 1. DATA CHÍNH
 */
const danhSachBan = ref<any[]>([])
const danhSachKhuVuc = ref<any[]>([])
const showPopupAdd = ref(false)

/**
 * 2. STATE UI
 */
const idKhuVucDangChon = ref<number | null>(null)
const banDangChon = ref<any | null>(null)
const showPopupCheck = ref(false)
/**
 * 3. LOAD BÀN
 */
const loadBan = async () => {
  const res = await BanApi.getAll()
  danhSachBan.value = res.data
}

/**
 * LOAD KHU VỰC
 */
const loadKhuVuc = async () => {
  const res = await getAllKhuVuc()
  danhSachKhuVuc.value = res.data
}

/**
 * 4. TAB CHANGE
 */
const handleChangeTab = (idKhuVuc: number) => {
  idKhuVucDangChon.value = idKhuVuc
  banDangChon.value = null // đổi tab thì reset selection
}

/**
 * 5. SELECT BÀN (click 1 lần)
 */
const handleSelectBan = (ban: any) => {
  banDangChon.value = ban
}

/**
 * 6. OPEN DETAIL (double click)
 */
const handleOpenDetail = (ban: any) => {
  banDangChon.value = ban
  showPopupCheck.value = true
}

// hàm xóa
const handleDeleteBan = async (idBan: number) => {
  const confirmDelete = confirm('Xóa bàn này?')

  if (!confirmDelete) return

  try {
    await BanApi.delete(idBan)

    await loadBan()
  } catch (error) {
    console.error(error)
    alert('Không thể xóa bàn')
  }
}

//hàm đóng popup
const closePopup = () => {
  showPopupCheck.value = false
}

//hàm mở popup AddBan
const openPopupAdd = () => {
  showPopupAdd.value = true
}
//hàm sau khi thêm thành công
const handleAddSuccess = async () => {
  showPopupAdd.value = false
  await loadBan()
}
//hàm đóng AddBan
const closePopupAdd = () => {
  showPopupAdd.value = false
}

onMounted(() => {
  loadBan()
  loadKhuVuc()
})
</script>

<template>
  <div class="ban-view">
    <!-- TAB KHU VỰC -->
    <BanTab :listKhuVuc="danhSachKhuVuc" @change="handleChangeTab" @add="openPopupAdd">
      <template #default="{ idKhuVuc }">
        <!-- LIST BÀN -->
        <BanList
          :danhSachBan="danhSachBan"
          :idKhuVuc="idKhuVuc"
          @select="handleSelectBan"
          @openDetail="handleOpenDetail"
          @delete="handleDeleteBan"
        />
      </template>
    </BanTab>
    <BanDetail
      v-if="showPopupCheck"
      :visible="showPopupCheck"
      :banSelected="banDangChon"
      :idKhuVuc="idKhuVucDangChon"
      @close="closePopup"
      @success="loadBan"
    />

    <AddBan v-if="showPopupAdd" @close="closePopupAdd" @success="handleAddSuccess" />
  </div>
</template>

<style scoped>
.ban-view {
  background: #0b0b0d;
  min-height: 100vh;
  padding: 12px;
}
</style>
