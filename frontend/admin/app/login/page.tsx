"use client";

import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { Button, Input, Form, message } from "antd";
import { loginAction } from "@/actions/auth";
import { useRouter } from "next/navigation"; // 1. Import useRouter
import { useState } from "react";

const loginSchema = z.object({
  usernameOrEmail: z.string().min(1, "Yêu cầu nhập tài khoản"),
  password: z.string().min(6, "Mật khẩu tối thiểu 6 ký tự"),
});

type LoginInput = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const router = useRouter(); // 2. Khởi tạo router
  const [loading, setLoading] = useState(false);

  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginInput>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      usernameOrEmail: "",
      password: "",
    },
  });

  const onFinish = async (values: LoginInput) => {
    setLoading(true);
    const res = await loginAction(values);
    setLoading(false);

    if (res.success) {
      message.success(res.message);
      router.replace("/admin/dashboard");
    } else {
      message.error(res.message);
    }
  };

  return (
    <div className="flex h-screen items-center justify-center bg-gray-100">
      <div className="w-full max-w-md p-8 bg-white rounded-lg shadow-md">
        <h1 className="text-2xl font-bold mb-6 text-center">Admin Login</h1>

        {/* Dùng handleSubmit của React Hook Form bọc ngoài onFinish */}
        <Form layout="vertical" onFinish={handleSubmit(onFinish)}>
          {/* Sử dụng Controller để kết nối Antd Input với React Hook Form */}
          <Form.Item label="Username/Email" validateStatus={errors.usernameOrEmail ? "error" : ""} help={errors.usernameOrEmail?.message}>
            <Controller
              name="usernameOrEmail"
              control={control}
              render={({ field }) => <Input {...field} placeholder="admin@example.com" size="large" />}
            />
          </Form.Item>

          <Form.Item label="Mật khẩu" validateStatus={errors.password ? "error" : ""} help={errors.password?.message}>
            <Controller
              name="password"
              control={control}
              render={({ field }) => <Input.Password {...field} placeholder="******" size="large" />}
            />
          </Form.Item>

          <Button
            type="primary"
            htmlType="submit"
            className="w-full h-10 mt-2"
            loading={loading} // Hiển thị loading khi đang gọi API
          >
            Đăng nhập
          </Button>
        </Form>
      </div>
    </div>
  );
}
