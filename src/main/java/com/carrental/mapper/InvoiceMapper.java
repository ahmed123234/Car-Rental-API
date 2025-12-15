package com.carrental.mapper;

import com.carrental.dto.response.InvoiceResponse;
import com.carrental.entity.Invoice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class InvoiceMapper {
    
    public InvoiceResponse toResponse(Invoice invoice) {
        if (invoice == null) {
            return null;
        }
        
        long rentalDays = ChronoUnit.DAYS.between(
            invoice.getRental().getPickupDate(),
            invoice.getRental().getReturnDate()
        );
        
        String rentalPeriod = invoice.getRental().getPickupDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) 
            + " to " + invoice.getRental().getReturnDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        
        return InvoiceResponse.builder()
            .id(invoice.getId())
            .rentalId(invoice.getRental().getId())
            .invoiceNumber(invoice.getInvoiceNumber())
            .vehicleMakeModel(invoice.getRental().getVehicle().getMake() + " " + invoice.getRental().getVehicle().getModel())
            .rentalPeriod(rentalPeriod)
            .dailyRate(invoice.getRental().getDailyRate())
            .rentalDays(rentalDays)
            .subtotal(invoice.getSubtotal())
            .taxes(invoice.getTaxes())
            .discount(invoice.getDiscount())
            .totalAmount(invoice.getTotalAmount())
            .notes(invoice.getNotes())
            .createdAt(invoice.getCreatedAt())
            .build();
    }
}
