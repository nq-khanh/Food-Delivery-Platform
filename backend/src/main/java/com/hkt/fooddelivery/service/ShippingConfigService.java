package com.hkt.fooddelivery.service;

import com.hkt.fooddelivery.dto.ShippingConfigRequest;
import com.hkt.fooddelivery.dto.ShippingConfigResponse;
import com.hkt.fooddelivery.entity.ShippingConfig;
import com.hkt.fooddelivery.exception.BusinessException;
import com.hkt.fooddelivery.exception.ResourceNotFoundException;
import com.hkt.fooddelivery.mapper.ShippingConfigMapper;
import com.hkt.fooddelivery.repository.ShippingConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingConfigService {

    private final ShippingConfigRepository repo;
    private final ShippingConfigMapper mapper;

    // CREATE
    public ShippingConfigResponse create(ShippingConfigRequest req) {
        validateTime(req.activeFrom(), req.activeTo());
        validateConflict(null, req);

        ShippingConfig config = new ShippingConfig(
                req.configName(),
                req.baseFee(),
                req.baseDistanceKm(),
                req.feePerKm(),
                req.priority()
        );

        config.activate();

        if (req.activeFrom() != null) config.activate();
        if (req.activeTo() != null) config.deactivate(); // optional logic

        return mapper.toResponse(repo.save(config));
    }

    // GET BY ID
    public ShippingConfigResponse getById(Integer id) {
        ShippingConfig config = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Config không tồn tại"));
        return mapper.toResponse(config);
    }

    // PAGINATION
    public Page<ShippingConfigResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("priority").descending());

        return repo.findAll(pageable)
                .map(mapper::toResponse);
    }

    // UPDATE
    public ShippingConfigResponse update(Integer id, ShippingConfigRequest req) {
        ShippingConfig config = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Config không tồn tại"));

        validateTime(req.activeFrom(), req.activeTo());
        validateConflict(id, req);

        config.rename(req.configName());
        config.updatePricing(req.baseFee(), req.baseDistanceKm(), req.feePerKm());
        config.changePriority(req.priority());

        return mapper.toResponse(repo.save(config));
    }

    // DELETE
    public void delete(Integer id) {
        ShippingConfig config = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Config không tồn tại"));

        if (config.isDefault()) {
            throw new BusinessException("Không thể xoá default config");
        }

        repo.delete(config);
    }

    // ACTIVATE / DEACTIVATE
    public void activate(Integer id) {
        ShippingConfig config = getEntity(id);
        config.activate();
    }

    public void deactivate(Integer id) {
        ShippingConfig config = getEntity(id);
        config.deactivate();
    }

    public ShippingConfigResponse getCurrent() {
        ShippingConfig config = repo.findAll().stream()
                .filter(c -> c.isEffectiveAt(Instant.now()))
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("No shipping config"));

        return mapper.toResponse(config);
    }

    public BigDecimal calculateFee(BigDecimal distanceKm) {
        ShippingConfig c = getCurrentEntity();

        if (distanceKm.compareTo(c.getBaseDistanceKm()) <= 0) {
            return c.getBaseFee();
        }

        BigDecimal extraKm = distanceKm.subtract(c.getBaseDistanceKm());

        return c.getBaseFee().add(
                extraKm.multiply(c.getFeePerKm())
        );
    }

    // ================= PRIVATE =================

    private ShippingConfig getEntity(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Config không tồn tại"));
    }

    private void validateTime(Instant from, Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException("activeFrom phải <= activeTo");
        }
    }

    private void validateConflict(Integer currentId, ShippingConfigRequest req) {
        List<ShippingConfig> configs = repo.findByPriorityAndIsActiveTrue(req.priority());

        for (ShippingConfig c : configs) {
            if (currentId != null && c.getId().equals(currentId)) continue;

            boolean overlap =
                    (req.activeFrom() == null || c.getActiveTo() == null || !req.activeFrom().isAfter(c.getActiveTo()))
                            &&
                            (req.activeTo() == null || c.getActiveFrom() == null || !req.activeTo().isBefore(c.getActiveFrom()));

            if (overlap) {
                throw new BusinessException("Config bị trùng thời gian và priority");
            }
        }
    }

    private ShippingConfig getCurrentEntity() {
        Instant now = Instant.now();

        return repo.findActiveConfigs(now)
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("Không có shipping config hợp lệ"));
    }
}