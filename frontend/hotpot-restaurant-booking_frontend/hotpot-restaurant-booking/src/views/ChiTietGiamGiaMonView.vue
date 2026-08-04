<template>
  <div class="container">
    <div class="khu-vuc-dieu-huong">
      <!-- <button class="nut-quay-lai" @click="quayLaiDotGiamGia">
        ⬅ Quay lại Đợt giảm giá
      </button> -->
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
import ChiTietGiamGiaComboApi from '../api/ChiTietGiamGiaCombo.ts'
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
const bieuThucLoaiGiam = ref('')
const trangHienTai = ref(0)
const kichThuocTrang = ref(5) 
const tongSoTrang = ref(0)

// Helper bóc tách tên / ID an toàn từ nhiều cấu trúc API Backend
const layIdKhoiItem = (item: any, propId: string, nestedProp: string): number | null => {
  const val = item[propId] ?? item[nestedProp]?.[propId]
  return val ? Number(val) : null
}

const layTenKhoiItem = (item: any, propTen: string, nestedProp: string): string => {
  return item[propTen] ?? item[nestedProp]?.[propTen] ?? ''
}

// ---------------------------------------------------------
// 🔥 FETCH CẢ MÓN VÀ COMBO RỒI GOM NHÓM DỮ LIỆU THÀNH 1 DÒNG
// ---------------------------------------------------------
const fetchDuLieu = async () => {
  try {
    // 1. Lấy toàn bộ danh sách Món và Combo (Không truyền phân trang xuống Backend để tránh bị xé lẻ)
    const [resMon, resCombo] = await Promise.allSettled([
      ChiTietGiamGiaMonApi.search(
        bieuThucTenChuongTrinh.value,
        bieuThucTenMon.value,
        undefined, 
        undefined,
        bieuThucLoaiGiam.value, 
        0,
        1000 // Lấy danh sách đủ lớn để Client tự gộp & phân trang
      ),
      ChiTietGiamGiaComboApi.timKiemCTGGC(
        bieuThucTenChuongTrinh.value,
        bieuThucTenMon.value,
        undefined,
        undefined,
        bieuThucLoaiGiam.value,
        0,
        1000
      )
    ])
    
    let rawListMon: any[] = []
    let rawListCombo: any[] = []

    // 2. Bóc tách dữ liệu Món
    if (resMon.status === 'fulfilled') {
      const responseData = resMon.value.data as any
      rawListMon = responseData?.content || (Array.isArray(responseData) ? responseData : [])
    }

    // 3. Bóc tách dữ liệu Combo
    if (resCombo.status === 'fulfilled') {
      const responseDataCombo = resCombo.value.data as any
      rawListCombo = responseDataCombo?.content || (Array.isArray(responseDataCombo) ? responseDataCombo : [])
    }

    // 4. Gom nhóm & Ghép toàn bộ Món + Combo vào 1 Mảng chung
    const mapGomNhom = new Map<string, ChiTietGiamGiaMon>()

    rawListMon.forEach((item: any) => {
      const idChiTiet = item.idChiTietGiamGiaMon ?? item.id
      const monId = layIdKhoiItem(item, 'idMon', 'mon')
      const monTen = layTenKhoiItem(item, 'tenMon', 'mon')
      const keyGomNhom = idChiTiet ? `MON_${idChiTiet}` : `MON_TMP_${Math.random()}`

      mapGomNhom.set(keyGomNhom, {
        ...item,
        idChiTietGiamGiaMon: idChiTiet,
        tenChuongTrinh: item.tenChuongTrinh || item.dotGiamGia?.tenChuongTrinh || 'Chương trình',
        danhSachMon: (monId || monTen) ? [{ idMon: monId || 0, tenMon: monTen || 'Món ăn' }] : (item.danhSachMon || []),
        danhSachCombo: [],
        danhSachDanhMuc: []
      })
    })

    rawListCombo.forEach((item: any) => {
      const idChiTiet = item.idChiTietGiamGiaCombo ?? item.idChiTietGiamGiaMon ?? item.id
      const comboId = layIdKhoiItem(item, 'idCombo', 'combo')
      const comboTen = layTenKhoiItem(item, 'tenCombo', 'combo')
      const keyGomNhom = idChiTiet ? `COMBO_${idChiTiet}` : `COMBO_TMP_${Math.random()}`

      mapGomNhom.set(keyGomNhom, {
        ...item,
        idChiTietGiamGiaMon: idChiTiet,
        tenChuongTrinh: item.tenChuongTrinh || item.dotGiamGia?.tenChuongTrinh || 'Chương trình',
        danhSachMon: [],
        danhSachCombo: (comboId || comboTen) ? [{ idCombo: comboId || 0, tenCombo: comboTen || 'Combo' }] : (item.danhSachCombo || []),
        danhSachDanhMuc: []
      })
    })

    const tatCaDuLieu = Array.from(mapGomNhom.values())

    // 💥 5. BƯỚC TÍNH PHÂN TRANG CHUẨN ĐÉT 5 ITEM / TRANG
    tongSoTrang.value = Math.ceil(tatCaDuLieu.length / kichThuocTrang.value) || 1

    // Kiểm tra nếu trang hiện tại vượt quá tổng số trang thì lùi về trang cuối
    if (trangHienTai.value >= tongSoTrang.value) {
      trangHienTai.value = Math.max(0, tongSoTrang.value - 1)
    }

    const viTriBatDau = trangHienTai.value * kichThuocTrang.value
    const viTriKetThuc = viTriBatDau + kichThuocTrang.value

    // Cắt đúng 5 phần tử hiển thị lên Bảng
    danhSach.value = tatCaDuLieu.slice(viTriBatDau, viTriKetThuc)

  } catch (error) {
    console.error("Lỗi khi tải danh sách chi tiết giảm giá phân trang:", error)
  }
}

// Hàm xử lý quay lại màn hình danh sách đợt giảm giá chính
const quayLaiDotGiamGia = () => {
  router.push({ name: 'giam-gia', query: { tab: 'dot-giam-gia' } })
}

const nhanSuKienTimKiem = async (boLoc: { tenChuongTrinh: string, tenMon: string, loaiGiam: string }) => {
  bieuThucTenChuongTrinh.value = boLoc.tenChuongTrinh
  bieuThucTenMon.value = boLoc.tenMon
  bieuThucLoaiGiam.value = boLoc.loaiGiam
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
  bieuThucLoaiGiam.value = ''
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

const luu = async (payload: any) => {
  const isUpdate = selectedId.value !== null
  const tenHanhDong = isUpdate ? 'cập nhật' : 'thêm mới'

  if (!confirm(`Bạn có chắc chắn muốn ${tenHanhDong} mục giảm giá này?`)) return

  try {
    const isCombo = payload.idCombo || (payload.danhSachComboId && payload.danhSachComboId.length > 0)
    // 🔥 Phân nhánh gọi API tùy thuộc vào đối tượng người dùng đang thao tác
    if (isCombo) {
      if (isUpdate && selectedId.value) {
        await ChiTietGiamGiaComboApi.updateCTGGC(selectedId.value, payload)
      } else {
        await ChiTietGiamGiaComboApi.addCTGGC(payload)
      }
    } else {
      // Mặc định là Món ăn
      if (isUpdate && selectedId.value) {
        await ChiTietGiamGiaMonApi.update(selectedId.value, payload)
      } else {
        await ChiTietGiamGiaMonApi.add(payload)
      }
    }

    alert(`${isUpdate ? 'Cập nhật' : 'Thêm mới'} chi tiết giảm giá thành công!`)
    themMoi()
    await fetchDuLieu()
  } catch (error: any) {
    const lỗiTừBackend = error.response?.data?.message || error.response?.data || `Lỗi hệ thống khi ${tenHanhDong}!`;
    alert(lỗiTừBackend)
  }
}

const xoa = async (id: number) => {
  try {
    await ChiTietGiamGiaMonApi.delete(id)
    alert("Xoá/ngừng áp dụng chi tiết giảm giá thành công!")
    if (selectedId.value === id) themMoi()
    await fetchDuLieu()
  } catch (error: any) {
    const lỗiTừBackend = error.response?.data?.message || error.response?.data || "Không thể xoá chi tiết giảm giá này!";
    alert(lỗiTừBackend)
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  padding: 120px 32px 32px;
  background: #fcf8f2;
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
  background: rgba(255, 248, 234, 0.96);
  border: 1px solid #e6d2aa;
  color: #5f3d22;
  padding: 10px 18px;
  border-radius: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.nut-quay-lai:hover {
  background: #f8d46a;
  border-color: #d8a85c;
  color: #1a1410;
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