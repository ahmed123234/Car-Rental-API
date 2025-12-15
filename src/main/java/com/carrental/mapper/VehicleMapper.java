package com.carrental.mapper;

import com.carrental.dto.response.VehicleDTO;
import com.carrental.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Vehicle Mapper
 * Maps between Vehicle entity and VehicleDTO
 */
@Mapper
public interface VehicleMapper {

    VehicleMapper INSTANCE = Mappers.getMapper(VehicleMapper.class);

    /**
     * Convert Vehicle entity to VehicleDTO
     * 
     * @param vehicle Vehicle entity
     * @return VehicleDTO
     */
    VehicleDTO toDTO(Vehicle vehicle);

    /**
     * Convert VehicleDTO to Vehicle entity
     * 
     * @param vehicleDTO VehicleDTO
     * @return Vehicle entity
     */
    Vehicle toEntity(VehicleDTO vehicleDTO);
}