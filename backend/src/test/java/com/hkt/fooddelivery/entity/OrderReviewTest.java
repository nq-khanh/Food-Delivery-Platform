package com.hkt.fooddelivery.entity;

import org.junit.jupiter.api.*;
import org.locationtech.jts.geom.Point;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderReviewTest {

    private Order order;
    private User customer;
    private Restaurant restaurant;
    private Product product1;
    private Product product2;
    private User admin;
    private Shipper shipper;
    private Point deliveryLocation;

    @BeforeEach
    void setUp() {
        order = mock(Order.class);
        customer = mock(User.class);
        restaurant = mock(Restaurant.class);
        admin = mock(User.class);
        shipper = mock(Shipper.class);
        deliveryLocation = mock(Point.class);

        // Mock Product và đảm bảo equals/hashCode dựa trên ID
        product1 = mock(Product.class);
        when(product1.getId()).thenReturn(UUID.randomUUID());
        when(product1.getName()).thenReturn("Pizza");
        when(product1.getPrice()).thenReturn(new java.math.BigDecimal("100"));

        product2 = mock(Product.class);
        when(product2.getId()).thenReturn(UUID.randomUUID());
        when(product2.getName()).thenReturn("Coke");
        when(product2.getPrice()).thenReturn(new java.math.BigDecimal("20"));

        order = new Order(customer, restaurant, "123 Street", deliveryLocation);
    }

    @Test
    @DisplayName("Nên tạo review thành công cho đơn hàng đã hoàn thành")
    void review_Success() {
        // Arrange: Chuẩn bị đơn hàng trạng thái COMPLETED
        order.addItem(product1, 1);
        order.confirm(admin);
        order.assignShipper(shipper);
        order.startShipping(admin);
        order.complete(admin);

        ReviewItemCommand itemCmd = new ReviewItemCommand(product1, 5, "Ngon lắm", "http://image.com");
        List<ReviewItemCommand> commands = List.of(itemCmd);

        // Act
        OrderReview review = order.review(5, "Quán sạch sẽ", 4, commands);

        // Assert
        assertNotNull(review);
        assertEquals(5, review.getRestaurantRating());
        assertEquals(4, review.getShipperRating());
        assertEquals(1, review.getItemReviews().size());
        assertEquals("Pizza", review.getItemReviews().get(0).getProduct().getName());
    }

    @Test
    @DisplayName("Nên tạo review thành công khi không có shipper rating (tùy chọn)")
    void review_Success_NoShipperRating() {
        order.addItem(product1, 1);
        order.confirm(admin);
        order.assignShipper(shipper);
        order.startShipping(admin);
        order.complete(admin);

        // Act: shipperRating truyền null
        OrderReview review = order.review(5, "Good", null, null);

        // Assert
        assertNotNull(review);
        assertNull(review.getShipperRating());
    }

    @Test
    @DisplayName("Nên ném lỗi nếu đơn hàng chưa hoàn thành")
    void review_Fail_NotCompleted() {
        // Đơn hàng mặc định là PENDING
        assertThrows(IllegalStateException.class, () -> {
            order.review(5, "Good", 5, null);
        });
    }

    @Test
    @DisplayName("Nên ném lỗi nếu sản phẩm review không có trong đơn hàng")
    void review_Fail_ProductNotInOrder() {
        order.addItem(product1, 1);
        order.confirm(admin);
        order.assignShipper(shipper);
        order.startShipping(admin);
        order.complete(admin);

        // product2 không được add vào order
        ReviewItemCommand invalidCmd = new ReviewItemCommand(product2, 5, "Hơi tệ", null);

        assertThrows(IllegalArgumentException.class, () -> {
            order.review(5, "Ok", 5, List.of(invalidCmd));
        });
    }

    @Test
    @DisplayName("Nên ném lỗi nếu rating ngoài khoảng 1-5")
    void review_Fail_InvalidRating() {
        order.addItem(product1, 1);
        order.confirm(admin);
        order.assignShipper(shipper);
        order.startShipping(admin);
        order.complete(admin);

        assertThrows(IllegalArgumentException.class, () -> {
            order.review(6, "Quá tốt", 5, null); // Rating 6 là sai
        });
    }

    @Test
    @DisplayName("Nên ném lỗi nếu đơn hàng đã được review trước đó")
    void review_Fail_AlreadyReviewed() {
        order.addItem(product1, 1);
        order.confirm(admin);
        order.assignShipper(shipper);
        order.startShipping(admin);
        order.complete(admin);

        // Review lần 1
        order.review(5, "Lần 1", 5, null);

        // Review lần 2 phải lỗi
        assertThrows(IllegalStateException.class, () -> {
            order.review(4, "Lần 2", 4, null);
        });
    }

    @Test
    @DisplayName("Nhà hàng nên phản hồi được đánh giá")
    void restaurantReply_Success() {
        // Arrange
        order.addItem(product1, 1);
        order.confirm(admin);
        order.assignShipper(shipper);
        order.startShipping(admin);
        order.complete(admin);
        OrderReview review = order.review(5, "Tốt", 5, null);

        // Act
        review.replyByRestaurant("Cảm ơn quý khách!");

        // Assert
        assertEquals("Cảm ơn quý khách!", review.getRestaurantReply());
        assertNotNull(review.getRepliedAt());
    }

    @Test
    @DisplayName("Nên ném lỗi nếu nội dung phản hồi trống")
    void restaurantReply_Fail_BlankContent() {
        order.addItem(product1, 1);
        order.confirm(admin);         // Chuyển PENDING -> CONFIRMED
        order.assignShipper(shipper);
        order.startShipping(admin);   // Chuyển CONFIRMED -> SHIPPING
        order.complete(admin);        // Chuyển SHIPPING -> COMPLETED
        OrderReview review = order.review(5, "Tốt", 5, null);

        assertThrows(IllegalArgumentException.class, () -> review.replyByRestaurant("  "));
    }
}
