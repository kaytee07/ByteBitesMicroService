package com.bytebites.emailservice.event;

import com.bytebites.emailservice.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlacedEvent {
    private UUID orderId;
    private UUID customerId;
    private UUID restaurantId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String email;
}
