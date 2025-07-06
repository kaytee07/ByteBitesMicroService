package com.bytebites.restaurantservice.dto;

import java.util.UUID;

public record MenuItemDto(
        UUID id,
        String name,
        double price
) {}
