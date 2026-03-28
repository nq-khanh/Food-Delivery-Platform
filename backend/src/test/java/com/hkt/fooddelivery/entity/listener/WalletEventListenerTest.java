package com.hkt.fooddelivery.entity.listener;

import com.hkt.fooddelivery.entity.*;
import com.hkt.fooddelivery.entity.event.OrderCompletedEvent;
import com.hkt.fooddelivery.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
// Cách 1: Chuyển strictness sang LENIENT để bỏ qua lỗi Unnecessary Stubbing
@MockitoSettings(strictness = Strictness.LENIENT)
class WalletEventListenerTest {

    @Mock

    private WalletRepository walletRepo;

    @InjectMocks
    private WalletEventListener walletEventListener;

    private Order order;
    private User merchantOwner;
    private User shipperUser;
    private Wallet merchantWallet;
    private Wallet shipperWallet;

    @BeforeEach
    void setUp() {
        // Khởi tạo các ID và Object cơ bản (không stubbing ở đây nếu không dùng chung 100%)
        merchantOwner = mock(User.class);
        shipperUser = mock(User.class);

        merchantWallet = spy(new Wallet(merchantOwner));
        shipperWallet = spy(new Wallet(shipperUser));

        order = mock(Order.class);

        // Các stubbing dùng chung cho đa số các test
        lenient().when(merchantOwner.getId()).thenReturn(UUID.randomUUID());
        lenient().when(shipperUser.getId()).thenReturn(UUID.randomUUID());
        lenient().when(order.getOrderCode()).thenReturn("ORD-123");
    }

    @Test
    @DisplayName("Nên cộng tiền cho cả nhà hàng và shipper khi đơn hàng hoàn thành")
    void shouldCreditBothMerchantAndShipperWhenOrderCompleted() {
        // Given
        Restaurant restaurant = mock(Restaurant.class);
        when(restaurant.getOwner()).thenReturn(merchantOwner);
        when(order.getRestaurant()).thenReturn(restaurant);
        when(order.getSubtotal()).thenReturn(new BigDecimal("100.00"));

        Shipper shipper = mock(Shipper.class);
        when(shipper.getUser()).thenReturn(shipperUser);
        when(order.getShipper()).thenReturn(shipper);
        when(order.getShippingFee()).thenReturn(new BigDecimal("15.00"));

        when(walletRepo.findByUserId(merchantOwner.getId())).thenReturn(Optional.of(merchantWallet));
        when(walletRepo.findByUserId(shipperUser.getId())).thenReturn(Optional.of(shipperWallet));

        OrderCompletedEvent event = new OrderCompletedEvent(order);

        // When
        walletEventListener.handleOrderCompleted(event);

        // Then
        verify(merchantWallet).receiveOrderRevenue(eq(order), eq(new BigDecimal("100.00")));
        verify(shipperWallet).receiveOrderRevenue(eq(order), eq(new BigDecimal("15.00")));
        verify(walletRepo, times(2)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Chỉ cộng tiền cho nhà hàng nếu đơn hàng không có shipper")
    void shouldOnlyCreditMerchantIfNoShipper() {
        // Given
        Restaurant restaurant = mock(Restaurant.class);
        when(restaurant.getOwner()).thenReturn(merchantOwner);
        when(order.getRestaurant()).thenReturn(restaurant);
        when(order.getSubtotal()).thenReturn(new BigDecimal("100.00"));

        when(order.getShipper()).thenReturn(null);
        when(walletRepo.findByUserId(merchantOwner.getId())).thenReturn(Optional.of(merchantWallet));

        OrderCompletedEvent event = new OrderCompletedEvent(order);

        // When
        walletEventListener.handleOrderCompleted(event);

        // Then
        verify(merchantWallet).receiveOrderRevenue(any(), any());
        verify(walletRepo, times(1)).save(merchantWallet);
        // Đảm bảo không tìm ví shipper
        verify(walletRepo, never()).findByUserId(shipperUser.getId());
    }

    @Test
    @DisplayName("Không gây lỗi nếu không tìm thấy ví trong database")
    void shouldNotFailIfWalletNotFound() {
        // Given
        Restaurant restaurant = mock(Restaurant.class);
        when(restaurant.getOwner()).thenReturn(merchantOwner);
        when(order.getRestaurant()).thenReturn(restaurant);

        when(walletRepo.findByUserId(any())).thenReturn(Optional.empty());

        OrderCompletedEvent event = new OrderCompletedEvent(order);

        // When
        walletEventListener.handleOrderCompleted(event);

        // Then
        verify(walletRepo, never()).save(any());
    }
}