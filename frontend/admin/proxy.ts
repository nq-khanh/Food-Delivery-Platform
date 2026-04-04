import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function proxy(request: NextRequest) {
  const token = request.cookies.get('accessToken')?.value;
  const role = request.cookies.get('userRole')?.value;
  const { pathname } = request.nextUrl;

  // Nếu truy cập vào /admin mà thiếu token hoặc không phải ADMIN
  if (pathname.startsWith('/admin')) {
    if (!token || role !== 'ADMIN') {
      // Xóa sạch cookie rác nếu có (để tránh loop)
      const response = NextResponse.redirect(new URL('/login', request.url));
      response.cookies.delete('accessToken');
      response.cookies.delete('userRole');
      return response;
    }
  }

  // Chống quay lại trang Login nếu đã là Admin hợp lệ
  if (pathname.startsWith('/login') && token && role === 'ADMIN') {
    return NextResponse.redirect(new URL('/admin/dashboard', request.url));
  }

  return NextResponse.next();
}