package com.cnpm.bookingflight.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cnpm.bookingflight.domain.Seat;
import com.cnpm.bookingflight.dto.request.SeatRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.SeatMapper;
import com.cnpm.bookingflight.repository.SeatRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatService {
        final SeatRepository seatRepository;
        final SeatMapper seatMapper;

        public ResponseEntity<APIResponse<List<Seat>>> getAllSeats() {
                APIResponse<List<Seat>> response = APIResponse.<List<Seat>>builder()
                                .status(200)
                                .message("Get all seats successfully")
                                .data(seatRepository.findAll())
                                .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Seat>> getSeatById(Long id) {
                APIResponse<Seat> response = APIResponse.<Seat>builder()
                                .status(200)
                                .message("Get seat by id successfully")
                                .data(seatRepository.findById(id)
                                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)))
                                .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Seat>> createSeat(SeatRequest request) {
                Seat existingSeat = seatRepository.findBySeatCode(request.getSeatCode());
                if (existingSeat != null) {
                        throw new AppException(ErrorCode.EXISTED);
                }

                APIResponse<Seat> response = APIResponse.<Seat>builder()
                                .status(201)
                                .message("Create seat successfully")
                                .data(seatRepository.save(seatMapper.toSeat(request)))
                                .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Seat>> updateSeat(Long id, SeatRequest request) {
                seatRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

                Seat existingSeat = seatMapper.toSeat(request);
                existingSeat.setId(id);
                seatRepository.save(existingSeat);

                APIResponse<Seat> response = APIResponse.<Seat>builder()
                                .status(200)
                                .message("Update seat successfully")
                                .data(existingSeat)
                                .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Void>> deleteSeat(Long id) {
                seatRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

                seatRepository.deleteById(id);

                APIResponse<Void> response = APIResponse.<Void>builder()
                                .status(204)
                                .message("Delete seat successfully")
                                .build();
                return ResponseEntity.ok(response);
        }
}
