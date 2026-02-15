# Tài liệu Đặc tả Cơ sở dữ liệu - Hệ thống Food Delivery

Tài liệu này mô tả chi tiết cấu trúc bảng, các mối quan hệ và quy tắc dữ liệu cho hệ thống quản lý đặt đồ ăn dựa trên sơ đồ thiết kế.

---

## 1. Tổng quan Thực thể (Entities)

Hệ thống được chia thành 4 phân vùng chức năng chính:
- **Người dùng (Users & Profiles):** Quản lý tài khoản và thông tin cá nhân.
- **Địa chỉ & Vị trí:** Quản lý sổ địa chỉ của khách hàng.
- **Giỏ hàng & Đặt hàng:** Quy trình từ khi chọn món đến khi thanh toán.
- **Khuyến mãi & Đánh giá:** Các tính năng hỗ trợ marketing và chất lượng dịch vụ.

---

## 2. Chi tiết các Bảng dữ liệu

### 2.1 Nhóm Người dùng (Authentication & Identity)

#### Bảng `Users`
Lưu trữ thông tin định danh và đăng nhập cơ bản.
- **id**: `bigint` (PK) - Mã định danh duy nhất.
- **phone**: `varchar(15)` (NN) - Số điện thoại dùng để đăng nhập.
- **email**: `varchar(100)` - Email liên hệ.
- **role**: `enum` (customer, restaurant, shipper, admin) - Phân quyền người dùng.
- **status**: `enum` (active, inactive, banned) - Trạng thái tài khoản.

#### Bảng `CustomerProfiles`
Thông tin chi tiết dành riêng cho khách hàng.
- **user_id**: `bigint` (FK) - Liên kết tới `Users.id`.
- **full_name**: `varchar(150)` - Họ và tên đầy đủ.
- **loyalty_points**: `int` - Điểm tích lũy thành viên.
- **referral_code**: `varchar(20)` - Mã giới thiệu cá nhân.

---

### 2.2 Nhóm Đơn hàng & Giỏ hàng (Sales Flow)

#### Bảng `Orders`
Bảng trung tâm lưu trữ mọi giao dịch đặt hàng.
- **order_code**: `varchar(50)` (NN) - Mã đơn hàng hiển thị cho người dùng (VD: ORD123).
- **customer_id**: `bigint` (FK) - Người đặt hàng.
- **delivery_address_text**: `text` - Địa chỉ giao hàng tại thời điểm đặt (Snapshot).
- **total_amount**: `decimal(12,2)` - Tổng số tiền cuối cùng sau giảm giá.
- **payment_method**: `enum` (cash, momo, vnpay, wallet).
- **status**: Trạng thái đơn (pending, confirmed, delivering, delivered, cancelled).

#### Bảng `CartItems`
Lưu thông tin món ăn trong giỏ hàng chờ thanh toán.
- **options_json**: `json` - Lưu các tùy chọn món ăn (VD: `{ "sugar": "50%", "ice": "none" }`).

---

### 2.3 Nhóm Vị trí & Phản hồi

#### Bảng `CustomerAddresses`
- **latitude / longitude**: `decimal` - Tọa độ chính xác để định vị trên bản đồ và tính phí ship.
- **is_default**: `boolean` - Đánh dấu địa chỉ mặc định khi đặt hàng.

#### Bảng `Reviews`
- **rating**: `tinyint` - Điểm đánh giá (thường từ 1-5).
- **is_visible**: `boolean` - Kiểm soát việc hiển thị đánh giá (ẩn nếu vi phạm quy tắc).

---

## 3. Các mối quan hệ chính (ER Relationships)

| Từ bảng | Tới bảng | Loại quan hệ | Diễn giải |
| :--- | :--- | :--- | :--- |
| `Users` | `CustomerProfiles` | 1:1 | Mỗi user có một hồ sơ khách hàng. |
| `Users` | `CustomerAddresses` | 1:N | Một khách hàng có nhiều địa chỉ nhận hàng. |
| `Orders` | `OrderStatusHistory` | 1:N | Một đơn hàng có lịch sử thay đổi trạng thái. |
| `Carts` | `CartItems` | 1:N | Một giỏ hàng chứa nhiều món ăn. |
| `Orders` | `Reviews` | 1:1 | Mỗi đơn hàng chỉ được đánh giá một lần. |

---

## 4. Quy tắc dữ liệu (Data Rules)

1. **Tính nhất quán (Data Snapshot):** Thông tin số điện thoại (`customer_phone_snapshot`) và địa chỉ trong bảng `Orders` phải được lưu trực tiếp dạng text thay vì tham chiếu (Reference) để đảm bảo lịch sử đơn hàng không bị thay đổi nếu khách hàng cập nhật hồ sơ sau này.
2. **Độ chính xác tiền tệ:** Tất cả các trường liên quan đến giá tiền (`subtotal`, `shipping_fee`, `discount_amount`) sử dụng kiểu `decimal(12,2)` để tránh sai số dấu phẩy động.
3. **Quản lý thời gian:** Tất cả các bảng đều có `created_at` và `updated_at` (Timestamp) để phục vụ việc truy vết dữ liệu và báo cáo.
