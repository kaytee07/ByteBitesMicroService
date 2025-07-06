package com.bytebites.authservice.service;

import com.bytebites.authservice.Repository.UserRepository;
import com.bytebites.authservice.dto.AuthRequest;
import com.bytebites.authservice.dto.AuthResponse;
import com.bytebites.authservice.dto.RegisterRequest;
import com.bytebites.authservice.enums.AuthProvider;
import com.bytebites.authservice.model.User;
import com.bytebites.authservice.security.CustomUserDetails;
import com.bytebites.authservice.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public void register(@Valid RegisterRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new BadCredentialsException("Password required for local login.");
        }

        if (userRepo.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use.");
        }

        var user = new User();

        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setProvider(AuthProvider.LOCAL);
        user.setRoles(List.of(request.role()));

        userRepo.save(user);
    }

    public AuthResponse login(AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User user = userDetails.getUser();

        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        return new AuthResponse(accessToken, refreshToken);
    }

}

