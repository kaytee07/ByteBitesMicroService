package com.bytebites.restaurantservice.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        int statusCode,
        String message,
        LocalDateTime timestamp
) {}