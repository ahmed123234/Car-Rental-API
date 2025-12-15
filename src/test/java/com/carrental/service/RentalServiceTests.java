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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTests {
    
    @Mock
    private RentalRepository rentalRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private VehicleRepository vehicleRepository;
    
    @Mock
    private RentalMapper rentalMapper;
    
    @InjectMocks
    private RentalService rentalService;
    
    private User testUser;
    private Vehicle testVehicle;
    private Rental testRental;
    private CreateRentalRequest createRequest;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .email("user@test.com")
            .firstName("John")
            .lastName("Doe")
            .build();
        
        testVehicle = Vehicle.builder()
            .id(1L)
            .make("Toyota")
            .model("Camry")
            .dailyRate(BigDecimal.valueOf(50))
            .build();
        
        LocalDateTime now = LocalDateTime.now();
        testRental = Rental.builder()
            .id(1L)
            .user(testUser)
            .vehicle(testVehicle)
            .pickupDate(now.plusDays(2))
            .returnDate(now.plusDays(5))
            .pickupLocation("Airport")
            .returnLocation("Downtown")
            .dailyRate(BigDecimal.valueOf(50))
            .totalCost(BigDecimal.valueOf(150))
            .status(RentalStatus.PENDING)
            .build();
        
        createRequest = CreateRentalRequest.builder()
            .vehicleId(1L)
            .pickupDate(now.plusDays(2))
            .returnDate(now.plusDays(5))
            .pickupLocation("Airport")
            .returnLocation("Downtown")
            .build();
    }
    
    @Test
    void testCreateRental_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(rentalRepository.findConflictingRentals(anyLong(), any(), any(), anyList()))
            .thenReturn(Collections.emptyList());
        when(rentalRepository.save(any(Rental.class))).thenReturn(testRental);
        when(rentalMapper.toResponse(testRental)).thenReturn(RentalResponse.builder()
            .id(1L)
            .userId(1L)
            .vehicleId(1L)
            .status(RentalStatus.PENDING)
            .build());
        
        RentalResponse response = rentalService.createRental(1L, createRequest);
        
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(rentalRepository, times(1)).save(any(Rental.class));
    }
    
    @Test
    void testCreateRental_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(RentalException.class, () -> rentalService.createRental(1L, createRequest));
    }
    
    @Test
    void testCreateRental_VehicleNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(RentalException.class, () -> rentalService.createRental(1L, createRequest));
    }
    
    @Test
    void testCreateRental_InvalidDates() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        
        LocalDateTime now = LocalDateTime.now();
        CreateRentalRequest invalidRequest = CreateRentalRequest.builder()
            .vehicleId(1L)
            .pickupDate(now.plusDays(5))
            .returnDate(now.plusDays(2))
            .pickupLocation("Airport")
            .returnLocation("Downtown")
            .build();
        
        assertThrows(RentalException.class, () -> rentalService.createRental(1L, invalidRequest));
    }
    
    @Test
    void testCreateRental_VehicleNotAvailable() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(rentalRepository.findConflictingRentals(anyLong(), any(), any(), anyList()))
            .thenReturn(java.util.Arrays.asList(testRental));
        
        assertThrows(BookingConflictException.class, () -> rentalService.createRental(1L, createRequest));
    }
    
    @Test
    void testGetRentalById_Success() {
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        when(rentalMapper.toResponse(testRental)).thenReturn(RentalResponse.builder()
            .id(1L)
            .userId(1L)
            .status(RentalStatus.PENDING)
            .build());
        
        RentalResponse response = rentalService.getRentalById(1L);
        
        assertNotNull(response);
        assertEquals(1L, response.getId());
    }
    
    @Test
    void testGetRentalById_NotFound() {
        when(rentalRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(RentalException.class, () -> rentalService.getRentalById(1L));
    }
    
    @Test
    void testCancelRental_Success() {
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(testRental);
        when(rentalMapper.toResponse(testRental)).thenReturn(RentalResponse.builder()
            .id(1L)
            .status(RentalStatus.CANCELLED)
            .build());
        
        RentalResponse response = rentalService.cancelRental(1L, 1L);
        
        assertNotNull(response);
        verify(rentalRepository, times(1)).save(any(Rental.class));
    }
    
    @Test
    void testCancelRental_Unauthorized() {
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        
        assertThrows(RentalException.class, () -> rentalService.cancelRental(1L, 2L));
    }
    
    @Test
    void testConfirmRental_Success() {
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(testRental);
        when(rentalMapper.toResponse(testRental)).thenReturn(RentalResponse.builder()
            .id(1L)
            .status(RentalStatus.CONFIRMED)
            .build());
        
        RentalResponse response = rentalService.confirmRental(1L);
        
        assertNotNull(response);
        verify(rentalRepository, times(1)).save(any(Rental.class));
    }
    
    @Test
    void testUpdateRental_Success() {
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        when(rentalRepository.findConflictingRentals(anyLong(), any(), any(), anyList()))
            .thenReturn(Collections.emptyList());
        when(rentalRepository.save(any(Rental.class))).thenReturn(testRental);
        when(rentalMapper.toResponse(testRental)).thenReturn(RentalResponse.builder()
            .id(1L)
            .status(RentalStatus.PENDING)
            .build());
        
        LocalDateTime newPickup = LocalDateTime.now().plusDays(3);
        LocalDateTime newReturn = LocalDateTime.now().plusDays(6);
        UpdateRentalRequest updateRequest = UpdateRentalRequest.builder()
            .pickupDate(newPickup)
            .returnDate(newReturn)
            .build();
        
        RentalResponse response = rentalService.updateRental(1L, 1L, updateRequest);
        
        assertNotNull(response);
        verify(rentalRepository, times(1)).save(any(Rental.class));
    }
}
