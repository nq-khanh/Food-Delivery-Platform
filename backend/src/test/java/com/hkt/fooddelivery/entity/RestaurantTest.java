package com.hkt.fooddelivery.entity;

import com.hkt.fooddelivery.entity.enums.ApprovalStatus;
import com.hkt.fooddelivery.entity.enums.DayOfWeek;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RestaurantTest {

    private Restaurant restaurant;
    private User owner;
    private Point location;

    @BeforeEach
    void setUp() {
        owner = mock(User.class);
        location = mock(Point.class);
        restaurant = new Restaurant(owner, "HKT Kitchen", "123 District 1", location);
    }

    @Test
    @DisplayName("Nên mặc định là PENDING và Inactive khi mới tạo")
    void constructor_InitialState() {
        assertEquals(ApprovalStatus.PENDING, restaurant.getApprovalStatus());
        assertFalse(restaurant.isActive());
    }

    @Test
    @DisplayName("Nên hoạt động chính xác khi mở cửa trong ngày (8h - 22h)")
    void isOpenNow_NormalHours() {
        restaurant.approve(); // Phải active và approved
        restaurant.addOperatingHour(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(22, 0));

        // Thứ 2, lúc 10:00 AM (Trong khung giờ)
        Instant openTime = LocalDateTime.of(2026, 3, 23, 10, 0).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
        // Thứ 2, lúc 7:00 AM (Trước khung giờ)
        Instant beforeOpen = LocalDateTime.of(2026, 3, 23, 7, 0).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();

        assertTrue(restaurant.isOpenNow(openTime));
        assertFalse(restaurant.isOpenNow(beforeOpen));
    }

    @Test
    @DisplayName("Nên hoạt động chính xác khi mở cửa xuyên đêm (22h - 3h sáng hôm sau)")
    void isOpenNow_OvernightHours() {
        restaurant.approve();
        // Mở 22:00, đóng 03:00 rạng sáng
        restaurant.addOperatingHour(DayOfWeek.FRIDAY, LocalTime.of(22, 0), LocalTime.of(3, 0));

        // Thứ 6, lúc 23:30 (Đang mở)
        Instant lateNight = LocalDateTime.of(2026, 3, 27, 23, 30).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
        // Thứ 6, lúc 02:00 AM (Vẫn đang mở - tính theo logic overnight của Friday)
        Instant earlyMorning = LocalDateTime.of(2026, 3, 27, 2, 0).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
        // Thứ 6, lúc 10:00 AM (Đang đóng)
        Instant morning = LocalDateTime.of(2026, 3, 27, 10, 0).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();

        assertTrue(restaurant.isOpenNow(lateNight));
        assertTrue(restaurant.isOpenNow(earlyMorning));
        assertFalse(restaurant.isOpenNow(morning));
    }

    @Test
    @DisplayName("Nên trả về false nếu nhà hàng chưa được duyệt hoặc bị khóa")
    void isOpenNow_InactiveOrNotApproved() {
        restaurant.addOperatingHour(DayOfWeek.MONDAY, LocalTime.of(0, 0), LocalTime.of(23, 59));
        Instant now = LocalDateTime.of(2026, 3, 23, 12, 0).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();

        // Mặc định là PENDING và Inactive
        assertFalse(restaurant.isOpenNow(now));

        restaurant.approve(); // Active = true
        assertTrue(restaurant.isOpenNow(now));

        restaurant.deactivate(); // Active = false
        assertFalse(restaurant.isOpenNow(now));
    }

    @Test
    @DisplayName("Nên cập nhật rating trung bình chính xác bằng công thức lũy tiến")
    void updateRating_ShouldCalculateCorrectly() {
        // Lần 1: 5 sao
        restaurant.updateRating(5);
        assertEquals(new java.math.BigDecimal("5.0"), restaurant.getRatingAvg());
        assertEquals(1, restaurant.getReviewCount());

        // Lần 2: 4 sao -> (5+4)/2 = 4.5
        restaurant.updateRating(4);
        assertEquals(new java.math.BigDecimal("4.5"), restaurant.getRatingAvg());
        assertEquals(2, restaurant.getReviewCount());

        // Lần 3: 3 sao -> (4.5*2 + 3)/3 = 4.0
        restaurant.updateRating(3);
        assertEquals(new java.math.BigDecimal("4.0"), restaurant.getRatingAvg());
        assertEquals(3, restaurant.getReviewCount());
    }

    @Test
    @DisplayName("Nên chặn việc phê duyệt nếu trạng thái không phải PENDING")
    void approve_OnlyPending() {
        restaurant.approve();
        assertThrows(IllegalStateException.class, () -> restaurant.approve());

        // Sau khi bị reject cũng không được approve lại (quy trình nghiệp vụ)
        // Lưu ý: Tùy nghiệp vụ bạn có thể cho phép approve lại sau khi sửa đổi
    }
}