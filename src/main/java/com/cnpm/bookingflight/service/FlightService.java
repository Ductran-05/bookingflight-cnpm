package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Flight;
import com.cnpm.bookingflight.domain.Flight_Airport;
import com.cnpm.bookingflight.domain.Flight_Seat;
import com.cnpm.bookingflight.domain.Parameters;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.FlightRequest;
import com.cnpm.bookingflight.dto.request.Flight_AirportRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.FlightCountResponse;
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
import com.cnpm.bookingflight.repository.ParametersRepository;
import com.cnpm.bookingflight.repository.TicketRepository;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        final ParametersRepository parametersRepository;

        private void validateFlightRequest(@Valid FlightRequest request) {
                // Lấy thông số từ bảng Parameters
                Parameters parameters = parametersRepository.findById(1L)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

                // Tính thời gian bay
                LocalDateTime departureDateTime = LocalDateTime.of(request.getDepartureDate(), request.getDepartureTime());
                LocalDateTime arrivalDateTime = LocalDateTime.of(request.getArrivalDate(), request.getArrivalTime());

                // Kiểm tra thời gian đến phải sau thời gian đi
                if (!arrivalDateTime.isAfter(departureDateTime)) {
                        throw new AppException(ErrorCode.INVALID_FLIGHT_DURATION,
                                "Arrival time must be after departure time");
                }

                // Kiểm tra thời gian bay tối thiểu
                long flightDurationMinutes = Duration.between(departureDateTime, arrivalDateTime).toMinutes();
                if (flightDurationMinutes < parameters.getMinFlightTime()) {
                        throw new AppException(ErrorCode.INVALID_FLIGHT_DURATION,
                                "Flight duration must be at least " + parameters.getMinFlightTime() + " minutes");
                }

                // Kiểm tra số lượng sân bay trung gian
                if (request.getInterAirports() != null && request.getInterAirports().size() > parameters.getMaxInterQuantity()) {
                        throw new AppException(ErrorCode.INVALID_INTER_AIRPORTS,
                                "Number of intermediate airports cannot exceed " + parameters.getMaxInterQuantity());
                }

                // Kiểm tra sân bay trung gian
                if (request.getInterAirports() != null && !request.getInterAirports().isEmpty()) {
                        for (Flight_AirportRequest interAirport : request.getInterAirports()) {
                                // Kiểm tra thời gian khởi hành phải sau thời gian đến tại sân bay trung gian
                                if (!interAirport.getDepartureDateTime().isAfter(interAirport.getArrivalDateTime())) {
                                        throw new AppException(ErrorCode.INVALID_STOP_DURATION,
                                                "Departure time at intermediate airport must be after arrival time");
                                }

                                // Kiểm tra thời gian dừng tại sân bay trung gian
                                long stopDurationMinutes = Duration.between(interAirport.getArrivalDateTime(), interAirport.getDepartureDateTime()).toMinutes();
                                if (stopDurationMinutes < parameters.getMinStopTime() || stopDurationMinutes > parameters.getMaxStopTime()) {
                                        throw new AppException(ErrorCode.INVALID_STOP_DURATION,
                                                "Stop duration must be between " + parameters.getMinStopTime() + " and " + parameters.getMaxStopTime() + " minutes");
                                }

                                // Kiểm tra thời gian đến của sân bay trung gian nằm trong khoảng thời gian bay
                                if (interAirport.getArrivalDateTime().isBefore(departureDateTime) ||
                                        interAirport.getArrivalDateTime().isAfter(arrivalDateTime) ||
                                        interAirport.getDepartureDateTime().isBefore(departureDateTime) ||
                                        interAirport.getDepartureDateTime().isAfter(arrivalDateTime)) {
                                        throw new AppException(ErrorCode.INVALID_STOP_DURATION,
                                                "Intermediate airport times must be between flight departure and arrival times");
                                }
                        }
                }
        }

        public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllFlights(Specification<Flight> spec, Pageable pageable) {
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

        public ResponseEntity<APIResponse<FlightResponse>> createFlight(@Valid FlightRequest request) {
                validateFlightRequest(request);

                Flight existingFlight = flightRepository.findByFlightCode(request.getFlightCode());
                if (existingFlight != null) {
                        throw new AppException(ErrorCode.EXISTED);
                }

                Flight flight = flightMapper.toFlight(request);
                Flight savedFlight = flightRepository.save(flight);

                // Lưu các sân bay trung gian (Flight_Airport)
                if (request.getInterAirports() != null && !request.getInterAirports().isEmpty()) {
                        List<Flight_Airport> flightAirports = request.getInterAirports().stream()
                                .map(flightAirportRequest -> flightAirportMapper.toFlight_Airport(flightAirportRequest, savedFlight.getId()))
                                .collect(Collectors.toList());
                        flightAirportRepository.saveAll(flightAirports);
                }

                // Lưu các ghế (Flight_Seat)
                if (request.getSeats() != null && !request.getSeats().isEmpty()) {
                        List<Flight_Seat> flightSeats = request.getSeats().stream()
                                .map(flightSeatRequest -> flightSeatMapper.toFlight_Seat(flightSeatRequest, savedFlight.getId()))
                                .collect(Collectors.toList());
                        flightSeatRepository.saveAll(flightSeats);
                }

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

        @Transactional
        public ResponseEntity<APIResponse<FlightResponse>> updateFlight(Long id, @Valid FlightRequest request) {
                validateFlightRequest(request);

                Flight flight = flightRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                if (ticketRepository.existsByFlightId(id)) {
                        throw new AppException(ErrorCode.FLIGHT_HAS_TICKETS);
                }

                Flight updatedFlight = flightMapper.toFlight(request);
                updatedFlight.setId(id);
                Flight savedFlight = flightRepository.save(updatedFlight);

                flightAirportRepository.deleteByIdFlightId(id);
                if (request.getInterAirports() != null && !request.getInterAirports().isEmpty()) {
                        List<Flight_Airport> flightAirports = request.getInterAirports().stream()
                                .map(flightAirportRequest -> flightAirportMapper.toFlight_Airport(flightAirportRequest, savedFlight.getId()))
                                .collect(Collectors.toList());
                        flightAirportRepository.saveAll(flightAirports);
                }

                flightSeatRepository.deleteByIdFlightId(id);
                if (request.getSeats() != null && !request.getSeats().isEmpty()) {
                        List<Flight_Seat> flightSeats = request.getSeats().stream()
                                .map(flightSeatRequest -> flightSeatMapper.toFlight_Seat(flightSeatRequest, savedFlight.getId()))
                                .collect(Collectors.toList());
                        flightSeatRepository.saveAll(flightSeats);
                }

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

        public ResponseEntity<APIResponse<FlightCountResponse>> getFlightCount(String period) {
                LocalDate currentDate = LocalDate.now();
                int currentYear = currentDate.getYear();
                int currentMonth = currentDate.getMonthValue();

                FlightCountResponse flightCountResponse = new FlightCountResponse();
                flightCountResponse.setPeriodType(period);

                switch (period.toLowerCase()) {
                        case "month":
                                long currentMonthCount = flightRepository.countFlightsByMonth(currentYear, currentMonth);
                                long previousMonthCount = flightRepository.countFlightsByMonth(
                                        currentMonth == 1 ? currentYear - 1 : currentYear,
                                        currentMonth == 1 ? 12 : currentMonth - 1
                                );
                                flightCountResponse.setCurrentPeriodCount(currentMonthCount);
                                flightCountResponse.setPreviousPeriodCount(previousMonthCount);
                                break;

                        case "year":
                                long currentYearCount = flightRepository.countFlightsByYear(currentYear);
                                long previousYearCount = flightRepository.countFlightsByYear(currentYear - 1);
                                flightCountResponse.setCurrentPeriodCount(currentYearCount);
                                flightCountResponse.setPreviousPeriodCount(previousYearCount);
                                break;

                        default:
                                throw new AppException(ErrorCode.INVALID_PERIOD_TYPE);
                }

                APIResponse<FlightCountResponse> response = APIResponse.<FlightCountResponse>builder()
                        .status(200)
                        .message("Get flight count successfully")
                        .data(flightCountResponse)
                        .build();
                return ResponseEntity.ok(response);
        }
}