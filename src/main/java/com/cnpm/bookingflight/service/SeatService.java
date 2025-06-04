package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Seat;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.SeatRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.SeatResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.ResultPaginationMapper;
import com.cnpm.bookingflight.mapper.SeatMapper;
import com.cnpm.bookingflight.repository.SeatRepository;
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
public class SeatService {
        final SeatRepository seatRepository;
        final SeatMapper seatMapper;
        final ResultPaginationMapper resultPaginationMapper;

        public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllSeats(Specification<Seat> spec,
                                                                            Pageable pageable) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("isDeleted"), false));
                ResultPaginationDTO result = resultPaginationMapper
                        .toResultPagination(seatRepository.findAll(spec, pageable).map(seatMapper::toSeatResponse));
                APIResponse<ResultPaginationDTO> response = APIResponse.<ResultPaginationDTO>builder()
                        .status(200)
                        .message("Get all seats successfully")
                        .data(result)
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<SeatResponse>> getSeatById(Long id) {
                Seat seat = seatRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                APIResponse<SeatResponse> response = APIResponse.<SeatResponse>builder()
                        .status(200)
                        .message("Get seat by id successfully")
                        .data(seatMapper.toSeatResponse(seat))
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<SeatResponse>> createSeat(SeatRequest request) {
                Seat existingSeat = seatRepository.findBySeatCode(request.getSeatCode());
                if (existingSeat != null) {
                        throw new AppException(ErrorCode.EXISTED);
                }
                Seat newSeat = seatMapper.toSeat(request);
                newSeat.setIsDeleted(false);
                APIResponse<SeatResponse> response = APIResponse.<SeatResponse>builder()
                        .status(201)
                        .message("Create seat successfully")
                        .data(seatMapper.toSeatResponse(seatRepository.save(newSeat)))
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<SeatResponse>> updateSeat(Long id, SeatRequest request) {
                Seat existingSeat = seatRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                Seat updatedSeat = seatMapper.toSeat(request);
                updatedSeat.setId(id);
                updatedSeat.setIsDeleted(existingSeat.getIsDeleted());
                APIResponse<SeatResponse> response = APIResponse.<SeatResponse>builder()
                        .status(200)
                        .message("Update seat successfully")
                        .data(seatMapper.toSeatResponse(seatRepository.save(updatedSeat)))
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Void>> deleteSeat(Long id) {
                Seat existingSeat = seatRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                existingSeat.setIsDeleted(true);
                seatRepository.save(existingSeat);
                APIResponse<Void> response = APIResponse.<Void>builder()
                        .status(200)
                        .message("Delete seat successfully")
                        .build();
                return ResponseEntity.ok(response);
        }
}