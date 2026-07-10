import { useAuthStore } from "@/stores/AuthStore";
import axios from "axios";

const getApiBaseUrl = () => {
    const configuredUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/";
    return configuredUrl.endsWith('/') ? configuredUrl : `${configuredUrl}/`;
};

const ApiClient = axios.create({
    baseURL: getApiBaseUrl(),
    headers: {
        "Content-Type": "application/json"
    }
});
ApiClient.interceptors.request.use((config) => {
    const authStore = useAuthStore();
    if(authStore.token){
        config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
})
export default ApiClient;