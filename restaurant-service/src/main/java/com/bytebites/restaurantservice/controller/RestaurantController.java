package com.bytebites.restaurantservice.controller;

import com.bytebites.restaurantservice.dto.CreateRestaurantRequest;
import com.bytebites.restaurantservice.dto.RestaurantDto;
import com.bytebites.restaurantservice.service.RestaurantServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantServiceImpl restaurantService;

    @GetMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<RestaurantDto>> getAll(){
        return ResponseEntity.ok(restaurantService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<RestaurantDto> create(
            @RequestBody CreateRestaurantRequest request,
            @RequestHeader("X-User-ID") UUID userId) {
        RestaurantDto createdRestaurant = restaurantService.createRestaurant(request, userId);
        return new ResponseEntity<>(createdRestaurant, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto> getById(@PathVariable UUID id) {
        RestaurantDto restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(restaurant);
    }

    @GetMapping("/owner")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<RestaurantDto>> getByOwner(@RequestHeader("X-User-ID") UUID ownerId) {
        List<RestaurantDto> restaurants = restaurantService.getRestaurantsByOwner(ownerId);
        return ResponseEntity.ok(restaurants);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<RestaurantDto> update(
            @PathVariable UUID id,
            @RequestBody CreateRestaurantRequest request,
            @RequestHeader("X-User-ID") UUID userId) {
        RestaurantDto updatedRestaurant = restaurantService.updateRestaurant(id, request, userId);
        return ResponseEntity.ok(updatedRestaurant);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id,
            @RequestHeader("X-User-ID") UUID ownerId) {
        restaurantService.deleteRestaurant(id, ownerId);
    }
}