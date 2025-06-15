package com.cnpm.bookingflight.mapper;

import java.util.List;

import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.domain.Flight;
import com.cnpm.bookingflight.domain.Seat;
import org.springframework.stereotype.Component;

import com.cnpm.bookingflight.domain.Ticket;
import com.cnpm.bookingflight.dto.request.TicketRequest;
import com.cnpm.bookingflight.dto.response.TicketResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.repository.AccountRepository;
import com.cnpm.bookingflight.repository.FlightRepository;
import com.cnpm.bookingflight.repository.SeatRepository;
import com.cnpm.bookingflight.repository.TicketRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class TicketMapper {
        final FlightRepository flightRepository;
        final SeatRepository seatRepository;
        final AccountRepository accountRepository;
        final FlightMapper flightMapper;
        final TicketRepository ticketRepository;

        public Ticket updateTicket(Ticket ticket, TicketRequest request) {
                TicketRequest.TicketInfo ticketInfo = request.getTickets().get(0);
                ticket.setFlight(flightRepository.findById(request.getFlightId())
                                .orElseThrow(() -> new AppException(ErrorCode.INVALID)));
                ticket.setSeat(seatRepository.findById(ticketInfo.getSeatId())
                                .orElseThrow(() -> new AppException(ErrorCode.INVALID)));
                ticket.setPassengerEmail(ticketInfo.getPassengerEmail());
                ticket.setPassengerName(ticketInfo.getPassengerName());
                ticket.setPassengerPhone(ticketInfo.getPassengerPhone());
                ticket.setPassengerIDCard(ticketInfo.getPassengerIDCard());
                ticket.setUserBooking(null); // Đặt userBooking là null cho POST /tickets
                return ticket;
        }

        public Ticket toTicket(TicketRequest.TicketInfo ticketInfo, Long flightId, Long userId) {
                Flight flight = flightRepository.findById(flightId)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                Seat seat = seatRepository.findById(ticketInfo.getSeatId())
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                Account userBooking = userId != null ? accountRepository.findById(userId)
                                .orElseThrow(() -> new AppException(ErrorCode.INVALID)) : null;
                String ticketCode = flight.getFlightCode() + "T" + (ticketRepository.countTicketsBySeat(seat) + 1);
                return Ticket.builder()
                                .flight(flight)
                                .ticketCode(ticketCode)
                                .seat(seat)
                                .passengerName(ticketInfo.getPassengerName())
                                .passengerEmail(ticketInfo.getPassengerEmail())
                                .passengerPhone(ticketInfo.getPassengerPhone())
                                .passengerIDCard(ticketInfo.getPassengerIDCard())
                                .userBooking(userBooking)
                                .isPaid(false)
                                .build();
        }

        public TicketResponse toTicketResponse(Ticket ticket) {
                return TicketResponse.builder()
                                .id(ticket.getId())
                                .flight(flightMapper.toFlightTicketResponse(ticket.getFlight()))
                                .seat(ticket.getSeat())
                                .ticketCode(ticket.getTicketCode())
                                .passengerEmail(ticket.getPassengerEmail())
                                .passengerPhone(ticket.getPassengerPhone())
                                .passengerIDCard(ticket.getPassengerIDCard())
                                .passengerName(ticket.getPassengerName())
                                .userBooking(ticket.getUserBooking()) // Thêm ánh xạ userBooking
                                .build();
        }

        public List<TicketResponse> toTicketResponseList(List<Ticket> tickets) {
                return tickets.stream()
                                .map(this::toTicketResponse)
                                .toList();
        }
}