package com.hkt.fooddelivery.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductTest {

    private Restaurant mockRestaurant;
    private Category mockCategory;
    private Product product;

    @BeforeEach
    void setUp() {
        mockRestaurant = mock(Restaurant.class);
        when(mockRestaurant.getId()).thenReturn(UUID.randomUUID());

        mockCategory = mock(Category.class);
        when(mockCategory.getRestaurant()).thenReturn(mockRestaurant);

        product = new Product(mockRestaurant, "Pizza Hải Sản", new BigDecimal("150000"));
    }

    @Test
    @DisplayName("Nên tạo sản phẩm thành công với dữ liệu hợp lệ")
    void createProduct_Success() {
        assertAll(
                () -> assertEquals("Pizza Hải Sản", product.getName()),
                () -> assertEquals(new BigDecimal("150000.00"), product.getPrice()),
                () -> assertTrue(product.isAvailable())
        );
    }

    @Test
    @DisplayName("Nên ném lỗi khi giá sản phẩm nhỏ hơn 0")
    void createProduct_InvalidPrice_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Product(mockRestaurant, "Test", new BigDecimal("-1"))
        );
    }

    @Test
    @DisplayName("Nên gán Category thành công nếu cùng nhà hàng")
    void assignCategory_SameRestaurant_Success() {
        product.assignCategory(mockCategory);
        assertEquals(mockCategory, product.getCategory());
    }

    @Test
    @DisplayName("Nên ném lỗi nếu gán Category của nhà hàng khác")
    void assignCategory_DifferentRestaurant_ThrowsException() {
        Restaurant otherRestaurant = mock(Restaurant.class);
        when(otherRestaurant.getId()).thenReturn(UUID.randomUUID());

        Category otherCategory = mock(Category.class);
        when(otherCategory.getRestaurant()).thenReturn(otherRestaurant);

        assertThrows(IllegalArgumentException.class, () ->
                product.assignCategory(otherCategory)
        );
    }

    @Test
    @DisplayName("Tính toán Rating trung bình lũy tiến chính xác")
    void updateRating_ShouldCalculateMovingAverage() {
        // Lần 1: 5 sao -> (0*0 + 5)/1 = 5.0
        product.updateRating(5);
        assertEquals(new BigDecimal("5.0"), product.getRatingAvg());

        // Lần 2: 4 sao -> (5.0*1 + 4)/2 = 4.5
        product.updateRating(4);
        assertEquals(new BigDecimal("4.5"), product.getRatingAvg());

        // Lần 3: 3 sao -> (4.5*2 + 3)/3 = 4.0
        product.updateRating(3);
        assertEquals(new BigDecimal("4.0"), product.getRatingAvg());
    }

    @Test
    @DisplayName("Nên tạo ProductEmbedding thành công với vector 768 dims")
    void createEmbedding_ValidDimensions_Success() {
        float[] validEmbedding = new float[768];
        ProductEmbedding embedding = new ProductEmbedding(product, validEmbedding);

        assertEquals(768, embedding.getEmbedding().length);
    }

    @Test
    @DisplayName("Nên ném lỗi nếu vector embedding sai kích thước")
    void createEmbedding_InvalidDimensions_ThrowsException() {
        float[] invalidEmbedding = new float[128];
        assertThrows(IllegalArgumentException.class, () ->
                new ProductEmbedding(product, invalidEmbedding)
        );
    }
}