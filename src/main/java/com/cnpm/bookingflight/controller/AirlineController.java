package com.cnpm.bookingflight.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cnpm.bookingflight.domain.Airline;
import com.cnpm.bookingflight.dto.request.AirlineRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.service.AirlineService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequestMapping("/airlines")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AirlineController {

    final AirlineService airlineService;

    @GetMapping()
    public ResponseEntity<APIResponse<List<Airline>>> getALlAirlines() {
        return airlineService.getAllAirlines();
    }

    @PostMapping()
    public ResponseEntity<APIResponse<Airline>> createAirline(@RequestBody AirlineRequest request) {
        return airlineService.createAirline(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<Airline>> createAirline(@PathVariable("id") Long id,
            @RequestBody AirlineRequest request) {
        return airlineService.updateAirline(request, id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<Airline>> getAirlineById(@PathVariable("id") Long id) {
        return airlineService.getAirlineById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteAirline(@PathVariable("id") Long id) {
        return airlineService.deleteAirline(id);
    }
}
