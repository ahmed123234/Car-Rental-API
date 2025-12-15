package com.carrental.service;

import com.carrental.dto.request.CreateRentalRequest;
import com.carrental.dto.request.UpdateRentalRequest;
import com.carrental.dto.response.RentalResponse;
import com.carrental.entity.Rental;
import com.carrental.entity.User;
import com.carrental.entity.Vehicle;
import com.carrental.entity.enums.RentalStatus;
import com.carrental.exception.BookingConflictException;
import com.carrental.exception.RentalException;
import com.carrental.mapper.RentalMapper;
import com.carrental.repository.RentalRepository;
import com.carrental.repository.UserRepository;
import com.carrental.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RentalService {
    
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final RentalMapper rentalMapper;
    
    private static final List<RentalStatus> BLOCKING_STATUSES = 
        Arrays.asList(RentalStatus.PENDING, RentalStatus.CONFIRMED, RentalStatus.ACTIVE);
    
    public RentalResponse createRental(Long userId, CreateRentalRequest request) {
        log.info("Creating rental for user: {}, vehicle: {}", userId, request.getVehicleId());
        
        // Validate dates
        validateDates(request.getPickupDate(), request.getReturnDate());
        
        // Fetch user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RentalException("User not found"));
        
        // Fetch vehicle
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
            .orElseThrow(() -> new RentalException("Vehicle not found"));
        
        // Check vehicle availability
        checkVehicleAvailability(vehicle.getId(), request.getPickupDate(), request.getReturnDate());
        
        // Calculate rental cost
        long rentalDays = calculateRentalDays(request.getPickupDate(), request.getReturnDate());
        BigDecimal totalCost = calculateTotalCost(vehicle.getDailyRate(), rentalDays);
        
        // Create rental
        Rental rental = Rental.builder()
            .user(user)
            .vehicle(vehicle)
            .pickupDate(request.getPickupDate())
            .returnDate(request.getReturnDate())
            .pickupLocation(request.getPickupLocation())
            .returnLocation(request.getReturnLocation())
            .dailyRate(vehicle.getDailyRate())
            .totalCost(totalCost)
            .specialRequests(request.getSpecialRequests())
            .status(RentalStatus.PENDING)
            .build();
        
        Rental savedRental = rentalRepository.save(rental);
        log.info("Rental created successfully with ID: {}", savedRental.getId());
        
        return rentalMapper.toResponse(savedRental);
    }
    
    @Transactional(readOnly = true)
    public RentalResponse getRentalById(Long rentalId) {
        log.debug("Fetching rental: {}", rentalId);
        
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new RentalException("Rental not found with ID: " + rentalId));
        
        return rentalMapper.toResponse(rental);
    }
    
    @Transactional(readOnly = true)
    public Page<RentalResponse> getUserRentals(Long userId, Pageable pageable) {
        log.debug("Fetching rentals for user: {}", userId);
        
        return rentalRepository.findByUserId(userId, pageable)
            .map(rentalMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<RentalResponse> getUserRentalsByStatus(Long userId, RentalStatus status, Pageable pageable) {
        log.debug("Fetching rentals for user: {} with status: {}", userId, status);
        
        return rentalRepository.findByUserIdAndStatus(userId, status, pageable)
            .map(rentalMapper::toResponse);
    }
    
    public RentalResponse updateRental(Long rentalId, Long userId, UpdateRentalRequest request) {
        log.info("Updating rental: {} for user: {}", rentalId, userId);
        
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new RentalException("Rental not found"));
        
        if (!rental.getUser().getId().equals(userId)) {
            throw new RentalException("Unauthorized: Cannot update rental belonging to another user");
        }
        
        if (rental.getStatus() != RentalStatus.PENDING) {
            throw new RentalException("Only PENDING rentals can be modified");
        }
        
        if (LocalDateTime.now().plusHours(24).isAfter(rental.getPickupDate())) {
            throw new RentalException("Cannot modify rental less than 24 hours before pickup");
        }
        
        // Update fields if provided
        if (request.getPickupDate() != null) {
            validateDates(request.getPickupDate(), request.getReturnDate() != null ? request.getReturnDate() : rental.getReturnDate());
            checkVehicleAvailability(rental.getVehicle().getId(), request.getPickupDate(), 
                                    request.getReturnDate() != null ? request.getReturnDate() : rental.getReturnDate());
            rental.setPickupDate(request.getPickupDate());
        }
        
        if (request.getReturnDate() != null) {
            validateDates(request.getPickupDate() != null ? request.getPickupDate() : rental.getPickupDate(), request.getReturnDate());
            rental.setReturnDate(request.getReturnDate());
        }
        
        if (request.getPickupLocation() != null) {
            rental.setPickupLocation(request.getPickupLocation());
        }
        
        if (request.getReturnLocation() != null) {
            rental.setReturnLocation(request.getReturnLocation());
        }
        
        if (request.getSpecialRequests() != null) {
            rental.setSpecialRequests(request.getSpecialRequests());
        }
        
        // Recalculate total cost
        long rentalDays = calculateRentalDays(rental.getPickupDate(), rental.getReturnDate());
        BigDecimal newTotalCost = calculateTotalCost(rental.getDailyRate(), rentalDays);
        rental.setTotalCost(newTotalCost);
        
        Rental updatedRental = rentalRepository.save(rental);
        log.info("Rental updated successfully: {}", rentalId);
        
        return rentalMapper.toResponse(updatedRental);
    }
    
    public RentalResponse cancelRental(Long rentalId, Long userId) {
        log.info("Cancelling rental: {} for user: {}", rentalId, userId);
        
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new RentalException("Rental not found"));
        
        if (!rental.getUser().getId().equals(userId)) {
            throw new RentalException("Unauthorized: Cannot cancel rental belonging to another user");
        }
        
        if (rental.getStatus() == RentalStatus.COMPLETED || rental.getStatus() == RentalStatus.CANCELLED) {
            throw new RentalException("Cannot cancel a " + rental.getStatus().getDescription().toLowerCase() + " rental");
        }
        
        if (rental.getStatus() == RentalStatus.ACTIVE) {
            throw new RentalException("Cannot cancel an active rental. Please return the vehicle first.");
        }
        
        rental.setStatus(RentalStatus.CANCELLED);
        Rental cancelledRental = rentalRepository.save(rental);
        log.info("Rental cancelled successfully: {}", rentalId);
        
        return rentalMapper.toResponse(cancelledRental);
    }
    
    public RentalResponse confirmRental(Long rentalId) {
        log.info("Confirming rental: {}", rentalId);
        
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new RentalException("Rental not found"));
        
        if (rental.getStatus() != RentalStatus.PENDING) {
            throw new RentalException("Only PENDING rentals can be confirmed");
        }
        
        rental.setStatus(RentalStatus.CONFIRMED);
        Rental confirmedRental = rentalRepository.save(rental);
        log.info("Rental confirmed successfully: {}", rentalId);
        
        return rentalMapper.toResponse(confirmedRental);
    }
    
    public RentalResponse activateRental(Long rentalId) {
        log.info("Activating rental: {}", rentalId);
        
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new RentalException("Rental not found"));
        
        if (rental.getStatus() != RentalStatus.CONFIRMED) {
            throw new RentalException("Only CONFIRMED rentals can be activated");
        }
        
        rental.setStatus(RentalStatus.ACTIVE);
        Rental activeRental = rentalRepository.save(rental);
        log.info("Rental activated successfully: {}", rentalId);
        
        return rentalMapper.toResponse(activeRental);
    }
    
    public RentalResponse completeRental(Long rentalId) {
        log.info("Completing rental: {}", rentalId);
        
        Rental rental = rentalRepository.findById(rentalId)
            .orElseThrow(() -> new RentalException("Rental not found"));
        
        if (rental.getStatus() != RentalStatus.ACTIVE) {
            throw new RentalException("Only ACTIVE rentals can be completed");
        }
        
        rental.setStatus(RentalStatus.COMPLETED);
        Rental completedRental = rentalRepository.save(rental);
        log.info("Rental completed successfully: {}", rentalId);
        
        return rentalMapper.toResponse(completedRental);
    }
    
    private void validateDates(LocalDateTime pickupDate, LocalDateTime returnDate) {
        if (returnDate.isBefore(pickupDate) || returnDate.isEqual(pickupDate)) {
            throw new RentalException("Return date must be after pickup date");
        }
    }
    
    private void checkVehicleAvailability(Long vehicleId, LocalDateTime pickupDate, LocalDateTime returnDate) {
        List<Rental> conflicts = rentalRepository.findConflictingRentals(vehicleId, pickupDate, returnDate, BLOCKING_STATUSES);
        
        if (!conflicts.isEmpty()) {
            throw new BookingConflictException("Vehicle is not available for the selected dates");
        }
    }
    
    private long calculateRentalDays(LocalDateTime pickupDate, LocalDateTime returnDate) {
        return ChronoUnit.DAYS.between(pickupDate, returnDate);
    }
    
    private BigDecimal calculateTotalCost(BigDecimal dailyRate, long rentalDays) {
        return dailyRate.multiply(BigDecimal.valueOf(rentalDays));
    }
}