package com.cnpm.bookingflight.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cnpm.bookingflight.domain.Plane;
import com.cnpm.bookingflight.dto.request.PlaneRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.service.PlaneService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/planes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaneController {
    final PlaneService planeService;

    @GetMapping()
    ResponseEntity<APIResponse<List<Plane>>> getAllPlanes() {
        return planeService.getAllPlanes();
    }

    @GetMapping("/{id}")
    ResponseEntity<APIResponse<Plane>> getPlaneById(@PathVariable("id") Long id) {
        return planeService.getPlaneById(id);
    }

    @PostMapping()
    ResponseEntity<APIResponse<Plane>> createPlane(@RequestBody PlaneRequest request) {
        return planeService.createPlane(request);
    }

    @PutMapping("/{id}")
    ResponseEntity<APIResponse<Plane>> updatePlane(@PathVariable("id") Long id, @RequestBody PlaneRequest request) {
        return planeService.updatePlane(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<APIResponse<Void>> deletePlane(@PathVariable("id") Long id) {
        return planeService.deletePlane(id);
    }
}
