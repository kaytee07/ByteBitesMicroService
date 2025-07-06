package com.bytebites.authservice.dto;

import com.bytebites.authservice.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String email,
        @NotNull UserRole role
) {}