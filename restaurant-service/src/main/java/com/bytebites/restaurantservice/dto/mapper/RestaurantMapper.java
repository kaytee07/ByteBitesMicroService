package com.bytebites.restaurantservice.dto.mapper;

import com.bytebites.restaurantservice.dto.CreateRestaurantRequest;
import com.bytebites.restaurantservice.dto.RestaurantDto;
import com.bytebites.restaurantservice.model.Restaurant;

import java.util.Collections;
import java.util.stream.Collectors;

public class RestaurantMapper {

    public static Restaurant toEntity(CreateRestaurantRequest request) {
        if (request == null) {
            return null;
        }
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.name());
        restaurant.setAddress(request.address());
        return restaurant;
    }

    public static RestaurantDto toDto(Restaurant restaurant) {
        if (restaurant == null) {
            return null;
        }
        return new RestaurantDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getOwnerId(),
                restaurant.getMenuItems() == null ? Collections.emptyList() : restaurant.getMenuItems().stream()
                        .map(MenuItemMapper::toDto)
                        .collect(Collectors.toList())
        );
    }
}
