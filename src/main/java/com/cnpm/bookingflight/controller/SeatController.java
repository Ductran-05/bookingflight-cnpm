package com.cnpm.bookingflight.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cnpm.bookingflight.domain.Seat;
import com.cnpm.bookingflight.dto.request.SeatRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.service.SeatService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatController {
    final SeatService seatService;

    @GetMapping()
    public ResponseEntity<APIResponse<List<Seat>>> getSeats() {
        return seatService.getAllSeats();
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<Seat>> getSeatById(@PathVariable("id") Long id) {
        return seatService.getSeatById(id);
    }

    @PostMapping()
    public ResponseEntity<APIResponse<Seat>> createSeat(@RequestBody SeatRequest request) {
        return seatService.createSeat(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<Seat>> updateSeat(@PathVariable("id") Long id,
            @RequestBody SeatRequest request) {
        return seatService.updateSeat(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteSeat(@PathVariable("id") Long id) {
        return seatService.deleteSeat(id);
    }
}
