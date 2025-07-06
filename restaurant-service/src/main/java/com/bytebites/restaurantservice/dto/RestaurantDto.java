
package com.bytebites.restaurantservice.dto;

import java.util.List;
import java.util.UUID;

public record RestaurantDto(
        UUID id,
        String name,
        String address,
        UUID ownerId,
        List<MenuItemDto> menuItems
) {}