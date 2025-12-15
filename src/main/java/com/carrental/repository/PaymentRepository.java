package com.carrental.repository;

import com.carrental.entity.Payment;
import com.carrental.entity.enums.PaymentStatus;
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
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    List<Payment> findByRentalId(Long rentalId);
    
    Page<Payment> findByUserId(Long userId, Pageable pageable);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.rental.id = :rentalId AND p.status = :status")
    Optional<Payment> findByRentalIdAndStatus(@Param("rentalId") Long rentalId, 
                                              @Param("status") PaymentStatus status);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED' AND p.createdAt >= :startDate")
    long countCompletedPaymentsSince(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED' AND p.createdAt >= :startDate")
    java.math.BigDecimal calculateTotalRevenueFrom(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT p FROM Payment p WHERE p.status IN ('PENDING', 'FAILED') AND p.createdAt < :expirationTime")
    List<Payment> findExpiredPendingPayments(@Param("expirationTime") LocalDateTime expirationTime);
}