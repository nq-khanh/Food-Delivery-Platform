package com.hkt.fooddelivery.repository;

import com.hkt.fooddelivery.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VoucherRepository extends JpaRepository<Voucher, UUID> {

    Optional<Voucher> findByCode(String code);

    boolean existsByCode(String code);
}