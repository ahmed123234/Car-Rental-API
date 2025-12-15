package com.carrental.repository;

import com.carrental.entity.Vehicle;
import com.carrental.entity.enums.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Vehicle Repository
 * Data access layer for Vehicle entity
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Find vehicle by license plate
     * 
     * @param licensePlate License plate
     * @return Optional containing vehicle if found
     */
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    /**
     * Find vehicle by VIN
     * 
     * @param vin VIN number
     * @return Optional containing vehicle if found
     */
    Optional<Vehicle> findByVin(String vin);

    /**
     * Check if license plate exists
     * 
     * @param licensePlate License plate
     * @return true if exists, false otherwise
     */
    boolean existsByLicensePlate(String licensePlate);

    /**
     * Check if VIN exists
     * 
     * @param vin VIN number
     * @return true if exists, false otherwise
     */
    boolean existsByVin(String vin);

    /**
     * Find vehicles by type with pagination
     * 
     * @param type Vehicle type
     * @param pageable Pagination parameters
     * @return Page of vehicles
     */
    Page<Vehicle> findByType(String type, Pageable pageable);

    /**
     * Find vehicles by status with pagination
     * 
     * @param status Vehicle status
     * @param pageable Pagination parameters
     * @return Page of vehicles
     */
    Page<Vehicle> findByStatus(VehicleStatus status, Pageable pageable);

    /**
     * Find vehicles by daily rate range with pagination
     * 
     * @param minPrice Minimum daily rate
     * @param maxPrice Maximum daily rate
     * @param pageable Pagination parameters
     * @return Page of vehicles
     */
    Page<Vehicle> findByDailyRateBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find available vehicles for date range
     * Vehicles that have no conflicting rentals
     * 
     * @param startDate Rental start date
     * @param endDate Rental end date
     * @return List of available vehicles
     */
    @Query("SELECT v FROM Vehicle v WHERE v.status = 'ACTIVE' AND v.id NOT IN " +
           "(SELECT DISTINCT r.vehicle.id FROM Rental r WHERE " +
           "r.status NOT IN ('CANCELLED', 'COMPLETED') AND " +
           "r.pickupDate < :endDate AND r.returnDate > :startDate)")
    List<Vehicle> findAvailableVehicles(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Check if vehicle is available for date range
     * 
     * @param vehicleId Vehicle ID
     * @param startDate Rental start date
     * @param endDate Rental end date
     * @return true if available, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(r) = 0 THEN true ELSE false END FROM Rental r " +
           "WHERE r.vehicle.id = :vehicleId AND r.status NOT IN ('CANCELLED', 'COMPLETED') " +
           "AND r.pickupDate < :endDate AND r.returnDate > :startDate")
    boolean isVehicleAvailable(@Param("vehicleId") Long vehicleId,
                               @Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);

    /**
     * Count vehicles by status
     * 
     * @param status Vehicle status
     * @return Count of vehicles with given status
     */
    long countByStatus(VehicleStatus status);

    /**
     * Find all active vehicles with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of active vehicles
     */
    Page<Vehicle> findByStatus(VehicleStatus status);

    /**
     * Find vehicles by make and model
     * 
     * @param make Vehicle make
     * @param model Vehicle model
     * @return List of matching vehicles
     */
    List<Vehicle> findByMakeAndModel(String make, String model);

    /**
     * Find vehicles by year
     * 
     * @param year Vehicle year
     * @param pageable Pagination parameters
     * @return Page of vehicles
     */
    Page<Vehicle> findByYear(Integer year, Pageable pageable);
}