package com.bytebites.orderservice.controller;
import com.bytebites.orderservice.dto.CreateOrderRequest;
import com.bytebites.orderservice.dto.OrderView;
import com.bytebites.orderservice.service.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;



@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderServiceImpl orderService;

    @GetMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<OrderView>> getAll(){
        return ResponseEntity.ok(orderService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<OrderView> create(
            @RequestBody CreateOrderRequest request,
            @RequestHeader("X-User-ID") UUID userId) {
        OrderView createdOrder = orderService.createOrder(request, userId);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping("/myorder")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<OrderView>> getMyOrders(@RequestHeader("X-User-ID") UUID ownerId) {
        List<OrderView> orders = orderService.findAllOrdersById(ownerId);
        return ResponseEntity.ok(orders);
    }

}