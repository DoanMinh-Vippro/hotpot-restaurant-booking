<template>
  <div class="container">
    <div class="khu-vuc-dieu-huong">
      <!-- <button class="nut-quay-lai" @click="quayLaiThucDon">
        ⬅ Quay lại Thực đơn
      </button> -->
    </div>
    
    <div class="cot-trai">
      <ComboTable
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
import ChiTietComBoApi from '../api/ChiTietComBoApi'
import ComboApi from '../api/ComBoApi' 

import Form from '../components/ChiTietComBoForm.vue'
import ComboTable from '../components/ChiTietComBoTable.vue'
import Preview from '../components/ChiTietComBoPreview.vue'
import Pagination from '../components/Pagination.vue' 

import type {
  ChiTietComBo,
  ChiTietComBoRequest,
} from '../api/ChiTietComBoApi'

const route = useRoute()
const router = useRouter()
const danhSach = ref<ChiTietComBo[]>([])
const itemChon = ref<ChiTietComBo | undefined>()
const selectedId = ref<number | null>(null)
const formRef = ref()

const bieuThucTenCombo = ref('')
const bieuThucTenMon = ref('')
const trangHienTai = ref(0)
const kichThuocTrang = ref(50) 
const tongSoTrang = ref(0)

// ---------------------------------------------------------
// 🔥 FETCH VÀ GOM NHÓM DỮ LIỆU THÀNH 1 DÒNG DUY NHẤT
// ---------------------------------------------------------
const fetchDuLieu = async () => {
  try {
    const res = await ChiTietComBoApi.searchCTCB(
      bieuThucTenCombo.value,
      bieuThucTenMon.value,
      undefined, 
      undefined, 
      trangHienTai.value,
      kichThuocTrang.value
    )
    
    const responseData = res.data as any
    let rawList: any[] = []
    
    if (responseData && responseData.content) {
      rawList = responseData.content
      tongSoTrang.value = responseData.totalPages || 0
    } else {
      rawList = Array.isArray(responseData) ? responseData : []
      tongSoTrang.value = 1
    }

    // MAP GOM NHÓM CHUẨN ĐỂ KHÔNG BỊ PHÂN TÁCH DÒNG
    const mapGomNhom = new Map<string | number, ChiTietComBo>()

    rawList.forEach((item: any) => {
      // Bóc tách linh hoạt ID Combo và Tên Combo từ nhiều kiểu response
      const rawComboId = item.idCombo ?? item.combo?.idCombo
      const rawComboTen = item.tenCombo ?? item.combo?.tenCombo ?? 'Combo'

      // Key gom nhóm chuẩn (Ép về Number để không lệch String/Number)
      const keyCombo = rawComboId ? Number(rawComboId) : rawComboTen

      // Bóc tách món ăn
      const monId = item.idMon ?? item.mon?.idMon
      const monTen = item.tenMon ?? item.mon?.tenMon

      if (!mapGomNhom.has(keyCombo)) {
        mapGomNhom.set(keyCombo, {
          ...item,
          idCombo: rawComboId ? Number(rawComboId) : item.idCombo,
          tenCombo: rawComboTen,
          danhSachMon: (monId || monTen) ? [
            {
              idMon: monId ? Number(monId) : 0,
              tenMon: monTen || 'Món ăn',
              soLuong: item.soLuong
            }
          ] : []
        })
      } else {
        const existingCombo = mapGomNhom.get(keyCombo)!
        
        if (!existingCombo.danhSachMon) {
          existingCombo.danhSachMon = []
        }

        const isExisted = existingCombo.danhSachMon.some(m => {
          if (monId && m.idMon) return Number(m.idMon) === Number(monId)
          return m.tenMon === monTen
        })

        if (!isExisted && (monId || monTen)) {
          existingCombo.danhSachMon.push({
            idMon: monId ? Number(monId) : 0,
            tenMon: monTen || 'Món ăn',
            soLuong: item.soLuong
          })
        }
      }
    })

    danhSach.value = Array.from(mapGomNhom.values())

  } catch (error) {
    console.error("Lỗi khi tải danh sách chi tiết combo phân trang:", error)
  }
}

// Hàm xử lý chuyển hướng quay về route 'thucDon'
const quayLaiThucDon = () => {
  router.push({ name: 'thucDon' })
}

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

onMounted(async () => {
  if (route.query.idCombo) {
    const idComboTuQuery = Number(route.query.idCombo)
    if (!isNaN(idComboTuQuery)) {
      
      formRef.value?.fillForm({
        idCombo: idComboTuQuery
      })

      try {
        const comboRes = await ComboApi.hienThiComBo()
        const danhSachCB = Array.isArray(comboRes.data) ? comboRes.data : (comboRes.data as any).content || []
        const comboTimThay = danhSachCB.find((c: any) => c.idCombo === idComboTuQuery)
        
        if (comboTimThay) {
          bieuThucTenCombo.value = comboTimThay.tenCombo
        }
      } catch (err) {
        console.error("Hệ thống lỗi khi bóc tách tên Combo từ API danh sách:", err)
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

const sua = (item: ChiTietComBo) => {
  itemChon.value = item
  selectedId.value = item.idChiTietCombo
  formRef.value?.fillForm(item)
}

const luu = async (payload: ChiTietComBoRequest) => {
  const isUpdate = selectedId.value !== null
  const tenHanhDong = isUpdate ? 'cập nhật' : 'thêm mới'

  if (!confirm(`Bạn có chắc chắn muốn ${tenHanhDong} thành phần chi tiết combo này?`)) return

  try {
    if (isUpdate && selectedId.value) {
      // Gọi đúng tên hàm API gốc
      await ChiTietComBoApi.updateCTCB(selectedId.value, payload)
    } else {
      // Gọi đúng tên hàm API gốc
      await ChiTietComBoApi.addCTCB(payload)
    }

    alert(`${isUpdate ? 'Cập nhật' : 'Thêm mới'} chi tiết combo thành công!`)
    themMoi()
    await fetchDuLieu()
  } catch (error: any) {
    const lỗiTừBackend = error.response?.data?.message || error.response?.data || `Lỗi hệ thống khi ${tenHanhDong}!`;
    alert(lỗiTừBackend)
  }
}

const xoa = async (id: number) => {
  try {
    // Gọi đúng tên hàm API gốc
    await ChiTietComBoApi.deleteCTCB(id)
    alert("Ngưng chi tiết combo thành công!")
    if (selectedId.value === id) themMoi()
    await fetchDuLieu()
  } catch (error: any) {
    const lỗiTừBackend = error.response?.data?.message || error.response?.data || "Không thể xoá chi tiết combo này!";
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

/* Thiết kế thanh điều hướng nút quay về thực đơn */
.khu-vuc-dieu-huong {
  grid-column: 1 / -1;
  margin-bottom: -8px;
}

.nut-quay-lai {
  background: rgba(255, 248, 234, 0.96);
  border: 1px solid #e6d2aa;
  color: #8b5e34;
  padding: 10px 18px;
  border-radius: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.nut-quay-lai:hover {
  background: #fff3d3;
  border-color: #d8a85c;
  color: #5f3d22;
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