import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
// import vueDevTools from 'vite-plugin-vue-devtools' // Tạm vô hiệu hóa

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // vueDevTools(), // Tạm vô hiệu hóa để tránh lỗi auto-discovery
  ],
  define: {
    // Sửa lỗi "ReferenceError: global is not defined" cho SockJS / StompJS
    global: 'window',
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // (Tùy chọn) Proxy luôn WebSocket nếu sau này muốn chạy qua cổng Vite 5173
      '/ws-print': {
        target: 'http://localhost:8080',
        ws: true,
        changeOrigin: true,
      },
    },
  },
})