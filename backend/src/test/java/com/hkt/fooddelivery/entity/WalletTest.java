package com.hkt.fooddelivery.entity;

import com.hkt.fooddelivery.entity.enums.PayoutRequestStatus;
import com.hkt.fooddelivery.entity.enums.WalletTransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletTest {

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        wallet = new Wallet(user);
    }

    @Test
    @DisplayName("Nên khởi tạo ví với số dư bằng 0")
    void shouldInitializeWithZeroBalance() {
        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(wallet.getTransactions()).isEmpty();
    }

    @Test
    @DisplayName("Nên cộng tiền thành công và ghi lại giao dịch")
    void shouldCreditSuccessfully() {
        // Given
        Order order = mock(Order.class);
        when(order.getOrderCode()).thenReturn("ORD-100");
        BigDecimal creditAmount = new BigDecimal("150.50");

        // When
        wallet.credit(creditAmount, order, "Test credit");

        // Then
        assertThat(wallet.getBalance()).isEqualByComparingTo("150.50");
        assertThat(wallet.getTransactions()).hasSize(1);

        WalletTransaction tx = wallet.getTransactions().get(0);
        assertThat(tx.getAmount()).isEqualByComparingTo("150.50");
        assertThat(tx.getType()).isEqualTo(WalletTransactionType.ORDER_REVENUE);
        assertThat(tx.getOrder()).isEqualTo(order);
    }

    @Test
    @DisplayName("Nên trừ tiền rút (Payout) và trả về đối tượng Transaction")
    void shouldDebitForPayoutSuccessfully() {
        // Given
        ReflectionTestUtils.setField(wallet, "balance", new BigDecimal("1000.00"));
        PayoutRequest request = mock(PayoutRequest.class);
        UUID requestId = UUID.randomUUID();
        when(request.getId()).thenReturn(requestId);
        BigDecimal payoutAmount = new BigDecimal("200.00");

        // When
        WalletTransaction tx = wallet.debitForPayout(payoutAmount, request);

        // Then
        assertThat(wallet.getBalance()).isEqualByComparingTo("800.00");
        assertThat(tx.getAmount()).isEqualByComparingTo("-200.00");
        assertThat(tx.getType()).isEqualTo(WalletTransactionType.WITHDRAWAL);
        assertThat(tx.getDescription()).contains(requestId.toString());
    }

    @Test
    @DisplayName("Nên ném lỗi khi trừ tiền quá số dư khả dụng")
    void shouldThrowExceptionWhenInsufficientBalance() {
        // Given
        ReflectionTestUtils.setField(wallet, "balance", new BigDecimal("50.00"));
        BigDecimal debitAmount = new BigDecimal("100.00");

        // When & Then
        assertThatThrownBy(() -> wallet.debit(debitAmount, WalletTransactionType.WITHDRAWAL, "Withdraw"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    @DisplayName("Nên làm tròn số dư đến 2 chữ số thập phân")
    void shouldNormalizeBalanceScale() {
        // Given
        BigDecimal complexAmount = new BigDecimal("100.12345"); // 100.12

        // When
        wallet.credit(complexAmount, mock(Order.class), "Normalization test");

        // Then
        // Scale sẽ là 2 do hàm normalize() trong Wallet sử dụng HALF_UP
        assertThat(wallet.getBalance().scale()).isEqualTo(2);
        assertThat(wallet.getBalance()).isEqualByComparingTo("100.12");
    }

    @Test
    @DisplayName("Nên cập nhật thông tin ngân hàng hợp lệ")
    void shouldUpdateBankInfo() {
        wallet.updateBankInfo("Vietcombank", "123456789", "NGUYEN VAN A");

        assertThat(wallet.getBankName()).isEqualTo("Vietcombank");
        assertThat(wallet.getBankAccountNumber()).isEqualTo("123456789");
        assertThat(wallet.getBankAccountHolder()).isEqualTo("NGUYEN VAN A");
    }

    @Test
    @DisplayName("Nên ném lỗi nếu cập nhật thông tin ngân hàng trống")
    void shouldThrowExceptionWhenBankInfoIsBlank() {
        assertThatThrownBy(() -> wallet.updateBankInfo(" ", "123", "Owner"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Quy trình phối hợp: Ví tạo transaction và Request xác nhận approved")
    void shouldWorkTogetherInPayoutProcess() {
        // 1. Setup
        ReflectionTestUtils.setField(wallet, "balance", new BigDecimal("500.00"));
        // Cần ID thật để pass check equality
        UUID walletId = UUID.randomUUID();
        ReflectionTestUtils.setField(wallet, "id", walletId);

        PayoutRequest request = PayoutRequest.create(wallet, new BigDecimal("100.00"));

        // 2. Wallet thực hiện trừ tiền và tạo TX
        WalletTransaction tx = wallet.debitForPayout(new BigDecimal("100.00"), request);

        // 3. Request nhận TX để hoàn tất
        request.markApproved(tx);

        // 4. Verify
        assertThat(wallet.getBalance()).isEqualByComparingTo("400.00");
        assertThat(request.getStatus()).isEqualTo(PayoutRequestStatus.APPROVED);
        assertThat(request.getTransaction()).isEqualTo(tx);
    }
}