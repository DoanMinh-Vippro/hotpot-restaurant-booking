<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import HoaDonList from '../components/HoaDonList.vue'
import HoaDonDetail from '../components/HoaDonDetail.vue'
import HoaDonApi from '../api/HoaDonApi'
import type { HoaDon, HoaDonChiTiet } from '../api/HoaDonApi'
import { printInvoiceReceipt } from '../utils/printInvoice'
const router = useRouter()
const danh_sach_hoa_don = ref<HoaDon[]>([])
const chi_tiet = ref<HoaDonChiTiet[]>([])
const id_da_chon = ref<number | null>(null)
const dang_tai_hoa_don = ref(false)
const dang_tai_chi_tiet = ref(false)
const thong_bao_loi = ref('')
const hoa_don_da_chon = computed(() =>
  danh_sach_hoa_don.value.find((item) => item.idHoaDon === id_da_chon.value),
)
const so_da_thanh_toan = computed(
  () => danh_sach_hoa_don.value.filter((item) => item.trangThaiThanhToan === 1).length,
)
const getTimestamp = (value: string | number[] | null | undefined): number => {
  if (!value) return 0
  if (Array.isArray(value)) {
    const [year = 0, month = 1, day = 1, hour = 0, minute = 0, second = 0] = value
    return new Date(year, month - 1, day, hour, minute, second).getTime()
  }
  const parsed = new Date(value).getTime()
  return isNaN(parsed) ? 0 : parsed
}
const tai_hoa_don = async () => {
  dang_tai_hoa_don.value = true
  thong_bao_loi.value = ''
  try {
    const response = await HoaDonApi.getDanhSach()
    // Sort descending by date (newest first)
    const sortedData = [...response.data].sort((a, b) => {
      const timeA = getTimestamp(a.thoiGianXuat)
      const timeB = getTimestamp(b.thoiGianXuat)
      return timeB - timeA || b.idHoaDon - a.idHoaDon
    })
    danh_sach_hoa_don.value = sortedData
    id_da_chon.value = sortedData[0]?.idHoaDon ?? null
    if (id_da_chon.value) {
      await tai_chi_tiet(id_da_chon.value)
    }
  } catch (error) {
    thong_bao_loi.value = 'Không tải được dữ liệu hóa đơn. Kiểm tra backend và đường dẫn API.'
    console.error(error)
  } finally {
    dang_tai_hoa_don.value = false
  }
}
const tai_chi_tiet = async (idHoaDon: number) => {
  id_da_chon.value = idHoaDon
  dang_tai_chi_tiet.value = true
  thong_bao_loi.value = ''
  try {
    const response = await HoaDonApi.getChiTiet(idHoaDon)
    chi_tiet.value = response.data
  } catch (error) {
    chi_tiet.value = []
    thong_bao_loi.value = 'Không tải được chi tiết hóa đơn.'
    console.error(error)
  } finally {
    dang_tai_chi_tiet.value = false
  }
}

const in_hoa_don_hien_tai = () => {
  if (!hoa_don_da_chon.value) {
    alert('Vui lòng chọn một hóa đơn trước khi in.')
    return
  }

  printInvoiceReceipt(hoa_don_da_chon.value, chi_tiet.value)
}

onMounted(tai_hoa_don)
</script>
<template>
  <main class="trang-hoa-don">
    <header class="tieu-de-hoa-don">
      <div>
        <p class="tieu-le">Quản lý thanh toán</p>
        <h1>Hóa đơn</h1>
      </div>
      <div class="nhom-hanh-dong">
        <button class="nut-phu" type="button" @click="in_hoa_don_hien_tai">In hoá đơn</button>
        <button class="nut-chinh" type="button" @click="tai_hoa_don">Tải lại</button>
      </div>
    </header>
    <section class="luoi-tom-tat">
      <div class="chi-muc-tom-tat">
        <span>Tổng hóa đơn</span>
        <strong>{{ danh_sach_hoa_don.length }}</strong>
      </div>
      <div class="chi-muc-tom-tat">
        <span>Đã thanh toán</span>
        <strong>{{ so_da_thanh_toan }}</strong>
      </div>
    </section>
    <p v-if="thong_bao_loi" class="thong-bao-loi">{{ thong_bao_loi }}</p>
    <section class="khong-gian-hoa-don">
      <HoaDonList
        :hoaDons="danh_sach_hoa_don"
        :loading="dang_tai_hoa_don"
        :selectedId="id_da_chon"
        @select="tai_chi_tiet"
      />
      <HoaDonDetail
        :selectedHoaDon="hoa_don_da_chon"
        :chiTiets="chi_tiet"
        :loading="dang_tai_chi_tiet"
      />
    </section>
  </main>
</template>
<style scoped>
.trang-hoa-don {
  min-height: 100vh;
  padding: 32px clamp(16px, 4vw, 56px);
  color: #5f3d22;
  background: linear-gradient(135deg, #f9efe0 0%, #f4e4c6 100%);
  font-family:
    Inter,
    ui-sans-serif,
    system-ui,
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    sans-serif;
}
.tieu-de-hoa-don,
.luoi-tom-tat,
.khong-gian-hoa-don {
  max-width: 1440px;
  margin-inline: auto;
}
.tieu-de-hoa-don {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr) auto;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
}
.tieu-de-hoa-don h1 {
  margin: 0;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: clamp(2.1rem, 5vw, 4rem);
  text-align: center;
  letter-spacing: 0;
}
.tieu-le {
  margin: 0 0 8px;
  color: #8b5e34;
  font-size: 0.75rem;
  font-weight: 700;
  text-align: center;
  text-transform: uppercase;
}
.nhom-hanh-dong {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.nut-chinh,
.nut-ma,
.nut-phu {
  min-height: 42px;
  border-radius: 6px;
  padding: 0 16px;
  font-weight: 700;
  border: 1px solid rgba(215, 180, 106, 0.45);
  color: #f7f2e9;
  cursor: pointer;
}
.nut-chinh {
  background: #d8a85c;
  color: #3d2814;
}
.nut-phu,
.nut-ma {
  background: #fff6df;
  color: #8b5e34;
}
.luoi-tom-tat {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}
.chi-muc-tom-tat {
  border: 1px solid #e6d2aa;
  background: rgba(255, 248, 234, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 18px;
  box-shadow: 0 8px 20px rgba(103, 72, 32, 0.06);
}
.chi-muc-tom-tat span {
  display: block;
  margin-bottom: 6px;
  color: #8f6b46;
  font-size: 0.78rem;
}
.chi-muc-tom-tat strong {
  font-size: clamp(1.4rem, 3vw, 2rem);
}
.khong-gian-hoa-don {
  display: grid;
  grid-template-columns: minmax(280px, 380px) 1fr;
  gap: 18px;
}
.thong-bao-loi {
  max-width: 1440px;
  margin: 14px auto;
  color: #ffd8d8;
}
@media (max-width: 900px) {
  .tieu-de-hoa-don {
    grid-template-columns: 1fr;
  }
  .tieu-de-hoa-don h1,
  .tieu-le {
    text-align: left;
  }
  .luoi-tom-tat,
  .khong-gian-hoa-don {
    grid-template-columns: 1fr;
  }
}
</style>
