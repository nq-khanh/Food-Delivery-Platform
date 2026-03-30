package com.hkt.fooddelivery.mapper;

import com.hkt.fooddelivery.dto.ShipperResponse;
import com.hkt.fooddelivery.entity.Shipper;
import org.springframework.stereotype.Component;

@Component
public class ShipperMapper {

    public ShipperResponse toResponse(Shipper s) {
        if (s == null) return null;

        Double lat = null;
        Double lng = null;

        if (s.getLocation() != null) {
            lat = s.getLocation().getY();
            lng = s.getLocation().getX();
        }

        return new ShipperResponse(
                s.getId(),
                s.getVehicleInfo(),
                s.getLicensePlate(),
                s.isOnline(),
                s.isBusy(),
                s.getApprovalStatus(),
                lat,
                lng,
                s.getRatingAvg(),
                s.getReviewCount(),
                s.getUpdatedAt(),
                mapUser(s)
        );
    }

    private ShipperResponse.UserInfo mapUser(Shipper s) {
        var u = s.getUser();
        if (u == null) return null;

        String fullName = ((u.getFirstName() != null ? u.getFirstName() : "") +
                " " +
                (u.getLastName() != null ? u.getLastName() : "")).trim();

        return new ShipperResponse.UserInfo(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                fullName,
                u.getPhone()
        );
    }
}