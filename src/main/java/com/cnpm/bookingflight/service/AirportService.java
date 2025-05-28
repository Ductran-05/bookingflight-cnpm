package com.cnpm.bookingflight.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cnpm.bookingflight.domain.Airport;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.AirportRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.AirportMapper;
import com.cnpm.bookingflight.mapper.ResultPaginationMapper;
import com.cnpm.bookingflight.repository.AirportRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AirportService {
    final AirportRepository airportRepository;
    final AirportMapper airportMapper;
    final ResultPaginationMapper resultPaginationMapper;

    public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllAirports(Specification<Airport> spec,
            Pageable pageable) {
        spec = spec.and((root, query, cb) -> cb.equal(root.get("isDeleted"), false));
        Page<Airport> page = airportRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = resultPaginationMapper.toResultPagination(page);
        APIResponse<ResultPaginationDTO> response = APIResponse.<ResultPaginationDTO>builder()
                .status(200)
                .message("Get all airports successfully")
                .data(resultPaginationDTO)
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Airport>> getAirportById(Long id) {
        APIResponse<Airport> response = APIResponse.<Airport>builder()
                .status(200)
                .message("Get airport by id successfully")
                .data(airportRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Airport>> createAirport(AirportRequest request) {
        Airport existingAirport = airportRepository.findByAirportCode(request.getAirportCode());
        if (existingAirport != null) {
            throw new AppException(ErrorCode.EXISTED);
        }
        Airport newAirport = airportMapper.toAirport(request);
        newAirport.setIsDeleted(false); // Đảm bảo isDeleted là false khi tạo mới
        APIResponse<Airport> response = APIResponse.<Airport>builder()
                .status(201)
                .message("Create airport successfully")
                .data(airportRepository.save(newAirport))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Airport>> updateAirport(Long id, AirportRequest request) {
        Airport existingAirport = airportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        Airport updatedAirport = airportMapper.toAirport(request);
        updatedAirport.setId(id);
        updatedAirport.setIsDeleted(existingAirport.getIsDeleted()); // Giữ nguyên trạng thái isDeleted
        APIResponse<Airport> response = APIResponse.<Airport>builder()
                .status(200)
                .message("Update airport successfully")
                .data(airportRepository.save(updatedAirport))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Void>> deleteAirport(Long id) {
        Airport existingAirport = airportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        existingAirport.setIsDeleted(true); // Chuyển sang trạng thái xóa mềm
        airportRepository.save(existingAirport);
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(200)
                .message("Delete airport successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}