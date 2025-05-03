package com.cnpm.bookingflight.mapper;

import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Ticket;
import com.cnpm.bookingflight.dto.request.TicketRequest;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.repository.FlightRepository;
import com.cnpm.bookingflight.repository.SeatRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class TicketMapper {
    final FlightRepository flightRepository;
    final SeatRepository seatRepository;

    public Ticket toTicket(TicketRequest request) {
        return Ticket.builder()
                .flight(flightRepository.findById(request.getFlightId())
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID)))
                .seat(seatRepository.findById(request.getSeatId())
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID)))
                .passengerEmail(request.getPassengerEmail())
                .passengerName(request.getPassengerName())
                .passengerPhone(request.getPassengerPhone())
                .passengerIDCard(request.getPassengerIDCard())
                .isPaid(request.getIsPaid())
                .build();
    }
}
