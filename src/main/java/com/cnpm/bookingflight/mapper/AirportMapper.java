package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.Airport;
import com.cnpm.bookingflight.dto.request.AirportRequest;
import com.cnpm.bookingflight.dto.response.AirportResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.repository.CityRepository;
import com.cnpm.bookingflight.repository.FlightRepository;
import com.cnpm.bookingflight.repository.Flight_AirportRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class AirportMapper {
    final CityRepository cityRepository;
    final FlightRepository flightRepository;
    final Flight_AirportRepository flightAirportRepository;

    public Airport toAirport(AirportRequest request) {
        return Airport.builder()
                .airportCode(request.getAirportCode())
                .airportName(request.getAirportName())
                .city(cityRepository.findById(request.getCityId())
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID)))
                .build();
    }

    public Airport updateAirport(Airport airport, AirportRequest request) {
        Airport updatedAirport = this.toAirport(request);
        updatedAirport.setId(airport.getId());
        return updatedAirport;
    }

    public AirportResponse toAirportResponse(Airport airport) {
        boolean hasForeignKey = flightRepository.existsByDepartureAirportIdOrArrivalAirportId(airport.getId(), airport.getId())
                || flightAirportRepository.existsByAirportId(airport.getId());
        return AirportResponse.builder()
                .id(airport.getId())
                .airportCode(airport.getAirportCode())
                .airportName(airport.getAirportName())
                .cityName(airport.getCity().getCityName())
                .canDelete(!hasForeignKey)
                .build();
    }
}