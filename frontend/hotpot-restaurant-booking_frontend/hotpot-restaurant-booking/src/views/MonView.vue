<template>
  <div class="container">
    
    <div class="cot-trai">
      <MonTable
        :danh-sach-mon="danhSachMon"
        :loading="loading"
        :selected-id="selectedId"
        :danh-sach-danh-muc="danhSachDanhMuc"
        @edit="suaMon"
        @delete="xoaMon"
        @add="themMoi"
        @search="nhanSuKienTimKiem"
        @reset="lamMoiTimKiem"
        @go-to-category="chuyenSangDanhMuc"
      />

      <Pagination 
        :page-no="trangHienTai"
        :total-pages="tongSoTrang"
        @change-page="chuyenTrang"
      />
    </div>

    <div class="cot-phai">
      <MonForm
        ref="formRef"
        :danh-sach-danh-muc="danhSachDanhMuc"
        :danh-sach-mon="danhSachMon"
        @submit="luuMon"
      />

      <MonPreview
        :mon-da-chon="monDangChon"
      />
    </div>

  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import MonApi from '../api/MonApi'
import DanhMucApi from '../api/DanhMucApi'

import MonForm from '../components/MonForm.vue'
import MonTable from '../components/MonTable.vue'
import MonPreview from '../components/MonPreview.vue'
import Pagination from '../components/Pagination.vue' 

import type { Mon, MonRequest } from '../api/MonApi'
import type { DanhMuc } from '../api/DanhMucApi'

const router = useRouter()
const danhSachMon = ref<Mon[]>([])
const danhSachDanhMuc = ref<DanhMuc[]>([])

const monDangChon = ref<Mon>()
const selectedId = ref<number | null>(null)
const loading = ref(false)
const formRef = ref()

const bieuThucTenMon = ref('')
const bieuThucLoaiDanhMuc = ref('')
const trangHienTai = ref(0)
const kichThuocTrang = ref(5) 
const tongSoTrang = ref(0)

const fetchDuLieu = async () => {
  loading.value = true
  try {
    const res = await MonApi.searchMon(
      bieuThucTenMon.value,
      undefined, 
      undefined, 
      bieuThucLoaiDanhMuc.value,
      trangHienTai.value,
      kichThuocTrang.value
    )
    
    const responseData = res.data as any
    if (responseData && responseData.content) {
      danhSachMon.value = responseData.content
      tongSoTrang.value = responseData.totalPages || 0
    } else {
      danhSachMon.value = Array.isArray(responseData) ? responseData : []
      tongSoTrang.value = 1
    }
  } catch (error) {
    console.error("Hệ thống lỗi khi nạp danh sách món ăn phân trang:", error)
  } finally {
    loading.value = false
  }
}

const loadDanhMuc = async () => {
  try {
    const res = await DanhMucApi.getDanhSach()
    danhSachDanhMuc.value = Array.isArray(res.data) ? res.data : (res.data as any).content || []
  } catch (error) {
    console.error("Lỗi khi tải danh mục cấu hình:", error)
  }
}

// Chuyển sang route quản lý danh mục (Khớp name: 'danhMuc' trong router/index.ts)
const chuyenSangDanhMuc = () => {
  router.push({ name: 'danhMuc' })
}

const nhanSuKienTimKiem = async (boLoc: { tenMon: string, loaiDanhMuc: string }) => {
  bieuThucTenMon.value = boLoc.tenMon
  bieuThucLoaiDanhMuc.value = boLoc.loaiDanhMuc
  trangHienTai.value = 0
  await fetchDuLieu()
}

const chuyenTrang = async (trangMucTieu: number) => {
  trangHienTai.value = trangMucTieu
  await fetchDuLieu()
}

const lamMoiTimKiem = async () => {
  bieuThucTenMon.value = ''
  bieuThucLoaiDanhMuc.value = ''
  trangHienTai.value = 0
  await fetchDuLieu()
}

onMounted(async () => {
  await fetchDuLieu()
  await loadDanhMuc()
})

const themMoi = () => {
  monDangChon.value = undefined
  selectedId.value = null
  formRef.value?.fillForm()
}

const suaMon = (mon: Mon) => {
  monDangChon.value = mon
  selectedId.value = mon.idMon
  formRef.value?.fillForm(mon)
}

const luuMon = async (payload: MonRequest) => {
  const isUpdate = selectedId.value !== null
  const actionName = isUpdate ? 'cập nhật' : 'thêm mới'

  if (!confirm(`Bạn có chắc chắn muốn ${actionName} món này không?`)) return

  try {
    if (isUpdate) {
      await MonApi.updateMon(selectedId.value!, payload)
    } else {
      await MonApi.addMon(payload)
    }

    themMoi()
    await fetchDuLieu()
    alert(`${isUpdate ? 'Cập nhật' : 'Thêm mới'} món thành công!`)
    
  } catch (error: any) {
    const errorMsg = error.response?.data?.message || error.response?.data || `Có lỗi xảy ra khi ${actionName} món!`;
    alert(errorMsg); 
  }
}

const xoaMon = async (idMon: number) => {
  if (!confirm('Bạn có chắc chắn muốn ngưng bán món này không?')) return

  try {
    await MonApi.deleteMon(idMon)

    if (selectedId.value === idMon) themMoi()
    await fetchDuLieu()
    
    alert('Đã ngưng bán món thành công!')
  } catch (error: any) {
    alert('Có lỗi xảy ra khi ngưng bán món!');
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  padding: 120px 32px 32px;
  background: #0f0f0f;
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 24px;
  align-items: start;
}

.cot-trai, .cot-phai {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

@media (max-width: 1200px) {
  .container {
    grid-template-columns: 1fr;
  }
}
</style>