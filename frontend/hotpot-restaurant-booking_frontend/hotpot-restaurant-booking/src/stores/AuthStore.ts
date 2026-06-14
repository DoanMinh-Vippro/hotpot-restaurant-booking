import { defineStore } from 'pinia'
import { jwtDecode } from 'jwt-decode'

// 1. Định nghĩa interface cho Payload của JWT
// Giúp TypeScript hiểu rõ cấu trúc của decoded token
interface JwtPayload {
  scope: string
  // Thêm các trường khác nếu cần, ví dụ: sub, exp...
}

// 2. Định nghĩa interface cho State
interface AuthState {
  token: string | null
  userRole: string | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('token'),
    userRole: null,
  }),

  actions: {
    login(token: string) {
      this.token = token
      localStorage.setItem('token', token)
      this.decodeToken(token)
    },

    decodeToken(token: string) {
      try {
        // Sử dụng Generic type <JwtPayload> thay cho 'any'
        const decoded = jwtDecode<JwtPayload>(token)
        this.userRole = decoded.scope || null
      } catch {
        // Dùng dấu _ để linter hiểu là ta cố tình bỏ qua biến này
        this.logout()
      }
    },

    logout() {
      this.token = null
      this.userRole = null
      localStorage.removeItem('token')
    },
  },

  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => state.userRole === 'ROLE_ADMIN' || state.userRole === 'ROLE_STAFF',
    isUser: (state) => state.userRole === 'ROLE_USER',
  },
})
