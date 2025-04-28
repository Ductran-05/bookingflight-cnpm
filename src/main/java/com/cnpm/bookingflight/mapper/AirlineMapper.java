package com.cnpm.bookingflight.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cnpm.bookingflight.domain.Airline;
import com.cnpm.bookingflight.dto.request.AirlineRequest;

@Mapper(componentModel = "spring")
public interface AirlineMapper {
    @Mapping(target = "id", ignore = true)
    public Airline toAirline(AirlineRequest request);
}
