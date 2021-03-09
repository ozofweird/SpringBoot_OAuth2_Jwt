package com.tistory.ozofweird.springboot_oauth2_jwt.controller.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";

    @Builder
    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}