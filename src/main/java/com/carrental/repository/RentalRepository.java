package com.carrental.repository;

import com.carrental.entity.Rental;
import com.carrental.entity.enums.RentalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    
    Page<Rental> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT r FROM Rental r WHERE r.status = :status AND r.user.id = :userId")
    Page<Rental> findByUserIdAndStatus(Long userId, RentalStatus status, Pageable pageable);
    
    @Query("SELECT r FROM Rental r WHERE r.vehicle.id = :vehicleId AND r.status IN :statuses " +
           "AND NOT (r.returnDate < :pickupDate OR r.pickupDate > :returnDate)")
    List<Rental> findConflictingRentals(@Param("vehicleId") Long vehicleId,
                                        @Param("pickupDate") LocalDateTime pickupDate,
                                        @Param("returnDate") LocalDateTime returnDate,
                                        @Param("statuses") List<RentalStatus> statuses);
    
    @Query("SELECT r FROM Rental r WHERE r.vehicle.id = :vehicleId AND r.status IN ('PENDING', 'CONFIRMED', 'ACTIVE')")
    List<Rental> findActiveRentalsByVehicle(@Param("vehicleId") Long vehicleId);
    
    Optional<Rental> findById(Long id);
    
    @Query("SELECT COUNT(r) FROM Rental r WHERE r.vehicle.id = :vehicleId AND r.status = 'COMPLETED'")
    long countCompletedRentalsByVehicle(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT SUM(r.totalCost) FROM Rental r WHERE r.status = 'COMPLETED' AND r.createdAt >= :startDate")
    Optional<java.math.BigDecimal> calculateRevenueFromDate(@Param("startDate") LocalDateTime startDate);
}
