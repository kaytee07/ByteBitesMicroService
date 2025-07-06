package com.bytebites.restaurantservice.repository;

import com.bytebites.restaurantservice.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    List<Restaurant> findByOwnerId(UUID ownerId);
}

