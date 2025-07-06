package com.bytebites.authservice.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}