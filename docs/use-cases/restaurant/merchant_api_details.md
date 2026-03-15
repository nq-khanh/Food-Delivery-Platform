# API Specification — Module Restaurant (Merchant)

> **Dự án**: Food Delivery Platform  
> **Module**: Restaurant (Merchant)  
> **Phiên bản**: 1.0  
> **Desciption**: Đặc tả chi tiết chi tiết API của Merchant  
> **Ngày**: 2026-03-15

---

## 1.1 Đăng ký nhà hàng mới

**`POST /api/merchant/register`**

**Chức năng**: Merchant đăng ký nhà hàng mới. Trạng thái ban đầu là `PENDING`, chờ Admin phê duyệt.

**Authentication**: ✅ Bearer Token (user đã đăng nhập, có thể chưa là RESTAURANT_OWNER)

**Request Body**:

```json
{
  "name": "Phở Bắc Hà",
  "address": "45 Nguyễn Trãi, Quận 5, TP.HCM",
  "latitude": 10.758,
  "longitude": 106.682,
  "description": "Phở truyền thống Hà Nội chính gốc",
  "operating_hours": [
    {
      "day_of_week": 1,
      "open_time": "07:00",
      "close_time": "22:00",
      "is_closed": false
    },
    {
      "day_of_week": 2,
      "open_time": "07:00",
      "close_time": "22:00",
      "is_closed": false
    },
    {
      "day_of_week": 3,
      "open_time": "07:00",
      "close_time": "22:00",
      "is_closed": false
    },
    {
      "day_of_week": 4,
      "open_time": "07:00",
      "close_time": "22:00",
      "is_closed": false
    },
    {
      "day_of_week": 5,
      "open_time": "07:00",
      "close_time": "22:00",
      "is_closed": false
    },
    {
      "day_of_week": 6,
      "open_time": "08:00",
      "close_time": "21:00",
      "is_closed": false
    },
    {
      "day_of_week": 0,
      "open_time": null,
      "close_time": null,
      "is_closed": true
    }
  ]
}
```

| Field                           | Kiểu    | Bắt buộc     | Ràng buộc                                  |
| ------------------------------- | ------- | ------------ | ------------------------------------------ |
| `name`                          | string  | ✅           | 1–255 ký tự                                |
| `address`                       | string  | ✅           | Không được rỗng                            |
| `latitude`                      | number  | ✅           | -90 ≤ lat ≤ 90                             |
| `longitude`                     | number  | ✅           | -180 ≤ lng ≤ 180                           |
| `description`                   | string  | ❌           | Tối đa 2000 ký tự                          |
| `operating_hours`               | array   | ✅           | 7 phần tử (0=CN đến 6=T7)                  |
| `operating_hours[].day_of_week` | int     | ✅           | 0–6                                        |
| `operating_hours[].open_time`   | string  | Có điều kiện | `HH:mm` — bắt buộc nếu `is_closed = false` |
| `operating_hours[].close_time`  | string  | Có điều kiện | `HH:mm` — bắt buộc nếu `is_closed = false` |
| `operating_hours[].is_closed`   | boolean | ✅           | —                                          |

**Response** `201 Created`:

```json
{
  "success": true,
  "data": {
    "restaurant_id": "uuid-...",
    "name": "Phở Bắc Hà",
    "approval_status": "PENDING",
    "created_at": "2024-01-15T08:00:00Z"
  },
  "message": "Đăng ký thành công. Vui lòng chờ quản trị viên phê duyệt."
}
```

**Lỗi phổ biến**:

| Điều kiện                            | HTTP | errorCode                   |
| ------------------------------------ | ---- | --------------------------- |
| User đã có nhà hàng PENDING/APPROVED | 409  | `RESTAURANT_ALREADY_EXISTS` |
| Thiếu trường bắt buộc                | 422  | `VALIDATION_ERROR`          |

**Logic Database**:

- **INSERT** `restaurants` (`approval_status = PENDING`, `is_active = false`, `rating_avg = 0`)
- **INSERT** `restaurant_operating_hours` (7 bản ghi tương ứng 7 ngày)
- **UPDATE** `user_roles`: thêm role `RESTAURANT_OWNER` nếu chưa có

---

## 1.2 Xem hồ sơ nhà hàng

**`GET /api/merchant/profile`**

**Chức năng**: Lấy toàn bộ thông tin nhà hàng của chủ quán đang đăng nhập.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Response** `200 OK`:

```json
{
  "success": true,
  "data": {
    "id": "uuid-...",
    "name": "Phở Bắc Hà",
    "address": "45 Nguyễn Trãi, Quận 5, TP.HCM",
    "latitude": 10.758,
    "longitude": 106.682,
    "description": "Phở truyền thống Hà Nội chính gốc",
    "cover_image_url": "https://cdn.example.com/restaurants/uuid.jpg",
    "approval_status": "APPROVED",
    "is_active": true,
    "rating_avg": 4.5,
    "created_at": "2024-01-15T08:00:00Z",
    "operating_hours": [
      {
        "day_of_week": 0,
        "open_time": null,
        "close_time": null,
        "is_closed": true
      },
      {
        "day_of_week": 1,
        "open_time": "07:00",
        "close_time": "22:00",
        "is_closed": false
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:

- **SELECT** `restaurants` WHERE `owner_id = current_user_id`
- **SELECT** `restaurant_operating_hours` WHERE `restaurant_id`
- **SELECT** `media_files` WHERE `entity_type = 'RESTAURANT'` AND `entity_id`

---

## 1.3 Cập nhật hồ sơ nhà hàng

**`PUT /api/merchant/profile`**

**Chức năng**: Merchant cập nhật thông tin nhà hàng. Chỉ chứa các trường muốn thay đổi.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER, APPROVED)

**Request Body**:

```json
{
  "name": "Phở Bắc Hà Premium",
  "description": "Phở truyền thống Hà Nội - Không gian nâng cấp mới",
  "address": "45 Nguyễn Trãi, Quận 5, TP.HCM",
  "latitude": 10.758,
  "longitude": 106.682,
  "is_active": true,
  "operating_hours": [
    {
      "day_of_week": 1,
      "open_time": "06:30",
      "close_time": "23:00",
      "is_closed": false
    }
  ]
}
```

| Field             | Kiểu    | Bắt buộc | Mô tả                                       |
| ----------------- | ------- | -------- | ------------------------------------------- |
| `name`            | string  | ❌       | 1–255 ký tự                                 |
| `description`     | string  | ❌       | Tối đa 2000 ký tự                           |
| `address`         | string  | ❌       | Địa chỉ mới                                 |
| `latitude`        | number  | ❌       | Cần cập nhật cùng longitude nếu đổi địa chỉ |
| `longitude`       | number  | ❌       | Cần cập nhật cùng latitude nếu đổi địa chỉ  |
| `is_active`       | boolean | ❌       | Bật/tắt nhận đơn                            |
| `operating_hours` | array   | ❌       | Chỉ gửi các ngày cần cập nhật               |

**Response** `200 OK`:

```json
{
  "success": true,
  "data": {
    "id": "uuid-...",
    "name": "Phở Bắc Hà Premium",
    "is_active": true
  },
  "message": "Cập nhật hồ sơ nhà hàng thành công"
}
```

**Logic Database**:

- **UPDATE** `restaurants`
- **UPSERT** `restaurant_operating_hours` (ON CONFLICT `restaurant_id, day_of_week` DO UPDATE)
- Trigger async: cập nhật `restaurant_embeddings` nếu `name` hoặc `description` thay đổi

---

## 1.4 Upload ảnh bìa nhà hàng

**`POST /api/merchant/profile/cover-image`**

**Chức năng**: Upload hoặc thay thế ảnh bìa nhà hàng lên MinIO.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER, APPROVED)

**Content-Type**: `multipart/form-data`

**Request Body**:

```
file: <binary image file>
```

| Field  | Kiểu | Bắt buộc | Ràng buộc                |
| ------ | ---- | -------- | ------------------------ |
| `file` | file | ✅       | JPG/PNG/WebP, tối đa 5MB |

**Response** `200 OK`:

```json
{
  "success": true,
  "data": {
    "cover_image_url": "https://minio.example.com/restaurants/uuid-restaurant/cover.jpg"
  },
  "message": "Cập nhật ảnh bìa thành công"
}
```

**Logic Database**:

- Upload file lên MinIO bucket `restaurants/`
- **UPSERT** `media_files` (`entity_type = 'RESTAURANT'`, `entity_id = restaurant_id`)

---

# 2. Quản lý Danh mục (Category)

---

## 2.1 Danh sách danh mục

**`GET /api/merchant/categories`**

**Chức năng**: Lấy danh sách danh mục món ăn của nhà hàng hiện tại, sắp xếp theo `display_order`.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Response** `200 OK`:

```json
{
  "success": true,
  "data": [
    { "id": 1, "name": "Món chính", "display_order": 1, "product_count": 8 },
    { "id": 2, "name": "Đồ uống", "display_order": 2, "product_count": 5 },
    { "id": 3, "name": "Tráng miệng", "display_order": 3, "product_count": 3 }
  ],
  "message": "OK"
}
```

**Logic Database**:

- **SELECT** `categories` WHERE `restaurant_id = current_restaurant_id` ORDER BY `display_order`
- COUNT `products` theo `category_id`

---

## 2.2 Tạo danh mục mới

**`POST /api/merchant/categories`**

**Chức năng**: Tạo danh mục món ăn mới trong menu.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER, APPROVED)

**Request Body**:

```json
{
  "name": "Tráng miệng",
  "display_order": 3
}
```

| Field           | Kiểu   | Bắt buộc | Ràng buộc                       |
| --------------- | ------ | -------- | ------------------------------- |
| `name`          | string | ✅       | 1–100 ký tự                     |
| `display_order` | int    | ❌       | ≥ 1, mặc định: max hiện tại + 1 |

**Response** `201 Created`:

```json
{
  "success": true,
  "data": { "id": 4, "name": "Tráng miệng", "display_order": 3 },
  "message": "Tạo danh mục thành công"
}
```

**Logic Database**: **INSERT** `categories` (`restaurant_id = current_restaurant_id`)

---

## 2.3 Cập nhật danh mục

**`PUT /api/merchant/categories/{id}`**

**Chức năng**: Cập nhật tên và thứ tự hiển thị danh mục.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER, APPROVED)

**Path Params**: `id` — ID danh mục (integer)

**Request Body**:

```json
{
  "name": "Tráng miệng & Bánh ngọt",
  "display_order": 2
}
```

| Field           | Kiểu   | Bắt buộc | Ràng buộc   |
| --------------- | ------ | -------- | ----------- |
| `name`          | string | ❌       | 1–100 ký tự |
| `display_order` | int    | ❌       | ≥ 1         |

**Response** `200 OK`:

```json
{
  "success": true,
  "data": { "id": 4, "name": "Tráng miệng & Bánh ngọt", "display_order": 2 },
  "message": "Cập nhật danh mục thành công"
}
```

**Lỗi phổ biến**:

| Điều kiện                         | HTTP | errorCode            |
| --------------------------------- | ---- | -------------------- |
| Danh mục không thuộc nhà hàng này | 403  | `CATEGORY_NOT_OWNED` |
| `id` không tồn tại                | 404  | `NOT_FOUND`          |

**Logic Database**:

- **SELECT** `categories` kiểm tra `restaurant_id = current_restaurant_id`
- **UPDATE** `categories`

---

## 2.4 Xóa danh mục

**`DELETE /api/merchant/categories/{id}`**

**Chức năng**: Xóa danh mục. Sản phẩm thuộc danh mục bị xóa sẽ có `category_id = NULL` (không bị xóa).

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER, APPROVED)

**Path Params**: `id` — ID danh mục

**Response** `200 OK`:

```json
{
  "success": true,
  "data": {
    "deleted_category_id": 4,
    "affected_products": 3
  },
  "message": "Đã xóa danh mục. 3 sản phẩm liên quan được chuyển về 'Không có danh mục'."
}
```

**Logic Database**:

- **SELECT** `categories` kiểm tra ownership → **DELETE** `categories`
- PostgreSQL `ON DELETE SET NULL` tự động cập nhật `products.category_id = NULL`

---

# 3. Quản lý Sản phẩm (Product)

---

## 3.1 Danh sách sản phẩm

**`GET /api/merchant/products`**

**Chức năng**: Lấy toàn bộ sản phẩm trong menu, có thể lọc theo danh mục và trạng thái.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Query Params**:

| Param          | Kiểu    | Bắt buộc | Mô tả                           |
| -------------- | ------- | -------- | ------------------------------- |
| `category_id`  | int     | ❌       | Lọc theo danh mục               |
| `is_available` | boolean | ❌       | Lọc theo trạng thái còn hàng    |
| `page`         | int     | ❌       | Số trang (mặc định: 1)          |
| `size`         | int     | ❌       | Số bản ghi/trang (mặc định: 20) |

**Response** `200 OK`:

```json
{
  "success": true,
  "data": {
    "total": 25,
    "page": 1,
    "size": 20,
    "items": [
      {
        "id": "uuid-...",
        "name": "Phở bò tái nạm",
        "price": 65000.0,
        "description": "Phở truyền thống với tái và nạm",
        "is_available": true,
        "category": { "id": 1, "name": "Món chính" },
        "image_url": "https://cdn.example.com/products/uuid.jpg",
        "created_at": "2024-01-10T08:00:00Z"
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:

- **SELECT** `products` WHERE `restaurant_id = current_restaurant_id`
- **JOIN** `categories`
- **SELECT** `media_files` (`entity_type = 'PRODUCT'`)

---

## 3.2 Thêm sản phẩm mới

**`POST /api/merchant/products`**

**Chức năng**: Thêm món ăn mới vào menu. Sau khi tạo, hệ thống async tạo vector embedding.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER, APPROVED)

**Request Body**:

```json
{
  "category_id": 1,
  "name": "Phở gà đặc biệt",
  "price": 60000.0,
  "description": "Phở gà với nước dùng trong vắt, thịt gà mềm",
  "is_available": true
}
```

| Field          | Kiểu    | Bắt buộc | Ràng buộc                                      |
| -------------- | ------- | -------- | ---------------------------------------------- |
| `category_id`  | int     | ❌       | FK → `categories.id` (phải thuộc nhà hàng này) |
| `name`         | string  | ✅       | 1–255 ký tự                                    |
| `price`        | number  | ✅       | price ≥ 0 (VNĐ)                                |
| `description`  | string  | ❌       | Tối đa 1000 ký tự                              |
| `is_available` | boolean | ❌       | Mặc định: `true`                               |

**Response** `201 Created`:

```json
{
  "success": true,
  "data": {
    "id": "uuid-...",
    "name": "Phở gà đặc biệt",
    "price": 60000.0,
    "is_available": true,
    "category": { "id": 1, "name": "Món chính" },
    "created_at": "2024-01-15T10:00:00Z"
  },
  "message": "Thêm sản phẩm thành công"
}
```

**Logic Database**:

- **INSERT** `products` (`restaurant_id = current_restaurant_id`)
- Trigger async: gửi `name + description` → OpenAI Embeddings API → **INSERT** `product_embeddings`

---

## 3.3 Cập nhật sản phẩm

**`PUT /api/merchant/products/{id}`**

**Chức năng**: Cập nhật thông tin món ăn. Giá mới chỉ áp dụng cho đơn hàng mới.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER, APPROVED)

**Path Params**: `id` — UUID sản phẩm

**Request Body**:

```json
{
  "name": "Phở gà đặc biệt (size lớn)",
  "price": 70000.0,
  "description": "Phở gà size lớn - thêm thịt gà",
  "category_id": 1,
  "is_available": true
}
```

| Field          | Kiểu    | Bắt buộc | Mô tả                                        |
| -------------- | ------- | -------- | -------------------------------------------- |
| `name`         | string  | ❌       | Tên mới                                      |
| `price`        | number  | ❌       | Giá mới (chỉ áp dụng cho đơn hàng tương lai) |
| `description`  | string  | ❌       | Mô tả mới                                    |
| `category_id`  | int     | ❌       | Danh mục mới (phải thuộc nhà hàng này)       |
| `is_available` | boolean | ❌       | Trạng thái còn hàng                          |

**Response** `200 OK`:

```json
{
  "success": true,
  "data": {
    "id": "uuid-...",
    "name": "Phở gà đặc biệt (size lớn)",
    "price": 70000.0,
    "is_available": true
  },
  "message": "Cập nhật sản phẩm thành công"
}
```

> ⚠️ **Quan trọng**: Giá `price` mới chỉ áp dụng cho đơn hàng đặt **sau** khi cập nhật. Đơn hàng đã đặt giữ nguyên `price_at_purchase` (snapshot pattern).

**Lỗi phổ biến**:

| Điều kiện                         | HTTP | errorCode           |
| --------------------------------- | ---- | ------------------- |
| Sản phẩm không thuộc nhà hàng này | 403  | `PRODUCT_NOT_OWNED` |
| `id` không tồn tại                | 404  | `NOT_FOUND`         |

**Logic Database**:

- **SELECT** `products` kiểm tra `restaurant_id = current_restaurant_id`
- **UPDATE** `products`
- Trigger async: cập nhật `product_embeddings` nếu `name` hoặc `description` thay đổi

---

## 3.4 Bật/tắt trạng thái còn hàng

**`PATCH /api/merchant/products/{id}/availability`**

**Chức năng**: Nhanh chóng bật/tắt trạng thái "còn hàng" của một món — thao tác nhanh, không cần gửi toàn bộ body.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER, APPROVED)

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
  "data": {
    "id": "uuid-...",
    "name": "Phở gà đặc biệt",
    "is_available": false
  },
  "message": "Đã tắt trạng thái còn hàng"
}
```

**Logic Database**: **UPDATE** `products` set `is_available = {value}` WHERE `id` AND `restaurant_id = current_restaurant_id`

---

## 3.5 Xóa sản phẩm

**`DELETE /api/merchant/products/{id}`**

**Chức năng**: Xóa sản phẩm khỏi menu. Không thể xóa nếu sản phẩm đang có trong đơn hàng chưa hoàn thành.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER, APPROVED)

**Path Params**: `id` — UUID sản phẩm

**Response** `200 OK`:

```json
{
  "success": true,
  "data": null,
  "message": "Đã xóa sản phẩm thành công"
}
```

**Lỗi phổ biến**:

| Điều kiện                                  | HTTP | errorCode                 |
| ------------------------------------------ | ---- | ------------------------- |
| Sản phẩm đang có trong đơn PENDING → READY | 409  | `PRODUCT_IN_ACTIVE_ORDER` |
| Sản phẩm không thuộc nhà hàng này          | 403  | `PRODUCT_NOT_OWNED`       |

**Logic Database**:

- **SELECT** `order_items` JOIN `orders` WHERE `product_id = {id}` AND `order_status NOT IN ('COMPLETED', 'CANCELLED')` → nếu có thì từ chối
- **DELETE** `products` (cascade: `product_embeddings`, `media_files`)

---

## 3.6 Upload ảnh sản phẩm

**`POST /api/merchant/products/{id}/image`**

**Chức năng**: Upload hoặc thay thế ảnh cho sản phẩm.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER, APPROVED)

**Path Params**: `id` — UUID sản phẩm

**Content-Type**: `multipart/form-data`

**Request Body**:

```
file: <binary image file>
```

| Field  | Kiểu | Bắt buộc | Ràng buộc                |
| ------ | ---- | -------- | ------------------------ |
| `file` | file | ✅       | JPG/PNG/WebP, tối đa 5MB |

**Response** `200 OK`:

```json
{
  "success": true,
  "data": {
    "product_id": "uuid-...",
    "image_url": "https://minio.example.com/products/uuid-product/image.jpg"
  },
  "message": "Cập nhật ảnh sản phẩm thành công"
}
```

**Logic Database**:

- Upload file lên MinIO bucket `products/`
- **UPSERT** `media_files` (`entity_type = 'PRODUCT'`, `entity_id = {id}`)

---

# 4. Xử lý Đơn hàng

---

## 4.1 Danh sách đơn hàng mới (đang xử lý)

**`GET /api/merchant/orders/active`**

**Chức năng**: Lấy danh sách đơn hàng đang ở trạng thái `PENDING`, `CONFIRMED`, `PREPARING` — cần Merchant xử lý.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER, APPROVED)

**Query Params**:

| Param    | Kiểu   | Bắt buộc | Mô tả                                                            |
| -------- | ------ | -------- | ---------------------------------------------------------------- |
| `status` | string | ❌       | Lọc: `PENDING`, `CONFIRMED`, `PREPARING`. Mặc định: trả về cả 3. |

**Response** `200 OK`:

```json
{
  "success": true,
  "data": [
    {
      "order_id": "uuid-...",
      "order_code": "FD20240115002",
      "order_status": "PENDING",
      "placed_at": "2024-01-15T13:00:00Z",
      "customer": {
        "name": "Nguyễn Văn A",
        "phone": "090****567"
      },
      "items": [
        {
          "product_id": "uuid-...",
          "product_name": "Phở bò tái nạm",
          "quantity": 2,
          "unit_price": 65000.0,
          "subtotal": 130000.0
        }
      ],
      "subtotal": 130000.0,
      "delivery_address": "123 Nguyễn Huệ, Quận 1, TP.HCM",
      "note": "Ít cay, không hành"
    }
  ],
  "message": "OK"
}
```

**Logic Database**:

- **SELECT** `orders` WHERE `restaurant_id = current_restaurant_id` AND `order_status IN ('PENDING', 'CONFIRMED', 'PREPARING')`
- **JOIN** `order_items`, `products`, `users` (customer, chỉ lấy tên và phone đã mask)
- Sắp xếp theo `placed_at ASC` (đơn cũ hơn lên trước)

---

## 4.2 Lịch sử đơn hàng

**`GET /api/merchant/orders/history`**

**Chức năng**: Xem lịch sử tất cả đơn hàng của nhà hàng (mọi trạng thái).

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Query Params**:

| Param    | Kiểu   | Bắt buộc | Mô tả                     |
| -------- | ------ | -------- | ------------------------- |
| `status` | string | ❌       | Lọc theo trạng thái       |
| `from`   | date   | ❌       | Từ ngày `YYYY-MM-DD`      |
| `to`     | date   | ❌       | Đến ngày `YYYY-MM-DD`     |
| `page`   | int    | ❌       | Số trang (mặc định: 1)    |
| `size`   | int    | ❌       | Số bản ghi (mặc định: 20) |

**Response** `200 OK`:

```json
{
  "success": true,
  "data": {
    "total": 150,
    "page": 1,
    "size": 20,
    "items": [
      {
        "order_id": "uuid-...",
        "order_code": "FD20240115002",
        "order_status": "COMPLETED",
        "total_amount": 200000.0,
        "placed_at": "2024-01-15T13:00:00Z",
        "completed_at": "2024-01-15T13:45:00Z"
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:

- **SELECT** `orders` WHERE `restaurant_id = current_restaurant_id`
- Hỗ trợ filter và phân trang

---

## 4.3 Chi tiết đơn hàng

**`GET /api/merchant/orders/{id}`**

**Chức năng**: Xem thông tin đầy đủ của một đơn hàng.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Path Params**: `id` — UUID đơn hàng

**Response** `200 OK`:

```json
{
  "success": true,
  "data": {
    "order_id": "uuid-...",
    "order_code": "FD20240115002",
    "order_status": "PREPARING",
    "payment_status": "UNPAID",
    "customer": {
      "name": "Nguyễn Văn A",
      "phone": "090****567"
    },
    "items": [
      {
        "product_id": "uuid-...",
        "product_name": "Phở bò tái nạm",
        "quantity": 2,
        "unit_price": 65000.0,
        "subtotal": 130000.0
      }
    ],
    "subtotal": 130000.0,
    "shipping_fee": 25000.0,
    "discount_amount": 0.0,
    "total_amount": 155000.0,
    "commission_amount": 13000.0,
    "note": "Ít cay, không hành",
    "delivery_address": "123 Nguyễn Huệ, Quận 1, TP.HCM",
    "placed_at": "2024-01-15T13:00:00Z",
    "status_history": [
      { "status": "PENDING", "changed_at": "2024-01-15T13:00:00Z" },
      { "status": "CONFIRMED", "changed_at": "2024-01-15T13:05:00Z" },
      { "status": "PREPARING", "changed_at": "2024-01-15T13:10:00Z" }
    ]
  },
  "message": "OK"
}
```

**Lỗi phổ biến**:

| Điều kiện                         | HTTP | errorCode   |
| --------------------------------- | ---- | ----------- |
| Đơn hàng không thuộc nhà hàng này | 403  | `FORBIDDEN` |
| `id` không tồn tại                | 404  | `NOT_FOUND` |

**Logic Database**:

- **SELECT** `orders` WHERE `id` AND `restaurant_id = current_restaurant_id`
- **SELECT** `order_items` JOIN `products`
- **SELECT** `order_status_history` ORDER BY `changed_at`

---

## 4.4 Xác nhận đơn hàng

**`PATCH /api/merchant/orders/{id}/confirm`**

**Chức năng**: Merchant xác nhận đơn hàng. Chuyển `PENDING` → `CONFIRMED`.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER, APPROVED)

**Path Params**: `id` — UUID đơn hàng

**Request Body**: _(không cần body)_

**Response** `200 OK`:

```json
{
  "success": true,
  "data": {
    "order_id": "uuid-...",
    "order_code": "FD20240115002",
    "order_status": "CONFIRMED"
  },
  "message": "Đã xác nhận đơn hàng"
}
```

**Lỗi phổ biến**:

| Điều kiện                    | HTTP | errorCode             |
| ---------------------------- | ---- | --------------------- |
| `order_status ≠ PENDING`     | 409  | `ORDER_INVALID_STATE` |
| Đơn không thuộc nhà hàng này | 403  | `FORBIDDEN`           |

**Logic Database**:

- **SELECT** `orders` kiểm tra `restaurant_id` và `order_status = 'PENDING'`
- **UPDATE** `orders` set `order_status = 'CONFIRMED'`
- **INSERT** `order_status_history` (`status = 'CONFIRMED'`, `changed_by = current_user_id`)

---

## 4.5 Bắt đầu chuẩn bị món

**`PATCH /api/merchant/orders/{id}/prepare`**

**Chức năng**: Merchant bắt đầu chuẩn bị. Chuyển `CONFIRMED` → `PREPARING`.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER, APPROVED)

**Path Params**: `id` — UUID đơn hàng

**Response** `200 OK`:

```json
{
  "success": true,
  "data": { "order_id": "uuid-...", "order_status": "PREPARING" },
  "message": "Đang chuẩn bị đơn hàng"
}
```

**Lỗi phổ biến**:

| Điều kiện                  | HTTP | errorCode             |
| -------------------------- | ---- | --------------------- |
| `order_status ≠ CONFIRMED` | 409  | `ORDER_INVALID_STATE` |

**Logic Database**:

- **UPDATE** `orders` set `order_status = 'PREPARING'`
- **INSERT** `order_status_history`

---

## 4.6 Sẵn sàng giao hàng

**`PATCH /api/merchant/orders/{id}/ready`**

**Chức năng**: Merchant hoàn tất chuẩn bị. Chuyển `PREPARING` → `READY`. Đơn sẽ xuất hiện cho Shipper.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER, APPROVED)

**Path Params**: `id` — UUID đơn hàng

**Response** `200 OK`:

```json
{
  "success": true,
  "data": { "order_id": "uuid-...", "order_status": "READY" },
  "message": "Đơn hàng đã sẵn sàng để giao"
}
```

**Lỗi phổ biến**:

| Điều kiện                  | HTTP | errorCode             |
| -------------------------- | ---- | --------------------- |
| `order_status ≠ PREPARING` | 409  | `ORDER_INVALID_STATE` |

**Logic Database**:

- **UPDATE** `orders` set `order_status = 'READY'`
- **INSERT** `order_status_history`
- Notify Shipper (WebSocket broadcast đến Shipper trong khu vực)

---

## 4.7 Từ chối đơn hàng

**`PATCH /api/merchant/orders/{id}/reject`**

**Chức năng**: Merchant từ chối đơn hàng. Chỉ được từ chối khi đơn đang ở `PENDING`.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER, APPROVED)

**Path Params**: `id` — UUID đơn hàng

**Request Body**:

```json
{
  "reason": "Nhà hàng tạm đóng cửa để nâng cấp"
}
```

| Field    | Kiểu   | Bắt buộc | Ràng buộc   |
| -------- | ------ | -------- | ----------- |
| `reason` | string | ✅       | 1–500 ký tự |

**Response** `200 OK`:

```json
{
  "success": true,
  "data": {
    "order_id": "uuid-...",
    "order_status": "CANCELLED",
    "refund_triggered": true
  },
  "message": "Đã từ chối đơn hàng. Khách hàng sẽ được hoàn tiền nếu đã thanh toán."
}
```

**Lỗi phổ biến**:

| Điều kiện                | HTTP | errorCode             |
| ------------------------ | ---- | --------------------- |
| `order_status ≠ PENDING` | 409  | `ORDER_INVALID_STATE` |

**Logic Database**:

- **UPDATE** `orders` set `order_status = 'CANCELLED'`, `cancelled_at = NOW()`
- **INSERT** `order_status_history`
- Nếu `payment_status = 'PAID'`: UPDATE `payment_status = 'REFUNDED'`, trigger hoàn tiền async
- Nếu có voucher: **UPDATE** `vouchers` giảm `used_count`
- Gửi thông báo Customer (WebSocket/email)

---

# 5. Báo cáo & Đánh giá

---

## 5.1 Báo cáo doanh thu

**`GET /api/merchant/reports/revenue`**

**Chức năng**: Thống kê doanh thu nhà hàng theo khoảng thời gian. Chỉ tính các đơn `COMPLETED`.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Query Params**:

| Param  | Kiểu | Bắt buộc | Mô tả                 |
| ------ | ---- | -------- | --------------------- |
| `from` | date | ✅       | Từ ngày `YYYY-MM-DD`  |
| `to`   | date | ✅       | Đến ngày `YYYY-MM-DD` |

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
      "gross_revenue": 25000000.0,
      "commission_total": 2500000.0,
      "net_revenue": 22500000.0
    },
    "daily_breakdown": [
      {
        "date": "2024-01-15",
        "orders": 8,
        "gross_revenue": 1200000.0,
        "commission": 120000.0,
        "net_revenue": 1080000.0
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:

- **SELECT** `orders` WHERE `restaurant_id` AND `order_status = 'COMPLETED'` AND `placed_at BETWEEN from AND to`
- SUM `subtotal` (gross), SUM `commission_amount` (commission), tính `net = gross - commission`
- COUNT đơn hủy trong cùng khoảng thời gian
- GROUP BY `DATE(placed_at)` → `daily_breakdown`

---

## 5.2 Xem đánh giá nhà hàng

**`GET /api/merchant/reviews`**

**Chức năng**: Merchant xem đánh giá từ khách hàng, lọc theo thời gian và điểm.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Query Params**:

| Param    | Kiểu | Bắt buộc | Mô tả                           |
| -------- | ---- | -------- | ------------------------------- |
| `from`   | date | ❌       | Từ ngày `YYYY-MM-DD`            |
| `to`     | date | ❌       | Đến ngày `YYYY-MM-DD`           |
| `rating` | int  | ❌       | Lọc theo điểm (1–5)             |
| `page`   | int  | ❌       | Số trang (mặc định: 1)          |
| `size`   | int  | ❌       | Số bản ghi/trang (mặc định: 20) |

**Response** `200 OK`:

```json
{
  "success": true,
  "data": {
    "total": 120,
    "avg_rating": 4.3,
    "page": 1,
    "items": [
      {
        "review_id": "uuid-...",
        "order_code": "FD20240114001",
        "customer_name": "Trần Thị B",
        "rating": 5,
        "comment": "Phở ngon tuyệt vời, giao nhanh!",
        "created_at": "2024-01-14T20:00:00Z",
        "item_reviews": [
          {
            "product_name": "Phở bò tái nạm",
            "rating": 5,
            "comment": "Nước dùng ngọt thanh, thịt tươi"
          }
        ]
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:

- **SELECT** `order_reviews` WHERE `restaurant_id = current_restaurant_id`
- **JOIN** `orders` (lấy `order_code`), **JOIN** `users` (lấy tên customer)
- **SELECT** `order_item_reviews` JOIN `order_items` JOIN `products`
- AVG `rating` → `avg_rating`

---

# 6. Ví & Rút tiền

---

## 6.1 Xem thông tin ví

**`GET /api/merchant/wallet`**

**Chức năng**: Lấy số dư ví hiện tại và lịch sử giao dịch của Merchant.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Query Params** (lịch sử giao dịch):

| Param  | Kiểu | Bắt buộc | Mô tả                     |
| ------ | ---- | -------- | ------------------------- |
| `page` | int  | ❌       | Số trang (mặc định: 1)    |
| `size` | int  | ❌       | Số bản ghi (mặc định: 20) |

**Response** `200 OK`:

```json
{
  "success": true,
  "data": {
    "wallet_id": "uuid-...",
    "balance": 15000000.0,
    "transactions": {
      "total": 85,
      "page": 1,
      "items": [
        {
          "transaction_code": "TXN-20240115-001",
          "type": "RECEIVE_PAYMENT",
          "amount": 250000.0,
          "description": "Thanh toán đơn hàng FD20240115002",
          "created_at": "2024-01-15T13:45:00Z"
        },
        {
          "transaction_code": "TXN-20240110-005",
          "type": "WITHDRAW",
          "amount": -5000000.0,
          "description": "Rút tiền về Vietcombank ****1234",
          "created_at": "2024-01-10T09:00:00Z"
        }
      ]
    }
  },
  "message": "OK"
}
```

**Logic Database**:

- **SELECT** `wallets` WHERE `user_id = current_user_id`
- **SELECT** `wallet_transactions` WHERE `wallet_id` ORDER BY `created_at DESC`

---

## 6.2 Danh sách phương thức rút tiền

**`GET /api/merchant/payout-methods`**

**Chức năng**: Lấy danh sách tài khoản ngân hàng đã đăng ký.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Response** `200 OK`:

```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-...",
      "bank_name": "Vietcombank",
      "bank_account_number": "****1234",
      "bank_account_holder": "NGUYEN VAN A",
      "bank_branch": "Chi nhánh TP.HCM",
      "is_default": true
    }
  ],
  "message": "OK"
}
```

**Logic Database**: **SELECT** `payout_methods` WHERE `user_id = current_user_id`

---

## 6.3 Thêm phương thức rút tiền

**`POST /api/merchant/payout-methods`**

**Chức năng**: Thêm tài khoản ngân hàng mới để nhận tiền.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Request Body**:

```json
{
  "bank_name": "Techcombank",
  "bank_account_number": "19012345678901",
  "bank_account_holder": "NGUYEN VAN A",
  "bank_branch": "Chi nhánh Quận 1",
  "is_default": false
}
```

| Field                 | Kiểu    | Bắt buộc | Ràng buộc                  |
| --------------------- | ------- | -------- | -------------------------- |
| `bank_name`           | string  | ✅       | ≤ 100 ký tự                |
| `bank_account_number` | string  | ✅       | Số tài khoản               |
| `bank_account_holder` | string  | ✅       | Tên chủ tài khoản (IN HOA) |
| `bank_branch`         | string  | ❌       | Chi nhánh                  |
| `is_default`          | boolean | ❌       | Mặc định: `false`          |

**Response** `201 Created`:

```json
{
  "success": true,
  "data": {
    "id": "uuid-...",
    "bank_name": "Techcombank",
    "bank_account_number": "****8901",
    "bank_account_holder": "NGUYEN VAN A",
    "is_default": false
  },
  "message": "Thêm phương thức rút tiền thành công"
}
```

**Logic Database**:

- Nếu `is_default = true`: **UPDATE** `payout_methods` set `is_default = false` WHERE `user_id`
- **INSERT** `payout_methods`

---

## 6.4 Gửi yêu cầu rút tiền

**`POST /api/merchant/payout-requests`**

**Chức năng**: Merchant gửi yêu cầu rút tiền từ ví về tài khoản ngân hàng.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER, APPROVED)

**Request Body**:

```json
{
  "amount": 5000000.0,
  "payout_method_id": "uuid-..."
}
```

| Field              | Kiểu   | Bắt buộc | Ràng buộc                      |
| ------------------ | ------ | -------- | ------------------------------ |
| `amount`           | number | ✅       | amount > 0 và ≤ wallet.balance |
| `payout_method_id` | UUID   | ✅       | Phải thuộc user hiện tại       |

**Response** `201 Created`:

```json
{
  "success": true,
  "data": {
    "request_id": "uuid-...",
    "amount": 5000000.0,
    "status": "PENDING",
    "bank_info": {
      "bank_name": "Vietcombank",
      "account_number": "****1234",
      "account_holder": "NGUYEN VAN A"
    },
    "wallet_balance_after": 10000000.0,
    "created_at": "2024-01-15T09:00:00Z"
  },
  "message": "Yêu cầu rút tiền đã được gửi. Admin sẽ xử lý trong 1–3 ngày làm việc."
}
```

**Lỗi phổ biến**:

| Điều kiện                               | HTTP | errorCode              |
| --------------------------------------- | ---- | ---------------------- |
| `wallet.balance < amount`               | 400  | `INSUFFICIENT_BALANCE` |
| `payout_method_id` không thuộc user này | 403  | `FORBIDDEN`            |

**Logic Database**:

- **SELECT** `wallets` WHERE `user_id = current_user_id` — kiểm tra `balance >= amount`
- **SELECT** `payout_methods` — kiểm tra ownership
- **BEGIN TRANSACTION**:
  - **INSERT** `payout_requests` (`status = PENDING`)
  - **UPDATE** `wallets` set `balance = balance - amount` (tạm giữ)
- **COMMIT**

---

## 6.5 Lịch sử yêu cầu rút tiền

**`GET /api/merchant/payout-requests`**

**Chức năng**: Xem lịch sử các yêu cầu rút tiền đã gửi.

**Authentication**: ✅ Bearer Token (RESTAURANT_OWNER)

**Query Params**: `page`, `size`, `status` (PENDING/APPROVED/REJECTED)

**Response** `200 OK`:

```json
{
  "success": true,
  "data": {
    "total": 10,
    "items": [
      {
        "request_id": "uuid-...",
        "amount": 5000000.0,
        "status": "APPROVED",
        "bank_name": "Vietcombank",
        "account_number": "****1234",
        "created_at": "2024-01-10T09:00:00Z",
        "approved_at": "2024-01-11T14:00:00Z"
      }
    ]
  },
  "message": "OK"
}
```

**Logic Database**:

- **SELECT** `payout_requests` JOIN `wallets` WHERE `wallets.user_id = current_user_id`
- **JOIN** `payout_methods` (lấy thông tin ngân hàng)

---

## Tóm tắt API — Module Merchant

| Method   | Endpoint                                   | Chức năng                      | Auth     |
| -------- | ------------------------------------------ | ------------------------------ | -------- |
| `POST`   | `/api/merchant/register`                   | Đăng ký nhà hàng               | ✅       |
| `GET`    | `/api/merchant/profile`                    | Xem hồ sơ nhà hàng             | ✅ OWNER |
| `PUT`    | `/api/merchant/profile`                    | Cập nhật hồ sơ nhà hàng        | ✅ OWNER |
| `POST`   | `/api/merchant/profile/cover-image`        | Upload ảnh bìa                 | ✅ OWNER |
| `GET`    | `/api/merchant/categories`                 | Danh sách danh mục             | ✅ OWNER |
| `POST`   | `/api/merchant/categories`                 | Tạo danh mục                   | ✅ OWNER |
| `PUT`    | `/api/merchant/categories/{id}`            | Cập nhật danh mục              | ✅ OWNER |
| `DELETE` | `/api/merchant/categories/{id}`            | Xóa danh mục                   | ✅ OWNER |
| `GET`    | `/api/merchant/products`                   | Danh sách sản phẩm             | ✅ OWNER |
| `POST`   | `/api/merchant/products`                   | Thêm sản phẩm                  | ✅ OWNER |
| `PUT`    | `/api/merchant/products/{id}`              | Cập nhật sản phẩm              | ✅ OWNER |
| `PATCH`  | `/api/merchant/products/{id}/availability` | Bật/tắt còn hàng               | ✅ OWNER |
| `DELETE` | `/api/merchant/products/{id}`              | Xóa sản phẩm                   | ✅ OWNER |
| `POST`   | `/api/merchant/products/{id}/image`        | Upload ảnh sản phẩm            | ✅ OWNER |
| `GET`    | `/api/merchant/orders/active`              | Đơn hàng đang xử lý            | ✅ OWNER |
| `GET`    | `/api/merchant/orders/history`             | Lịch sử đơn hàng               | ✅ OWNER |
| `GET`    | `/api/merchant/orders/{id}`                | Chi tiết đơn hàng              | ✅ OWNER |
| `PATCH`  | `/api/merchant/orders/{id}/confirm`        | Xác nhận đơn                   | ✅ OWNER |
| `PATCH`  | `/api/merchant/orders/{id}/prepare`        | Bắt đầu chuẩn bị               | ✅ OWNER |
| `PATCH`  | `/api/merchant/orders/{id}/ready`          | Sẵn sàng giao                  | ✅ OWNER |
| `PATCH`  | `/api/merchant/orders/{id}/reject`         | Từ chối đơn                    | ✅ OWNER |
| `GET`    | `/api/merchant/reports/revenue`            | Báo cáo doanh thu              | ✅ OWNER |
| `GET`    | `/api/merchant/reviews`                    | Đánh giá nhà hàng              | ✅ OWNER |
| `GET`    | `/api/merchant/wallet`                     | Xem ví                         | ✅ OWNER |
| `GET`    | `/api/merchant/payout-methods`             | Danh sách phương thức rút tiền | ✅ OWNER |
| `POST`   | `/api/merchant/payout-methods`             | Thêm phương thức rút tiền      | ✅ OWNER |
| `POST`   | `/api/merchant/payout-requests`            | Gửi yêu cầu rút tiền           | ✅ OWNER |
| `GET`    | `/api/merchant/payout-requests`            | Lịch sử yêu cầu rút tiền       | ✅ OWNER |

---

_Tài liệu API đặc tả đầy đủ cho module Restaurant (Merchant) — Food Delivery Platform. Tuân thủ RESTful best practices và chuẩn đặt tên Spring Boot._
