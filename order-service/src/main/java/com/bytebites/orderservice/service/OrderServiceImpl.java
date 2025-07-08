package com.bytebites.orderservice.service;


import com.bytebites.orderservice.dto.CreateOrderRequest;
import com.bytebites.orderservice.dto.OrderView;
import com.bytebites.orderservice.event.OrderPlacedEvent;
import com.bytebites.orderservice.model.Order;
import com.bytebites.orderservice.dto.mapper.OrderMapper;
import com.bytebites.orderservice.repository.OrderRepository;
import com.bytebites.orderservice.util.CustomUserPrincipal;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;


    @CircuitBreaker(name = "OrderService", fallbackMethod = "fallbackOrder")
    @Retry(name = "orderService")
    public List<OrderView> findAll() {
        log.info("Fetching all orders");
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toView)
                .collect(Collectors.toList());
    }

    public OrderView createOrder(CreateOrderRequest request, UUID userId) {
        log.info("Received order request from userId={} for restaurantId={}", userId, request.restaurantId());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        Order order = orderMapper.toEntity(request, userId);

        Order saved = orderRepository.save(order);
        OrderPlacedEvent event = new OrderPlacedEvent();
        event.setOrderId(saved.getId());
        event.setCustomerId(saved.getCustomerId());
        event.setRestaurantId(saved.getRestaurantId());
        event.setTotalAmount(saved.getTotalAmount());
        event.setStatus(saved.getStatus());
        event.setEmail(principal.getEmail());
        kafkaTemplate.send("orders.placed", event);
        log.info("Order placed successfully. OrderId={}", saved.getId());
        return orderMapper.toView(saved);
    }

    @CircuitBreaker(name = "OrderService", fallbackMethod = "fallbackOrder")
    @Retry(name = "orderService")
    public List<OrderView> findAllOrdersById(UUID userId) {
        log.info("Fetching all orders for userId={}", userId);
        return orderRepository.findByCustomerId(userId)
                .stream()
                .map(orderMapper::toView)
                .collect(Collectors.toList());
    }



    public List<OrderView> fallbackOrder(Throwable ex) {
        log.warn("Fallback triggered for findAll: {}", ex.getMessage());
        return Collections.emptyList();
    }


    public List<OrderView> fallbackOrder(UUID userId, Throwable ex) {
        log.warn("Fallback triggered for user {}: {}", userId, ex.getMessage());
        return Collections.emptyList();
    }

}
