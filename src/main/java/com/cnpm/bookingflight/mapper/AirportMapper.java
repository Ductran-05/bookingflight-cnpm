package com.cnpm.bookingflight.mapper;

import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Airport;
import com.cnpm.bookingflight.dto.request.AirportRequest;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.repository.CityRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class AirportMapper {
    final CityRepository cityRepository;

    public Airport toAirport(AirportRequest request) {
        return Airport.builder()
                .airportCode(request.getAirportCode())
                .airportName(request.getAirportName())
                .city(cityRepository.findById(request.getCityId())
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID)))
                .build();
    }
}
