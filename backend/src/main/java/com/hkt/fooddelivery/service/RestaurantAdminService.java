package com.hkt.fooddelivery.service;

import com.hkt.fooddelivery.dto.RestaurantResponse;
import com.hkt.fooddelivery.entity.Restaurant;
import com.hkt.fooddelivery.entity.enums.ApprovalStatus;
import com.hkt.fooddelivery.exception.ResourceNotFoundException;
import com.hkt.fooddelivery.mapper.RestaurantMapper;
import com.hkt.fooddelivery.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantAdminService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper mapper;

    private Restaurant getRestaurant(UUID id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà hàng với ID: " + id));
    }

    @Transactional
    public RestaurantResponse approve(UUID id) {
        Restaurant r = getRestaurant(id);
        r.approve();
        return mapper.toResponse(restaurantRepository.save(r));
    }

    @Transactional
    public RestaurantResponse reject(UUID id) {
        Restaurant r = getRestaurant(id);
        r.reject();
        return mapper.toResponse(restaurantRepository.save(r));
    }

    @Transactional
    public RestaurantResponse activate(UUID id) {
        Restaurant r = getRestaurant(id);
        r.activate();
        return mapper.toResponse(restaurantRepository.save(r));
    }

    @Transactional
    public RestaurantResponse deactivate(UUID id) {
        Restaurant r = getRestaurant(id);
        r.deactivate();
        return mapper.toResponse(restaurantRepository.save(r));
    }

    public List<RestaurantResponse> getPendingRestaurants() {
        return restaurantRepository.findByApprovalStatusWithOwner(ApprovalStatus.PENDING)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}