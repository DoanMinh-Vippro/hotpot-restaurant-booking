<template>
  <div class="trang-quan-ly-mon">
    <div class="cot-danh-sach">
      <MonTable
        :danhSachMon="danhSachMon"
        :loading="loading"
        :selectedId="selectedId"
        :danhSachDanhMuc="danhSachDanhMuc"
        @edit="chonMonAnXemPreview"
        @delete="xuLyXoaMon"
        @add="chuyenSangThemMoi"
        @search="xuLyTimKiem"
        @reset="xuLyLamMoi"
        @go-to-category="() => $router.push('/danhMuc')" 
      />

      <!-- 📄 BỔ SUNG COMPONENT PHÂN TRANG GIONG VIEW COMBO -->
      <Pagination 
        :page-no="trangHienTai"
        :total-pages="tongSoTrang"
        @change-page="chuyenTrang"
      />
    </div>

    <div class="cot-bieu-mau">
      <MonForm
        ref="formRef"
        :danhSachDanhMuc="danhSachDanhMuc"
        :danhSachMon="danhSachMon"
        @submit="xuLySubmitForm"
      />

      <MonPreview :monDaChon="monDaChon" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import MonApi, { type Mon, type MonRequest } from '../api/MonApi'
import DanhMucApi, { type DanhMuc } from '../api/DanhMucApi'
import MonTable from '../components/MonTable.vue'
import MonForm from '../components/MonForm.vue'
import MonPreview from '../components/MonPreview.vue'
import Pagination from '../components/Pagination.vue' // Import Pagination component

// Quản lý trạng thái và danh sách dữ liệu
const loading = ref(false)
const danhSachMon = ref<Mon[]>([])
const danhSachDanhMuc = ref<DanhMuc[]>([])

// Quản lý bản ghi đang chọn phục vụ xem trước (Preview) và sửa form
const selectedId = ref<number | null>(null)
const monDaChon = ref<Mon | undefined>(undefined)
const formRef = ref<any>(null)

// Bộ lọc tìm kiếm & Phân trang đồng bộ với Combo
const filterData = ref({
  tenMon: '',
  loaiDanhMuc: ''
})
const trangHienTai = ref(0)
const kichThuocTrang = ref(5)
const tongSoTrang = ref(0)

// Các hàm fetch dữ liệu từ API
const taiDanhSachDanhMuc = async () => {
  try {
    const res = await DanhMucApi.getDanhSach()
    danhSachDanhMuc.value = res.data
  } catch (error) {
    console.error('Lỗi tải danh mục:', error)
  }
}

const taiDanhSachMon = async () => {
  loading.value = true
  try {
    const res = await MonApi.searchMon(
      filterData.value.tenMon,
      undefined,
      undefined,
      filterData.value.loaiDanhMuc,
      trangHienTai.value,
      kichThuocTrang.value 
    )

    const responseData = res.data as any

    if (responseData && responseData.content) {
      danhSachMon.value = responseData.content
      tongSoTrang.value = responseData.totalPages || 0

      // Cập nhật lại dữ liệu cho khu vực Preview nếu item đang chọn bị thay đổi sau khi fetch lại
      if (monDaChon.value) {
        const itemMoi = danhSachMon.value.find(m => m.idMon === monDaChon.value?.idMon)
        if (itemMoi) monDaChon.value = itemMoi
      }
    } else {
      danhSachMon.value = Array.isArray(responseData) ? responseData : []
      tongSoTrang.value = 1
    }
  } catch (error) {
    console.error('Lỗi tải danh sách món ăn:', error)
  } finally {
    loading.value = false
  }
}

// Chuyển trang khi bấm nút phân trang
const chuyenTrang = async (trangMucTieu: number) => {
  trangHienTai.value = trangMucTieu
  await taiDanhSachMon()
}

// Điều hướng trạng thái Form
const chonMonAnXemPreview = (mon: Mon) => {
  selectedId.value = mon.idMon
  monDaChon.value = mon
  formRef.value?.fillForm(mon) 
}

const chuyenSangThemMoi = () => {
  selectedId.value = null
  monDaChon.value = undefined
  formRef.value?.fillForm() 
}

// Xử lý bộ lọc tìm kiếm
const xuLyTimKiem = (payload: { tenMon: string; loaiDanhMuc: string }) => {
  filterData.value.tenMon = payload.tenMon
  filterData.value.loaiDanhMuc = payload.loaiDanhMuc
  trangHienTai.value = 0 // Reset về trang 1
  taiDanhSachMon()
}

const xuLyLamMoi = () => {
  filterData.value.tenMon = ''
  filterData.value.loaiDanhMuc = ''
  trangHienTai.value = 0 // Reset về trang 1
  taiDanhSachMon()
}

const xuLySubmitForm = async (payload: any) => {
  const isUpdate = selectedId.value !== null
  const actionName = isUpdate ? 'cập nhật' : 'thêm mới'

  if (!confirm(`Bạn có chắc chắn muốn ${actionName} món ăn này không?`)) return

  loading.value = true
  try {
    let tenFileAnhCuoiCung = payload.hinhAnh; 
    if (payload.fileThat) {
      const uploadRes = await MonApi.uploadImage(payload.fileThat);
      tenFileAnhCuoiCung = uploadRes.data; 
    }

    // Đóng gói request payload sạch sẽ
    const dataGuiDi: MonRequest = {
      tenMon: payload.tenMon,
      hinhAnh: tenFileAnhCuoiCung, 
      donGiaHienTai: payload.donGiaHienTai,
      idDanhMuc: payload.idDanhMuc,
      trangThai: payload.trangThai,
      trangThaiBan: payload.trangThaiBan 
    }

    if (isUpdate) {
      await MonApi.updateMon(selectedId.value!, dataGuiDi)
    } else {
      await MonApi.addMon(dataGuiDi)
    }

    alert(`${isUpdate ? 'Cập nhật' : 'Thêm mới'} món ăn thành công!`)
    chuyenSangThemMoi()
    await taiDanhSachMon()

  } catch (error: any) {
    const beErrorMsg = error.response?.data?.message || error.response?.data || `Có lỗi khi ${actionName}!`;
    alert(beErrorMsg);
  } finally {
    loading.value = false
  }
}

// Ngưng bán món ăn (Xóa mềm)
const xuLyXoaMon = async (idMon: number) => {
  if (confirm('Bạn có chắc chắn muốn ngưng bán món ăn này không?')) {
    try {
      await MonApi.deleteMon(idMon)
      alert('Đã ngưng bán món ăn thành công!')
      
      if (selectedId.value === idMon) chuyenSangThemMoi()
      await taiDanhSachMon()
    } catch (error: any) {
      const beErrorMsg = error.response?.data?.message || error.response?.data || 'Có lỗi xảy ra khi ngưng bán món!';
      alert(beErrorMsg);
    }
  }
}

onMounted(() => {
  taiDanhSachDanhMuc()
  taiDanhSachMon()
})
</script>

<style scoped>
.trang-quan-ly-mon {
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
  .trang-quan-ly-mon {
    grid-template-columns: 1fr;
  }
}
</style>