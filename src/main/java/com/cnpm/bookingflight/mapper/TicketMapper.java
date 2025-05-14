package com.cnpm.bookingflight.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Ticket;
import com.cnpm.bookingflight.dto.request.TicketRequest;
import com.cnpm.bookingflight.dto.response.TicketResponse;
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
        final FlightMapper flightMapper;

        public Ticket updateTicket(Ticket ticket, TicketRequest request) {
                Ticket updatedTicket = this.toTicket(request);
                updatedTicket.setId(ticket.getId());
                return updatedTicket;
        }

        public Ticket toTicket(TicketRequest request) {
                TicketRequest.TicketInfo ticketInfo = null;
                if (request.getTickets() != null && !request.getTickets().isEmpty()) {
                        ticketInfo = request.getTickets().get(0);
                } else {
                        throw new AppException(ErrorCode.INVALID);
                }

                Long seatId = ticketInfo.getSeatId();
                if (seatId == null) {
                        throw new AppException(ErrorCode.INVALID);
                }

                return Ticket.builder()
                        .flight(flightRepository.findById(request.getFlightId())
                                .orElseThrow(() -> new AppException(ErrorCode.INVALID)))
                        .seat(seatRepository.findById(seatId)
                                .orElseThrow(() -> new AppException(ErrorCode.INVALID)))
                        .passengerEmail(ticketInfo.getPassengerEmail())
                        .passengerName(ticketInfo.getPassengerName())
                        .passengerPhone(ticketInfo.getPassengerPhone())
                        .passengerIDCard(ticketInfo.getPassengerIDCard())
                        .build();
        }

        public TicketResponse toTicketResponse(Ticket ticket) {
                return TicketResponse.builder()
                        .id(ticket.getId())
                        .flight(flightMapper.toFlightTicketResponse(ticket.getFlight()))
                        .seat(ticket.getSeat())
                        .passengerEmail(ticket.getPassengerEmail())
                        .passengerPhone(ticket.getPassengerPhone())
                        .passengerIDCard(ticket.getPassengerIDCard())
                        .passengerName(ticket.getPassengerName())
                        .build();
        }

        public List<TicketResponse> toTicketResponseList(List<Ticket> tickets) {
                return tickets.stream()
                        .map(this::toTicketResponse)
                        .toList();
        }
}