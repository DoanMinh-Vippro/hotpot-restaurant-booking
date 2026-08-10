<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import TaiKhoanApi from '@/api/TaiKhoanApi'
import NhanVienApi from '@/api/NhanVienApi'
import ChucVuApi from '@/api/ChucVuApi'
import { createKhachHang, getAllKhachHang, getKhachHangByTaiKhoanId } from '@/api/khachhang'
import HoaDonApi from '@/api/HoaDonApi'
import DatBanApi from '@/api/DatBanApi'
import DatBanQuanLyApi from '@/api/DatBanQuanLy'
import { PERMISSION_MODULES, getPermissionsForRole, savePermissionsForRole, deletePermissionsForRole, generateRoleCode } from '@/utils/rolePermissions'
import type { HoaDonChiTiet } from '@/api/HoaDonApi'
const router = useRouter()
// Tab state (1: ADMIN, 2: NHÂN VIÊN, 3: KHÁCH HÀNG, 4: CHỨC VỤ, custom roles: 1000+id)
const activeTab = ref<number | string>(1)
const roleViewTab = ref<number | string>(1)
const loading = ref(true)
// Data states
const accounts = ref<any[]>([])
const employees = ref<any[]>([])
const customers = ref<any[]>([])
const roles = ref<any[]>([])
const showCreateAccountModal = ref(false)
const accountFormErrors = ref<Record<string, string>>({})
const createAccountForm = ref({
  id: null as number | null,
  tenDangNhap: '',
  matKhau: '',
  idChucVu: 3,
  hoTen: '',
  soDienThoai: '',
  email: '',
  diaChi: '',
  gioiTinh: true,
})
const showRoleModal = ref(false)
const roleForm = ref({
  id: null as number | null,
  maChucVu: '',
  tenChucVu: '',
  permissions: [] as string[],
})
const roleFormMode = ref<'create' | 'edit'>('create')
const roleTabBase = 1000
// Search & Sort states for each tab
const searchAdmin = ref('')
const sortAdmin = ref('id_desc')
const searchStaff = ref('')
const sortStaff = ref('id_desc')
const searchCustomer = ref('')
const sortCustomer = ref('id_desc')
const searchRole = ref('')
const sortRole = ref('id_desc')
// Modal states
const showDetailModal = ref(false)
const modalType = ref<'admin' | 'staff' | 'customer' | null>(null)
const selectedDetail = ref<any>(null)
const customerInvoices = ref<any[]>([])
const customerInvoiceDetails = ref<Record<number, HoaDonChiTiet[]>>({})
const invoiceLoading = ref(false)
const customerModalTab = ref<'info' | 'invoices' | 'bookings'>('info')
const customerBookings = ref<any[]>([])
const bookingLoading = ref(false)

const isRootAdminAccount = (acc: any) => {
  const roleName = String(acc?.tenChucVu || acc?.chucVu?.tenChucVu || '').trim().toUpperCase()
  const roleId = Number(acc?.idChucVu ?? acc?.chucVu?.idChucVu ?? acc?.chucVu?.id ?? 0)
  const accountCode = String(acc?.maTaiKhoan || '').trim().toUpperCase()
  const username = String(acc?.tenDangNhap || '').trim().toLowerCase()

  return (roleId === 1 || roleName === 'ADMIN' || roleName === 'ROLE_ADMIN') && (
    Number(acc?.id) === 1 ||
    accountCode === 'TK001' ||
    username === 'admin' ||
    username === 'tk001'
  )
}

const isProtectedAccount = (acc: any) => {
  if (!acc) return false
  const roleName = String(acc?.tenChucVu || acc?.chucVu?.tenChucVu || '').trim().toUpperCase()
  const roleId = Number(acc?.idChucVu ?? acc?.chucVu?.idChucVu ?? acc?.chucVu?.id ?? 0)

  return isRootAdminAccount(acc) || roleId === 2 || roleName === 'STAFF' || roleName === 'ROLE_STAFF'
}

const isSystemRole = (role: any) => {
  const roleId = Number(role?.id ?? role?.idChucVu ?? 0)
  const roleName = String(role?.tenChucVu || role?.name || '').trim().toUpperCase()

  return roleId === 1 || roleId === 2 || roleId === 3 || roleName === 'ADMIN' || roleName === 'USER' || roleName === 'STAFF' || roleName === 'ROLE_ADMIN' || roleName === 'ROLE_USER' || roleName === 'ROLE_STAFF'
}

const canEditRole = (role: any) => {
  const roleId = Number(role?.id ?? role?.idChucVu ?? 0)
  const roleName = String(role?.tenChucVu || role?.name || '').trim().toUpperCase()

  return !isSystemRole(role) || roleId === 2 || roleName === 'STAFF' || roleName === 'ROLE_STAFF'
}

const canDeleteRole = (role: any) => {
  return !isSystemRole(role)
}

const isAdminRoleOption = (role: any) => {
  const roleId = Number(role?.id ?? role?.idChucVu ?? 0)
  const roleName = String(role?.tenChucVu || role?.name || '').trim().toUpperCase()
  return roleId === 1 || roleName === 'ADMIN' || roleName === 'ROLE_ADMIN'
}

const availableAccountRoles = computed(() => {
  return roles.value.filter((role: any) => !isAdminRoleOption(role))
})

const loadCustomerBookings = async (khachHangId: number) => {
  bookingLoading.value = true
  try {
    let all: any[] = []
    try {
      const res = await DatBanQuanLyApi.getAll()
      all = Array.isArray(res?.data) ? res.data : []
    } catch (errAdmin) {
      console.warn('Không lấy được danh sách đặt bàn quản lý, thử endpoint khách hàng:', errAdmin)
      const res = await DatBanApi.getAll()
      all = Array.isArray(res?.data) ? res.data : []
    }

    customerBookings.value = all.filter((b: any) => Number(b.idKhachHang ?? b.khachHangId) === Number(khachHangId))
  } catch (err) {
    console.error('Không lấy được lịch sử đặt bàn của khách:', err)
    customerBookings.value = []
  } finally {
    bookingLoading.value = false
  }
}

const formatBookingTableLabel = (booking: any) => {
  const names: string[] = []

  if (Array.isArray(booking?.dsBan)) {
    booking.dsBan.forEach((ban: any) => {
      const name = String(ban?.tenBan || ban?.name || ban?.ten || '').trim()
      if (name && !names.includes(name)) names.push(name)
    })
  }

  if (names.length === 0) {
    const raw = String(booking?.tenBan || booking?.ten || '').trim()
    if (raw) {
      const splitNames = raw.split(/[;,]/).map((item: string) => item.trim()).filter(Boolean)
      splitNames.forEach((name: string) => {
        if (name && !names.includes(name)) names.push(name)
      })
    }
  }

  if (names.length > 0) return `${names.join(', ')} (${names.length} bàn)`
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

const clearCreateAccountError = (field: string) => {
  if (accountFormErrors.value[field]) {
    delete accountFormErrors.value[field]
  }
}

const validateCreateAccountForm = () => {
  accountFormErrors.value = {}
  const form = createAccountForm.value
  const username = form.tenDangNhap.trim()
  const password = form.matKhau.trim()
  const fullName = form.hoTen.trim()
  const phone = form.soDienThoai.trim()
  const email = form.email.trim()
  const address = form.diaChi.trim()

  if (!username) {
    accountFormErrors.value.tenDangNhap = 'Tên đăng nhập không được để trống'
  } else if (username.length < 3) {
    accountFormErrors.value.tenDangNhap = 'Tên đăng nhập tối thiểu 3 ký tự'
  }

  if (!password) {
    accountFormErrors.value.matKhau = 'Mật khẩu không được để trống'
  } else if (password.length < 6) {
    accountFormErrors.value.matKhau = 'Mật khẩu tối thiểu 6 ký tự'
  }

  if (!fullName) {
    accountFormErrors.value.hoTen = 'Họ và tên không được để trống'
  }

  if (!phone) {
    accountFormErrors.value.soDienThoai = 'Số điện thoại không được để trống'
  } else if (!/^[0-9]{9,11}$/.test(phone)) {
    accountFormErrors.value.soDienThoai = 'Số điện thoại phải là 9-11 chữ số'
  }

  if (!email) {
    accountFormErrors.value.email = 'Email không được để trống'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    accountFormErrors.value.email = 'Email không hợp lệ'
  }

  if (!address) {
    accountFormErrors.value.diaChi = 'Địa chỉ không được để trống'
  }

  return Object.keys(accountFormErrors.value).length === 0
}

const resetCreateAccountForm = () => {
  accountFormErrors.value = {}
  createAccountForm.value = {
    id: null,
    tenDangNhap: '',
    matKhau: '',
    idChucVu: 3,
    hoTen: '',
    soDienThoai: '',
    email: '',
    diaChi: '',
    gioiTinh: true,
  }
}

const openCreateAccountModal = () => {
  resetCreateAccountForm()
  showCreateAccountModal.value = true
}

const submitCreateAccount = async () => {
  if (!validateCreateAccountForm()) {
    return
  }

  const form = createAccountForm.value
  const roleId = Number(form.idChucVu)

  try {
    const selectedRole = roles.value.find((role: any) => Number(role.id) === roleId)
  const isUserRole = selectedRole
    ? String(selectedRole.tenChucVu || '').toUpperCase() === 'USER'
    : roleId === 3

  if (isUserRole) {
      await createKhachHang({
        tenKhachHang: form.hoTen.trim(),
        gioiTinh: form.gioiTinh,
        diaChi: form.diaChi.trim(),
        soDienThoai: form.soDienThoai.trim(),
        email: form.email.trim(),
        trangThai: true,
        taiKhoan: {
          tenDangNhap: form.tenDangNhap.trim(),
          matKhau: form.matKhau,
          trangThai: true,
          chucVu: { idChucVu: roleId },
        },
      })
    } else {
      await NhanVienApi.add({
        maNhanVien: `NV${Date.now().toString().slice(-6)}`,
        tenNhanVien: form.hoTen.trim(),
        gioiTinh: form.gioiTinh,
        soDienThoai: form.soDienThoai.trim(),
        email: form.email.trim(),
        diaChi: form.diaChi.trim(),
        trangThai: true,
        idChucVu: roleId,
        tenDangNhap: form.tenDangNhap.trim(),
        matKhau: form.matKhau,
      })
    }

    alert('Thêm tài khoản mới thành công! Mã tài khoản sẽ tự sinh bởi hệ thống.')
    showCreateAccountModal.value = false
    await loadData()
  } catch (error) {
    console.error('Thêm tài khoản mới thất bại:', error)
    alert('Không thể thêm tài khoản mới. Vui lòng kiểm tra lại dữ liệu.')
  }
}

const resetRoleForm = () => {
  roleForm.value = {
    id: null,
    maChucVu: '',
    tenChucVu: '',
    permissions: [],
  }
  roleFormMode.value = 'create'
}

const openRoleForm = (role?: any) => {
  if (role) {
    const roleName = String(role.tenChucVu || '').trim()
    const roleKey = roleName ? `ROLE_${roleName.toUpperCase()}` : ''
    roleForm.value = {
      id: role.id,
      maChucVu: role.maChucVu || '',
      tenChucVu: role.tenChucVu || '',
      permissions: roleKey ? getPermissionsForRole(roleKey) : [],
    }
    roleFormMode.value = 'edit'
  } else {
    resetRoleForm()
  }
  showRoleModal.value = true
}

const submitRole = async () => {
  const tenChucVu = roleForm.value.tenChucVu.trim()

  if (!tenChucVu) {
    alert('Vui lòng nhập tên chức vụ.')
    return
  }

  if (roleFormMode.value === 'create' || !roleForm.value.maChucVu.trim()) {
    roleForm.value.maChucVu = generateRoleCode()
  }

  const sanitizedCode = roleForm.value.maChucVu.trim().toUpperCase()
  const duplicateCode = roles.value.some(
    (role: any) =>
      String(role.maChucVu).trim().toUpperCase() === sanitizedCode &&
      String(role.id) !== String(roleForm.value.id),
  )

  if (duplicateCode && roleFormMode.value === 'create') {
    roleForm.value.maChucVu = generateRoleCode()
  }

  const oldRoleKey = roleFormMode.value === 'edit'
    ? `ROLE_${String(roles.value.find((role: any) => String(role.id) === String(roleForm.value.id))?.tenChucVu || '').trim().toUpperCase()}`
    : ''

  try {
    if (roleFormMode.value === 'create') {
      await ChucVuApi.add({
        maChucVu: roleForm.value.maChucVu.trim(),
        tenChucVu,
      })
      savePermissionsForRole(`ROLE_${tenChucVu.toUpperCase()}`, roleForm.value.permissions)
      alert('Thêm chức vụ mới thành công!')
    } else {
      await ChucVuApi.update(Number(roleForm.value.id), {
        maChucVu: roleForm.value.maChucVu.trim(),
        tenChucVu,
      })
      const newRoleKey = `ROLE_${tenChucVu.toUpperCase()}`
      if (oldRoleKey && oldRoleKey !== newRoleKey) {
        deletePermissionsForRole(oldRoleKey)
      }
      savePermissionsForRole(newRoleKey, roleForm.value.permissions)
      alert('Cập nhật chức vụ thành công!')
    }

    showRoleModal.value = false
    await loadData()
  } catch (error) {
    console.error('Lưu chức vụ thất bại:', error)
    alert('Không thể lưu chức vụ. Vui lòng kiểm tra lại dữ liệu.')
  }
}

const deleteRole = async (role: any) => {
  if ([1, 2, 3].includes(Number(role.id))) {
    alert('Không thể xóa các chức vụ mặc định của hệ thống.')
    return
  }

  if (!confirm(`Bạn có chắc chắn muốn xóa chức vụ "${role.tenChucVu}"?`)) {
    return
  }

  try {
    await ChucVuApi.delete(Number(role.id))
    alert('Xóa chức vụ thành công!')
    await loadData()
  } catch (error) {
    console.error('Xóa chức vụ thất bại:', error)
    alert('Không thể xóa chức vụ này.')
  }
}

// Convert helper
const toBoolean = (val: any) => {
  if (val === true || val === 1 || val === '1') return true
  if (val === false || val === 0 || val === '0') return false
  if (typeof val === 'string') {
    return val.toLowerCase() === 'true'
  }
  return false
}
// Load data
const loadData = async () => {
  loading.value = true
  try {
    const [resAccounts, resEmployees, resCustomers, resRoles] = await Promise.all([
      TaiKhoanApi.getAll(),
      NhanVienApi.getAll(),
      getAllKhachHang(),
      ChucVuApi.getAll(),
    ])
    accounts.value = resAccounts.data || []
    employees.value = (resEmployees.data || []).map((nv: any) => ({
      ...nv,
      gioiTinh: toBoolean(nv.gioiTinh),
      trangThai: toBoolean(nv.trangThai),
    }))
    customers.value = (resCustomers.data || []).map((kh: any) => ({
      ...kh,
      gioiTinh: toBoolean(kh.gioiTinh),
      trangThai: toBoolean(kh.trangThai),
    }))
    roles.value = resRoles.data || []

    if (!availableAccountRoles.value.some((role: any) => Number(role.id) === Number(createAccountForm.value.idChucVu))) {
      const fallbackRole = availableAccountRoles.value.find((role: any) => Number(role.id) === 3) || availableAccountRoles.value[0]
      if (fallbackRole) {
        createAccountForm.value.idChucVu = Number(fallbackRole.id)
      }
    }
  } catch (error) {
    console.error('Lỗi khi tải dữ liệu:', error)
  } finally {
    loading.value = false
  }
}
onMounted(() => {
  loadData()
})
// Correlate Employee profiles to accounts
const getEmployeeForAccount = (accId: number) => {
  return employees.value.find((e: any) => String(e.idTaiKhoan) === String(accId))
}
// Correlate Customer profiles to accounts
const getCustomerForAccount = (accId: number) => {
  return customers.value.find(
    (c: any) =>
      String(c.taiKhoan?.idTaiKhoan) === String(accId) ||
      String(c.taiKhoan?.id) === String(accId),
  )
}

const getAccountById = async (accId: number) => {
  try {
    const res = await TaiKhoanApi.findById(accId)
    return res.data || null
  } catch (err) {
    console.warn('Không lấy được tài khoản chi tiết:', err)
    return null
  }
}

const refreshEmployeeForAccount = async (accId: number) => {
  try {
    const res = await NhanVienApi.getByTaiKhoanId(accId)
    if (!res?.data) return null
    const emp = {
      ...res.data,
      gioiTinh: toBoolean(res.data.gioiTinh),
      trangThai: toBoolean(res.data.trangThai),
    }
    const idx = employees.value.findIndex((e: any) => String(e.idTaiKhoan) === String(accId))
    if (idx >= 0) {
      employees.value[idx] = emp
    } else {
      employees.value.push(emp)
    }
    return emp
  } catch (err) {
    console.warn('Không lấy được hồ sơ nhân viên theo tài khoản khi refresh:', err)
    return null
  }
}
// Helper formatting functions
const formatCurrency = (value: any) => {
  if (value === undefined || value === null || value === '') return '0 đ'
  return Number(value).toLocaleString('vi-VN') + ' đ'
}
const formatDateTime = (dateTimeValue: string | number[] | null) => {
  if (!dateTimeValue) return '---'

  let date: Date
  if (Array.isArray(dateTimeValue)) {
    const [year = 0, month = 1, day = 1, hour = 0, minute = 0, second = 0] = dateTimeValue
    date = new Date(year, month - 1, day, hour, minute, second)
  } else {
    date = new Date(dateTimeValue)
  }

  if (isNaN(date.getTime())) return String(dateTimeValue)
  return `${date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} ngày ${date.toLocaleDateString('vi-VN')}`
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
  return 'Chưa có'
}
const invoiceItemName = (item: HoaDonChiTiet) => {
  return item.tenMon ?? item.tenCombo ?? 'Món chưa đặt tên'
}
// --- Tab 1: ADMIN COMPUTED ---
const filteredAdmins = computed(() => {
  let list = accounts.value.filter(
    (acc: any) => String(acc.idChucVu) === '1' || String(acc.tenChucVu).toUpperCase() === 'ADMIN',
  )

  if (searchAdmin.value.trim()) {
    const q = searchAdmin.value.toLowerCase()
    list = list.filter(
      (acc: any) =>
        (acc.tenDangNhap || '').toLowerCase().includes(q) ||
        (acc.maTaiKhoan || '').toLowerCase().includes(q),
    )
  }
  // Sort
  list = [...list].sort((a: any, b: any) => {
    if (sortAdmin.value === 'id_desc') return b.id - a.id
    if (sortAdmin.value === 'id_asc') return a.id - b.id
    if (sortAdmin.value === 'username_asc')
      return (a.tenDangNhap || '').localeCompare(b.tenDangNhap || '')
    if (sortAdmin.value === 'username_desc')
      return (b.tenDangNhap || '').localeCompare(a.tenDangNhap || '')
    return 0
  })
  return list
})
// --- Tab 2: NHÂN VIÊN COMPUTED ---
const filteredStaff = computed(() => {
  let list = accounts.value.filter(
    (acc: any) => String(acc.idChucVu) === '2' || String(acc.tenChucVu).toUpperCase() === 'STAFF',
  )

  // Map account properties with corresponding employee profile
  let mappedList = list.map((acc: any) => {
    const emp = getEmployeeForAccount(acc.id)
    return {
      account: acc,
      employee: emp,
    }
  })
  if (searchStaff.value.trim()) {
    const q = searchStaff.value.toLowerCase()
    mappedList = mappedList.filter((item: any) => {
      const accMatch =
        (item.account.tenDangNhap || '').toLowerCase().includes(q) ||
        (item.account.maTaiKhoan || '').toLowerCase().includes(q)
      const empMatch = item.employee
        ? (item.employee.tenNhanVien || '').toLowerCase().includes(q) ||
          (item.employee.maNhanVien || '').toLowerCase().includes(q) ||
          (item.employee.soDienThoai || '').includes(q) ||
          (item.employee.email || '').toLowerCase().includes(q)
        : false
      return accMatch || empMatch
    })
  }
  // Sort
  mappedList = [...mappedList].sort((a: any, b: any) => {
    if (sortStaff.value === 'id_desc') return b.account.id - a.account.id
    if (sortStaff.value === 'id_asc') return a.account.id - b.account.id
    if (sortStaff.value === 'username_asc')
      return (a.account.tenDangNhap || '').localeCompare(b.account.tenDangNhap || '')
    if (sortStaff.value === 'username_desc')
      return (b.account.tenDangNhap || '').localeCompare(a.account.tenDangNhap || '')
    if (sortStaff.value === 'name_asc') {
      const nameA = a.employee?.tenNhanVien || ''
      const nameB = b.employee?.tenNhanVien || ''
      return nameA.localeCompare(nameB)
    }
    if (sortStaff.value === 'name_desc') {
      const nameA = a.employee?.tenNhanVien || ''
      const nameB = b.employee?.tenNhanVien || ''
      return nameB.localeCompare(nameA)
    }
    return 0
  })
  return mappedList
})
// --- Tab 3: KHÁCH HÀNG COMPUTED ---
const filteredCustomers = computed(() => {
  let list = accounts.value.filter(
    (acc: any) => String(acc.idChucVu) === '3' || String(acc.tenChucVu).toUpperCase() === 'USER',
  )

  // Map account properties with corresponding customer profile
  let mappedList = list.map((acc: any) => {
    const cust = getCustomerForAccount(acc.id)
    return {
      account: acc,
      customer: cust,
    }
  })
  if (searchCustomer.value.trim()) {
    const q = searchCustomer.value.toLowerCase()
    mappedList = mappedList.filter((item: any) => {
      const accMatch =
        (item.account.tenDangNhap || '').toLowerCase().includes(q) ||
        (item.account.maTaiKhoan || '').toLowerCase().includes(q)
      const custMatch = item.customer
        ? (item.customer.tenKhachHang || '').toLowerCase().includes(q) ||
          (item.customer.maKhachHang || '').toLowerCase().includes(q) ||
          (item.customer.soDienThoai || '').includes(q) ||
          (item.customer.email || '').toLowerCase().includes(q)
        : false
      return accMatch || custMatch
    })
  }
  // Sort
  mappedList = [...mappedList].sort((a: any, b: any) => {
    if (sortCustomer.value === 'id_desc') return b.account.id - a.account.id
    if (sortCustomer.value === 'id_asc') return a.account.id - b.account.id
    if (sortCustomer.value === 'username_asc')
      return (a.account.tenDangNhap || '').localeCompare(b.account.tenDangNhap || '')
    if (sortCustomer.value === 'username_desc')
      return (b.account.tenDangNhap || '').localeCompare(a.account.tenDangNhap || '')
    if (sortCustomer.value === 'name_asc') {
      const nameA = a.customer?.tenKhachHang || ''
      const nameB = b.customer?.tenKhachHang || ''
      return nameA.localeCompare(nameB)
    }
    if (sortCustomer.value === 'name_desc') {
      const nameA = a.customer?.tenKhachHang || ''
      const nameB = b.customer?.tenKhachHang || ''
      return nameB.localeCompare(nameA)
    }
    return 0
  })
  return mappedList
})
const customRoleTabs = computed(() => {
  return roles.value
    .filter((role: any) => ![1, 2, 3].includes(Number(role.id)))
    .map((role: any) => ({
      ...role,
      tabKey: roleTabBase + Number(role.id),
    }))
})

const activeRoleInfo = computed(() => {
  if (typeof roleViewTab.value === 'number' && roleViewTab.value >= roleTabBase) {
    const roleId = roleViewTab.value - roleTabBase
    return roles.value.find((role: any) => String(role.id) === String(roleId)) || null
  }

  if (typeof roleViewTab.value === 'number' && [1, 2, 3].includes(roleViewTab.value)) {
    const roleId = roleViewTab.value
    return roles.value.find((role: any) => String(role.id) === String(roleId)) || null
  }

  return null
})

const filteredRoleAccounts = computed(() => {
  const role = activeRoleInfo.value
  if (!role) return []
  const roleId = Number(role.id)
  let list = accounts.value.filter((acc: any) => String(acc.idChucVu) === String(roleId))

  if (searchRole.value.trim()) {
    const q = searchRole.value.toLowerCase()
    list = list.filter(
      (acc: any) =>
        (acc.tenDangNhap || '').toLowerCase().includes(q) ||
        (acc.maTaiKhoan || '').toLowerCase().includes(q) ||
        (acc.tenChucVu || '').toLowerCase().includes(q),
    )
  }

  return [...list].sort((a: any, b: any) => {
    if (sortRole.value === 'id_desc') return b.id - a.id
    if (sortRole.value === 'id_asc') return a.id - b.id
    if (sortRole.value === 'username_asc')
      return (a.tenDangNhap || '').localeCompare(b.tenDangNhap || '')
    if (sortRole.value === 'username_desc')
      return (b.tenDangNhap || '').localeCompare(a.tenDangNhap || '')
    return 0
  })
})

const filteredRoles = computed(() => {
  let list = [...roles.value]
  if (searchRole.value.trim()) {
    const q = searchRole.value.toLowerCase()
    list = list.filter(
      (role: any) =>
        (role.maChucVu || '').toLowerCase().includes(q) ||
        (role.tenChucVu || '').toLowerCase().includes(q),
    )
  }

  return [...list].sort((a: any, b: any) => {
    if (sortRole.value === 'id_desc') return b.id - a.id
    if (sortRole.value === 'id_asc') return a.id - b.id
    if (sortRole.value === 'name_asc')
      return (a.tenChucVu || '').localeCompare(b.tenChucVu || '')
    if (sortRole.value === 'name_desc')
      return (b.tenChucVu || '').localeCompare(a.tenChucVu || '')
    return 0
  })
})
// Detail viewers
const viewAdminDetail = (acc: any) => {
  const emp = getEmployeeForAccount(acc.id)
  selectedDetail.value = { account: acc, employee: emp }
  modalType.value = 'admin'
  showDetailModal.value = true
}
const viewStaffDetail = async (item: any) => {
  selectedDetail.value = item
  modalType.value = 'staff'
  showDetailModal.value = true

  const accId = selectedDetail.value?.account?.id
  if (!accId) {
    return
  }

  const freshAccount = await getAccountById(accId)
  if (freshAccount) {
    selectedDetail.value.account = {
      ...selectedDetail.value.account,
      ...freshAccount,
    }
  }

  selectedDetail.value.employee = await refreshEmployeeForAccount(accId)
}
const viewCustomerDetail = async (item: any) => {
  // Set initial selection and open modal
  selectedDetail.value = item
  modalType.value = 'customer'
  customerModalTab.value = 'info'
  showDetailModal.value = true

  // Ensure we have the customer profile object available. If not, fetch account detail
  const normalizeCustomer = (c: any) => {
    if (!c) return null
    const id = c.idKhachHang ?? c.id ?? c.khachHangId ?? c.customerId ?? c.id_khach_hang
    const ten = c.tenKhachHang ?? c.hoTen ?? c.name ?? c.fullName ?? c.ten
    const ma = c.maKhachHang ?? c.ma ?? c.customerCode ?? c.ma_khach_hang
    const sdt = c.soDienThoai ?? c.phone ?? c.sdt
    const email = c.email ?? c.mail
    const diaChi = c.diaChi ?? c.address ?? c.dia_chi
    let gioiTinh = c.gioiTinh ?? c.gender
    if (typeof gioiTinh === 'string') {
      if (['m', 'male'].includes(String(gioiTinh).toLowerCase())) gioiTinh = true
      else if (['f', 'female'].includes(String(gioiTinh).toLowerCase())) gioiTinh = false
    }
    if (gioiTinh === '1' || gioiTinh === 1) gioiTinh = true
    if (gioiTinh === '0' || gioiTinh === 0) gioiTinh = false
    return {
      idKhachHang: id,
      tenKhachHang: ten ?? '',
      maKhachHang: ma ?? '',
      soDienThoai: sdt ?? '',
      email: email ?? '',
      diaChi: diaChi ?? '',
      gioiTinh: typeof gioiTinh === 'boolean' ? gioiTinh : null,
      trangThai: c.trangThai ?? null,
      _raw: c,
    }
  }

  if (!selectedDetail.value?.customer) {
    const accId = selectedDetail.value?.account?.id || selectedDetail.value?.id
    if (accId) {
      try {
        const accRes = await TaiKhoanApi.findById(accId)
        const accData = accRes.data || {}
        let foundCustomer = accData.khachHang || accData.customer || accData.khach_hang || null
        if (!foundCustomer) {
          const found = customers.value.find(
            (c: any) =>
              c.taiKhoan?.idTaiKhoan === accId ||
              c.taiKhoan?.id === accId,
          )
          if (found) {
            foundCustomer = found
          } else {
            try {
              const res = await getAllKhachHang()
              const all = res.data || []
              const f2 = all.find(
                (c: any) =>
                  c.taiKhoan?.idTaiKhoan === accId ||
                  c.taiKhoan?.id === accId,
              )
              if (f2) {
                foundCustomer = f2
              }
            } catch (err) {
              console.error('Không thể tải lại danh sách khách hàng để tìm hồ sơ:', err)
            }
          }
        }
        if (!foundCustomer) {
          try {
            const res = await getKhachHangByTaiKhoanId(accId)
            foundCustomer = res.data || null
          } catch (err) {
            console.warn('Không tìm thấy khách hàng qua tài khoản ID:', err)
          }
        }
        selectedDetail.value.customer = normalizeCustomer(foundCustomer)
      } catch (err) {
        console.error('Không thể lấy chi tiết tài khoản từ backend:', err)
      }
    }
  } else {
    // If already present, normalize for consistent template fields
    selectedDetail.value.customer = normalizeCustomer(selectedDetail.value.customer)
  }

  // Resolve customer id robustly (handle different backend field names)
  const cust = selectedDetail.value?.customer
  const khachHangId =
    cust?.idKhachHang ?? cust?.id ?? cust?.khachHangId ?? cust?.customerId ?? null

  if (khachHangId) {
    invoiceLoading.value = true
    customerInvoices.value = []
    customerInvoiceDetails.value = {}
    try {
      const res = await HoaDonApi.getByKhachHangId(Number(khachHangId))
      const invoices = res.data || []
      customerInvoices.value = invoices

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
      customerInvoiceDetails.value = Object.fromEntries(detailEntries)
    } catch (error) {
      console.error('Không lấy được lịch sử hoá đơn:', error)
      customerInvoices.value = []
      customerInvoiceDetails.value = {}
    } finally {
      invoiceLoading.value = false
    }
  } else {
    customerInvoices.value = []
    customerInvoiceDetails.value = {}
  }
      // Load bookings for this customer (if backend supports)
      if (khachHangId) {
        await loadCustomerBookings(Number(khachHangId))
      } else {
        customerBookings.value = []
      }
}
const closeDetailModal = () => {
  showDetailModal.value = false
  selectedDetail.value = null
  modalType.value = null
}
// Lock/Unlock Account function (Tab 3)
const handleToggleLock = async (item: any) => {
  const currentStatus = item.account.trangThai
  const actionText = currentStatus ? 'KHÓA' : 'MỞ KHÓA'

  if (
    confirm(
      `Bạn có chắc chắn muốn ${actionText} tài khoản của khách hàng "${item.customer?.tenKhachHang || item.account.tenDangNhap}"?`,
    )
  ) {
    try {
      await TaiKhoanApi.update(item.account.id, {
        trangThai: !currentStatus,
      })
      alert(`${actionText} tài khoản thành công!`)
      await loadData()
    } catch (error) {
      console.error('Lỗi khi thay đổi trạng thái tài khoản:', error)
      alert('Thao tác thất bại!')
    }
  }
}
const removeFromLocalState = (accountId: number) => {
  accounts.value = accounts.value.filter((acc: any) => Number(acc.id) !== Number(accountId))
  employees.value = employees.value.filter((nv: any) => Number(nv.idTaiKhoan) !== Number(accountId))
  customers.value = customers.value.filter((kh: any) => {
    const khAccountId = kh?.taiKhoan?.idTaiKhoan ?? kh?.taiKhoan?.id ?? null
    return Number(khAccountId) !== Number(accountId)
  })
}

const handleDeleteAccount = async (id: number) => {
  if (!id) {
    alert('Không tìm thấy tài khoản để xóa.')
    return
  }

  const account = accounts.value.find((acc: any) => Number(acc.id) === Number(id))
  if (isProtectedAccount(account)) {
    alert('Tài khoản này được bảo vệ khỏi thao tác xóa từ giao diện.')
    return
  }

  if (confirm('Bạn có chắc chắn muốn xóa tài khoản này?')) {
    try {
      await TaiKhoanApi.delete(id)
      removeFromLocalState(id)
      alert('Xóa tài khoản thành công!')
      await loadData()
    } catch (error) {
      console.error('Xóa thất bại:', error)
      alert('Không thể xóa tài khoản này!')
    }
  }
}
</script>
<template>
  <div class="account-mgmt-page">
    <!-- Page Header -->
    <div class="page-header-wrapper">
      <h2>👤 QUẢN LÝ TÀI KHOẢN HỆ THỐNG</h2>
      <button class="btn-add-account" @click="openCreateAccountModal">➕ THÊM TÀI KHOẢN</button>
    </div>

    <hr class="line-break" />
    <!-- Navigation Tabs -->
    <div class="tabs-navigation">
      <button :class="['tab-btn', { active: activeTab === 1 }]" @click="activeTab = 1">
        👑 ADMIN
      </button>
      <button :class="['tab-btn', { active: activeTab === 2 }]" @click="activeTab = 2">
        💼 NHÂN VIÊN
      </button>
      <button :class="['tab-btn', { active: activeTab === 3 }]" @click="activeTab = 3">
        👥 KHÁCH HÀNG
      </button>
      <button :class="['tab-btn', { active: activeTab === 4 }]" @click="activeTab = 4">
        ⚙️ CHỨC VỤ
      </button>
    </div>
    <!-- Spinner Loading -->
    <div v-if="loading" class="text-loading">
      🔄 Đang tải và cấu trúc lại thông tin từ hệ thống...
    </div>
    <div v-else class="tab-content-wrapper">
      <!-- ================= TAB 1: ADMIN ================= -->
      <div v-if="activeTab === 1" class="tab-pane">
        <div class="filters-row">
          <div class="search-box">
            <input
              v-model="searchAdmin"
              type="text"
              placeholder="Tìm tên đăng nhập hoặc mã tài khoản..."
            />
          </div>
          <div class="sort-box">
            <label>Sắp xếp:</label>
            <select v-model="sortAdmin" class="select-classic">
              <option value="id_desc">Mới nhất (Default)</option>
              <option value="id_asc">Cũ nhất</option>
              <option value="username_asc">Tên đăng nhập (A-Z)</option>
              <option value="username_desc">Tên đăng nhập (Z-A)</option>
            </select>
          </div>
        </div>
        <div class="table-container">
          <table class="table-classic">
            <thead>
              <tr>
                <th style="width: 80px; text-align: center">ID</th>
                <th style="width: 150px">Mã Tài Khoản</th>
                <th>Tên Đăng Nhập</th>
                <th style="width: 180px; text-align: center">Trạng Thái</th>
                <th style="width: 250px; text-align: center">Hành Động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="acc in filteredAdmins" :key="acc.id">
                <td style="text-align: center; color: #888">{{ acc.id }}</td>
                <td style="font-weight: bold; color: #aaa">{{ acc.maTaiKhoan }}</td>
                <td class="highlight-text">{{ acc.tenDangNhap }}</td>
                <td style="text-align: center; font-weight: bold">
                  <span :class="acc.trangThai ? 'status-green' : 'status-red'">
                    {{ acc.trangThai ? 'Hoạt động' : 'Ngừng hoạt động' }}
                  </span>
                </td>
                <td style="text-align: center">
                  <button class="btn-action view" @click="viewAdminDetail(acc)">
                    Xem chi tiết
                  </button>
                  <button
                    v-if="!isRootAdminAccount(acc)"
                    class="btn-action delete"
                    @click="handleDeleteAccount(acc.id)"
                  >
                    Xóa
                  </button>
                </td>
              </tr>
              <tr v-if="filteredAdmins.length === 0">
                <td colspan="5" style="text-align: center; color: #999; padding: 30px 0">
                  📭 Không tìm thấy tài khoản Admin nào trùng khớp.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <!-- ================= TAB 2: NHÂN VIÊN ================= -->
      <div v-if="activeTab === 2" class="tab-pane">
        <div class="filters-row">
          <div class="search-box">
            <input
              v-model="searchStaff"
              type="text"
              placeholder="Tìm tài khoản, tên nhân viên, SĐT, email..."
            />
          </div>
          <div class="sort-box">
            <label>Sắp xếp:</label>
            <select v-model="sortStaff" class="select-classic">
              <option value="id_desc">Mới nhất (Default)</option>
              <option value="id_asc">Cũ nhất</option>
              <option value="username_asc">Tên đăng nhập (A-Z)</option>
              <option value="username_desc">Tên đăng nhập (Z-A)</option>
              <option value="name_asc">Tên nhân viên (A-Z)</option>
              <option value="name_desc">Tên nhân viên (Z-A)</option>
            </select>
          </div>
        </div>
        <div class="table-container">
          <table class="table-classic">
            <thead>
              <tr>
                <th style="width: 80px; text-align: center">ID</th>
                <th>Tên Đăng Nhập</th>
                <th>Họ Tên Nhân Viên</th>
                <th style="width: 140px">Số Điện Thoại</th>
                <th>Chức Vụ</th>
                <th style="width: 150px; text-align: center">Trạng Thái TK</th>
                <th style="width: 250px; text-align: center">Hành Động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in filteredStaff" :key="item.account.id">
                <td style="text-align: center; color: #888">{{ item.account.id }}</td>
                <td class="highlight-text">{{ item.account.tenDangNhap }}</td>
                <td style="color: #fff; font-weight: 500">
                  {{ item.employee?.tenNhanVien || '---' }}
                </td>
                <td style="color: #c5a059">{{ item.employee?.soDienThoai || '---' }}</td>
                <td style="font-style: italic; color: #999">{{ item.account.tenChucVu }}</td>
                <td style="text-align: center; font-weight: bold">
                  <span :class="item.account.trangThai ? 'status-green' : 'status-red'">
                    {{ item.account.trangThai ? 'Hoạt động' : 'Bị khóa' }}
                  </span>
                </td>
                <td style="text-align: center">
                  <button class="btn-action view" @click="viewStaffDetail(item)">
                    Xem chi tiết
                  </button>
                  <button
                    v-if="!isProtectedAccount(item.account)"
                    class="btn-action delete"
                    @click="handleDeleteAccount(item.account.id)"
                  >
                    Xóa
                  </button>
                </td>
              </tr>
              <tr v-if="filteredStaff.length === 0">
                <td colspan="7" style="text-align: center; color: #999; padding: 30px 0">
                  📭 Không tìm thấy tài khoản nhân viên nào.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <!-- ================= TAB 3: KHÁCH HÀNG ================= -->
      <div v-if="activeTab === 3" class="tab-pane">
        <div class="filters-row">
          <div class="search-box">
            <input
              v-model="searchCustomer"
              type="text"
              placeholder="Tìm tài khoản, tên khách hàng, SĐT, email..."
            />
          </div>
          <div class="sort-box">
            <label>Sắp xếp:</label>
            <select v-model="sortCustomer" class="select-classic">
              <option value="id_desc">Mới nhất (Default)</option>
              <option value="id_asc">Cũ nhất</option>
              <option value="username_asc">Tên đăng nhập (A-Z)</option>
              <option value="username_desc">Tên đăng nhập (Z-A)</option>
              <option value="name_asc">Tên khách hàng (A-Z)</option>
              <option value="name_desc">Tên khách hàng (Z-A)</option>
            </select>
          </div>
        </div>
        <div class="table-container">
          <table class="table-classic">
            <thead>
              <tr>
                <th style="width: 80px; text-align: center">ID</th>
                <th>Tên Đăng Nhập</th>
                <th>Mã Khách Hàng</th>
                <th>Tên Khách Hàng</th>
                <th style="width: 150px">Số Điện Thoại</th>
                <th>Email</th>
                <th style="width: 140px; text-align: center">Trạng Thái TK</th>
                <th style="width: 250px; text-align: center">Hành Động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in filteredCustomers" :key="item.account.id">
                <td style="text-align: center; color: #888">{{ item.account.id }}</td>
                <td class="highlight-text">{{ item.account.tenDangNhap }}</td>
                <td style="color: #aaa; font-weight: 500">{{ item.customer?.maKhachHang || '---' }}</td>
                <td style="color: #fff; font-weight: 500">{{ item.customer?.tenKhachHang || '---' }}</td>
                <td style="color: #c5a059">{{ item.customer?.soDienThoai || '---' }}</td>
                <td style="color: #599fff; font-size: 13px">{{ item.customer?.email || '---' }}</td>
                <td style="text-align: center; font-weight: bold">
                  <span :class="item.account.trangThai ? 'status-green' : 'status-red'">
                    {{ item.account.trangThai ? 'Hoạt động' : 'Bị Khóa' }}
                  </span>
                </td>
                <td style="text-align: center">
                  <button class="btn-action view" @click="viewCustomerDetail(item)">
                    Chi tiết
                  </button>
                  <button
                    :class="['btn-action', item.account.trangThai ? 'lock' : 'unlock']"
                    @click="handleToggleLock(item)"
                  >
                    {{ item.account.trangThai ? 'Khóa TK' : 'Mở Khóa' }}
                  </button>
                  <button class="btn-action delete" @click="handleDeleteAccount(item.account.id)">
                    Xóa
                  </button>
                </td>
              </tr>
              <tr v-if="filteredCustomers.length === 0">
                <td colspan="7" style="text-align: center; color: #999; padding: 30px 0">
                  📭 Không tìm thấy tài khoản khách hàng nào.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <!-- ================= TAB 4: CHỨC VỤ ================= -->
      <div v-if="activeTab === 4" class="tab-pane">
        <div class="role-management-panel">
          <div class="role-toolbar">
            <label>Chọn chức vụ để xem tài khoản</label>
            <select v-model="roleViewTab" class="select-classic">
              <option :value="1">👑 Admin</option>
              <option :value="2">💼 Nhân viên</option>
              <option :value="3">👥 Khách hàng</option>
              <option v-for="role in customRoleTabs" :key="role.id" :value="role.tabKey">
                🧩 {{ role.tenChucVu }}
              </option>
            </select>
            <button class="role-tab-btn add" @click="openRoleForm()">＋ THÊM CHỨC VỤ</button>
          </div>

          <div class="filters-row" style="margin-top: 16px">
            <div class="search-box">
              <input v-model="searchRole" type="text" placeholder="Tìm chức vụ hoặc mã chức vụ..." />
            </div>
            <div class="sort-box">
              <label>Sắp xếp:</label>
              <select v-model="sortRole" class="select-classic">
                <option value="id_desc">Mới nhất (Default)</option>
                <option value="id_asc">Cũ nhất</option>
                <option value="name_asc">Tên chức vụ (A-Z)</option>
                <option value="name_desc">Tên chức vụ (Z-A)</option>
              </select>
            </div>
          </div>

          <div class="role-panel-content">
            <div class="role-list-card">
              <div class="role-card-header">
                <h4>Danh sách chức vụ</h4>
                <span>{{ filteredRoles.length }} mục</span>
              </div>
              <div v-for="role in filteredRoles" :key="role.id" class="role-item">
                <div>
                  <div class="role-item-title">{{ role.tenChucVu }}</div>
                  <div class="role-item-sub">{{ role.maChucVu }}</div>
                </div>
                <div class="role-item-actions">
                  <button v-if="canEditRole(role)" class="btn-action view" @click="openRoleForm(role)">Sửa</button>
                  <button v-if="canDeleteRole(role)" class="btn-action delete" @click="deleteRole(role)">Xóa</button>
                </div>
              </div>
              <div v-if="filteredRoles.length === 0" class="no-profile-text">
                📭 Chưa có chức vụ nào phù hợp.
              </div>
            </div>

            <div class="role-account-card">
              <div class="role-card-header">
                <h4>{{ activeRoleInfo?.tenChucVu || 'Chọn chức vụ' }}</h4>
                <span>{{ filteredRoleAccounts.length }} tài khoản</span>
              </div>
              <div v-if="activeRoleInfo" class="table-container">
                <table class="table-classic">
                  <thead>
                    <tr>
                      <th style="width: 80px; text-align: center">ID</th>
                      <th>Mã tài khoản</th>
                      <th>Tên đăng nhập</th>
                      <th>Trạng thái</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="acc in filteredRoleAccounts" :key="acc.id">
                      <td style="text-align: center; color: #888">{{ acc.id }}</td>
                      <td style="font-weight: bold; color: #8b5e34">{{ acc.maTaiKhoan }}</td>
                      <td class="highlight-text">{{ acc.tenDangNhap }}</td>
                      <td>
                        <span :class="acc.trangThai ? 'status-green' : 'status-red'">
                          {{ acc.trangThai ? 'Hoạt động' : 'Ngừng hoạt động' }}
                        </span>
                      </td>
                    </tr>
                    <tr v-if="filteredRoleAccounts.length === 0">
                      <td colspan="4" style="text-align: center; color: #999; padding: 24px 0">
                        📭 Chưa có tài khoản nào thuộc chức vụ này.
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div v-else class="no-profile-text">
                Chọn một tab chức vụ để xem tài khoản tương ứng.
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showCreateAccountModal" class="modal-overlay" @click.self="showCreateAccountModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>➕ THÊM TÀI KHOẢN MỚI</h3>
          <span class="close-btn" @click="showCreateAccountModal = false">&times;</span>
        </div>
        <div class="modal-body">
          <div class="modal-form-grid">
            <div class="modal-form-field">
              <label>Tên đăng nhập</label>
              <input v-model="createAccountForm.tenDangNhap" type="text" placeholder="Nhập tên đăng nhập" :class="{ 'is-error': accountFormErrors.tenDangNhap }" @input="clearCreateAccountError('tenDangNhap')" />
              <span v-if="accountFormErrors.tenDangNhap" class="form-error-message">{{ accountFormErrors.tenDangNhap }}</span>
            </div>
            <div class="modal-form-field">
              <label>Mật khẩu</label>
              <input v-model="createAccountForm.matKhau" type="password" placeholder="Nhập mật khẩu" :class="{ 'is-error': accountFormErrors.matKhau }" @input="clearCreateAccountError('matKhau')" />
              <span v-if="accountFormErrors.matKhau" class="form-error-message">{{ accountFormErrors.matKhau }}</span>
            </div>
            <div class="modal-form-field">
              <label>Chức vụ</label>
              <select v-model.number="createAccountForm.idChucVu" class="select-classic">
                <option v-for="role in availableAccountRoles" :key="role.id" :value="Number(role.id)">
                  {{ role.tenChucVu }}
                </option>
              </select>
            </div>
            <div class="modal-form-field">
              <label>Họ và tên</label>
              <input v-model="createAccountForm.hoTen" type="text" placeholder="Nhập họ và tên" :class="{ 'is-error': accountFormErrors.hoTen }" @input="clearCreateAccountError('hoTen')" />
              <span v-if="accountFormErrors.hoTen" class="form-error-message">{{ accountFormErrors.hoTen }}</span>
            </div>
            <div class="modal-form-field">
              <label>Số điện thoại</label>
              <input v-model="createAccountForm.soDienThoai" type="text" placeholder="Nhập số điện thoại" :class="{ 'is-error': accountFormErrors.soDienThoai }" @input="clearCreateAccountError('soDienThoai')" />
              <span v-if="accountFormErrors.soDienThoai" class="form-error-message">{{ accountFormErrors.soDienThoai }}</span>
            </div>
            <div class="modal-form-field">
              <label>Email</label>
              <input v-model="createAccountForm.email" type="email" placeholder="Nhập email" :class="{ 'is-error': accountFormErrors.email }" @input="clearCreateAccountError('email')" />
              <span v-if="accountFormErrors.email" class="form-error-message">{{ accountFormErrors.email }}</span>
            </div>
            <div class="modal-form-field">
              <label>Địa chỉ</label>
              <input v-model="createAccountForm.diaChi" type="text" placeholder="Nhập địa chỉ" :class="{ 'is-error': accountFormErrors.diaChi }" @input="clearCreateAccountError('diaChi')" />
              <span v-if="accountFormErrors.diaChi" class="form-error-message">{{ accountFormErrors.diaChi }}</span>
            </div>
            <div class="modal-form-field">
              <label>Giới tính</label>
              <select v-model="createAccountForm.gioiTinh" class="select-classic">
                <option :value="true">Nam</option>
                <option :value="false">Nữ</option>
              </select>
            </div>
            <div class="modal-form-field full-width">
              <label>Mã tài khoản</label>
              <input type="text" value="Sẽ được hệ thống tự sinh" disabled />
            </div>
          </div>
          <div class="modal-actions">
            <button class="btn-gray" @click="showCreateAccountModal = false">Hủy</button>
            <button class="btn-action view" @click="submitCreateAccount">Lưu tài khoản</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showRoleModal" class="modal-overlay" @click.self="showRoleModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ roleFormMode === 'create' ? '➕ THÊM CHỨC VỤ' : '✏️ SỬA CHỨC VỤ' }}</h3>
          <span class="close-btn" @click="showRoleModal = false">&times;</span>
        </div>
        <div class="modal-body">
          <div class="modal-form-grid">
            <div class="modal-form-field">
              <label>Mã chức vụ</label>
              <input
                v-model="roleForm.maChucVu"
                :disabled="roleFormMode === 'create'"
                type="text"
                placeholder="Sẽ được hệ thống tự sinh"
              />
            </div>
            <div class="modal-form-field">
              <label>Tên chức vụ</label>
              <input v-model="roleForm.tenChucVu" type="text" placeholder="Nhập tên chức vụ" />
            </div>
            <div class="modal-form-field full-width">
              <label>Quyền truy cập module</label>
              <div class="permissions-grid">
                <label v-for="perm in PERMISSION_MODULES" :key="perm.key" class="permission-checkbox">
                  <input
                    type="checkbox"
                    :value="perm.key"
                    v-model="roleForm.permissions"
                  />
                  <span>{{ perm.label }}</span>
                </label>
              </div>
            </div>
          </div>
          <div class="modal-actions">
            <button class="btn-gray" @click="showRoleModal = false">Hủy</button>
            <button class="btn-action view" @click="submitRole">Lưu chức vụ</button>
          </div>
        </div>
      </div>
    </div>

    <!-- ================= DETAIL MODAL POPUP ================= -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>
            🔍 CHI TIẾT TÀI KHOẢN (#{{
              selectedDetail?.account?.maTaiKhoan || selectedDetail?.maTaiKhoan
            }})
          </h3>
          <span class="close-btn" @click="closeDetailModal">&times;</span>
        </div>

        <div class="modal-body" v-if="selectedDetail">
          <!-- ADMIN Account Details -->
          <template v-if="modalType === 'admin'">
            <div class="detail-section-title">🔑 Thông Tin Tài Khoản</div>
            <table class="detail-table">
              <tbody>
                <tr>
                  <td class="lbl">Tên Đăng Nhập:</td>
                  <td class="val highlight-text">{{ selectedDetail.account.tenDangNhap }}</td>
                </tr>
                <tr>
                  <td class="lbl">Chức Vụ Hệ Thống:</td>
                  <td class="val"><span class="badge-role-admin">ADMIN</span></td>
                </tr>
                <tr>
                  <td class="lbl">Trạng Thái Tài Khoản:</td>
                  <td class="val">
                    <span
                      :class="selectedDetail.account.trangThai ? 'status-green' : 'status-red'"
                      style="font-weight: bold"
                    >
                      {{ selectedDetail.account.trangThai ? 'Đang hoạt động' : 'Đang khóa' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="selectedDetail.employee">
              <div class="detail-section-title">👤 Thông Tin Cá Nhân Người Sở Hữu (Nhân Viên)</div>
              <table class="detail-table">
                <tbody>
                  <tr>
                    <td class="lbl">Họ và Tên:</td>
                    <td class="val text-gold">{{ selectedDetail.employee.tenNhanVien }}</td>
                  </tr>
                  <tr>
                    <td class="lbl">Số Điện Thoại:</td>
                    <td class="val">{{ selectedDetail.employee.soDienThoai }}</td>
                  </tr>
                  <tr>
                    <td class="lbl">Email:</td>
                    <td class="val text-blue">{{ selectedDetail.employee.email }}</td>
                  </tr>
                  <tr>
                    <td class="lbl">Địa Chỉ:</td>
                    <td class="val">{{ selectedDetail.employee.diaChi }}</td>
                  </tr>
                  <tr>
                    <td class="lbl">Giới Tính:</td>
                    <td class="val">{{ selectedDetail.employee.gioiTinh ? 'Nam' : 'Nữ' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-else class="no-profile-text">
              ℹ️ Tài khoản admin này không liên kết hồ sơ nhân viên cụ thể.
            </div>
          </template>
          <!-- STAFF Account Details -->
          <template v-if="modalType === 'staff'">
            <div class="detail-section-title">💼 Hồ Sơ Nhân Viên & Chức Vụ</div>
            <table class="detail-table" v-if="selectedDetail.employee">
              <tbody>
                <tr>
                  <td class="lbl">Họ Tên Nhân Viên:</td>
                  <td class="val text-gold">{{ selectedDetail.employee.tenNhanVien }}</td>
                </tr>
                <tr>
                  <td class="lbl">Mã Nhân Viên:</td>
                  <td class="val highlight-gray">{{ selectedDetail.employee.maNhanVien }}</td>
                </tr>
                <tr>
                  <td class="lbl">Số Điện Thoại:</td>
                  <td class="val">{{ selectedDetail.employee.soDienThoai }}</td>
                </tr>
                <tr>
                  <td class="lbl">Email:</td>
                  <td class="val text-blue">{{ selectedDetail.employee.email }}</td>
                </tr>
                <tr>
                  <td class="lbl">Địa Chỉ:</td>
                  <td class="val">{{ selectedDetail.employee.diaChi }}</td>
                </tr>
                <tr>
                  <td class="lbl">Giới Tính:</td>
                  <td class="val">{{ selectedDetail.employee.gioiTinh ? 'Nam' : 'Nữ' }}</td>
                </tr>
                <tr>
                  <td class="lbl">Trạng Thái Làm Việc:</td>
                  <td class="val">
                    <span :class="selectedDetail.employee.trangThai ? 'status-green' : 'status-red'">
                      {{ selectedDetail.employee.trangThai ? 'Đang làm việc' : 'Đã nghỉ việc' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-else class="no-profile-text">
              ⚠️ Không tìm thấy hồ sơ nhân viên cho tài khoản này.
            </div>
            <div class="detail-section-title">🔑 Thông Tin Tài Khoản</div>
            <table class="detail-table">
              <tbody>
                <tr>
                  <td class="lbl">Tên Đăng Nhập:</td>
                  <td class="val highlight-text">{{ selectedDetail.account.tenDangNhap }}</td>
                </tr>
                <tr>
                  <td class="lbl">Quyền (Chức vụ):</td>
                  <td class="val">
                    <span class="badge-role-staff">{{ selectedDetail.account.tenChucVu }}</span>
                  </td>
                </tr>
                <tr>
                  <td class="lbl">Trạng Thái Tài Khoản:</td>
                  <td class="val">
                    <span
                      :class="selectedDetail.account.trangThai ? 'status-green' : 'status-red'"
                      style="font-weight: bold"
                    >
                      {{ selectedDetail.account.trangThai ? 'Đang hoạt động' : 'Đang khóa' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </template>
          <!-- CUSTOMER Account Details (separated tabs: info / invoices) -->
          <template v-if="modalType === 'customer'">
            <div class="customer-detail-tabs">
              <button
                :class="['tab-small', { active: customerModalTab === 'info' }]"
                @click="customerModalTab = 'info'"
              >
                👤 Thông tin
              </button>
              <button
                :class="['tab-small', { active: customerModalTab === 'invoices' }]"
                @click="customerModalTab = 'invoices'"
              >
                🧾 Lịch sử hoá đơn
              </button>
              <button
                :class="['tab-small', { active: customerModalTab === 'bookings' }]"
                @click="customerModalTab = 'bookings'"
              >
                🍲 Lịch sử đặt bàn
              </button>
            </div>

            <div v-if="customerModalTab === 'info'">
              <div class="detail-section-title">👤 Hồ Sơ Khách Hàng</div>
              <table class="detail-table" v-if="selectedDetail.customer">
                <tbody>
                  <tr>
                    <td class="lbl">Mã Khách Hàng:</td>
                    <td class="val highlight-gray">{{ selectedDetail.customer.maKhachHang || '---' }}</td>
                  </tr>
                  <tr>
                    <td class="lbl">Họ Tên Khách Hàng:</td>
                    <td class="val text-gold">{{ selectedDetail.customer.tenKhachHang || '---' }}</td>
                  </tr>
                  <tr>
                    <td class="lbl">Số Điện Thoại:</td>
                    <td class="val">{{ selectedDetail.customer.soDienThoai || '---' }}</td>
                  </tr>
                  <tr>
                    <td class="lbl">Email:</td>
                    <td class="val text-blue">{{ selectedDetail.customer.email || 'Chưa đăng ký' }}</td>
                  </tr>
                  <tr>
                    <td class="lbl">Địa Chỉ:</td>
                    <td class="val">{{ selectedDetail.customer.diaChi || '---' }}</td>
                  </tr>
                  <tr>
                    <td class="lbl">Giới Tính:</td>
                    <td class="val">
                      {{ selectedDetail.customer.gioiTinh === null || selectedDetail.customer.gioiTinh === undefined ? 'Chưa xác định' : selectedDetail.customer.gioiTinh ? 'Nam' : 'Nữ' }}
                    </td>
                  </tr>
                  <tr>
                    <td class="lbl">Trạng Thái Khách Hàng:</td>
                    <td class="val">
                      <span :class="selectedDetail.customer.trangThai ? 'status-green' : 'status-red'">
                        {{ selectedDetail.customer.trangThai ? 'Đang hoạt động' : 'Ngừng hoạt động' }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
              <div v-else class="no-profile-text">
                ⚠️ Không tìm thấy hồ sơ thông tin cá nhân khách hàng cho tài khoản này.
              </div>

              <div class="detail-section-title">🔑 Thông Tin Tài Khoản</div>
              <table class="detail-table">
                <tbody>
                  <tr>
                    <td class="lbl">Tên Đăng Nhập:</td>
                    <td class="val highlight-text">{{ selectedDetail.account.tenDangNhap }}</td>
                  </tr>
                  <tr>
                    <td class="lbl">Trạng Thái Tài Khoản:</td>
                    <td class="val">
                      <span
                        :class="selectedDetail.account.trangThai ? 'status-green' : 'status-red'"
                        style="font-weight: bold"
                      >
                        {{
                          selectedDetail.account.trangThai
                            ? 'Đang hoạt động (Active)'
                            : 'Bị Khóa (Disabled)'
                        }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="customerModalTab === 'invoices'">
              <div class="detail-section-title">🧾 Lịch Sử Hoá Đơn</div>
              <div v-if="invoiceLoading" class="text-loading">Đang tải lịch sử hoá đơn...</div>
              <div v-else-if="customerInvoices.length === 0" class="no-profile-text">
                📭 Khách hàng này chưa có hoá đơn nào.
              </div>
              <div v-else class="invoices-list">
                <div v-for="inv in customerInvoices" :key="inv.idHoaDon" class="invoice-card-mini">
                  <div class="invoice-card-header">
                    <div>
                      <span class="invoice-status">{{ invoiceStatusLabel(inv.trangThaiHoaDon) }}</span>
                      <strong class="invoice-code">{{ inv.maHoaDon }}</strong>
                      <span class="invoice-date">{{ formatDateTime(inv.thoiGianXuat || inv.thoiGian) }}</span>
                    </div>
                    <div class="invoice-total-box">
                      <span>Tổng tiền</span>
                      <strong>{{ formatCurrency(inv.tongTien) }}</strong>
                    </div>
                  </div>

                  <div class="invoice-info-grid">
                    <div>
                      <span>Khách hàng</span>
                      <strong>{{ inv.tenKhachHang || selectedDetail.customer?.tenKhachHang || 'Khách lẻ' }}</strong>
                    </div>
                    <div>
                      <span>Số điện thoại</span>
                      <strong>{{ inv.sdtKhachHang || selectedDetail.customer?.soDienThoai || 'Chưa có' }}</strong>
                    </div>
                    <div>
                      <span>Bàn</span>
                      <strong>{{ formatInvoiceTableLabel(inv) }}</strong>
                    </div>
                    <div>
                      <span>Giờ vào bàn</span>
                      <strong>{{ formatDateTime(inv.gioVaoBan || inv.thoiGianXuat) }}</strong>
                    </div>
                    <div>
                      <span>Giờ rời bàn</span>
                      <strong>{{ formatDateTime(inv.gioRoiBan) }}</strong>
                    </div>
                    <div>
                      <span>Nhân viên</span>
                      <strong>{{ inv.tenNhanVien || 'Chưa có' }}</strong>
                    </div>
                    <div>
                      <span>Thanh toán</span>
                      <strong :class="inv.trangThaiThanhToan === 1 ? 'status-green' : 'status-red'">
                        {{ paymentStatusLabel(inv.trangThaiThanhToan) }}
                      </strong>
                    </div>
                    <div>
                      <span>Phương thức</span>
                      <strong>{{ paymentMethodLabel(inv.phuongThucThanhToan) }}</strong>
                    </div>
                    <div>
                      <span>Mã giao dịch</span>
                      <strong>{{ inv.maGiaoDich || 'Không có' }}</strong>
                    </div>
                    <div>
                      <span>Mã giảm giá</span>
                      <strong>{{ inv.maGiamGia || 'Không có' }}</strong>
                    </div>
                  </div>

                  <div class="invoice-money-grid">
                    <div>
                      <span>Trước giảm</span>
                      <strong>{{ formatCurrency(inv.tienTruocGiam) }}</strong>
                    </div>
                    <div>
                      <span>Tiền cọc</span>
                      <strong>{{ formatCurrency(inv.tienCoc) }}</strong>
                    </div>
                    <div>
                      <span>Giảm giá</span>
                      <strong>{{ formatCurrency(inv.tienGiamGia) }}</strong>
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
                          v-for="item in customerInvoiceDetails[inv.idHoaDon] || []"
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
                    <div
                      v-if="(customerInvoiceDetails[inv.idHoaDon] || []).length === 0"
                      class="invoice-empty-detail"
                    >
                      Hóa đơn này chưa có chi tiết món.
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="customerModalTab === 'bookings'">
              <div class="detail-section-title">🍲 Lịch Sử Đặt Bàn & Tiền Cọc</div>
              <div v-if="bookingLoading" class="text-loading">Đang tải lịch sử đặt bàn...</div>
              <div v-else-if="!customerBookings || customerBookings.length === 0" class="no-profile-text">
                📭 Khách hàng này chưa từng đặt bàn trên hệ thống.
              </div>
              <div v-else class="booking-history-container">
                <div v-for="(booking, index) in customerBookings" :key="booking.idDatBan" class="booking-item-card">
                  <div class="booking-item-header">
                    <span>Đơn Đặt Bàn #{{ booking.idDatBan }} (Lần {{ Number(index) + 1 }})</span>
                    <span :class="booking.trangThai === 'DA_XAC_NHAN' ? 'status-green' : 'status-red'" style="font-size:11px;">● {{ booking.trangThaiText || booking.trangThai }}</span>
                  </div>
                  <table class="detail-table style-compact">
                    <tbody>
                      <tr>
                        <td class="lbl">Thời gian dự kiến:</td>
                        <td class="val text-blue">{{ formatDateTime(booking.thoiGianDenDuKien || booking.ngayDat) }}</td>
                      </tr>
                      <tr>
                        <td class="lbl">Số người:</td>
                        <td class="val"><span class="badge-count">{{ booking.soNguoi }} Người</span></td>
                      </tr>
                      <tr>
                        <td class="lbl">Bàn / Ghi chú:</td>
                        <td class="val">{{ formatBookingTableLabel(booking) }} <span v-if="booking.ghiChu" style="color:#aaa;">— "{{ booking.ghiChu }}"</span></td>
                      </tr>
                      <tr>
                        <td class="lbl">Tiền cọc:</td>
                        <td class="val">{{ formatCurrency(booking.soTienCoc) }} <span :class="booking.trangThaiCoc === 'DA_COC' ? 'status-green' : 'status-red'" style="font-size:12px; margin-left:6px">({{ booking.trangThaiCoc === 'DA_COC' ? 'Đã cọc' : 'Chưa cọc' }})</span></td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </template>
        </div>

        <div class="modal-footer">
          <button class="btn-gray" @click="closeDetailModal">Đóng lại</button>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.account-mgmt-page {
  padding: 20px;
  background: linear-gradient(135deg, #f9efe0 0%, #f4e4c6 100%);
  min-height: 100vh;
  font-family: Arial, sans-serif;
  color: #5f3d22;
}
.page-header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}
.btn-back-home {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #4b7c45, #6d9b5d);
  color: #ffffff;
  border: 1px solid #4b7c45;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-back-home:hover {
  background: linear-gradient(135deg, #3f693b, #5a8550);
  border-color: #d8a85c;
  color: #fffaf1;
}
.btn-back-home svg {
  transition: transform 0.2s ease;
}
.btn-back-home:hover svg {
  transform: scale(1.1);
}
h2 {
  color: #8b5e34;
  margin: 0;
}
.btn-add-account {
  background: linear-gradient(135deg, #8b5e34, #c98b3e);
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 10px 16px;
  font-weight: bold;
  cursor: pointer;
}
.btn-add-account:hover {
  transform: translateY(-1px);
  filter: brightness(1.06);
}
.line-break {
  border: 0;
  border-top: 1px solid #e6d2aa;
  margin-bottom: 20px;
}
.tabs-navigation {
  display: flex;
  flex-wrap: nowrap;
  gap: 10px;
  margin-bottom: 20px;
  border-bottom: 2px solid #e6d2aa;
  padding-bottom: 10px;
  overflow-x: auto;
}
.tab-btn {
  background-color: #fff8ea;
  color: #8b5e34;
  border: 1px solid #e6d2aa;
  padding: 10px 20px;
  font-weight: bold;
  border-radius: 6px 6px 0 0;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  white-space: nowrap;
}
.tab-btn:hover {
  color: #3d2814;
  background-color: #f3dfb4;
}
.tab-btn.active {
  background: linear-gradient(135deg, #d8a85c, #f1cf87);
  color: #3d2814;
  border-color: #d8a85c;
  transform: translateY(-2px);
  box-shadow: 0 -4px 10px rgba(103, 72, 32, 0.16);
}
.filters-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: rgba(255, 248, 234, 0.95);
  padding: 12px 20px;
  border-radius: 12px;
  border: 1px solid #e6d2aa;
  box-shadow: 0 8px 18px rgba(103, 72, 32, 0.06);
}
.search-box input {
  background-color: #fffaf1;
  color: #5f3d22;
  border: 1px solid #e6d2aa;
  padding: 8px 14px;
  border-radius: 6px;
  width: 320px;
  outline: none;
  transition: border-color 0.2s;
}
.search-box input:focus {
  border-color: #d8a85c;
}
.sort-box {
  display: flex;
  align-items: center;
  gap: 8px;
}
.sort-box label {
  font-size: 13px;
  color: #8f6b46;
}
.select-classic {
  background-color: #fffaf1;
  color: #5f3d22;
  border: 1px solid #e6d2aa;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  outline: none;
}
.select-classic:focus {
  border-color: #d8a85c;
}
.table-container {
  overflow-x: auto;
  border-radius: 12px;
  border: 1px solid #e6d2aa;
  background: rgba(255, 248, 234, 0.95);
}
.table-classic {
  width: 100%;
  border-collapse: collapse;
  background: transparent;
}
.table-classic th {
  background: #f3dfb4;
  color: #8b5e34;
  padding: 12px 16px;
  text-align: left;
  border-bottom: 2px solid #e6d2aa;
  font-size: 13px;
  letter-spacing: 0.5px;
}
.table-classic td {
  padding: 12px 16px;
  border-bottom: 1px solid #efe0c1;
  font-size: 14px;
  color: #5f3d22;
}
.table-classic tr:hover {
  background-color: #fef4de;
}
.highlight-text {
  font-weight: bold;
  color: #8b5e34;
}
.status-green {
  color: #28a745;
}
.status-red {
  color: #dc3545;
}
.btn-action {
  padding: 6px 12px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 12px;
  cursor: pointer;
  border: 1px solid;
  margin-right: 5px;
  background: transparent;
  transition: all 0.2s ease;
}
.view {
  border-color: #8b5e34;
  color: #8b5e34;
}
.view:hover {
  background-color: #8b5e34;
  color: #fff;
}
.delete {
  border-color: #be5b46;
  color: #be5b46;
}
.delete:hover {
  background-color: #be5b46;
  color: #fff;
}
.lock {
  border-color: #c98b3e;
  color: #c98b3e;
}
.lock:hover {
  background-color: #c98b3e;
  color: #fff;
}
.unlock {
  border-color: #4b7c45;
  color: #4b7c45;
}
.unlock:hover {
  background-color: #4b7c45;
  color: #fff;
}
.text-loading {
  text-align: center;
  padding: 40px;
  font-size: 15px;
  color: #8f6b46;
  font-style: italic;
}
.role-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: bold;
  letter-spacing: 0.5px;
}
.role-badge.admin {
  background-color: rgba(220, 53, 69, 0.14);
  color: #b94a4a;
  border: 1px solid rgba(220, 53, 69, 0.24);
}
.role-badge.staff {
  background-color: rgba(13, 110, 253, 0.12);
  color: #5f7fb8;
  border: 1px solid rgba(13, 110, 253, 0.2);
}
.role-badge.user {
  background-color: rgba(40, 167, 69, 0.12);
  color: #4b7c45;
  border: 1px solid rgba(40, 167, 69, 0.2);
}
.role-select {
  background-color: #fffaf1;
  color: #8b5e34;
  border: 1px solid #e6d2aa;
  padding: 6px 12px;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
  outline: none;
}
.role-select:focus {
  box-shadow: 0 0 5px rgba(216, 168, 92, 0.35);
}
.role-management-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.role-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
.role-toolbar label {
  color: #8b5e34;
  font-weight: bold;
  font-size: 13px;
}
.role-tab-btn {
  background: #fffaf1;
  color: #8b5e34;
  border: 1px solid #e6d2aa;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
}
.role-tab-btn.active {
  background: linear-gradient(135deg, #d8a85c, #f1cf87);
  color: #3d2814;
}
.role-tab-btn.add {
  border-style: dashed;
}
.role-panel-content {
  display: grid;
  grid-template-columns: minmax(280px, 320px) 1fr;
  gap: 16px;
}
.role-list-card,
.role-account-card {
  background: rgba(255, 248, 234, 0.95);
  border: 1px solid #e6d2aa;
  border-radius: 12px;
  padding: 16px;
}
.role-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.role-card-header h4 {
  margin: 0;
  color: #8b5e34;
}
.role-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #efe0c1;
}
.role-item:last-child {
  border-bottom: none;
}
.role-item-title {
  font-weight: bold;
  color: #5f3d22;
}
.role-item-sub {
  font-size: 12px;
  color: #8f6b46;
}
.role-item-actions {
  display: flex;
  gap: 6px;
}
.modal-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.modal-form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.modal-form-field label {
  font-size: 12px;
  font-weight: bold;
  color: #8b5e34;
}
.modal-form-field.full-width {
  grid-column: 1 / -1;
}
.modal-form-field input,
.modal-form-field select {
  background: #fffaf1;
  border: 1px solid #e6d2aa;
  border-radius: 6px;
  padding: 8px 10px;
  color: #5f3d22;
}
.permissions-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}
.permission-checkbox {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff9e6;
  border: 1px solid #e6d2aa;
  border-radius: 8px;
  padding: 10px 12px;
  color: #735c2e;
  font-size: 13px;
}
.permission-checkbox input {
  width: 18px;
  height: 18px;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
.btn-gray {
  background: #efe0c1;
  color: #5f3d22;
  border: 1px solid #e6d2aa;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
}
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.72);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}
.modal-content {
  background: rgba(255, 248, 234, 0.98);
  border: 1px solid #e6d2aa;
  border-radius: 8px;
  width: 600px;
  box-shadow: 0 5px 25px rgba(103, 72, 32, 0.18);
  overflow: hidden;
}
.modal-header {
  background: #f3dfb4;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e6d2aa;
}
.modal-header h3 {
  margin: 0;
  color: #8b5e34;
  font-size: 15px;
  letter-spacing: 0.5px;
}
.close-btn {
  color: #8f6b46;
  font-size: 24px;
  font-weight: bold;
  cursor: pointer;
  line-height: 1;
}
.close-btn:hover {
  color: #3d2814;
}
.modal-body {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}
.detail-section-title {
  color: #8b5e34;
  font-size: 14px;
  font-weight: bold;
  letter-spacing: 0.5px;
  margin-top: 15px;
  margin-bottom: 10px;
  padding-bottom: 5px;
  border-bottom: 1px dashed #e6d2aa;
}
.detail-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 15px;
}
.detail-table td {
  padding: 8px 6px;
  border-bottom: 1px solid #efe0c1;
  font-size: 13.5px;
}
.detail-table .lbl {
  width: 180px;
  color: #8f6b46;
}
.detail-table .val {
  color: #5f3d22;
}
.text-gold {
  color: #c98b3e;
  font-weight: bold;
}
.text-blue {
  color: #5f7fb8;
}
.highlight-gray {
  font-family: monospace;
  color: #8f6b46;
}
.no-profile-text {
  color: #8f6b46;
  font-style: italic;
  padding: 10px 0;
  font-size: 13px;
}
.badge-role-admin {
  background-color: rgba(220, 53, 69, 0.14);
  color: #b94a4a;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: bold;
}
.badge-role-staff {
  background-color: rgba(13, 110, 253, 0.12);
  color: #5f7fb8;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: bold;
}
.booking-history-container {
  max-height: 250px;
  overflow-y: auto;
  padding-right: 5px;
}
.customer-detail-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.tab-small {
  background: transparent;
  border: 1px solid #e6d2aa;
  color: #8b5e34;
  padding: 6px 10px;
  border-radius: 6px;
  cursor: pointer;
}
.tab-small.active {
  background: linear-gradient(135deg, #d8a85c, #f1cf87);
  color: #3d2814;
  border-color: #d8a85c;
}
.invoice-card-mini {
  background-color: #fffaf1;
  border: 1px solid #e6d2aa;
  border-radius: 6px;
  padding: 14px;
  margin-bottom: 12px;
}
.invoice-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  border-bottom: 1px solid #efe0c1;
  padding-bottom: 10px;
  margin-bottom: 12px;
}
.invoice-code {
  display: block;
  color: #8b5e34;
  font-size: 16px;
}
.invoice-date {
  display: block;
  color: #8f6b46;
  font-size: 12px;
  margin-top: 4px;
}
.invoice-status {
  display: inline-block;
  color: #3d2814;
  background: linear-gradient(135deg, #d8a85c, #f1cf87);
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 11px;
  font-weight: bold;
  margin-bottom: 6px;
}
.invoice-total-box {
  min-width: 150px;
  text-align: right;
}
.invoice-total-box span,
.invoice-info-grid span,
.invoice-money-grid span {
  display: block;
  color: #8f6b46;
  font-size: 12px;
  margin-bottom: 4px;
}
.invoice-total-box strong {
  color: #4b7c45;
  font-size: 16px;
}
.invoice-info-grid,
.invoice-money-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 16px;
  margin-bottom: 12px;
}
.invoice-info-grid strong,
.invoice-money-grid strong {
  color: #5f3d22;
  font-size: 13px;
  word-break: break-word;
}
.invoice-money-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  background-color: #fff8ea;
  border: 1px solid #e6d2aa;
  border-radius: 6px;
  padding: 10px;
}
.invoice-detail-table-wrap {
  overflow-x: auto;
}
.invoice-detail-title {
  color: #8b5e34;
  font-size: 13px;
  font-weight: bold;
  margin-bottom: 8px;
}
.invoice-detail-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}
.invoice-detail-table th,
.invoice-detail-table td {
  border-bottom: 1px solid #efe0c1;
  padding: 8px;
  text-align: left;
  vertical-align: top;
}
.invoice-detail-table th {
  color: #8b5e34;
  background-color: #f3dfb4;
}
.combo-items {
  color: #8f6b46;
  font-size: 11px;
  margin-top: 3px;
}
.invoice-empty-detail {
  color: #8f6b46;
  font-size: 12px;
  font-style: italic;
  padding: 8px 0;
}
.booking-item-card {
  background-color: #fffaf1;
  border: 1px solid #e6d2aa;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 10px;
}
.booking-item-header {
  display: flex;
  justify-content: space-between;
  font-weight: bold;
  color: #8b5e34;
  font-size: 12.5px;
  border-bottom: 1px solid #efe0c1;
  padding-bottom: 6px;
  margin-bottom: 6px;
}
.style-compact td {
  padding: 4px 6px !important;
  border-bottom: none !important;
}
.style-compact .lbl {
  width: 150px !important;
}
.badge-count {
  background: linear-gradient(135deg, #d8a85c, #f1cf87);
  color: #3d2814;
  padding: 1px 5px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: bold;
}
.gold-text {
  color: #c98b3e;
  font-weight: bold;
}
.modal-footer {
  padding: 15px 20px;
  background: #f3dfb4;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #e6d2aa;
}
.btn-gray {
  background: #fff3d3;
  color: #8b5e34;
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
  border: 1px solid #e6d2aa;
}
.btn-gray:hover {
  background: #f3dfb4;
}
</style>
