// ==== Authentication Service Tests ====
package com.carrental.service;

import com.carrental.dto.request.LoginRequest;
import com.carrental.dto.request.RegisterRequest;
import com.carrental.dto.response.AuthResponse;
import com.carrental.entity.User;
import com.carrental.entity.enums.UserRole;
import com.carrental.entity.enums.UserStatus;
import com.carrental.exception.ValidationException;
import com.carrental.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("TestPassword@123")
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("TestPassword@123")
                .build();
    }

    @Test
    void testRegisterNewUser() {
        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
    }

    @Test
    void testRegisterDuplicateEmail() {
        authService.register(registerRequest);

        assertThrows(ValidationException.class, () -> authService.register(registerRequest));
    }

    @Test
    void testLoginValidCredentials() {
        authService.register(registerRequest);

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getAccessToken()).isNotNull();
    }

    @Test
    void testLoginInvalidPassword() {
        authService.register(registerRequest);

        LoginRequest invalidLogin = LoginRequest.builder()
                .email("test@example.com")
                .password("WrongPassword@123")
                .build();

        assertThrows(ValidationException.class, () -> authService.login(invalidLogin));
    }

    @Test
    void testRefreshToken() {
        AuthResponse registrationResponse = authService.register(registerRequest);

        com.carrental.dto.request.RefreshTokenRequest refreshRequest =
                com.carrental.dto.request.RefreshTokenRequest.builder()
                        .refreshToken(registrationResponse.getRefreshToken())
                        .build();

        AuthResponse newTokens = authService.refreshToken(refreshRequest);

        assertThat(newTokens.getAccessToken()).isNotNull();
        assertThat(newTokens.getRefreshToken()).isNotNull();
        assertThat(newTokens.getAccessToken()).isNotEqualTo(registrationResponse.getAccessToken());
    }
}
