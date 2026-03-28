package com.hkt.fooddelivery.entity.listener;

import com.hkt.fooddelivery.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OrderReviewListenerTest {

    private OrderReviewListener listener;
    private OrderReview review;
    private Restaurant restaurant;
    private Shipper shipper;
    private Product product;
    private OrderItemReview itemReview;

    @BeforeEach
    void setUp() {
        listener = new OrderReviewListener();
        review = mock(OrderReview.class);
        restaurant = new Restaurant(mock(User.class), "Quan Ngon", "Addr", mock(org.locationtech.jts.geom.Point.class));
        shipper = new Shipper(mock(User.class), "29A-12345");
        product = new Product(restaurant, "Burger", new BigDecimal("50"));

        itemReview = mock(OrderItemReview.class);
        when(itemReview.getProduct()).thenReturn(product);
        when(itemReview.getRating()).thenReturn(5);

        when(review.getRestaurant()).thenReturn(restaurant);
        when(review.getRestaurantRating()).thenReturn(4);
        when(review.getShipper()).thenReturn(shipper);
        when(review.getShipperRating()).thenReturn(5);
        when(review.getItemReviews()).thenReturn(List.of(itemReview));
    }

    @Test
    @DisplayName("Nên cập nhật rating trung bình cho Restaurant, Shipper và Product sau khi lưu Review")
    void afterReviewSaved_ShouldUpdateAllRatings() {
        // Act
        listener.afterReviewSaved(review);

        // Assert Restaurant (0 -> 4.0)
        assertEquals(new BigDecimal("4.0"), restaurant.getRatingAvg());
        assertEquals(1, restaurant.getReviewCount());

        // Assert Shipper (0 -> 5.0)
        assertEquals(new BigDecimal("5.0"), shipper.getRatingAvg());
        assertEquals(1, shipper.getReviewCount());

        // Assert Product (0 -> 5.0)
        assertEquals(new BigDecimal("5.0"), product.getRatingAvg());
        assertEquals(1, product.getReviewCount());
    }

    @Test
    @DisplayName("Tính toán Rating lũy tiến phải chính xác")
    void ratingCalculation_ShouldBeAccurate() {
        // Giả sử quán đã có 1 review 3 sao
        restaurant.updateRating(3);

        // Thêm review 5 sao mới (thông qua listener)
        listener.afterReviewSaved(review); // review trong setup là 4 sao

        // (3 + 4) / 2 = 3.5
        assertEquals(new BigDecimal("3.5"), restaurant.getRatingAvg());
        assertEquals(2, restaurant.getReviewCount());
    }
}