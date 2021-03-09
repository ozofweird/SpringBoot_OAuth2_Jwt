package com.tistory.ozofweird.springboot_oauth2_jwt.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column
    private String imageUrl;

    @Column
    private Role role;

    @Column
    private Boolean emailVerified = false;

    @JsonIgnore
    @Column
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Column
    private String providerId;

    @Builder
    public User(String name, String email, String imageUrl, Role role, Boolean emailVerified, String password, AuthProvider provider, String providerId) {
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        this.role = role;
        this.emailVerified = emailVerified;
        this.password = password;
        this.provider = provider;
        this.providerId = providerId;
    }

    public User update(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
        return this;
    }
}
