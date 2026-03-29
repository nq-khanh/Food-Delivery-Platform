import axios from 'axios'

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

// Attach token automatically
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// ── Mock helpers ─────────────────────────────────────────
const delay = (ms = 800) => new Promise((r) => setTimeout(r, ms))

const MOCK_USER = {
  id: 'mock-uuid-001',
  email: '',
  first_name: '',
  last_name: '',
  roles: ['CUSTOMER'],
  is_verified: true,
}

// ── Auth Service ─────────────────────────────────────────

/**
 * POST /api/auth/login
 * Trả về access_token và lưu vào localStorage
 */
export async function login({ email, password }) {
  await delay(900)

  // Mock validation
  if (!email || !password) throw new Error('Email và mật khẩu là bắt buộc')

  // Simulate a successful login
  const mockResponse = {
    success: true,
    data: {
      access_token: 'mock.jwt.token.' + btoa(email),
      refresh_token: 'mock.refresh.token',
      expires_in: 900,
      user: { ...MOCK_USER, email },
    },
    message: 'Đăng nhập thành công',
  }

  localStorage.setItem('access_token', mockResponse.data.access_token)
  localStorage.setItem('user', JSON.stringify(mockResponse.data.user))
  return mockResponse.data
}

/**
 * POST /api/auth/register
 * Tạo tài khoản mới
 */
export async function register({ email, password, first_name, last_name, phone }) {
  await delay(1000)

  const mockResponse = {
    success: true,
    data: {
      id: 'mock-uuid-' + Date.now(),
      email,
      first_name,
      last_name,
      is_verified: false,
    },
    message: 'Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.',
  }

  // Store temp token for next step (setup restaurant)
  localStorage.setItem('access_token', 'mock.jwt.token.' + btoa(email))
  localStorage.setItem('user', JSON.stringify({ ...mockResponse.data, roles: ['CUSTOMER'] }))
  return mockResponse.data
}

/**
 * POST /api/auth/logout
 */
export function logout() {
  localStorage.removeItem('access_token')
  localStorage.removeItem('user')
}

export default api
