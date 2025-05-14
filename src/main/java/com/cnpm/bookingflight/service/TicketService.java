package com.cnpm.bookingflight.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cnpm.bookingflight.domain.Ticket;
import com.cnpm.bookingflight.domain.id.Flight_SeatId;
import com.cnpm.bookingflight.dto.request.TicketRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.TicketResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.TicketMapper;
import com.cnpm.bookingflight.repository.Flight_SeatRepository;
import com.cnpm.bookingflight.repository.TicketRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketService {

    final TicketRepository ticketRepository;
    final Flight_SeatService flight_SeatService;
    final TicketMapper ticketMapper;
    final Flight_SeatRepository flight_SeatRepository;

    public ResponseEntity<APIResponse<List<TicketResponse>>> getAllTickets() {
        APIResponse<List<TicketResponse>> response = APIResponse.<List<TicketResponse>>builder()
                .data(ticketMapper.toTicketResponseList(ticketRepository.findAll()))
                .status(200)
                .message("get all tickets successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<TicketResponse>> getTicketById(Long id) {
        APIResponse<TicketResponse> response = APIResponse.<TicketResponse>builder()
                .data(ticketMapper.toTicketResponse(ticketRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID))))
                .status(200)
                .message("get ticket by id successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<List<TicketResponse>>> bookingTicket(TicketRequest request) {
        // Kiểm tra flightId
        if (request.getFlightId() == null) {
            throw new AppException(ErrorCode.INVALID);
        }

        // Kiểm tra danh sách vé (tickets)
        if (request.getTickets() == null || request.getTickets().isEmpty()) {
            throw new AppException(ErrorCode.INVALID);
        }

        // Đếm số lượng vé cho từng seatId
        Map<Long, Integer> seatQuantities = new HashMap<>();
        for (TicketRequest.TicketInfo ticketInfo : request.getTickets()) {
            if (ticketInfo.getSeatId() == null) {
                throw new AppException(ErrorCode.INVALID);
            }
            seatQuantities.merge(ticketInfo.getSeatId(), 1, Integer::sum);
        }

        // Kiểm tra và cập nhật số lượng vé còn lại
        for (Map.Entry<Long, Integer> entry : seatQuantities.entrySet()) {
            Long seatId = entry.getKey();
            int quantity = entry.getValue();
            TicketRequest tempRequest = new TicketRequest();
            tempRequest.setFlightId(request.getFlightId());
            List<TicketRequest.TicketInfo> tempTickets = new ArrayList<>();
            TicketRequest.TicketInfo tempTicketInfo = new TicketRequest.TicketInfo();
            tempTicketInfo.setSeatId(seatId);
            tempTickets.add(tempTicketInfo);
            tempRequest.setTickets(tempTickets);
            flight_SeatService.bookingTicket(tempRequest, quantity);
        }

        // Tạo vé cho mỗi thông tin vé (ticketInfo)
        List<Ticket> tickets = new ArrayList<>();
        for (TicketRequest.TicketInfo ticketInfo : request.getTickets()) {
            TicketRequest tempRequest = new TicketRequest();
            tempRequest.setFlightId(request.getFlightId());
            List<TicketRequest.TicketInfo> tempTickets = new ArrayList<>();
            tempTickets.add(ticketInfo);
            tempRequest.setTickets(tempTickets);
            Ticket ticket = ticketMapper.toTicket(tempRequest);
            tickets.add(ticket);
        }

        // Lưu tất cả vé
        List<Ticket> savedTickets = ticketRepository.saveAll(tickets);

        // Trả về danh sách vé đã tạo
        APIResponse<List<TicketResponse>> response = APIResponse.<List<TicketResponse>>builder()
                .status(201)
                .message("Booking tickets successfully")
                .data(ticketMapper.toTicketResponseList(savedTickets))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Void>> deleteTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        flight_SeatRepository.deleteById(new Flight_SeatId(ticket.getFlight().getId(), ticket.getSeat().getId()));
        ticketRepository.deleteById(id);
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(200)
                .message("delete by id successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<TicketResponse>> updateTicket(Long id, TicketRequest request) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        // Kiểm tra danh sách vé và lấy thông tin đầu tiên
        TicketRequest.TicketInfo ticketInfo = null;
        if (request.getTickets() != null && !request.getTickets().isEmpty()) {
            ticketInfo = request.getTickets().get(0);
        } else {
            throw new AppException(ErrorCode.INVALID);
        }

        // Kiểm tra nếu seatId thay đổi
        if (!ticket.getSeat().getId().equals(ticketInfo.getSeatId())) {
            flight_SeatRepository.deleteById(new Flight_SeatId(ticket.getFlight().getId(), ticket.getSeat().getId()));
            TicketRequest tempRequest = new TicketRequest();
            tempRequest.setFlightId(request.getFlightId());
            List<TicketRequest.TicketInfo> tempTickets = new ArrayList<>();
            tempTickets.add(ticketInfo);
            tempRequest.setTickets(tempTickets);
            flight_SeatService.bookingTicket(tempRequest, 1); // Cập nhật với quantity=1
        }

        // Cập nhật thông tin vé
        Ticket updatedTicket = ticketMapper.updateTicket(ticket, request);
        APIResponse<TicketResponse> response = APIResponse.<TicketResponse>builder()
                .status(200)
                .message("update ticket successfully")
                .data(ticketMapper.toTicketResponse(ticketRepository.save(updatedTicket)))
                .build();
        return ResponseEntity.ok(response);
    }
}