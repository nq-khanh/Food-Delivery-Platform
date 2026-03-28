package com.hkt.fooddelivery.entity.listener;

import com.hkt.fooddelivery.entity.Order;
import com.hkt.fooddelivery.entity.event.OrderCompletedEvent;
import com.hkt.fooddelivery.repository.WalletRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
public class WalletEventListener {
    private final WalletRepository walletRepo;

    public WalletEventListener(WalletRepository walletRepo) {
        this.walletRepo = walletRepo;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderCompleted(OrderCompletedEvent event) {
        Order order = event.order();

        // 1. Xử lý ví Nhà hàng (Dùng Optional để tránh NullPointerException)
        if (order.getRestaurant() != null && order.getRestaurant().getOwner() != null) {
            walletRepo.findByUserId(order.getRestaurant().getOwner().getId())
                    .ifPresent(wallet -> {
                        wallet.receiveOrderRevenue(order, order.getSubtotal());
                        walletRepo.save(wallet);
                    });
        }

        // 2. Xử lý ví Shipper (nếu có)
        if (order.getShipper() != null && order.getShipper().getUser() != null) {
            walletRepo.findByUserId(order.getShipper().getUser().getId())
                    .ifPresent(wallet -> {
                        wallet.receiveOrderRevenue(order, order.getShippingFee());
                        walletRepo.save(wallet);
                    });
        }
    }
}