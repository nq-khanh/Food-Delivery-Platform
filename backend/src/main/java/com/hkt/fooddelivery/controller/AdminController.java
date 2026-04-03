package com.hkt.fooddelivery.controller;

import com.hkt.fooddelivery.repository.ShipperRepository;
import com.hkt.fooddelivery.service.AdminRestaurantService;
import com.hkt.fooddelivery.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final AdminRestaurantService restaurantAdminService;
    private final ShipperRepository shipperRepository;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

}