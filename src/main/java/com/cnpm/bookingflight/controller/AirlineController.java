package com.cnpm.bookingflight.controller;

import com.cnpm.bookingflight.domain.Airline;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.AirlineRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.AirlinePopularityResponse;
import com.cnpm.bookingflight.dto.response.AirlineResponse;
import com.cnpm.bookingflight.service.AirlineService;
import com.turkraft.springfilter.boot.Filter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequestMapping("/airlines")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AirlineController {

    final AirlineService airlineService;

    @GetMapping
    public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllAirlines(@Filter Specification<Airline> spec,
                                                                           Pageable pageable) {
        return airlineService.getAllAirlines(spec, pageable);
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<APIResponse<AirlineResponse>> createAirline(@RequestPart("airline") AirlineRequest request,
                                                                      @RequestPart(value = "logo", required = false) MultipartFile logo) throws IOException {
        return airlineService.createAirline(request, logo);
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<APIResponse<AirlineResponse>> updateAirline(@PathVariable("id") Long id,
                                                                      @RequestPart("airline") AirlineRequest request,
                                                                      @RequestPart(value = "logo", required = false) MultipartFile logo) throws IOException {
        return airlineService.updateAirline(request, id, logo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<AirlineResponse>> getAirlineById(@PathVariable("id") Long id) {
        return airlineService.getAirlineById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteAirline(@PathVariable("id") Long id) {
        return airlineService.deleteAirline(id);
    }

    @GetMapping("/flights/airline-popular")
    public ResponseEntity<APIResponse<AirlinePopularityResponse>> getAirlinePopularity() {
        return airlineService.getAirlinePopularity();
    }
}