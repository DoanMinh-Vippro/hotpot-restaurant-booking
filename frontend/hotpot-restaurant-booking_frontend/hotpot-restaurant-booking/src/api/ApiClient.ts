import { useAuthStore } from "@/stores/AuthStore";
import axios from "axios";

const getApiBaseUrl = () => {
    const configuredUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/";
    return configuredUrl.endsWith('/') ? configuredUrl : `${configuredUrl}/`;
};

const ApiClient = axios.create({
    baseURL: getApiBaseUrl(),
    headers: { "Content-Type": "application/json" }
});

ApiClient.interceptors.request.use((config) => {
    const authStore = useAuthStore();
    // Lấy token từ Pinia, nếu mất thì lấy từ localStorage
    const token = authStore.token || localStorage.getItem("token");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

ApiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            console.warn("Phiên đăng nhập hết hạn!");
            localStorage.removeItem("token");
            if (!window.location.pathname.includes("/auth")) {
                window.location.href = "/auth";
            }
        }
        return Promise.reject(error);
    }
);

export default ApiClient;