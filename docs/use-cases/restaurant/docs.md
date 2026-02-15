# TÀI LIỆU ĐẶC TẢ USE CASE & CLASS DIAGRAM

## Module: Restaurant

### Thông tin tài liệu

- Ngày tạo: 15/02/2026
- Người thực hiện: Nguyễn Quang Khánh

### Mục tiêu

Tài liệu mô tả Class diagram và Use Case diagram của module Restaurant, với mục đích giải thích và báo cáo kết quả cá nhân của tuần làm việc đầu tiên.

## 1. Biểu đồ Use Case (Chức năng Chủ nhà hàng)

Tác nhân **Chủ nhà hàng (Restaurant Owner)** có các quyền hạn chính sau:

### Quản lý thực đơn và món ăn

- **CRUD Item:** Thêm mới, xem chi tiết, chỉnh sửa thông tin và xóa món ăn.
- **CRUD MENU:** Quản lý các danh mục thực đơn (ví dụ: Món khai vị, Món chính, Đồ uống).
- **Bật/tắt món ăn:** Chuyển đổi trạng thái hiển thị của món ăn trên ứng dụng (Còn hàng/Hết hàng).

### Quản lý vận hành đơn hàng

- **Xem danh sách đơn hàng:** Theo dõi tất cả các đơn hàng đang chờ hoặc đã hoàn tất.
- **Xác nhận/Từ chối đơn hàng:** Tiếp nhận đơn hàng mới từ khách hàng hoặc hủy đơn nếu cần.
- **Cập nhật trạng thái đơn hàng:** Cập nhật tiến độ (Ví dụ: Đang chuẩn bị, Đang giao, Đã giao).

### Báo cáo & Thống kê

- **Xem báo cáo doanh thu:** Theo dõi các chỉ số tài chính và hiệu quả kinh doanh.

---

## 2. Biểu đồ Class (Cấu trúc dữ liệu)

Hệ thống bao gồm các thực thể chính với các mối quan hệ logic sau:

### 2.1. Thực thể Người dùng & Địa chỉ

- **User:** Lưu trữ thông tin định danh người dùng.
- **Address:** Chứa thông tin địa chỉ chi tiết (Label, đường, chi tiết). Một `User` liên kết với `Address`.

### 2.2. Thực thể Nhà hàng & Thực đơn

- **Restaurant:** \* Thuộc tính: `name`, `address`, `opening_time`, `closing_time`, `avg_rating`, `status`.
  - Mối quan hệ: Một nhà hàng sở hữu nhiều `Menu`.
- **Menu:** \* Thuộc tính: `name`, `description`.
  - Mối quan hệ: Một `Menu` chứa nhiều `Item`.
- **Item:** \* Thuộc tính: `name`, `price`, `description`, `img_url`, `status`.

### 2.3. Thực thể Đơn hàng & Thanh toán

- **Order:** Quản lý thông tin chung (Customer ID, Shipper ID, Status).
- **OrderDetails:** (Class liên kết từ Order)
  - Lưu trữ: `subtotal`, `shipping_fee`, `total_amount`, và danh sách món ăn đã đặt (`order_items`).
- **Voucher:** Áp dụng mã giảm giá trực tiếp vào đơn hàng.

### 2.4. Thực thể Đánh giá (Review)

- **Review:** Cho phép lưu trữ đánh giá từ `User` dành cho `Restaurant` hoặc `Item`.
  - Thuộc tính: `rating` (double), `comment`, `review_type` (Enum).

---
