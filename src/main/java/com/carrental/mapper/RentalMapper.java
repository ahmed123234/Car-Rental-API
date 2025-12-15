package com.carrental.mapper;

import com.carrental.dto.response.RentalResponse;
import com.carrental.entity.Rental;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class RentalMapper {
    
    public RentalResponse toResponse(Rental rental) {
        if (rental == null) {
            return null;
        }
        
        long rentalDays = ChronoUnit.DAYS.between(rental.getPickupDate(), rental.getReturnDate());
        
        return RentalResponse.builder()
            .id(rental.getId())
            .userId(rental.getUser().getId())
            .vehicleId(rental.getVehicle().getId())
            .vehicleMakeModel(rental.getVehicle().getMake() + " " + rental.getVehicle().getModel())
            .pickupDate(rental.getPickupDate())
            .returnDate(rental.getReturnDate())
            .pickupLocation(rental.getPickupLocation())
            .returnLocation(rental.getReturnLocation())
            .dailyRate(rental.getDailyRate())
            .totalCost(rental.getTotalCost())
            .additionalFees(rental.getAdditionalFees())
            .status(rental.getStatus())
            .specialRequests(rental.getSpecialRequests())
            .rentalDays(rentalDays)
            .createdAt(rental.getCreatedAt())
            .updatedAt(rental.getUpdatedAt())
            .build();
    }
}