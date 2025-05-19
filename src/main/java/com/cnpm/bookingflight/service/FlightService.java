package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Flight;
import com.cnpm.bookingflight.dto.request.FlightRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.FlightResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.FlightMapper;
import com.cnpm.bookingflight.repository.FlightRepository;
import com.cnpm.bookingflight.repository.TicketRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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

    public ResponseEntity<APIResponse<List<FlightResponse>>> getAllFlights() {
        List<FlightResponse> flights = flightRepository.findAll().stream()
                .map(flight -> {
                    boolean hasTickets = ticketRepository.existsByFlightId(flight.getId());
                    return flightMapper.toFlightResponse(flight)
                            .toBuilder()
                            .canUpdate(!hasTickets)
                            .canDelete(!hasTickets)
                            .build();
                })
                .collect(Collectors.toList());

        APIResponse<List<FlightResponse>> response = APIResponse.<List<FlightResponse>>builder()
                .data(flights)
                .status(200)
                .message("Get all flights successfully")
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
        Flight existingFlight = flightRepository.findByFlightCode(request.getFlightCode());
        if (existingFlight != null) {
            throw new AppException(ErrorCode.EXISTED);
        }
        Flight flight = flightMapper.toFlight(request);
        Flight savedFlight = flightRepository.save(flight);
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