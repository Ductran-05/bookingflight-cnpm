package com.cnpm.bookingflight.service;

import org.springframework.stereotype.Service;

import com.cnpm.bookingflight.domain.Flight_Seat;
import com.cnpm.bookingflight.domain.id.Flight_SeatId;
import com.cnpm.bookingflight.dto.request.TicketRequest;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.repository.FlightRepository;
import com.cnpm.bookingflight.repository.Flight_SeatRepository;
import com.cnpm.bookingflight.repository.SeatRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Flight_SeatService {
    final FlightRepository flightRepository;
    final SeatRepository seatRepository;
    final Flight_SeatRepository flight_SeatRepository;

    public void bookingTicket(TicketRequest request) {
        flightRepository.findById(request.getFlightId()).orElseThrow(() -> new AppException(ErrorCode.INVALID));
        seatRepository.findById(request.getSeatId()).orElseThrow(() -> new AppException(ErrorCode.INVALID));

        Flight_Seat ticket = flight_SeatRepository
                .findById(new Flight_SeatId(request.getFlightId(), request.getSeatId()))
                .orElseThrow(() -> new AppException(ErrorCode.INVALID));
        if (ticket.getRemainingTickets() == 0)
            throw new AppException(ErrorCode.OUT_OF_TICKETS);

        ticket.setRemainingTickets(ticket.getRemainingTickets() - 1);
        flight_SeatRepository.save(ticket);
    }
}
