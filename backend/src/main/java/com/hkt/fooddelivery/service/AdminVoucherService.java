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
public class AdminVoucherService {

    private final VoucherRepository repo;
    private final VoucherMapper mapper;

    // CREATE
    public VoucherResponse create(VoucherRequest req) {
        if (repo.existsByCode(req.code())) {
            throw new DuplicateResourceException("Voucher code đã tồn tại");
        }

        validate(req);

        Voucher v = new Voucher(
                req.code(),
                req.discountType(),
                req.discountValue(),
                req.expiryDate()
        );

        // optional fields
        if (req.targetType() != null) {
            // nếu muốn cho set
        }

        return mapper.toResponse(repo.save(v));
    }

    // GET BY ID
    public VoucherResponse getById(UUID id) {
        return mapper.toResponse(getEntity(id));
    }

    // GET BY CODE (rất quan trọng cho user apply)
    public VoucherResponse getByCode(String code) {
        Voucher v = repo.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại"));

        return mapper.toResponse(v);
    }

    // PAGINATION
    public Page<VoucherResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return repo.findAll(pageable)
                .map(mapper::toResponse);
    }

    // UPDATE
    public VoucherResponse update(UUID id, VoucherRequest req) {
        Voucher v = getEntity(id);

        validate(req);

        // ❗ Không cho đổi code (best practice)
        // nếu muốn cho đổi thì phải check unique lại

        v.activate(); // optional

        return mapper.toResponse(repo.save(v));
    }

    // DELETE
    public void delete(UUID id) {
        Voucher v = getEntity(id);
        repo.delete(v);
    }

    // ACTIVATE / DEACTIVATE
    public void activate(UUID id) {
        Voucher v = getEntity(id);
        v.activate();
    }

    public void deactivate(UUID id) {
        Voucher v = getEntity(id);
        v.deactivate();
    }

    // ================= PRIVATE =================

    private Voucher getEntity(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại"));
    }

    private void validate(VoucherRequest req) {
        if (req.expiryDate().isBefore(Instant.now())) {
            throw new BusinessException("Expiry date phải ở tương lai");
        }
    }
}