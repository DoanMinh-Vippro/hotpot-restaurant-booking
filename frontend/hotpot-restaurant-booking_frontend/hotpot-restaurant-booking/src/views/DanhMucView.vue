<template>
  <div class="container">
    <div class="khu-vuc-dieu-huong">
      <button class="nut-quay-lai" @click="quayLaiThucDon">
        ⬅ Quay lại Thực đơn
      </button>
    </div>

    <div class="cot-trai">
      <DanhMucTable
        :danh-sach="danhSach"
        :selected-id="selectedId"
        @edit="sua"
        @delete="xoa"
        @add="themMoi"
        @search="nhanSuKienTimKiem"
        @reset="lamMoiTimKiem"
      />

      <Pagination :page-no="trangHienTai" :total-pages="tongSoTrang" @change-page="chuyenTrang" />
    </div>

    <div class="cot-phai">
      <DanhMucForm 
        ref="formRef" 
        :danh-sach="danhSach"
        @submit="luu" 
      />
      <DanhMucPreview :item="itemChon" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router' // Import useRouter để thực hiện quay về
import DanhMucApi from '../api/DanhMucApi'

import DanhMucTable from '../components/DanhMucTable.vue'
import DanhMucForm from '../components/DanhMucForm.vue'
import DanhMucPreview from '../components/DanhMucPreview.vue'
import Pagination from '../components/Pagination.vue'

import type { DanhMuc, DanhMucRequest } from '../api/DanhMucApi'

const router = useRouter()
const danhSach = ref<DanhMuc[]>([])
const itemChon = ref<DanhMuc | undefined>()
const selectedId = ref<number | null>(null)
const formRef = ref()

// Trạng thái phân trang tập trung
const tuKhoaHienTai = ref('')
const trangHienTai = ref(0)
const kichThuocTrang = ref(5)
const tongSoTrang = ref(0)

const fetchDuLieu = async () => {
  try {
    const target = tuKhoaHienTai.value.trim()
    let res

    if (target) {
      res = await DanhMucApi.search(target, trangHienTai.value, kichThuocTrang.value)
    } else {
      res = await DanhMucApi.phanTrang(trangHienTai.value, kichThuocTrang.value)
    }

    const responseData = res.data as any
    if (responseData && responseData.content) {
      danhSach.value = responseData.content
      tongSoTrang.value = responseData.totalPages || 0
    } else {
      danhSach.value = Array.isArray(responseData) ? responseData : []
      tongSoTrang.value = 1
    }
  } catch (error) {
    console.error('Lỗi hệ thống khi tải dữ liệu:', error)
  }
}

// Hàm thực hiện quay về màn Thực đơn chính
const quayLaiThucDon = () => {
  router.push({ name: 'thucDon' })
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
  itemChon.value = undefined
  selectedId.value = null
  formRef.value?.fillForm()
}

const sua = (item: DanhMuc) => {
  itemChon.value = item
  selectedId.value = item.idDanhMuc
  formRef.value?.fillForm(item)
}

const luu = async (payload: DanhMucRequest) => {
  const isUpdate = selectedId.value !== null
  const hanhDong = isUpdate ? 'cập nhật' : 'thêm mới'

  if (!confirm(`Bạn có chắc chắn muốn ${hanhDong} danh mục này không?`)) return

  try {
    if (isUpdate && selectedId.value) {
      await DanhMucApi.update(selectedId.value, payload)
    } else {
      await DanhMucApi.add(payload)
    }

    alert(`${isUpdate ? 'Cập nhật' : 'Thêm mới'} danh mục thành công!`)
    themMoi()
    await fetchDuLieu()
  } catch (error: any) {
    const lỗiCủaBackend =
      error.response?.data?.message || error.response?.data || `Có lỗi hệ thống xảy ra!`
    alert(lỗiCủaBackend)
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

/* Thanh điều hướng chứa nút quay lại Thực đơn trải rộng toàn hàng Grid */
.khu-vuc-dieu-huong {
  grid-column: 1 / -1;
  margin-bottom: -8px;
}

.nut-quay-lai {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #c7c7c7;
  padding: 10px 18px;
  border-radius: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.nut-quay-lai:hover {
  background: rgba(248, 212, 106, 0.15);
  border-color: #f8d46a;
  color: #f8d46a;
}

.cot-trai,
.cot-phai {
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