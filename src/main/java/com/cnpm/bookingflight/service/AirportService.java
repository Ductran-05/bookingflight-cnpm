package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Airport;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.AirportRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.AirportResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.AirportMapper;
import com.cnpm.bookingflight.mapper.ResultPaginationMapper;
import com.cnpm.bookingflight.repository.AirportRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
        Page<AirportResponse> page = airportRepository.findAll(spec, pageable).map(airportMapper::toAirportResponse);
        ResultPaginationDTO resultPaginationDTO = resultPaginationMapper.toResultPagination(page);
        APIResponse<ResultPaginationDTO> response = APIResponse.<ResultPaginationDTO>builder()
                .status(200)
                .message("Get all airports successfully")
                .data(resultPaginationDTO)
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<AirportResponse>> getAirportById(Long id) {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        APIResponse<AirportResponse> response = APIResponse.<AirportResponse>builder()
                .status(200)
                .message("Get airport by id successfully")
                .data(airportMapper.toAirportResponse(airport))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<AirportResponse>> createAirport(AirportRequest request) {
        Airport existingAirport = airportRepository.findByAirportCode(request.getAirportCode());
        if (existingAirport != null) {
            throw new AppException(ErrorCode.EXISTED);
        }
        Airport newAirport = airportMapper.toAirport(request);
        newAirport.setIsDeleted(false);
        APIResponse<AirportResponse> response = APIResponse.<AirportResponse>builder()
                .status(201)
                .message("Create airport successfully")
                .data(airportMapper.toAirportResponse(airportRepository.save(newAirport)))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<AirportResponse>> updateAirport(Long id, AirportRequest request) {
        Airport existingAirport = airportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        Airport updatedAirport = airportMapper.updateAirport(existingAirport, request);
        updatedAirport.setIsDeleted(existingAirport.getIsDeleted());
        APIResponse<AirportResponse> response = APIResponse.<AirportResponse>builder()
                .status(200)
                .message("Update airport successfully")
                .data(airportMapper.toAirportResponse(airportRepository.save(updatedAirport)))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Void>> deleteAirport(Long id) {
        Airport existingAirport = airportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        existingAirport.setIsDeleted(true);
        airportRepository.save(existingAirport);
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(200)
                .message("Delete airport successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}