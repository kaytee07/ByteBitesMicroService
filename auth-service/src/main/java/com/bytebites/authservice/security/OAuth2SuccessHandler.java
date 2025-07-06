package com.bytebites.authservice.security;

import com.bytebites.authservice.Repository.UserRepository;
import com.bytebites.authservice.enums.UserRole;

import com.bytebites.authservice.model.User;
import com.bytebites.authservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.List;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public OAuth2SuccessHandler(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");


        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(name);
            newUser.setEmail(email);
            newUser.setRoles(List.of(UserRole.CUSTOMER));
            return userRepository.save(newUser);
        });

        String jwtToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());


        response.sendRedirect("/oauth2/success?token=" + jwtToken + "&refreshToken=" + refreshToken);
    }
}