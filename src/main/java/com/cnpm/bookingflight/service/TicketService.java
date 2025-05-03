package com.cnpm.bookingflight.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cnpm.bookingflight.domain.Ticket;
import com.cnpm.bookingflight.dto.request.TicketRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.TicketMapper;
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

    public ResponseEntity<APIResponse<List<Ticket>>> getAllTickets() {
        APIResponse<List<Ticket>> response = APIResponse.<List<Ticket>>builder()
                .data(ticketRepository.findAll())
                .status(200)
                .message("get all tickets successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Ticket>> getTicketById(Long id) {
        APIResponse<Ticket> response = APIResponse.<Ticket>builder()
                .data(ticketRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)))
                .status(200)
                .message("get plane by id successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Ticket>> bookingTicket(TicketRequest request) {
        flight_SeatService.bookingTicket(request);
        APIResponse<Ticket> response = APIResponse.<Ticket>builder()
                .status(201)
                .message("Booking ticket successfully")
                .data(ticketRepository.save(ticketMapper.toTicket(request)))
                .build();
        return ResponseEntity.ok(response);
    }
    // b1: dien thong tin gom flight ID va Seat ID
    // b2: xet coi flight_seat co remaining >0 hay khong
    // b3: tru di 1, tao ve
}
