package com.cnpm.bookingflight.controller;

import com.cnpm.bookingflight.domain.Airport;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.AirportRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.AirportResponse;
import com.cnpm.bookingflight.service.AirportService;
import com.turkraft.springfilter.boot.Filter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/airports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AirportController {
    final AirportService airportService;

    @GetMapping
    public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllAirports(
            @Filter Specification<Airport> spec, Pageable pageable) {
        return airportService.getAllAirports(spec, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<AirportResponse>> getAirport(@PathVariable("id") Long id) {
        return airportService.getAirportById(id);
    }

    @PostMapping
    public ResponseEntity<APIResponse<AirportResponse>> createAirport(@RequestBody AirportRequest request) {
        return airportService.createAirport(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteAirport(@PathVariable("id") Long id) {
        return airportService.deleteAirport(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<AirportResponse>> updateAirport(@PathVariable("id") Long id,
                                                                      @RequestBody AirportRequest request) {
        return airportService.updateAirport(id, request);
    }
}