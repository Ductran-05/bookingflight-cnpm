package com.cnpm.bookingflight.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cnpm.bookingflight.dto.request.FlightRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.FlightResponse;
import com.cnpm.bookingflight.service.FlightService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@RequestMapping("/flights")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class FlightController {

    final FlightService flightService;

    @GetMapping()
    public ResponseEntity<APIResponse<List<FlightResponse>>> getAllFlights() {
        return flightService.getAllFlights();
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
