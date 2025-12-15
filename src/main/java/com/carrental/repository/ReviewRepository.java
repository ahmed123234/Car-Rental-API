package com.carrental.repository;

import com.carrental.entity.Review;
import com.carrental.entity.enums.ReviewStatus;
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
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Page<Review> findByVehicleIdAndStatus(Long vehicleId, ReviewStatus status, Pageable pageable);
    
    Page<Review> findByVehicleId(Long vehicleId, Pageable pageable);
    
    List<Review> findByUserId(Long userId);
    
    List<Review> findByStatus(ReviewStatus status);
    
    @Query("SELECT r FROM Review r WHERE r.vehicle.id = :vehicleId AND r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    Page<Review> findApprovedReviewsByVehicle(@Param("vehicleId") Long vehicleId, Pageable pageable);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.vehicle.id = :vehicleId AND r.status = 'APPROVED'")
    Optional<Double> findAverageRatingByVehicle(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT r FROM Review r WHERE r.rental.id = :rentalId")
    Optional<Review> findByRentalId(@Param("rentalId") Long rentalId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.vehicle.id = :vehicleId AND r.status = 'APPROVED'")
    long countApprovedReviewsByVehicle(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT r FROM Review r WHERE r.status = 'FLAGGED' AND r.createdAt >= :since ORDER BY r.createdAt DESC")
    List<Review> findFlaggedReviewsSince(@Param("since") LocalDateTime since);
    
    @Query(value = "SELECT rating, COUNT(*) as count FROM reviews WHERE vehicle_id = :vehicleId AND status = 'APPROVED' GROUP BY rating ORDER BY rating DESC",
           nativeQuery = true)
    List<Object[]> findRatingDistributionByVehicle(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT r FROM Review r WHERE r.vehicle.id = :vehicleId AND r.status = 'APPROVED' ORDER BY r.helpfulCount DESC")
    Page<Review> findMostHelpfulReviewsByVehicle(@Param("vehicleId") Long vehicleId, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.vehicle.id = :vehicleId AND r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    Page<Review> findRecentReviewsByVehicle(@Param("vehicleId") Long vehicleId, Pageable pageable);
}