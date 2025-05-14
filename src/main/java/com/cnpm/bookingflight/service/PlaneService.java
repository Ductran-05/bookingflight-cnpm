package com.cnpm.bookingflight.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cnpm.bookingflight.domain.Plane;
import com.cnpm.bookingflight.dto.request.PlaneRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.PlaneMapper;
import com.cnpm.bookingflight.repository.PlaneRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaneService {
    final PlaneRepository planeRepository;
    final PlaneMapper planeMapper;

    public ResponseEntity<APIResponse<List<Plane>>> getAllPlanes(Specification<Plane> spec) {
        List<Plane> page = planeRepository.findAll(spec);
        APIResponse<List<Plane>> response = APIResponse.<List<Plane>>builder()
                .data(page)
                .status(200)
                .message("get all planes successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Plane>> getPlaneById(Long id) {
        APIResponse<Plane> response = APIResponse.<Plane>builder()
                .data(planeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)))
                .status(200)
                .message("get plane by id successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Plane>> createPlane(PlaneRequest request) {
        Plane existingPlane = planeRepository.findByPlaneCode(request.getPlaneCode());
        if (existingPlane != null) {
            throw new AppException(ErrorCode.EXISTED);
        }
        APIResponse<Plane> response = APIResponse.<Plane>builder()
                .data(planeRepository.save(planeMapper.toPlane(request)))
                .status(201)
                .message("create plane successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Plane>> updatePlane(Long id, PlaneRequest request) {
        planeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        Plane existingPlane = planeMapper.toPlane(request);
        existingPlane.setId(id);
        planeRepository.save(existingPlane);
        APIResponse<Plane> response = APIResponse.<Plane>builder()
                .data(existingPlane)
                .status(200)
                .message("update plane successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Void>> deletePlane(Long id) {
        planeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        planeRepository.deleteById(id);
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(204)
                .message("delete plane successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
