package com.cnpm.bookingflight.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cnpm.bookingflight.domain.Ticket;
import com.cnpm.bookingflight.domain.id.Flight_SeatId;
import com.cnpm.bookingflight.dto.request.TicketRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.TicketResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.TicketMapper;
import com.cnpm.bookingflight.repository.Flight_SeatRepository;
import com.cnpm.bookingflight.repository.TicketRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketService {

    final TicketRepository ticketRepository;

    final Flight_SeatService flight_SeatService;

    final TicketMapper ticketMapper;

    final Flight_SeatRepository flight_SeatRepository;

    public ResponseEntity<APIResponse<List<TicketResponse>>> getAllTickets() {
        APIResponse<List<TicketResponse>> response = APIResponse.<List<TicketResponse>>builder()
                .data(ticketMapper.toTicketResponseList(ticketRepository.findAll()))
                .status(200)
                .message("get all tickets successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<TicketResponse>> getTicketById(Long id) {
        APIResponse<TicketResponse> response = APIResponse.<TicketResponse>builder()
                .data(ticketMapper.toTicketResponse(ticketRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID))))
                .status(200)
                .message("get plane by id successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<TicketResponse>> bookingTicket(TicketRequest request) {
        flight_SeatService.bookingTicket(request);
        APIResponse<TicketResponse> response = APIResponse.<TicketResponse>builder()
                .status(201)
                .message("Booking ticket successfully")
                .data(ticketMapper.toTicketResponse(ticketRepository.save(ticketMapper.toTicket(request))))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Void>> deleteTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        flight_SeatRepository.deleteById(new Flight_SeatId(ticket.getFlight().getId(), ticket.getSeat().getId()));
        // xoa ve trong bang ve trung gian
        ticketRepository.deleteById(id);
        // xoa ve trong bang ve
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(200)
                .message("delete by id successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<TicketResponse>> updateTicket(Long id, TicketRequest request) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        // tim ve cu
        if (ticket.getSeat().getId() != request.getSeatId()) {
            flight_SeatRepository.deleteById(new Flight_SeatId(ticket.getFlight().getId(), ticket.getSeat().getId()));
            flight_SeatService.bookingTicket(request);
        }
        APIResponse<TicketResponse> response = APIResponse.<TicketResponse>builder()
                .status(200)
                .message("update ticket successfully")
                .data(ticketMapper.toTicketResponse(ticketRepository.save(ticketMapper.updateTicket(ticket, request))))
                .build();
        return ResponseEntity.ok(response);
    }
}
