const delay = (ms = 1000) => new Promise((r) => setTimeout(r, ms))

/**
 * POST /api/merchant/register
 * Payload shape theo api-docs.md §3.1
 *
 * @param {{
 *   name: string,
 *   address: string,
 *   latitude: number,
 *   longitude: number,
 *   description?: string,
 *   operating_hours: Array<{
 *     day_of_week: number,  // 0=CN, 1=T2 ... 6=T7
 *     open_time: string|null,
 *     close_time: string|null,
 *     is_closed: boolean
 *   }>
 * }} data
 */
export async function registerRestaurant(data) {
  await delay(1200)

  const mockResponse = {
    success: true,
    data: {
      restaurant_id: 'mock-restaurant-' + Date.now(),
      name: data.name,
      approval_status: 'PENDING',
    },
    message: 'Đăng ký thành công. Vui lòng chờ quản trị viên phê duyệt.',
  }

  // Persist so PendingPage can read it
  localStorage.setItem('pending_restaurant', JSON.stringify(mockResponse.data))
  return mockResponse.data
}

/**
 * GET /api/merchant/profile
 */
export async function getMerchantProfile() {
  await delay(500)
  const stored = localStorage.getItem('pending_restaurant')
  if (!stored) throw new Error('Không tìm thấy thông tin nhà hàng')
  return JSON.parse(stored)
}
