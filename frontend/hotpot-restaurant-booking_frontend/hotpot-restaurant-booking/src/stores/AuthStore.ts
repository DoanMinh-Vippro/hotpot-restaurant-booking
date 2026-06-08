import { defineStore } from 'pinia';
import { jwtDecode } from 'jwt-decode';

export const useAuthStore = defineStore('auth', {
  state: () => {
    const token = localStorage.getItem('token');
    let role = null;
    
    // Tự động decode ngay khi khởi tạo nếu đã có token
    if (token) {
      try {
        const decoded: any = jwtDecode(token);
        role = decoded.scope;
      } catch (e) {
        localStorage.removeItem('token');
      }
    }
    
    return {
      token: token,
      userRole: role as string | null
    };
  },
  
  actions: {
    // Gọi hàm này sau khi đăng nhập thành công từ API
    login(token: string) {
      this.token = token;
      localStorage.setItem('token', token);
      this.decodeToken(token);
    },

    decodeToken(token: string) {
      try {
        const decoded: any = jwtDecode(token);
        this.userRole = decoded.scope; // Lấy 'scope' từ JWT
      } catch (error) {
        this.logout();
      }
    },

    logout() {
      this.token = null;
      this.userRole = null;
      localStorage.removeItem('token');
      // Sau khi logout có thể điều hướng về trang chủ
    }
  },

  getters: {
    isAuthenticated: (state) => !!state.token,
    
    // Nếu bạn muốn ADMIN và STAFF đều vào được các menu quản lý:
    isAdmin: (state) => {
      return state.userRole === 'ROLE_ADMIN' || state.userRole === 'ROLE_STAFF';
    },
    
    isUser: (state) => state.userRole === 'ROLE_USER',
  }
});