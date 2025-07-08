package com.bytebites.orderservice.dto;

import com.bytebites.orderservice.enums.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;


public record CreateOrderRequest(
        @NotNull(message = "Restaurant ID is required")
        UUID restaurantId,

        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal totalAmount,

        @NotNull(message = "Order status is required")
        OrderStatus status
) {}

