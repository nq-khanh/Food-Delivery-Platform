import axios from "axios";
import Cookies from "js-cookie";

const api = axios.create({
  baseURL: "http://localhost:8090",
});

// Request Interceptor: Gắn Access Token
api.interceptors.request.use((config) => {
  const token = Cookies.get("accessToken");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Response Interceptor: Xử lý lỗi 401 và Refresh Token
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Nếu lỗi 401 (hết hạn) và chưa thử refresh lần nào
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      const refreshToken = Cookies.get("refreshToken");

      if (refreshToken) {
        try {
          // Gọi API Refresh Token của Spring Boot
          // Swagger của bạn: POST /api/auth/refresh (gửi string token trong body)
          const res = await axios.post("http://localhost:8090/api/auth/refresh", refreshToken, {
            headers: { "Content-Type": "application/json" }
          });

          if (res.data.success) {
            const newAccessToken = res.data.data.accessToken;
            const newRefreshToken = res.data.data.refreshToken;

            // Cập nhật Cookie mới
            Cookies.set("accessToken", newAccessToken);
            Cookies.set("refreshToken", newRefreshToken);

            // Gắn token mới và thực hiện lại request ban đầu
            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
            return api(originalRequest);
          }
        } catch (refreshError) {
          // Refresh thất bại (hết hạn cả Refresh Token) -> Logout
          Cookies.remove("accessToken");
          Cookies.remove("refreshToken");
          Cookies.remove("userRole");
          window.location.href = "/login";
        }
      }
    }
    return Promise.reject(error);
  }
);

export default api;