package com.bytebites.emailservice.listener;

import com.bytebites.emailservice.event.OrderPlacedEvent;
import com.bytebites.emailservice.service.EmailService;
import com.bytebites.emailservice.util.CustomUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventListener {

    @Autowired
    private EmailService emailService;

    @KafkaListener(
            topics = "orders.placed",
            groupId = "email-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Received OrderPlacedEvent: {}", event.getOrderId());


        String customerEmail = event.getEmail();

        String subject = "Your ByteBites Order #" + event.getOrderId() + " is Confirmed!";
        String body = "Dear Customer,\n\n" +
                "Thank you for your order! Your order with ID " + event.getOrderId() +
                " for a total of $" + event.getTotalAmount() + " has been placed.\n\n" +
                "We'll notify you when it's on its way.\n\n" +
                "Thanks for choosing ByteBites!";

        emailService.sendOrderConfirmationEmail(customerEmail, subject, body);
    }
}
