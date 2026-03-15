# Tài liệu Đặc tả Chức năng và Use Case: Vai trò Customer

Tài liệu này tổng hợp toàn bộ Yêu cầu chức năng, Phi chức năng, Danh sách Use Case và Sơ đồ hoạt động dành riêng cho **Customer** trên Nền tảng Food Delivery.

---

## 1. Yêu cầu chức năng (Functional Requirements - FR)

| Mã | Tên chức năng | Mô tả chi tiết |
|---|---|---|
| **FR-C01** | Đăng ký tài khoản | Đăng ký bằng Email, Mật khẩu, Họ tên và Số điện thoại (Bắt buộc). Xác thực qua link kích hoạt/OTP gửi tới email (JavaMail). |
| **FR-C02** | Đăng nhập hệ thống | Đăng nhập bằng Email và Password, nhận JWT Access Token và Refresh Token. Cấp quyền role `CUSTOMER`. |
| **FR-C03** | Quản lý Hồ sơ | Cập nhật Avatar, Họ tên, Số điện thoại (Rất quan trọng để Shipper liên lạc dự phòng). |
| **FR-C04** | Quản lý Sổ địa chỉ | Xem, Thêm mới, Chỉnh sửa, Xóa (soft-delete) địa chỉ nhận hàng. Đánh dấu 1 địa chỉ làm `is_default`. Lưu tọa độ lat/lng từ Google Maps API. |
| **FR-C05** | Khám phá & Gợi ý | Hiển thị danh sách Gợi ý quán ngon, Quán đang khuyến mãi tại trang chủ. |
| **FR-C06** | Tìm kiếm Nhà hàng/Món ăn | Hỗ trợ AI Semantic Search (qua pgvector). Cho phép lấy GPS thiết bị hoặc **chọn 1 địa chỉ giao hàng bất kỳ** làm mốc tìm kiếm trong bán kính. Kết hợp filter (Khoảng cách, Đánh giá, Đang mở). |
| **FR-C07** | Xem Menu nhà hàng | Phân chia thực đơn theo Category, trạng thái món (`is_available`). |
| **FR-C08** | Quản lý Giỏ hàng | Thêm món ăn kèm Option/Topping (Size L, thêm đá,...). Cảnh báo và yêu cầu xóa giỏ cũ nếu thêm món của nhà hàng khác (chỉ cho phép 1 nhà hàng trong giỏ). |
| **FR-C09** | Quản lý Ví Voucher | Xem danh sách Voucher từ nền tảng/nhà hàng. Hiển thị điều kiện áp dụng (`min_order_value`). Cho phép áp dụng hoặc nhập tay mã mới. |
| **FR-C10** | Đặt hàng & Thanh toán | **[Có Pre-checkout Validation]** Xác thực lại giá và số lượng trước khi lên đơn. Hỗ trợ ghi chú cho Shipper/Quán. Thanh toán COD hoặc VNPay (cơ chế Idempotent). |
| **FR-C11** | Theo dõi Đơn hàng | Realtime tracking qua WebSocket các trạng thái: Pending, Confirmed, Preparing, Ready, Shipping, Delivering. |
| **FR-C12** | Liên lạc Shipper | Hiển thị thông tin Shipper (tên, SĐT, biển số xe) khi đơn sang trạng thái `SHIPPING`. |
| **FR-C13** | Hủy Đơn hàng | Khách được tự hủy khi đơn đang `PENDING` hoặc `CONFIRMED`. Sẽ xử lý Refund (VNPay) hoặc hoàn về tài khoản. |
| **FR-C14** | Lịch sử Đơn hàng | Liệt kê các đơn đã hoàn thành/hủy. Mua lại đơn cũ. |
| **FR-C15** | Đánh giá Đơn hàng | Đánh giá sao (1-5) và text cho Nhà hàng, Món ăn. Hỗ trợ **upload tối đa 3 hình ảnh/video** lên MinIO. |

---

## 2. Yêu cầu phi chức năng (NFR) ảnh hưởng đến Customer

1. **Bảo mật và Xác thực (Security):** Mật khẩu khách hàng phải được băm (hash) BCrypt. Session quản lý qua chuẩn JWT Stateless với TTL của Access Token nhỏ (15 phút) nhằm giảm rủi ro chiếm đoạt token.
2. **Hiệu năng tìm kiếm (Performance):** Tốc độ API Search ## 3. Bản Đặc tả Chi tiết các Use Case (UC)

Dựa trên tài liệu SRS, dưới đây là bản đặc tả chi tiết tập Use Case cốt lõi dành cho **Customer**, bao gồm đầy đủ luồng sự kiện chính và ngoại lệ:

### UC-01: Đăng ký tài khoản (FR-C01)
| Thuộc tính | Chi tiết |
|---|---|
| **Tác nhân** | Customer (chưa có tài khoản) |
| **Mô tả** | Người dùng mới tạo tài khoản Customer trên hệ thống bằng email và mật khẩu. |
| **Tiền điều kiện** | Người dùng chưa có tài khoản với email này. Dịch vụ gửi email (JavaMail) hoạt động bình thường. |
| **Hậu điều kiện** | Tài khoản được tạo với `is_verified = true`. Ví điện tử được khởi tạo với `balance = 0`. |
| **Luồng chính** | 1. Người dùng truy cập màn hình đăng ký.<br>2. Nhập email, mật khẩu, họ tên, số điện thoại.<br>3. Hệ thống kiểm tra khoảng trắng, tính hợp lệ. Đảm bảo email và số điện thoại chưa tồn tại.<br>4. Hệ thống hash mật khẩu (BCrypt), tạo bản ghi `users`, gán role `CUSTOMER`, khởi tạo ví với `balance = 0`.<br>5. Hệ thống gửi email xác thực chứa token (qua JavaMail).<br>6. Người dùng click link xác thực trong email.<br>7. Hệ thống cập nhật trạng thái hoạt động `is_verified = true`.<br>8. Hệ thống thông báo thành công, chuyển sang màn hình đăng nhập. |
| **Ngoại lệ** | - *E1 - Email đã tồn tại:* Hệ thống thông báo "Email đã được sử dụng".<br>- *E2 - Số điện thoại đã tồn tại:* Hệ thống thông báo "Số điện thoại đã được sử dụng".<br>- *E3 - Token xác thực hết hạn:* Cho phép gửi lại email xác thực (sau 60 giây).<br>- *E4 - Lỗi gửi mail (JavaMail server error):* Hệ thống vẫn tạo user cờ `is_verified = false`, gửi log lỗi và thông báo người dùng nên thử lại thao tác nhận email sau. |

### UC-02: Đăng nhập hệ thống (FR-C02)
| Thuộc tính | Chi tiết |
|---|---|
| **Tác nhân** | Customer (và tất cả Actor đã có tài khoản) |
| **Mô tả** | Người dùng xác thực danh tính và nhận JWT access token + refresh token để sử dụng hệ thống. |
| **Tiền điều kiện** | Tài khoản đã đăng ký và xác thực email. Tài khoản không bị vô hiệu hóa (`is_active = true`). |
| **Hậu điều kiện** | Người dùng nhận được access token hợp lệ. Refresh token được lưu vào database. |
| **Luồng chính** | 1. Người dùng nhập email và mật khẩu.<br>2. Hệ thống truy vấn `users` theo email, kiểm tra `is_active = true`.<br>3. Hệ thống so sánh mật khẩu với `password_hash` (BCrypt verify).<br>4. Hệ thống lấy danh sách roles của người dùng.<br>5. Hệ thống tạo access token (TTL 15 phút) và refresh token.<br>6. Hệ thống lưu refresh token vào bảng `refresh_tokens`.<br>7. Trả về `access_token`, `refresh_token`, thông tin user và roles. Cho phép truy cập vào Trang chủ. |
| **Ngoại lệ** | - *E1 - Email không tồn tại:* Thông báo "Email hoặc mật khẩu không đúng" (không báo rõ là chưa có email để tránh rò rỉ thông tin).<br>- *E2 - Sai Mật khẩu:* Thông báo "Email hoặc mật khẩu không đúng".<br>- *E3 - Tài khoản bị khóa:* (`is_active = false`) Thông báo "Tài khoản đã bị vô hiệu hóa". |

### UC-03: Tìm kiếm nhà hàng / món ăn (FR-C05, FR-C06)
| Thuộc tính | Chi tiết |
|---|---|
| **Tác nhân** | Customer (không bắt buộc đăng nhập) |
| **Mô tả** | Customer tìm kiếm nhà hàng theo vị trí địa lý hoặc tìm kiếm món ăn theo từ khóa ngữ nghĩa (AI Semantic Search với pgvector). |
| **Tiền điều kiện** | Có ít nhất một nhà hàng được approval trên Database và đang hoạt động. Dịch vụ pgvector db extension chạy bình thường. |
| **Hậu điều kiện** | Hiển thị danh sách kết quả phù hợp, được xếp hạng theo thứ tự ưu tiên nhất. |
| **Luồng chính** | 1. Customer nhập định vị hiện tại (lat/lng) hoặc lấy từ cấp phép GPS ứng dụng.<br>2. Hệ thống truy vấn `restaurants` có `approval_status = APPROVED`, `is_active = true` trong bán kính chỉ định quanh mốc đó.<br>3. Nếu ngời dùng *không nhập từ khóa*: Trả về danh sách quán ngon theo thứ tự khoảng cách.<br>4. Nếu ngời dùng *có từ khóa* (VD: "Món nướng đêm"): Hệ thống nhúng từ khóa thành vector embedding, thực hiện phép tìm kiếm cosine similarity trên `restaurant_embeddings` và `product_embeddings` (HNSW index).<br>5. Hệ thống tính khoảng cách và sắp xếp kết quả hỗn hợp dựa vào *Độ liên quan (Similarity) + Khoảng cách*.<br>6. Trả về danh sách UI kèm theo thông tin như giờ mở cửa trong hôm nay, khoảng cách.<br>7. Customer nhấp chọn nhà hàng mong muốn để xem menu chi tiết. |
| **Ngoại lệ** | - *E1 - Không có nhà hàng bán kính:* Tự động nới lỏng rộng phạm vi bán kính hệ thống, hoặc nếu vẫn trống sẽ thông báo "Không tìm thấy quán quanh bạn".<br>- *E2 - Module AI Error (pgvector không phản hồi):* Hệ thống tự Fallback xuống truy vấn text Full-text Search hoặc `LIKE` trên cột Tên/Mô tả. |

### UC-04: Quản lý giỏ hàng (FR-C08)
| Thuộc tính | Chi tiết |
|---|---|
| **Tác nhân** | Customer |
| **Mô tả** | Khách hàng lựa chọn và thiết lập số lượng các món ăn muốn đặt của 1 nhà hàng. |
| **Tiền điều kiện** | Món ăn đó phải nằm trong Menu hiện còn trạng thái kinh doanh (`is_available = true`). |
| **Hậu điều kiện** | Lưu trạng thái Giỏ hàng trong DB hoặc Local/Session của User. |
| **Luồng chính** | 1. Customer duyệt Menu cửa hàng `A`, tiến hành "Thêm vào giỏ" đối với món `X`.<br>2. Chọn tùy chỉnh số lượng / Size.<br>3. Lưu trạng thái Giỏ hàng, tính tổng tiền. |
| **Quy tắc (BR-08)** | Môi trường giỏ hàng chỉ nhận duy nhất 1 Quán tại một thời điểm. Nếu Customer qua nhà hàng `B` thêm món, hệ thống tung Alert xác nhận phải Xóa sạch giỏ hiện tại (Quán `A`) thì mới thêm món của Quán `B` được. |

### UC-05: Đặt hàng và Thanh toán (FR-C10)
| Thuộc tính | Chi tiết |
|---|---|
| **Tác nhân** | Customer (đã đăng nhập) |
| **Mô tả** | Quá trình lập đơn đặt hàng, sử dụng mã voucher và thanh toán qua hình thức COD/VNPay (bản mock) và sinh hóa đơn hệ thống. |
| **Tiền điều kiện** | Đã đăng nhập, có sản phẩm hợp lệ trong giỏ. Chọn địa chỉ giao hàng hợp quy. Quán ăn đang trong thời gian mở cửa tiếp nhận đơn. |
| **Hậu điều kiện** | Hóa đơn ghi chú Database được tạo với `order_status = PENDING`. |
| **Luồng chính** | 1. Customer mở giỏ hàng kiểm tra danh mục và gán một địa chỉ giao hàng.<br>2. Áp dụng mã Giảm giá (Voucher) hợp lệ (Ví dụ: tổng tiền >= `min_order_value`). Hệ thống tính toán % khấu trừ.<br>3. Chọn phương thức trả tiền (COD mặt / VNPay Bank Transfer). Xác nhận nhấp "Đặt hàng".<br>4. Hệ thống quét snapshot lấy thông số giá mới nhất của thực đơn, tải lên `shipping_config` và `commission_rate` hiện hành trên hệ thống (Luồng chống vọt giá).<br>5. Hệ thống tính chi phí cuối: Subtotal, Shipping fee, Discount lượng tiền, Commission lượng tiền, và Tổng phải thanh toán Total amount.<br>6. Mở Database Transaction nguyên tử: `INSERT orders` (snapshot). `INSERT order_items` lưu `price_at_purchase`. `INSERT order_status_history` đánh dấu `PENDING`. `UPDATE vouchers` (tăng số `used_count` để tính giới hạn).<br>7. **Nếu là Mock Bank (VNPay):** Sinh Payment Gateway URL -> Redirect sang Gateway VNPay Mock. Nhập thẻ và thao tác Webhook hook về -> Callback xác minh `vnp_txn_ref` cập nhật trạng thái tiền `payment_status = PAID`.<br>8. **Nếu là COD:** Bỏ qua Gateway, nhận luôn đơn về ghi `payment_status = UNPAID` và trạng thái `PENDING`.<br>9. Merchant nhận notification WebSocket báo có biến Order mới. |
| **Ngoại lệ** | - *E1 - Voucher quá hạn / Kháng điều kiện (hết luợt, đơn nhỏ hơn mức min):* Quăng Alert thông báo tại bước 2.<br>- *E2 - VNPay thanh toán thất bại / Hủy giữa chừng:* Callback báo lỗi đổ về. Đơn Order nhảy vào trạng thái lửng `UNPAID` chực chờ Khách tái bấm.<br>- *E3 - Lỗi Transaction (Mất network khúc quan trọng):* Rollback toàn bộ trạng thái DB. Báo Failed về màn hình GUI.<br>- *Idempotency Check:* Nếu Khách ấn Pay liên tục, Gateway kiểm tra trùng lặp `Idempotency-Key` để tránh làm 2 đơn kép ảo. |

### UC-06: Theo dõi và Hủy đơn hàng (FR-C11, FR-C13)
| Thuộc tính | Chi tiết |
|---|---|
| **Tác nhân** | Customer (đã đăng nhập) |
| **Mô tả** | Customer nắm bắt trạng thái tiến trình đơn đồ ăn, hỗ trợ thao tác thu hồi hủy nếu chưa vượt mốc thời gian. |
| **Tiền điều kiện** | Cần có tối thiểu 1 order đang được xử lý tiếp nhận (`Active`). |
| **Hậu điều kiện** | Đơn hàng được cập nhật tình trạng Status và xử lý dòng tiền (nếu cần). |
| **Luồng Theo dõi** | 1. Truy cập App/Danh mục Đơn hàng.<br>2. Giao diện tải Realtime (WebSocket): Lịch sử timeline Status (Từ `PENDING` -> `CONFIRMED` -> `PREPARING` -> `READY` -> `SHIPPING`).<br>3. Thông tin anh/chị Shipper (Tên tuổi, SĐT đổ về để liên lạc gọi điện hỏi thăm) khi đạt mốc `SHIPPING`. |
| **Luồng Hủy Đơn** | 1. Customer bấm nút "Hủy đơn", hệ thống nảy popup cho điền Text Lý do hủy.<br>2. Bộ check (Check logic nghiệp vụ): Đơn hàng **chỉ được phép Hủy khi Status mới chỉ là `PENDING` hoặc `CONFIRMED`** và chưa có dấu Shipper rờ tay vào.<br>3. Bypass hệ thống cập nhật Status `= CANCELLED`. Ghi vết mốc thời gian bị hủy.<br>4. Nếu khách hàng trót quẹt tiền (VNPay) trả thẻ -> Chuyển thanh toán báo `REFUNDED` báo cho luồng Hoàn tiền Refund.<br>5. Nếu khách hàng dùng Voucher -> Hạ số `used_count` để tái sử dụng lượt.<br>6. Notification Push Web cho Quán (Merchant) ngọc dẹp giỏ / không làm món nữa. |
| **Ngoại lệ** | - *E1 - Bấm hủy khi Nấu rồi (Status muộn như PREPARING/SHIPPING):* Reject thao tác, tung Toast Error: "Không thể hủy đơn vì Quán đang nấu hoặc Tài xế đang trên đường giao!". |

### UC-07: Đánh giá đơn hàng (FR-C14)
| Thuộc tính | Chi tiết |
|---|---|
| **Tác nhân** | Customer |
| **Mô tả** | Nhận xét, cho điểm sao tổng trải nghiệm sau giao dịch thành công. |
| **Tiền điều kiện** | Order chỉ có thể Reviews khi đạt trạng thái cờ `COMPLETED`. Mỗi đơn chỉ đc Feedback duy nhất 1 lần. |
| **Hậu điều kiện** | Dữ liệu tính toán xếp hạng sao của nhà hàng đó được thay đổi. |
| **Luồng chính** | 1. Lựa chọn một đơn Hoàn tất trong danh sách Lịch sử mua hàng, bấm Cập nhật Đánh giá.<br>2. Chấm điểm Point tổng thể (1 đến 5 sao), gõ văn bản Comment.<br>3. Đánh giá chia tách nhỏ theo từng món bên trong Order Item đó (Điểm 1 tới 5 riêng từng món tuỳ độ ngon dở).<br>4. Xác nhận Gửi (Nút Submit).<br>5. Backend thực hiện `INSERT order_reviews` cho đơn, `INSERT order_item_reviews` vào chi tiết Item.<br>6. Khởi động background Trigger tính điểm mới cho nhà hàng (`UPDATE restaurants SET rating_avg = AVG(rating)`). |
| **Ngoại lệ** | - *E1 - Không thể Reviews:* Đơn chưa `COMPLETED` thì tự giấu (hide) nút Rate.<br>- *E2 - Đơn này đã Feedback rồi:* Quăng lỗi Conflict (409) "Bạn đã đánh giá đơn hàng này rồi". |


