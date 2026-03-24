package com.hkt.fooddelivery.repository;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.hkt.fooddelivery.entity.Role;
import com.hkt.fooddelivery.entity.User;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Nên lưu User thành công và tìm lại được bằng Email")
    void shouldSaveAndFindUserByEmail() {
        User newUser = new User(
                "nttung1901",
                "2251010093tung@ou.edu.vn",
                "0948451901",
                "Tùng",
                "Nguyễn",
                "fjsldfsdfsdfsdfsdf"
        );

        User savedUser = userRepository.save(newUser);
        Optional<User> foundUser = userRepository.findByEmail("2251010093tung@ou.edu.vn");

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("2251010093tung@ou.edu.vn");
        assertThat(foundUser.get().getFirstName()).isEqualTo("Tùng");
        assertThat(foundUser.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Nên trả về Optional rỗng khi tìm Email không tồn tại")
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<User> foundUser = userRepository.findByEmail("notfound@example.com");
        assertThat(foundUser).isEmpty();
    }
}