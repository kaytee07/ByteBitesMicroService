package com.bytebites.restaurantservice.Interface;
import com.bytebites.restaurantservice.dto.CreateRestaurantRequest;
import com.bytebites.restaurantservice.dto.RestaurantDto;

import java.util.List;
import java.util.UUID;

public interface RestaurantService {

    RestaurantDto createRestaurant(CreateRestaurantRequest request, UUID ownerId);

    RestaurantDto getRestaurantById(UUID id);

    List<RestaurantDto> getRestaurantsByOwner(UUID ownerId);

    RestaurantDto updateRestaurant(UUID id, CreateRestaurantRequest request, UUID ownerId);

    void deleteRestaurant(UUID id, UUID ownerId);

}

