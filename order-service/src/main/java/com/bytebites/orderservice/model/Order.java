package com.bytebites.orderservice.model;
import com.bytebites.orderservice.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    @NotNull
    @Column(nullable = false)
    private UUID customerId;

    @NotNull
    @Column(nullable = false)
    private UUID restaurantId;

    @NotNull
    @Positive
    @Column(nullable = false)
    private BigDecimal totalAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime createdAt;

}



