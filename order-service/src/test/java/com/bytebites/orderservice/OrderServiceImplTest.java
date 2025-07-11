package com.bytebites.orderservice;

import com.bytebites.orderservice.dto.CreateOrderRequest;
import com.bytebites.orderservice.dto.OrderView;
import com.bytebites.orderservice.model.Order;
import com.bytebites.orderservice.enums.OrderStatus;
import com.bytebites.orderservice.event.OrderPlacedEvent;
import com.bytebites.orderservice.dto.mapper.OrderMapper;
import com.bytebites.orderservice.repository.OrderRepository;
import com.bytebites.orderservice.service.OrderServiceImpl;
import com.bytebites.orderservice.util.CustomUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID userId;
    private UUID orderId;
    private UUID restaurantId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();
    }

    @Test
    void findAll_shouldReturnMappedOrders() {
        Order order = new Order();
        OrderView view = new OrderView(orderId, userId, restaurantId, new BigDecimal("100.00"), OrderStatus.CONFIRMED.name());

        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderMapper.toView(order)).thenReturn(view);

        List<OrderView> result = orderService.findAll();

        assertEquals(1, result.size());
        assertEquals(view, result.get(0));

        verify(orderRepository).findAll();
        verify(orderMapper).toView(order);
    }

    @Test
    void findAllOrdersById_shouldReturnUserOrders() {
        Order order = new Order();
        order.setCustomerId(userId);

        OrderView view = new OrderView(orderId, userId, restaurantId, new BigDecimal("100.00"), OrderStatus.CONFIRMED.name());

        when(orderRepository.findByCustomerId(userId)).thenReturn(List.of(order));
        when(orderMapper.toView(order)).thenReturn(view);

        List<OrderView> result = orderService.findAllOrdersById(userId);

        assertEquals(1, result.size());
        assertEquals(view, result.get(0));

        verify(orderRepository).findByCustomerId(userId);
        verify(orderMapper).toView(order);
    }

    @Test
    void createOrder_shouldSaveOrderAndSendKafkaEvent() {
        CreateOrderRequest request = new CreateOrderRequest(
                restaurantId,
                new BigDecimal("100.00"),
                OrderStatus.CONFIRMED
        );

        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(userId);
        order.setRestaurantId(restaurantId);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setStatus(OrderStatus.CONFIRMED);

        OrderView view = new OrderView(orderId, userId, restaurantId, new BigDecimal("100.00"), OrderStatus.CONFIRMED.name());

        when(orderMapper.toEntity(request, userId)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toView(order)).thenReturn(view);

        // Mock Security Context
        CustomUserPrincipal principal = new CustomUserPrincipal(userId.toString(), "user@example.com");
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        OrderView result = orderService.createOrder(request, userId);

        assertNotNull(result);
        assertEquals(view, result);

        verify(orderRepository).save(order);
        verify(kafkaTemplate).send(eq("orders.placed"), any(OrderPlacedEvent.class));
    }

    @Test
    void createOrder_shouldThrowException_whenSaveFails() {
        CreateOrderRequest request = new CreateOrderRequest(
                restaurantId,
                new BigDecimal("100.00"),
                OrderStatus.CONFIRMED
        );

        Order order = new Order();
        when(orderMapper.toEntity(request, userId)).thenReturn(order);
        when(orderRepository.save(order)).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> orderService.createOrder(request, userId));
    }

    @Test
    void createOrder_shouldSendCorrectKafkaEvent() {
        CreateOrderRequest request = new CreateOrderRequest(
                restaurantId,
                new BigDecimal("100.00"),
                OrderStatus.CONFIRMED
        );

        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(userId);
        order.setRestaurantId(restaurantId);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setStatus(OrderStatus.CONFIRMED);

        OrderView view = new OrderView(orderId, userId, restaurantId, new BigDecimal("100.00"), OrderStatus.CONFIRMED.name());

        when(orderMapper.toEntity(request, userId)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toView(order)).thenReturn(view);

        // Security context
        CustomUserPrincipal principal = new CustomUserPrincipal(userId.toString(), "user@example.com");
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        orderService.createOrder(request, userId);

        ArgumentCaptor<OrderPlacedEvent> captor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(kafkaTemplate).send(eq("orders.placed"), captor.capture());

        OrderPlacedEvent event = captor.getValue();
        assertEquals(orderId, event.getOrderId());
        assertEquals(userId, event.getCustomerId());
        assertEquals(new BigDecimal("100.00"), event.getTotalAmount());
    }

    @Test
    void fallbackOrder_shouldReturnEmptyList_whenNoUserId() {
        List<OrderView> fallback = orderService.fallbackFindAll(new RuntimeException("Service failure"));
        assertTrue(fallback.isEmpty());
    }

    @Test
    void fallbackOrder_shouldReturnEmptyList_whenUserIdProvided() {
        List<OrderView> fallback = orderService.fallbackFindByUser(userId, new RuntimeException("Timeout"));
        assertTrue(fallback.isEmpty());
    }
}
