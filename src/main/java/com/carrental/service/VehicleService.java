// Vehicle Service
package com.carrental.service;

import com.carrental.dto.request.CreateVehicleRequest;
import com.carrental.dto.request.UpdateVehicleRequest;
import com.carrental.dto.response.VehicleDTO;
import com.carrental.entity.Vehicle;
import com.carrental.entity.enums.VehicleStatus;
import com.carrental.exception.ResourceNotFoundException;
import com.carrental.exception.ValidationException;
import com.carrental.mapper.VehicleMapper;
import com.carrental.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vehicle Service
 * Handles vehicle management and availability operations
 */
@Service
@Transactional
public class VehicleService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleService.class);

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleMapper vehicleMapper;

    /**
     * Get all vehicles with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of vehicle DTOs
     */
    public Page<VehicleDTO> getAllVehicles(Pageable pageable) {
        logger.debug("Fetching all vehicles with pagination: page={}, size={}", 
            pageable.getPageNumber(), pageable.getPageSize());
        
        return vehicleRepository.findAll(pageable)
                .map(vehicleMapper::toDTO);
    }

    /**
     * Get vehicle by ID with caching
     * 
     * @param id Vehicle ID
     * @return Vehicle DTO
     * @throws ResourceNotFoundException if vehicle not found
     */
    @Cacheable(value = "vehicles", key = "#id")
    public VehicleDTO getVehicleById(Long id) {
        logger.debug("Fetching vehicle with ID: {}", id);
        
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));
        
        return vehicleMapper.toDTO(vehicle);
    }

    /**
     * Create new vehicle
     * 
     * @param request Create vehicle request
     * @return Created vehicle DTO
     * @throws ValidationException if license plate or VIN already exists
     */
    @CacheEvict(value = "vehicles", allEntries = true)
    public VehicleDTO createVehicle(CreateVehicleRequest request) {
        logger.info("Creating new vehicle with license plate: {}", request.getLicensePlate());

        // Check if license plate already exists
        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new ValidationException("License plate already exists: " + request.getLicensePlate());
        }

        // Check if VIN already exists
        if (vehicleRepository.existsByVin(request.getVin())) {
            throw new ValidationException("VIN already exists: " + request.getVin());
        }

        // Create vehicle entity
        Vehicle vehicle = Vehicle.builder()
                .licensePlate(request.getLicensePlate())
                .vin(request.getVin())
                .make(request.getMake())
                .model(request.getModel())
                .year(request.getYear())
                .color(request.getColor())
                .transmission(request.getTransmission())
                .dailyRate(request.getDailyRate())
                .type(request.getType())
                .status(VehicleStatus.ACTIVE)
                .mileage(0)
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        logger.info("Vehicle created successfully with ID: {}", savedVehicle.getId());

        return vehicleMapper.toDTO(savedVehicle);
    }

    /**
     * Update vehicle
     * 
     * @param id Vehicle ID
     * @param request Update vehicle request
     * @return Updated vehicle DTO
     * @throws ResourceNotFoundException if vehicle not found
     */
    @CacheEvict(value = "vehicles", key = "#id")
    public VehicleDTO updateVehicle(Long id, UpdateVehicleRequest request) {
        logger.info("Updating vehicle with ID: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));

        // Update allowed fields
        if (request.getColor() != null && !request.getColor().isEmpty()) {
            vehicle.setColor(request.getColor());
        }

        if (request.getDailyRate() != null && request.getDailyRate().compareTo(BigDecimal.ZERO) > 0) {
            vehicle.setDailyRate(request.getDailyRate());
        }

        if (request.getStatus() != null) {
            vehicle.setStatus(request.getStatus());
        }

        if (request.getMileage() != null && request.getMileage() >= 0) {
            vehicle.setMileage(request.getMileage());
        }

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        logger.info("Vehicle updated successfully with ID: {}", id);

        return vehicleMapper.toDTO(updatedVehicle);
    }

    /**
     * Delete vehicle (soft delete)
     * 
     * @param id Vehicle ID
     * @throws ResourceNotFoundException if vehicle not found
     */
    @CacheEvict(value = "vehicles", key = "#id")
    public void deleteVehicle(Long id) {
        logger.info("Deleting vehicle with ID: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));

        vehicle.setStatus(VehicleStatus.INACTIVE);
        vehicleRepository.save(vehicle);

        logger.info("Vehicle soft-deleted with ID: {}", id);
    }

    /**
     * Get available vehicles for given date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return List of available vehicle DTOs
     */
    public List<VehicleDTO> getAvailableVehicles(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Fetching available vehicles from {} to {}", startDate, endDate);

        List<Vehicle> availableVehicles = vehicleRepository.findAvailableVehicles(startDate, endDate);
        
        return availableVehicles.stream()
                .map(vehicleMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check vehicle availability for date range
     * 
     * @param vehicleId Vehicle ID
     * @param startDate Start date
     * @param endDate End date
     * @return true if vehicle is available, false otherwise
     */
    public boolean isVehicleAvailable(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Checking availability for vehicle ID: {} from {} to {}", vehicleId, startDate, endDate);

        return vehicleRepository.isVehicleAvailable(vehicleId, startDate, endDate);
    }

    /**
     * Get vehicles by type
     * 
     * @param type Vehicle type
     * @param pageable Pagination parameters
     * @return Page of vehicle DTOs
     */
    public Page<VehicleDTO> getVehiclesByType(String type, Pageable pageable) {
        logger.debug("Fetching vehicles by type: {}", type);

        return vehicleRepository.findByType(type, pageable)
                .map(vehicleMapper::toDTO);
    }

    /**
     * Get vehicles by price range
     * 
     * @param minPrice Minimum daily rate
     * @param maxPrice Maximum daily rate
     * @param pageable Pagination parameters
     * @return Page of vehicle DTOs
     */
    public Page<VehicleDTO> getVehiclesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        logger.debug("Fetching vehicles by price range: {} - {}", minPrice, maxPrice);

        return vehicleRepository.findByDailyRateBetween(minPrice, maxPrice, pageable)
                .map(vehicleMapper::toDTO);
    }

    /**
     * Update vehicle status
     * 
     * @param vehicleId Vehicle ID
     * @param status New status
     * @throws ResourceNotFoundException if vehicle not found
     */
    @CacheEvict(value = "vehicles", key = "#vehicleId")
    public void updateVehicleStatus(Long vehicleId, VehicleStatus status) {
        logger.info("Updating vehicle status for ID: {} to {}", vehicleId, status);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        vehicle.setStatus(status);
        vehicleRepository.save(vehicle);

        logger.info("Vehicle status updated successfully");
    }

    /**
     * Get total vehicles count
     * 
     * @return Total number of active vehicles
     */
    public long getTotalVehiclesCount() {
        logger.debug("Getting total vehicles count");
        
        return vehicleRepository.countByStatus(VehicleStatus.ACTIVE);
    }

    /**
     * Check if license plate exists
     * 
     * @param licensePlate License plate
     * @return true if exists, false otherwise
     */
    public boolean licensePlateExists(String licensePlate) {
        return vehicleRepository.existsByLicensePlate(licensePlate);
    }

    /**
     * Check if VIN exists
     * 
     * @param vin VIN number
     * @return true if exists, false otherwise
     */
    public boolean vinExists(String vin) {
        return vehicleRepository.existsByVin(vin);
    }
}