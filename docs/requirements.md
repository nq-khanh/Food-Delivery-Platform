## 1. Mục tiêu dự án

Xây dựng nền tảng đặt giao đồ ăn đa vai trò (Customer, Merchant, Shipper, Admin).
Mục tiêu là vận hành được luồng đặt đơn thực tế và xử lý vận hành cơ bản.

## 2. Phạm vi
- Quản lý tài khoản cho 4 vai trò.
- Tìm kiếm món ăn, nhà hàng gần khu vực giao.
- Giỏ hàng, áp voucher, đặt đơn, thanh toán COD/VNPAY.
- Merchant quản lý món, nhận đơn, cập nhật trạng thái nấu.
- Shipper tự nhận đơn, giao đơn, cập nhật trạng thái giao.
- Admin duyệt merchant/shipper, quản lý voucher, cấu hình phí ship, xem báo cáo tổng quan.
- Ví hệ thống và yêu cầu rút tiền cho merchant/shipper, admin chuyển khoản thủ công.

## 3. Actors và trách nhiệm

- Customer: tìm món, đặt đơn, thanh toán, theo dõi đơn.
- Merchant: quản lý cửa hàng và món, xử lý đơn, xem doanh thu.
- Shipper: nhận đơn và giao đơn, theo dõi thu nhập.
- Admin: quản trị hệ thống, duyệt đối tác, cấu hình vận hành.

## 4. Functional requirements theo actor

### 4.1 Customer

- Đăng ký, đăng nhập, đổi mật khẩu, quên mật khẩu.
- Cập nhật thông tin cá nhân, avatar, số điện thoại.
- Quản lý danh sách địa chỉ nhận hàng, chọn địa chỉ mặc định.
- Tìm kiếm món ăn/nhà hàng:
  - Gần tôi (dựa trên lat/lng).
  - Liên quan theo từ khóa.
  - Theo ngữ cảnh tìm kiếm cơ bản (food type, giá, khoảng cách, available).
- Thêm/sửa/xóa món trong giỏ hàng.
- Áp voucher hợp lệ.
- Đặt đơn với COD hoặc VNPAY.
- Theo dõi trạng thái đơn, xem lịch sử, xem chi tiết đơn.
- Xem thông tin liên lạc shipper khi đơn đã được nhận.
- Hủy đơn theo rule trạng thái.
- Đánh giá đơn hàng và đánh giá từng món trong đơn sau khi hoàn tất.

### 4.2 Merchant

- Đăng ký trở thành đối tác nhà hàng (từ tài khoản customer có sẵn).
- Cập nhật thông tin nhà hàng, giờ mở cửa.
- CRUD category và product.
- Bật/tắt món hết hàng (`is_available`).
- Nhận và xác nhận đơn mới.
- Cập nhật trạng thái đơn: đang nấu -> sẵn sàng giao.
- Xem thống kê doanh thu cơ bản theo ngày/tuần/tháng.
- Gửi yêu cầu rút tiền từ ví.

### 4.3 Shipper

- Đăng ký trở thành đối tác tài xế (từ tài khoản customer có sẵn).
- Xem danh sách đơn chờ nhận (có điểm lấy + điểm giao + chi tiết đơn).
- Tự nhận đơn.
- Nút điều hướng Google Maps đến điểm lấy/giao.
- Cập nhật trạng thái giao hàng.
- Xem lịch sử đơn đã nhận và thu nhập.
- Gửi yêu cầu rút tiền từ ví.

### 4.4 Admin

- Duyệt merchant đăng ký mới.
- Duyệt shipper đăng ký mới.
- CRUD quản lý merchant, shipper, voucher.
- Cấu hình phí ship theo khoảng cách và peak time.
- Theo dõi payout request, xử lý chuyển khoản thủ công.
- Báo cáo tổng quan hệ thống: tổng số đơn, GMV, doanh thu nền tảng, tỷ lệ hủy đơn.

## 5. Non-functional requirements

- Security: JWT + refresh token, password hash an toàn, RBAC.
- Performance: API p95 < 500ms, tìm kiếm p95 < 800ms.
- Availability: uptime môi trường demo >= 99% trong giai đoạn bảo vệ.
- Reliability: mọi transition trạng thái đơn phải có log lịch sử.
- Auditability: có log cho các hành động admin và payout.

## 6. Business rules

- Đơn chỉ được shipper nhận khi ở trạng thái `READY_FOR_PICKUP`.
- Customer được hủy đơn khi chưa có shipper nhận hoặc theo cấu hình.
- Voucher chỉ áp dụng khi còn hạn, còn lượt dùng, đạt min order value.
- Payout request chỉ hợp lệ khi số dư ví >= số tiền yêu cầu.
- Merchant/Shipper chỉ được hoạt động sau khi admin duyệt.
- Chỉ cho phép đánh giá khi đơn ở trạng thái `COMPLETED` hoặc `DELIVERED`.
- Mỗi order chỉ có 1 đánh giá tổng quan; mỗi order item chỉ có 1 đánh giá.