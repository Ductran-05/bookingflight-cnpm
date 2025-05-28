package com.cnpm.bookingflight.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cnpm.bookingflight.domain.Flight;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.FlightRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.FlightResponse;
import com.cnpm.bookingflight.service.FlightService;
import com.turkraft.springfilter.boot.Filter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequestMapping("/flights")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class FlightController {

    final FlightService flightService;

    @GetMapping()
    public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllFlights(@Filter Specification<Flight> spec,
            Pageable pageable) {
        return flightService.getAllFlights(spec, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<FlightResponse>> getFlightById(@PathVariable("id") Long id) {
        return flightService.getFlightById(id);
    }

    @PostMapping()
    public ResponseEntity<APIResponse<FlightResponse>> createFlight(@RequestBody FlightRequest request) {
        return flightService.createFlight(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<FlightResponse>> updateFlight(@PathVariable("id") Long id,
            @RequestBody FlightRequest request) {
        return flightService.updateFlight(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteFlightById(@PathVariable("id") Long id) {
        return flightService.deleteFlightById(id);
    }
}
