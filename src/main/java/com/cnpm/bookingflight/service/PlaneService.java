package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Plane;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.PlaneRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.PlaneResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.PlaneMapper;
import com.cnpm.bookingflight.mapper.ResultPaginationMapper;
import com.cnpm.bookingflight.repository.PlaneRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaneService {
        final PlaneRepository planeRepository;
        final PlaneMapper planeMapper;
        final ResultPaginationMapper resultPaginationMapper;

        public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllPlanes(Specification<Plane> spec,
                                                                             Pageable pageable) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("isDeleted"), false));
                ResultPaginationDTO result = resultPaginationMapper
                        .toResultPagination(planeRepository.findAll(spec, pageable).map(planeMapper::toPlaneResponse));
                APIResponse<ResultPaginationDTO> response = APIResponse.<ResultPaginationDTO>builder()
                        .status(200)
                        .message("Get all planes successfully")
                        .data(result)
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<PlaneResponse>> getPlaneById(Long id) {
                Plane plane = planeRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                APIResponse<PlaneResponse> response = APIResponse.<PlaneResponse>builder()
                        .data(planeMapper.toPlaneResponse(plane))
                        .status(200)
                        .message("Get plane by id successfully")
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<PlaneResponse>> createPlane(PlaneRequest request) {
                Plane existingPlane = planeRepository.findByPlaneCode(request.getPlaneCode());
                if (existingPlane != null) {
                        throw new AppException(ErrorCode.EXISTED);
                }
                Plane newPlane = planeMapper.toPlane(request);
                newPlane.setIsDeleted(false);
                APIResponse<PlaneResponse> response = APIResponse.<PlaneResponse>builder()
                        .data(planeMapper.toPlaneResponse(planeRepository.save(newPlane)))
                        .status(201)
                        .message("Create plane successfully")
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<PlaneResponse>> updatePlane(Long id, PlaneRequest request) {
                Plane existingPlane = planeRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                Plane updatedPlane = planeMapper.toPlane(request);
                updatedPlane.setId(id);
                updatedPlane.setIsDeleted(existingPlane.getIsDeleted());
                APIResponse<PlaneResponse> response = APIResponse.<PlaneResponse>builder()
                        .data(planeMapper.toPlaneResponse(planeRepository.save(updatedPlane)))
                        .status(200)
                        .message("Update plane successfully")
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Void>> deletePlane(Long id) {
                Plane existingPlane = planeRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                existingPlane.setIsDeleted(true);
                planeRepository.save(existingPlane);
                APIResponse<Void> response = APIResponse.<Void>builder()
                        .status(200)
                        .message("Delete plane successfully")
                        .build();
                return ResponseEntity.ok(response);
        }
}