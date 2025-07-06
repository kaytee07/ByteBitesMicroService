package com.bytebites.emailservice.util;


import lombok.Data;

@Data
public class CustomUserPrincipal {
    private String userId;
    private String email;

    public CustomUserPrincipal(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }

}
