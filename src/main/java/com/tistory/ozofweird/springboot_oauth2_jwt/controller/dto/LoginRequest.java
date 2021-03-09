package com.tistory.ozofweird.springboot_oauth2_jwt.controller.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginRequest {

    private String email;
    private String password;

    @Builder
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
