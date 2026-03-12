# Tài liệu Thiết kế API — Food Delivery Platform

> **Phiên bản**: 1.0  
> **Cập nhật**: 2025  
> **Base URL**: `https://api.fooddelivery.vn`

---

## Quy ước chung

### Xác thực
Các endpoint yêu cầu xác thực phải gửi kèm header:
```
Authorization: Bearer <access_token>
```

### Cấu trúc Response thành công
```json
{
  "success": true,
  "data": { },
  "message": "OK"
}
```

### Cấu trúc Response lỗi
```json
{
  "success": false,
  "errorCode": "ERROR_CODE",
  "message": "Mô tả lỗi chi tiết"
}
```

### Một số Error Code phổ biến

| errorCode | HTTP Status | Ý nghĩa |
|-----------|-------------|---------|
| `UNAUTHORIZED` | 401 | Chưa đăng nhập hoặc token hết hạn |
| `FORBIDDEN` | 403 | Không có quyền thực hiện |
| `NOT_FOUND` | 404 | Tài nguyên không tồn tại |
| `VALIDATION_ERROR` | 422 | Dữ liệu đầu vào không hợp lệ |
| `ORDER_INVALID_STATE` | 409 | Đơn hàng không thể chuyển sang trạng thái yêu cầu |
| `DUPLICATE_REQUEST` | 409 | Yêu cầu bị trùng lặp (idempotency) |
| `INSUFFICIENT_BALANCE` | 400 | Số dư ví không đủ |

---

# 1. Xác thực & Hồ sơ người dùng (Auth / Profile)

---

## 1.1 Đăng ký tài khoản

**`POST /api/auth/register`**

**Chức năng**: Tạo tài khoản người dùng mới (Customer). Gửi email xác thực sau khi đăng ký thành công.

**Authentication**: ❌ Không yêu cầu

**Request Body**:
```json
{
  "email": "nguyen.van.a@gmail.com",
  "password": "Str0ngP@ssword",
  "first_name": "An",
  "last_name": "Nguyễn Văn",
  "phone": "0901234567"
}
```

| Field | Kiểu | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `email` | string | ✅ | Email đăng nhập, phải duy nhất |
| `password` | string | ✅ | Mật khẩu (tối thiểu 8 ký tự) |
| `first_name` | string | ✅ | Tên (≤ 50 ký tự) |
| `last_name` | string | ✅ | Họ (≤ 50 ký tự) |
| `phone` | string | ✅ | Số điện thoại, phải duy nhất |

**Response** `201 Created`:
```json
{
  "success": true,
  "data": {
    "id": "uuid-...",
    "email": "nguyen.van.a@gmail.com",
    "first_name": "An",
    "last_name": "Nguyễn Văn",
    "is_verified": false
  },
  "message": "Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản."
}
```

**Logic Database**:
- **INSERT** `users` (password được hash trước khi lưu)
- **INSERT** `user_roles` (gán role CUSTOMER mặc định)
- **INSERT** `wallets` (khởi tạo ví với balance = 0)
- **INSERT** `user_verifications` (type = EMAIL_VERIFY, lưu token_hash)

---

## 1.2 Đăng nhập

**`POST /api/auth/login`**

**Chức năng**: Xác thực người dùng, trả về access token và refresh token.

**Authentication**: ❌ Không yêu cầu

**Request Body**:
```json
{
  "email": "nguyen.van.a@gmail.com",
  "password": "Str0ngP@ssword"
}
```

| Field | Kiểu | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `email` | string | ✅ | Email đã đăng ký |
| `password` | string | ✅ | Mật khẩu |

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "access_token": "eyJhbGci...",
    "refresh_token": "eyJhbGci...",
    "expires_in": 900,
    "user": {
      "id": "uuid-...",
      "email": "nguyen.van.a@gmail.com",
      "first_name": "An",
      "last_name": "Nguyễn Văn",
      "roles": ["CUSTOMER"],
      "is_verified": true
    }
  },
  "message": "Đăng nhập thành công"
}
```

**Logic Database**:
- **SELECT** `users` theo email, kiểm tra `is_active = true`
- **SELECT** `user_roles` → `roles` để lấy danh sách vai trò
- **INSERT** `refresh_tokens` (lưu token mới, set `expiry_date`)

---

## 1.3 Làm mới Access Token

**`POST /api/auth/refresh-token`**

**Chức năng**: Sử dụng refresh token hợp lệ để cấp access token mới mà không cần đăng nhập lại.

**Authentication**: ❌ Không yêu cầu (dùng refresh token)

**Request Body**:
```json
{
  "refresh_token": "eyJhbGci..."
}
```

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "access_token": "eyJhbGci...",
    "expires_in": 900
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `refresh_tokens` theo token, kiểm tra `is_revoked = false` và `expiry_date > NOW()`
- **UPDATE** `refresh_tokens` set `is_revoked = true` (token rotation)
- **INSERT** `refresh_tokens` (cấp refresh token mới)

---

## 1.4 Quên mật khẩu

**`POST /api/auth/forgot-password`**

**Chức năng**: Gửi email chứa link đặt lại mật khẩu cho người dùng.

**Authentication**: ❌ Không yêu cầu

**Request Body**:
```json
{
  "email": "nguyen.van.a@gmail.com"
}
```

**Response** `200 OK`:
```json
{
  "success": true,
  "data": null,
  "message": "Nếu email tồn tại, link đặt lại mật khẩu sẽ được gửi trong vài phút."
}
```

> **Lưu ý bảo mật**: Response luôn trả về 200 dù email có tồn tại hay không, để tránh lộ thông tin.

**Logic Database**:
- **SELECT** `users` theo email
- **DELETE** `user_verifications` (xóa token cũ chưa dùng, cùng type)
- **INSERT** `user_verifications` (type = PASSWORD_RESET, set `expires_at`)

---

## 1.5 Đặt lại mật khẩu

**`POST /api/auth/reset-password`**

**Chức năng**: Đặt mật khẩu mới bằng token xác thực được gửi qua email.

**Authentication**: ❌ Không yêu cầu

**Request Body**:
```json
{
  "token": "reset-token-from-email",
  "new_password": "NewStr0ng@123"
}
```

| Field | Kiểu | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `token` | string | ✅ | Token nhận từ email |
| `new_password` | string | ✅ | Mật khẩu mới (tối thiểu 8 ký tự) |

**Response** `200 OK`:
```json
{
  "success": true,
  "data": null,
  "message": "Mật khẩu đã được đặt lại thành công. Vui lòng đăng nhập lại."
}
```

**Logic Database**:
- **SELECT** `user_verifications` theo `token_hash`, kiểm tra `type = PASSWORD_RESET` và `expires_at > NOW()`
- **UPDATE** `users` set `password_hash` mới
- **DELETE** `user_verifications` (xóa token đã dùng)
- **UPDATE** `refresh_tokens` set `is_revoked = true` (thu hồi toàn bộ session cũ)

---

## 1.6 Đổi mật khẩu

**`POST /api/auth/change-password`**

**Chức năng**: Người dùng đã đăng nhập tự đổi mật khẩu của mình.

**Authentication**: ✅ Bearer Token

**Request Body**:
```json
{
  "current_password": "OldP@ssword",
  "new_password": "NewStr0ng@123"
}
```

**Response** `200 OK`:
```json
{
  "success": true,
  "data": null,
  "message": "Mật khẩu đã được thay đổi thành công."
}
```

**Logic Database**:
- **SELECT** `users` theo ID từ token, xác minh `current_password`
- **UPDATE** `users` set `password_hash` mới
- **UPDATE** `refresh_tokens` set `is_revoked = true` (đăng xuất các thiết bị khác)

---

## 1.7 Xem thông tin cá nhân

**`GET /api/users/me`**

**Chức năng**: Lấy thông tin hồ sơ của người dùng hiện tại.

**Authentication**: ✅ Bearer Token

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "id": "uuid-...",
    "email": "nguyen.van.a@gmail.com",
    "first_name": "An",
    "last_name": "Nguyễn Văn",
    "phone": "0901234567",
    "avatar_url": "https://cdn.example.com/avatars/uuid.jpg",
    "is_verified": true,
    "is_active": true,
    "roles": ["CUSTOMER"],
    "created_at": "2024-01-15T08:30:00Z"
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `users` theo ID từ JWT
- **SELECT** `user_roles` → `roles`

---

## 1.8 Cập nhật thông tin cá nhân

**`PUT /api/users/me`**

**Chức năng**: Cập nhật thông tin hồ sơ cá nhân (tên, số điện thoại, ảnh đại diện).

**Authentication**: ✅ Bearer Token

**Request Body**:
```json
{
  "first_name": "An",
  "last_name": "Trần Văn",
  "phone": "0909999888",
  "avatar_url": "https://cdn.example.com/avatars/new.jpg"
}
```

| Field | Kiểu | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `first_name` | string | ❌ | Tên (≤ 50 ký tự) |
| `last_name` | string | ❌ | Họ (≤ 50 ký tự) |
| `phone` | string | ❌ | Số điện thoại mới (phải duy nhất) |
| `avatar_url` | string | ❌ | URL ảnh đại diện |

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "id": "uuid-...",
    "first_name": "An",
    "last_name": "Trần Văn",
    "phone": "0909999888",
    "avatar_url": "https://cdn.example.com/avatars/new.jpg"
  },
  "message": "Cập nhật thông tin thành công"
}
```

**Logic Database**:
- **UPDATE** `users` set các trường được cập nhật

---

## 1.9 Danh sách địa chỉ giao hàng

**`GET /api/users/me/addresses`**

**Chức năng**: Lấy toàn bộ địa chỉ giao hàng đã lưu của người dùng.

**Authentication**: ✅ Bearer Token

**Response** `200 OK`:
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-...",
      "address_name": "Nhà riêng",
      "full_address": "123 Nguyễn Huệ, Quận 1, TP.HCM",
      "latitude": 10.77609800,
      "longitude": 106.70115300,
      "is_default": true
    },
    {
      "id": "uuid-...",
      "address_name": "Công ty",
      "full_address": "456 Lê Lợi, Quận 1, TP.HCM",
      "latitude": 10.77450000,
      "longitude": 106.69800000,
      "is_default": false
    }
  ],
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `user_addresses` WHERE `user_id = current_user_id`

---

## 1.10 Thêm địa chỉ giao hàng

**`POST /api/users/me/addresses`**

**Chức năng**: Thêm địa chỉ giao hàng mới. Nếu `is_default = true`, địa chỉ mặc định cũ sẽ bị hủy.

**Authentication**: ✅ Bearer Token

**Request Body**:
```json
{
  "address_name": "Nhà bạn bè",
  "full_address": "789 Hai Bà Trưng, Quận 3, TP.HCM",
  "latitude": 10.78900000,
  "longitude": 106.68500000,
  "is_default": false
}
```

| Field | Kiểu | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `address_name` | string | ✅ | Tên gọi ngắn (≤ 50 ký tự) |
| `full_address` | string | ✅ | Địa chỉ đầy đủ |
| `latitude` | number | ✅ | Vĩ độ |
| `longitude` | number | ✅ | Kinh độ |
| `is_default` | boolean | ❌ | Mặc định: false |

**Response** `201 Created`:
```json
{
  "success": true,
  "data": {
    "id": "uuid-...",
    "address_name": "Nhà bạn bè",
    "full_address": "789 Hai Bà Trưng, Quận 3, TP.HCM",
    "latitude": 10.78900000,
    "longitude": 106.68500000,
    "is_default": false
  },
  "message": "Thêm địa chỉ thành công"
}
```

**Logic Database**:
- Nếu `is_default = true`: **UPDATE** `user_addresses` set `is_default = false` WHERE `user_id = current_user_id`
- **INSERT** `user_addresses`
- *Ràng buộc*: Unique partial index `idx_only_one_default_address` đảm bảo chỉ 1 địa chỉ mặc định

---

## 1.11 Cập nhật địa chỉ giao hàng

**`PUT /api/users/me/addresses/{id}`**

**Chức năng**: Cập nhật thông tin địa chỉ giao hàng theo ID.

**Authentication**: ✅ Bearer Token

**Path Params**: `id` — UUID của địa chỉ

**Request Body**:
```json
{
  "address_name": "Nhà riêng (mới)",
  "full_address": "100 Lê Duẩn, Quận 1, TP.HCM",
  "latitude": 10.77800000,
  "longitude": 106.70300000,
  "is_default": true
}
```

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "id": "uuid-...",
    "address_name": "Nhà riêng (mới)",
    "full_address": "100 Lê Duẩn, Quận 1, TP.HCM",
    "latitude": 10.77800000,
    "longitude": 106.70300000,
    "is_default": true
  },
  "message": "Cập nhật địa chỉ thành công"
}
```

**Logic Database**:
- **SELECT** `user_addresses` kiểm tra quyền sở hữu (`user_id = current_user_id`)
- Nếu `is_default = true`: **UPDATE** `user_addresses` set `is_default = false` cho các địa chỉ khác
- **UPDATE** `user_addresses`

---

## 1.12 Xóa địa chỉ giao hàng

**`DELETE /api/users/me/addresses/{id}`**

**Chức năng**: Xóa địa chỉ giao hàng theo ID.

**Authentication**: ✅ Bearer Token

**Path Params**: `id` — UUID của địa chỉ

**Response** `200 OK`:
```json
{
  "success": true,
  "data": null,
  "message": "Đã xóa địa chỉ thành công"
}
```

**Logic Database**:
- **SELECT** `user_addresses` kiểm tra `user_id = current_user_id`
- **DELETE** `user_addresses` theo ID (ON DELETE CASCADE từ `users`)

---

# 2. Khách hàng — Tìm kiếm, Giỏ hàng & Đặt hàng

---

## 2.1 Tìm nhà hàng gần đây

**`GET /api/catalog/restaurants/nearby`**

**Chức năng**: Tìm kiếm nhà hàng theo vị trí địa lý (bán kính) và từ khóa. Hỗ trợ tìm kiếm ngữ nghĩa bằng AI nếu `keyword` có nội dung.

**Authentication**: ❌ Không yêu cầu

**Query Params**:

| Param | Kiểu | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `lat` | number | ✅ | Vĩ độ vị trí hiện tại |
| `lng` | number | ✅ | Kinh độ vị trí hiện tại |
| `keyword` | string | ❌ | Từ khóa tìm kiếm (tên nhà hàng, loại món) |
| `radius_km` | number | ❌ | Bán kính tìm kiếm (mặc định: 5km) |
| `page` | int | ❌ | Số trang (mặc định: 1) |
| `size` | int | ❌ | Số kết quả mỗi trang (mặc định: 20) |

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "total": 42,
    "page": 1,
    "size": 20,
    "items": [
      {
        "id": "uuid-...",
        "name": "Phở Bắc Hà",
        "address": "45 Nguyễn Trãi, Quận 5, TP.HCM",
        "rating_avg": 4.5,
        "distance_km": 1.2,
        "is_active": true,
        "cover_image_url": "https://cdn.example.com/restaurants/uuid.jpg",
        "operating_today": {
          "open_time": "07:00",
          "close_time": "22:00",
          "is_open_now": true
        }
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `restaurants` WHERE `is_active = true` AND `approval_status = 'APPROVED'`
- Dùng Haversine formula hoặc PostGIS để tính khoảng cách theo `lat`/`lng`
- Nếu có `keyword`: vector similarity search qua **`restaurant_embeddings`** (pgvector cosine)
- **SELECT** `restaurant_operating_hours` để lấy giờ hôm nay
- **SELECT** `media_files` (entity_type = 'RESTAURANT') để lấy ảnh bìa

---

## 2.2 Tìm kiếm món ăn thông minh (AI Search)

**`GET /api/catalog/products/search`**

**Chức năng**: Tìm kiếm món ăn theo từ khóa ngữ nghĩa (ví dụ: "món cay đậm đà", "đồ ăn chay"). Sử dụng vector embedding để cho kết quả liên quan.

**Authentication**: ❌ Không yêu cầu

**Query Params**:

| Param | Kiểu | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `q` | string | ✅ | Cụm từ tìm kiếm ngữ nghĩa |
| `context` | string | ❌ | Ngữ cảnh bổ sung (lat,lng để ưu tiên nhà hàng gần) |
| `page` | int | ❌ | Số trang |
| `size` | int | ❌ | Kết quả mỗi trang |

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "total": 15,
    "items": [
      {
        "product_id": "uuid-...",
        "product_name": "Bún bò Huế đặc biệt",
        "price": 75000.00,
        "description": "Bún bò cay nồng đặc trưng xứ Huế",
        "is_available": true,
        "restaurant": {
          "id": "uuid-...",
          "name": "Bún Bò Cô Hai",
          "rating_avg": 4.3
        },
        "image_url": "https://cdn.example.com/products/uuid.jpg"
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:
- Vector embedding query `q` → tìm trong **`product_embeddings`** bằng cosine similarity (HNSW index)
- **JOIN** `products` (WHERE `is_available = true`)
- **JOIN** `restaurants` (WHERE `is_active = true` AND `approval_status = 'APPROVED'`)
- **SELECT** `media_files` (entity_type = 'PRODUCT')

---

## 2.3 Xem menu nhà hàng

**`GET /api/catalog/restaurants/{id}/products`**

**Chức năng**: Lấy toàn bộ menu của một nhà hàng, nhóm theo danh mục.

**Authentication**: ❌ Không yêu cầu

**Path Params**: `id` — UUID của nhà hàng

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "restaurant": {
      "id": "uuid-...",
      "name": "Phở Bắc Hà",
      "rating_avg": 4.5,
      "is_active": true
    },
    "menu": [
      {
        "category_id": 1,
        "category_name": "Món chính",
        "display_order": 1,
        "products": [
          {
            "id": "uuid-...",
            "name": "Phở bò tái nạm",
            "price": 65000.00,
            "description": "Phở truyền thống với tái và nạm",
            "is_available": true,
            "image_url": "https://cdn.example.com/products/uuid.jpg"
          }
        ]
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `restaurants` theo ID
- **SELECT** `categories` WHERE `restaurant_id = {id}` ORDER BY `display_order`
- **SELECT** `products` WHERE `restaurant_id = {id}` (GROUP BY `category_id`)
- **SELECT** `media_files` (entity_type = 'PRODUCT')

---

## 2.4 Thêm sản phẩm vào giỏ hàng

**`POST /api/cart/items`**

**Chức năng**: Thêm món ăn vào giỏ hàng (lưu trên server, gắn với session/user). Giỏ hàng chỉ chứa sản phẩm từ **một nhà hàng duy nhất**.

**Authentication**: ✅ Bearer Token

**Request Body**:
```json
{
  "product_id": "uuid-...",
  "quantity": 2
}
```

| Field | Kiểu | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `product_id` | UUID | ✅ | ID sản phẩm |
| `quantity` | int | ✅ | Số lượng (> 0) |

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "cart_id": "session-uuid-...",
    "restaurant_id": "uuid-...",
    "items": [
      {
        "item_id": "cart-item-uuid",
        "product_id": "uuid-...",
        "product_name": "Phở bò tái nạm",
        "price": 65000.00,
        "quantity": 2,
        "subtotal": 130000.00
      }
    ],
    "total": 130000.00
  },
  "message": "Đã thêm vào giỏ hàng"
}
```

> **Lưu ý**: Nếu giỏ hàng đã có sản phẩm từ nhà hàng khác, trả về lỗi `CART_RESTAURANT_CONFLICT` yêu cầu người dùng xác nhận xóa giỏ cũ.

**Logic Database**:
- **SELECT** `products` kiểm tra `is_available = true`
- Giỏ hàng lưu trong Redis/Cache hoặc bảng cart tạm thời

---

## 2.5 Cập nhật số lượng trong giỏ hàng

**`PUT /api/cart/items/{id}`**

**Chức năng**: Thay đổi số lượng của một sản phẩm trong giỏ. Nếu `quantity = 0`, xóa sản phẩm khỏi giỏ.

**Authentication**: ✅ Bearer Token

**Path Params**: `id` — ID cart item

**Request Body**:
```json
{
  "quantity": 3
}
```

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "item_id": "cart-item-uuid",
    "quantity": 3,
    "subtotal": 195000.00,
    "cart_total": 195000.00
  },
  "message": "Đã cập nhật giỏ hàng"
}
```

**Logic Database**: Cập nhật giỏ hàng trong cache

---

## 2.6 Xóa sản phẩm khỏi giỏ hàng

**`DELETE /api/cart/items/{id}`**

**Chức năng**: Xóa một sản phẩm khỏi giỏ hàng.

**Authentication**: ✅ Bearer Token

**Path Params**: `id` — ID cart item

**Response** `200 OK`:
```json
{
  "success": true,
  "data": null,
  "message": "Đã xóa sản phẩm khỏi giỏ hàng"
}
```

---

## 2.7 Kiểm tra Voucher

**`POST /api/vouchers/validate`**

**Chức năng**: Kiểm tra mã voucher có hợp lệ và tính toán số tiền được giảm dựa trên tổng giá trị đơn hàng hiện tại.

**Authentication**: ✅ Bearer Token

**Request Body**:
```json
{
  "code": "SUMMER30",
  "order_value": 250000.00
}
```

| Field | Kiểu | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `code` | string | ✅ | Mã voucher |
| `order_value` | number | ✅ | Tổng giá trị đơn hàng (≥ 0) |

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "voucher_id": "uuid-...",
    "code": "SUMMER30",
    "discount_type": "PERCENTAGE",
    "discount_value": 30,
    "discount_amount": 75000.00,
    "max_discount_amount": 100000.00,
    "final_discount": 75000.00,
    "is_valid": true
  },
  "message": "Voucher hợp lệ"
}
```

**Logic Database**:
- **SELECT** `vouchers` WHERE `code = 'SUMMER30'`
- Kiểm tra: `is_active = true`, `expiry_date > NOW()`, `used_count < usage_limit`, `order_value >= min_order_value`
- Tính `discount_amount`: nếu PERCENTAGE thì `MIN(order_value * discount_value%, max_discount_amount)`

---

## 2.8 Đặt hàng (Checkout)

**`POST /api/orders/checkout`**

**Chức năng**: Tạo đơn hàng mới từ giỏ hàng. Tính toán và lưu snapshot giá, phí ship, hoa hồng tại thời điểm đặt hàng. Idempotency key bắt buộc.

**Authentication**: ✅ Bearer Token

**Request Header**:
```
Idempotency-Key: client-generated-uuid
```

**Request Body**:
```json
{
  "restaurant_id": "uuid-...",
  "delivery_address_id": "uuid-...",
  "voucher_code": "SUMMER30",
  "payment_method": "VNPAY",
  "note": "Ít cay, không hành"
}
```

| Field | Kiểu | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `restaurant_id` | UUID | ✅ | ID nhà hàng |
| `delivery_address_id` | UUID | ✅ | ID địa chỉ giao hàng |
| `voucher_code` | string | ❌ | Mã giảm giá (nếu có) |
| `payment_method` | string | ✅ | VNPAY hoặc WALLET |
| `note` | string | ❌ | Ghi chú cho nhà hàng |

**Response** `201 Created`:
```json
{
  "success": true,
  "data": {
    "order_id": "uuid-...",
    "order_code": "FD20240115001",
    "subtotal": 250000.00,
    "shipping_fee": 25000.00,
    "discount_amount": 75000.00,
    "total_amount": 200000.00,
    "order_status": "PENDING",
    "payment_status": "UNPAID",
    "payment_url": "https://sandbox.vnpayment.vn/paymentv2/..."
  },
  "message": "Đặt hàng thành công"
}
```

**Logic Database**:
- **SELECT** `products` + giá hiện tại, **SELECT** `shipping_configs` (lấy config đang áp dụng)
- **SELECT** `vouchers`, `system_configs` (commission_rate)
- **INSERT** `orders` (snapshot: `subtotal`, `shipping_fee`, `discount_amount`, `total_amount`, `commission_rate`, `commission_amount`, địa chỉ giao hàng)
- **INSERT** `order_items` (lưu `price_at_purchase` snapshot)
- **INSERT** `order_status_history` (status = PENDING)
- **UPDATE** `vouchers` tăng `used_count` (nếu có voucher)

---

## 2.9 Danh sách đơn hàng của tôi

**`GET /api/orders/my`**

**Chức năng**: Lấy lịch sử đơn hàng của người dùng hiện tại, sắp xếp theo thời gian mới nhất.

**Authentication**: ✅ Bearer Token

**Query Params**:

| Param | Kiểu | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `status` | string | ❌ | Lọc theo trạng thái: PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED |
| `page` | int | ❌ | Số trang |
| `size` | int | ❌ | Kết quả mỗi trang |

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "total": 25,
    "page": 1,
    "items": [
      {
        "order_id": "uuid-...",
        "order_code": "FD20240115001",
        "restaurant_name": "Phở Bắc Hà",
        "total_amount": 200000.00,
        "order_status": "COMPLETED",
        "payment_status": "PAID",
        "placed_at": "2024-01-15T12:30:00Z",
        "has_review": true
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `orders` WHERE `customer_id = current_user_id`
- **JOIN** `restaurants` (lấy tên)
- **SELECT** `order_reviews` (kiểm tra đã review chưa)

---

## 2.10 Chi tiết đơn hàng

**`GET /api/orders/{id}`**

**Chức năng**: Xem thông tin chi tiết của một đơn hàng bao gồm danh sách món, trạng thái giao hàng.

**Authentication**: ✅ Bearer Token

**Path Params**: `id` — UUID đơn hàng

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "order_id": "uuid-...",
    "order_code": "FD20240115001",
    "order_status": "SHIPPING",
    "payment_status": "PAID",
    "restaurant": {
      "id": "uuid-...",
      "name": "Phở Bắc Hà",
      "address": "45 Nguyễn Trãi, Quận 5"
    },
    "items": [
      {
        "product_name": "Phở bò tái nạm",
        "quantity": 2,
        "price_at_purchase": 65000.00,
        "subtotal": 130000.00
      }
    ],
    "delivery_address": "123 Nguyễn Huệ, Quận 1, TP.HCM",
    "subtotal": 130000.00,
    "shipping_fee": 25000.00,
    "discount_amount": 0.00,
    "total_amount": 155000.00,
    "shipper": {
      "name": "Trần Văn B",
      "phone": "0912345678",
      "current_lat": 10.77500000,
      "current_lng": 106.70000000
    },
    "placed_at": "2024-01-15T12:30:00Z",
    "status_history": [
      { "status": "PENDING", "changed_at": "2024-01-15T12:30:00Z" },
      { "status": "CONFIRMED", "changed_at": "2024-01-15T12:35:00Z" },
      { "status": "SHIPPING", "changed_at": "2024-01-15T12:50:00Z" }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `orders`, `order_items`, `restaurants`, `shippers`
- **SELECT** `order_status_history` ORDER BY `changed_at`

---

## 2.11 Hủy đơn hàng

**`POST /api/orders/{id}/cancel`**

**Chức năng**: Khách hàng hủy đơn hàng. Chỉ được hủy khi đơn ở trạng thái `PENDING` hoặc `CONFIRMED`.

**Authentication**: ✅ Bearer Token

**Path Params**: `id` — UUID đơn hàng

**Request Body**:
```json
{
  "reason": "Đặt nhầm món"
}
```

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "order_id": "uuid-...",
    "order_status": "CANCELLED",
    "refund_status": "Hoàn tiền sẽ được xử lý trong 1-3 ngày làm việc"
  },
  "message": "Đã hủy đơn hàng thành công"
}
```

**Logic Database**:
- **SELECT** `orders` kiểm tra `customer_id` và `order_status IN ('PENDING', 'CONFIRMED')`
- **UPDATE** `orders` set `order_status = 'CANCELLED'`, `cancelled_at = NOW()`
- **INSERT** `order_status_history`
- **UPDATE** `vouchers` giảm `used_count` (nếu có dùng voucher)
- Nếu đã thanh toán: trigger hoàn tiền (xử lý async)

---

## 2.12 Đánh giá đơn hàng

**`POST /api/orders/{id}/review`**

**Chức năng**: Gửi đánh giá cho đơn hàng đã hoàn thành. Chỉ được đánh giá đơn có `order_status = COMPLETED`. Mỗi đơn chỉ được đánh giá một lần.

**Authentication**: ✅ Bearer Token

**Path Params**: `id` — UUID đơn hàng

**Request Body**:
```json
{
  "rating": 5,
  "comment": "Phở ngon, giao hàng nhanh!",
  "item_reviews": [
    {
      "order_item_id": 101,
      "product_id": "uuid-...",
      "rating": 5,
      "comment": "Sợi phở dai ngon"
    }
  ]
}
```

| Field | Kiểu | Bắt buộc | Ràng buộc |
|-------|------|----------|-----------|
| `rating` | int | ✅ | 1 ≤ rating ≤ 5 |
| `comment` | string | ❌ | Nội dung bình luận |
| `item_reviews[].rating` | int | ✅ | 1 ≤ rating ≤ 5 |

**Response** `201 Created`:
```json
{
  "success": true,
  "data": {
    "review_id": "uuid-...",
    "rating": 5,
    "comment": "Phở ngon, giao hàng nhanh!",
    "created_at": "2024-01-15T15:00:00Z"
  },
  "message": "Cảm ơn bạn đã đánh giá!"
}
```

**Logic Database**:
- **SELECT** `orders` kiểm tra `customer_id`, `order_status = 'COMPLETED'`
- **INSERT** `order_reviews` (1:1 với order → unique constraint `order_id`)
- **INSERT** `order_item_reviews` cho từng item
- **UPDATE** `restaurants.rating_avg` (tính lại average: `AVG(rating)` từ `order_reviews`)

---

## 2.13 Xem đánh giá đơn hàng

**`GET /api/orders/{id}/review`**

**Chức năng**: Lấy thông tin đánh giá đã gửi cho đơn hàng.

**Authentication**: ✅ Bearer Token

**Path Params**: `id` — UUID đơn hàng

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "review_id": "uuid-...",
    "rating": 5,
    "comment": "Phở ngon, giao hàng nhanh!",
    "item_reviews": [
      {
        "product_name": "Phở bò tái nạm",
        "rating": 5,
        "comment": "Sợi phở dai ngon"
      }
    ],
    "created_at": "2024-01-15T15:00:00Z"
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `order_reviews` WHERE `order_id`
- **SELECT** `order_item_reviews` JOIN `order_items` JOIN `products`

---

# 3. Nhà hàng (Merchant)

---

## 3.1 Đăng ký nhà hàng

**`POST /api/merchant/register`**

**Chức năng**: Chủ quán đăng ký nhà hàng mới. Trạng thái ban đầu là `PENDING`, chờ admin phê duyệt.

**Authentication**: ✅ Bearer Token (user có role RESTAURANT_OWNER)

**Request Body**:
```json
{
  "name": "Phở Bắc Hà",
  "address": "45 Nguyễn Trãi, Quận 5, TP.HCM",
  "latitude": 10.75800000,
  "longitude": 106.68200000,
  "description": "Phở truyền thống Hà Nội chính gốc",
  "operating_hours": [
    { "day_of_week": 1, "open_time": "07:00", "close_time": "22:00", "is_closed": false },
    { "day_of_week": 0, "open_time": null, "close_time": null, "is_closed": true }
  ]
}
```

| Field | Kiểu | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `name` | string | ✅ | Tên nhà hàng (≤ 255 ký tự) |
| `address` | string | ✅ | Địa chỉ đầy đủ |
| `latitude` | number | ✅ | Vĩ độ |
| `longitude` | number | ✅ | Kinh độ |
| `description` | string | ❌ | Mô tả nhà hàng |
| `operating_hours` | array | ✅ | Giờ hoạt động (0=CN, 1=T2, ..., 6=T7) |

**Response** `201 Created`:
```json
{
  "success": true,
  "data": {
    "restaurant_id": "uuid-...",
    "name": "Phở Bắc Hà",
    "approval_status": "PENDING"
  },
  "message": "Đăng ký thành công. Vui lòng chờ quản trị viên phê duyệt."
}
```

**Logic Database**:
- **INSERT** `restaurants` (approval_status = 'PENDING', is_active = false)
- **INSERT** `restaurant_operating_hours` (mỗi ngày trong tuần)
- **UPDATE** `user_roles` thêm role RESTAURANT_OWNER nếu chưa có

---

## 3.2 Xem hồ sơ nhà hàng

**`GET /api/merchant/profile`**

**Chức năng**: Lấy thông tin nhà hàng của chủ quán đang đăng nhập.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "id": "uuid-...",
    "name": "Phở Bắc Hà",
    "address": "45 Nguyễn Trãi, Quận 5, TP.HCM",
    "latitude": 10.75800000,
    "longitude": 106.68200000,
    "description": "Phở truyền thống Hà Nội chính gốc",
    "approval_status": "APPROVED",
    "rating_avg": 4.5,
    "is_active": true,
    "operating_hours": [
      { "day_of_week": 1, "open_time": "07:00", "close_time": "22:00", "is_closed": false }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `restaurants` WHERE `owner_id = current_user_id`
- **SELECT** `restaurant_operating_hours`

---

## 3.3 Cập nhật hồ sơ nhà hàng

**`PUT /api/merchant/profile`**

**Chức năng**: Chủ quán cập nhật thông tin nhà hàng.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Request Body**:
```json
{
  "name": "Phở Bắc Hà Premium",
  "description": "Phở truyền thống Hà Nội - Nâng cấp không gian mới",
  "is_active": true,
  "operating_hours": [
    { "day_of_week": 1, "open_time": "06:30", "close_time": "23:00", "is_closed": false }
  ]
}
```

**Response** `200 OK`:
```json
{
  "success": true,
  "data": { "id": "uuid-...", "name": "Phở Bắc Hà Premium" },
  "message": "Cập nhật hồ sơ nhà hàng thành công"
}
```

**Logic Database**:
- **UPDATE** `restaurants`
- **UPDATE** `restaurant_operating_hours` (upsert theo `day_of_week`)

---

## 3.4 Danh sách danh mục menu

**`GET /api/merchant/categories`**

**Chức năng**: Lấy danh sách danh mục món ăn của nhà hàng.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Response** `200 OK`:
```json
{
  "success": true,
  "data": [
    { "id": 1, "name": "Món chính", "display_order": 1 },
    { "id": 2, "name": "Đồ uống", "display_order": 2 }
  ],
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `categories` WHERE `restaurant_id` ORDER BY `display_order`

---

## 3.5 Tạo danh mục mới

**`POST /api/merchant/categories`**

**Chức năng**: Tạo danh mục món ăn mới trong menu nhà hàng.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Request Body**:
```json
{
  "name": "Tráng miệng",
  "display_order": 3
}
```

**Response** `201 Created`:
```json
{
  "success": true,
  "data": { "id": 3, "name": "Tráng miệng", "display_order": 3 },
  "message": "Tạo danh mục thành công"
}
```

**Logic Database**: **INSERT** `categories`

---

## 3.6 Cập nhật danh mục

**`PUT /api/merchant/categories/{id}`**

**Chức năng**: Cập nhật tên hoặc thứ tự hiển thị của danh mục.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Path Params**: `id` — ID danh mục (integer)

**Request Body**:
```json
{
  "name": "Tráng miệng & Bánh ngọt",
  "display_order": 2
}
```

**Response** `200 OK`:
```json
{
  "success": true,
  "data": { "id": 3, "name": "Tráng miệng & Bánh ngọt", "display_order": 2 },
  "message": "Cập nhật danh mục thành công"
}
```

**Logic Database**: **UPDATE** `categories` sau khi kiểm tra `restaurant_id` thuộc về owner

---

## 3.7 Xóa danh mục

**`DELETE /api/merchant/categories/{id}`**

**Chức năng**: Xóa danh mục. Sản phẩm thuộc danh mục này sẽ có `category_id = NULL` (ON DELETE SET NULL).

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Path Params**: `id` — ID danh mục

**Response** `200 OK`:
```json
{
  "success": true,
  "data": null,
  "message": "Đã xóa danh mục. Sản phẩm liên quan sẽ được chuyển về danh mục chung."
}
```

**Logic Database**:
- **DELETE** `categories` → **UPDATE** `products` set `category_id = NULL` (ON DELETE SET NULL)

---

## 3.8 Danh sách sản phẩm

**`GET /api/merchant/products`**

**Chức năng**: Lấy toàn bộ sản phẩm trong menu của nhà hàng.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Query Params**: `category_id`, `is_available`, `page`, `size`

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "total": 25,
    "items": [
      {
        "id": "uuid-...",
        "name": "Phở bò tái nạm",
        "price": 65000.00,
        "category": { "id": 1, "name": "Món chính" },
        "is_available": true,
        "image_url": "https://cdn.example.com/products/uuid.jpg"
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `products` WHERE `restaurant_id`
- **JOIN** `categories`
- **SELECT** `media_files` (entity_type = 'PRODUCT')

---

## 3.9 Thêm sản phẩm mới

**`POST /api/merchant/products`**

**Chức năng**: Thêm món ăn mới vào menu nhà hàng.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Request Body**:
```json
{
  "category_id": 1,
  "name": "Phở gà đặc biệt",
  "price": 60000.00,
  "description": "Phở gà với nước dùng trong vắt, thịt gà mềm",
  "is_available": true
}
```

| Field | Kiểu | Bắt buộc | Ràng buộc |
|-------|------|----------|-----------|
| `category_id` | int | ❌ | FK → categories.id |
| `name` | string | ✅ | ≤ 255 ký tự |
| `price` | number | ✅ | price ≥ 0 |
| `description` | string | ❌ | Mô tả món ăn |
| `is_available` | boolean | ❌ | Mặc định: true |

**Response** `201 Created`:
```json
{
  "success": true,
  "data": {
    "id": "uuid-...",
    "name": "Phở gà đặc biệt",
    "price": 60000.00,
    "is_available": true,
    "created_at": "2024-01-15T10:00:00Z"
  },
  "message": "Thêm sản phẩm thành công"
}
```

**Logic Database**:
- **INSERT** `products`
- Trigger async: Tạo vector embedding → **INSERT** `product_embeddings`

---

## 3.10 Cập nhật sản phẩm

**`PUT /api/merchant/products/{id}`**

**Chức năng**: Cập nhật thông tin món ăn (tên, giá, mô tả, danh mục).

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Path Params**: `id` — UUID sản phẩm

**Request Body**:
```json
{
  "name": "Phở gà đặc biệt (size lớn)",
  "price": 70000.00,
  "description": "Phở gà size lớn - thêm thịt",
  "category_id": 1
}
```

**Response** `200 OK`:
```json
{
  "success": true,
  "data": { "id": "uuid-...", "name": "Phở gà đặc biệt (size lớn)", "price": 70000.00 },
  "message": "Cập nhật sản phẩm thành công"
}
```

> **Lưu ý**: Giá mới chỉ áp dụng cho đơn hàng **mới**. Đơn đã đặt giữ nguyên `price_at_purchase`.

**Logic Database**:
- **UPDATE** `products`
- Trigger async: Cập nhật **`product_embeddings`** nếu tên/mô tả thay đổi

---

## 3.11 Bật/tắt trạng thái còn hàng

**`PATCH /api/merchant/products/{id}/availability`**

**Chức năng**: Nhanh chóng bật/tắt trạng thái "còn hàng" của một món.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Path Params**: `id` — UUID sản phẩm

**Request Body**:
```json
{
  "is_available": false
}
```

**Response** `200 OK`:
```json
{
  "success": true,
  "data": { "id": "uuid-...", "is_available": false },
  "message": "Đã cập nhật trạng thái sản phẩm"
}
```

**Logic Database**: **UPDATE** `products` set `is_available`

---

## 3.12 Đơn hàng mới

**`GET /api/merchant/orders/new`**

**Chức năng**: Lấy danh sách đơn hàng mới đang chờ xác nhận hoặc đang chuẩn bị.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Response** `200 OK`:
```json
{
  "success": true,
  "data": [
    {
      "order_id": "uuid-...",
      "order_code": "FD20240115002",
      "order_status": "PENDING",
      "customer_name": "Nguyễn Văn A",
      "items": [
        { "product_name": "Phở bò tái", "quantity": 1, "price": 65000.00 }
      ],
      "subtotal": 65000.00,
      "note": "Ít cay",
      "placed_at": "2024-01-15T13:00:00Z"
    }
  ],
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `orders` WHERE `restaurant_id` AND `order_status IN ('PENDING', 'CONFIRMED', 'PREPARING')`
- **JOIN** `order_items`, `products`, `users` (customer)

---

## 3.13 Xác nhận đơn hàng

**`PATCH /api/merchant/orders/{id}/confirm`**

**Chức năng**: Nhà hàng xác nhận đơn hàng. Chuyển `order_status` từ `PENDING` → `CONFIRMED`.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Path Params**: `id` — UUID đơn hàng

**Response** `200 OK`:
```json
{
  "success": true,
  "data": { "order_id": "uuid-...", "order_status": "CONFIRMED" },
  "message": "Đã xác nhận đơn hàng"
}
```

**Logic Database**:
- **SELECT** `orders` kiểm tra `restaurant_id` và `order_status = 'PENDING'`
- **UPDATE** `orders` set `order_status = 'CONFIRMED'`
- **INSERT** `order_status_history`

---

## 3.14 Bắt đầu chuẩn bị món

**`PATCH /api/merchant/orders/{id}/preparing`**

**Chức năng**: Đánh dấu đơn đang được chuẩn bị. Chuyển trạng thái `CONFIRMED` → `PREPARING`.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Response** `200 OK`:
```json
{
  "success": true,
  "data": { "order_id": "uuid-...", "order_status": "PREPARING" },
  "message": "Đang chuẩn bị đơn hàng"
}
```

**Logic Database**:
- **UPDATE** `orders` set `order_status = 'PREPARING'`
- **INSERT** `order_status_history`

---

## 3.15 Sẵn sàng giao hàng

**`PATCH /api/merchant/orders/{id}/ready`**

**Chức năng**: Đơn hàng đã chuẩn bị xong, chờ shipper đến lấy. Chuyển `PREPARING` → `READY`.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Response** `200 OK`:
```json
{
  "success": true,
  "data": { "order_id": "uuid-...", "order_status": "READY" },
  "message": "Đơn hàng đã sẵn sàng để giao"
}
```

**Logic Database**:
- **UPDATE** `orders` set `order_status = 'READY'`
- **INSERT** `order_status_history`
- Gửi thông báo push cho shipper đã nhận đơn

---

## 3.16 Xem đánh giá nhà hàng

**`GET /api/merchant/reviews`**

**Chức năng**: Nhà hàng xem đánh giá từ khách hàng, có thể lọc theo thời gian và điểm số.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Query Params**:

| Param | Kiểu | Mô tả |
|-------|------|-------|
| `from` | date | Từ ngày (YYYY-MM-DD) |
| `to` | date | Đến ngày |
| `rating` | int | Lọc theo điểm (1-5) |
| `page` | int | Số trang |

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "total": 120,
    "avg_rating": 4.3,
    "items": [
      {
        "review_id": "uuid-...",
        "order_code": "FD20240114001",
        "customer_name": "Trần Thị B",
        "rating": 5,
        "comment": "Phở ngon tuyệt!",
        "created_at": "2024-01-14T20:00:00Z",
        "item_reviews": [
          { "product_name": "Phở bò tái nạm", "rating": 5, "comment": "Ngon lắm" }
        ]
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `order_reviews` WHERE `restaurant_id`
- **JOIN** `orders`, `users` (customer), `order_item_reviews`

---

## 3.17 Báo cáo doanh thu

**`GET /api/merchant/reports/revenue`**

**Chức năng**: Thống kê doanh thu nhà hàng theo khoảng thời gian.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Query Params**: `from` (date), `to` (date)

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "period": { "from": "2024-01-01", "to": "2024-01-31" },
    "summary": {
      "total_orders": 150,
      "completed_orders": 138,
      "cancelled_orders": 12,
      "gross_revenue": 25000000.00,
      "commission_total": 2500000.00,
      "net_revenue": 22500000.00
    },
    "daily_breakdown": [
      { "date": "2024-01-15", "orders": 8, "revenue": 1200000.00, "commission": 120000.00 }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `orders` WHERE `restaurant_id` AND `placed_at BETWEEN from AND to` AND `order_status = 'COMPLETED'`
- GROUP BY ngày, tính SUM `subtotal`, SUM `commission_amount`

---

## 3.18 Yêu cầu rút tiền (Merchant)

**`POST /api/merchant/payout-requests`**

**Chức năng**: Chủ quán gửi yêu cầu rút tiền từ ví về tài khoản ngân hàng.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Request Body**:
```json
{
  "amount": 5000000.00,
  "payout_method_id": "uuid-..."
}
```

| Field | Kiểu | Bắt buộc | Ràng buộc |
|-------|------|----------|-----------|
| `amount` | number | ✅ | amount > 0 |
| `payout_method_id` | UUID | ✅ | FK → payout_methods.id |

**Response** `201 Created`:
```json
{
  "success": true,
  "data": {
    "request_id": "uuid-...",
    "amount": 5000000.00,
    "status": "PENDING",
    "bank_name": "Vietcombank",
    "bank_account_number": "****1234",
    "created_at": "2024-01-15T09:00:00Z"
  },
  "message": "Yêu cầu rút tiền đã được gửi. Vui lòng chờ xét duyệt."
}
```

**Logic Database**:
- **SELECT** `wallets` WHERE `user_id`, kiểm tra `balance >= amount`
- **SELECT** `payout_methods` kiểm tra ownership
- **INSERT** `payout_requests` (status = PENDING)
- **UPDATE** `wallets` tạm trừ số dư (pending deduction)

---

# 4. Tài xế (Shipper)

---

## 4.1 Đăng ký tài xế

**`POST /api/shipper/register`**

**Chức năng**: Người dùng đăng ký trở thành tài xế giao hàng. Trạng thái chờ admin phê duyệt.

**Authentication**: ✅ Bearer Token

**Request Body**:
```json
{
  "vehicle_info": "Xe máy Honda Wave",
  "license_plate": "59P1-12345"
}
```

| Field | Kiểu | Bắt buộc | Ràng buộc |
|-------|------|----------|-----------|
| `vehicle_info` | string | ✅ | ≤ 100 ký tự |
| `license_plate` | string | ✅ | Phải duy nhất trong hệ thống |

**Response** `201 Created`:
```json
{
  "success": true,
  "data": {
    "shipper_id": "uuid-...",
    "license_plate": "59P1-12345",
    "approval_status": "PENDING"
  },
  "message": "Đăng ký tài xế thành công. Vui lòng chờ phê duyệt."
}
```

**Logic Database**:
- **INSERT** `shippers` (id = user_id, is_online = false, is_busy = false)
- **UPDATE** `user_roles` thêm role SHIPPER (chờ duyệt)

---

## 4.2 Danh sách đơn hàng khả dụng

**`GET /api/shipper/orders/available`**

**Chức năng**: Lấy danh sách đơn hàng đang ở trạng thái `READY` (đã chuẩn bị xong, chưa có shipper) trong bán kính gần shipper.

**Authentication**: ✅ Bearer Token (SHIPPER)

**Query Params**:

| Param | Kiểu | Mô tả |
|-------|------|-------|
| `lat` | number | Vĩ độ hiện tại của shipper |
| `lng` | number | Kinh độ hiện tại |

**Response** `200 OK`:
```json
{
  "success": true,
  "data": [
    {
      "order_id": "uuid-...",
      "order_code": "FD20240115003",
      "restaurant": {
        "name": "Phở Bắc Hà",
        "address": "45 Nguyễn Trãi, Quận 5",
        "latitude": 10.75800000,
        "longitude": 106.68200000
      },
      "delivery_address": "123 Nguyễn Huệ, Quận 1",
      "delivery_lat": 10.77609800,
      "delivery_lng": 106.70115300,
      "shipping_fee": 25000.00,
      "distance_km": 3.2,
      "placed_at": "2024-01-15T13:05:00Z"
    }
  ],
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `orders` WHERE `order_status = 'READY'` AND `shipper_id IS NULL`
- Tính khoảng cách từ vị trí shipper đến nhà hàng

---

## 4.3 Nhận đơn hàng

**`PATCH /api/shipper/orders/{id}/accept`**

**Chức năng**: Tài xế nhận đơn hàng. Chuyển `order_status` → `SHIPPING`, gán `shipper_id`.

**Authentication**: ✅ Bearer Token (SHIPPER)

**Path Params**: `id` — UUID đơn hàng

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "order_id": "uuid-...",
    "order_status": "SHIPPING",
    "restaurant_address": "45 Nguyễn Trãi, Quận 5",
    "customer_phone": "090****567"
  },
  "message": "Đã nhận đơn hàng thành công"
}
```

**Logic Database**:
- **SELECT** `orders` kiểm tra `order_status = 'READY'` AND `shipper_id IS NULL`
- **UPDATE** `orders` set `shipper_id = current_shipper_id`, `order_status = 'SHIPPING'`
- **UPDATE** `shippers` set `is_busy = true`
- **INSERT** `order_status_history`

---

## 4.4 Đã lấy hàng tại nhà hàng

**`PATCH /api/shipper/orders/{id}/picked-up`**

**Chức năng**: Xác nhận đã lấy hàng tại nhà hàng, đang trên đường giao.

**Authentication**: ✅ Bearer Token (SHIPPER)

**Response** `200 OK`:
```json
{
  "success": true,
  "data": { "order_id": "uuid-...", "order_status": "DELIVERING" },
  "message": "Đã xác nhận lấy hàng"
}
```

**Logic Database**:
- **UPDATE** `orders` set `order_status = 'DELIVERING'`
- **INSERT** `order_status_history`

---

## 4.5 Giao hàng thành công

**`PATCH /api/shipper/orders/{id}/delivered`**

**Chức năng**: Xác nhận giao hàng thành công. Hoàn tất đơn hàng, kích hoạt quy trình thanh toán cho nhà hàng và tài xế.

**Authentication**: ✅ Bearer Token (SHIPPER)

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "order_id": "uuid-...",
    "order_status": "COMPLETED",
    "shipper_income": 22500.00
  },
  "message": "Giao hàng thành công!"
}
```

**Logic Database**:
- **UPDATE** `orders` set `order_status = 'COMPLETED'`, `completed_at = NOW()`
- **INSERT** `order_status_history`
- **UPDATE** `shippers` set `is_busy = false`
- **UPDATE** `wallets` (nhà hàng: cộng `subtotal - commission_amount`; shipper: cộng phần của `shipping_fee`)
- **INSERT** `wallet_transactions` (2 bản ghi: cho nhà hàng và shipper)

---

## 4.6 Lịch sử đơn hàng đã giao

**`GET /api/shipper/orders/history`**

**Chức năng**: Xem lịch sử các đơn hàng đã giao của tài xế.

**Authentication**: ✅ Bearer Token (SHIPPER)

**Query Params**: `from`, `to`, `page`, `size`

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "total": 200,
    "items": [
      {
        "order_id": "uuid-...",
        "order_code": "FD20240115003",
        "restaurant_name": "Phở Bắc Hà",
        "delivery_address": "123 Nguyễn Huệ, Quận 1",
        "shipping_fee": 25000.00,
        "completed_at": "2024-01-15T13:45:00Z"
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `orders` WHERE `shipper_id = current_shipper_id` AND `order_status = 'COMPLETED'`

---

## 4.7 Thống kê thu nhập tài xế

**`GET /api/shipper/income`**

**Chức năng**: Thống kê thu nhập của tài xế theo khoảng thời gian.

**Authentication**: ✅ Bearer Token (SHIPPER)

**Query Params**: `from`, `to`

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "period": { "from": "2024-01-01", "to": "2024-01-31" },
    "total_orders": 85,
    "total_income": 2550000.00,
    "wallet_balance": 1800000.00,
    "daily_breakdown": [
      { "date": "2024-01-15", "orders": 5, "income": 150000.00 }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `wallet_transactions` WHERE `wallet_id` AND `type = 'RECEIVE_PAYMENT'`
- **JOIN** `wallets` để lấy `balance` hiện tại

---

## 4.8 Yêu cầu rút tiền (Shipper)

**`POST /api/shipper/payout-requests`**

**Chức năng**: Tài xế gửi yêu cầu rút tiền. Logic tương tự mục 3.18.

**Authentication**: ✅ Bearer Token (SHIPPER)

**Request Body**:
```json
{
  "amount": 1000000.00,
  "payout_method_id": "uuid-..."
}
```

**Response** `201 Created`:
```json
{
  "success": true,
  "data": {
    "request_id": "uuid-...",
    "amount": 1000000.00,
    "status": "PENDING"
  },
  "message": "Yêu cầu rút tiền đã được gửi"
}
```

**Logic Database**: Tương tự mục 3.18 — **INSERT** `payout_requests`, **UPDATE** `wallets`

---

# 5. Thanh toán (Payment)

---

## 5.1 Tạo URL thanh toán VNPay

**`POST /api/payments/vnpay/create-url`**

**Chức năng**: Tạo URL redirect đến cổng thanh toán VNPay cho đơn hàng chỉ định.

**Authentication**: ✅ Bearer Token

**Request Body**:
```json
{
  "order_id": "uuid-..."
}
```

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "payment_url": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=20000000&vnp_TxnRef=FD20240115001&...",
    "vnp_txn_ref": "FD20240115001",
    "amount": 200000.00,
    "expires_at": "2024-01-15T14:00:00Z"
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `orders` kiểm tra `customer_id`, `payment_status = 'UNPAID'`, `total_amount`
- **INSERT** `payments` (status = PENDING, `vnp_txn_ref`)

---

## 5.2 Callback từ VNPay

**`GET /api/payments/vnpay/callback`**

**Chức năng**: Endpoint nhận kết quả trả về từ VNPay sau khi người dùng hoàn tất hoặc hủy thanh toán. Bắt buộc xử lý **idempotent** (kiểm tra `vnp_txn_ref` đã xử lý chưa trước khi update).

**Authentication**: ❌ Không yêu cầu (xác thực bằng VNPay checksum)

**Query Params** (từ VNPay redirect):

| Param | Mô tả |
|-------|-------|
| `vnp_TxnRef` | Mã giao dịch tham chiếu |
| `vnp_ResponseCode` | `00` = thành công |
| `vnp_TransactionNo` | Mã giao dịch ngân hàng |
| `vnp_Amount` | Số tiền (VNPay gửi x100) |
| `vnp_SecureHash` | Chữ ký xác thực |

**Response** `200 OK` (redirect về frontend kèm kết quả):
```json
{
  "success": true,
  "data": {
    "order_id": "uuid-...",
    "order_code": "FD20240115001",
    "payment_status": "SUCCESS",
    "amount": 200000.00
  },
  "message": "Thanh toán thành công"
}
```

**Logic Database**:
- Xác thực `vnp_SecureHash`
- **SELECT** `payments` WHERE `vnp_txn_ref` — nếu đã `SUCCESS` thì bỏ qua (idempotency)
- **UPDATE** `payments` set `payment_status`, `vnp_transaction_no`
- **UPDATE** `orders` set `payment_status = 'PAID'` (nếu thành công)

---

## 5.3 Kiểm tra trạng thái thanh toán

**`GET /api/payments/orders/{orderId}/status`**

**Chức năng**: Truy vấn trạng thái thanh toán của đơn hàng.

**Authentication**: ✅ Bearer Token

**Path Params**: `orderId` — UUID đơn hàng

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "order_id": "uuid-...",
    "order_code": "FD20240115001",
    "payment_status": "PAID",
    "amount": 200000.00,
    "paid_at": "2024-01-15T13:15:00Z",
    "transaction_ref": "FD20240115001"
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `orders` WHERE `id`
- **SELECT** `payments` WHERE `order_id` AND `payment_status = 'SUCCESS'`

---

# 6. Quản trị (Admin)

---

## 6.1 Danh sách nhà hàng chờ duyệt

**`GET /api/admin/merchants/pending`**

**Chức năng**: Lấy danh sách nhà hàng đang chờ phê duyệt.

**Authentication**: ✅ Bearer Token (ADMIN)

**Query Params**: `page`, `size`

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "total": 5,
    "items": [
      {
        "restaurant_id": "uuid-...",
        "name": "Cơm tấm Sài Gòn",
        "owner": { "id": "uuid-...", "full_name": "Lê Văn C", "email": "c@gmail.com" },
        "address": "100 Cống Quỳnh, Quận 1",
        "created_at": "2024-01-14T08:00:00Z"
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `restaurants` WHERE `approval_status = 'PENDING'`
- **JOIN** `users` (owner)

---

## 6.2 Phê duyệt nhà hàng

**`PATCH /api/admin/merchants/{id}/approve`**

**Chức năng**: Admin phê duyệt nhà hàng. Chuyển `approval_status = 'APPROVED'`, kích hoạt nhà hàng.

**Authentication**: ✅ Bearer Token (ADMIN)

**Path Params**: `id` — UUID nhà hàng

**Response** `200 OK`:
```json
{
  "success": true,
  "data": { "restaurant_id": "uuid-...", "approval_status": "APPROVED" },
  "message": "Đã phê duyệt nhà hàng thành công"
}
```

**Logic Database**:
- **UPDATE** `restaurants` set `approval_status = 'APPROVED'`, `is_active = true`
- Trigger async: Tạo vector embedding → **INSERT** `restaurant_embeddings`

---

## 6.3 Từ chối nhà hàng

**`PATCH /api/admin/merchants/{id}/reject`**

**Chức năng**: Admin từ chối đăng ký nhà hàng kèm lý do.

**Authentication**: ✅ Bearer Token (ADMIN)

**Request Body**:
```json
{
  "reason": "Hồ sơ không đầy đủ, vui lòng bổ sung giấy phép kinh doanh"
}
```

**Response** `200 OK`:
```json
{
  "success": true,
  "data": { "restaurant_id": "uuid-...", "approval_status": "REJECTED" },
  "message": "Đã từ chối nhà hàng"
}
```

**Logic Database**: **UPDATE** `restaurants` set `approval_status = 'REJECTED'`

---

## 6.4 Danh sách tài xế chờ duyệt

**`GET /api/admin/shippers/pending`**

**Chức năng**: Lấy danh sách tài xế đang chờ phê duyệt.

**Authentication**: ✅ Bearer Token (ADMIN)

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "total": 3,
    "items": [
      {
        "shipper_id": "uuid-...",
        "full_name": "Trần Văn D",
        "phone": "0912345678",
        "vehicle_info": "Honda Wave",
        "license_plate": "59P1-12345",
        "registered_at": "2024-01-14T10:00:00Z"
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `shippers` JOIN `users` — lọc các shipper chưa được cấp quyền đầy đủ

---

## 6.5 Phê duyệt tài xế

**`PATCH /api/admin/shippers/{id}/approve`**

**Chức năng**: Admin phê duyệt tài xế, cho phép nhận đơn hàng.

**Authentication**: ✅ Bearer Token (ADMIN)

**Response** `200 OK`:
```json
{
  "success": true,
  "data": { "shipper_id": "uuid-...", "status": "APPROVED" },
  "message": "Đã phê duyệt tài xế"
}
```

**Logic Database**: **UPDATE** `shippers` set trạng thái duyệt, **UPDATE** `user_roles` kích hoạt role SHIPPER

---

## 6.6 Từ chối tài xế

**`PATCH /api/admin/shippers/{id}/reject`**

**Chức năng**: Admin từ chối hồ sơ tài xế.

**Authentication**: ✅ Bearer Token (ADMIN)

**Request Body**:
```json
{
  "reason": "Biển số xe không hợp lệ"
}
```

**Response** `200 OK`:
```json
{
  "success": true,
  "data": { "shipper_id": "uuid-...", "status": "REJECTED" },
  "message": "Đã từ chối hồ sơ tài xế"
}
```

---

## 6.7 Danh sách Voucher

**`GET /api/admin/vouchers`**

**Chức năng**: Lấy toàn bộ danh sách mã giảm giá trong hệ thống.

**Authentication**: ✅ Bearer Token (ADMIN)

**Query Params**: `is_active`, `page`, `size`

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "total": 15,
    "items": [
      {
        "id": "uuid-...",
        "code": "SUMMER30",
        "discount_type": "PERCENTAGE",
        "discount_value": 30,
        "min_order_value": 100000.00,
        "max_discount_amount": 100000.00,
        "usage_limit": 500,
        "used_count": 123,
        "expiry_date": "2024-08-31T23:59:59Z",
        "is_active": true
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**: **SELECT** `vouchers`

---

## 6.8 Tạo Voucher

**`POST /api/admin/vouchers`**

**Chức năng**: Tạo mã giảm giá mới.

**Authentication**: ✅ Bearer Token (ADMIN)

**Request Body**:
```json
{
  "code": "NEWUSER50",
  "discount_type": "PERCENTAGE",
  "discount_value": 50,
  "min_order_value": 150000.00,
  "max_discount_amount": 50000.00,
  "usage_limit": 1000,
  "expiry_date": "2024-12-31T23:59:59Z",
  "is_active": true
}
```

| Field | Kiểu | Ràng buộc |
|-------|------|-----------|
| `code` | string | Phải duy nhất |
| `discount_type` | string | PERCENTAGE hoặc FIXED_AMOUNT |
| `discount_value` | number | ≥ 0 |
| `min_order_value` | number | ≥ 0 |
| `max_discount_amount` | number | ≥ 0 (chỉ áp dụng cho PERCENTAGE) |
| `usage_limit` | int | Số lần tối đa được dùng |

**Response** `201 Created`:
```json
{
  "success": true,
  "data": { "id": "uuid-...", "code": "NEWUSER50" },
  "message": "Tạo voucher thành công"
}
```

**Logic Database**: **INSERT** `vouchers` (used_count = 0)

---

## 6.9 Cập nhật Voucher

**`PUT /api/admin/vouchers/{id}`**

**Chức năng**: Chỉnh sửa thông tin voucher (gia hạn, thay đổi điều kiện, bật/tắt).

**Authentication**: ✅ Bearer Token (ADMIN)

**Path Params**: `id` — UUID voucher

**Request Body**: Các trường tương tự 6.8

**Response** `200 OK`:
```json
{
  "success": true,
  "data": { "id": "uuid-...", "code": "NEWUSER50", "is_active": false },
  "message": "Cập nhật voucher thành công"
}
```

**Logic Database**: **UPDATE** `vouchers`

---

## 6.10 Xóa Voucher

**`DELETE /api/admin/vouchers/{id}`**

**Chức năng**: Xóa mã giảm giá (chỉ xóa được voucher chưa được sử dụng).

**Authentication**: ✅ Bearer Token (ADMIN)

**Response** `200 OK`:
```json
{
  "success": true,
  "data": null,
  "message": "Đã xóa voucher"
}
```

**Logic Database**:
- **SELECT** `vouchers` kiểm tra `used_count = 0`
- **DELETE** `vouchers`

---

## 6.11 Danh sách cấu hình phí ship

**`GET /api/admin/shipping-configs`**

**Chức năng**: Lấy lịch sử và cấu hình phí ship hiện tại.

**Authentication**: ✅ Bearer Token (ADMIN)

**Response** `200 OK`:
```json
{
  "success": true,
  "data": [
    {
      "id": 3,
      "base_fee": 15000.00,
      "base_distance_km": 2.00,
      "fee_per_km": 5000.00,
      "active_from": "2024-01-01T00:00:00Z",
      "is_current": true
    },
    {
      "id": 2,
      "base_fee": 12000.00,
      "base_distance_km": 2.00,
      "fee_per_km": 4000.00,
      "active_from": "2023-06-01T00:00:00Z",
      "is_current": false
    }
  ],
  "message": "OK"
}
```

**Logic Database**: **SELECT** `shipping_configs` ORDER BY `active_from DESC`

---

## 6.12 Tạo cấu hình phí ship mới

**`POST /api/admin/shipping-configs`**

**Chức năng**: Tạo bảng giá phí ship mới (có hiệu lực từ thời điểm chỉ định).

**Authentication**: ✅ Bearer Token (ADMIN)

**Request Body**:
```json
{
  "base_fee": 18000.00,
  "base_distance_km": 2.00,
  "fee_per_km": 5500.00,
  "active_from": "2024-02-01T00:00:00Z"
}
```

**Response** `201 Created`:
```json
{
  "success": true,
  "data": { "id": 4, "base_fee": 18000.00, "active_from": "2024-02-01T00:00:00Z" },
  "message": "Tạo cấu hình phí ship thành công"
}
```

**Logic Database**: **INSERT** `shipping_configs` (không xóa bản ghi cũ, giữ lịch sử)

---

## 6.13 Cập nhật cấu hình phí ship

**`PUT /api/admin/shipping-configs/{id}`**

**Chức năng**: Chỉnh sửa cấu hình phí ship (nên hạn chế cập nhật bản ghi cũ, ưu tiên tạo mới).

**Authentication**: ✅ Bearer Token (ADMIN)

**Response** `200 OK`:
```json
{
  "success": true,
  "data": { "id": 4, "base_fee": 20000.00 },
  "message": "Cập nhật cấu hình phí ship thành công"
}
```

**Logic Database**: **UPDATE** `shipping_configs`

---

## 6.14 Danh sách yêu cầu rút tiền

**`GET /api/admin/payout-requests`**

**Chức năng**: Admin xem toàn bộ yêu cầu rút tiền, lọc theo trạng thái.

**Authentication**: ✅ Bearer Token (ADMIN)

**Query Params**: `status` (PENDING/APPROVED/REJECTED), `page`, `size`

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "total": 8,
    "items": [
      {
        "request_id": "uuid-...",
        "user": { "id": "uuid-...", "full_name": "Lê Văn C", "role": "RESTAURANT_OWNER" },
        "amount": 5000000.00,
        "bank_name": "Vietcombank",
        "bank_account_number": "****1234",
        "bank_account_holder": "LE VAN C",
        "status": "PENDING",
        "created_at": "2024-01-15T09:00:00Z"
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `payout_requests`
- **JOIN** `wallets`, `users`, `payout_methods`

---

## 6.15 Duyệt yêu cầu rút tiền

**`PATCH /api/admin/payout-requests/{id}/approve`**

**Chức năng**: Admin phê duyệt yêu cầu rút tiền, thực hiện trừ số dư ví.

**Authentication**: ✅ Bearer Token (ADMIN)

**Path Params**: `id` — UUID yêu cầu

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "request_id": "uuid-...",
    "status": "APPROVED",
    "approved_at": "2024-01-15T11:00:00Z"
  },
  "message": "Đã duyệt yêu cầu rút tiền"
}
```

**Logic Database**:
- **UPDATE** `payout_requests` set `status = 'APPROVED'`, `approved_by`, `approved_at = NOW()`
- **UPDATE** `wallets` trừ `amount` khỏi `balance`
- **INSERT** `wallet_transactions` (type = WITHDRAW)

---

## 6.16 Từ chối yêu cầu rút tiền

**`PATCH /api/admin/payout-requests/{id}/reject`**

**Chức năng**: Admin từ chối yêu cầu rút tiền, hoàn trả số dư bị tạm giữ.

**Authentication**: ✅ Bearer Token (ADMIN)

**Request Body**:
```json
{
  "reason": "Thông tin tài khoản ngân hàng không khớp"
}
```

**Response** `200 OK`:
```json
{
  "success": true,
  "data": { "request_id": "uuid-...", "status": "REJECTED" },
  "message": "Đã từ chối yêu cầu rút tiền"
}
```

**Logic Database**:
- **UPDATE** `payout_requests` set `status = 'REJECTED'`, `approved_by`, `approved_at`
- **UPDATE** `wallets` hoàn trả `amount` về `balance`

---

## 6.17 Báo cáo tổng quan hệ thống

**`GET /api/admin/reports/overview`**

**Chức năng**: Thống kê tổng quan toàn hệ thống theo khoảng thời gian (doanh thu, số đơn, người dùng mới,...).

**Authentication**: ✅ Bearer Token (ADMIN)

**Query Params**: `from` (date), `to` (date)

**Response** `200 OK`:
```json
{
  "success": true,
  "data": {
    "period": { "from": "2024-01-01", "to": "2024-01-31" },
    "orders": {
      "total": 1250,
      "completed": 1180,
      "cancelled": 70,
      "completion_rate": 94.4
    },
    "revenue": {
      "gmv": 180000000.00,
      "total_commission": 18000000.00,
      "shipping_fees_collected": 31250000.00
    },
    "users": {
      "new_customers": 320,
      "new_restaurants": 12,
      "new_shippers": 8,
      "active_shippers": 45
    },
    "top_restaurants": [
      { "name": "Phở Bắc Hà", "orders": 85, "revenue": 12750000.00 }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:
- **SELECT** `orders` WHERE `placed_at BETWEEN from AND to` — aggregate (COUNT, SUM)
- **SELECT** `users` WHERE `created_at BETWEEN from AND to` — GROUP BY role
- **SELECT** `wallet_transactions` WHERE `type = 'RECEIVE_PAYMENT'` — SUM commission

---

# Phụ lục: Bảng tóm tắt Authentication

| Nhóm | Endpoint | Auth Required | Role |
|------|----------|---------------|------|
| Auth | `POST /auth/register`, `POST /auth/login`, `POST /auth/forgot-password`, `POST /auth/reset-password` | ❌ | — |
| Auth | `POST /auth/refresh-token`, `POST /auth/change-password` | ✅ | Bất kỳ |
| Profile | `GET/PUT /users/me`, `*/addresses*` | ✅ | Bất kỳ |
| Catalog | `GET /catalog/*` | ❌ | — |
| Cart | `POST/PUT/DELETE /cart/items` | ✅ | CUSTOMER |
| Orders | `POST /vouchers/validate`, `POST /orders/checkout`, `GET /orders/my`, `*/cancel`, `*/review` | ✅ | CUSTOMER |
| Merchant | `POST /merchant/register` | ✅ | Bất kỳ |
| Merchant | Tất cả `GET/PUT/PATCH /merchant/*` | ✅ | RESTAURANT_OWNER |
| Shipper | `POST /shipper/register` | ✅ | Bất kỳ |
| Shipper | Tất cả `GET/PATCH /shipper/*` | ✅ | SHIPPER |
| Payment | `POST /payments/vnpay/create-url`, `GET */status` | ✅ | Bất kỳ |
| Payment | `GET /payments/vnpay/callback` | ❌ (VNPay checksum) | — |
| Admin | Tất cả `/admin/*` | ✅ | ADMIN |

---

*Tài liệu này được tạo dựa trên Database Schema và danh sách API Endpoints đã thiết kế. Mọi thay đổi schema cần được cập nhật đồng bộ vào tài liệu này.*
