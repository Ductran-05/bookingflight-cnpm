package com.cnpm.bookingflight.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cnpm.bookingflight.domain.Airport;
import com.cnpm.bookingflight.dto.request.AirportRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.AirportMapper;
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

    public ResponseEntity<APIResponse<List<Airport>>> getAllAirports(Specification<Airport> spec) {

        List<Airport> page = airportRepository.findAll(spec);
        APIResponse<List<Airport>> response = APIResponse.<List<Airport>>builder()
                .status(200)
                .message("Get all airports successfully")
                .data(page)
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
        APIResponse<Airport> response = APIResponse.<Airport>builder()
                .status(201)
                .message("Create airport successfully")
                .data(airportRepository.save(airportMapper.toAirport(request)))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Airport>> updateAirport(Long id, AirportRequest request) {
        airportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        Airport updatedAirport = airportMapper.toAirport(request);
        updatedAirport.setId(id);
        APIResponse<Airport> response = APIResponse.<Airport>builder()
                .status(200)
                .message("Update airport successfully")
                .data(airportRepository.save(updatedAirport))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Void>> deleteAirport(Long id) {
        airportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        airportRepository.deleteById(id);
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(204)
                .message("Delete airport successfully")
                .build();
        return ResponseEntity.ok(response);
    }

}
