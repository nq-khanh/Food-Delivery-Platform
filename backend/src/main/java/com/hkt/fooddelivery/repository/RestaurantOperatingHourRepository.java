package com.hkt.fooddelivery.repository;

import java.util.List;
import java.util.UUID;

import com.hkt.fooddelivery.entity.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hkt.fooddelivery.entity.RestaurantOperatingHour;

public interface RestaurantOperatingHourRepository extends JpaRepository<RestaurantOperatingHour, Integer> {

    List<RestaurantOperatingHour> findByRestaurantId(UUID restaurantId);

    List<RestaurantOperatingHour> findByRestaurantIdAndDayOfWeek(UUID restaurantId, DayOfWeek dayOfWeek);
}
