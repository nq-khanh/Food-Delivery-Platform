# Requirements — Module Restaurant (Merchant)

> **Dự án**: Food Delivery Platform  
> **Module**: Restaurant (Merchant)  
> **Desciption**: Đặc tả chi tiết yêu cầu chi tiết của Merchant  
> **Phiên bản**: 1.0  
> **Ngày**: 15-03-2026

---

## 1. Tổng quan module

Module **Restaurant (Merchant)** quản lý toàn bộ nghiệp vụ dành cho chủ nhà hàng (Merchant) trên nền tảng Food Delivery Platform. Merchant là đối tác kinh doanh — họ đăng ký nhà hàng, quản lý thực đơn, xử lý đơn hàng và nhận doanh thu thông qua ví hệ thống.

Phạm vi module bao gồm:

| Nhóm chức năng         | Mô tả                                                   |
| ---------------------- | ------------------------------------------------------- |
| Đăng ký & Phê duyệt    | Merchant đăng ký nhà hàng, Admin phê duyệt hoặc từ chối |
| Quản lý hồ sơ nhà hàng | Cập nhật thông tin, ảnh bìa, giờ hoạt động              |
| Quản lý thực đơn       | CRUD danh mục (category) và sản phẩm (product)          |
| Xử lý đơn hàng         | Nhận đơn, xác nhận, chuẩn bị, chuyển giao Shipper       |
| Báo cáo & Doanh thu    | Thống kê theo ngày/tuần/tháng, xem đánh giá khách       |
| Ví & Rút tiền          | Xem số dư ví, lịch sử giao dịch, yêu cầu rút tiền       |

---

## 2. Yêu cầu chức năng (Functional Requirements)

### 2.1 Đăng ký & Phê duyệt nhà hàng

| Mã          | Mô tả yêu cầu                                                                                                                                                 |
| ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **FR-M01**  | Merchant đăng ký nhà hàng từ tài khoản Customer hiện có, cung cấp: tên, địa chỉ (có toạ độ lat/lng), mô tả, ảnh bìa, giờ hoạt động theo từng ngày trong tuần. |
| **FR-M01a** | Trạng thái ban đầu sau khi đăng ký là `PENDING`. Merchant chưa thể hoạt động cho đến khi Admin phê duyệt.                                                     |
| **FR-M01b** | Merchant nhận thông báo qua email khi hồ sơ được Admin phê duyệt hoặc từ chối (kèm lý do từ chối).                                                            |
| **FR-M01c** | Một tài khoản user chỉ có thể sở hữu **một nhà hàng** tại một thời điểm.                                                                                      |

### 2.2 Quản lý thông tin nhà hàng

| Mã          | Mô tả yêu cầu                                                                                                                       |
| ----------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| **FR-M02**  | Merchant cập nhật thông tin nhà hàng: tên, địa chỉ, mô tả, ảnh bìa, trạng thái hoạt động (`is_active`).                             |
| **FR-M03**  | Merchant quản lý giờ hoạt động theo từng ngày trong tuần (T2–CN), bao gồm giờ mở/đóng và nghỉ cả ngày.                              |
| **FR-M03a** | Merchant bật/tắt trạng thái hoạt động tổng thể (`is_active`) — khi tắt, nhà hàng không hiển thị với Customer và không nhận đơn mới. |
| **FR-M03b** | Chỉ nhà hàng đang trong giờ hoạt động (`is_active = true` AND đang trong khung giờ hôm nay) mới được phép nhận đơn mới.             |

### 2.3 Quản lý thực đơn — Danh mục (Category)

| Mã          | Mô tả yêu cầu                                                                                                                              |
| ----------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| **FR-M04**  | Merchant tạo danh mục mới trong menu (tên, thứ tự hiển thị).                                                                               |
| **FR-M04a** | Merchant cập nhật tên và thứ tự hiển thị danh mục.                                                                                         |
| **FR-M04b** | Merchant xóa danh mục. Sản phẩm thuộc danh mục bị xóa sẽ chuyển về trạng thái không có danh mục (`category_id = NULL`), không bị xóa theo. |
| **FR-M04c** | Merchant sắp xếp lại thứ tự các danh mục theo `display_order`.                                                                             |

### 2.4 Quản lý thực đơn — Sản phẩm (Product)

| Mã          | Mô tả yêu cầu                                                                                                                                        |
| ----------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- | ---- |
| **FR-M05**  | Merchant thêm món ăn mới: tên, giá, mô tả, danh mục. Hệ thống tự động tạo vector embedding async sau khi tạo.                                        | Must |
| **FR-M05a** | Merchant cập nhật thông tin món ăn: tên, giá, mô tả, danh mục, ảnh. Giá mới chỉ áp dụng cho đơn hàng mới; đơn đã đặt giữ nguyên `price_at_purchase`. | Must |
| **FR-M05b** | Merchant xóa món ăn. Không thể xóa món đang có trong đơn hàng chưa hoàn thành.                                                                       | Must |
| **FR-M06**  | Merchant bật/tắt trạng thái "còn hàng" (`is_available`) của từng món — thao tác nhanh, không cần cập nhật toàn bộ thông tin.                         | Must |
| **FR-M05c** | Merchant upload ảnh cho món ăn (tích hợp MinIO).                                                                                                     | Must |

### 2.5 Xử lý đơn hàng

| Mã          | Mô tả yêu cầu                                                                                                                                |
| ----------- | -------------------------------------------------------------------------------------------------------------------------------------------- | ------ |
| **FR-M07**  | Merchant xem danh sách đơn hàng mới (trạng thái `PENDING`, `CONFIRMED`, `PREPARING`). Danh sách cập nhật qua polling hoặc WebSocket (STOMP). | Must   |
| **FR-M07a** | Merchant xem chi tiết đơn hàng: danh sách món, số lượng, ghi chú của khách, thông tin địa chỉ giao hàng.                                     | Must   |
| **FR-M08**  | Merchant xác nhận đơn hàng: `PENDING` → `CONFIRMED`.                                                                                         | Must   |
| **FR-M08a** | Merchant từ chối đơn hàng (kèm lý do): `PENDING` → `CANCELLED`. Hệ thống hoàn tiền cho Customer nếu đã thanh toán.                           | Must   |
| **FR-M08b** | Merchant cập nhật trạng thái chuẩn bị: `CONFIRMED` → `PREPARING`.                                                                            | Must   |
| **FR-M08c** | Merchant đánh dấu sẵn sàng giao: `PREPARING` → `READY`. Shipper có thể thấy và nhận đơn.                                                     | Must   |
| **FR-M07b** | Merchant xem lịch sử toàn bộ đơn hàng (tất cả trạng thái), có thể lọc theo trạng thái và thời gian.                                          | Should |

### 2.6 Báo cáo & Đánh giá

| Mã          | Mô tả yêu cầu                                                                                                                                    |
| ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------ | ------ |
| **FR-M09**  | Merchant xem thống kê doanh thu: tổng đơn, đơn hoàn thành, tổng doanh thu, tổng hoa hồng, doanh thu ròng. Hỗ trợ lọc theo khoảng ngày tùy chỉnh. | Must   |
| **FR-M09a** | Thống kê doanh thu có breakdown theo ngày.                                                                                                       | Must   |
| **FR-M09b** | Merchant xem đánh giá từ khách hàng cho nhà hàng và từng món ăn, có thể lọc theo điểm và thời gian.                                              | Should |

### 2.7 Ví & Rút tiền

| Mã          | Mô tả yêu cầu                                                                                                                     |
| ----------- | --------------------------------------------------------------------------------------------------------------------------------- | ------ |
| **FR-M10**  | Merchant quản lý phương thức rút tiền (thông tin ngân hàng): thêm, sửa, xóa, đặt mặc định.                                        | Must   |
| **FR-M10a** | Merchant xem số dư ví điện tử hiện tại và lịch sử giao dịch.                                                                      | Must   |
| **FR-M10b** | Merchant gửi yêu cầu rút tiền: chọn phương thức thanh toán và nhập số tiền muốn rút. Hệ thống kiểm tra số dư đủ và tạm giữ số dư. | Must   |
| **FR-M10c** | Merchant xem lịch sử yêu cầu rút tiền và trạng thái (PENDING / APPROVED / REJECTED).                                              | Should |

---

## 3. Quy tắc nghiệp vụ (Business Rules)

| Mã         | Quy tắc                                                                                                                                                             |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **BR-M01** | Merchant chỉ được tiếp nhận đơn hàng khi `approval_status = APPROVED` AND `is_active = true` AND nhà hàng đang trong giờ hoạt động.                                 |
| **BR-M02** | Merchant chỉ được thao tác trên đơn hàng của chính nhà hàng mình (kiểm tra `restaurant_id`).                                                                        |
| **BR-M03** | Trình tự chuyển trạng thái đơn hàng bắt buộc: `PENDING` → `CONFIRMED` → `PREPARING` → `READY`. Không được bỏ qua bước.                                              |
| **BR-M04** | Khi Merchant từ chối đơn (`PENDING` → `CANCELLED`): hệ thống cần hoàn tiền cho Customer nếu đã thanh toán; giảm `used_count` voucher (nếu có).                      |
| **BR-M05** | Giá sản phẩm (`price`) chỉ áp dụng cho đơn hàng **mới**. Đơn đã đặt lưu `price_at_purchase` — snapshot không thay đổi theo giá hiện tại.                            |
| **BR-M06** | Yêu cầu rút tiền chỉ hợp lệ khi `wallet.balance >= amount`. Số dư bị tạm giữ ngay khi tạo request (tránh double-spend).                                             |
| **BR-M07** | Doanh thu phân chia sau khi đơn `COMPLETED`: Merchant nhận `subtotal - commission_amount`; tỷ lệ hoa hồng được snapshot vào đơn hàng tại thời điểm checkout.        |
| **BR-M08** | Sản phẩm bị tắt (`is_available = false`) không hiển thị để Customer thêm vào giỏ hàng, nhưng vẫn tồn tại trong các đơn hàng đã đặt.                                 |
| **BR-M09** | Khi xóa danh mục, sản phẩm thuộc danh mục đó không bị xóa — `category_id` được set `NULL` (ON DELETE SET NULL).                                                     |
| **BR-M10** | Một Merchant chỉ sở hữu một nhà hàng tại một thời điểm.                                                                                                             |
| **BR-M11** | Vector embedding của nhà hàng và sản phẩm được tạo/cập nhật **bất đồng bộ** (async) sau khi Admin phê duyệt nhà hàng hoặc sau khi tên/mô tả sản phẩm được thay đổi. |

---

## 4. Luồng trạng thái đơn hàng (Order State Machine — phần Merchant)

```
                        [Merchant xác nhận]
   PENDING ──────────────────────────────────► CONFIRMED
      │                                             │
      │ [Merchant từ chối / Customer hủy]           │ [Merchant bắt đầu nấu]
      ▼                                             ▼
  CANCELLED                                    PREPARING
                                                    │
                                                    │ [Merchant hoàn thành]
                                                    ▼
                                                  READY
                                                    │
                                          [Shipper nhận đơn - pessimistic lock]
                                                    ▼
                                                 SHIPPING
                                                    │
                                          [Shipper xác nhận lấy hàng]
                                                    ▼
                                               DELIVERING
                                                    │
                                          [Shipper xác nhận giao thành công]
                                                    ▼
                                               COMPLETED
```

**Chú thích**: Merchant chỉ trực tiếp điều khiển các trạng thái từ `PENDING` đến `READY`. Từ `SHIPPING` trở đi do Shipper điều khiển — Merchant chỉ quan sát.

---

## 5. Yêu cầu phi chức năng (Non-Functional Requirements)

### 5.1 Hiệu năng

| Mã          | Yêu cầu                                                                    | Ngưỡng                          |
| ----------- | -------------------------------------------------------------------------- | ------------------------------- |
| **NFR-P01** | API truy vấn đơn hàng, sản phẩm phải trả về trong thời gian chấp nhận được |
| **NFR-P02** | Danh sách đơn hàng mới phải được polling cập nhật thường xuyên             | Mỗi 15–30s hoặc WebSocket STOMP |
| **NFR-P03** | Truy vấn lịch sử đơn hàng có phân trang                                    | Mặc định 20 bản ghi/trang       |

### 5.2 Bảo mật

| Mã          | Yêu cầu                                                                                                                               |
| ----------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| **NFR-S01** | Tất cả API của Merchant phải yêu cầu JWT Bearer Token hợp lệ.                                                                         |
| **NFR-S02** | Phân quyền RBAC: chỉ user có role `RESTAURANT_OWNER` và `approval_status = APPROVED` mới được gọi Merchant API.                       |
| **NFR-S03** | Merchant chỉ truy cập dữ liệu của **nhà hàng mình**. Backend phải kiểm tra `restaurant.owner_id = current_user_id` trên mọi thao tác. |
| **NFR-S04** | Thông tin ngân hàng (tài khoản rút tiền) phải được mã hóa hoặc che một phần khi hiển thị (`****1234`).                                |

### 5.3 Độ tin cậy

| Mã          | Yêu cầu                                                                                             |
| ----------- | --------------------------------------------------------------------------------------------------- |
| **NFR-R01** | Thao tác đọc/ghi ví và payout phải sử dụng DB transaction để tránh race condition.                  |
| **NFR-R02** | Pessimistic lock khi Shipper nhận đơn (`SELECT FOR UPDATE`) để tránh hai Shipper cùng nhận một đơn. |
| **NFR-R03** | Vector embedding được tạo bất đồng bộ — nếu thất bại phải có cơ chế retry.                          |
| **NFR-R04** | Toàn bộ thay đổi trạng thái đơn hàng phải được ghi vào `order_status_history` (audit log).          |

### 5.4 Khả năng bảo trì

| Mã          | Yêu cầu                                                                                  |
| ----------- | ---------------------------------------------------------------------------------------- |
| **NFR-M01** | API phải được document đầy đủ bằng Swagger/OpenAPI.                                      |
| **NFR-M02** | Convention đặt tên Spring Boot: Controller → Service → Repository, package theo feature. |
| **NFR-M03** | Database migration sử dụng Flyway hoặc Liquibase, không dùng `ddl-auto=create`.          |

### 5.5 Lưu trữ file

| Mã          | Yêu cầu                                                                             |
| ----------- | ----------------------------------------------------------------------------------- |
| **NFR-F01** | Ảnh nhà hàng và sản phẩm được lưu trên MinIO (self-hosted, tương thích S3).         |
| **NFR-F02** | Bucket: `restaurants/` cho ảnh bìa nhà hàng, `products/` cho ảnh món ăn.            |
| **NFR-F03** | URL ảnh được lưu vào bảng `media_files` với `entity_type` và `entity_id` tương ứng. |

---

## 6. Dữ liệu cần thiết (Data Requirements)

### 6.1 Bảng chính liên quan module Merchant

| Bảng                         | Vai trò                                          |
| ---------------------------- | ------------------------------------------------ |
| `restaurants`                | Thông tin nhà hàng, trạng thái phê duyệt, rating |
| `restaurant_operating_hours` | Giờ hoạt động theo từng ngày                     |
| `categories`                 | Danh mục món ăn trong menu                       |
| `products`                   | Món ăn: tên, giá, mô tả, trạng thái còn hàng     |
| `orders`                     | Đơn hàng: snapshot giá, hoa hồng, trạng thái     |
| `order_items`                | Chi tiết từng món trong đơn, `price_at_purchase` |
| `order_status_history`       | Audit log mọi thay đổi trạng thái đơn            |
| `wallets`                    | Số dư ví của Merchant                            |
| `wallet_transactions`        | Lịch sử giao dịch ví (nhận tiền, rút tiền)       |
| `payout_methods`             | Thông tin ngân hàng để rút tiền                  |
| `payout_requests`            | Yêu cầu rút tiền                                 |
| `order_reviews`              | Đánh giá của Customer về đơn hàng                |
| `media_files`                | Ảnh nhà hàng và sản phẩm                         |
| `restaurant_embeddings`      | Vector embedding cho AI semantic search          |
| `product_embeddings`         | Vector embedding cho AI semantic search          |

### 6.2 Enumerations

| Enum                      | Giá trị                                                                                          |
| ------------------------- | ------------------------------------------------------------------------------------------------ |
| `approval_status`         | `PENDING`, `APPROVED`, `REJECTED`                                                                |
| `order_status`            | `PENDING`, `CONFIRMED`, `PREPARING`, `READY`, `SHIPPING`, `DELIVERING`, `COMPLETED`, `CANCELLED` |
| `payment_status`          | `UNPAID`, `PAID`, `REFUNDED`                                                                     |
| `wallet_transaction_type` | `DEPOSIT`, `WITHDRAW`, `RECEIVE_PAYMENT`                                                         |
| `payout_status`           | `PENDING`, `APPROVED`, `REJECTED`                                                                |

---

## 7. Tích hợp & Phụ thuộc ngoài

| Tích hợp                     | Mục đích                                                    | Ghi chú                         |
| ---------------------------- | ----------------------------------------------------------- | ------------------------------- |
| **MinIO**                    | Upload ảnh bìa nhà hàng và ảnh món ăn                       | Self-hosted, tương thích S3 API |
| **OpenAI Embeddings API**    | Tạo vector embedding cho restaurant & product               | Async, retry nếu thất bại       |
| **JavaMail (Spring Mail)**   | Gửi email thông báo phê duyệt/từ chối hồ sơ; kết quả payout | Cần config SMTP                 |
| **Spring WebSocket (STOMP)** | Real-time cập nhật đơn hàng mới cho Merchant                | Chỉ cho luồng theo dõi đơn      |
| **pgvector (PostgreSQL)**    | Semantic search nhà hàng và món ăn                          | HNSW index, cosine similarity   |

---
