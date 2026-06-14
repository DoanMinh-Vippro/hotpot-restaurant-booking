<script setup lang="ts">
import BanApi from '@/api/BanApi'
import DatBanQuanLyApi from '@/api/DatBanQuanLy'
import DatBanQuanLyForm from '@/components/DatBanQuanLyForm.vue'
import DatBanQuanLyTable from '@/components/DatBanQuanLyTable.vue'
import { onMounted, ref } from 'vue'

const list = ref([]) //bien truyen xuong table
const form = ref<any>({}) // Khai báo là ref chứa object
const listBan = ref([]) //biến lấy danh sách bàn để load vào select

const loadData = async () => {
  const response = await DatBanQuanLyApi.getAll()
  const resBan = await BanApi.getAll()
  list.value = response.data
  listBan.value = resBan.data
}

const detail = async (datBanQuanLy: any) => {
  try {
    // Gọi API lấy chi tiết bằng ID
    const response = await DatBanQuanLyApi.findById(datBanQuanLy.idDatBan)

    // Gán dữ liệu thật từ API trả về vào form
    form.value = { ...response.data }

    // Ép kiểu
    form.value.idBan = Number(form.value.idBan)
    form.value.idkhachHang = Number(form.value.idKhachHang)

    console.log('Dữ liệu chi tiết từ API:', form.value)
  } catch (error) {
    console.error('Lỗi khi lấy chi tiết:', error)
  }
}

const deleteById = async (id: number) => {
  if (confirm('ban co chac chan muon xoa?')) {
    await DatBanQuanLyApi.delete(id)
    loadData()
  }
}

onMounted(loadData)
</script>

<template>
  <DatBanQuanLyForm :datBanQuanLy="form" :listBan="listBan" @refresh="loadData"></DatBanQuanLyForm>
  <DatBanQuanLyTable :list="list" @detail="detail" @delete="deleteById"></DatBanQuanLyTable>
</template>
