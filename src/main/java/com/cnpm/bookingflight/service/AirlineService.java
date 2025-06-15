package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Airline;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.AirlineRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.AirlinePopularityResponse;
import com.cnpm.bookingflight.dto.response.AirlineResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.AirlineMapper;
import com.cnpm.bookingflight.mapper.ResultPaginationMapper;
import com.cnpm.bookingflight.repository.AirlineRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AirlineService {
        final AirlineRepository airlineRepository;
        final AirlineMapper airlineMapper;
        final ImageUploadService imageUploadService;
        final ResultPaginationMapper resultPaginationMapper;

        public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllAirlines(Specification<Airline> spec,
                                                                               Pageable pageable) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("isDeleted"), false));
                Page<AirlineResponse> page = airlineRepository.findAll(spec, pageable).map(airlineMapper::toAirlineResponse);
                ResultPaginationDTO resultPaginationDTO = resultPaginationMapper.toResultPagination(page);
                APIResponse<ResultPaginationDTO> response = APIResponse.<ResultPaginationDTO>builder()
                        .status(200)
                        .message("Get all airlines successfully")
                        .data(resultPaginationDTO)
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<AirlineResponse>> createAirline(AirlineRequest request, MultipartFile logo)
                throws IOException {
                Airline existingAirline = airlineRepository.findByAirlineCode(request.getAirlineCode());
                if (existingAirline != null) {
                        throw new AppException(ErrorCode.EXISTED);
                }
                Airline newAirline = airlineMapper.toAirline(request);
                newAirline.setIsDeleted(false);
                if (logo != null && !logo.isEmpty()) {
                        String logoUrl = imageUploadService.uploadImage(logo, "airline_logos");
                        newAirline.setLogo(logoUrl);
                }

                APIResponse<AirlineResponse> response = APIResponse.<AirlineResponse>builder()
                        .data(airlineMapper.toAirlineResponse(airlineRepository.save(newAirline)))
                        .status(201)
                        .message("Create airline successfully")
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<AirlineResponse>> getAirlineById(Long id) {
                Airline airline = airlineRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                APIResponse<AirlineResponse> response = APIResponse.<AirlineResponse>builder()
                        .data(airlineMapper.toAirlineResponse(airline))
                        .status(200)
                        .message("Get airline by id successfully")
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<AirlineResponse>> updateAirline(AirlineRequest request, Long id, MultipartFile logo)
                throws IOException {
                Airline existingAirline = airlineRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

                Airline updatedAirline = airlineMapper.toAirline(request);
                updatedAirline.setId(id);
                updatedAirline.setIsDeleted(existingAirline.getIsDeleted());
                if (logo != null && !logo.isEmpty()) {
                        String logoUrl = imageUploadService.uploadImage(logo, "airline_logos");
                        updatedAirline.setLogo(logoUrl);
                } else {
                        updatedAirline.setLogo(existingAirline.getLogo());
                }

                APIResponse<AirlineResponse> response = APIResponse.<AirlineResponse>builder()
                        .data(airlineMapper.toAirlineResponse(airlineRepository.save(updatedAirline)))
                        .status(200)
                        .message("Update airline successfully")
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Void>> deleteAirline(Long id) {
                Airline existingAirline = airlineRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                existingAirline.setIsDeleted(true);
                airlineRepository.save(existingAirline);
                APIResponse<Void> response = APIResponse.<Void>builder()
                        .status(200)
                        .message("Delete airline successfully")
                        .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<AirlinePopularityResponse>> getAirlinePopularity() {
                List<Object[]> ticketCounts = airlineRepository.countTicketsByAirline();
                long totalTickets = ticketCounts.stream()
                        .mapToLong(row -> ((Number) row[2]).longValue())
                        .sum();

                if (totalTickets == 0) {
                        return ResponseEntity.ok(APIResponse.<AirlinePopularityResponse>builder()
                                .status(200)
                                .message("No tickets sold")
                                .data(new AirlinePopularityResponse(
                                        new AirlinePopularityResponse.AirlineInfo("None", 0.0),
                                        new AirlinePopularityResponse.AirlineInfo("None", 0.0),
                                        new AirlinePopularityResponse.AirlineInfo("None", 0.0)))
                                .build());
                }

                List<AirlinePopularityResponse.AirlineInfo> airlineInfos = new ArrayList<>();
                for (Object[] row : ticketCounts) {
                        String airlineName = (String) row[1];
                        long tickets = ((Number) row[2]).longValue();
                        double percentage = (tickets * 100.0) / totalTickets;
                        percentage = Math.round(percentage * 100) / 100.0; // Làm tròn đến 2 chữ số thập phân
                        airlineInfos.add(new AirlinePopularityResponse.AirlineInfo(airlineName, percentage));
                }

                airlineInfos.sort(Comparator.comparingDouble(AirlinePopularityResponse.AirlineInfo::getPercentage).reversed());

                AirlinePopularityResponse responseData = new AirlinePopularityResponse();
                responseData.setAirline1(airlineInfos.size() > 0 ? airlineInfos.get(0) : new AirlinePopularityResponse.AirlineInfo("None", 0.0));
                responseData.setAirline2(airlineInfos.size() > 1 ? airlineInfos.get(1) : new AirlinePopularityResponse.AirlineInfo("None", 0.0));

                double otherPercentage = airlineInfos.size() > 2
                        ? airlineInfos.subList(2, airlineInfos.size()).stream().mapToDouble(AirlinePopularityResponse.AirlineInfo::getPercentage).sum()
                        : 0.0;
                otherPercentage = Math.round(otherPercentage * 100) / 100.0; // Làm tròn đến 2 chữ số thập phân
                responseData.setOtherAirlines(new AirlinePopularityResponse.AirlineInfo("Other Airlines", otherPercentage));

                APIResponse<AirlinePopularityResponse> response = APIResponse.<AirlinePopularityResponse>builder()
                        .status(200)
                        .message("Get airline popularity successfully")
                        .data(responseData)
                        .build();
                return ResponseEntity.ok(response);
        }
}