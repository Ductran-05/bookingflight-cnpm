package com.cnpm.bookingflight.controller;

import com.cnpm.bookingflight.domain.Flight;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.FlightRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.FlightCountResponse;
import com.cnpm.bookingflight.dto.response.FlightResponse;
import com.cnpm.bookingflight.service.FlightService;
import com.turkraft.springfilter.boot.Filter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightController {

    final FlightService flightService;

    @GetMapping
    public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllFlights(@Filter Specification<Flight> spec,
            Pageable pageable,
            @RequestParam(value = "minPrice", required = false) List<Long> minPrice,
            @RequestParam(value = "maxPrice", required = false) List<Long> maxPrice,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "arrivalDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrivalDate,
            @RequestParam(value = "departureDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @RequestParam(value = "straight", required = false) Boolean straight,
            @RequestParam(value = "seat", required = false) List<Long> seats,
            @RequestParam(value = "airline", required = false) List<Long> airlines) {
        return flightService.getAllFlights(spec, pageable, minPrice, maxPrice, from, to, arrivalDate, departureDate,
                straight, seats, airlines);
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<FlightResponse>> getFlightById(@PathVariable("id") Long id) {
        return flightService.getFlightById(id);
    }

    @PostMapping
    public ResponseEntity<APIResponse<FlightResponse>> createFlight(@RequestBody FlightRequest request) {
        return flightService.createFlight(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<FlightResponse>> updateFlight(@PathVariable("id") Long id,
            @RequestBody FlightRequest request) {
        return flightService.updateFlight(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteFlight(@PathVariable("id") Long id) {
        return flightService.deleteFlightById(id);
    }

    @GetMapping("/flightcount")
    public ResponseEntity<APIResponse<FlightCountResponse>> getFlightCount(@RequestParam("period") String period) {
        return flightService.getFlightCount(period);
    }
}