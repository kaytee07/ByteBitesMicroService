package com.bytebites.restaurantservice.listener;

import com.bytebites.restaurantservice.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaOrderEventListener {

    @KafkaListener(
            topics = "orders.placed",
            groupId = "restaurant-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Restaurant-Service: Received OrderPlacedEvent for Order ID: {}", event.getOrderId());
        log.info("Informing restaurant {} to start preparing the order.", event.getRestaurantId());
    }
}