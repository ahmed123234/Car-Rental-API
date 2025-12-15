// ==== User Service Tests ====
package com.carrental.service;

import com.carrental.dto.request.ChangePasswordRequest;
import com.carrental.dto.response.UserProfileResponse;
import com.carrental.entity.User;
import com.carrental.entity.enums.UserRole;
import com.carrental.entity.enums.UserStatus;
import com.carrental.exception.ResourceNotFoundException;
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
class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("TestPassword@123"))
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        testUser = userRepository.save(testUser);
    }

    @Test
    void testGetUserById() {
        User user = userService.getUserById(testUser.getId());

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testGetUserByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void testGetUserByEmail() {
        User user = userService.getUserByEmail("test@example.com");

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(testUser.getId());
    }

    @Test
    void testMapUserToProfileResponse() {
        UserProfileResponse response = userService.mapUserToProfileResponse(testUser);

        assertThat(response.getId()).isEqualTo(testUser.getId());
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(response.getFirstName()).isEqualTo(testUser.getFirstName());
    }

    @Test
    void testUpdateUserProfile() {
        UserProfileResponse updateRequest = UserProfileResponse.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phone("9876543210")
                .build();

        UserProfileResponse response = userService.updateUserProfile(testUser.getId(), updateRequest);

        assertThat(response.getFirstName()).isEqualTo("Jane");
        assertThat(response.getLastName()).isEqualTo("Smith");
        assertThat(response.getPhone()).isEqualTo("9876543210");
    }

    @Test
    void testChangePassword() {
        ChangePasswordRequest changeRequest = ChangePasswordRequest.builder()
                .oldPassword("TestPassword@123")
                .newPassword("NewPassword@456")
                .build();

        userService.changePassword(testUser.getId(), changeRequest);

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("NewPassword@456", updatedUser.getPassword())).isTrue();
    }

    @Test
    void testChangePasswordInvalidOldPassword() {
        ChangePasswordRequest changeRequest = ChangePasswordRequest.builder()
                .oldPassword("WrongPassword@123")
                .newPassword("NewPassword@456")
                .build();

        assertThrows(ValidationException.class, () -> userService.changePassword(testUser.getId(), changeRequest));
    }

    @Test
    void testEmailExists() {
        boolean exists = userService.emailExists("test@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void testEmailNotExists() {
        boolean exists = userService.emailExists("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void testDeleteUser() {
        userService.deleteUser(testUser.getId());

        User deletedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(deletedUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }
}