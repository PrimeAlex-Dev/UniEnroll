package com.unienroll.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret = "UniEnrollSecretKeyForJwtAuthenticationSpringSecurityMustBeAtLeast256BitsLong2026!";
    private long accessTokenExpirationMs = 900000;     // 15 minutes
    private long refreshTokenExpirationMs = 604800000; // 7 days
}
