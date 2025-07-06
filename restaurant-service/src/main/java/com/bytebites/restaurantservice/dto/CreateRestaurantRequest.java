package com.bytebites.restaurantservice.dto;

public record CreateRestaurantRequest(
        String name,
        String address
) {}
