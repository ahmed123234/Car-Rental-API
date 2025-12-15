package com.carrental.service;

import com.carrental.dto.request.CreateVehicleRequest;
import com.carrental.dto.request.UpdateVehicleRequest;
import com.carrental.dto.response.VehicleDTO;
import com.carrental.entity.Vehicle;
import com.carrental.entity.enums.TransmissionType;
import com.carrental.entity.enums.VehicleStatus;
import com.carrental.entity.enums.VehicleType;
import com.carrental.exception.ResourceNotFoundException;
import com.carrental.exception.ValidationException;
import com.carrental.mapper.VehicleMapper;
import com.carrental.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VehicleServiceTests {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleMapper vehicleMapper;

    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();

        testVehicle = Vehicle.builder()
                .licensePlate("ABC123")
                .vin("WVWZZZ3CZ9E123456")
                .make("Toyota")
                .model("Camry")
                .year(2024)
                .color("White")
                .transmission(TransmissionType.AUTOMATIC)
                .dailyRate(new BigDecimal("75.00"))
                .type(VehicleType.SEDAN)
                .status(VehicleStatus.ACTIVE)
                .mileage(5000)
                .build();

        testVehicle = vehicleRepository.save(testVehicle);
    }

    @Test
    void testGetVehicleById() {
        VehicleDTO vehicleDTO = vehicleService.getVehicleById(testVehicle.getId());

        assertThat(vehicleDTO).isNotNull();
        assertThat(vehicleDTO.getLicensePlate()).isEqualTo("ABC123");
        assertThat(vehicleDTO.getMake()).isEqualTo("Toyota");
    }

    @Test
    void testGetVehicleByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> vehicleService.getVehicleById(999L));
    }

    @Test
    void testCreateVehicle() {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .licensePlate("XYZ789")
                .vin("WVWZZZ3CZ9E654321")
                .make("Honda")
                .model("Civic")
                .year(2023)
                .color("Black")
                .transmission(TransmissionType.MANUAL)
                .dailyRate(new BigDecimal("50.00"))
                .type(VehicleType.SEDAN)
                .build();

        VehicleDTO vehicleDTO = vehicleService.createVehicle(request);

        assertThat(vehicleDTO).isNotNull();
        assertThat(vehicleDTO.getLicensePlate()).isEqualTo("XYZ789");
        assertThat(vehicleDTO.getStatus()).isEqualTo(VehicleStatus.ACTIVE);
    }

    @Test
    void testCreateVehicleDuplicateLicensePlate() {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .licensePlate("ABC123")
                .vin("WVWZZZ3CZ9E999999")
                .make("Honda")
                .model("Civic")
                .year(2023)
                .transmission(TransmissionType.MANUAL)
                .dailyRate(new BigDecimal("50.00"))
                .type(VehicleType.SEDAN)
                .build();

        assertThrows(ValidationException.class, () -> vehicleService.createVehicle(request));
    }

    @Test
    void testUpdateVehicle() {
        UpdateVehicleRequest request = UpdateVehicleRequest.builder()
                .color("Red")
                .dailyRate(new BigDecimal("85.00"))
                .mileage(6000)
                .build();

        VehicleDTO vehicleDTO = vehicleService.updateVehicle(testVehicle.getId(), request);

        assertThat(vehicleDTO.getColor()).isEqualTo("Red");
        assertThat(vehicleDTO.getDailyRate()).isEqualTo(new BigDecimal("85.00"));
        assertThat(vehicleDTO.getMileage()).isEqualTo(6000);
    }

    @Test
    void testDeleteVehicle() {
        vehicleService.deleteVehicle(testVehicle.getId());

        Vehicle deletedVehicle = vehicleRepository.findById(testVehicle.getId()).orElseThrow();
        assertThat(deletedVehicle.getStatus()).isEqualTo(VehicleStatus.INACTIVE);
    }

    @Test
    void testGetAllVehicles() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<VehicleDTO> vehiclesPage = vehicleService.getAllVehicles(pageable);

        assertThat(vehiclesPage.getContent()).isNotEmpty();
        assertThat(vehiclesPage.getTotalElements()).isGreaterThan(0);
    }

    @Test
    void testGetVehiclesByType() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<VehicleDTO> vehiclesPage = vehicleService.getVehiclesByType("SEDAN", pageable);

        assertThat(vehiclesPage.getContent()).isNotEmpty();
    }

    @Test
    void testIsVehicleAvailable() {
        java.time.LocalDateTime startDate = java.time.LocalDateTime.now().plusDays(1);
        java.time.LocalDateTime endDate = java.time.LocalDateTime.now().plusDays(5);

        boolean available = vehicleService.isVehicleAvailable(testVehicle.getId(), startDate, endDate);

        assertThat(available).isTrue();
    }

    @Test
    void testLicensePlateExists() {
        boolean exists = vehicleService.licensePlateExists("ABC123");

        assertThat(exists).isTrue();
    }

    @Test
    void testVinExists() {
        boolean exists = vehicleService.vinExists("WVWZZZ3CZ9E123456");

        assertThat(exists).isTrue();
    }
}
