import api from "@/lib/axios";
import { ApiResponse } from "@/types/auth";
import { ShipperResponse } from "@/types/shipper";

export const shipperService = {
  // Lấy danh sách shipper đang chờ duyệt
  getPending: async () => {
    const res = await api.get<ApiResponse<ShipperResponse[]>>("/api/admin/shippers/pending");
    return res.data.data;
  },

  // Duyệt shipper
  approve: async (id: string) => {
    const res = await api.patch<ApiResponse<ShipperResponse>>(`/api/admin/shippers/${id}/approve`);
    return res.data;
  },

  // Từ chối shipper
  reject: async (id: string) => {
    const res = await api.patch<ApiResponse<ShipperResponse>>(`/api/admin/shippers/${id}/reject`);
    return res.data;
  }
};