<template>
  <div class="container">
    
    <div class="cot-trai">
      <Table
        :danh-sach="danhSach"
        :loading="false"
        :selected-id="selectedId"
        @edit="sua"
        @delete="xoa"
        @add="themMoi"
        @search="nhanSuKienTimKiem"
        @reset="lamMoiTimKiem"
      />

      <Pagination 
        :page-no="trangHienTai"
        :total-pages="tongSoTrang"
        @change-page="chuyenTrang"
      />
    </div>

    <div class="cot-phai">
      <Form ref="formRef" @submit="luu" />

      <Preview :item="itemChon" />
    </div>

  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import ChiTietGiamGiaMonApi from '../api/ChiTietGiamGiaMonApi'

import Form from '../components/ChiTietGiamGiaMonForm.vue'
import Table from '../components/ChiTietGiamGiaMonTable.vue'
import Preview from '../components/ChiTietGiamGiaMonPreview.vue'
import Pagination from '../components/Pagination.vue' // Import phân trang dùng chung

import type {
  ChiTietGiamGiaMon,
  ChiTietGiamGiaMonRequest,
} from '../api/ChiTietGiamGiaMonApi'

const danhSach = ref<ChiTietGiamGiaMon[]>([])
const itemChon = ref<ChiTietGiamGiaMon | undefined>()
const selectedId = ref<number | null>(null)
const formRef = ref()

// Quản lý trạng thái bộ lọc và phân trang tập trung
const bieuThucTenChuongTrinh = ref('')
const bieuThucTenMon = ref('')
const trangHienTai = ref(0)
const kichThuocTrang = ref(5) // Mặc định hiển thị 5 bản ghi mỗi trang
const tongSoTrang = ref(0)

// HÀM TẢI DỮ LIỆU ĐỒNG BỘ VỚI STATE BỘ LỌC VÀ LẬT TRANG
const fetchDuLieu = async () => {
  try {
    const res = await ChiTietGiamGiaMonApi.search(
      bieuThucTenChuongTrinh.value,
      bieuThucTenMon.value,
      undefined, // mucMin
      undefined, // mucMax
      trangHienTai.value,
      kichThuocTrang.value
    )
    
    const responseData = res.data as any
    
    // Đọc cấu trúc Page JSON từ Spring Boot trả về
    if (responseData && responseData.content) {
      danhSach.value = responseData.content
      tongSoTrang.value = responseData.totalPages || 0
    } else {
      danhSach.value = Array.isArray(responseData) ? responseData : []
      tongSoTrang.value = 1
    }
  } catch (error) {
    console.error("Lỗi khi tải danh sách chi tiết giảm giá phân trang:", error)
  }
}

// Khi Table phát tín hiệu tìm kiếm, reset số trang về 0
const nhanSuKienTimKiem = async (boLoc: { tenChuongTrinh: string, tenMon: string }) => {
  bieuThucTenChuongTrinh.value = boLoc.tenChuongTrinh
  bieuThucTenMon.value = boLoc.tenMon
  trangHienTai.value = 0
  await fetchDuLieu()
}

const chuyenTrang = async (trangMucTieu: number) => {
  trangHienTai.value = trangMucTieu
  await fetchDuLieu()
}

const lamMoiTimKiem = async () => {
  bieuThucTenChuongTrinh.value = ''
  bieuThucTenMon.value = ''
  trangHienTai.value = 0
  await fetchDuLieu()
}

onMounted(fetchDuLieu)

const themMoi = () => {
  itemChon.value = undefined
  selectedId.value = null
  formRef.value?.fillForm()
}

const sua = (item: ChiTietGiamGiaMon) => {
  itemChon.value = item
  selectedId.value = item.idChiTietGiamGiaMon
  formRef.value?.fillForm(item)
}

const luu = async (payload: ChiTietGiamGiaMonRequest) => {
  const isUpdate = selectedId.value !== null
  const tenHanhDong = isUpdate ? 'cập nhật' : 'thêm mới'

  if (!confirm(`Bạn có chắc chắn muốn ${tenHanhDong} mục giảm giá này?`)) return

  try {
    if (isUpdate && selectedId.value) {
      await ChiTietGiamGiaMonApi.update(selectedId.value, payload)
    } else {
      await ChiTietGiamGiaMonApi.add(payload)
    }

    alert(`${isUpdate ? 'Cập nhật' : 'Thêm mới'} chi tiết giảm giá món thành công!`)
    themMoi()
    await fetchDuLieu()
  } catch (error: any) {
    const lỗiTừBackend = error.response?.data?.message || error.response?.data || `Lỗi hệ thống khi ${tenHanhDong}!`;
    alert(lỗiTừBackend)
  }
}

const xoa = async (id: number) => {
  if (selectedId.value === id) themMoi()
  await fetchDuLieu()
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