<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import DatBanQuanLyApi from '@/api/DatBanQuanLy'
import ReservationToolbar from '@/components/DatBanQuanLy/ReservationToolbar.vue'
import ReservationTable from '@/components/DatBanQuanLy/ReservationTable.vue'
import ReservationDetailDialog from '@/components/DatBanQuanLy/ReservationDetailDialog.vue'
import TaoDatBanDialog from '@/components/DatBanQuanLy/TaoDatBanDialog.vue'
import DoiBanDialog from '@/components/DatBanQuanLy/DoiBanDialog.vue'
import DoiGioDialog from '@/components/DatBanQuanLy/DoiGioDialog.vue'
import XacNhanDialog from '@/components/DatBanQuanLy/XacNhanDialog.vue'
import TinhTrangBanDialog from '@/components/DatBanQuanLy/TinhTrangBanDialog.vue'

//===============state=======================================================================
const dsDatBan = ref([])
const dsBanTrong = ref([])
const selectedReservation = ref<any>(null)
const showDetail = ref(false)
const showAdd = ref(false)
const showDoiBan = ref(false)
const showDoiGio = ref(false)
const showConfirm = ref(false)
const showPayment = ref(false)
const paymentData = ref<any>(null)

const filter = ref({
  keyword: '',

  trangThai: '',

  tuNgay: '',

  denNgay: '',
})
const confirmTitle = ref('')
const confirmMessage = ref('')
const confirmAction = ref<Function>()
const showTinhTrangBan = ref(false)

//=========================================================================================================
const loadData = async () => {
  const res = await DatBanQuanLyApi.search({
    keyword: filter.value.keyword || undefined,
    trangThai: filter.value.trangThai || undefined,
    tuNgay: filter.value.tuNgay || undefined,
    denNgay: filter.value.denNgay || undefined,
  })

  dsDatBan.value = res.data
}

const router = useRouter()

const loadToday = async () => {
  const today = new Date().toISOString().substring(0, 10)
  filter.value.tuNgay = today
  filter.value.denNgay = today
  await loadData()
}

const handleSearch = async (data: any) => {
  filter.value = {
    ...data,
  }

  await loadData()
}

const openDetail = async (item: any) => {
  try {
    const res = await DatBanQuanLyApi.findById(item.idDatBan)
    selectedReservation.value = res.data
    showDetail.value = true
  } catch (error) {
    console.error('Lỗi lấy chi tiết:', error)
  }
}

const loadBanTrong = async (data: any) => {
  try {
    const res = await DatBanQuanLyApi.getDanhSachBanTrong(
      data.thoiGianDenDuKien,
      data.soNguoi,
      data.idDatBan,
    )

    dsBanTrong.value = res.data

    console.log('BÀN TRỐNG:', dsBanTrong.value)
  } catch (error) {
    console.error('Lỗi lấy danh sách bàn trống:', error)

    dsBanTrong.value = []
  }
}

const openCreate = () => {
  showAdd.value = true
}

const closeAllDialog = () => {
  // hàm đóng tất cả popup
  showDetail.value = false
  showAdd.value = false
  showDoiBan.value = false
  showDoiGio.value = false
  showConfirm.value = false
  selectedReservation.value = null
}

const openDoiBan = async (item: any) => {
  selectedReservation.value = item
  showDetail.value = false
  await loadBanTrong({
    thoiGianDenDuKien: item.thoiGianDenDuKien,
    soNguoi: item.soNguoi,
    idDatBan: item.idDatBan,
  })
  showDoiBan.value = true
}

const handleDoiBan = async (data: any) => {
  // luu đổi bàn
  try {
    await DatBanQuanLyApi.doiBan(selectedReservation.value.idDatBan, data)
    closeAllDialog()
    await loadData()
  } catch (error) {
    console.error('Lỗi đổi bàn:', error)
  }
}

const openDoiGio = (item: any) => {
  selectedReservation.value = item
  showDetail.value = false
  showDoiGio.value = true
}

const handleDoiGio = async (data: any) => {
  // lưu đổi giờ
  try {
    await DatBanQuanLyApi.doiGio(selectedReservation.value.idDatBan, data)

    closeAllDialog()

    await loadData()
  } catch (error) {
    console.error('Lỗi đổi giờ:', error)
  }
}

const openConfirm = (item: any) => {
  showDetail.value = false
  selectedReservation.value = item
  confirmTitle.value = 'Xác nhận đặt bàn'
  confirmMessage.value = `Xác nhận đơn đặt bàn #${item.idDatBan}?`
  confirmAction.value = async () => {
    await DatBanQuanLyApi.xacNhan(item.idDatBan)
    closeAllDialog()
    await loadData()
  }
  showConfirm.value = true
}

const handleCreate = async (data: any) => {
  // Không có combo -> tạo luôn
  if (!data.dsCombo || data.dsCombo.length === 0) {
    try {
      await DatBanQuanLyApi.add(data)

      closeAllDialog()

      await loadData()
    } catch (e) {
      console.error(e)
    }

    return
  }

  // Có combo -> mở popup thanh toán
  paymentData.value = data

  showPayment.value = true
}

const handleCheckIn = async (item: any) => {
  try {
    await DatBanQuanLyApi.checkIn(item.idDatBan)
    closeAllDialog()
    await loadData()

    const tableId = item?.dsBan?.[0]?.idBan
    const tableName = item?.dsBan?.[0]?.tenBan
    if (tableId != null) {
      router.push({
        name: 'ban-hang',
        query: {
          pendingTableId: String(tableId),
          pendingDatBanId: String(item.idDatBan),
          pendingTableName: tableName || '',
        },
      })
    }
  } catch (error) {
    console.error('Lỗi check-in:', error)
  }
}

const handleHuy = (item: any) => {
  showDetail.value = false
  selectedReservation.value = item
  confirmTitle.value = 'Huỷ đơn đặt bàn'
  confirmMessage.value = `Bạn có chắc muốn huỷ đơn #${item.idDatBan} không?`
  confirmAction.value = async () => {
    await DatBanQuanLyApi.delete(item.idDatBan)
    closeAllDialog()
    await loadData()
  }
  showConfirm.value = true
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="page-container">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h1>Quản lý đặt bàn</h1>
        <p>Quản lý đơn đặt trước, xác nhận bàn và điều chỉnh lịch đặt</p>
      </div>

      <div class="action-group">
        <button class="btn-create" @click="openCreate">+ Tạo đơn đặt bàn</button>

        <button class="btn-status" @click="showTinhTrangBan = true">Tình trạng bàn</button>
      </div>
    </div>

    <!-- Bộ lọc -->
    <ReservationToolbar
      v-model:keyword="filter.keyword"
      v-model:trangThai="filter.trangThai"
      v-model:tuNgay="filter.tuNgay"
      v-model:denNgay="filter.denNgay"
      @search="handleSearch"
      @today="loadToday"
    />

    <!-- Danh sách -->
    <ReservationTable :list="dsDatBan" @detail="openDetail" @huy="handleHuy" />

    <!-- Chi tiết -->
    <ReservationDetailDialog
      :visible="showDetail"
      :reservation="selectedReservation"
      @close="closeAllDialog"
      @xacNhan="openConfirm"
      @checkIn="handleCheckIn"
      @doiBan="openDoiBan"
      @doiGio="openDoiGio"
    />

    <!-- Tạo đơn -->
    <TaoDatBanDialog
      v-if="showAdd"
      :visible="showAdd"
      :dsBanTrong="dsBanTrong"
      @close="closeAllDialog"
      @save="handleCreate"
      @refresh="loadData"
      @check-ban="loadBanTrong"
    />

    <!-- Đổi bàn -->
    <DoiBanDialog
      :visible="showDoiBan"
      :reservation="selectedReservation"
      :dsBanTrong="dsBanTrong"
      @close="closeAllDialog"
      @save="handleDoiBan"
    />

    <!-- Đổi giờ -->
    <DoiGioDialog
      :visible="showDoiGio"
      :reservation="selectedReservation"
      @close="closeAllDialog"
      @save="handleDoiGio"
    />

    <!-- Xác nhận -->
    <XacNhanDialog
      :visible="showConfirm"
      :title="confirmTitle"
      :message="confirmMessage"
      @close="closeAllDialog"
      @confirm="confirmAction && confirmAction()"
    />
  </div>
  <!-- Tình trạng bàn -->
  <TinhTrangBanDialog :show="showTinhTrangBan" @close="showTinhTrangBan = false" />
</template>

<style scoped>
.page-container {
  min-height: 100%;
  padding: 24px;
  background: #f7f3eb;
}

/* ================= HEADER ================= */

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #4a3824;
  letter-spacing: 0.5px;
}

.page-header p {
  margin-top: 8px;
  color: #8b7658;
  font-size: 14px;
}

/* ================= ACTION BUTTONS ================= */

.action-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn-create,
.btn-status {
  border: none;
  padding: 13px 24px;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.25s;
}

/* Tạo đơn */

.btn-create {
  background: #b9975b;
  color: white;
  box-shadow: 0 8px 18px rgba(185, 151, 91, 0.25);
}

.btn-create:hover {
  background: #a27f45;
  transform: translateY(-2px);
}

/* Tình trạng bàn */

.btn-status {
  background: #4a3824;
  color: white;
  box-shadow: 0 8px 18px rgba(74, 56, 36, 0.18);
}

.btn-status:hover {
  background: #352819;
  transform: translateY(-2px);
}

/* ================= CONTENT ================= */

.page-container :deep(.toolbar) {
  margin-bottom: 24px;
}

.page-container :deep(.table-container) {
  margin-top: 24px;
}

/* ================= RESPONSIVE ================= */

@media (max-width: 900px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .action-group {
    width: 100%;
  }

  .btn-create,
  .btn-status {
    flex: 1;
  }
}
</style>
