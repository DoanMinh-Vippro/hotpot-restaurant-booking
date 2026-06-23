import ApiClient from "./ApiClient";

const AuthApi = {
    // Hàm này gọi đến backend để đăng nhập
    login(data: { tenDangNhap: string, matKhau: string }) {
        // Gọi thẳng vào controller xử lý đăng nhập bên Java
        return ApiClient.post("/api/auth/login", data);
    },

    // Hàm này gọi đến backend để đăng ký tài khoản đơn giản
    register(data: any) {
        return ApiClient.post("/api/auth/register", data);
    },

    // Hàm này gọi đến backend để đăng ký khách hàng với đầy đủ thông tin
    registerCustomer(data: {
        tenDangNhap: string,
        matKhau: string,
        tenKhachHang: string,
        gioiTinh: boolean,
        soDienThoai: string,
        email: string,
        diaChi: string
    }) {
        return ApiClient.post("/api/auth/register-customer", data);
    }
}

export default AuthApi