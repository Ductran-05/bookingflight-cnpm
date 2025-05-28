package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Flight;
import com.cnpm.bookingflight.domain.Flight_Airport;
import com.cnpm.bookingflight.domain.Flight_Seat;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.FlightRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.FlightResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.FlightMapper;
import com.cnpm.bookingflight.mapper.Flight_AirportMapper;
import com.cnpm.bookingflight.mapper.Flight_SeatMapper;
import com.cnpm.bookingflight.mapper.ResultPaginationMapper;
import com.cnpm.bookingflight.repository.FlightRepository;
import com.cnpm.bookingflight.repository.Flight_AirportRepository;
import com.cnpm.bookingflight.repository.Flight_SeatRepository;
import com.cnpm.bookingflight.repository.TicketRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightService {

        final FlightRepository flightRepository;
        final FlightMapper flightMapper;
        final TicketRepository ticketRepository;
        final Flight_AirportMapper flightAirportMapper;
        final Flight_SeatMapper flightSeatMapper;
        final Flight_AirportRepository flightAirportRepository;
        final Flight_SeatRepository flightSeatRepository;
        final ResultPaginationMapper resultPaginationMapper;

        public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllFlights(Specification<Flight> spec,
                        Pageable pageable) {
                ResultPaginationDTO result = resultPaginationMapper
                                .toResultPagination(flightRepository.findAll(spec, pageable)
                                                .map(flightMapper::toFlightResponse));
                APIResponse<ResultPaginationDTO> response = APIResponse.<ResultPaginationDTO>builder()
                                .status(200)
                                .message("Get all flights successfully")
                                .data(result)
                                .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<FlightResponse>> getFlightById(Long id) {
                Flight flight = flightRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                boolean hasTickets = ticketRepository.existsByFlightId(id);
                FlightResponse flightResponse = flightMapper.toFlightResponse(flight)
                                .toBuilder()
                                .canUpdate(!hasTickets)
                                .canDelete(!hasTickets)
                                .build();

                APIResponse<FlightResponse> response = APIResponse.<FlightResponse>builder()
                                .data(flightResponse)
                                .status(200)
                                .message("Get flight by id successfully")
                                .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<FlightResponse>> createFlight(FlightRequest request) {
                // Kiểm tra mã chuyến bay đã tồn tại
                Flight existingFlight = flightRepository.findByFlightCode(request.getFlightCode());
                if (existingFlight != null) {
                        throw new AppException(ErrorCode.EXISTED);
                }

                // Tạo và lưu chuyến bay
                Flight flight = flightMapper.toFlight(request);
                Flight savedFlight = flightRepository.save(flight);

                // Lưu các sân bay trung gian (Flight_Airport)
                if (request.getInterAirports() != null && !request.getInterAirports().isEmpty()) {
                        List<Flight_Airport> flightAirports = request.getInterAirports().stream()
                                        .map(flightAirportRequest -> flightAirportMapper
                                                        .toFlight_Airport(flightAirportRequest, savedFlight.getId()))
                                        .collect(Collectors.toList());
                        flightAirportRepository.saveAll(flightAirports);
                }

                // Lưu các ghế (Flight_Seat)
                if (request.getSeats() != null && !request.getSeats().isEmpty()) {
                        List<Flight_Seat> flightSeats = request.getSeats().stream()
                                        .map(flightSeatRequest -> flightSeatMapper.toFlight_Seat(flightSeatRequest,
                                                        savedFlight.getId()))
                                        .collect(Collectors.toList());
                        flightSeatRepository.saveAll(flightSeats);
                }

                // Tạo response
                FlightResponse flightResponse = flightMapper.toFlightResponse(savedFlight)
                                .toBuilder()
                                .canUpdate(true)
                                .canDelete(true)
                                .build();

                APIResponse<FlightResponse> response = APIResponse.<FlightResponse>builder()
                                .data(flightResponse)
                                .status(201)
                                .message("Create flight successfully")
                                .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<FlightResponse>> updateFlight(Long id, FlightRequest request) {
                Flight flight = flightRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                if (ticketRepository.existsByFlightId(id)) {
                        throw new AppException(ErrorCode.FLIGHT_HAS_TICKETS);
                }
                Flight updatedFlight = flightMapper.toFlight(request);
                updatedFlight.setId(id);
                Flight savedFlight = flightRepository.save(updatedFlight);
                FlightResponse flightResponse = flightMapper.toFlightResponse(savedFlight)
                                .toBuilder()
                                .canUpdate(true)
                                .canDelete(true)
                                .build();

                APIResponse<FlightResponse> response = APIResponse.<FlightResponse>builder()
                                .data(flightResponse)
                                .status(200)
                                .message("Update flight successfully")
                                .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<APIResponse<Void>> deleteFlightById(Long id) {
                Flight flight = flightRepository.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                if (ticketRepository.existsByFlightId(id)) {
                        throw new AppException(ErrorCode.FLIGHT_HAS_TICKETS);
                }
                flightRepository.deleteById(id);
                APIResponse<Void> response = APIResponse.<Void>builder()
                                .status(204)
                                .message("Delete flight successfully")
                                .build();
                return ResponseEntity.ok(response);
        }
}