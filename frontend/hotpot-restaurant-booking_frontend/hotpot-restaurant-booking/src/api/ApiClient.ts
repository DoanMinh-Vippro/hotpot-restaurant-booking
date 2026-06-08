import { useAuthStore } from "@/stores/AuthStore";
import axios from "axios";
const ApiClient = axios.create({
    baseURL: "http://localhost:8080/",
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