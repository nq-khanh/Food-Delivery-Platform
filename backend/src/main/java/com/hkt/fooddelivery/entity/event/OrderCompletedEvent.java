package com.hkt.fooddelivery.entity.event;

import com.hkt.fooddelivery.entity.Order;

public record OrderCompletedEvent(Order order) {}