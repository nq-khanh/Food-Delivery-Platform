package com.hkt.fooddelivery.repository;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.hkt.fooddelivery.entity.Role;
import com.hkt.fooddelivery.entity.User;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test") // Sử dụng file application-test.yml nếu có
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Nên lưu User thành công và tìm lại được bằng Email")
    void shouldSaveAndFindUserByEmail() {
        // 1. Arrange (Chuẩn bị dữ liệu)
        User newUser = User.builder()
                .email("tung.nguyen@example.com")
                .passwordHash("hashed_password")
                .role(Role.USER)
                .firstName("Tùng")
                .lastName("Nguyễn")
                .phone("0987654321")
                .isActive(true)
                .isVerified(false)
                .build();

        // 2. Act (Thực hiện hành động)
        User savedUser = userRepository.save(newUser);
        Optional<User> foundUser = userRepository.findByEmail("tung.nguyen@example.com");

        // 3. Assert (Kiểm tra kết quả)
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull(); // Kiểm tra UUID đã được sinh ra chưa
        
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("tung.nguyen@example.com");
        assertThat(foundUser.get().getFirstName()).isEqualTo("Tùng");
        assertThat(foundUser.get().getCreatedAt()).isNotNull(); // Kiểm tra @PrePersist có hoạt động không
    }

    @Test
    @DisplayName("Nên trả về Optional rỗng khi tìm Email không tồn tại")
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<User> foundUser = userRepository.findByEmail("notfound@example.com");
        assertThat(foundUser).isEmpty();
    }
}