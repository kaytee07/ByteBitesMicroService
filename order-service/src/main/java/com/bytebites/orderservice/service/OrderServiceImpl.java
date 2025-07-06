package com.bytebites.orderservice.service;


import com.bytebites.orderservice.dto.CreateOrderRequest;
import com.bytebites.orderservice.dto.OrderView;
import com.bytebites.orderservice.event.OrderPlacedEvent;
import com.bytebites.orderservice.model.Order;
import com.bytebites.orderservice.dto.mapper.OrderMapper;
import com.bytebites.orderservice.repository.OrderRepository;
import com.bytebites.orderservice.util.CustomUserPrincipal;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;


    public List<OrderView> findAll() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toView)
                .collect(Collectors.toList());
    }

    public OrderView createOrder(CreateOrderRequest request, UUID userId) {
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
        return orderMapper.toView(saved);
    }

    public List<OrderView> findAllOrdersById(UUID userId) {
        return orderRepository.findByCustomerId(userId)
                .stream()
                .map(orderMapper::toView)
                .collect(Collectors.toList());
    }
}
