package com.cnpm.bookingflight.controller;

import com.cnpm.bookingflight.domain.Airline;
import com.cnpm.bookingflight.dto.request.AirlineRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.service.AirlineService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequestMapping("/airlines")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AirlineController {

    final AirlineService airlineService;

    @GetMapping()
    public ResponseEntity<APIResponse<List<Airline>>> getAllAirlines() {
        return airlineService.getAllAirlines();
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<APIResponse<Airline>> createAirline(@RequestPart("airline") AirlineRequest request,
                                                              @RequestPart(value = "logo", required = false) MultipartFile logo) throws IOException {
        return airlineService.createAirline(request, logo);
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<APIResponse<Airline>> updateAirline(@PathVariable("id") Long id,
                                                              @RequestPart("airline") AirlineRequest request,
                                                              @RequestPart(value = "logo", required = false) MultipartFile logo) throws IOException {
        return airlineService.updateAirline(request, id, logo);
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
