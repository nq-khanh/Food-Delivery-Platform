package com.hkt.fooddelivery.repository;

import java.util.Optional;
import java.util.UUID;

import com.hkt.fooddelivery.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hkt.fooddelivery.entity.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUserId(UUID userId);
    Optional<Wallet> findByUser(User user);

    boolean existsByUserId(UUID userId);
}
