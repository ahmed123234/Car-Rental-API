// ==== Authentication Service ====
package com.carrental.service;

import com.carrental.dto.request.LoginRequest;
import com.carrental.dto.request.RegisterRequest;
import com.carrental.dto.request.RefreshTokenRequest;
import com.carrental.dto.response.AuthResponse;
import com.carrental.entity.User;
import com.carrental.entity.enums.UserRole;
import com.carrental.entity.enums.UserStatus;
import com.carrental.exception.ResourceNotFoundException;
import com.carrental.exception.ValidationException;
import com.carrental.repository.UserRepository;
import com.carrental.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Register new user
     */
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering new user with email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Email already registered: {}", request.getEmail());
            throw new ValidationException("Email already registered: " + request.getEmail());
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", savedUser.getId());

        // Generate tokens
        String accessToken = tokenProvider.generateAccessToken(
                savedUser.getId(),
                savedUser.getEmail(),
                Collections.singletonList(savedUser.getRole().name())
        );

        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getId());

        return AuthResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600)
                .tokenType("Bearer")
                .build();
    }

    /**
     * Login user
     */
    public AuthResponse login(LoginRequest request) {
        logger.info("User login attempt with email: {}", request.getEmail());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("User authenticated successfully: {}", request.getEmail());

            // Get user details
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Check if user is active
            if (user.getStatus() != UserStatus.ACTIVE) {
                logger.warn("Login attempt for inactive user: {}", request.getEmail());
                throw new ValidationException("User account is not active");
            }

            // Generate tokens
            String accessToken = tokenProvider.generateAccessToken(
                    user.getId(),
                    user.getEmail(),
                    Collections.singletonList(user.getRole().name())
            );

            String refreshToken = tokenProvider.generateRefreshToken(user.getId());

            logger.info("Login successful for user: {}", request.getEmail());

            return AuthResponse.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(3600)
                    .tokenType("Bearer")
                    .build();

        } catch (BadCredentialsException ex) {
            logger.error("Invalid credentials for user: {}", request.getEmail());
            throw new ValidationException("Invalid email or password");
        }
    }

    /**
     * Refresh access token
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        logger.info("Refreshing access token");

        if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
            throw new ValidationException("Refresh token is required");
        }

        // Validate refresh token
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            logger.warn("Invalid refresh token");
            throw new ValidationException("Invalid or expired refresh token");
        }

        // Get user ID from refresh token
        Long userId = tokenProvider.getUserIdFromToken(request.getRefreshToken());
        if (userId == null) {
            throw new ValidationException("Invalid refresh token");
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Generate new access token
        String accessToken = tokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                Collections.singletonList(user.getRole().name())
        );

        // Generate new refresh token
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        logger.info("Access token refreshed for user: {}", user.getEmail());

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600)
                .tokenType("Bearer")
                .build();
    }

    /**
     * Get current user
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    /**
     * Logout user
     */
    public void logout() {
        logger.info("User logout");
        SecurityContextHolder.clearContext();
    }
}