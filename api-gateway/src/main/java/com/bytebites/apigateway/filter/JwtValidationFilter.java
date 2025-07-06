package com.bytebites.apigateway.filter; // Assuming a package name

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.List;

@Component
public class JwtValidationFilter implements GatewayFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtValidationFilter.class);

    @Value("${app.jwt.secret}")
    private String secret;

    private Key key;

    @PostConstruct
    public void init() {
        // FIX: Secret key must be Base64-decoded to match the signing key from JwtUtil
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Paths that do not require authentication
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/auth/login",
            "/auth/register",
            "/eureka"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        if (isPathExcluded(request.getURI().getPath())) {
            logger.info("Excluding path from JWT validation: {}", request.getURI().getPath());
            return chain.filter(exchange);
        }

        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            logger.warn("Missing Authorization header for protected path: {}", request.getURI().getPath());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Invalid Authorization header format for protected path: {}", request.getURI().getPath());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Object rolesClaim = claims.get("roles");
            String rolesHeaderValue = "";
            if (rolesClaim instanceof List) {
                rolesHeaderValue = String.join(",", (List<String>) rolesClaim);
            } else if (rolesClaim != null) {
                rolesHeaderValue = rolesClaim.toString();
            }

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-ID", claims.get("userId", String.class))
                    .header("X-User-Roles", rolesHeaderValue)
                    .header("X-User-Email", claims.get("email", String.class))
                    .build();

            logger.info("JWT validated successfully for user: {}", claims.getSubject());
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (ExpiredJwtException e) {
            logger.error("JWT expired: {}", e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException e) {
            logger.error("Invalid JWT: {}", e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        } catch (Exception e) {
            logger.error("Error during JWT validation: {}", e.getMessage(), e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }

    private boolean isPathExcluded(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }
}