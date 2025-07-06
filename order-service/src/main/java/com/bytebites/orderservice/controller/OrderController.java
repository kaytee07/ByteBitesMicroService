package com.bytebites.orderservice.controller;
import com.bytebites.orderservice.dto.CreateOrderRequest;
import com.bytebites.orderservice.dto.OrderView;
import com.bytebites.orderservice.service.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.UUID;



@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderServiceImpl orderService;

    @Operation(
            summary = "Get all orders",
            description = "Only restaurant owners can fetch all orders",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval"),
                    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
            }
    )
    @GetMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<OrderView>> getAllOrders(){
        return ResponseEntity.ok(orderService.findAll());
    }

    @Operation(
            summary = "Create a new order",
            description = "Allows customers to place a new order",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Order created"),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
            }
    )
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderView> create(
            @RequestBody CreateOrderRequest request,
            @RequestHeader("X-User-ID") UUID userId) {
        OrderView createdOrder = orderService.createOrder(request, userId);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get my orders",
            description = "Fetch all orders for the currently logged-in customer",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Orders retrieved")
            }
    )
    @GetMapping("/myorder")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<OrderView>> getMyOrders(@RequestHeader("X-User-ID") UUID ownerId) {
        List<OrderView> orders = orderService.findAllOrdersById(ownerId);
        return ResponseEntity.ok(orders);
    }

}