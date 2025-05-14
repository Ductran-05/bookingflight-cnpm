package com.cnpm.bookingflight.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cnpm.bookingflight.dto.request.TicketRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.TicketResponse;
import com.cnpm.bookingflight.service.TicketService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketController {

    final TicketService ticketService;

    @GetMapping()
    public ResponseEntity<APIResponse<List<TicketResponse>>> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<TicketResponse>> getTicketById(@PathVariable("id") Long id) {
        return ticketService.getTicketById(id);
    }

    @PostMapping()
    public ResponseEntity<APIResponse<List<TicketResponse>>> bookingTicket(@RequestBody TicketRequest request) {
        return ticketService.bookingTicket(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<TicketResponse>> updateTicket(@PathVariable("id") Long id,
                                                                    @RequestBody TicketRequest request) {
        return ticketService.updateTicket(id, request);
    }
}