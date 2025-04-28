package com.cnpm.bookingflight.mapper;

import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Flight_Seat;
import com.cnpm.bookingflight.domain.id.Flight_SeatId;
import com.cnpm.bookingflight.dto.request.Flight_SeatRequest;
import com.cnpm.bookingflight.repository.SeatRepository;
import com.cnpm.bookingflight.repository.FlightRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Flight_SeatMapper {
        final SeatRepository seatRepository;
        final FlightRepository flightRepository;

        public Flight_Seat toFlight_Seat(Flight_SeatRequest request, Long flightId) {
                return Flight_Seat.builder()
                                .id(new Flight_SeatId(flightId, request.getSeatId()))
                                .flight(flightRepository.findById(flightId).orElseThrow())
                                .seat(seatRepository.findById(request.getSeatId()).orElseThrow())
                                .quantity(request.getQuantity())
                                .remainingTickets(request.getQuantity())
                                .price(flightRepository.findById(flightId).orElseThrow().getOriginalPrice()
                                                * seatRepository.findById(request.getSeatId()).orElseThrow().getPrice())
                                .build();
        }
}
