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

import ComBoApi from '../api/ComBoApi'

import ComboForm from '../components/ComBoForm.vue'
import ComboTable from '../components/ComBoTable.vue'
import ComboPreview from '../components/ComBoPreview.vue'
import Pagination from '../components/Pagination.vue' // Đảm bảo đã import đúng đường dẫn

import type { Combo, ComboRequest } from '../api/ComBoApi'

const danhSachCombo = ref<Combo[]>([])
const comboDangChon = ref<Combo | undefined>(undefined)
const selectedId = ref<number | null>(null)
const formRef = ref()

// =========================================================================
// KHAI BÁO STATE QUẢN LÝ PHÂN TRANG (QUAN TRỌNG ĐỂ THOÁT KHÓA 1/1)
// =========================================================================
const tuKhoaHienTai = ref('')
const trangHienTai = ref(0)
const kichThuocTrang = ref(5) // Mỗi trang hiển thị đúng 5 dòng
const tongSoTrang = ref(0)    // State này sẽ thay đổi số hiển thị trên Pagination

// HÀM TẢI DỮ LIỆU ĐỒNG BỘ MỌI BIẾN STATE ĐỔI TRANG
const fetchDuLieu = async () => {
  try {
    // Gọi API tìm kiếm có truyền pageNo (trang hiện tại) và pageSize
    const res = await ComBoApi.timKiemComBo({
      tenCombo: tuKhoaHienTai.value,
      pageNo: trangHienTai.value,
      pageSize: kichThuocTrang.value
    })
    
    const responseData = res.data as any
    
    // Kiểm tra cấu trúc Page trả về từ Spring Boot
    if (responseData && responseData.content) {
      danhSachCombo.value = responseData.content       // Đổ mảng bản ghi vào bảng
      tongSoTrang.value = responseData.totalPages || 0  // CẬP NHẬT TỔNG SỐ TRANG THỰC TẾ TỪ DB
    } else {
      // Phòng hờ nếu Backend chưa trả về Page chuẩn
      danhSachCombo.value = Array.isArray(responseData) ? responseData : []
      tongSoTrang.value = 1
    }
  } catch (error) {
    console.error("Hệ thống lỗi khi nạp danh sách combo phân trang:", error)
  }
}

// Khi gõ từ khóa từ Table con bắn ra, reset trang về 0 để lọc từ đầu
const nhanSuKienTimKiem = async (tuKhoa: string) => {
  tuKhoaHienTai.value = tuKhoa
  trangHienTai.value = 0
  await fetchDuLieu()
}

// HÀM KÍCH HOẠT KHI ẤN NÚT LẬT TRANG (NÚT ◀ ▶ PHÁT SỰ KIỆN VÀO ĐÂY)
const chuyenTrang = async (trangMucTieu: number) => {
  trangHienTai.value = trangMucTieu // Thay đổi số trang hiện tại
  await fetchDuLieu()               // Gọi lại API để lấy dữ liệu trang mới
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
  align-items: start; /* Neo phẳng đều từ đỉnh để chống lệch thụt thò */
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