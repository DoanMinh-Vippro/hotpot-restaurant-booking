<template>
  <div class="container">
    
    <div class="cot-trai">
      <ChiTietComBoTable
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
      <ChiTietComBoForm
        ref="formRef"
        @submit="luu"
      />

      <ChiTietComBoPreview
        :item="itemChon"
      />
    </div>

  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import ChiTietComBoApi from '../api/ChiTietComBoApi'

import ChiTietComBoTable from '../components/ChiTietComBoTable.vue'
import ChiTietComBoForm from '../components/ChiTietComBoForm.vue'
import ChiTietComBoPreview from '../components/ChiTietComBoPreview.vue'
import Pagination from '../components/Pagination.vue' // Import Pagination dùng chung

import type { ChiTietComBo, ChiTietComBoRequest } from '../api/ChiTietComBoApi'

const danhSach = ref<ChiTietComBo[]>([])
const itemChon = ref<ChiTietComBo | undefined>(undefined)
const selectedId = ref<number | null>(null)
const formRef = ref()

// Biến quản lý trạng thái phân trang động tại View cha
const bieuThucTenCombo = ref('')
const bieuThucTenMon = ref('')
const trangHienTai = ref(0)
const kichThuocTrang = ref(5) // Cố định hiển thị 5 dòng mỗi trang
const tongSoTrang = ref(0)

// HÀM TẢI DỮ LIỆU ĐỒNG BỘ ĐIỀU KIỆN LẬT TRANG
const fetchDuLieu = async () => {
  try {
    const res = await ChiTietComBoApi.searchCTCB(
      bieuThucTenCombo.value,
      bieuThucTenMon.value,
      undefined, // giaMin
      undefined, // giaMax
      trangHienTai.value,
      kichThuocTrang.value
    )
    
    const responseData = res.data as any
    
    // Đọc cấu trúc Page bọc từ Backend Spring Boot trả về
    if (responseData && responseData.content) {
      danhSach.value = responseData.content
      tongSoTrang.value = responseData.totalPages || 0
    } else {
      danhSach.value = Array.isArray(responseData) ? responseData : []
      tongSoTrang.value = 1
    }
  } catch (error) {
    console.error("Lỗi khi lấy danh sách chi tiết combo phân trang:", error)
  }
}

// Khi người dùng nhập bộ lọc từ Table con bắn ra, đưa trang hiện tại về 0
const nhanSuKienTimKiem = async (boLoc: { tenCombo: string, tenMon: string }) => {
  bieuThucTenCombo.value = boLoc.tenCombo
  bieuThucTenMon.value = boLoc.tenMon
  trangHienTai.value = 0
  await fetchDuLieu()
}

const chuyenTrang = async (trangMucTieu: number) => {
  trangHienTai.value = trangMucTieu
  await fetchDuLieu()
}

const lamMoiTimKiem = async () => {
  bieuThucTenCombo.value = ''
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

const sua = (item: ChiTietComBo) => {
  itemChon.value = item
  selectedId.value = item.idChiTietCombo
  formRef.value?.fillForm(item)
}

const luu = async (payload: ChiTietComBoRequest) => {
  const isUpdate = selectedId.value !== null
  const hanhDong = isUpdate ? 'cập nhật' : 'thêm mới'

  if (!confirm(`Bạn có chắc chắn muốn ${hanhDong} chi tiết combo này?`)) return

  try {
    if (isUpdate && selectedId.value) {
      await ChiTietComBoApi.updateCTCB(selectedId.value, payload)
    } else {
      await ChiTietComBoApi.addCTCB(payload)
    }

    alert(`${isUpdate ? 'Cập nhật' : 'Thêm mới'} thành công chi tiết combo!`)
    themMoi()
    await fetchDuLieu()
  } catch (error: any) {
    const tinLoiBackend = error.response?.data?.message || error.response?.data || `Lỗi hệ thống khi thực hiện ${hanhDong}!`;
    alert(tinLoiBackend)
  }
}

const xoa = async (id: number) => {
  try {
    await ChiTietComBoApi.deleteCTCB(id)
    alert('Đã xóa bỏ/ngưng bán thành phần combo thành công!')

    if (selectedId.value === id) themMoi()
    await fetchDuLieu()
  } catch (error) {
    alert('Không thể thực hiện tác vụ xóa, vui lòng thử lại!')
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
  align-items: start; /* Neo phẳng từ đỉnh chống lệch thô */
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