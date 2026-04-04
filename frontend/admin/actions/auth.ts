// src/actions/auth.ts
"use server";

import api from "@/lib/axios";
import { LoginSchema } from "@/schemas/auth";
import { ApiResponse, AuthResponse, LoginRequest } from "@/types/auth";
import { UserProfileResponse } from "@/types/user";
import { cookies } from "next/headers";

export async function loginAction(values: LoginRequest) {
  const cookieStore = await cookies(); // Khởi tạo sớm để dùng cho việc dọn dẹp
  
  const validatedFields = LoginSchema.safeParse(values);
  if (!validatedFields.success) return { success: false, message: "Dữ liệu không hợp lệ" };

  // Hàm helper để dọn dẹp cookie khi thất bại
  const clearAuthCookies = () => {
    cookieStore.delete("accessToken");
    cookieStore.delete("refreshToken");
    cookieStore.delete("userRole");
  };

  try {
    const response = await api.post<ApiResponse<AuthResponse>>("/api/auth/login", validatedFields.data);
    const result = response.data;

    if (!result.success) {
      clearAuthCookies(); // Xóa nếu sai pass/user
      return { 
        success: false, 
        message: result.error?.message || "Thông tin đăng nhập không chính xác" 
      };
    }

    if (result.data) {
      const token = result.data.accessToken;

      try {
        const profileRes = await api.get<ApiResponse<UserProfileResponse>>("/api/users/me", {
          headers: { Authorization: `Bearer ${token}` },
        });

        const userProfile = profileRes.data.data;

        // KIỂM TRA ROLE ADMIN
        if (userProfile.role !== "ADMIN") {
          clearAuthCookies(); // Xóa sạch nếu user đúng pass nhưng KHÔNG PHẢI ADMIN
          return { 
            success: false, 
            message: "Tài khoản của bạn không có quyền truy cập trang quản trị!" 
          };
        }

        // CHỈ GHI KHI TẤT CẢ ĐỀU THÀNH CÔNG
        cookieStore.set("accessToken", token, { httpOnly: false, maxAge: 60 * 60 * 24, path: "/" });
        cookieStore.set("refreshToken", result.data.refreshToken, { httpOnly: false, maxAge: 60 * 60 * 24 * 7, path: "/" });
        cookieStore.set("userRole", userProfile.role, { httpOnly: false, path: "/" });

        return { success: true, message: "Đăng nhập thành công" };

      } catch (profileError: any) {
        clearAuthCookies();
        return { success: false, message: "Lỗi xác thực quyền hạn" };
      }
    }

    clearAuthCookies();
    return { success: false, message: "Lỗi hệ thống: Dữ liệu trống" };

  } catch (error: any) {
    clearAuthCookies(); // Xóa nếu lỗi kết nối/server error (500)
    const serverMessage = error.response?.data?.error?.message;
    return { 
      success: false, 
      message: serverMessage || "Lỗi kết nối Backend" 
    };
  }
}