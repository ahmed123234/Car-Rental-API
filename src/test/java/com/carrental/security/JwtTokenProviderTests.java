// ==== JWT Token Provider Tests ====
package com.carrental.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtTokenProviderTests {

    @Autowired
    private JwtTokenProvider tokenProvider;

    private Long userId;
    private String email;
    private List<String> roles;

    @BeforeEach
    void setUp() {
        userId = 1L;
        email = "test@example.com";
        roles = Arrays.asList("CUSTOMER");
    }

    @Test
    void testGenerateAccessToken() {
        String token = tokenProvider.generateAccessToken(userId, email, roles);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void testGenerateRefreshToken() {
        String token = tokenProvider.generateRefreshToken(userId);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void testValidateToken() {
        String token = tokenProvider.generateAccessToken(userId, email, roles);

        boolean isValid = tokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void testValidateInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = tokenProvider.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void testGetUserIdFromToken() {
        String token = tokenProvider.generateAccessToken(userId, email, roles);

        Long extractedUserId = tokenProvider.getUserIdFromToken(token);

        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void testGetEmailFromToken() {
        String token = tokenProvider.generateAccessToken(userId, email, roles);

        String extractedEmail = tokenProvider.getEmailFromToken(token);

        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    void testGetRolesFromToken() {
        String token = tokenProvider.generateAccessToken(userId, email, roles);

        List<String> extractedRoles = tokenProvider.getRolesFromToken(token);

        assertThat(extractedRoles).isEqualTo(roles);
    }

    @Test
    void testIsTokenExpired() {
        String token = tokenProvider.generateAccessToken(userId, email, roles);

        boolean isExpired = tokenProvider.isTokenExpired(token);

        assertThat(isExpired).isFalse();
    }

    @Test
    void testGetExpirationDateFromToken() {
        String token = tokenProvider.generateAccessToken(userId, email, roles);

        java.util.Date expirationDate = tokenProvider.getExpirationDateFromToken(token);

        assertThat(expirationDate).isNotNull();
        assertThat(expirationDate).isAfter(new java.util.Date());
    }
}