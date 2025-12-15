package com.carrental.controller;

import com.carrental.dto.request.CreateRentalRequest;
import com.carrental.dto.request.UpdateRentalRequest;
import com.carrental.dto.response.RentalResponse;
import com.carrental.entity.enums.RentalStatus;
import com.carrental.security.JwtTokenProvider;
import com.carrental.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Rental Management", description = "APIs for managing rental bookings")
@SecurityRequirement(name = "Bearer Authentication")
public class RentalController {
    
    private final RentalService rentalService;
    private final JwtTokenProvider jwtTokenProvider;
    
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create a new rental booking")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Rental created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Vehicle not available for selected dates")
    })
    public ResponseEntity<RentalResponse> createRental(
        @Valid @RequestBody CreateRentalRequest request,
        @RequestHeader("Authorization") String token) {
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        log.info("Creating rental for user: {}", userId);
        
        RentalResponse response = rentalService.createRental(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(summary = "Get rental details by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rental found"),
        @ApiResponse(responseCode = "404", description = "Rental not found")
    })
    public ResponseEntity<RentalResponse> getRental(
        @Parameter(description = "Rental ID") @PathVariable Long id) {
        
        RentalResponse response = rentalService.getRentalById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/rentals")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get all rentals for current user")
    public ResponseEntity<Page<RentalResponse>> getUserRentals(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestHeader("Authorization") String token) {
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        Pageable pageable = PageRequest.of(page, size);
        
        Page<RentalResponse> response = rentalService.getUserRentals(userId, pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/rentals/status/{status}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get rentals by status for current user")
    public ResponseEntity<Page<RentalResponse>> getUserRentalsByStatus(
        @Parameter(description = "Rental status") @PathVariable RentalStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestHeader("Authorization") String token) {
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        Pageable pageable = PageRequest.of(page, size);
        
        Page<RentalResponse> response = rentalService.getUserRentalsByStatus(userId, status, pageable);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Update a pending rental booking")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rental updated successfully"),
        @ApiResponse(responseCode = "404", description = "Rental not found"),
        @ApiResponse(responseCode = "400", description = "Invalid update or booking not pending")
    })
    public ResponseEntity<RentalResponse> updateRental(
        @Parameter(description = "Rental ID") @PathVariable Long id,
        @Valid @RequestBody UpdateRentalRequest request,
        @RequestHeader("Authorization") String token) {
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        log.info("Updating rental: {} for user: {}", id, userId);
        
        RentalResponse response = rentalService.updateRental(id, userId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cancel a rental booking")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rental cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Rental not found"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel rental")
    })
    public ResponseEntity<RentalResponse> cancelRental(
        @Parameter(description = "Rental ID") @PathVariable Long id,
        @RequestHeader("Authorization") String token) {
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        log.info("Cancelling rental: {} for user: {}", id, userId);
        
        RentalResponse response = rentalService.cancelRental(id, userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Confirm a pending rental (Admin only)")
    public ResponseEntity<RentalResponse> confirmRental(
        @Parameter(description = "Rental ID") @PathVariable Long id) {
        
        log.info("Confirming rental: {}", id);
        RentalResponse response = rentalService.confirmRental(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a confirmed rental (Admin only)")
    public ResponseEntity<RentalResponse> activateRental(
        @Parameter(description = "Rental ID") @PathVariable Long id) {
        
        log.info("Activating rental: {}", id);
        RentalResponse response = rentalService.activateRental(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Complete an active rental (Admin only)")
    public ResponseEntity<RentalResponse> completeRental(
        @Parameter(description = "Rental ID") @PathVariable Long id) {
        
        log.info("Completing rental: {}", id);
        RentalResponse response = rentalService.completeRental(id);
        return ResponseEntity.ok(response);
    }
}