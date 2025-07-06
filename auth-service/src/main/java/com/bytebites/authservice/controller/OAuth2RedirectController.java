package com.bytebites.authservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class OAuth2RedirectController {

    @GetMapping("/oauth2/success")
    public ResponseEntity<String> handleOAuth2Success(
            @RequestParam String token,
            @RequestParam String refreshToken
    ) {
        log.info("OAuth2 login successful. Access Token: " + token + ", Refresh Token: " + refreshToken);
        return ResponseEntity.ok("Login successful. Access Token: " + token);
    }
}