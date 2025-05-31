package com.cnpm.bookingflight.controller;

import com.cnpm.bookingflight.domain.Ticket;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.TicketRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.BookingRateResponse;
import com.cnpm.bookingflight.dto.response.RevenueResponse;
import com.cnpm.bookingflight.dto.response.TicketRefundCheckResponse;
import com.cnpm.bookingflight.dto.response.TicketResponse;
import com.cnpm.bookingflight.service.TicketService;
import com.turkraft.springfilter.boot.Filter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketController {

    final TicketService ticketService;

    @GetMapping()
    public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllTickets(@Filter Specification<Ticket> spec,
                                                                          Pageable pageable) {
        return ticketService.getAllTickets(spec, pageable);
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

    @GetMapping("/revenue")
    public ResponseEntity<APIResponse<RevenueResponse>> getRevenue(@RequestParam("period") String period) {
        return ticketService.getRevenue(period);
    }

    @GetMapping("/booking-rate")
    public ResponseEntity<APIResponse<BookingRateResponse>> getBookingRate() {
        return ticketService.getBookingRate();
    }

    @GetMapping("/{id}/refund-check")
    public ResponseEntity<APIResponse<TicketRefundCheckResponse>> checkRefund(@PathVariable("id") Long ticketId) {
        return ticketService.checkRefund(ticketId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteTicket(@PathVariable("id") Long ticketId) {
        return ticketService.deleteTicket(ticketId);
    }
}