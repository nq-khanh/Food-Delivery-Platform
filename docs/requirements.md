# TÀI LIỆU PHÂN TÍCH YÊU CẦU HỆ THỐNG
## Food Delivery Platform
**Software Requirements Specification (SRS)**

| | |
|---|---|
| **Phiên bản** | 1.1 |
| **Ngày cập nhật** | Tháng 3, 2026 |
| **Trạng thái** | Hoàn chỉnh |
| **Loại tài liệu** | Capstone Project — Tài liệu Phân tích & Thiết kế |

---

## Mục lục

1. [Tổng quan dự án](#1-tổng-quan-dự-án)
2. [Actors và trách nhiệm](#2-actors-và-trách-nhiệm)
3. [Yêu cầu chức năng](#3-yêu-cầu-chức-năng)
4. [Yêu cầu phi chức năng](#4-yêu-cầu-phi-chức-năng)
5. [Quy tắc nghiệp vụ](#5-quy-tắc-nghiệp-vụ)
6. [Use Case](#6-use-case)
7. [Mô hình trạng thái đơn hàng](#7-mô-hình-trạng-thái-đơn-hàng)
8. [Kiến trúc & Công nghệ sử dụng](#8-kiến-trúc--công-nghệ-sử-dụng)

---

## 1. Tổng quan dự án

### 1.1 Mục tiêu dự án

Xây dựng nền tảng đặt và giao đồ ăn trực tuyến đa vai trò (Customer, Merchant, Shipper, Admin), cho phép vận hành đầy đủ luồng đặt đơn thực tế và quản lý vận hành cơ bản trong khuôn khổ đồ án nhóm 10 tuần.

Dự án hướng đến giải quyết bài toán phí hoa hồng cao (25–30%) của các nền tảng hiện tại như GrabFood/ShopeeFood, giúp nhà hàng nhỏ tự vận hành kênh bán hàng online với chi phí thấp hơn.

### 1.2 Phạm vi hệ thống

- Quản lý tài khoản cho 4 vai trò: Customer, Merchant, Shipper, Admin.
- Tìm kiếm món ăn, nhà hàng theo vị trí địa lý và từ khóa (hỗ trợ AI semantic search).
- Giỏ hàng, áp voucher, đặt đơn, thanh toán COD / VNPay (mock).
- Merchant quản lý menu, nhận đơn, cập nhật trạng thái chuẩn bị.
- Shipper tự nhận đơn, giao đơn, cập nhật trạng thái giao hàng.
- Admin duyệt merchant/shipper, quản lý voucher, cấu hình phí ship, báo cáo tổng quan.
- Ví hệ thống và yêu cầu rút tiền cho merchant/shipper; admin xử lý chuyển khoản thủ công.

> **Phạm vi loại trừ:** Không triển khai push notification real-time (WebSocket), không tích hợp lưu trữ file phân tán (MinIO/S3). Ứng dụng mobile cho Shipper sẽ được phát triển bổ sung sau khi hoàn thành các yêu cầu chính của đề tài.

### 1.3 Định nghĩa & Từ viết tắt

| Thuật ngữ | Ý nghĩa |
|-----------|---------|
| SRS | Software Requirements Specification — Tài liệu đặc tả yêu cầu |
| UC | Use Case — Kịch bản sử dụng |
| FR | Functional Requirement — Yêu cầu chức năng |
| NFR | Non-Functional Requirement — Yêu cầu phi chức năng |
| RBAC | Role-Based Access Control — Kiểm soát truy cập theo vai trò |
| JWT | JSON Web Token — Cơ chế xác thực stateless |
| GMV | Gross Merchandise Value — Tổng giá trị hàng hóa giao dịch |
| COD | Cash On Delivery — Thanh toán tiền mặt khi nhận hàng |
| VNPay | Cổng thanh toán trực tuyến (tích hợp mock trong phạm vi đề tài) |
| pgvector | Extension PostgreSQL hỗ trợ lưu và tìm kiếm vector (AI Semantic Search) |
| MVC | Model-View-Controller — Mô hình kiến trúc giao diện web phía server |

---

## 2. Actors và trách nhiệm

Hệ thống có 4 actor chính, kiểm soát quyền truy cập theo mô hình RBAC.

| Actor | Vai trò hệ thống | Trách nhiệm chính | Điều kiện kích hoạt |
|-------|-----------------|-------------------|---------------------|
| Customer | `CUSTOMER` | Tìm kiếm, đặt đơn, thanh toán, theo dõi & đánh giá | Tự đăng ký, xác thực email |
| Merchant | `RESTAURANT_OWNER` | Quản lý nhà hàng, menu, xử lý đơn, xem doanh thu | Đăng ký và được Admin phê duyệt |
| Shipper | `SHIPPER` | Nhận và giao đơn, theo dõi thu nhập, rút tiền | Đăng ký và được Admin phê duyệt |
| Admin | `ADMIN` | Quản trị toàn hệ thống, duyệt đối tác, cấu hình vận hành, báo cáo | Tài khoản được tạo thủ công |

---

## 3. Yêu cầu chức năng (Functional Requirements)

### 3.1 Customer

- **FR-C01:** Đăng ký tài khoản bằng email, xác thực qua email (JavaMail).
- **FR-C02:** Đăng nhập bằng email/mật khẩu, hỗ trợ quên mật khẩu qua email.
- **FR-C03:** Cập nhật thông tin cá nhân (họ tên, avatar, số điện thoại).
- **FR-C04:** Quản lý danh sách địa chỉ giao hàng, đặt địa chỉ mặc định.
- **FR-C05:** Tìm kiếm nhà hàng gần vị trí hiện tại (lat/lng, bán kính).
- **FR-C06:** Tìm kiếm món ăn theo từ khóa (hỗ trợ AI semantic search với pgvector).
- **FR-C07:** Xem menu nhà hàng theo danh mục.
- **FR-C08:** Thêm, sửa, xóa món trong giỏ hàng (giỏ hàng chỉ từ 1 nhà hàng).
- **FR-C09:** Áp mã voucher hợp lệ khi thanh toán.
- **FR-C10:** Đặt đơn hàng với phương thức thanh toán COD hoặc VNPay (mock).
- **FR-C11:** Theo dõi trạng thái đơn hàng, xem lịch sử chuyển đổi trạng thái.
- **FR-C12:** Xem thông tin liên lạc shipper khi đơn đã được nhận.
- **FR-C13:** Hủy đơn hàng theo quy tắc trạng thái (BR-02).
- **FR-C14:** Đánh giá đơn hàng và từng món ăn sau khi hoàn tất.
- **FR-C15:** Xem lịch sử đơn hàng.

### 3.2 Merchant

- **FR-M01:** Đăng ký trở thành đối tác nhà hàng từ tài khoản Customer có sẵn.
- **FR-M02:** Cập nhật thông tin nhà hàng (tên, địa chỉ, mô tả, ảnh bìa).
- **FR-M03:** Quản lý giờ hoạt động theo từng ngày trong tuần.
- **FR-M04:** CRUD danh mục (category) trong menu.
- **FR-M05:** CRUD sản phẩm/món ăn (tên, giá, mô tả, hình ảnh).
- **FR-M06:** Bật/tắt trạng thái còn hàng của từng món (`is_available`).
- **FR-M07:** Xem danh sách đơn hàng mới và xác nhận đơn.
- **FR-M08:** Cập nhật trạng thái đơn: xác nhận → đang nấu → sẵn sàng giao.
- **FR-M09:** Xem thống kê doanh thu theo ngày/tuần/tháng.
- **FR-M10:** Gửi yêu cầu rút tiền từ ví hệ thống.

### 3.3 Shipper

- **FR-S01:** Đăng ký trở thành tài xế từ tài khoản Customer có sẵn.
- **FR-S02:** Xem danh sách đơn hàng sẵn sàng giao gần vị trí hiện tại.
- **FR-S03:** Tự nhận đơn hàng (hệ thống dùng pessimistic lock tránh xung đột).
- **FR-S04:** Cập nhật trạng thái giao hàng: đã lấy hàng → đang giao → đã giao.
- **FR-S05:** Xem thông tin điểm lấy/giao, hỗ trợ mở Google Maps điều hướng.
- **FR-S06:** Xem lịch sử đơn đã giao và thống kê thu nhập.
- **FR-S07:** Gửi yêu cầu rút tiền từ ví hệ thống.

### 3.4 Admin

- **FR-A01:** Xem và duyệt / từ chối đăng ký nhà hàng mới.
- **FR-A02:** Xem và duyệt / từ chối đăng ký tài xế mới.
- **FR-A03:** Quản lý danh sách merchant và shipper đang hoạt động (CRUD).
- **FR-A04:** CRUD mã voucher giảm giá.
- **FR-A05:** Cấu hình bảng phí ship (phí cơ bản, phí theo km, lưu lịch sử thay đổi).
- **FR-A06:** Xem danh sách yêu cầu rút tiền, duyệt hoặc từ chối.
- **FR-A07:** Xem báo cáo tổng quan hệ thống (GMV, hoa hồng, số đơn, tỷ lệ hủy).

---

## 4. Yêu cầu phi chức năng (Non-Functional Requirements)

| Mã | Nhóm | Yêu cầu | Chỉ số / Ghi chú |
|----|------|---------|------------------|
| NFR-01 | Bảo mật | Xác thực bằng JWT + Refresh Token | Access token TTL ≤ 15 phút |
| NFR-02 | Bảo mật | Mật khẩu được hash trước khi lưu | BCrypt với cost factor ≥ 10 |
| NFR-03 | Bảo mật | Kiểm soát truy cập theo vai trò (RBAC) | Spring Security, mỗi endpoint gắn role |
| NFR-04 | Hiệu năng | API response time p95 < 500ms | Đo tại load bình thường |
| NFR-05 | Hiệu năng | API tìm kiếm p95 < 800ms | Bao gồm vector search pgvector |
| NFR-06 | Sẵn sàng | Uptime môi trường demo ≥ 99% | Trong giai đoạn bảo vệ đồ án |
| NFR-07 | Độ tin cậy | Mọi chuyển đổi trạng thái đơn có audit log | Bảng `order_status_history` |
| NFR-08 | Kiểm tra | Log đầy đủ hành động Admin & Payout | Lưu `user_id`, `timestamp`, `action` |
| NFR-09 | Idempotency | Callback thanh toán xử lý idempotent | Kiểm tra `vnp_txn_ref` trước khi xử lý |

---

## 5. Quy tắc nghiệp vụ (Business Rules)

| Mã | Quy tắc |
|----|---------|
| BR-01 | Shipper chỉ được nhận đơn khi đơn ở trạng thái `READY` và chưa có shipper nào nhận. |
| BR-02 | Customer được hủy đơn khi đơn đang ở trạng thái `PENDING` hoặc `CONFIRMED` (chưa có shipper). Sau đó không thể hủy. |
| BR-03 | Voucher chỉ áp dụng khi: còn thời hạn, còn lượt dùng (`used_count < usage_limit`), và giá trị đơn hàng ≥ `min_order_value`. |
| BR-04 | Yêu cầu rút tiền chỉ hợp lệ khi số dư ví ≥ số tiền yêu cầu rút. |
| BR-05 | Merchant và Shipper chỉ được hoạt động trên hệ thống sau khi Admin phê duyệt hồ sơ. |
| BR-06 | Đánh giá đơn hàng chỉ được phép thực hiện khi đơn ở trạng thái `COMPLETED`. |
| BR-07 | Mỗi đơn hàng chỉ có tối đa 1 đánh giá tổng quan; mỗi order item chỉ có 1 đánh giá. |
| BR-08 | Giỏ hàng chỉ được chứa sản phẩm từ một nhà hàng duy nhất tại một thời điểm. |
| BR-09 | Giá lưu trong `order_items` là `price_at_purchase` — snapshot tại thời điểm đặt hàng, không thay đổi khi nhà hàng cập nhật giá sau đó. |
| BR-10 | Hoa hồng nền tảng và phí ship được snapshot vào `orders` tại thời điểm checkout theo bảng cấu hình hiện hành. |
| BR-11 | Mỗi user chỉ có tối đa 1 địa chỉ giao hàng được đánh dấu là mặc định (`is_default = true`). |
| BR-12 | Phân chia doanh thu sau khi đơn `COMPLETED`: Merchant nhận `subtotal - commission_amount`; Shipper nhận phần `shipping_fee` theo tỷ lệ cấu hình. |

---

## 6. Use Case

### 6.1 Danh sách Use Case tổng hợp

| Mã UC | Actor | Tên Use Case | FR liên quan |
|-------|-------|--------------|--------------|
| UC-01 | Customer | Đăng ký tài khoản | FR-C01 |
| UC-02 | Customer | Đăng nhập hệ thống | FR-C02 |
| UC-03 | Customer | Quản lý địa chỉ giao hàng | FR-C04 |
| UC-04 | Customer | Tìm kiếm nhà hàng / món ăn | FR-C05, FR-C06 |
| UC-05 | Customer | Quản lý giỏ hàng | FR-C08 |
| UC-06 | Customer | Áp dụng voucher | FR-C09 |
| UC-07 | Customer | Đặt hàng và thanh toán | FR-C10 |
| UC-08 | Customer | Theo dõi & hủy đơn hàng | FR-C11, FR-C13 |
| UC-09 | Customer | Đánh giá đơn hàng | FR-C14 |
| UC-10 | Merchant | Đăng ký nhà hàng | FR-M01 |
| UC-11 | Merchant | Quản lý thông tin nhà hàng | FR-M02, FR-M03 |
| UC-12 | Merchant | Quản lý menu (Category & Product) | FR-M04, FR-M05, FR-M06 |
| UC-13 | Merchant | Xử lý đơn hàng | FR-M07, FR-M08 |
| UC-14 | Merchant | Xem báo cáo doanh thu | FR-M09 |
| UC-15 | Merchant | Yêu cầu rút tiền | FR-M10 |
| UC-16 | Shipper | Đăng ký tài xế | FR-S01 |
| UC-17 | Shipper | Nhận và xử lý đơn hàng | FR-S02, FR-S03, FR-S04 |
| UC-18 | Shipper | Xem thu nhập và lịch sử | FR-S06 |
| UC-19 | Shipper | Yêu cầu rút tiền | FR-S07 |
| UC-20 | Admin | Phê duyệt Merchant / Shipper | FR-A01, FR-A02 |
| UC-21 | Admin | Quản lý Voucher | FR-A04 |
| UC-22 | Admin | Cấu hình phí ship | FR-A05 |
| UC-23 | Admin | Xử lý yêu cầu rút tiền | FR-A06 |
| UC-24 | Admin | Xem báo cáo tổng quan | FR-A07 |

---

### 6.2 Đặc tả chi tiết Use Case

---

#### UC-01 — Đăng ký tài khoản

| | |
|---|---|
| **Mã Use Case** | UC-01 |
| **Tên Use Case** | Đăng ký tài khoản |
| **Actor** | Customer (chưa có tài khoản) |
| **Mô tả** | Người dùng mới tạo tài khoản Customer trên hệ thống bằng email và mật khẩu. |
| **Điều kiện tiên quyết** | Người dùng chưa có tài khoản với email này. Dịch vụ gửi email (JavaMail) hoạt động bình thường. |
| **Luồng chính** | 1. Người dùng truy cập màn hình đăng ký.<br>2. Nhập email, mật khẩu, họ tên, số điện thoại.<br>3. Hệ thống kiểm tra email và số điện thoại chưa tồn tại.<br>4. Hệ thống hash mật khẩu (BCrypt), tạo bản ghi `users`, gán role `CUSTOMER`, khởi tạo ví với `balance = 0`.<br>5. Hệ thống gửi email xác thực chứa token (JavaMail).<br>6. Người dùng click link xác thực trong email.<br>7. Hệ thống cập nhật `is_verified = true`.<br>8. Hệ thống thông báo thành công, chuyển sang màn hình đăng nhập. |
| **Luồng thay thế** | 3a. Email đã tồn tại → Thông báo "Email đã được sử dụng".<br>3b. Số điện thoại đã tồn tại → Thông báo "Số điện thoại đã được sử dụng".<br>6a. Token hết hạn → Cho phép gửi lại email xác thực (sau 60 giây). |
| **Ngoại lệ** | Dịch vụ JavaMail gặp sự cố: ghi log lỗi, thông báo người dùng thử lại sau. |
| **Hậu điều kiện** | Tài khoản được tạo với `is_verified = true`. Ví điện tử được khởi tạo với `balance = 0`. |

---

#### UC-02 — Đăng nhập hệ thống

| | |
|---|---|
| **Mã Use Case** | UC-02 |
| **Tên Use Case** | Đăng nhập hệ thống |
| **Actor** | Tất cả Actor đã có tài khoản |
| **Mô tả** | Người dùng xác thực danh tính và nhận JWT access token + refresh token để sử dụng hệ thống. |
| **Điều kiện tiên quyết** | Tài khoản đã đăng ký và xác thực email. Tài khoản không bị vô hiệu hóa (`is_active = true`). |
| **Luồng chính** | 1. Người dùng nhập email và mật khẩu.<br>2. Hệ thống truy vấn `users` theo email, kiểm tra `is_active = true`.<br>3. Hệ thống so sánh mật khẩu với `password_hash` (BCrypt verify).<br>4. Hệ thống lấy danh sách roles của người dùng.<br>5. Hệ thống tạo access token (TTL 15 phút) và refresh token.<br>6. Hệ thống lưu refresh token vào bảng `refresh_tokens`.<br>7. Trả về `access_token`, `refresh_token`, thông tin user và roles. |
| **Luồng thay thế** | 2a. Email không tồn tại → Thông báo "Email hoặc mật khẩu không đúng" (không lộ thông tin cụ thể).<br>3a. Mật khẩu sai → Thông báo "Email hoặc mật khẩu không đúng".<br>2b. `is_active = false` → Thông báo "Tài khoản đã bị vô hiệu hóa". |
| **Ngoại lệ** | — |
| **Hậu điều kiện** | Người dùng nhận được access token hợp lệ. Refresh token được lưu vào database. |

---

#### UC-04 — Tìm kiếm nhà hàng / món ăn

| | |
|---|---|
| **Mã Use Case** | UC-04 |
| **Tên Use Case** | Tìm kiếm nhà hàng / món ăn |
| **Actor** | Customer (không bắt buộc đăng nhập) |
| **Mô tả** | Customer tìm kiếm nhà hàng theo vị trí địa lý hoặc tìm kiếm món ăn theo từ khóa ngữ nghĩa (AI Semantic Search với pgvector). |
| **Điều kiện tiên quyết** | Có ít nhất một nhà hàng được phê duyệt và đang hoạt động. Dịch vụ pgvector hoạt động bình thường. |
| **Luồng chính** | 1. Customer nhập vị trí hiện tại (lat/lng) hoặc cho phép lấy GPS.<br>2. Hệ thống truy vấn `restaurants` có `approval_status = APPROVED`, `is_active = true` trong bán kính chỉ định.<br>3. Nếu có từ khóa: hệ thống tạo vector embedding, thực hiện cosine similarity search trên `restaurant_embeddings` và `product_embeddings` (HNSW index).<br>4. Hệ thống tính khoảng cách và sắp xếp kết quả theo độ liên quan + khoảng cách.<br>5. Hệ thống trả về danh sách kèm thông tin giờ mở cửa hôm nay.<br>6. Customer chọn nhà hàng để xem menu chi tiết. |
| **Luồng thay thế** | 2a. Không có nhà hàng trong bán kính → Mở rộng bán kính tự động hoặc thông báo.<br>3a. pgvector không phản hồi → Fallback sang tìm kiếm LIKE trên tên/mô tả. |
| **Ngoại lệ** | Không có kết nối GPS: Customer nhập địa chỉ thủ công. |
| **Hậu điều kiện** | Danh sách nhà hàng/món ăn phù hợp được hiển thị theo thứ tự ưu tiên. |

---

#### UC-07 — Đặt hàng và thanh toán

| | |
|---|---|
| **Mã Use Case** | UC-07 |
| **Tên Use Case** | Đặt hàng và thanh toán |
| **Actor** | Customer (đã đăng nhập) |
| **Mô tả** | Customer hoàn tất đặt hàng từ giỏ hàng, chọn phương thức thanh toán COD hoặc VNPay (mock) và tạo đơn hàng trong hệ thống. |
| **Điều kiện tiên quyết** | Customer đã đăng nhập và có ít nhất 1 sản phẩm trong giỏ hàng. Customer đã chọn địa chỉ giao hàng hợp lệ. Nhà hàng đang trong giờ hoạt động. |
| **Luồng chính** | 1. Customer xem tóm tắt giỏ hàng và chọn địa chỉ giao hàng.<br>2. Customer áp mã voucher (nếu có): hệ thống kiểm tra hợp lệ và tính `discount_amount`.<br>3. Customer chọn phương thức thanh toán (COD hoặc VNPay mock) và xác nhận.<br>4. Hệ thống lấy giá hiện tại sản phẩm, tải `shipping_config` và `commission_rate` hiện hành.<br>5. Hệ thống tính: `subtotal`, `shipping_fee`, `discount_amount`, `total_amount`, `commission_amount`.<br>6. Hệ thống mở DB transaction: INSERT `orders` (snapshot), INSERT `order_items` (lưu `price_at_purchase`), INSERT `order_status_history` (PENDING), UPDATE `vouchers` (tăng `used_count`).<br>7. Nếu VNPay mock: hệ thống tạo URL thanh toán giả, redirect Customer.<br>8. VNPay callback: hệ thống xác thực, cập nhật `payment_status = PAID` (idempotent theo `vnp_txn_ref`).<br>9. Nếu COD: `order_status = PENDING`, `payment_status = UNPAID`.<br>10. Merchant nhận thông báo về đơn hàng mới (polling hoặc refresh thủ công). |
| **Luồng thay thế** | 2a. Voucher không hợp lệ → Thông báo lý do cụ thể (hết hạn/hết lượt/chưa đủ giá trị).<br>8a. VNPay trả về lỗi → Đơn ở trạng thái PENDING/UNPAID, cho phép thử lại.<br>6a. DB transaction thất bại → Rollback toàn bộ, trả lỗi cho Customer. |
| **Ngoại lệ** | `Idempotency-Key` trùng lặp: trả về kết quả lần xử lý đầu tiên, không tạo đơn mới. |
| **Hậu điều kiện** | Đơn hàng được tạo với `order_status = PENDING`. Nếu VNPay: `payment_status = PAID` sau callback thành công. Merchant nhận thông báo đơn mới. |

---

#### UC-08 — Theo dõi và hủy đơn hàng

| | |
|---|---|
| **Mã Use Case** | UC-08 |
| **Tên Use Case** | Theo dõi và hủy đơn hàng |
| **Actor** | Customer (đã đăng nhập) |
| **Mô tả** | Customer xem trạng thái đơn hàng và có thể hủy đơn theo quy tắc nghiệp vụ. |
| **Điều kiện tiên quyết** | Customer có ít nhất 1 đơn hàng đang xử lý. |
| **Luồng chính** | 1. Customer truy cập danh sách đơn hàng, chọn đơn cần xem.<br>2. Hệ thống hiển thị: trạng thái hiện tại, lịch sử trạng thái, thông tin shipper (nếu có).<br>3. Customer chọn "Hủy đơn" và nhập lý do.<br>4. Hệ thống kiểm tra `order_status IN (PENDING, CONFIRMED)` và chưa có shipper.<br>5. Hệ thống cập nhật `order_status = CANCELLED`, `cancelled_at = NOW()`, INSERT `order_status_history`.<br>6. Nếu đã thanh toán VNPay: cập nhật `payment_status = REFUNDED`, ghi nhận để xử lý hoàn tiền.<br>7. Nếu có voucher: giảm `used_count`.<br>8. Merchant nhận thông báo hủy đơn. |
| **Luồng thay thế** | 4a. `order_status = SHIPPING` hoặc `COMPLETED` → Từ chối, thông báo "Không thể hủy đơn đang giao hoặc đã hoàn thành". |
| **Ngoại lệ** | — |
| **Hậu điều kiện** | Đơn hàng ở trạng thái `CANCELLED`. Hoàn tiền được ghi nhận nếu đã thanh toán. |

---

#### UC-09 — Đánh giá đơn hàng

| | |
|---|---|
| **Mã Use Case** | UC-09 |
| **Tên Use Case** | Đánh giá đơn hàng |
| **Actor** | Customer (đã đăng nhập) |
| **Mô tả** | Customer đánh giá tổng thể đơn hàng và từng món ăn sau khi đơn hoàn thành. |
| **Điều kiện tiên quyết** | Đơn hàng ở trạng thái `COMPLETED`. Chưa có đánh giá nào cho đơn hàng này. |
| **Luồng chính** | 1. Customer chọn đơn đã hoàn thành, nhấn "Đánh giá".<br>2. Customer nhập điểm tổng (1–5 sao) và nhận xét.<br>3. Customer đánh giá từng món ăn (điểm 1–5, nhận xét tùy chọn).<br>4. Customer xác nhận gửi đánh giá.<br>5. Hệ thống INSERT `order_reviews` (unique theo `order_id`).<br>6. Hệ thống INSERT `order_item_reviews` cho từng món.<br>7. Hệ thống tính lại: `UPDATE restaurants SET rating_avg = AVG(rating)`. |
| **Luồng thay thế** | 1a. Đơn chưa `COMPLETED` → Ẩn nút đánh giá.<br>5a. Đánh giá đã tồn tại → Thông báo "Bạn đã đánh giá đơn hàng này". |
| **Ngoại lệ** | — |
| **Hậu điều kiện** | Đánh giá được lưu. `rating_avg` của nhà hàng được cập nhật. |

---

#### UC-13 — Xử lý đơn hàng (Merchant)

| | |
|---|---|
| **Mã Use Case** | UC-13 |
| **Tên Use Case** | Xử lý đơn hàng |
| **Actor** | Merchant (đã đăng nhập, được Admin duyệt) |
| **Mô tả** | Merchant xác nhận đơn mới và cập nhật trạng thái chuẩn bị cho đến khi sẵn sàng giao cho Shipper. |
| **Điều kiện tiên quyết** | Nhà hàng có đơn hàng ở trạng thái `PENDING`. Nhà hàng đang trong giờ hoạt động. |
| **Luồng chính** | 1. Merchant vào trang quản lý, xem danh sách đơn mới (polling/refresh thủ công).<br>2. Merchant xem chi tiết: danh sách món, địa chỉ giao, ghi chú khách.<br>3. Merchant nhấn "Xác nhận" → `order_status = CONFIRMED`.<br>4. Merchant bắt đầu chuẩn bị → "Đang nấu" → `order_status = PREPARING`.<br>5. Hoàn thành → "Sẵn sàng giao" → `order_status = READY`.<br>6. Shipper có thể thấy đơn trong danh sách khả dụng. |
| **Luồng thay thế** | 3a. Merchant không thể thực hiện → "Từ chối" kèm lý do → `order_status = CANCELLED`, Customer được ghi nhận hoàn tiền. |
| **Ngoại lệ** | — |
| **Hậu điều kiện** | Đơn hàng ở trạng thái `READY`, sẵn sàng cho Shipper nhận. |

---

#### UC-17 — Nhận và xử lý đơn hàng (Shipper)

| | |
|---|---|
| **Mã Use Case** | UC-17 |
| **Tên Use Case** | Nhận và xử lý đơn hàng |
| **Actor** | Shipper (đã đăng nhập, được Admin duyệt) |
| **Mô tả** | Shipper xem danh sách đơn cần giao, tự nhận đơn, lấy hàng và giao đến khách hàng. |
| **Điều kiện tiên quyết** | Có ít nhất 1 đơn ở trạng thái `READY` trong khu vực. |
| **Luồng chính** | 1. Shipper mở app mobile, xem danh sách đơn khả dụng gần vị trí hiện tại.<br>2. Shipper chọn đơn và nhấn "Nhận đơn".<br>3. Hệ thống thực hiện pessimistic lock (SELECT FOR UPDATE): kiểm tra đơn chưa có shipper.<br>4. Hệ thống gán `shipper_id`, cập nhật `order_status = SHIPPING`, `is_busy = true`.<br>5. Shipper di chuyển đến nhà hàng (có thể mở Google Maps điều hướng).<br>6. Shipper nhấn "Đã lấy hàng" → `order_status = DELIVERING`.<br>7. Shipper giao hàng, nhấn "Đã giao" → `order_status = COMPLETED`.<br>8. Hệ thống phân chia doanh thu: cộng ví Merchant và Shipper, INSERT `wallet_transactions`. |
| **Luồng thay thế** | 3a. Đơn đã có shipper khác nhận → Thông báo "Đơn hàng đã được nhận bởi shipper khác". |
| **Ngoại lệ** | Shipper không liên lạc được khách: ghi chú, báo cáo Admin xử lý. |
| **Hậu điều kiện** | Đơn hàng `COMPLETED`. Ví Merchant và Shipper được cộng tiền. `is_busy = false`. |

---

#### UC-20 — Phê duyệt Merchant / Shipper

| | |
|---|---|
| **Mã Use Case** | UC-20 |
| **Tên Use Case** | Phê duyệt Merchant / Shipper |
| **Actor** | Admin |
| **Mô tả** | Admin xem xét hồ sơ đăng ký của Merchant và Shipper, phê duyệt hoặc từ chối kèm lý do. |
| **Điều kiện tiên quyết** | Có hồ sơ Merchant hoặc Shipper ở trạng thái `PENDING`. |
| **Luồng chính** | 1. Admin truy cập trang Admin (Spring MVC + Thymeleaf), xem danh sách hồ sơ chờ duyệt.<br>2. Admin xem chi tiết hồ sơ: thông tin nhà hàng/tài xế, biển số xe, mô tả.<br>3. Admin nhấn "Phê duyệt" → `approval_status = APPROVED`, `is_active = true`.<br>4. Hệ thống kích hoạt role `RESTAURANT_OWNER` hoặc `SHIPPER` cho user.<br>5. Hệ thống gửi email thông báo kết quả (JavaMail).<br>6. Trigger async: tạo vector embedding cho nhà hàng mới được duyệt. |
| **Luồng thay thế** | 3a. Admin chọn "Từ chối", nhập lý do → `approval_status = REJECTED`. Email kèm lý do gửi cho đối tác. |
| **Ngoại lệ** | — |
| **Hậu điều kiện** | Merchant/Shipper được cấp quyền hoạt động hoặc nhận thông báo từ chối qua email. |

---

#### UC-23 — Xử lý yêu cầu rút tiền

| | |
|---|---|
| **Mã Use Case** | UC-23 |
| **Tên Use Case** | Xử lý yêu cầu rút tiền |
| **Actor** | Admin |
| **Mô tả** | Admin xem xét và xử lý yêu cầu rút tiền từ ví của Merchant/Shipper, thực hiện chuyển khoản thủ công ngoài hệ thống. |
| **Điều kiện tiên quyết** | Có yêu cầu `payout_request` ở trạng thái `PENDING`. Số dư ví ≥ số tiền yêu cầu (đã kiểm tra khi tạo yêu cầu). |
| **Luồng chính** | 1. Admin vào trang quản lý payout, xem danh sách yêu cầu đang chờ.<br>2. Admin xem chi tiết: thông tin ngân hàng, số tiền, số dư ví.<br>3. Admin thực hiện chuyển khoản thủ công ra ngoài hệ thống.<br>4. Admin nhấn "Duyệt" → `payout_status = APPROVED`.<br>5. Hệ thống UPDATE `wallets` (trừ số dư), INSERT `wallet_transactions` (type = WITHDRAW).<br>6. Hệ thống gửi email thông báo kết quả (JavaMail). |
| **Luồng thay thế** | 4a. Admin nhấn "Từ chối" kèm lý do → `payout_status = REJECTED`. Hệ thống hoàn trả số dư bị tạm giữ về ví. |
| **Ngoại lệ** | — |
| **Hậu điều kiện** | Yêu cầu được xử lý (APPROVED/REJECTED). Số dư ví được cập nhật chính xác. |

---

## 7. Mô hình trạng thái đơn hàng (Order State Machine)

Mọi chuyển đổi trạng thái đều được ghi lại trong bảng `order_status_history` (audit log).

### 7.1 Các trạng thái và ý nghĩa

| Trạng thái | Người thực hiện | Ý nghĩa |
|------------|----------------|---------|
| `PENDING` | System (khi checkout) | Đơn hàng vừa tạo, chờ Merchant xác nhận |
| `CONFIRMED` | Merchant | Merchant đã xác nhận, chuẩn bị bắt đầu |
| `PREPARING` | Merchant | Đang chuẩn bị món ăn |
| `READY` | Merchant | Món ăn sẵn sàng, chờ Shipper đến lấy |
| `SHIPPING` | Shipper | Shipper đã nhận đơn, di chuyển đến nhà hàng |
| `DELIVERING` | Shipper | Shipper đã lấy hàng, đang trên đường giao |
| `COMPLETED` | Shipper | Giao hàng thành công, đơn kết thúc |
| `CANCELLED` | Customer / Merchant / System | Đơn bị hủy |

### 7.2 Luồng chuyển đổi trạng thái hợp lệ

| Từ trạng thái | Sang trạng thái | Điều kiện / Actor |
|--------------|----------------|------------------|
| `PENDING` | `CONFIRMED` | Merchant xác nhận đơn |
| `PENDING` | `CANCELLED` | Customer hủy / Merchant từ chối |
| `CONFIRMED` | `PREPARING` | Merchant bắt đầu nấu |
| `CONFIRMED` | `CANCELLED` | Customer hủy (khi chưa có shipper) |
| `PREPARING` | `READY` | Merchant hoàn thành chuẩn bị |
| `READY` | `SHIPPING` | Shipper nhận đơn (kiểm tra chưa có shipper — pessimistic lock) |
| `SHIPPING` | `DELIVERING` | Shipper xác nhận đã lấy hàng tại nhà hàng |
| `DELIVERING` | `COMPLETED` | Shipper xác nhận giao thành công |

---

## 8. Kiến trúc & Công nghệ sử dụng

### 8.1 Yêu cầu công nghệ chung

| STT | Thành phần | Công nghệ |
|-----|-----------|-----------|
| 1 | Backend | Spring Boot (Java 17+) |
| 2 | Database | PostgreSQL |
| 3 | Frontend — Admin | Spring MVC + Thymeleaf (Server-side Rendering) |
| 4 | Frontend — Customer & Merchant | React JS (SPA) |
| 5 | Frontend — Shipper | Mobile App (React Native / Flutter) — phát triển bổ sung sau |
| 6 | API | RESTful API |
| 7 | Security | Spring Security (JWT Authentication & RBAC Authorization) |
| 8 | Version Control | Git + GitHub |
| 9 | Documentation | README, API docs (Swagger/OpenAPI), Database schema |

### 8.2 Yêu cầu công nghệ bổ sung theo tính năng

| Tính năng | Công nghệ | Mô tả áp dụng |
|-----------|-----------|---------------|
| AI Semantic Search | pgvector (PostgreSQL extension) + OpenAI Embeddings API | Tìm kiếm nhà hàng, món ăn theo ngữ nghĩa |
| Thanh toán | VNPay (mock) | Mô phỏng luồng thanh toán online |
| Email | JavaMail (Spring Mail) | Xác thực tài khoản, thông báo duyệt hồ sơ, kết quả payout |
| Lưu trữ file / ảnh | MinIO (Object Storage) | Upload ảnh sản phẩm, nhà hàng, avatar người dùng |
| Realtime tracking | Spring WebSocket (STOMP) | Theo dõi trạng thái đơn hàng real-time phía Customer & Merchant |

> **Lưu ý về phạm vi kỹ thuật:**
> - **Admin Panel** dùng Spring MVC + Thymeleaf (server-side rendering), phù hợp với các tác vụ quản trị nội bộ ít tương tác.
> - **Customer & Merchant** dùng React JS (SPA) để đảm bảo trải nghiệm người dùng mượt mà, hỗ trợ real-time update qua WebSocket.
> - **MinIO** được triển khai local (self-hosted) để lưu trữ ảnh, tương thích API với S3, dễ nâng cấp sau này.
> - **WebSocket (STOMP)** chỉ áp dụng cho luồng theo dõi trạng thái đơn hàng. Các tính năng còn lại dùng REST API thông thường.
> - **Trang Shipper** không nằm trong yêu cầu bắt buộc của đề tài. Sẽ được phát triển dưới dạng mobile app (React Native hoặc Flutter) sau khi hoàn thành đầy đủ các yêu cầu chính.

### 8.3 Kiến trúc tổng thể

```
┌──────────────────────────────────────────────────────────────────┐
│                         Client Layer                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │  Thymeleaf MVC  │  │  React JS (SPA) │  │   Mobile App    │  │
│  │  Admin Panel    │  │Customer/Merchant│  │  Shipper App    │  │
│  │(server-rendered)│  │  (REST + WS)    │  │ (sau khi xong) │  │
│  └────────┬────────┘  └───────┬─────────┘  └──────┬──────────┘  │
└───────────┼───────────────────┼────────────────────┼────────────┘
            │  HTTP/MVC         │  REST API           │  REST API
            │                  │  WebSocket (STOMP)  │
┌───────────▼───────────────────▼────────────────────▼────────────┐
│                        Backend Layer                             │
│                   Spring Boot (Java 17+)                        │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────────┐ │
│  │ MVC Controllers│  │REST Controllers│  │  WebSocket Handler   │ │
│  │ (Thymeleaf)  │  │  (JSON API)  │  │  (STOMP — order track) │ │
│  └──────────────┘  └──────┬───────┘  └────────────────────────┘ │
│                           │                                      │
│  ┌──────────────┐  ┌──────▼───────┐  ┌───────────┐             │
│  │Spring Security│  │   Services   │  │ JavaMail  │             │
│  │  JWT + RBAC  │  │(Business     │  │  (Email)  │             │
│  └──────────────┘  │  Logic)      │  └───────────┘             │
│                    └──────┬───────┘                             │
│                    ┌──────▼───────┐                             │
│                    │ Repositories │                             │
│                    │ (Spring Data │                             │
│                    │    JPA)      │                             │
│                    └──────┬───────┘                             │
└───────────────────────────┼─────────────────────────────────────┘
                            │
          ┌─────────────────┴──────────────────┐
          │                                    │
┌─────────▼──────────────────┐  ┌─────────────▼──────────────────┐
│      Database Layer         │  │       Storage Layer            │
│      PostgreSQL 12+         │  │       MinIO (Local)            │
│                             │  │                                │
│  ┌──────────────────────┐   │  │  ┌──────────────────────────┐ │
│  │  Relational Tables   │   │  │  │  Buckets:                │ │
│  │  (3NF Schema)        │   │  │  │  - products/             │ │
│  └──────────────────────┘   │  │  │  - restaurants/          │ │
│  ┌──────────────────────┐   │  │  │  - avatars/              │ │
│  │  pgvector Extension  │   │  │  └──────────────────────────┘ │
│  │  (AI Semantic Search)│   │  │                                │
│  └──────────────────────┘   │  └────────────────────────────────┘
└─────────────────────────────┘
```

---
