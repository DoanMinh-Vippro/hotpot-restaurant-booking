<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import GiamGiaList from '../components/GiamGiaList.vue'
import GiamGiaForm from '../components/GiamGiaForm.vue'
import GiamGiaPreview from '../components/GiamGiaPreview.vue'
import GiamGiaApi from '../api/GiamGiaApi'
import type { GiamGia } from '../api/GiamGiaApi'

const danh_sach_giam_gia = ref<GiamGia[]>([])
const dang_tai = ref(false)
const dang_gui = ref(false)
const loi_may_chu = ref('')
const thong_bao_thanh_cong = ref('')
const tim_kiem_query = ref('')
const id_da_chon = ref<number | null>(null)
const che_do_bieu_mau = ref<'create' | 'edit'>('create')
const bieu_mau_ref = ref<InstanceType<typeof GiamGiaForm>>()

const giam_gia_da_chon = computed(() => danh_sach_giam_gia.value.find((item) => item.idGiamGia === id_da_chon.value))

const danh_sach_da_loc = computed(() => {
  if (!tim_kiem_query.value.trim()) return danh_sach_giam_gia.value
  const q = tim_kiem_query.value.trim().toLowerCase()
  return danh_sach_giam_gia.value.filter((d) => 
    d.maGiamGia.toLowerCase().includes(q) ||
    (d.dieuKienSuDung && d.dieuKienSuDung.toLowerCase().includes(q))
  )
})

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

onMounted(tai_danh_sach_giam_gia)
</script>

<template>
  <main class="trang-giam-gia">
    <header class="tieu-de-trang">
      <div>
        <p class="tieu-le">Quản lý giảm giá</p>
        <h1>CRUD Giảm giá</h1>
      </div>
      <button class="nut-chinh" type="button" @click="tai_danh_sach_giam_gia">Tải lại</button>
    </header>

    <section class="giam-gia-grid">
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
  </main>
</template>

<style scoped>
.trang-giam-gia {
  padding: 120px 60px 40px;
  min-height: calc(100vh - 120px);
  color: #f1f1f1;
  background: radial-gradient(circle at top, rgba(197, 160, 89, 0.12), transparent 35%),
              linear-gradient(180deg, #070707 0%, #0f0f0f 100%);
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

.tieu-le {
  text-transform: uppercase;
  letter-spacing: 2px;
  color: #f8d46a;
  margin-bottom: 8px;
  font-size: 0.8rem;
}

.nut-chinh {
  border: 1px solid rgba(248, 212, 106, 0.45);
  background: #f8d46a;
  color: #16110a;
  min-height: 42px;
  border-radius: 6px;
  padding: 0 16px;
  font-weight: 700;
  cursor: pointer;
}

.giam-gia-grid {
  display: grid;
  grid-template-columns: minmax(320px, 1.2fr) minmax(360px, 0.9fr);
  gap: 24px;
  align-items: start;
}

@media (max-width: 1024px) {
  .giam-gia-grid {
    grid-template-columns: 1fr;
  }

  .tieu-de-trang {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
