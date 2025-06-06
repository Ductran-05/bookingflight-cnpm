package com.cnpm.bookingflight.controller;

import com.cnpm.bookingflight.domain.Seat;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.SeatRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.SeatResponse;
import com.cnpm.bookingflight.service.SeatService;
import com.turkraft.springfilter.boot.Filter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatController {
    final SeatService seatService;

    @GetMapping()
    public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllSeats(@Filter Specification<Seat> spec,
                                                                        Pageable pageable) {
        return seatService.getAllSeats(spec, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<SeatResponse>> getSeatById(@PathVariable("id") Long id) {
        return seatService.getSeatById(id);
    }

    @PostMapping()
    public ResponseEntity<APIResponse<SeatResponse>> createSeat(@RequestBody SeatRequest request) {
        return seatService.createSeat(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<SeatResponse>> updateSeat(@PathVariable("id") Long id,
                                                                @RequestBody SeatRequest request) {
        return seatService.updateSeat(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteSeat(@PathVariable("id") Long id) {
        return seatService.deleteSeat(id);
    }
}