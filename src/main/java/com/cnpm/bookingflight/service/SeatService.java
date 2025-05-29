package com.cnpm.bookingflight.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cnpm.bookingflight.domain.Seat;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.SeatRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.ResultPaginationMapper;
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
        final ResultPaginationMapper resultPaginationMapper;

        public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllSeats(Specification<Seat> spec,
                        Pageable pageable) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("isDeleted"), false));

                ResultPaginationDTO result = resultPaginationMapper
                                .toResultPagination(seatRepository.findAll(spec, pageable));
                APIResponse<ResultPaginationDTO> response = APIResponse.<ResultPaginationDTO>builder()
                                .status(200)
                                .message("Get all seats successfully")
                                .data(result)
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
                Seat newSeat = seatMapper.toSeat(request);
                newSeat.setIsDeleted(false); // Đảm bảo isDeleted là false khi tạo mới
                APIResponse<Seat> response = APIResponse.<Seat>builder()
                                .status(201)
                                .message("Create seat successfully")
                                .data(seatRepository.save(newSeat))
                                .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Seat>> updateSeat(Long id, SeatRequest request) {
                Seat existingSeat = seatRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                Seat updatedSeat = seatMapper.toSeat(request);
                updatedSeat.setId(id);
                updatedSeat.setIsDeleted(existingSeat.getIsDeleted()); // Giữ nguyên trạng thái isDeleted
                APIResponse<Seat> response = APIResponse.<Seat>builder()
                                .status(200)
                                .message("Update seat successfully")
                                .data(seatRepository.save(updatedSeat))
                                .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Void>> deleteSeat(Long id) {
                Seat existingSeat = seatRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                existingSeat.setIsDeleted(true); // Chuyển sang trạng thái xóa mềm
                seatRepository.save(existingSeat);
                APIResponse<Void> response = APIResponse.<Void>builder()
                                .status(200)
                                .message("Delete seat successfully")
                                .build();
                return ResponseEntity.ok(response);
        }
}