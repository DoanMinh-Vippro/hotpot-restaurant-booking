<template>
  <div class="container">
    
    <div class="cot-trai">
      <DotGiamGiaTable
        :danh-sach="danhSach"
        :selected-id="selectedId"
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
      <DotGiamGiaForm
        ref="formRef"
        :danh-sach="danhSach"
        @submit="luu"
      />

      <DotGiamGiaPreview
        :item="itemChon"
      />
    </div>

  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import DotGiamGiaApi from '../api/DotGiamGiaApi'

import DotGiamGiaTable from '../components/DotGiamGiaTable.vue'
import DotGiamGiaForm from '../components/DotGiamGiaForm.vue'
import DotGiamGiaPreview from '../components/DotGiamGiaPreview.vue'
import Pagination from '../components/Pagination.vue' 

import type { DotGiamGia, DotGiamGiaRequest } from '../api/DotGiamGiaApi'

const router = useRouter()
const danhSach = ref<DotGiamGia[]>([])
const itemChon = ref<DotGiamGia | undefined>()
const selectedId = ref<number | null>(null)
const formRef = ref()

const bieuThucTenChuongTrinh = ref('')
const bieuThucTuNgay = ref('')
const bieuThucDenNgay = ref('')
const trangHienTai = ref(0)
const kichThuocTrang = ref(5) 
const tongSoTrang = ref(0)

const fetchDuLieu = async () => {
  try {
    const tuNgayTarget = bieuThucTuNgay.value
    const denNgayTarget = bieuThucDenNgay.value

    if (tuNgayTarget && denNgayTarget && new Date(denNgayTarget) < new Date(tuNgayTarget)) {
      return alert("Tìm kiếm thất bại: Ngày kết thúc không được nhỏ hơn ngày bắt đầu lọc!")
    }

    const res = await DotGiamGiaApi.search(
      bieuThucTenChuongTrinh.value,
      tuNgayTarget || undefined,
      denNgayTarget || undefined,
      trangHienTai.value,
      kichThuocTrang.value
    )
    
    const responseData = res.data as any
    
    if (responseData && responseData.content) {
      danhSach.value = responseData.content
      tongSoTrang.value = responseData.totalPages || 0
    } else {
      danhSach.value = Array.isArray(responseData) ? responseData : []
      tongSoTrang.value = 1
    }
  } catch (error) {
    console.error("Hệ thống lỗi khi nạp danh sách đợt giảm giá phân trang:", error)
  }
}

const chuyenSangChiTiet = (item: DotGiamGia) => {
  router.push({
    name: 'CTGGM',
    query: { idDotGiamGia: item.idDotGiamGia.toString() }
  })
}

const nhanSuKienTimKiem = async (boLoc: { tenChuongTrinh: string, tuNgay: string, denNgay: string }) => {
  bieuThucTenChuongTrinh.value = boLoc.tenChuongTrinh
  bieuThucTuNgay.value = boLoc.tuNgay
  bieuThucDenNgay.value = boLoc.denNgay
  trangHienTai.value = 0
  await fetchDuLieu()
}

const chuyenTrang = async (trangMucTieu: number) => {
  trangHienTai.value = trangMucTieu
  await fetchDuLieu()
}

const lamMoiTimKiem = async () => {
  bieuThucTenChuongTrinh.value = ''
  bieuThucTuNgay.value = ''
  bieuThucDenNgay.value = ''
  trangHienTai.value = 0
  await fetchDuLieu()
}

onMounted(fetchDuLieu)

const themMoi = () => {
  itemChon.value = undefined
  selectedId.value = null
  formRef.value?.fillForm()
}

const sua = (item: DotGiamGia) => {
  itemChon.value = item
  selectedId.value = item.idDotGiamGia
  formRef.value?.fillForm(item)
}

const luu = async (payload: DotGiamGiaRequest) => {
  const isUpdate = selectedId.value !== null
  const tenHanhDong = isUpdate ? 'cập nhật' : 'thêm mới'

  if (!confirm(`Bạn có chắc chắn muốn ${tenHanhDong} chương trình giảm giá này không?`)) return

  try {
    if (isUpdate && selectedId.value) {
      await DotGiamGiaApi.update(selectedId.value, payload)
    } else {
      await DotGiamGiaApi.add(payload)
    }

    alert(`${isUpdate ? 'Cập nhật' : 'Thêm mới'} chương trình giảm giá thành công!`)
    themMoi()
    await fetchDuLieu()
  } catch (error: any) {
    const messageLoiBackend = error.response?.data?.message || error.response?.data || `Lỗi hệ thống khi thực hiện ${tenHanhDong}!`;
    alert(messageLoiBackend)
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