package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Flight;
import com.cnpm.bookingflight.domain.FlightTicketSalesReport;
import com.cnpm.bookingflight.domain.Ticket;
import com.cnpm.bookingflight.domain.id.FlightTicketSalesReportId;
import com.cnpm.bookingflight.dto.request.FlightTicketSalesReportRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.FlightTicketSalesReportResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.FlightTicketSalesReportMapper;
import com.cnpm.bookingflight.repository.FlightRepository;
import com.cnpm.bookingflight.repository.FlightTicketSalesReportRepository;
import com.cnpm.bookingflight.repository.TicketRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightTicketSalesReportService {
    final FlightRepository flightRepository;
    final TicketRepository ticketRepository;
    final FlightTicketSalesReportRepository flightTicketSalesReportRepository;
    final FlightTicketSalesReportMapper flightTicketSalesReportMapper;

    public ResponseEntity<APIResponse<List<FlightTicketSalesReportResponse>>> generateReport(FlightTicketSalesReportRequest request) {
        int year = request.getYear();
        int month = request.getMonth();

        // Tạo khoảng thời gian lọc: đầu tháng đến cuối tháng
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);

        // Tìm tất cả các chuyến bay trong tháng và năm được yêu cầu
        List<Flight> flights = flightRepository.findAll().stream()
                .filter(flight -> {
                    LocalDate departureDate = flight.getDepartureDate();
                    LocalTime departureTime = flight.getDepartureTime();
                    if (departureDate == null || departureTime == null) {
                        return false;
                    }
                    LocalDateTime flightDateTime = departureDate.atTime(departureTime);
                    return flightDateTime.isAfter(startDate) && flightDateTime.isBefore(endDate);
                })
                .toList();

        if (flights.isEmpty()) {
            throw new AppException(ErrorCode.NO_FLIGHTS_FOUND);
        }

        // Tính tổng doanh thu trong tháng
        double totalRevenue = 0;
        List<FlightTicketSalesReport> reports = new ArrayList<>();
        for (Flight flight : flights) {
            List<Ticket> tickets = ticketRepository.findAll().stream()
                    .filter(ticket -> ticket.getFlight().getId().equals(flight.getId()))
                    .toList();
            double flightRevenue = tickets.stream()
                    .mapToDouble(ticket -> ticket.getSeat().getPrice())
                    .sum();
            totalRevenue += flightRevenue;

            FlightTicketSalesReport report = FlightTicketSalesReport.builder()
                    .id(new FlightTicketSalesReportId(flight.getId(), year, month))
                    .flight(flight)
                    .ticketCount(tickets.size())
                    .percentage(0.0) // Sẽ được cập nhật sau khi tính tổng doanh thu
                    .build();
            reports.add(report);
        }

        // Cập nhật tỷ lệ doanh thu
        final double finalTotalRevenue = totalRevenue;
        reports.forEach(report -> {
            List<Ticket> tickets = ticketRepository.findAll().stream()
                    .filter(ticket -> ticket.getFlight().getId().equals(report.getFlight().getId()) )
                    .toList();
            double flightRevenue = tickets.stream()
                    .mapToDouble(ticket -> ticket.getSeat().getPrice())
                    .sum();
            report.setPercentage(finalTotalRevenue > 0 ? (flightRevenue / finalTotalRevenue) * 100 : 0.0);
        });

        // Lưu báo cáo vào cơ sở dữ liệu
        flightTicketSalesReportRepository.saveAll(reports);

        // Chuyển đổi sang DTO để trả về
        List<FlightTicketSalesReportResponse> responseList = flightTicketSalesReportMapper
                .toFlightTicketSalesReportResponseList(reports);

        APIResponse<List<FlightTicketSalesReportResponse>> response = APIResponse.<List<FlightTicketSalesReportResponse>>builder()
                .status(200)
                .message("Flight ticket sales report generated successfully")
                .data(responseList)
                .build();
        return ResponseEntity.ok(response);
    }
}