package com.hkt.fooddelivery.service;

import com.hkt.fooddelivery.dto.VoucherRequest;
import com.hkt.fooddelivery.dto.VoucherResponse;
import com.hkt.fooddelivery.entity.Voucher;
import com.hkt.fooddelivery.exception.BusinessException;
import com.hkt.fooddelivery.exception.DuplicateResourceException;
import com.hkt.fooddelivery.exception.ResourceNotFoundException;
import com.hkt.fooddelivery.mapper.VoucherMapper;
import com.hkt.fooddelivery.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository repo;
    private final VoucherMapper mapper;

    public VoucherResponse apply(String code, java.math.BigDecimal orderAmount) {
        Voucher v = repo.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại"));

        if (!v.canUse(orderAmount)) {
            throw new BusinessException("Voucher không thể sử dụng");
        }

        v.markUsed();

        return mapper.toResponse(v);
    }

    public BigDecimal calculateDiscountPreview(String code, BigDecimal amount) {
        Voucher v = repo.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại"));

        if (!v.canUse(amount)) {
            throw new BusinessException("Đơn hàng chưa đủ điều kiện áp dụng mã này");
        }

        return v.calculateDiscount(amount);
    }

    public VoucherResponse validateVoucher(String code, BigDecimal amount) {
        Voucher v = repo.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại"));

        if (!v.canUse(amount)) {
            throw new BusinessException("Voucher không hợp lệ");
        }

        return mapper.toResponse(v);
    }

    public BigDecimal applyDiscount(String code, BigDecimal amount) {
        Voucher v = repo.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại"));

        BigDecimal discount = v.calculateDiscount(amount);
        v.markUsed();

        return discount;
    }
}