package com.hkt.fooddelivery.entity;

public record ReviewItemCommand(
        Product product,
        int rating,
        String comment,
        String imageUrl
) {}
