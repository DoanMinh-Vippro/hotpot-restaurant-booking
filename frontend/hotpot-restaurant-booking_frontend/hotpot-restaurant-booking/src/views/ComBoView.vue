<template>
  <div class="trang-quan-ly-combo">
    <div class="cot-danh-sach">
      <ComboTable
        :danhSachCombo="danhSachCombo"
        :loading="loading"
        :selectedId="selectedId"
        @edit="chonComboXemPreview"
        @delete="xuLyXoaCombo"
        @add="chuyenSangThemMoi"
        @search="xuLyTimKiem"
        @reset="xuLyLamMoi"
        @view-detail="chuyenSangChiTiet"
      />

      <!-- 📄 COMPONENT PHÂN TRANG GIONG VIEW MON -->
      <Pagination 
        :page-no="trangHienTai"
        :total-pages="tongSoTrang"
        @change-page="chuyenTrang"
      />
    </div>

    <div class="cot-bieu-mau">
      <ComboForm
        ref="formRef"
        :danhSachCombo="danhSachCombo"
        @submit="xuLySubmitForm"
      />

      <ComboPreview :comboDaChon="comboDaChon" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ComBoApi, { type Combo, type ComboRequest } from '../api/ComBoApi'

import ComboForm from '../components/ComBoForm.vue'
import ComboTable from '../components/ComBoTable.vue'
import ComboPreview from '../components/ComBoPreview.vue'
import Pagination from '../components/Pagination.vue'

const router = useRouter()

// Quản lý trạng thái và danh sách dữ liệu
const loading = ref(false)
const danhSachCombo = ref<Combo[]>([])

// Quản lý bản ghi đang chọn phục vụ xem trước (Preview) và sửa form
const selectedId = ref<number | null>(null)
const comboDaChon = ref<Combo | undefined>(undefined)
const formRef = ref<any>(null)

// Bộ lọc tìm kiếm & Phân trang đồng bộ với MonView
const filterData = ref({
  tenCombo: ''
})
const trangHienTai = ref(0)
const kichThuocTrang = ref(5)
const tongSoTrang = ref(0)

// Hàm fetch dữ liệu danh sách combo từ API
const taiDanhSachCombo = async () => {
  loading.value = true
  try {
    const res = await ComBoApi.timKiemComBo({
      tenCombo: filterData.value.tenCombo,
      pageNo: trangHienTai.value,
      pageSize: kichThuocTrang.value
    })

    const responseData = res.data as any

    if (responseData && responseData.content) {
      danhSachCombo.value = responseData.content
      tongSoTrang.value = responseData.totalPages || 0

      // Cập nhật lại dữ liệu cho khu vực Preview nếu item đang chọn bị thay đổi sau khi fetch lại
      if (comboDaChon.value) {
        const itemMoi = danhSachCombo.value.find(cb => cb.idCombo === comboDaChon.value?.idCombo)
        if (itemMoi) comboDaChon.value = itemMoi
      }
    } else {
      danhSachCombo.value = Array.isArray(responseData) ? responseData : []
      tongSoTrang.value = 1
    }
  } catch (error) {
    console.error('Lỗi tải danh sách combo:', error)
  } finally {
    loading.value = false
  }
}

// Chuyển trang khi bấm nút phân trang
const chuyenTrang = async (trangMucTieu: number) => {
  trangHienTai.value = trangMucTieu
  await taiDanhSachCombo()
}

// Điều hướng trang chi tiết
const chuyenSangChiTiet = (cb: Combo) => {
  router.push({
    name: 'CTCB',
    query: { idCombo: cb.idCombo.toString() }
  })
}

// Điều hướng trạng thái Form
const chonComboXemPreview = (combo: Combo) => {
  selectedId.value = combo.idCombo
  comboDaChon.value = combo
  formRef.value?.fillForm(combo)
}

const chuyenSangThemMoi = () => {
  selectedId.value = null
  comboDaChon.value = undefined
  formRef.value?.fillForm()
}

// Xử lý bộ lọc tìm kiếm
const xuLyTimKiem = (tuKhoa: string) => {
  filterData.value.tenCombo = tuKhoa
  trangHienTai.value = 0 // Reset về trang 1
  taiDanhSachCombo()
}

const xuLyLamMoi = () => {
  filterData.value.tenCombo = ''
  trangHienTai.value = 0 // Reset về trang 1
  taiDanhSachCombo()
}

const xuLySubmitForm = async (payload: ComboRequest & { fileThat?: File | null }) => {
  const isUpdate = selectedId.value !== null
  const actionName = isUpdate ? 'cập nhật' : 'thêm mới'

  if (!confirm(`Bạn có chắc chắn muốn ${actionName} combo này không?`)) return

  loading.value = true
  try {
    let tenFileAnhCuoiCung = payload.hinhAnh
    if (payload.fileThat) {
      const uploadRes = await ComBoApi.uploadImage(payload.fileThat)
      tenFileAnhCuoiCung = uploadRes.data
    }

    // Đóng gói request payload sạch sẽ
    const dataGuiDi: ComboRequest = {
      tenCombo: payload.tenCombo,
      giaCombo: payload.giaCombo,
      hinhAnh: tenFileAnhCuoiCung,
      trangThai: payload.trangThai,
      trangThaiBan: payload.trangThaiBan
    }

    if (isUpdate) {
      await ComBoApi.updateComBo(selectedId.value!, dataGuiDi)
    } else {
      await ComBoApi.addComBo(dataGuiDi)
    }

    alert(`${isUpdate ? 'Cập nhật' : 'Thêm mới'} combo thành công!`)
    chuyenSangThemMoi()
    await taiDanhSachCombo()

  } catch (error: any) {
    const beErrorMsg = error.response?.data?.message || error.response?.data || `Có lỗi khi ${actionName}!`;
    alert(beErrorMsg)
  } finally {
    loading.value = false
  }
}

// Ngưng bán combo (Xóa mềm)
const xuLyXoaCombo = async (idCombo: number) => {
  if (confirm('Bạn có chắc chắn muốn ngưng bán combo này không?')) {
    try {
      await ComBoApi.deleteComBo(idCombo)
      alert('Đã ngưng bán combo thành công!')

      if (selectedId.value === idCombo) chuyenSangThemMoi()
      await taiDanhSachCombo()
    } catch (error: any) {
      const beErrorMsg = error.response?.data?.message || error.response?.data || 'Có lỗi xảy ra khi ngưng bán combo!';
      alert(beErrorMsg)
    }
  }
}

onMounted(() => {
  taiDanhSachCombo()
})
</script>

<style scoped>
.trang-quan-ly-combo {
  min-height: 100vh;
  padding: 20px 0 32px;
  background: transparent;
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 24px;
  align-items: start;
}

.cot-danh-sach, .cot-bieu-mau {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

@media (max-width: 1200px) {
  .trang-quan-ly-combo {
    grid-template-columns: 1fr;
  }
}
</style>