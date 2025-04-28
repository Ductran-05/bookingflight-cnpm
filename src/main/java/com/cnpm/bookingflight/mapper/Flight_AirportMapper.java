package com.cnpm.bookingflight.mapper;

import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Flight_Airport;
import com.cnpm.bookingflight.domain.id.Flight_AirportId;
import com.cnpm.bookingflight.dto.request.Flight_AirportRequest;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.repository.AirportRepository;
import com.cnpm.bookingflight.repository.FlightRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Flight_AirportMapper {
        final AirportRepository airportRepository;
        final FlightRepository flightRepository;

        public Flight_Airport toFlight_Airport(Flight_AirportRequest request, Long flightId) {
                return Flight_Airport.builder()
                                .id(new Flight_AirportId(flightId, request.getAirportId()))
                                .flight(flightRepository.findById(flightId)
                                                .orElseThrow(() -> new AppException(ErrorCode.INVALID)))
                                .airport(airportRepository.findById(request.getAirportId())
                                                .orElseThrow(() -> new AppException(ErrorCode.INVALID)))
                                .departureDate(request.getDepartureDate())
                                .arrivalDate(request.getArrivalDate())
                                .departureTime(request.getDepartureTime())
                                .arrivalTime(request.getArrivalTime())
                                .note(request.getNote())
                                .build();
        }
}
