import api from "@/lib/axios";
import { ApiResponse } from "@/types/auth";
import { RestaurantResponse } from "@/types/restaurant";

export const restaurantService = {
  // Lấy danh sách chờ duyệt
  getPending: async () => {
    const res = await api.get<ApiResponse<RestaurantResponse[]>>("/api/admin/restaurants/pending");
    return res.data.data;
  },

  // Duyệt nhà hàng
  approve: async (id: string) => {
    const res = await api.patch<ApiResponse<RestaurantResponse>>(`/api/admin/restaurants/${id}/approve`);
    return res.data;
  },

  // Từ chối nhà hàng
  reject: async (id: string) => {
    const res = await api.patch<ApiResponse<RestaurantResponse>>(`/api/admin/restaurants/${id}/reject`);
    return res.data;
  }
};