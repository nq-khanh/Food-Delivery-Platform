package com.hkt.fooddelivery.mapper;

import com.hkt.fooddelivery.dto.RestaurantResponse;
import com.hkt.fooddelivery.entity.Restaurant;
import org.springframework.stereotype.Component;

@Component
public class RestaurantMapper {

    public RestaurantResponse toResponse(Restaurant res) {
        if (res == null) return null;

        Double lat = null;
        Double lng = null;

        if (res.getLocation() != null) {
            lat = res.getLocation().getY();
            lng = res.getLocation().getX();
        }

        return new RestaurantResponse(
                res.getId(),
                res.getName(),
                res.getAddress(),
                lat,
                lng,
                res.getDescription(),
                res.getLogoUrl(),
                res.getApprovalStatus(),
                res.getRatingAvg(),
                res.getReviewCount(),
                res.isActive(),
                res.getCreatedAt(),
                mapOwner(res)
        );
    }

    private RestaurantResponse.OwnerInfo mapOwner(Restaurant res) {
        if (res.getOwner() == null) return null;

        var o = res.getOwner();

        String fullName = ((o.getFirstName() != null ? o.getFirstName() : "") +
                " " +
                (o.getLastName() != null ? o.getLastName() : "")).trim();

        return new RestaurantResponse.OwnerInfo(
                o.getId(),
                o.getUsername(),
                o.getEmail(),
                fullName,
                o.getPhone()
        );
    }
}