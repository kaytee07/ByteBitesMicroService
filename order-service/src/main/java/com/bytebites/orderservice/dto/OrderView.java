package com.bytebites.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderView(
        UUID id,
        UUID customerId,
        UUID restaurantId,
        BigDecimal totalAmount,
        String status
) {
}
