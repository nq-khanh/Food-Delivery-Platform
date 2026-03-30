package com.hkt.fooddelivery.service;

import com.hkt.fooddelivery.dto.ShipperResponse;
import com.hkt.fooddelivery.entity.Shipper;
import com.hkt.fooddelivery.entity.enums.ApprovalStatus;
import com.hkt.fooddelivery.exception.ResourceNotFoundException; // Sử dụng Custom Exception
import com.hkt.fooddelivery.mapper.ShipperMapper;
import com.hkt.fooddelivery.repository.ShipperRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipperAdminService {

    private final ShipperRepository repository;
    private final ShipperMapper mapper;

    private Shipper getShipper(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Shipper với ID: " + id));
    }

    @Transactional
    public ShipperResponse approve(UUID id) {
        Shipper s = getShipper(id);
        s.approve();
        return mapper.toResponse(repository.save(s));
    }

    @Transactional
    public ShipperResponse reject(UUID id) {
        Shipper s = getShipper(id);
        s.reject();
        return mapper.toResponse(repository.save(s));
    }

    public List<ShipperResponse> getPending() {
        return repository.findByApprovalStatusWithUser(ApprovalStatus.PENDING)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}