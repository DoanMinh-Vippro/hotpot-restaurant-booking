<template>
  <div class="container">
    
    <div class="cot-trai">
      <ComboTable
        :danh-sach-combo="danhSachCombo"
        @edit="sua"
        @delete="xoa"
        @add="themMoi"
        @search="nhanSuKienTimKiem"
        @reset="lamMoiTimKiem"
        @view-detail="chuyenSangChiTiet"
      />

      <Pagination 
        :page-no="trangHienTai"
        :total-pages="tongSoTrang"
        @change-page="chuyenTrang"
      />
    </div>

    <div class="cot-phai">
      <ComboForm
        ref="formRef"
        :danh-sach-combo="danhSachCombo"
        @submit="luu"
      />
      <ComboPreview
        :combo-da-chon="comboDangChon"
      />
    </div>

  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ComBoApi from '../api/ComBoApi'

import ComboForm from '../components/ComBoForm.vue'
import ComboTable from '../components/ComBoTable.vue'
import ComboPreview from '../components/ComBoPreview.vue'
import Pagination from '../components/Pagination.vue'

import type { Combo, ComboRequest } from '../api/ComBoApi'

const router = useRouter()
const danhSachCombo = ref<Combo[]>([])
const comboDangChon = ref<Combo | undefined>(undefined)
const selectedId = ref<number | null>(null)
const formRef = ref()

const tuKhoaHienTai = ref('')
const trangHienTai = ref(0)
const kichThuocTrang = ref(5)
const tongSoTrang = ref(0)

const fetchDuLieu = async () => {
  try {
    const res = await ComBoApi.timKiemComBo({
      tenCombo: tuKhoaHienTai.value,
      pageNo: trangHienTai.value,
      pageSize: kichThuocTrang.value
    })
    
    const responseData = res.data as any
    
    if (responseData && responseData.content) {
      danhSachCombo.value = responseData.content
      tongSoTrang.value = responseData.totalPages || 0
    } else {
      danhSachCombo.value = Array.isArray(responseData) ? responseData : []
      tongSoTrang.value = 1
    }
  } catch (error) {
    console.error("Hệ thống lỗi khi nạp danh sách combo phân trang:", error)
  }
}

const chuyenSangChiTiet = (cb: Combo) => {
  router.push({
    name: 'CTCB',
    query: { idCombo: cb.idCombo.toString() }
  })
}

const nhanSuKienTimKiem = async (tuKhoa: string) => {
  tuKhoaHienTai.value = tuKhoa
  trangHienTai.value = 0
  await fetchDuLieu()
}

const chuyenTrang = async (trangMucTieu: number) => {
  trangHienTai.value = trangMucTieu
  await fetchDuLieu()
}

const lamMoiTimKiem = async () => {
  tuKhoaHienTai.value = ''
  trangHienTai.value = 0
  await fetchDuLieu()
}

onMounted(fetchDuLieu)

const themMoi = () => {
  comboDangChon.value = undefined
  selectedId.value = null
  formRef.value?.fillForm()
}

const sua = (cb: Combo) => {
  comboDangChon.value = cb
  selectedId.value = cb.idCombo || null
  formRef.value?.fillForm(cb)
}

const luu = async (payload: ComboRequest) => {
  const isUpdate = selectedId.value !== null
  const actionName = isUpdate ? 'cập nhật' : 'thêm mới'

  if (!confirm(`Bạn có chắc chắn muốn ${actionName} combo này không?`)) return

  try {
    let tenFileAnhCuoiCung = payload.hinhAnh;

    if (payload.fileThat) {
      const uploadRes = await ComBoApi.uploadImage(payload.fileThat);
      tenFileAnhCuoiCung = uploadRes.data; 
    }

    const dataGuiDi: ComboRequest = {
      tenCombo: payload.tenCombo,
      giaCombo: payload.giaCombo,
      hinhAnh: tenFileAnhCuoiCung,
      trangThai: payload.trangThai
    }

    if (isUpdate) {
      await ComBoApi.updateComBo(selectedId.value!, dataGuiDi)
    } else {
      await ComBoApi.addComBo(dataGuiDi)
    }

    alert(`${isUpdate ? 'Cập nhật' : 'Thêm mới'} combo thành công!`)
    themMoi()
    await fetchDuLieu()

  } catch (error: any) {
    const beErrorMsg = error.response?.data?.message || error.response?.data || `Có lỗi khi ${actionName}!`;
    alert(beErrorMsg);
  }
}

const xoa = async (id: number) => {
  try {
    await ComBoApi.deleteComBo(id)
    alert('Đã ngưng bán combo thành công!')

    if (selectedId.value === id) themMoi()
    await fetchDuLieu()
  } catch (error: any) {
    alert('Có lỗi xảy ra khi ngưng bán combo!');
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