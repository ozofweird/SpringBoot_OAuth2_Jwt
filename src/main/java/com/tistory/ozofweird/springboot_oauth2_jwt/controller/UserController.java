package com.tistory.ozofweird.springboot_oauth2_jwt.controller;

import com.tistory.ozofweird.springboot_oauth2_jwt.domain.user.User;
import com.tistory.ozofweird.springboot_oauth2_jwt.domain.user.UserRepository;
import com.tistory.ozofweird.springboot_oauth2_jwt.exception.ResourceNotFoundException;
import com.tistory.ozofweird.springboot_oauth2_jwt.security.CurrentUser;
import com.tistory.ozofweird.springboot_oauth2_jwt.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public User getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }
}
