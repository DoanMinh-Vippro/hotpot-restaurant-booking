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
        @go-to-category="() => $router.push('/danhmuc')" 
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

// Quản lý trạng thái và danh sách dữ liệu
const loading = ref(false)
const danhSachMon = ref<Mon[]>([])
const danhSachDanhMuc = ref<DanhMuc[]>([])

// Quản lý bản ghi đang chọn phục vụ xem trước (Preview) và sửa form
const selectedId = ref<number | null>(null)
const monDaChon = ref<Mon | undefined>(undefined)
const formRef = ref<any>(null)

// Bộ lọc tìm kiếm
const filterData = ref({
  tenMon: '',
  loaiDanhMuc: ''
})

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
      0,
      100 
    )
    danhSachMon.value = res.data.content || res.data
    

    if (monDaChon.value) {
      const itemMoi = danhSachMon.value.find(m => m.idMon === monDaChon.value?.idMon)
      if (itemMoi) monDaChon.value = itemMoi
    }
  } catch (error) {
    console.error('Lỗi tải danh sách món ăn:', error)
  } finally {
    loading.value = false
  }
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
  taiDanhSachMon()
}

const xuLyLamMoi = () => {
  filterData.value.tenMon = ''
  filterData.value.loaiDanhMuc = ''
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
    // Bắt lỗi Spring Boot Validation / Business Logic thông qua ApiClient
    const beErrorMsg = error.response?.data?.message || error.response?.data || `Có lỗi khi ${actionName}!`;
    alert(beErrorMsg);
  } finally {
    loading.value = false
  }
}

// Ngưng bán món ăn (Xóa mềm) giống Combo
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
  display: flex;
}
.container {
  min-height: 100vh;
  padding: 20px 0 32px;
  background: transparent;
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 24px;
  box-sizing: border-box;
}

.cot-danh-sach {
  flex: 2;
  display: flex;
  flex-direction: column;
}

.cot-bieu-mau {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

@media (max-width: 1024px) {
  .trang-quan-ly-mon {
    flex-direction: column;
  }
  .cot-danh-sach, .cot-bieu-mau {
    flex: 1;
  }
}
</style>