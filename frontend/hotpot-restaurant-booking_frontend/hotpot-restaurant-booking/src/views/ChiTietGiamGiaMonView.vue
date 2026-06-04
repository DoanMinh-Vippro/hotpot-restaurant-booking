<template>
  <div class="container">
    <div class="khu-vuc-dieu-huong">
      <button class="nut-quay-lai" @click="quayLaiDotGiamGia">
        ⬅ Quay lại Đợt giảm giá
      </button>
    </div>
    
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
      <Form 
        ref="formRef" 
        :danh-sach="danhSach"
        @submit="luu" 
      />

      <Preview :item="itemChon" />
    </div>

  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ChiTietGiamGiaMonApi from '../api/ChiTietGiamGiaMonApi'
import DotGiamGiaApi from '../api/DotGiamGiaApi'

import Form from '../components/ChiTietGiamGiaMonForm.vue'
import Table from '../components/ChiTietGiamGiaMonTable.vue'
import Preview from '../components/ChiTietGiamGiaMonPreview.vue'
import Pagination from '../components/Pagination.vue' 

import type {
  ChiTietGiamGiaMon,
  ChiTietGiamGiaMonRequest,
} from '../api/ChiTietGiamGiaMonApi'

const route = useRoute()
const router = useRouter()
const danhSach = ref<ChiTietGiamGiaMon[]>([])
const itemChon = ref<ChiTietGiamGiaMon | undefined>()
const selectedId = ref<number | null>(null)
const formRef = ref()

const bieuThucTenChuongTrinh = ref('')
const bieuThucTenMon = ref('')
const trangHienTai = ref(0)
const kichThuocTrang = ref(5) 
const tongSoTrang = ref(0)

const fetchDuLieu = async () => {
  try {
    const res = await ChiTietGiamGiaMonApi.search(
      bieuThucTenChuongTrinh.value,
      bieuThucTenMon.value,
      undefined, 
      undefined, 
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
    console.error("Lỗi khi tải danh sách chi tiết giảm giá phân trang:", error)
  }
}

// Hàm xử lý quay lại màn hình danh sách đợt giảm giá chính
const quayLaiDotGiamGia = () => {
  router.push({ name: 'dotGiamGia' })
}

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

onMounted(async () => {
  if (route.query.idDotGiamGia) {
    const idDggTuQuery = Number(route.query.idDotGiamGia)
    if (!isNaN(idDggTuQuery)) {
      formRef.value?.fillForm({
        idDotGiamGia: idDggTuQuery
      })

      try {
        const dggRes = await DotGiamGiaApi.getDanhSach()
        const danhSachDGG = Array.isArray(dggRes.data) ? dggRes.data : (dggRes.data as any).content || []
        const dggTimThay = danhSachDGG.find((d: any) => d.idDotGiamGia === idDggTuQuery)
        
        if (dggTimThay) {
          bieuThucTenChuongTrinh.value = dggTimThay.tenChuongTrinh
        }
      } catch (err) {
        console.error("Hệ thống không lấy được thông tin tên đợt giảm giá từ API:", err)
      }
    }
  }

  await fetchDuLieu()
})

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

/* Định vị khu vực nút quay lại trải dài toàn bộ hàng phía trên Grid */
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