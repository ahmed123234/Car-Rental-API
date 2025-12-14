// ==== Authentication Controller ====
package com.carrental.controller;

import com.carrental.dto.request.ChangePasswordRequest;
import com.carrental.dto.request.LoginRequest;
import com.carrental.dto.request.RefreshTokenRequest;
import com.carrental.dto.request.RegisterRequest;
import com.carrental.dto.response.AuthResponse;
import com.carrental.dto.response.UserProfileResponse;
import com.carrental.entity.User;
import com.carrental.service.AuthService;
import com.carrental.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication and authorization endpoints")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    /**
     * User Registration
     */
    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Create a new user account with email and password"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Register request received for email: {}", request.getEmail());

        AuthResponse authResponse = authService.register(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", "REGISTRATION_SUCCESS");
        response.put("message", "User registered successfully");
        response.put("data", authResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * User Login
     */
    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate user with email and password, returns access and refresh tokens"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid email or password"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login request received for email: {}", request.getEmail());

        AuthResponse authResponse = authService.login(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", "LOGIN_SUCCESS");
        response.put("message", "Login successful");
        response.put("data", authResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * Refresh Access Token
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Generate new access token using refresh token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<Map<String, Object>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        logger.info("Token refresh request received");

        AuthResponse authResponse = authService.refreshToken(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", "TOKEN_REFRESH_SUCCESS");
        response.put("message", "Token refreshed successfully");
        response.put("data", authResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * Get Current User Profile
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get current user profile",
            description = "Retrieve authenticated user's profile information"
    )
    @SecurityRequirement(name = "Bearer Token")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Map<String, Object>> getProfile() {
        logger.info("Get profile request received");

        User currentUser = authService.getCurrentUser();
        UserProfileResponse profileResponse = userService.mapUserToProfileResponse(currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", "PROFILE_RETRIEVED");
        response.put("message", "Profile retrieved successfully");
        response.put("data", profileResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * Update User Profile
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Update user profile",
            description = "Update authenticated user's profile information"
    )
    @SecurityRequirement(name = "Bearer Token")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, Object>> updateProfile(
            @Valid @RequestBody UserProfileResponse profileRequest) {
        logger.info("Update profile request received");

        User currentUser = authService.getCurrentUser();
        UserProfileResponse updatedProfile = userService.updateUserProfile(currentUser.getId(), profileRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", "PROFILE_UPDATED");
        response.put("message", "Profile updated successfully");
        response.put("data", updatedProfile);

        return ResponseEntity.ok(response);
    }

    /**
     * Change Password
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Change user password",
            description = "Change authenticated user's password"
    )
    @SecurityRequirement(name = "Bearer Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid current password"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        logger.info("Change password request received");

        User currentUser = authService.getCurrentUser();
        userService.changePassword(currentUser.getId(), request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", "PASSWORD_CHANGED");
        response.put("message", "Password changed successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Logout
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "User logout",
            description = "Logout authenticated user"
    )
    @SecurityRequirement(name = "Bearer Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, Object>> logout() {
        logger.info("Logout request received");

        authService.logout();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", "LOGOUT_SUCCESS");
        response.put("message", "Logout successful");

        return ResponseEntity.ok(response);
    }
}