package com.carrental.repository;

import com.carrental.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByRentalId(Long rentalId);
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}
