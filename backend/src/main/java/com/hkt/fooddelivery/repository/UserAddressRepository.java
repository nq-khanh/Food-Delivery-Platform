package com.hkt.fooddelivery.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hkt.fooddelivery.entity.UserAddress;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    List<UserAddress> findByUserId(UUID userId);

    Optional<UserAddress> findByUserIdAndIsDefaultTrue(UUID userId);

    boolean existsByUserIdAndIsDefaultTrue(UUID userId);
}
