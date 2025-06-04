package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.Airline;
import com.cnpm.bookingflight.dto.request.AirlineRequest;
import com.cnpm.bookingflight.dto.response.AirlineResponse;
import com.cnpm.bookingflight.repository.PlaneRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class AirlineMapper {

    @Autowired
    protected PlaneRepository planeRepository;

    @Mapping(target = "id", ignore = true)
    public abstract Airline toAirline(AirlineRequest request);

    public AirlineResponse toAirlineResponse(Airline airline) {
        boolean hasForeignKey = planeRepository.existsByAirlineId(airline.getId());
        return AirlineResponse.builder()
                .id(airline.getId())
                .airlineCode(airline.getAirlineCode())
                .airlineName(airline.getAirlineName())
                .logo(airline.getLogo())
                .canDelete(!hasForeignKey)
                .build();
    }
}