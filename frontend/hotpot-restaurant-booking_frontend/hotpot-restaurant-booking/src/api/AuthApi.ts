import ApiClient from "./ApiClient";

const AuthApi = {
    // Hàm này gọi đến backend để đăng nhập
    login(data: { tenDangNhap: string, matKhau: string }) {
        // Gọi thẳng vào controller xử lý đăng nhập bên Java
        return ApiClient.post("/api/auth/login", data);
    },

    // Hàm này gọi đến backend để đăng ký
    register(data: any) {
        return ApiClient.post("/api/auth/register", data);
    }
}

export default AuthApi