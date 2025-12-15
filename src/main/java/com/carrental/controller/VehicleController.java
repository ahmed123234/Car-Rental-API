// Vehicle Controller
package com.carrental.controller;

import com.carrental.dto.request.CreateVehicleRequest;
import com.carrental.dto.request.UpdateVehicleRequest;
import com.carrental.dto.response.VehicleDTO;
import com.carrental.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Vehicle Controller
 * REST endpoints for vehicle management
 */
@RestController
@RequestMapping("/vehicles")
@Tag(name = "Vehicles", description = "Vehicle management endpoints")
public class VehicleController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

    @Autowired
    private VehicleService vehicleService;

    /**
     * Get all vehicles with pagination
     * GET /vehicles
     */
    @GetMapping
    @Operation(
            summary = "Get all vehicles",
            description = "Retrieve all vehicles with pagination and filtering"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<Map<String, Object>> getAllVehicles(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

        logger.info("Get all vehicles request: page={}, size={}, sortBy={}, direction={}", 
            page, size, sortBy, direction);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<VehicleDTO> vehiclesPage = vehicleService.getAllVehicles(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", "VEHICLES_RETRIEVED");
        response.put("message", "Vehicles retrieved successfully");
        response.put("data", vehiclesPage.getContent());
        response.put("pagination", Map.of(
            "totalPages", vehiclesPage.getTotalPages(),
            "totalElements", vehiclesPage.getTotalElements(),
            "currentPage", vehiclesPage.getNumber(),
            "pageSize", vehiclesPage.getSize()
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * Get vehicle by ID
     * GET /vehicles/{id}
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get vehicle by ID",
            description = "Retrieve vehicle details by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<Map<String, Object>> getVehicleById(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {

        logger.info("Get vehicle by ID: {}", id);

        VehicleDTO vehicleDTO = vehicleService.getVehicleById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", "VEHICLE_RETRIEVED");
        response.put("message", "Vehicle retrieved successfully");
        response.put("data", vehicleDTO);

        return ResponseEntity.ok(response);
    }

    /**
     * Create new vehicle (Admin only)
     * POST /vehicles
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create new vehicle",
            description = "Create a new vehicle in the system (Admin only)"
    )
    @SecurityRequirement(name = "Bearer Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehicle created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Vehicle already exists")
    })
    public ResponseEntity<Map<String, Object>> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request) {

        logger.info("Create vehicle request: licensePlate={}", request.getLicensePlate());

        VehicleDTO vehicleDTO = vehicleService.createVehicle(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", "VEHICLE_CREATED");
        response.put("message", "Vehicle created successfully");
        response.put("data", vehicleDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update vehicle (Admin only)
     * PUT /vehicles/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update vehicle",
            description = "Update vehicle information (Admin only)"
    )
    @SecurityRequirement(name = "Bearer Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<Map<String, Object>> updateVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleRequest request) {

        logger.info("Update vehicle request: id={}", id);

        VehicleDTO vehicleDTO = vehicleService.updateVehicle(id, request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", "VEHICLE_UPDATED");
        response.put("message", "Vehicle updated successfully");
        response.put("data", vehicleDTO);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete vehicle (Admin only)
     * DELETE /vehicles/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete vehicle",
            description = "Delete a vehicle from the system (Admin only)"
    )
    @SecurityRequirement(name = "Bearer Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<Map<String, Object>> deleteVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {

        logger.info("Delete vehicle request: id={}", id);

        vehicleService.deleteVehicle(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", "VEHICLE_DELETED");
        response.put("message", "Vehicle deleted successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get vehicles by type
     * GET /vehicles/type/{type}
     */
    @GetMapping("/type/{type}")
    @Operation(
            summary = "Get vehicles by type",
            description = "Retrieve vehicles filtered by type"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<Map<String, Object>> getVehiclesByType(
            @Parameter(description = "Vehicle type") @PathVariable String type,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        logger.info("Get vehicles by type: type={}", type);

        Pageable pageable = PageRequest.of(page, size);
        Page<VehicleDTO> vehiclesPage = vehicleService.getVehiclesByType(type, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", "VEHICLES_RETRIEVED");
        response.put("message", "Vehicles retrieved successfully");
        response.put("data", vehiclesPage.getContent());
        response.put("pagination", Map.of(
            "totalPages", vehiclesPage.getTotalPages(),
            "totalElements", vehiclesPage.getTotalElements(),
            "currentPage", vehiclesPage.getNumber()
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * Get vehicles by price range
     * GET /vehicles/price
     */
    @GetMapping("/price")
    @Operation(
            summary = "Get vehicles by price range",
            description = "Retrieve vehicles filtered by daily rate range"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getVehiclesByPrice(
            @Parameter(description = "Minimum daily rate") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum daily rate") @RequestParam BigDecimal maxPrice,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        logger.info("Get vehicles by price range: minPrice={}, maxPrice={}", minPrice, maxPrice);

        Pageable pageable = PageRequest.of(page, size);
        Page<VehicleDTO> vehiclesPage = vehicleService.getVehiclesByPriceRange(minPrice, maxPrice, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", "VEHICLES_RETRIEVED");
        response.put("message", "Vehicles retrieved successfully");
        response.put("data", vehiclesPage.getContent());
        response.put("pagination", Map.of(
            "totalPages", vehiclesPage.getTotalPages(),
            "totalElements", vehiclesPage.getTotalElements(),
            "currentPage", vehiclesPage.getNumber()
        ));

        return ResponseEntity.ok(response);
    }
}