package com.cnpm.bookingflight.service;

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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AirlineService {
        final AirlineRepository airlineRepository;
        final AirlineMapper airlineMapper;
        final ImageUploadService imageUploadService;

        public ResponseEntity<APIResponse<List<Airline>>> getAllAirlines() {
                APIResponse<List<Airline>> response = APIResponse.<List<Airline>>builder()
                        .data(airlineRepository.findAllByIsDeletedFalse())
                        .status(200)
                        .message("Get all airlines successfully")
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Airline>> createAirline(AirlineRequest request, MultipartFile logo) throws IOException {
                Airline existingAirline = airlineRepository.findByAirlineCode(request.getAirlineCode());
                if (existingAirline != null) {
                        throw new AppException(ErrorCode.EXISTED);
                }
                Airline newAirline = airlineMapper.toAirline(request);
                newAirline.setIsDeleted(false); // Đảm bảo isDeleted là false khi tạo mới
                if (logo != null && !logo.isEmpty()) {
                        String logoUrl = imageUploadService.uploadImage(logo, "airline_logos");
                        newAirline.setLogo(logoUrl);
                }

                APIResponse<Airline> response = APIResponse.<Airline>builder()
                        .data(airlineRepository.save(newAirline))
                        .status(201)
                        .message("Create airline successfully")
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Airline>> getAirlineById(Long id) {
                APIResponse<Airline> response = APIResponse.<Airline>builder()
                        .data(airlineRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)))
                        .status(200)
                        .message("Get airline by id successfully")
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Airline>> updateAirline(AirlineRequest request, Long id, MultipartFile logo) throws IOException {
                Airline existingAirline = airlineRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

                Airline updatedAirline = airlineMapper.toAirline(request);
                updatedAirline.setId(id);
                updatedAirline.setIsDeleted(existingAirline.getIsDeleted()); // Giữ nguyên trạng thái isDeleted
                if (logo != null && !logo.isEmpty()) {
                        String logoUrl = imageUploadService.uploadImage(logo, "airline_logos");
                        updatedAirline.setLogo(logoUrl);
                } else {
                        updatedAirline.setLogo(existingAirline.getLogo());
                }

                APIResponse<Airline> response = APIResponse.<Airline>builder()
                        .data(airlineRepository.save(updatedAirline))
                        .status(200)
                        .message("Update airline successfully")
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Void>> deleteAirline(Long id) {
                Airline existingAirline = airlineRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                existingAirline.setIsDeleted(true); // Chuyển sang trạng thái xóa mềm
                airlineRepository.save(existingAirline);
                APIResponse<Void> response = APIResponse.<Void>builder()
                        .status(200)
                        .message("Delete airline successfully")
                        .build();
                return ResponseEntity.ok(response);
        }
}