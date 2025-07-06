package com.bytebites.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;


public record CreateOrderRequest(
        UUID restaurantId,
        BigDecimal totalAmount
) {}
