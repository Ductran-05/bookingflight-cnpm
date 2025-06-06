package com.cnpm.bookingflight.controller;

import com.cnpm.bookingflight.domain.Plane;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.PlaneRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.PlaneResponse;
import com.cnpm.bookingflight.service.PlaneService;
import com.turkraft.springfilter.boot.Filter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/planes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaneController {
    final PlaneService planeService;

    @GetMapping
    ResponseEntity<APIResponse<ResultPaginationDTO>> getAllPlanes(@Filter Specification<Plane> spec,
                                                                  Pageable pageable) {
        return planeService.getAllPlanes(spec, pageable);
    }

    @GetMapping("/{id}")
    ResponseEntity<APIResponse<PlaneResponse>> getPlaneById(@PathVariable("id") Long id) {
        return planeService.getPlaneById(id);
    }

    @PostMapping
    ResponseEntity<APIResponse<PlaneResponse>> createPlane(@RequestBody PlaneRequest request) {
        return planeService.createPlane(request);
    }

    @PutMapping("/{id}")
    ResponseEntity<APIResponse<PlaneResponse>> updatePlane(@PathVariable("id") Long id, @RequestBody PlaneRequest request) {
        return planeService.updatePlane(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<APIResponse<Void>> deletePlane(@PathVariable("id") Long id) {
        return planeService.deletePlane(id);
    }
}