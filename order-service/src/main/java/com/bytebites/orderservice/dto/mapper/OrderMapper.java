package com.bytebites.orderservice.dto.mapper;


import com.bytebites.orderservice.dto.CreateOrderRequest;
import com.bytebites.orderservice.dto.OrderView;
import com.bytebites.orderservice.model.Order;
import com.bytebites.orderservice.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class OrderMapper {

    public Order toEntity(CreateOrderRequest request, UUID customerId) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setRestaurantId(request.restaurantId());
        order.setTotalAmount(request.totalAmount());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    public OrderView toView(Order order) {
        return new OrderView(
                order.getId(),
                order.getCustomerId(),
                order.getRestaurantId(),
                order.getTotalAmount(),
                order.getStatus().name()
        );
    }
}
