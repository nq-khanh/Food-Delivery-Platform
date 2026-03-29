package com.hkt.fooddelivery.entity;

import com.hkt.fooddelivery.entity.enums.PayoutRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PayoutRequestTest {

    private Wallet wallet;
    private User user;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        wallet = spy(new Wallet(user));
        // Thiết lập ID cho wallet để vượt qua check .equals() trong markApproved
        ReflectionTestUtils.setField(wallet, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(wallet, "balance", new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("Nên khởi tạo yêu cầu rút tiền bằng static method create")
    void shouldCreatePayoutRequestUsingStaticMethod() {
        PayoutRequest request = PayoutRequest.create(wallet, new BigDecimal("500.00"));

        assertThat(request.getStatus()).isEqualTo(PayoutRequestStatus.PENDING);
        assertThat(request.getAmount()).isEqualByComparingTo("500.00");
        assertThat(request.getWallet()).isEqualTo(wallet);
    }

    @Test
    @DisplayName("Nên chuyển trạng thái APPROVED khi nhận transaction hợp lệ")
    void shouldMarkApprovedSuccessfully() {
        // Given
        PayoutRequest request = PayoutRequest.create(wallet, new BigDecimal("200.00"));

        // Giả lập một transaction được tạo từ ví
        WalletTransaction tx = mock(WalletTransaction.class);
        when(tx.getWallet()).thenReturn(wallet);

        // When
        request.markApproved(tx);

        // Then
        assertThat(request.getStatus()).isEqualTo(PayoutRequestStatus.APPROVED);
        assertThat(request.getTransaction()).isEqualTo(tx);
    }

    @Test
    @DisplayName("Nên ném lỗi nếu transaction truyền vào không thuộc về ví của request")
    void shouldThrowExceptionWhenTransactionWalletMismatch() {
        // Given
        PayoutRequest request = PayoutRequest.create(wallet, new BigDecimal("200.00"));

        Wallet otherWallet = mock(Wallet.class);
        WalletTransaction tx = mock(WalletTransaction.class);
        when(tx.getWallet()).thenReturn(otherWallet);

        // When & Then
        assertThatThrownBy(() -> request.markApproved(tx))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transaction does not belong to this wallet");
    }

    @Test
    @DisplayName("Nên từ chối và lưu lại lý do")
    void shouldRejectSuccessfully() {
        PayoutRequest request = PayoutRequest.create(wallet, new BigDecimal("100.00"));
        String reason = "Sai thông tin tài khoản";

        request.reject(reason);

        assertThat(request.getStatus()).isEqualTo(PayoutRequestStatus.REJECTED);
        assertThat(request.getNote()).isEqualTo(reason);
    }

    @Test
    @DisplayName("Không được phê duyệt nếu request không còn ở trạng thái PENDING")
    void shouldThrowExceptionWhenMarkingApprovedOnNonPendingRequest() {
        PayoutRequest request = PayoutRequest.create(wallet, new BigDecimal("100.00"));
        request.reject("Reason");

        WalletTransaction tx = mock(WalletTransaction.class);
        when(tx.getWallet()).thenReturn(wallet);

        assertThatThrownBy(() -> request.markApproved(tx))
                .isInstanceOf(IllegalStateException.class);
    }
}