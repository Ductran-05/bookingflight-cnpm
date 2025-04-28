package com.cnpm.bookingflight.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cnpm.bookingflight.domain.Flight;
import com.cnpm.bookingflight.domain.Flight_Airport;
import com.cnpm.bookingflight.domain.Flight_Seat;
import com.cnpm.bookingflight.dto.request.FlightRequest;
import com.cnpm.bookingflight.dto.request.Flight_AirportRequest;
import com.cnpm.bookingflight.dto.request.Flight_SeatRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.FlightResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.FlightMapper;
import com.cnpm.bookingflight.mapper.Flight_AirportMapper;
import com.cnpm.bookingflight.mapper.Flight_SeatMapper;
import com.cnpm.bookingflight.repository.AirportRepository;
import com.cnpm.bookingflight.repository.FlightRepository;
import com.cnpm.bookingflight.repository.Flight_AirportRepository;
import com.cnpm.bookingflight.repository.Flight_SeatRepository;
import com.cnpm.bookingflight.repository.PlaneRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightService {
    final FlightRepository flightRepository;
    final FlightMapper flightMapper;

    final AirportRepository airportRepository;
    final PlaneRepository planeRepository;

    final Flight_AirportMapper flight_AirportMapper;
    final Flight_AirportRepository flight_AirportRepository;

    final Flight_SeatMapper flight_SeatMapper;
    final Flight_SeatRepository flight_SeatRepository;

    public ResponseEntity<APIResponse<List<FlightResponse>>> getAllFlights() {
        APIResponse<List<FlightResponse>> response = APIResponse.<List<FlightResponse>>builder()
                .data(flightMapper.toFlightResponseList(flightRepository.findAll()))
                .status(200)
                .message("get all flights successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<FlightResponse>> createFlight(FlightRequest request) {
        Flight existingFlight = flightRepository.findByFlightCode(request.getFlightCode());
        if (existingFlight != null) {
            throw new AppException(ErrorCode.EXISTED);
        }
        airportRepository.findById(request.getArrivalAirportId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        // validate san bay den
        airportRepository.findById(request.getArrivalAirportId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID));
        // validate san bay di
        airportRepository.findById(request.getDepartureAirportId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID));
        // validate may bay
        planeRepository.findById(request.getPlaneId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID));
        // luu chuyen bay

        Flight flight = flightRepository.save(flightMapper.toFlight(request));
        // luu san bay trung gian
        for (Flight_AirportRequest item : request.getInterAirports()) {
            Flight_Airport interAirport = flight_AirportMapper.toFlight_Airport(item, flight.getId());
            flight_AirportRepository.save(interAirport);
        }
        // luu so luong ghe
        for (Flight_SeatRequest item : request.getSeats()) {
            Flight_Seat seat = flight_SeatMapper.toFlight_Seat(item, flight.getId());
            flight_SeatRepository.save(seat);
        }

        APIResponse<FlightResponse> response = APIResponse.<FlightResponse>builder()
                .data(flightMapper.toFlightResponse(flight))
                .status(201)
                .message("create flight successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<FlightResponse>> updateFlight(Long id, FlightRequest request) {
        // Tìm chuyến bay theo id
        flightRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // Validate các ID của sân bay và máy bay
        airportRepository.findById(request.getDepartureAirportId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID));
        airportRepository.findById(request.getArrivalAirportId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID));
        planeRepository.findById(request.getPlaneId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID));

        // Lưu lại thông tin chuyến bay đã cập nhật

        Flight updatedFlight = flightMapper.toFlight(request);
        updatedFlight.setId(id);
        flightRepository.save(updatedFlight);

        List<Flight_Airport> existingInterAirports = flight_AirportRepository.findByIdFlightId(id);
        flight_AirportRepository.deleteAll(existingInterAirports);

        for (Flight_AirportRequest item : request.getInterAirports()) {
            Flight_Airport interAirport = flight_AirportMapper.toFlight_Airport(item, id);
            flight_AirportRepository.save(interAirport);
        }

        List<Flight_Seat> existingSeats = flight_SeatRepository.findByIdFlightId(id);
        flight_SeatRepository.deleteAll(existingSeats);

        for (Flight_SeatRequest item : request.getSeats()) {
            Flight_Seat seat = flight_SeatMapper.toFlight_Seat(item, id);
            flight_SeatRepository.save(seat);
        }

        // Xây dựng response trả về
        APIResponse<FlightResponse> response = APIResponse.<FlightResponse>builder()
                .data(flightMapper.toFlightResponse(updatedFlight))
                .status(200)
                .message("Update flight successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Void>> deleteFlightById(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        List<Flight_Airport> existingInterAirports = flight_AirportRepository.findByIdFlightId(id);
        if (!existingInterAirports.isEmpty()) {
            flight_AirportRepository.deleteAll(existingInterAirports);
        }

        List<Flight_Seat> existingSeats = flight_SeatRepository.findByIdFlightId(id);
        if (!existingSeats.isEmpty()) {
            flight_SeatRepository.deleteAll(existingSeats);
        }

        flightRepository.delete(flight);

        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(200)
                .message("Delete flight successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<FlightResponse>> getFlightById(Long id) {
        // Tìm chuyến bay theo id
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // Tạo response trả về thông tin chuyến bay
        APIResponse<FlightResponse> response = APIResponse.<FlightResponse>builder()
                .data(flightMapper.toFlightResponse(flight))
                .status(200)
                .message("Get flight by ID successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}
