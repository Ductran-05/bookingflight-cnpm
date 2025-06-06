package com.cnpm.bookingflight.controller;

import com.cnpm.bookingflight.domain.City;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.CityRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.CityResponse;
import com.cnpm.bookingflight.service.CityService;
import com.turkraft.springfilter.boot.Filter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cities")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CityController {
    final CityService cityService;

    @GetMapping()
    public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllCities(@Filter Specification<City> spec,
                                                                         Pageable pageable) {
        return cityService.getAllCities(spec, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<CityResponse>> getCityById(@PathVariable("id") Long id) {
        return cityService.getCityById(id);
    }

    @PostMapping()
    public ResponseEntity<APIResponse<CityResponse>> createCity(@RequestBody CityRequest request) {
        return cityService.createCity(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<CityResponse>> updateCity(@PathVariable("id") Long id,
                                                                @RequestBody CityRequest request) {
        return cityService.updateCity(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteCity(@PathVariable("id") Long id) {
        return cityService.deleteCity(id);
    }
}