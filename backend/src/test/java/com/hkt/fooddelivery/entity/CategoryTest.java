package com.hkt.fooddelivery.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class CategoryTest {

    private Restaurant mockRestaurant;

    @BeforeEach
    void setUp() {
        mockRestaurant = mock(Restaurant.class);
    }

    @Test
    @DisplayName("Nên tạo Category thành công và chuẩn hóa tên")
    void createCategory_Success() {
        Category category = new Category(mockRestaurant, "  Món Khai Vị  ");

        assertEquals("Món Khai Vị", category.getName());
        assertEquals(0, category.getDisplayOrder());
    }

    @Test
    @DisplayName("Nên ném lỗi khi tên category trống hoặc null")
    void createCategory_InvalidName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Category(mockRestaurant, ""));
        assertThrows(NullPointerException.class, () -> new Category(mockRestaurant, null));
    }

    @Test
    @DisplayName("Thứ tự hiển thị không được là số âm")
    void changeDisplayOrder_Negative_ThrowsException() {
        Category category = new Category(mockRestaurant, "Drinks");

        assertThrows(IllegalArgumentException.class, () -> category.changeDisplayOrder(-1));
    }
}