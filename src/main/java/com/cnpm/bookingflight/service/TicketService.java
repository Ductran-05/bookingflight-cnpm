package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Flight;
import com.cnpm.bookingflight.domain.Flight_Seat;
import com.cnpm.bookingflight.domain.Parameters;
import com.cnpm.bookingflight.domain.Ticket;
import com.cnpm.bookingflight.domain.id.Flight_SeatId;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.TicketRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.TicketResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.ResultPaginationMapper;
import com.cnpm.bookingflight.mapper.TicketMapper;
import com.cnpm.bookingflight.repository.FlightRepository;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketService {

    final TicketRepository ticketRepository;
    final TicketMapper ticketMapper;
    final Flight_SeatRepository flight_SeatRepository;
    final ResultPaginationMapper resultPaginationMapper;
    final FlightRepository flightRepository;
    final ParametersRepository parametersRepository;

    public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllTickets(Specification<Ticket> spec,
                                                                          Pageable pageable) {
        ResultPaginationDTO result = resultPaginationMapper
                .toResultPagination(ticketRepository.findAll(spec, pageable).map(ticketMapper::toTicketResponse));
        APIResponse<ResultPaginationDTO> response = APIResponse.<ResultPaginationDTO>builder()
                .status(200)
                .message("Get all tickets successfully")
                .data(result)
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<TicketResponse>> getTicketById(Long id) {
        APIResponse<TicketResponse> response = APIResponse.<TicketResponse>builder()
                .data(ticketMapper.toTicketResponse(ticketRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND))))
                .status(200)
                .message("Get ticket by id successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<List<TicketResponse>>> bookingTicket(@Valid TicketRequest request) {
        // Lấy thông số từ bảng Parameters
        Parameters parameters = parametersRepository.findById(1L)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // Kiểm tra thời gian đặt vé
        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        LocalDateTime departureDateTime = LocalDateTime.of(flight.getDepartureDate(), flight.getDepartureTime());
        LocalDateTime bookingDeadline = departureDateTime.minusDays(parameters.getLatestBookingDay());
        LocalDateTime currentTime = LocalDateTime.now();
        if (currentTime.isAfter(bookingDeadline)) {
            throw new AppException(ErrorCode.INVALID_BOOKING_TIME,
                    "Booking must be made at least " + parameters.getLatestBookingDay() + " day(s) before departure");
        }

        // Đếm số lượng vé cho từng seatId
        Map<Long, Integer> seatQuantities = new HashMap<>();
        for (TicketRequest.TicketInfo ticketInfo : request.getTickets()) {
            seatQuantities.merge(ticketInfo.getSeatId(), 1, Integer::sum);
        }

        // Kiểm tra số vé còn lại
        for (Map.Entry<Long, Integer> entry : seatQuantities.entrySet()) {
            Long seatId = entry.getKey();
            int quantity = entry.getValue();
            Flight_Seat flightSeat = flight_SeatRepository.findById(new Flight_SeatId(request.getFlightId(), seatId))
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            if (flightSeat.getRemainingTickets() < quantity) {
                throw new AppException(ErrorCode.OUT_OF_TICKETS,
                        "Not enough tickets available for seat ID " + seatId);
            }
        }

        // Cập nhật số vé còn lại và tạo vé
        List<Ticket> tickets = new ArrayList<>();
        for (TicketRequest.TicketInfo ticketInfo : request.getTickets()) {
            Flight_Seat flightSeat = flight_SeatRepository.findById(new Flight_SeatId(request.getFlightId(), ticketInfo.getSeatId()))
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            flightSeat.setRemainingTickets(flightSeat.getRemainingTickets() - 1);
            flight_SeatRepository.save(flightSeat);

            Ticket ticket = ticketMapper.toTicket(ticketInfo, request.getFlightId());
            tickets.add(ticket);
        }

        List<Ticket> savedTickets = ticketRepository.saveAll(tickets);

        APIResponse<List<TicketResponse>> response = APIResponse.<List<TicketResponse>>builder()
                .status(201)
                .message("Booking tickets successfully")
                .data(ticketMapper.toTicketResponseList(savedTickets))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<TicketResponse>> updateTicket(Long id, @Valid TicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (request.getTickets() == null || request.getTickets().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_TICKET_INFO);
        }
        TicketRequest.TicketInfo ticketInfo = request.getTickets().get(0);

        // Kiểm tra nếu seatId thay đổi
        if (!ticket.getSeat().getId().equals(ticketInfo.getSeatId())) {
            Flight_Seat oldFlightSeat = flight_SeatRepository.findById(new Flight_SeatId(ticket.getFlight().getId(), ticket.getSeat().getId()))
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            oldFlightSeat.setRemainingTickets(oldFlightSeat.getRemainingTickets() + 1);
            flight_SeatRepository.save(oldFlightSeat);

            Flight_Seat newFlightSeat = flight_SeatRepository.findById(new Flight_SeatId(request.getFlightId(), ticketInfo.getSeatId()))
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            if (newFlightSeat.getRemainingTickets() < 1) {
                throw new AppException(ErrorCode.OUT_OF_TICKETS,
                        "Not enough tickets available for seat ID " + ticketInfo.getSeatId());
            }
            newFlightSeat.setRemainingTickets(newFlightSeat.getRemainingTickets() - 1);
            flight_SeatRepository.save(newFlightSeat);
        }

        // Cập nhật thông tin vé
        Ticket updatedTicket = ticketMapper.updateTicket(ticket, request);
        APIResponse<TicketResponse> response = APIResponse.<TicketResponse>builder()
                .status(200)
                .message("Update ticket successfully")
                .data(ticketMapper.toTicketResponse(ticketRepository.save(updatedTicket)))
                .build();
        return ResponseEntity.ok(response);
    }
}