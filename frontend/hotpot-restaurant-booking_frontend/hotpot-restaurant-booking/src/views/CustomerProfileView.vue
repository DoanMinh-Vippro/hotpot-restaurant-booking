<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/AuthStore'
import { getAllKhachHang, updateKhachHang } from '@/api/khachhang'
import HoaDonApi from '@/api/HoaDonApi'
import DatBanApi from '@/api/DatBanApi'
import DatBanQuanLyApi from '@/api/DatBanQuanLy'
import type { HoaDon, HoaDonChiTiet } from '@/api/HoaDonApi'
import { printInvoiceReceipt } from '@/utils/printInvoice'

const authStore = useAuthStore()
const router = useRouter()

// Dữ liệu khách hàng
const customerInfo = ref<any>(null)
const invoiceHistory = ref<any[]>([])
const invoiceDetails = ref<Record<number, HoaDonChiTiet[]>>({})
const loading = ref(true)
const isEditing = ref(false)
const editForm = ref<any>(null)
const activeProfileTab = ref<'info' | 'invoices' | 'bookings'>('info')

const customerBookings = ref<any[]>([])
const bookingLoading = ref(false)

// Dữ liệu từ form
const formData = ref({
  tenKhachHang: '',
  soDienThoai: '',
  email: '',
  diaChi: '',
  gioiTinh: true
})

const pageSize = ref(10)
const currentPage = ref(1)

// Computed properties
const paginatedInvoices = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return invoiceHistory.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(invoiceHistory.value.length / pageSize.value)
})

// Load dữ liệu
const normalizeCustomerIdentity = (source: any) => ({
  khachHangId: source?.khachHangId ?? source?.idKhachHang ?? source?.id ?? source?.customerId ?? null,
  tenKhachHang: source?.tenKhachHang ?? source?.hoTen ?? source?.name ?? source?.fullName ?? null,
  soDienThoai: source?.soDienThoai ?? source?.phone ?? source?.sdt ?? null,
  email: source?.email ?? source?.mail ?? null,
  diaChi: source?.diaChi ?? source?.address ?? null,
  gioiTinh: source?.gioiTinh ?? source?.gender ?? null,
  maKhachHang: source?.maKhachHang ?? source?.ma ?? source?.customerCode ?? null,
})

const matchesCustomerRecord = (record: any, identity: any) => {
  const normalizePhone = (value: any) => (value ? String(value).replace(/\D/g, '') : '')
  const normalizeText = (value: any) => (value ? String(value).toLowerCase() : '')

  const recordId = record?.idKhachHang ?? record?.id ?? record?.khachHangId ?? record?.customerId ?? null
  if (identity.khachHangId && recordId && Number(recordId) === Number(identity.khachHangId)) return true

  const phone = normalizePhone(identity.soDienThoai)
  const recordPhone = normalizePhone(record?.sdtKhachHang ?? record?.soDienThoai ?? record?.phone ?? record?.sdt)
  if (phone && recordPhone && (recordPhone === phone || recordPhone.endsWith(phone) || phone.endsWith(recordPhone))) return true

  const code = normalizeText(identity.maKhachHang)
  const recordCode = normalizeText(record?.maKhachHang ?? record?.customerCode ?? record?.ma)
  if (code && recordCode && (recordCode === code || code.includes(recordCode) || recordCode.includes(code))) return true

  const name = normalizeText(identity.tenKhachHang)
  const recordName = normalizeText(record?.tenKhachHang ?? record?.hoTen ?? record?.customerName ?? record?.name)
  if (name && recordName && (recordName.includes(name) || name.includes(recordName))) return true

  return false
}

const loadCustomerInfo = async () => {
  try {
    const identity = normalizeCustomerIdentity(authStore.customerInfo)
    const res = await getAllKhachHang()
    const allCustomers = res.data || []

    let foundCustomer = identity.khachHangId
      ? allCustomers.find((kh: any) => Number(kh.idKhachHang) === Number(identity.khachHangId))
      : null

    if (!foundCustomer) {
      foundCustomer = allCustomers.find((kh: any) => matchesCustomerRecord(kh, identity)) || null
    }

    customerInfo.value = foundCustomer || null

    formData.value = {
      tenKhachHang: authStore.customerInfo.tenKhachHang || customerInfo.value?.tenKhachHang || '',
      soDienThoai: authStore.customerInfo.soDienThoai || customerInfo.value?.soDienThoai || '',
      email: authStore.customerInfo.email || customerInfo.value?.email || '',
      diaChi: authStore.customerInfo.diaChi || customerInfo.value?.diaChi || '',
      gioiTinh: (authStore.customerInfo.gioiTinh ?? customerInfo.value?.gioiTinh) ?? true
    }
  } catch (error) {
    console.error('Lỗi khi tải thông tin khách hàng:', error)
  }
}

const loadInvoiceHistory = async () => {
  try {
    const identity = normalizeCustomerIdentity(authStore.customerInfo)
    let invoices: any[] = []

    if (identity.khachHangId) {
      try {
        const res = await HoaDonApi.getByKhachHangId(Number(identity.khachHangId))
        invoices = res.data || []
      } catch (error) {
        console.warn('Không lấy được hóa đơn qua khách hàng ID, thử lọc từ toàn bộ dữ liệu:', error)
      }
    }

    if (invoices.length === 0) {
      const res = await HoaDonApi.getDanhSach()
      invoices = (res.data || []).filter((invoice: any) => matchesCustomerRecord(invoice, identity))
    }

    invoiceHistory.value = invoices
    currentPage.value = 1

    const detailEntries = await Promise.all(
      invoices.map(async (invoice: any) => {
        try {
          const detailRes = await HoaDonApi.getChiTiet(invoice.idHoaDon)
          return [invoice.idHoaDon, detailRes.data || []] as const
        } catch (error) {
          console.warn('Không lấy được chi tiết hóa đơn:', invoice.idHoaDon, error)
          return [invoice.idHoaDon, []] as const
        }
      }),
    )
    invoiceDetails.value = Object.fromEntries(detailEntries)
  } catch (error) {
    console.error('Lỗi khi tải lịch sử hoá đơn:', error)
    invoiceHistory.value = []
    invoiceDetails.value = {}
  }
}

const normalizePhone = (value: any) => (value ? String(value).replace(/\D/g, '') : '')
const normalizeText = (value: any) => (value ? String(value).trim().toLowerCase() : '')

const extractBookingTableNames = (booking: any) => {
  const names: string[] = []

  if (Array.isArray(booking?.dsBan)) {
    booking.dsBan.forEach((ban: any) => {
      const name = String(ban?.tenBan || ban?.name || ban?.ten || '').trim()
      if (name && !names.includes(name)) names.push(name)
    })
  }

  if (names.length > 0) return `${names.join(', ')} (${names.length} bàn)`
  if (booking?.tenBan) return String(booking.tenBan)
  return 'Tự động xếp'
}

const formatInvoiceTableLabel = (invoice: any) => {
  const names: string[] = []

  if (Array.isArray(invoice?.dsBan)) {
    invoice.dsBan.forEach((ban: any) => {
      const name = String(ban?.tenBan || ban?.name || ban?.ten || '').trim()
      if (name && !names.includes(name)) names.push(name)
    })
  }

  if (names.length === 0) {
    const raw = String(invoice?.tenBan || '').trim()
    if (raw) {
      const splitNames = raw.split(/[;,]/).map((item: string) => item.trim()).filter(Boolean)
      splitNames.forEach((name: string) => {
        if (name && !names.includes(name)) names.push(name)
      })
    }
  }

  if (names.length > 0) return `${names.join(', ')} (${names.length} bàn)`
  return invoice?.loaiBan || `Bàn ${invoice?.idBan ?? '-'}`
}

const matchesBookingToCustomer = (booking: any, identity: any) => {
  if (!booking) return false

  const recordId = booking?.idKhachHang ?? booking?.khachHangId ?? booking?.customerId ?? null
  if (identity.khachHangId && recordId && Number(recordId) === Number(identity.khachHangId)) return true

  const phone = normalizePhone(identity.soDienThoai)
  const recordPhone = normalizePhone(booking?.sdtKhachHang ?? booking?.soDienThoai ?? booking?.phone ?? booking?.sdt)
  if (phone && recordPhone && (recordPhone === phone || recordPhone.endsWith(phone) || phone.endsWith(recordPhone))) return true

  const code = normalizeText(identity.maKhachHang)
  const recordCode = normalizeText(booking?.maKhachHang ?? booking?.customerCode ?? booking?.ma)
  if (code && recordCode && (recordCode === code || code.includes(recordCode) || recordCode.includes(code))) return true

  const name = normalizeText(identity.tenKhachHang)
  const recordName = normalizeText(booking?.tenKhachHang ?? booking?.hoTen ?? booking?.customerName ?? booking?.name)
  if (name && recordName && (recordName.includes(name) || name.includes(recordName))) return true

  return false
}

const loadBookingHistory = async () => {
  bookingLoading.value = true
  try {
    const identity = normalizeCustomerIdentity(authStore.customerInfo)
    const custId = identity.khachHangId
    const custPhone = normalizePhone(identity.soDienThoai)
    const custCode = normalizeText(identity.maKhachHang)
    const custName = normalizeText(identity.tenKhachHang)

    if (!custId && !custPhone && !custCode && !custName) {
      customerBookings.value = []
      return
    }

    let all: any[] = []
    try {
      const res = await DatBanQuanLyApi.getAll()
      all = Array.isArray(res?.data) ? res.data : []
    } catch (adminErr) {
      console.warn('Không lấy được đặt bàn quản lý, thử endpoint đặt bàn của khách hàng:', adminErr)
      const res = await DatBanApi.getAll()
      all = Array.isArray(res?.data) ? res.data : []
    }

    customerBookings.value = all
      .filter((booking: any) => matchesBookingToCustomer(booking, identity))
      .sort((a: any, b: any) => {
        const aTime = new Date(a?.thoiGianDenDuKien || a?.ngayDat || 0).getTime()
        const bTime = new Date(b?.thoiGianDenDuKien || b?.ngayDat || 0).getTime()
        return bTime - aTime
      })
  } catch (err) {
    console.error('Lỗi khi tải lịch sử đặt bàn:', err)
    customerBookings.value = []
  } finally {
    bookingLoading.value = false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([loadCustomerInfo(), loadInvoiceHistory(), loadBookingHistory()])
  } finally {
    loading.value = false
  }
}

const refreshHistory = async () => {
  await loadData()
}

onMounted(() => {
  if (!authStore.isAuthenticated || !authStore.customerInfo.khachHangId) {
    router.push('/auth')
    return
  }
  loadData()
})

// Handle edit
const toggleEdit = () => {
  isEditing.value = !isEditing.value
}

const handleSave = async () => {
  if (!formData.value.tenKhachHang || !formData.value.soDienThoai) {
    alert('Vui lòng nhập đầy đủ tên và số điện thoại!')
    return
  }

  try {
    const payload = {
      tenKhachHang: formData.value.tenKhachHang,
      soDienThoai: formData.value.soDienThoai,
      email: formData.value.email || null,
      diaChi: formData.value.diaChi || null,
      gioiTinh: formData.value.gioiTinh
    }
    
    await updateKhachHang(authStore.customerInfo.khachHangId!, payload)
    
    // Cập nhật store
    authStore.setCustomerInfo({
      tenKhachHang: formData.value.tenKhachHang,
      soDienThoai: formData.value.soDienThoai,
      email: formData.value.email,
      diaChi: formData.value.diaChi,
      gioiTinh: formData.value.gioiTinh
    })
    
    alert('Cập nhật thông tin thành công!')
    isEditing.value = false
    await loadCustomerInfo()
  } catch (error) {
    console.error('Lỗi khi cập nhật:', error)
    alert('Cập nhật thất bại!')
  }
}

const handleCancel = () => {
  isEditing.value = false
  // Reset form lại
  formData.value = {
    tenKhachHang: authStore.customerInfo.tenKhachHang || customerInfo.value?.tenKhachHang || '',
    soDienThoai: authStore.customerInfo.soDienThoai || customerInfo.value?.soDienThoai || '',
    email: authStore.customerInfo.email || customerInfo.value?.email || '',
    diaChi: authStore.customerInfo.diaChi || customerInfo.value?.diaChi || '',
    gioiTinh: (authStore.customerInfo.gioiTinh ?? customerInfo.value?.gioiTinh) ?? true
  }
}

const formatCurrency = (value: any) => {
  if (!value) return '0 đ'
  const num = parseFloat(value)
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(num)
}

const formatDateTime = (dateTimeValue: any) => {
  if (!dateTimeValue) return '-'

  let date: Date
  if (Array.isArray(dateTimeValue)) {
    const [year = 0, month = 1, day = 1, hour = 0, minute = 0, second = 0] = dateTimeValue
    date = new Date(year, month - 1, day, hour, minute, second)
  } else {
    date = new Date(dateTimeValue)
  }

  if (isNaN(date.getTime())) return String(dateTimeValue)

  return new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date)
}

const invoiceStatusLabel = (status: number | null | undefined) => {
  if (status === 1) return 'Đã xuất'
  if (status === 0) return 'Nháp'
  return 'Không rõ'
}

const paymentStatusLabel = (status: number | null | undefined) => {
  if (status === 1) return 'Đã thanh toán'
  if (status === 0) return 'Chưa thanh toán'
  return 'Không rõ'
}

const paymentMethodLabel = (method: number | null | undefined) => {
  if (method === 1) return 'Tiền mặt'
  if (method === 2) return 'Chuyển khoản'
  if (method === 3) return 'Thẻ'
  if (method === 4) return 'Kết hợp'
  return 'Chưa có'
}

const invoiceItemName = (item: HoaDonChiTiet) => {
  return item.tenMon ?? item.tenCombo ?? 'Món chưa đặt tên'
}

const bookingStatusLabel = (booking: any) => {
  if (booking?.trangThaiText) return booking.trangThaiText
  const status = booking?.trangThai
  if (status === 'CHO_XAC_NHAN') return 'Chờ xác nhận'
  if (status === 'DA_XAC_NHAN') return 'Đã xác nhận'
  if (status === 'DA_NHAN_BAN') return 'Đã nhận bàn'
  if (status === 'DA_HUY') return 'Đã hủy'
  if (status === 'HOAN_THANH') return 'Hoàn thành'
  return status || 'Không rõ'
}

const bookingStatusClass = (booking: any) => {
  const status = booking?.trangThai
  if (status === 'DA_XAC_NHAN' || status === 'DA_NHAN_BAN' || status === 'HOAN_THANH') return 'status-green'
  if (status === 'CHO_XAC_NHAN') return 'status-yellow'
  return 'status-red'
}

const bookingDepositStatusLabel = (booking: any) => {
  if (booking?.trangThaiCocText) return booking.trangThaiCocText
  const status = booking?.trangThaiCoc
  if (status === 'DA_COC') return 'Đã cọc'
  if (status === 'DA_HOAN_COC') return 'Đã hoàn cọc'
  if (status === 'KHONG_HOAN_COC') return 'Không hoàn cọc'
  if (status === 'CHUA_COC') return 'Chưa cọc'
  return 'Chưa có'
}

const bookingDepositStatusClass = (booking: any) => {
  const status = booking?.trangThaiCoc
  if (status === 'DA_COC') return 'status-green'
  if (status === 'DA_HOAN_COC') return 'status-yellow'
  return 'status-red'
}

const bookingPaymentMethodLabel = (booking: any) => {
  const method = booking?.phuongThucThanhToan
  if (method === 'CHUYEN_KHOAN') return 'Chuyển khoản'
  if (method === 'VNPAY') return 'VNPay'
  if (method === 'TIEN_MAT') return 'Tiền mặt'
  return ''
}

const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const exportInvoicePdf = (invoice: HoaDon) => {
  printInvoiceReceipt(invoice, invoiceDetails.value[invoice.idHoaDon] || [])
}
</script>

<template>
  <div class="profile-container">
    <!-- Back Button -->
    <div class="back-button" @click="router.push('/')">
      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <line x1="19" y1="12" x2="5" y2="12"></line>
        <polyline points="12 19 5 12 12 5"></polyline>
      </svg>
      QUAY LẠI TRANG CHỦ
    </div>

    <!-- Header -->
    <div class="profile-header">
      <div class="header-content">
        <h1>HỒ SƠ CÁ NHÂN</h1>
        <p>Quản lý thông tin và lịch sử giao dịch của bạn</p>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="loading-container">
      <p>Đang tải dữ liệu...</p>
    </div>

    <!-- Main content -->
    <div v-else class="profile-content">
      <div class="profile-tabs">
        <button
          :class="['profile-tab', { active: activeProfileTab === 'info' }]"
          @click="activeProfileTab = 'info'"
        >
          Thông tin khách hàng
        </button>
        <button
          :class="['profile-tab', { active: activeProfileTab === 'invoices' }]"
          @click="activeProfileTab = 'invoices'"
        >
          Lịch sử hoá đơn
        </button>
        <button class="profile-tab refresh-btn" @click="refreshHistory">
          🔄 Tải lại
        </button>
        <button
          :class="['profile-tab', { active: activeProfileTab === 'bookings' }]"
          @click="activeProfileTab = 'bookings'"
        >
          Lịch sử đặt bàn
        </button>
      </div>

      <div class="profile-main">
        <!-- Customer Info Section -->
        <section v-if="activeProfileTab === 'info'" class="info-section">
          <div class="section-header">
            <h2>THÔNG TIN CÁ NHÂN</h2>
            <button 
              v-if="!isEditing"
              @click="toggleEdit" 
              class="btn-edit"
            >
              ✏️ CHỈNH SỬA
            </button>
          </div>

          <!-- View Mode -->
          <div v-if="!isEditing && customerInfo" class="info-display">
            <div class="info-grid">
              <div class="info-box">
                <span class="label">Mã Khách Hàng</span>
                <span class="value">{{ authStore.customerInfo.maKhachHang || customerInfo.maKhachHang || '-' }}</span>
              </div>
              <div class="info-box">
                <span class="label">Tên</span>
                <span class="value">{{ authStore.customerInfo.tenKhachHang || customerInfo.tenKhachHang || '-' }}</span>
              </div>
              <div class="info-box">
                <span class="label">Số Điện Thoại</span>
                <span class="value">{{ authStore.customerInfo.soDienThoai || customerInfo.soDienThoai || '-' }}</span>
              </div>
              <div class="info-box">
                <span class="label">Email</span>
                <span class="value">{{ authStore.customerInfo.email || customerInfo.email || '-' }}</span>
              </div>
              <div class="info-box">
                <span class="label">Giới Tính</span>
                <span class="value">{{ (authStore.customerInfo.gioiTinh ?? customerInfo.gioiTinh) ? 'Nam' : 'Nữ' }}</span>
              </div>
              <div class="info-box">
                <span class="label">Địa Chỉ</span>
                <span class="value">{{ authStore.customerInfo.diaChi || customerInfo.diaChi || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- Edit Mode -->
          <div v-else class="info-edit-form">
            <div class="form-group">
              <label>Tên Khách Hàng *</label>
              <input 
                v-model="formData.tenKhachHang" 
                type="text" 
                placeholder="Nhập tên"
                required
              />
            </div>

            <div class="form-group">
              <label>Số Điện Thoại *</label>
              <input 
                v-model="formData.soDienThoai" 
                type="tel" 
                placeholder="Nhập số điện thoại"
                required
              />
            </div>

            <div class="form-group">
              <label>Email</label>
              <input 
                v-model="formData.email" 
                type="email" 
                placeholder="Nhập email"
              />
            </div>

            <div class="form-group">
              <label>Giới Tính</label>
              <select v-model="formData.gioiTinh">
                <option :value="true">Nam</option>
                <option :value="false">Nữ</option>
              </select>
            </div>

            <div class="form-group">
              <label>Địa Chỉ</label>
              <textarea 
                v-model="formData.diaChi" 
                placeholder="Nhập địa chỉ"
                rows="3"
              ></textarea>
            </div>

            <div class="form-actions">
              <button @click="handleSave" class="btn-save">💾 LƯU</button>
              <button @click="handleCancel" class="btn-cancel">❌ HỦY</button>
            </div>
          </div>
        </section>

        <!-- Invoice History Section -->
        <section v-if="activeProfileTab === 'invoices'" class="invoice-section">
          <div class="section-header">
            <h2>LỊCH SỬ HÓA ĐƠN</h2>
            <span class="invoice-count">{{ invoiceHistory.length }} hoá đơn</span>
          </div>

          <!-- Empty state -->
          <div v-if="invoiceHistory.length === 0" class="empty-state">
            <p>Bạn chưa có hoá đơn nào</p>
          </div>

          <!-- Invoice Cards -->
          <div v-else class="invoices-container">
            <div v-for="invoice in paginatedInvoices" :key="invoice.idHoaDon" class="invoice-card">
              <div class="invoice-card-header">
                <div>
                  <span class="invoice-status">{{ invoiceStatusLabel(invoice.trangThaiHoaDon) }}</span>
                  <h4 class="invoice-code">{{ invoice.maHoaDon }}</h4>
                  <p class="invoice-date">{{ formatDateTime(invoice.thoiGianXuat) }}</p>
                </div>
                <div class="invoice-total-box">
                  <span>Tổng tiền</span>
                  <strong>{{ formatCurrency(invoice.tongTien) }}</strong>
                  <button class="btn-export-pdf" @click.stop="exportInvoicePdf(invoice)">
                    🖨️ In hoá đơn
                  </button>
                </div>
              </div>

              <div class="invoice-info-grid">
                <div>
                  <span>Khách hàng</span>
                  <strong>{{ invoice.tenKhachHang || authStore.customerInfo.tenKhachHang || customerInfo?.tenKhachHang || 'Khách lẻ' }}</strong>
                </div>
                <div>
                  <span>Số điện thoại</span>
                  <strong>{{ invoice.sdtKhachHang || authStore.customerInfo.soDienThoai || customerInfo?.soDienThoai || 'Chưa có' }}</strong>
                </div>
                <div>
                  <span>Bàn</span>
                  <strong>{{ formatInvoiceTableLabel(invoice) }}</strong>
                </div>
                <div>
                  <span>Giờ vào bàn</span>
                  <strong>{{ formatDateTime(invoice.gioVaoBan || invoice.thoiGianXuat) }}</strong>
                </div>
                <div>
                  <span>Giờ rời bàn</span>
                  <strong>{{ formatDateTime(invoice.gioRoiBan) }}</strong>
                </div>
                <div>
                  <span>Nhân viên</span>
                  <strong>{{ invoice.tenNhanVien || 'Chưa có' }}</strong>
                </div>
                <div>
                  <span>Thanh toán</span>
                  <strong :class="invoice.trangThaiThanhToan === 1 ? 'status-paid-text' : 'status-pending-text'">
                    {{ paymentStatusLabel(invoice.trangThaiThanhToan) }}
                  </strong>
                </div>
                <div>
                  <span>Phương thức</span>
                  <strong>{{ paymentMethodLabel(invoice.phuongThucThanhToan) }}</strong>
                </div>
                <div>
                  <span>Mã giao dịch</span>
                  <strong>{{ invoice.maGiaoDich || 'Không có' }}</strong>
                </div>
                <div>
                  <span>Mã giảm giá</span>
                  <strong>{{ invoice.maGiamGia || 'Không có' }}</strong>
                </div>
              </div>

              <div class="invoice-money-grid">
                <div>
                  <span>Trước giảm</span>
                  <strong>{{ formatCurrency(invoice.tienTruocGiam) }}</strong>
                </div>
                <div>
                  <span>Tiền cọc</span>
                  <strong>{{ formatCurrency(invoice.tienCoc) }}</strong>
                </div>
                <div>
                  <span>Giảm giá</span>
                  <strong>{{ formatCurrency(invoice.tienGiamGia) }}</strong>
                </div>
              </div>

              <div class="invoice-detail-table-wrap">
                <div class="invoice-detail-title">Chi tiết món</div>
                <table class="invoice-detail-table">
                  <thead>
                    <tr>
                      <th>Mã</th>
                      <th>Món / combo</th>
                      <th>SL</th>
                      <th>Đơn giá</th>
                      <th>Giảm</th>
                      <th>Thành tiền</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr
                      v-for="item in invoiceDetails[invoice.idHoaDon] || []"
                      :key="item.idHoaDonChiTiet"
                    >
                      <td>{{ item.maHoaDonChiTiet }}</td>
                      <td>
                        <div>{{ invoiceItemName(item) }}</div>
                        <div v-if="item.comboItems?.length" class="combo-items">
                          Gồm: {{ item.comboItems.join(', ') }}
                        </div>
                      </td>
                      <td>{{ item.soLuong || 0 }}</td>
                      <td>{{ formatCurrency(item.giaBanTaiThoiDiem) }}</td>
                      <td>{{ formatCurrency(item.tienGiamGiaMon) }}</td>
                      <td>{{ formatCurrency(item.thanhTien) }}</td>
                    </tr>
                  </tbody>
                </table>
                <div v-if="(invoiceDetails[invoice.idHoaDon] || []).length === 0" class="invoice-empty-detail">
                  Hóa đơn này chưa có chi tiết món.
                </div>
              </div>
            </div>

            <!-- Pagination -->
            <div v-if="totalPages > 1" class="pagination">
              <button 
                @click="goToPage(currentPage - 1)"
                :disabled="currentPage === 1"
                class="btn-page"
              >
                ← Trước
              </button>
              
              <span class="page-info">
                Trang {{ currentPage }} / {{ totalPages }}
              </span>
              
              <button 
                @click="goToPage(currentPage + 1)"
                :disabled="currentPage === totalPages"
                class="btn-page"
              >
                Sau →
              </button>
            </div>
          </div>
        </section>

        <!-- Booking History Section -->
        <section v-if="activeProfileTab === 'bookings'" class="booking-section">
          <div class="section-header">
            <h2>LỊCH SỬ ĐẶT BÀN</h2>
            <span class="booking-count">{{ customerBookings.length }} lần</span>
          </div>

          <div v-if="bookingLoading" class="text-loading">Đang tải lịch sử đặt bàn...</div>
          <div v-else-if="customerBookings.length === 0" class="empty-state">
            <p>Bạn chưa có lịch sử đặt bàn nào.</p>
          </div>

          <div v-else class="bookings-container">
            <div v-for="(b, idx) in customerBookings" :key="b.idDatBan" class="booking-card">
              <div class="booking-card-header">
                <div>
                  <strong class="booking-code">Đơn #{{ b.idDatBan }} (Lần {{ Number(idx) + 1 }})</strong>
                  <div class="booking-date">{{ formatDateTime(b.thoiGianDenDuKien || b.ngayDat) }}</div>
                </div>
                <div class="booking-status">
                  <span :class="['status-pill', bookingStatusClass(b)]">● {{ bookingStatusLabel(b) }}</span>
                </div>
              </div>

              <div class="booking-detail-table-wrap">
                <table class="booking-detail-table">
                  <tbody>
                    <tr>
                      <td class="label">Ngày đặt:</td>
                      <td class="value">{{ formatDateTime(b.ngayDat && b.gioDat ? `${b.ngayDat}T${b.gioDat}` : b.ngayDat) }}</td>
                    </tr>
                    <tr>
                      <td class="label">Thời gian dự kiến:</td>
                      <td class="value">{{ formatDateTime(b.thoiGianDenDuKien || (b.ngayDat && b.gioDat ? `${b.ngayDat}T${b.gioDat}` : b.ngayDat)) }}</td>
                    </tr>
                    <tr>
                      <td class="label">Số người:</td>
                      <td class="value"><span class="badge-count">{{ b.soNguoi || 0 }} Người</span></td>
                    </tr>
                    <tr>
                      <td class="label">Bàn đã đặt:</td>
                      <td class="value">
                        <span>{{ extractBookingTableNames(b) }}</span>
                        <span v-if="b.ghiChu" class="booking-note">— "{{ b.ghiChu }}"</span>
                      </td>
                    </tr>
                    <tr>
                      <td class="label">Tiền cọc:</td>
                      <td class="value">{{ formatCurrency(b.soTienCoc) }} <span :class="['booking-deposit-status', bookingDepositStatusClass(b)]">({{ bookingDepositStatusLabel(b) }})</span></td>
                    </tr>
                    <tr v-if="b.tenCombo || bookingPaymentMethodLabel(b)">
                      <td class="label">Combo / Thanh toán:</td>
                      <td class="value">{{ [b.tenCombo, bookingPaymentMethodLabel(b)].filter(Boolean).join(' • ') }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  min-height: 100vh;
  padding: 20px;
  font-family: 'Montserrat', sans-serif;
  position: relative;
}

.back-button {
  position: absolute;
  top: 30px;
  left: 40px;
  color: #fff;
  opacity: 0.6;
  font-size: 0.75rem;
  letter-spacing: 2px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: 0.3s;
  z-index: 10;
}

.back-button:hover {
  opacity: 1;
  color: #c5a059;
}

.profile-header {
  background: linear-gradient(135deg, #c5a059 0%, #a67b3f 100%);
  color: #000;
  padding: 40px;
  border-radius: 8px;
  margin: 60px auto 40px;
  max-width: 1200px;
  text-align: center;
}

.profile-header h1 {
  margin: 0 0 10px;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 2px;
}

.profile-header p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
  letter-spacing: 0.5px;
}

.loading-container {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 16px;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-content {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.profile-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  border-bottom: 2px solid rgba(197, 160, 89, 0.25);
  padding-bottom: 10px;
}

.profile-tab {
  background: rgba(255, 255, 255, 0.04);
  color: #d8d8d8;
  border: 1px solid rgba(197, 160, 89, 0.25);
  padding: 10px 18px;
  border-radius: 6px 6px 0 0;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.5px;
  transition: all 0.25s;
}

.profile-tab:hover {
  background: rgba(197, 160, 89, 0.12);
  color: #fff;
}

.profile-tab.active {
  background: #c5a059;
  color: #111;
  border-color: #c5a059;
}

.profile-main {
  display: grid;
  gap: 30px;
}

/* Section Styles */
.info-section,
.invoice-section {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(197, 160, 89, 0.2);
  padding: 30px;
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid rgba(197, 160, 89, 0.3);
}

.section-header h2 {
  margin: 0;
  color: #c5a059;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 2px;
}

.invoice-count {
  background: rgba(197, 160, 89, 0.2);
  color: #c5a059;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.btn-edit {
  background: rgba(197, 160, 89, 0.2);
  color: #c5a059;
  border: 1px solid rgba(197, 160, 89, 0.5);
  padding: 8px 16px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s;
}

.btn-edit:hover {
  background: rgba(197, 160, 89, 0.3);
  border-color: #c5a059;
}

/* Info Display */
.info-display {
  padding: 10px 0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 15px;
}

.info-box {
  background: rgba(197, 160, 89, 0.05);
  border: 1px solid rgba(197, 160, 89, 0.15);
  padding: 15px;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-box .label {
  color: #c5a059;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 600;
}

.info-box .value {
  color: #e0e0e0;
  font-size: 14px;
  font-weight: 500;
}

/* Edit Form */
.info-edit-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  color: #c5a059;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.form-group input,
.form-group select,
.form-group textarea {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(197, 160, 89, 0.3);
  color: #e0e0e0;
  padding: 10px 12px;
  border-radius: 5px;
  font-size: 14px;
  font-family: 'Montserrat', sans-serif;
  transition: all 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.08);
  border-color: #c5a059;
  box-shadow: 0 0 0 3px rgba(197, 160, 89, 0.1);
}

.form-actions {
  grid-column: 1 / -1;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 15px;
}

.btn-save,
.btn-cancel {
  padding: 10px 24px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s;
}

.btn-save {
  background: #4caf50;
  color: white;
}

.btn-save:hover {
  background: #45a049;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.btn-cancel {
  background: rgba(244, 67, 54, 0.3);
  color: #ff6b6b;
  border: 1px solid rgba(244, 67, 54, 0.5);
}

.btn-cancel:hover {
  background: rgba(244, 67, 54, 0.4);
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
}

/* Booking History */
.booking-section {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(197, 160, 89, 0.2);
  padding: 30px;
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.bookings-container {
  display: grid;
  gap: 14px;
}

.booking-card {
  background: rgba(197, 160, 89, 0.05);
  border: 1px solid rgba(197, 160, 89, 0.15);
  border-radius: 8px;
  padding: 16px;
}

.booking-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(197, 160, 89, 0.15);
}

.booking-code {
  color: #c5a059;
  font-size: 15px;
  font-weight: 700;
}

.booking-date {
  margin-top: 4px;
  color: #999;
  font-size: 12px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.08);
}

.status-yellow {
  color: #f0c36d;
}

.status-green {
  color: #4caf50;
}

.status-red {
  color: #ff6b6b;
}

.booking-detail-table-wrap {
  overflow-x: auto;
}

.booking-detail-table {
  width: 100%;
  border-collapse: collapse;
}

.booking-detail-table td {
  padding: 8px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  vertical-align: top;
}

.booking-detail-table tr:last-child td {
  border-bottom: none;
}

.booking-detail-table .label {
  width: 35%;
  color: #c5a059;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.booking-detail-table .value {
  color: #f0f0f0;
  font-size: 13px;
}

.booking-note {
  color: #aaa;
  font-style: italic;
}

.booking-deposit-status {
  font-size: 12px;
  margin-left: 6px;
  font-weight: 700;
}

.badge-count {
  display: inline-block;
  background: rgba(197, 160, 89, 0.2);
  color: #c5a059;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
}

/* Invoice Cards */
.invoices-container {
  display: grid;
  gap: 12px;
  margin-bottom: 20px;
}

.invoice-card {
  background: rgba(197, 160, 89, 0.05);
  border: 1px solid rgba(197, 160, 89, 0.15);
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s;
}

.invoice-card:hover {
  background: rgba(197, 160, 89, 0.08);
  border-color: rgba(197, 160, 89, 0.3);
  box-shadow: 0 4px 12px rgba(197, 160, 89, 0.1);
}

.invoice-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(197, 160, 89, 0.15);
}

.invoice-code {
  margin: 0;
  color: #c5a059;
  font-size: 16px;
  font-weight: 600;
}

.invoice-date {
  margin: 3px 0 0;
  color: #999;
  font-size: 12px;
}

.invoice-status {
  display: inline-block;
  color: #111;
  background: #c5a059;
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 11px;
  font-weight: 700;
  margin-bottom: 7px;
}

.invoice-total-box {
  min-width: 150px;
  text-align: right;
}

.invoice-total-box span,
.invoice-info-grid span,
.invoice-money-grid span {
  display: block;
  color: #999;
  font-size: 12px;
  margin-bottom: 4px;
}

.invoice-total-box strong {
  color: #c5a059;
  font-size: 17px;
}

.btn-export-pdf {
  margin-top: 8px;
  border: none;
  border-radius: 6px;
  padding: 8px 10px;
  background: #c5a059;
  color: #111;
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-width: 110px;
}

.btn-export-pdf:hover {
  background: #dcb86b;
}

.invoice-info-grid,
.invoice-money-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 16px;
  margin-bottom: 14px;
}

.invoice-info-grid strong,
.invoice-money-grid strong {
  color: #e0e0e0;
  font-size: 13px;
  word-break: break-word;
}

.invoice-money-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  background: rgba(0, 0, 0, 0.22);
  border: 1px solid rgba(197, 160, 89, 0.15);
  border-radius: 6px;
  padding: 12px;
}

.status-paid-text {
  color: #81c784 !important;
}

.status-pending-text {
  color: #ef5350 !important;
}

.invoice-detail-table-wrap {
  overflow-x: auto;
}

.invoice-detail-title {
  color: #c5a059;
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 8px;
}

.invoice-detail-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}

.invoice-detail-table th,
.invoice-detail-table td {
  border-bottom: 1px solid rgba(197, 160, 89, 0.15);
  padding: 8px;
  text-align: left;
  vertical-align: top;
}

.invoice-detail-table th {
  color: #c5a059;
  background: rgba(197, 160, 89, 0.08);
}

.combo-items {
  color: #999;
  font-size: 11px;
  margin-top: 3px;
}

.invoice-empty-detail {
  color: #999;
  font-size: 12px;
  font-style: italic;
  padding: 8px 0;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  padding: 20px 0;
  border-top: 1px solid rgba(197, 160, 89, 0.15);
}

.btn-page {
  padding: 8px 16px;
  background: rgba(197, 160, 89, 0.2);
  color: #c5a059;
  border: 1px solid rgba(197, 160, 89, 0.4);
  border-radius: 5px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-page:hover:not(:disabled) {
  background: rgba(197, 160, 89, 0.3);
  border-color: #c5a059;
}

.btn-page:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.page-info {
  color: #999;
  font-size: 13px;
  font-weight: 500;
}

/* Responsive */
@media (max-width: 768px) {
  .profile-container {
    padding: 15px;
  }

  .back-button {
    top: 15px;
    left: 15px;
    font-size: 0.65rem;
  }

  .profile-header {
    margin-top: 50px;
    padding: 25px;
  }

  .profile-header h1 {
    font-size: 24px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .info-edit-form {
    grid-template-columns: 1fr;
  }

  .invoice-card {
    padding: 12px;
  }

  .card-details {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
