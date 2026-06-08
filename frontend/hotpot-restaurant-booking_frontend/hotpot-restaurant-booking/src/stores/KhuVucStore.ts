// import { defineStore } from 'pinia';
// import KhuVucApi from '@/api/KhuVucApi'; // Đảm bảo bạn đã có file api này

// export const useKhuVucStore = defineStore('khuVuc', {
//   state: () => ({
//     list: [] as any[] // Đây là cái "kho" rỗng ban đầu
//   }),
//   actions: {
//     // Hàm này sẽ đi lấy dữ liệu từ Backend và ném vào kho
//     async fetchKhuVuc() {
//       try {
//         const res = await KhuVucApi.getAll();
//         this.list = res.data;
//       } catch (error) {
//         console.error("Lỗi khi tải danh sách khu vực:", error);
//       }
//     }
//   }
// });