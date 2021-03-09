package com.tistory.ozofweird.springboot_oauth2_jwt;

import com.tistory.ozofweird.springboot_oauth2_jwt.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@RestController
public class SpringbootOauth2JwtApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootOauth2JwtApplication.class, args);
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }
}
