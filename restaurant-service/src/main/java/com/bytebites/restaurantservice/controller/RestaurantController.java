package com.bytebites.restaurantservice.controller;

import com.bytebites.restaurantservice.dto.CreateRestaurantRequest;
import com.bytebites.restaurantservice.dto.RestaurantDto;
import com.bytebites.restaurantservice.service.RestaurantServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurants", description = "Endpoints for managing restaurants")
public class RestaurantController {

    private final RestaurantServiceImpl restaurantService;

    @GetMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(
            summary = "Get all restaurants",
            description = "Retrieve a list of all available restaurants. Requires user to be authenticated.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Restaurants retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
            }
    )
    public ResponseEntity<List<RestaurantDto>> getAllRestaurants(){
        return ResponseEntity.ok(restaurantService.findAll());
    }

    @Operation(
            summary = "Create a restaurant",
            description = "Only restaurant owners can create a restaurant",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Restaurant created"),
                    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
            }
    )
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

    @Operation(
            summary = "Update restaurant details",
            description = "Allows a restaurant owner to update their own restaurant. Checks ownership internally.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Restaurant updated"),
                    @ApiResponse(responseCode = "403", description = "Not authorized to update this restaurant", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Restaurant not found", content = @Content)
            }
    )
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