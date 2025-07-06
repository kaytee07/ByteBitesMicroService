package com.bytebites.restaurantservice.service;

import com.bytebites.restaurantservice.Interface.RestaurantService;
import com.bytebites.restaurantservice.dto.CreateRestaurantRequest;
import com.bytebites.restaurantservice.dto.RestaurantDto;
import com.bytebites.restaurantservice.exception.ResourceNotFoundException;
import com.bytebites.restaurantservice.dto.mapper.RestaurantMapper;
import com.bytebites.restaurantservice.model.Restaurant;
import com.bytebites.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @CircuitBreaker(name = "restaurantService", fallbackMethod = "fallbackRestaurant")
    @Retry(name = "restaurantService")
    public  List<RestaurantDto> findAll(){
        return restaurantRepository.findAll().stream()
                .map(RestaurantMapper::toDto)
                .collect(Collectors.toList());
    }

    @CircuitBreaker(name = "restaurantService", fallbackMethod = "fallbackRestaurant")
    @Retry(name = "restaurantService")
    @Override
    @Transactional(readOnly = true)
    public RestaurantDto getRestaurantById(UUID id) {
        return restaurantRepository.findById(id)
                .map(RestaurantMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
    }

    @CircuitBreaker(name = "restaurantService", fallbackMethod = "fallbackRestaurant")
    @Retry(name = "restaurantService")
    @Override
    @Transactional(readOnly = true)
    public List<RestaurantDto> getRestaurantsByOwner(UUID ownerId) {
        return restaurantRepository.findByOwnerId(ownerId).stream()
                .map(RestaurantMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RestaurantDto createRestaurant(CreateRestaurantRequest request, UUID ownerId) {
        Restaurant newRestaurant = RestaurantMapper.toEntity(request);
        newRestaurant.setOwnerId(ownerId);
        Restaurant savedRestaurant = restaurantRepository.save(newRestaurant);
        return RestaurantMapper.toDto(savedRestaurant);
    }

    @Override
    @Transactional
    public RestaurantDto updateRestaurant(UUID id, CreateRestaurantRequest request, UUID ownerId) {
        Restaurant existingRestaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        if (!existingRestaurant.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not authorized to update this restaurant");
        }

        existingRestaurant.setName(request.name());
        existingRestaurant.setAddress(request.address());
        Restaurant savedRestaurant = restaurantRepository.save(existingRestaurant);
        return RestaurantMapper.toDto(savedRestaurant);
    }

    @Override
    @Transactional
    public void deleteRestaurant(UUID id, UUID ownerId) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not authorized to delete this restaurant");
        }

        restaurantRepository.delete(restaurant);
    }

    public RestaurantDto fallBackRestaurant(UUID id, Throwable ex) {
        log.warn("Fallback triggered for restaurant {} due to {}", id, ex.toString());
        return new RestaurantDto(id, null, null, null, null);
    }
}
