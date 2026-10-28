package com.unienroll.security;

import com.unienroll.config.JwtProperties;
import com.unienroll.modules.auth.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("UniEnrollSecretKeyForJwtAuthenticationSpringSecurityMustBeAtLeast256BitsLong2026!");
        jwtProperties.setAccessTokenExpirationMs(60000);   // 1 minute
        jwtProperties.setRefreshTokenExpirationMs(300000); // 5 minutes

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("Tạo Access Token hợp lệ và giải mã đúng các claims")
    void shouldGenerateAndValidateAccessTokenSuccessfully() {
        Long userId = 100L;
        String username = "student01";
        Role role = Role.ROLE_STUDENT;

        String token = jwtTokenProvider.generateAccessToken(userId, username, role);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(username, jwtTokenProvider.extractUsername(token));
        assertEquals(userId, jwtTokenProvider.extractUserId(token));
        assertEquals("ROLE_STUDENT", jwtTokenProvider.extractRole(token));
        assertEquals("ACCESS", jwtTokenProvider.extractTokenType(token));
    }

    @Test
    @DisplayName("Tạo Refresh Token hợp lệ với tokenType là REFRESH")
    void shouldGenerateRefreshTokenSuccessfully() {
        Long userId = 100L;
        String username = "student01";

        String refreshToken = jwtTokenProvider.generateRefreshToken(userId, username);

        assertNotNull(refreshToken);
        assertTrue(jwtTokenProvider.validateToken(refreshToken));
        assertEquals(username, jwtTokenProvider.extractUsername(refreshToken));
        assertEquals("REFRESH", jwtTokenProvider.extractTokenType(refreshToken));
    }

    @Test
    @DisplayName("Từ chối Token đã bị can thiệp chữ ký (tampered token)")
    void shouldRejectTamperedToken() {
        String token = jwtTokenProvider.generateAccessToken(100L, "student01", Role.ROLE_STUDENT);
        String tamperedToken = token + "xyz";

        assertFalse(jwtTokenProvider.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("Từ chối Token đã hết hạn")
    void shouldRejectExpiredToken() throws InterruptedException {
        // Cấu hình token hết hạn sau 10ms
        jwtProperties.setAccessTokenExpirationMs(10);
        jwtTokenProvider.init();

        String token = jwtTokenProvider.generateAccessToken(100L, "student01", Role.ROLE_STUDENT);
        Thread.sleep(50); // Chờ token hết hạn

        assertFalse(jwtTokenProvider.validateToken(token));
    }
}
