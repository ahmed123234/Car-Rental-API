package com.carrental.repository;

import com.carrental.entity.Refund;
import com.carrental.entity.enums.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    
    List<Refund> findByPaymentId(Long paymentId);
    
    List<Refund> findByRentalId(Long rentalId);
    
    Page<Refund> findByStatus(RefundStatus status, Pageable pageable);
    
    @Query("SELECT r FROM Refund r WHERE r.status = 'PROCESSING' AND r.createdAt < :cutoffTime")
    List<Refund> findStuckProcessingRefunds(@Param("cutoffTime") LocalDateTime cutoffTime);
}
