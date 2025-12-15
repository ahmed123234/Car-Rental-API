// ==== User Service ====
package com.carrental.service;

import com.carrental.dto.request.ChangePasswordRequest;
import com.carrental.dto.response.UserProfileResponse;
import com.carrental.entity.User;
import com.carrental.exception.ResourceNotFoundException;
import com.carrental.exception.ValidationException;
import com.carrental.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        logger.debug("Fetching user with ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        logger.debug("Fetching user with email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    /**
     * Map User entity to UserProfileResponse DTO
     */
    public UserProfileResponse mapUserToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Update user profile
     */
    public UserProfileResponse updateUserProfile(Long userId, UserProfileResponse profileRequest) {
        logger.info("Updating profile for user ID: {}", userId);

        User user = getUserById(userId);

        // Update allowed fields
        if (profileRequest.getFirstName() != null && !profileRequest.getFirstName().isEmpty()) {
            user.setFirstName(profileRequest.getFirstName());
        }

        if (profileRequest.getLastName() != null && !profileRequest.getLastName().isEmpty()) {
            user.setLastName(profileRequest.getLastName());
        }

        if (profileRequest.getPhone() != null && !profileRequest.getPhone().isEmpty()) {
            user.setPhone(profileRequest.getPhone());
        }

        if (profileRequest.getAddress() != null && !profileRequest.getAddress().isEmpty()) {
            user.setAddress(profileRequest.getAddress());
        }

        User updatedUser = userRepository.save(user);
        logger.info("User profile updated successfully for user ID: {}", userId);

        return mapUserToProfileResponse(updatedUser);
    }

    /**
     * Change password
     */
    public void changePassword(Long userId, ChangePasswordRequest request) {
        logger.info("Changing password for user ID: {}", userId);

        User user = getUserById(userId);

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            logger.warn("Invalid old password for user ID: {}", userId);
            throw new ValidationException("Invalid current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        logger.info("Password changed successfully for user ID: {}", userId);
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Delete user (soft delete)
     */
    public void deleteUser(Long userId) {
        logger.info("Deleting user ID: {}", userId);

        User user = getUserById(userId);
        user.setStatus(com.carrental.entity.enums.UserStatus.INACTIVE);
        userRepository.save(user);

        logger.info("User deleted (soft delete) for user ID: {}", userId);
    }
}
