# Use Case — Module Restaurant (Merchant)

> **Dự án**: Food Delivery Platform  
> **Module**: Restaurant (Merchant)  
> **Desciption**: Đặc tả chi tiết Use Case của Merchant  
> **Phiên bản**: 1.0  
> **Ngày**: 15-03-2026

---

## 1. Danh sách Use Case

| Mã UC      | Actor chính | Tên Use Case                               | FR liên quan   |
| ---------- | ----------- | ------------------------------------------ | -------------- |
| **UC-M01** | Merchant    | Đăng ký nhà hàng                           | FR-M01         |
| **UC-M02** | Merchant    | Quản lý thông tin & giờ hoạt động nhà hàng | FR-M02, FR-M03 |
| **UC-M03** | Merchant    | Quản lý danh mục (Category)                | FR-M04         |
| **UC-M04** | Merchant    | Quản lý sản phẩm (Product)                 | FR-M05, FR-M06 |
| **UC-M05** | Merchant    | Xử lý đơn hàng                             | FR-M07, FR-M08 |
| **UC-M06** | Merchant    | Xem báo cáo doanh thu                      | FR-M09         |
| **UC-M07** | Merchant    | Xem đánh giá khách hàng                    | FR-M09b        |
| **UC-M08** | Merchant    | Quản lý ví & yêu cầu rút tiền              | FR-M10         |
| **UC-A01** | Admin       | Phê duyệt / Từ chối hồ sơ nhà hàng         | —              |
| **UC-A02** | Admin       | Xử lý yêu cầu rút tiền của Merchant        | —              |
| **UC-S01** | System      | Tạo vector embedding (async)               | FR-M05, BR-M11 |
| **UC-S02** | System      | Phân chia doanh thu sau COMPLETED          | BR-M07         |

---

## 2. Đặc tả chi tiết Use Case

---

### UC-M01 — Đăng ký nhà hàng

| Thuộc tính               | Nội dung                                                                                                                                 |
| ------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------- |
| **Mã UC**                | UC-M01                                                                                                                                   |
| **Tên**                  | Đăng ký nhà hàng                                                                                                                         |
| **Actor chính**          | Merchant (người dùng có tài khoản Customer)                                                                                              |
| **Actor phụ**            | Admin (phê duyệt), System (tạo embedding)                                                                                                |
| **Mô tả**                | Người dùng đăng ký trở thành chủ nhà hàng bằng cách cung cấp thông tin nhà hàng. Hồ sơ ở trạng thái PENDING cho đến khi Admin phê duyệt. |
| **Điều kiện tiên quyết** | •Người dùng đã đăng nhập và có tài khoản đang hoạt động. •Người dùng chưa sở hữu nhà hàng nào đang hoạt động.                            |

**Luồng chính**:

1. Merchant truy cập trang "Đăng ký nhà hàng".
2. Merchant nhập thông tin: tên nhà hàng, địa chỉ, mô tả, upload ảnh bìa.
3. Merchant thiết lập giờ hoạt động theo từng ngày trong tuần (T2–CN), có thể đánh dấu ngày nghỉ.
4. Merchant xác nhận gửi đơn đăng ký.
5. Hệ thống xác thực dữ liệu đầu vào (tên, địa chỉ, toạ độ bắt buộc).
6. Hệ thống INSERT vào `restaurants` (`approval_status = PENDING`, `is_active = false`).
7. Hệ thống INSERT vào `restaurant_operating_hours`.
8. Hệ thống gán role `RESTAURANT_OWNER` cho user (nếu chưa có).
9. Hệ thống hiển thị thông báo: _"Đăng ký thành công. Vui lòng chờ Admin phê duyệt."_

**Luồng thay thế**:

- **5a. Dữ liệu không hợp lệ** (thiếu tên, thiếu toạ độ): Hiển thị lỗi validation, cho phép sửa và thử lại.
- **8a. User đã có nhà hàng PENDING hoặc APPROVED**: Từ chối đăng ký, thông báo _"Bạn đã có nhà hàng đang hoạt động."_

**Hậu điều kiện**: Nhà hàng được tạo với `approval_status = PENDING`. Admin sẽ thấy nhà hàng trong danh sách chờ duyệt.

---

### UC-M02 — Quản lý thông tin & giờ hoạt động nhà hàng

| Thuộc tính               | Nội dung                                                                                |
| ------------------------ | --------------------------------------------------------------------------------------- |
| **Mã UC**                | UC-M02                                                                                  |
| **Tên**                  | Quản lý thông tin & giờ hoạt động nhà hàng                                              |
| **Actor chính**          | Merchant                                                                                |
| **Mô tả**                | Merchant cập nhật thông tin nhà hàng và quản lý giờ hoạt động từng ngày trong tuần.     |
| **Điều kiện tiên quyết** | Merchant đã đăng nhập. Nhà hàng đã được Admin phê duyệt (`approval_status = APPROVED`). |

**Luồng chính (Cập nhật hồ sơ)**:

1. Merchant vào trang "Quản lý nhà hàng" → tab "Thông tin".
2. Merchant chỉnh sửa: tên, mô tả, địa chỉ, ảnh bìa.
3. Merchant bật/tắt trạng thái hoạt động (`is_active`).
4. Merchant lưu lại.
5. Hệ thống UPDATE `restaurants`.
6. Nếu tên/mô tả thay đổi: trigger async cập nhật `restaurant_embeddings`.

**Luồng chính (Cập nhật giờ hoạt động)**:

1. Merchant vào tab "Giờ hoạt động".
2. Merchant chọn ngày, thiết lập giờ mở/đóng hoặc đánh dấu nghỉ (`is_closed = true`).
3. Hệ thống UPSERT `restaurant_operating_hours` (theo `restaurant_id + day_of_week`).

**Hậu điều kiện**: Thông tin nhà hàng được cập nhật. Nếu `is_active = false`, nhà hàng không hiển thị trong kết quả tìm kiếm của Customer.

---

### UC-M03 — Quản lý danh mục (Category)

| Thuộc tính               | Nội dung                                                                  |
| ------------------------ | ------------------------------------------------------------------------- |
| **Mã UC**                | UC-M03                                                                    |
| **Tên**                  | Quản lý danh mục (Category)                                               |
| **Actor chính**          | Merchant                                                                  |
| **Mô tả**                | Merchant tổ chức menu bằng cách tạo, cập nhật và xóa các danh mục món ăn. |
| **Điều kiện tiên quyết** | Merchant đã đăng nhập. Nhà hàng đang hoạt động (`APPROVED`).              |

**Luồng chính — Tạo danh mục**:

1. Merchant nhập tên danh mục và thứ tự hiển thị.
2. Hệ thống INSERT `categories`.
3. Danh mục hiển thị trong menu nhà hàng ngay lập tức.

**Luồng chính — Cập nhật danh mục**:

1. Merchant chọn danh mục, cập nhật tên và/hoặc thứ tự hiển thị.
2. Hệ thống kiểm tra `restaurant_id` khớp với owner hiện tại.
3. Hệ thống UPDATE `categories`.

**Luồng chính — Xóa danh mục**:

1. Merchant chọn xóa danh mục.
2. Hệ thống hiển thị cảnh báo: _"Sản phẩm thuộc danh mục này sẽ không còn danh mục (nhưng không bị xóa)."_
3. Merchant xác nhận.
4. Hệ thống DELETE `categories` → PostgreSQL ON DELETE SET NULL cập nhật `products.category_id = NULL`.

**Hậu điều kiện**: Danh mục được tạo/cập nhật/xóa. Sản phẩm không bị xóa theo.

---

### UC-M04 — Quản lý sản phẩm (Product)

| Thuộc tính               | Nội dung                                                           |
| ------------------------ | ------------------------------------------------------------------ |
| **Mã UC**                | UC-M04                                                             |
| **Tên**                  | Quản lý sản phẩm (Product)                                         |
| **Actor chính**          | Merchant                                                           |
| **Actor phụ**            | System (tạo embedding async)                                       |
| **Mô tả**                | Merchant thêm, sửa, xóa món ăn và kiểm soát trạng thái "còn hàng". |
| **Điều kiện tiên quyết** | Merchant đã đăng nhập, nhà hàng đang hoạt động (`APPROVED`).       |

**Luồng chính — Thêm sản phẩm**:

1. Merchant nhập thông tin: tên, giá (≥ 0), mô tả, chọn danh mục, upload ảnh.
2. Hệ thống INSERT `products` (`is_available = true` mặc định).
3. Hệ thống INSERT `media_files` (liên kết ảnh).
4. Trigger async: gửi tên + mô tả đến OpenAI Embeddings API → INSERT `product_embeddings`.
5. Sản phẩm xuất hiện trong menu nhà hàng (hiển thị với Customer).

**Luồng chính — Cập nhật sản phẩm**:

1. Merchant chọn sản phẩm cần sửa, cập nhật thông tin.
2. Hệ thống UPDATE `products`.
3. Nếu tên hoặc mô tả thay đổi: trigger async cập nhật `product_embeddings`.
4. **Lưu ý**: Giá mới chỉ ảnh hưởng đơn hàng tương lai. Đơn cũ giữ nguyên `price_at_purchase`.

**Luồng chính — Bật/Tắt còn hàng**:

1. Merchant toggle `is_available` cho sản phẩm.
2. Hệ thống UPDATE `products` set `is_available`.
3. Sản phẩm `is_available = false` ẩn khỏi menu Customer ngay lập tức.

**Luồng chính — Xóa sản phẩm**:

1. Merchant yêu cầu xóa sản phẩm.
2. Hệ thống kiểm tra: sản phẩm không có trong đơn hàng đang xử lý (`PENDING` → `READY`).
3. Nếu hợp lệ: DELETE `products` (cascade xóa `product_embeddings`).
4. Nếu đang có đơn: Thông báo _"Không thể xóa — sản phẩm đang có trong đơn hàng chưa hoàn thành."_

**Hậu điều kiện**: Sản phẩm được tạo/cập nhật/xóa. Vector embedding được đồng bộ async.

---

### UC-M05 — Xử lý đơn hàng

| Thuộc tính               | Nội dung                                                                                       |
| ------------------------ | ---------------------------------------------------------------------------------------------- |
| **Mã UC**                | UC-M05                                                                                         |
| **Tên**                  | Xử lý đơn hàng                                                                                 |
| **Actor chính**          | Merchant                                                                                       |
| **Actor phụ**            | Customer (đặt đơn), Shipper (nhận đơn), System (phân doanh thu)                                |
| **Mô tả**                | Merchant xem và xử lý đơn hàng từ lúc nhận đơn mới cho đến khi bàn giao cho Shipper.           |
| **Điều kiện tiên quyết** | •Merchant đã đăng nhập, `APPROVED` và `is_active = true`. •Có đơn hàng ở trạng thái `PENDING`. |

**Luồng chính**:

1. Merchant mở trang "Quản lý đơn hàng" — danh sách đơn PENDING/CONFIRMED/PREPARING tự cập nhật (polling 15s hoặc WebSocket).
2. Merchant chọn đơn hàng để xem chi tiết: danh sách món, số lượng, ghi chú khách, địa chỉ giao hàng.
3. Merchant nhấn **"Xác nhận"** → hệ thống UPDATE `order_status = CONFIRMED`, INSERT `order_status_history`.
4. Merchant nhấn **"Bắt đầu chuẩn bị"** → `CONFIRMED → PREPARING`, ghi lịch sử.
5. Merchant nhấn **"Sẵn sàng giao"** → `PREPARING → READY`, ghi lịch sử.
6. Hệ thống hiển thị đơn trong danh sách khả dụng cho Shipper.
7. Shipper nhận đơn → `READY → SHIPPING` (Merchant chỉ quan sát từ bước này).

**Luồng thay thế — Merchant từ chối đơn**:

- **3a. Merchant nhấn "Từ chối"** (chỉ ở trạng thái `PENDING`):
  - Merchant nhập lý do từ chối.
  - Hệ thống UPDATE `order_status = CANCELLED`, INSERT `order_status_history`.
  - Nếu Customer đã thanh toán VNPay: trigger hoàn tiền, cập nhật `payment_status = REFUNDED`.
  - Nếu có voucher: giảm `used_count`.
  - Hệ thống gửi thông báo đến Customer (WebSocket/email).

**Luồng thay thế — Customer hủy đơn**:

- Nếu Customer hủy đơn (`PENDING` hoặc `CONFIRMED`): hệ thống tự động cập nhật, Merchant nhận thông báo đơn bị hủy.

**Ngoại lệ**:

- Mất kết nối trong khi xử lý: Trạng thái không thay đổi, Merchant thử lại thủ công.

**Hậu điều kiện**: Đơn hàng ở trạng thái `READY` sau khi Merchant hoàn thành. Mọi thay đổi được ghi vào `order_status_history`.

---

### UC-M06 — Xem báo cáo doanh thu

| Thuộc tính               | Nội dung                                                        |
| ------------------------ | --------------------------------------------------------------- |
| **Mã UC**                | UC-M06                                                          |
| **Tên**                  | Xem báo cáo doanh thu                                           |
| **Actor chính**          | Merchant                                                        |
| **Mô tả**                | Merchant xem thống kê doanh thu theo khoảng thời gian tùy chọn. |
| **Điều kiện tiên quyết** | Merchant đã đăng nhập, `APPROVED`.                              |

**Luồng chính**:

1. Merchant vào trang "Báo cáo" → chọn khoảng thời gian (`from`, `to`).
2. Hệ thống truy vấn `orders` WHERE `restaurant_id` AND `order_status = COMPLETED` trong khoảng thời gian.
3. Hệ thống tính và trả về:
   - Tổng đơn / Đơn thành công / Đơn hủy
   - Tổng doanh thu (gross), tổng hoa hồng, **doanh thu ròng** (net = gross − commission)
   - Breakdown theo ngày
4. Merchant xem biểu đồ và có thể export (should have).

**Hậu điều kiện**: Merchant có dữ liệu doanh thu để đánh giá hiệu quả kinh doanh.

---

### UC-M07 — Xem đánh giá khách hàng

| Thuộc tính               | Nội dung                                                             |
| ------------------------ | -------------------------------------------------------------------- |
| **Mã UC**                | UC-M07                                                               |
| **Tên**                  | Xem đánh giá khách hàng                                              |
| **Actor chính**          | Merchant                                                             |
| **Mô tả**                | Merchant xem các đánh giá từ khách hàng sau khi đơn hàng hoàn thành. |
| **Điều kiện tiên quyết** | Merchant đã đăng nhập. Nhà hàng có ít nhất một đánh giá.             |

**Luồng chính**:

1. Merchant vào trang "Đánh giá".
2. Merchant xem danh sách đánh giá (sắp xếp theo mới nhất), có thể lọc theo điểm (1–5) và khoảng ngày.
3. Hệ thống hiển thị:
   - Đánh giá tổng thể đơn hàng (điểm + bình luận)
   - Đánh giá từng món ăn trong đơn
   - Tên khách hàng (hiển thị tên, ẩn thông tin nhạy cảm), mã đơn hàng
4. Merchant xem điểm trung bình tổng thể (`rating_avg`).

**Hậu điều kiện**: Merchant nắm được phản hồi khách hàng để cải thiện chất lượng.

---

### UC-M08 — Quản lý ví & yêu cầu rút tiền

| Thuộc tính               | Nội dung                                                                                 |
| ------------------------ | ---------------------------------------------------------------------------------------- |
| **Mã UC**                | UC-M08                                                                                   |
| **Tên**                  | Quản lý ví & yêu cầu rút tiền                                                            |
| **Actor chính**          | Merchant                                                                                 |
| **Actor phụ**            | Admin (duyệt rút tiền)                                                                   |
| **Mô tả**                | Merchant xem số dư ví, lịch sử giao dịch và gửi yêu cầu rút tiền về tài khoản ngân hàng. |
| **Điều kiện tiên quyết** | Merchant đã đăng nhập, `APPROVED`.                                                       |

**Luồng chính — Xem ví**:

1. Merchant vào trang "Ví & Thanh toán".
2. Hệ thống hiển thị: số dư hiện tại, lịch sử giao dịch (RECEIVE_PAYMENT từ đơn hàng, WITHDRAW từ rút tiền).

**Luồng chính — Quản lý phương thức rút tiền**:

1. Merchant thêm thông tin ngân hàng: tên ngân hàng, số tài khoản, chủ tài khoản, chi nhánh.
2. Hệ thống INSERT `payout_methods`.
3. Merchant có thể đặt một phương thức làm mặc định.

**Luồng chính — Yêu cầu rút tiền**:

1. Merchant nhập số tiền muốn rút và chọn phương thức thanh toán.
2. Hệ thống kiểm tra `wallet.balance >= amount` (BR-M06).
3. Hệ thống INSERT `payout_requests` (status = PENDING).
4. Hệ thống UPDATE `wallets` — tạm trừ số dư (pending deduction).
5. Merchant nhận thông báo: _"Yêu cầu đã gửi. Admin sẽ xử lý trong 1–3 ngày làm việc."_

**Luồng thay thế**:

- **2a. Số dư không đủ**: Thông báo _"Số dư ví không đủ (hiện tại: X VNĐ)."_

**Hậu điều kiện**: Yêu cầu rút tiền ở trạng thái PENDING, chờ Admin duyệt. Số dư bị tạm giữ.

---

### UC-A01 — Phê duyệt / Từ chối hồ sơ nhà hàng

| Thuộc tính               | Nội dung                                                                             |
| ------------------------ | ------------------------------------------------------------------------------------ |
| **Mã UC**                | UC-A01                                                                               |
| **Tên**                  | Phê duyệt / Từ chối hồ sơ nhà hàng                                                   |
| **Actor chính**          | Admin                                                                                |
| **Mô tả**                | Admin xem xét hồ sơ nhà hàng đang ở PENDING và ra quyết định phê duyệt hoặc từ chối. |
| **Điều kiện tiên quyết** | Có nhà hàng với `approval_status = PENDING`. Admin đã đăng nhập.                     |

**Luồng chính — Phê duyệt**:

1. Admin vào trang Admin (Thymeleaf), xem danh sách nhà hàng chờ duyệt.
2. Admin xem chi tiết: thông tin nhà hàng, thông tin chủ quán, giờ hoạt động.
3. Admin nhấn **"Phê duyệt"**.
4. Hệ thống UPDATE `restaurants` set `approval_status = APPROVED`, `is_active = true`.
5. Hệ thống gửi email thông báo kết quả đến chủ quán (JavaMail).
6. Hệ thống trigger async: tạo `restaurant_embeddings` cho nhà hàng vừa được duyệt.

**Luồng thay thế — Từ chối**:

- **3a. Admin nhấn "Từ chối"**: Nhập lý do từ chối → UPDATE `approval_status = REJECTED` → Gửi email kèm lý do đến chủ quán.

**Hậu điều kiện**: Nhà hàng `APPROVED` có thể hoạt động và hiển thị trên hệ thống. Hoặc hồ sơ `REJECTED` với lý do rõ ràng.

---

### UC-A02 — Xử lý yêu cầu rút tiền của Merchant

| Thuộc tính               | Nội dung                                                                                                               |
| ------------------------ | ---------------------------------------------------------------------------------------------------------------------- |
| **Mã UC**                | UC-A02                                                                                                                 |
| **Tên**                  | Xử lý yêu cầu rút tiền của Merchant                                                                                    |
| **Actor chính**          | Admin                                                                                                                  |
| **Mô tả**                | Admin xem xét và xử lý yêu cầu rút tiền từ Merchant. Chuyển khoản thủ công ngoài hệ thống, sau đó cập nhật trạng thái. |
| **Điều kiện tiên quyết** | Có `payout_requests` với `status = PENDING`. Admin đã đăng nhập.                                                       |

**Luồng chính**:

1. Admin vào trang Payout Management, xem danh sách yêu cầu PENDING.
2. Admin xem chi tiết: số tiền, thông tin ngân hàng, số dư ví hiện tại.
3. Admin thực hiện chuyển khoản thủ công ra ngoài hệ thống.
4. Admin nhấn **"Duyệt"** → UPDATE `payout_requests.status = APPROVED`.
5. Hệ thống UPDATE `wallets` (giảm số dư chính thức), INSERT `wallet_transactions` (type = WITHDRAW).
6. Hệ thống gửi email thông báo đến Merchant (kèm thông tin giao dịch).

**Luồng thay thế — Từ chối**:

- **4a. Admin từ chối** (kèm lý do): UPDATE `payout_status = REJECTED` → Hệ thống **hoàn trả số dư tạm giữ** về ví Merchant → Gửi email thông báo.

**Hậu điều kiện**: Yêu cầu được APPROVED hoặc REJECTED. Số dư ví Merchant được cập nhật chính xác.
