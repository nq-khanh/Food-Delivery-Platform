import * as z from "zod";

export const LoginSchema = z.object({
  usernameOrEmail: z.string().min(1, "Vui lòng nhập Username hoặc Email"),
  password: z.string().min(6, "Mật khẩu phải có ít nhất 6 ký tự"),
});

export type LoginInput = z.infer<typeof LoginSchema>;