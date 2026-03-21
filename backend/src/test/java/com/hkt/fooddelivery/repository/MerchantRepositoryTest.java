package com.hkt.fooddelivery.repository;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import com.hkt.fooddelivery.entity.Category;
import com.hkt.fooddelivery.entity.Product;
import com.hkt.fooddelivery.entity.Restaurant;
import com.hkt.fooddelivery.entity.Role;
import com.hkt.fooddelivery.entity.User;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Merchant-related repositories.
 * Uses @DataJpaTest with H2 in-memory database.
 *
 * DoD: Tạo 1 Restaurant, thêm 1 Category và 2 Products,
 * sau đó truy vấn theo restaurant_id trả về đủ dữ liệu.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class MerchantRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private User owner;
    private Restaurant restaurant;
    private Category category;

    @BeforeEach
    void setUp() {
        // 1. Tạo User (role MERCHANT) làm owner
        owner = User.builder()
                .username("merchant01")
                .email("merchant@example.com")
                .passwordHash("$2a$10$hashedpassword")
                .role(Role.MERCHANT)
                .firstName("Nguyen")
                .lastName("Van A")
                .phone("0901234567")
                .isActive(true)
                .build();
        entityManager.persist(owner);

        // 2. Tạo 1 Restaurant
        restaurant = Restaurant.builder()
                .owner(owner)
                .name("Quán Phở Hà Nội")
                .address("123 Nguyễn Huệ, Q1, TP.HCM")
                .description("Phở bò truyền thống Hà Nội")
                .approvalStatus("APPROVED")
                .isActive(true)
                .build();
        entityManager.persist(restaurant);

        // 3. Thêm 1 Category
        category = Category.builder()
                .restaurant(restaurant)
                .name("Món chính")
                .displayOrder(1)
                .build();
        entityManager.persist(category);

        // 4. Thêm 2 Products thuộc category
        Product product1 = Product.builder()
                .restaurant(restaurant)
                .category(category)
                .name("Phở bò tái")
                .price(new BigDecimal("55000.00"))
                .description("Phở bò tái truyền thống")
                .isAvailable(true)
                .build();
        entityManager.persist(product1);

        Product product2 = Product.builder()
                .restaurant(restaurant)
                .category(category)
                .name("Phở bò chín")
                .price(new BigDecimal("50000.00"))
                .description("Phở bò chín nạm gầu")
                .isAvailable(true)
                .build();
        entityManager.persist(product2);

        entityManager.flush();
    }

    @Test
    @DisplayName("Truy vấn Products theo restaurant_id trả về đủ 2 sản phẩm")
    void findProductsByRestaurantId_shouldReturnTwoProducts() {
        List<Product> products = productRepository.findByRestaurantId(restaurant.getId());

        assertThat(products).hasSize(2);
        assertThat(products)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Phở bò tái", "Phở bò chín");
    }

    @Test
    @DisplayName("Truy vấn Categories theo restaurant_id trả về đủ 1 category")
    void findCategoriesByRestaurantId_shouldReturnOneCategory() {
        List<Category> categories = categoryRepository
                .findByRestaurantIdOrderByDisplayOrderAsc(restaurant.getId());

        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).getName()).isEqualTo("Món chính");
    }

    @Test
    @DisplayName("Truy vấn Restaurant theo owner_id trả về đúng nhà hàng")
    void findRestaurantsByOwnerId_shouldReturnRestaurant() {
        List<Restaurant> restaurants = restaurantRepository.findByOwnerId(owner.getId());

        assertThat(restaurants).hasSize(1);
        assertThat(restaurants.get(0).getName()).isEqualTo("Quán Phở Hà Nội");
    }

    @Test
    @DisplayName("Truy vấn Products available theo restaurant_id")
    void findAvailableProductsByRestaurantId_shouldReturnAvailableProducts() {
        List<Product> products = productRepository
                .findByRestaurantIdAndIsAvailableTrue(restaurant.getId());

        assertThat(products).hasSize(2);
    }

    @Test
    @DisplayName("existsByOwnerIdAndName trả về true khi tên nhà hàng đã tồn tại")
    void existsByOwnerIdAndName_shouldReturnTrue() {
        boolean exists = restaurantRepository
                .existsByOwnerIdAndName(owner.getId(), "Quán Phở Hà Nội");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("findByApprovalStatus trả về nhà hàng có status APPROVED")
    void findByApprovalStatus_shouldReturnApprovedRestaurants() {
        List<Restaurant> approved = restaurantRepository.findByApprovalStatus("APPROVED");

        assertThat(approved).hasSize(1);
        assertThat(approved.get(0).getApprovalStatus()).isEqualTo("APPROVED");
    }
}
