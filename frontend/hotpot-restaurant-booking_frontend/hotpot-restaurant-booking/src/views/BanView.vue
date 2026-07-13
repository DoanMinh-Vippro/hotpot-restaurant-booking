<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BanApi from '@/api/BanApi'
import { getAllKhuVuc } from '@/api/khuvuc'
import BanTab from '@/components/BanTab.vue'
import BanList from '@/components/BanList.vue'
import BanDetail from '@/components/BanDetail.vue'
import AddBan from '@/components/AddBan.vue'

const router = useRouter()

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
  min-height: 100vh;

  padding: 18px;

  background: linear-gradient(135deg, #fffdf8 0%, #f8f1e5 100%);

  color: #6f5a3a;
}

.page-top {
  margin-bottom: 18px;

  display: flex;

  align-items: center;
}

/* BUTTON QUAY LẠI */
.back-home-btn {
  display: inline-flex;

  align-items: center;

  gap: 8px;

  padding: 10px 18px;

  border-radius: 999px;

  border: 1px solid #e8d8bd;

  background: #fffaf2;

  color: #8a6a3f;

  font-size: 14px;

  font-weight: 700;

  cursor: pointer;

  transition: all 0.25s ease;

  box-shadow: 0 4px 12px rgba(160, 130, 80, 0.08);
}

.back-home-btn:hover {
  background: #f3dfb8;

  color: #6b5130;

  border-color: #dfc18a;

  transform: translateY(-2px);

  box-shadow: 0 8px 18px rgba(160, 130, 80, 0.15);
}

.back-home-btn:active {
  transform: scale(0.97);
}
</style>
