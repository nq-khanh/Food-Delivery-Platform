package com.hkt.fooddelivery.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import com.hkt.fooddelivery.entity.Role;
import com.hkt.fooddelivery.entity.User;
import com.hkt.fooddelivery.entity.UserAddress;
import com.hkt.fooddelivery.entity.UserToken;
import com.hkt.fooddelivery.entity.Wallet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Customer-related repositories.
 *
 * DoD: Tạo 1 User mới gắn kèm 1 Wallet với số dư ban đầu → cả hai Save/Find đều Pass.
 *
 * Profile "test" sử dụng H2 in-memory với ddl-auto: create-drop.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private UserTokenRepository userTokenRepository;

    private User customer;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .username("khach_hang_01")
                .email("customer01@example.com")
                .passwordHash("$2a$10$hashed_password_example")
                .role(Role.USER)
                .firstName("An")
                .lastName("Nguyễn")
                .phone("0912345678")
                .isActive(true)
                .isVerified(false)
                .build();

        entityManager.persist(customer);
        entityManager.flush();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 1 (DoD chính): Tạo User + Wallet với số dư ban đầu
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DoD: Tạo 1 User mới gắn kèm 1 Wallet với số dư ban đầu — Save & Find phải Pass")
    void shouldSaveUserAndWalletWithInitialBalance() {
        // Arrange
        Wallet wallet = Wallet.builder()
                .user(customer)
                .balance(new BigDecimal("50000.00"))
                .build();

        // Act
        Wallet savedWallet = walletRepository.save(wallet);
        entityManager.flush();
        entityManager.clear(); // Clear cache để test query thực sự từ DB

        Optional<Wallet> foundWallet = walletRepository.findByUserId(customer.getId());
        Optional<User> foundUser   = userRepository.findById(customer.getId());

        // Assert — User
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getRole()).isEqualTo(Role.USER);
        assertThat(foundUser.get().getEmail()).isEqualTo("customer01@example.com");

        // Assert — Wallet
        assertThat(savedWallet.getId()).isNotNull();
        assertThat(foundWallet).isPresent();
        assertThat(foundWallet.get().getBalance())
                .isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(foundWallet.get().getUser().getId()).isEqualTo(customer.getId());

        // existsByUserId
        assertThat(walletRepository.existsByUserId(customer.getId())).isTrue();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 2: Thêm UserAddress và tìm lại theo userId
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Nên lưu UserAddress và tìm lại được theo userId")
    void shouldSaveAndFindUserAddressByUserId() {
        // Arrange
        UserAddress address = UserAddress.builder()
                .user(customer)
                .addressName("Nhà riêng")
                .fullAddress("123 Lê Lợi, Quận 1, TP.HCM")
                .location("POINT(106.6952 10.7763)")
                .isDefault(true)
                .build();

        // Act
        userAddressRepository.save(address);
        entityManager.flush();
        entityManager.clear();

        List<UserAddress> addresses = userAddressRepository.findByUserId(customer.getId());
        Optional<UserAddress> defaultAddr = userAddressRepository
                .findByUserIdAndIsDefaultTrue(customer.getId());

        // Assert
        assertThat(addresses).hasSize(1);
        assertThat(addresses.get(0).getFullAddress()).isEqualTo("123 Lê Lợi, Quận 1, TP.HCM");

        assertThat(defaultAddr).isPresent();
        assertThat(defaultAddr.get().getAddressName()).isEqualTo("Nhà riêng");

        assertThat(userAddressRepository.existsByUserIdAndIsDefaultTrue(customer.getId())).isTrue();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 3: Lưu UserToken và tìm lại theo tokenHash
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Nên lưu UserToken và tìm lại được theo tokenHash")
    void shouldSaveAndFindUserTokenByHash() {
        // Arrange
        String hash = "sha256_hashed_refresh_token_abc123";
        UserToken token = UserToken.builder()
                .user(customer)
                .tokenHash(hash)
                .type("REFRESH")
                .expiresAt(Instant.now().plusSeconds(604800)) // 7 ngày
                .isRevoked(false)
                .build();

        // Act
        userTokenRepository.save(token);
        entityManager.flush();
        entityManager.clear();

        Optional<UserToken> foundToken = userTokenRepository.findByTokenHash(hash);
        Optional<UserToken> activeToken = userTokenRepository
                .findByTokenHashAndIsRevokedFalse(hash);

        // Assert
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getType()).isEqualTo("REFRESH");
        assertThat(foundToken.get().isRevoked()).isFalse();
        assertThat(foundToken.get().getCreatedAt()).isNotNull(); // @PrePersist

        assertThat(activeToken).isPresent();
    }
}
