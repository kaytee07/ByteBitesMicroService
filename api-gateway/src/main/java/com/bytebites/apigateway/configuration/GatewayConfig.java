package com.bytebites.apigateway.configuration;

import com.bytebites.apigateway.filter.JwtValidationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final JwtValidationFilter jwtValidationFilter;

    @Autowired
    public GatewayConfig(JwtValidationFilter jwtValidationFilter) {
        this.jwtValidationFilter = jwtValidationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/auth/**")
                        .filters(f -> f.filter(jwtValidationFilter))
                        .uri("lb://auth-service"))

                .route("restaurant-service", r -> r.path("/api/restaurants/**")
                        .filters(f -> f.filter(jwtValidationFilter))
                        .uri("lb://restaurant-service"))

                .route("order-service", r -> r.path("/api/orders/**")
                        .filters(f -> f.filter(jwtValidationFilter))
                        .uri("lb://order-service"))

                .build();
    }

}