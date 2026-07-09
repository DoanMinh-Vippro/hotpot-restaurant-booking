<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import GiamGiaList from '../components/GiamGiaList.vue'
import GiamGiaForm from '../components/GiamGiaForm.vue'
import GiamGiaPreview from '../components/GiamGiaPreview.vue'
import DotGiamGiaTable from '../components/DotGiamGiaTable.vue'
import DotGiamGiaForm from '../components/DotGiamGiaForm.vue'
import DotGiamGiaPreview from '../components/DotGiamGiaPreview.vue'
import Pagination from '../components/Pagination.vue'
import GiamGiaApi from '../api/GiamGiaApi'
import DotGiamGiaApi from '../api/DotGiamGiaApi'
import type { GiamGia } from '../api/GiamGiaApi'
import type { DotGiamGia, DotGiamGiaRequest } from '../api/DotGiamGiaApi'

const route = useRoute()
const router = useRouter()

// Tab state
const activeTab = ref<'giam-gia' | 'dot-giam-gia'>(
  (route.query.tab as 'giam-gia' | 'dot-giam-gia') || 'giam-gia'
)

// GiamGia state
const danh_sach_giam_gia = ref<GiamGia[]>([])
const dang_tai = ref(false)
const dang_gui = ref(false)
const loi_may_chu = ref('')
const thong_bao_thanh_cong = ref('')
const tim_kiem_query = ref('')
const id_da_chon = ref<number | null>(null)
const che_do_bieu_mau = ref<'create' | 'edit'>('create')
const bieu_mau_ref = ref<InstanceType<typeof GiamGiaForm>>()

// DotGiamGia state
const danhSach = ref<DotGiamGia[]>([])
const itemChon = ref<DotGiamGia | undefined>()
const selectedId = ref<number | null>(null)
const formRef = ref<InstanceType<typeof DotGiamGiaForm>>()
const bieuThucTenChuongTrinh = ref('')
const bieuThucTuNgay = ref('')
const bieuThucDenNgay = ref('')
const trangHienTai = ref(0)
const kichThuocTrang = ref(5)
const tongSoTrang = ref(0)
const dang_tai_dot = ref(false)
const loi_dot = ref('')

const giam_gia_da_chon = computed(() => danh_sach_giam_gia.value.find((item) => item.idGiamGia === id_da_chon.value))

const danh_sach_da_loc = computed(() => {
  if (!tim_kiem_query.value.trim()) return danh_sach_giam_gia.value
  const q = tim_kiem_query.value.trim().toLowerCase()
  return danh_sach_giam_gia.value.filter((d) => 
    d.maGiamGia.toLowerCase().includes(q) ||
    (d.dieuKienSuDung && d.dieuKienSuDung.toLowerCase().includes(q))
  )
})

// GiamGia methods
const tai_danh_sach_giam_gia = async () => {
  dang_tai.value = true
  loi_may_chu.value = ''
  try {
    const response = tim_kiem_query.value.trim()
      ? await GiamGiaApi.search(tim_kiem_query.value.trim())
      : await GiamGiaApi.getDanhSach()
    
    danh_sach_giam_gia.value = response.data
    if (!danh_sach_giam_gia.value.length) {
      id_da_chon.value = null
    } else if (id_da_chon.value == null) {
      id_da_chon.value = danh_sach_giam_gia.value[0]?.idGiamGia ?? null
    }
  } catch (error) {
    loi_may_chu.value = 'Không tải được dữ liệu giảm giá. Kiểm tra backend hoặc cấu hình API.'
    console.error(error)
  } finally {
    dang_tai.value = false
  }
}

const xu_ly_chon_giam_gia = (id: number) => {
  id_da_chon.value = id
}

const xu_ly_sua_giam_gia = (giam_gia: GiamGia) => {
  che_do_bieu_mau.value = 'edit'
  bieu_mau_ref.value?.chuan_bi_bieu_mau(giam_gia)
  loi_may_chu.value = ''
  thong_bao_thanh_cong.value = ''
}

const xu_ly_xoa_giam_gia = async (id: number) => {
  dang_tai.value = true
  loi_may_chu.value = ''
  thong_bao_thanh_cong.value = ''

  try {
    await GiamGiaApi.delete(id)
    thong_bao_thanh_cong.value = 'Xóa mã giảm giá thành công'
    await tai_danh_sach_giam_gia()
    xu_ly_huy_bieu_mau()
  } catch (error: any) {
    loi_may_chu.value = error.response?.data?.message || 'Không thể xóa mã giảm giá'
    console.error(error)
  } finally {
    dang_tai.value = false
  }
}

const xu_ly_gui_bieu_mau = async (payload: Record<string, unknown>) => {
  dang_gui.value = true
  loi_may_chu.value = ''
  thong_bao_thanh_cong.value = ''

  try {
    if (id_da_chon.value && che_do_bieu_mau.value === 'edit') {
      await GiamGiaApi.update(id_da_chon.value, payload as Partial<Omit<GiamGia, 'idGiamGia'>>)
      thong_bao_thanh_cong.value = 'Cập nhật giảm giá thành công'
    } else {
      await GiamGiaApi.create(payload as Omit<GiamGia, 'idGiamGia'>)
      thong_bao_thanh_cong.value = 'Tạo giảm giá thành công'
    }

    await tai_danh_sach_giam_gia()
    xu_ly_huy_bieu_mau()
  } catch (error: any) {
    loi_may_chu.value = error.response?.data?.message || 'Lỗi khi gửi dữ liệu giảm giá'
    console.error(error)
  } finally {
    dang_gui.value = false
  }
}

const xu_ly_huy_bieu_mau = () => {
  che_do_bieu_mau.value = 'create'
  id_da_chon.value = null
  bieu_mau_ref.value?.chuan_bi_bieu_mau()
  loi_may_chu.value = ''
  thong_bao_thanh_cong.value = ''
}

const xu_ly_them_moi = () => {
  xu_ly_huy_bieu_mau()
}

// DotGiamGia methods
const fetchDuLieu = async () => {
  dang_tai_dot.value = true
  loi_dot.value = ''
  try {
    const tuNgayTarget = bieuThucTuNgay.value
    const denNgayTarget = bieuThucDenNgay.value

    if (tuNgayTarget && denNgayTarget && new Date(denNgayTarget) < new Date(tuNgayTarget)) {
      loi_dot.value = "Tìm kiếm thất bại: Ngày kết thúc không được nhỏ hơn ngày bắt đầu lọc!"
      return
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
    loi_dot.value = "Không tải được dữ liệu đợt giảm giá"
    console.error(error)
  } finally {
    dang_tai_dot.value = false
  }
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
  try {
    await DotGiamGiaApi.delete(id)
    if (selectedId.value === id) themMoi()
    await fetchDuLieu()
  } catch (error: any) {
    console.error('Lỗi khi xóa đợt giảm giá:', error)
    alert(error.response?.data?.message || 'Không thể xóa đợt giảm giá')
  }
}

const chuyenSangChiTiet = (item: DotGiamGia) => {
  router.push({
    name: 'CTGGM',
    query: { idDotGiamGia: item.idDotGiamGia.toString() }
  })
}

onMounted(() => {
  tai_danh_sach_giam_gia()
  fetchDuLieu()
})
</script>

<template>
  <main class="trang-giam-gia">
    <header class="tieu-de-trang">
      <div>
        <p class="tieu-le">Quản lý giảm giá</p>
        <h1>Giảm Giá & Đợt Giảm Giá</h1>
      </div>
      <div class="header-buttons">
        <button class="nut-chinh" type="button" @click="activeTab === 'giam-gia' ? tai_danh_sach_giam_gia() : fetchDuLieu()">Tải lại</button>
      </div>
    </header>

    <!-- Tab Navigation -->
    <div class="tab-navigation">
      <button 
        :class="['tab-button', { active: activeTab === 'giam-gia' }]"
        @click="activeTab = 'giam-gia'"
      >
        Mã Giảm Giá
      </button>
      <button 
        :class="['tab-button', { active: activeTab === 'dot-giam-gia' }]"
        @click="activeTab = 'dot-giam-gia'"
      >
        Đợt Giảm Giá
      </button>
    </div>

    <!-- Tab 1: GiamGia -->
    <section v-show="activeTab === 'giam-gia'" class="giam-gia-grid">
      <GiamGiaList
        :danh_sach_giam_gia="danh_sach_da_loc"
        :loading="dang_tai"
        :selectedId="id_da_chon"
        :tim_kiem_query="tim_kiem_query"
        @select="xu_ly_chon_giam_gia"
        @edit="xu_ly_sua_giam_gia"
        @delete="xu_ly_xoa_giam_gia"
        @add="xu_ly_them_moi"
        @update:timKiemQuery="(q) => { tim_kiem_query = q; tai_danh_sach_giam_gia() }"
      />
      <div>
        <GiamGiaForm
          ref="bieu_mau_ref"
          :che_do_bieu_mau="che_do_bieu_mau"
          :dang_gui="dang_gui"
          :loi_may_chu="loi_may_chu"
          :thong_bao_thanh_cong="thong_bao_thanh_cong"
          @submit="xu_ly_gui_bieu_mau"
          @reset="xu_ly_huy_bieu_mau"
        />
        <GiamGiaPreview :giam_gia_da_chon="giam_gia_da_chon" />
      </div>
    </section>

    <!-- Tab 2: DotGiamGia -->
    <section v-show="activeTab === 'dot-giam-gia'" class="dot-giam-gia-container">
      <p v-if="loi_dot" class="thong-bao-loi">{{ loi_dot }}</p>
      
      <div class="dot-giam-gia-grid">
        <div class="cot-trai">
          <DotGiamGiaTable
            :danh-sach="danhSach"
            :selected-id="selectedId"
            :loading="dang_tai_dot"
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
    </section>
  </main>
</template>

<style scoped>
.trang-giam-gia {
  min-height: 100vh;
  padding: 24px clamp(16px, 3vw, 40px) 32px;
  background: linear-gradient(135deg, #f9efe0 0%, #f4e4c6 100%);
  color: #5f3d22;
}

.tieu-de-trang {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.tieu-de-trang h1 {
  margin: 6px 0 0;
  font-size: clamp(1.5rem, 3vw, 2.2rem);
  color: #8b5e34;
}

.tieu-le {
  margin: 0;
  color: #8f6b46;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 0.78rem;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.nut-chinh {
  border: none;
  border-radius: 999px;
  padding: 10px 16px;
  background: #d8a85c;
  color: #3d2814;
  font-weight: 700;
  cursor: pointer;
}

.tab-navigation {
  display: inline-flex;
  gap: 8px;
  padding: 6px;
  background: rgba(255, 248, 234, 0.9);
  border: 1px solid #e6d2aa;
  border-radius: 999px;
  margin-bottom: 20px;
}

.tab-button {
  border: none;
  border-radius: 999px;
  padding: 10px 16px;
  background: transparent;
  color: #8f6b46;
  font-weight: 700;
  cursor: pointer;
}

.tab-button.active {
  background: #d8a85c;
  color: #3d2814;
}

.giam-gia-grid,
.dot-giam-gia-grid {
  display: grid;
  grid-template-columns: minmax(320px, 1.1fr) minmax(320px, 0.9fr);
  gap: 18px;
  align-items: start;
}

.dot-giam-gia-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cot-trai,
.cot-phai {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.thong-bao-loi {
  margin: 0;
  padding: 12px 16px;
  border-radius: 12px;
  background: rgba(255, 107, 107, 0.12);
  color: #b84f3f;
  border: 1px solid rgba(255, 107, 107, 0.2);
}

@media (max-width: 1100px) {
  .giam-gia-grid,
  .dot-giam-gia-grid {
    grid-template-columns: 1fr;
  }
}
</style>

<style scoped>
.trang-giam-gia {
  padding: 120px 60px 40px;
  min-height: calc(100vh - 120px);
  color: #5f3d22;
  background: linear-gradient(135deg, #f9efe0 0%, #f4e4c6 100%);
}

.tieu-de-trang {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  margin-bottom: 28px;
}

.tieu-de-trang h1 {
  margin: 0;
  font-size: clamp(2rem, 2.4vw, 2.6rem);
}

.header-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}

.tieu-le {
  text-transform: uppercase;
  letter-spacing: 2px;
  color: #8b5e34;
  margin-bottom: 8px;
  font-size: 0.8rem;
}

.nut-chinh {
  border: 1px solid #e6d2aa;
  background: #d8a85c;
  color: #3d2814;
  min-height: 42px;
  border-radius: 6px;
  padding: 0 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nut-chinh:hover {
  background: #ffe3a3;
  box-shadow: 0 4px 12px rgba(103, 72, 32, 0.12);
}

/* Tab Navigation */
.tab-navigation {
  display: flex;
  gap: 12px;
  margin-bottom: 28px;
  border-bottom: 2px solid #e7d6b5;
  padding-bottom: 0;
}

.tab-button {
  padding: 12px 20px;
  background: transparent;
  border: none;
  color: #8b5e34;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.tab-button:hover {
  color: #d8a85c;
}

.tab-button.active {
  color: #8b5e34;
  border-bottom-color: #d8a85c;
}

/* Grids */
.giam-gia-grid,
.dot-giam-gia-grid {
  display: grid;
  grid-template-columns: minmax(320px, 1.2fr) minmax(360px, 0.9fr);
  gap: 24px;
  align-items: start;
}

.dot-giam-gia-container {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.thong-bao-loi {
  background: rgba(231, 76, 60, 0.2);
  border-left: 3px solid #e74c3c;
  color: #e74c3c;
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 16px;
  font-size: 0.95rem;
}

.cot-trai,
.cot-phai {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

@media (max-width: 1024px) {
  .giam-gia-grid,
  .dot-giam-gia-grid {
    grid-template-columns: 1fr;
  }

  .tieu-de-trang {
    flex-direction: column;
    align-items: stretch;
  }

  .tab-navigation {
    flex-wrap: wrap;
  }
}
</style>
