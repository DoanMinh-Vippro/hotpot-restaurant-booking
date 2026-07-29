import { defineStore } from 'pinia'
import { jwtDecode } from 'jwt-decode'

interface JwtPayload {
  scope: string
  exp?: number
}

const isTokenExpired = (decoded: JwtPayload): boolean => {
  if (!decoded.exp) return false
  return Date.now() >= decoded.exp * 1000
}

const decodeUserRoleFromToken = (token: string | null): string | null => {
  if (!token) return null
  try {
    const decoded = jwtDecode<JwtPayload>(token)
    if (isTokenExpired(decoded)) {
      localStorage.removeItem('token')
      return null
    }
    return decoded.scope || null
  } catch {
    localStorage.removeItem('token')
    return null
  }
}

interface CustomerInfo {
  khachHangId: number | null
  tenKhachHang: string | null
  soDienThoai: string | null
  email: string | null
  diaChi: string | null
  gioiTinh: boolean | null
  maKhachHang: string | null
}

interface AuthState {
  token: string | null
  userRole: string | null
  accountName: string | null
  customerInfo: CustomerInfo
}

const initialCustomerInfo: CustomerInfo = {
  khachHangId: null,
  tenKhachHang: null,
  soDienThoai: null,
  email: null,
  diaChi: null,
  gioiTinh: null,
  maKhachHang: null,
}

const clearStoredCustomerInfo = () => {
  localStorage.removeItem('khachHangId')
  localStorage.removeItem('tenKhachHang')
  localStorage.removeItem('soDienThoai')
  localStorage.removeItem('email')
  localStorage.removeItem('diaChi')
  localStorage.removeItem('gioiTinh')
  localStorage.removeItem('maKhachHang')
}

const hasCustomerInfo = (info?: Partial<CustomerInfo>): info is Partial<CustomerInfo> => {
  if (!info) return false
  return Object.values(info).some((value) => value !== undefined && value !== null && value !== '')
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => {
    const savedToken = localStorage.getItem('token')
    const savedRole = decodeUserRoleFromToken(savedToken)

    return {
      token: savedRole ? savedToken : null,
      userRole: savedRole,
      accountName: localStorage.getItem('tenDangNhap') || null,
      customerInfo: {
        khachHangId: localStorage.getItem('khachHangId') ? parseInt(localStorage.getItem('khachHangId')!) : null,
        tenKhachHang: localStorage.getItem('tenKhachHang'),
        soDienThoai: localStorage.getItem('soDienThoai'),
        email: localStorage.getItem('email'),
        diaChi: localStorage.getItem('diaChi'),
        gioiTinh: localStorage.getItem('gioiTinh') ? JSON.parse(localStorage.getItem('gioiTinh')!) : null,
        maKhachHang: localStorage.getItem('maKhachHang')
      },
    }
  },

  actions: {
    login(token: string, customerInfo?: Partial<CustomerInfo>, accountName?: string) {
      this.token = token
      localStorage.setItem('token', token)
      this.decodeToken(token)
      this.accountName = accountName || localStorage.getItem('tenDangNhap') || null

      if (accountName) {
        localStorage.setItem('tenDangNhap', accountName)
      }

      if (hasCustomerInfo(customerInfo)) {
        this.setCustomerInfo(customerInfo)
      } else {
        this.customerInfo = { ...initialCustomerInfo }
        clearStoredCustomerInfo()
      }
    },

    setCustomerInfo(info: Partial<CustomerInfo>) {
      if (info.khachHangId !== undefined) {
        this.customerInfo.khachHangId = info.khachHangId
        localStorage.setItem('khachHangId', info.khachHangId?.toString() || '')
      }
      
      if (info.tenKhachHang !== undefined) {
        this.customerInfo.tenKhachHang = info.tenKhachHang
        localStorage.setItem('tenKhachHang', info.tenKhachHang || '')
      }
      
      if (info.soDienThoai !== undefined) {
        this.customerInfo.soDienThoai = info.soDienThoai
        localStorage.setItem('soDienThoai', info.soDienThoai || '')
      }
      
      if (info.email !== undefined) {
        this.customerInfo.email = info.email
        localStorage.setItem('email', info.email || '')
      }
      
      if (info.diaChi !== undefined) {
        this.customerInfo.diaChi = info.diaChi
        localStorage.setItem('diaChi', info.diaChi || '')
      }
      
      if (info.gioiTinh !== undefined) {
        this.customerInfo.gioiTinh = info.gioiTinh
        localStorage.setItem('gioiTinh', JSON.stringify(info.gioiTinh))
      }
      
      if (info.maKhachHang !== undefined) {
        this.customerInfo.maKhachHang = info.maKhachHang
        localStorage.setItem('maKhachHang', info.maKhachHang || '')
      }
    },

    decodeToken(token: string) {
      try {
        const decoded = jwtDecode<JwtPayload>(token)
        this.userRole = decoded.scope || null
      } catch {
        this.logout()
      }
    },

    logout() {
      this.token = null
      this.userRole = null
      this.accountName = null
      this.customerInfo = {
        khachHangId: null,
        tenKhachHang: null,
        soDienThoai: null,
        email: null,
        diaChi: null,
        gioiTinh: null,
        maKhachHang: null
      }
      localStorage.removeItem('token')
      localStorage.removeItem('tenDangNhap')
      localStorage.removeItem('khachHangId')
      localStorage.removeItem('tenKhachHang')
      localStorage.removeItem('soDienThoai')
      localStorage.removeItem('email')
      localStorage.removeItem('diaChi')
      localStorage.removeItem('gioiTinh')
      localStorage.removeItem('maKhachHang')
    },
  },

  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => {
      const role = String(state.userRole || '').toUpperCase()
      return ['ROLE_ADMIN', 'ADMIN', 'ROLE_STAFF', 'STAFF', 'CASHIER', 'ROLE_CASHIER'].includes(role)
    },
    isShiftManager: (state) => {
      const role = String(state.userRole || '').toUpperCase()
      return ['ROLE_ADMIN', 'ADMIN', 'ROLE_STAFF', 'STAFF', 'CASHIER', 'ROLE_CASHIER'].includes(role)
    },
    isUser: (state) => state.userRole === 'ROLE_USER',
    khachHangId: (state) => state.customerInfo.khachHangId,
    tenKhachHang: (state) => state.customerInfo.tenKhachHang,
  },
})
