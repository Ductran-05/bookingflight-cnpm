package com.cnpm.bookingflight.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cnpm.bookingflight.domain.Airline;
import com.cnpm.bookingflight.dto.request.AirlineRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.AirlineMapper;
import com.cnpm.bookingflight.repository.AirlineRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AirlineService {
        final AirlineRepository airlineRepository;
        final AirlineMapper airlineMapper;

        public ResponseEntity<APIResponse<List<Airline>>> getAllAirlines() {
                APIResponse<List<Airline>> response = APIResponse.<List<Airline>>builder()
                                .data(airlineRepository.findAll())
                                .status(200)
                                .message("get all airlines successfully")
                                .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Airline>> createAirline(AirlineRequest request) {
                Airline existingAirline = airlineRepository.findByAirlineCode(request.getAirlineCode());
                if (existingAirline != null) {
                        throw new AppException(ErrorCode.EXISTED);
                }
                APIResponse<Airline> response = APIResponse.<Airline>builder()
                                .data(airlineRepository.save(airlineMapper.toAirline(request)))
                                .status(201)
                                .message("create airline successfully")
                                .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Airline>> getAirlineById(Long id) {
                APIResponse<Airline> response = APIResponse.<Airline>builder()
                                .data(airlineRepository.findById(id)
                                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)))
                                .status(200)
                                .message("get airline by id successfully")
                                .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Airline>> updateAirline(AirlineRequest request, Long id) {
                airlineRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

                APIResponse<Airline> response = APIResponse.<Airline>builder()
                                .data(airlineRepository.save(airlineMapper.toAirline(request)))
                                .status(200)
                                .message("update airline successfully")
                                .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Void>> deleteAirline(Long id) {
                airlineRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                airlineRepository.deleteById(id);
                APIResponse<Void> response = APIResponse.<Void>builder()
                                .status(204)
                                .message("delete airline successfully")
                                .build();
                return ResponseEntity.ok(response);
        }
}
