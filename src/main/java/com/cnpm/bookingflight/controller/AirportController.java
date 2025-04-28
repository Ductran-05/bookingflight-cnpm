package com.cnpm.bookingflight.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cnpm.bookingflight.domain.Airport;
import com.cnpm.bookingflight.dto.request.AirportRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.service.AirportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/airports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AirportController {
    final AirportService airportService;

    @GetMapping
    public ResponseEntity<APIResponse<List<Airport>>> getAllAirports() {
        return airportService.getAllAirports();
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<Airport>> getAirport(@PathVariable("id") Long id) {
        return airportService.getAirportById(id);
    }

    @PostMapping
    public ResponseEntity<APIResponse<Airport>> createAirport(@RequestBody AirportRequest request) {
        return airportService.createAirport(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteAirport(@PathVariable("id") Long id) {
        return airportService.deleteAirport(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<Airport>> updateAirport(@PathVariable("id") Long id,
            @RequestBody AirportRequest request) {
        return airportService.updateAirport(id, request);
    }

}
