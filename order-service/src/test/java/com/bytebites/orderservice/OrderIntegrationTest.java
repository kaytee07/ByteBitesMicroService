package com.bytebites.orderservice;

import com.bytebites.orderservice.dto.CreateOrderRequest;
import com.bytebites.orderservice.enums.OrderStatus;
import com.bytebites.orderservice.event.OrderPlacedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class OrderIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    private final UUID customerId = UUID.randomUUID();
    private final UUID restaurantOwnerId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();

    private final String customerEmail = "test@customer.com";
    private final String ownerEmail = "admin@restaurant.com";

    @Test
    void testCreateOrder() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
                restaurantId,
                new BigDecimal("25.50"),
                OrderStatus.PENDING
        );

        mockMvc.perform(post("/api/orders")
                        .header("X-User-ID", customerId.toString())
                        .header("X-User-Email", customerEmail)
                        .header("X-User-Roles", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.restaurantId").value(restaurantId.toString()))
                .andExpect(jsonPath("$.totalAmount", comparesEqualTo(25.50))) // <-- Fix here
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testGetAllOrdersAsOwner() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .header("X-User-ID", restaurantOwnerId.toString())
                        .header("X-User-Email", ownerEmail)
                        .header("X-User-Roles", "RESTAURANT_OWNER"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMyOrders() throws Exception {
        mockMvc.perform(get("/api/orders/myorder")
                        .header("X-User-ID", customerId.toString())
                        .header("X-User-Email", customerEmail)
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk());
    }
}

