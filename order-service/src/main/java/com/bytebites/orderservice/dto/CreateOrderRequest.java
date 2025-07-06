package com.bytebites.orderservice.dto;

import com.bytebites.orderservice.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;


public record CreateOrderRequest(
        UUID restaurantId,
        BigDecimal totalAmount,
        OrderStatus status
) {}
