package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Flight;
import com.cnpm.bookingflight.domain.MonthlyRevenueReport;
import com.cnpm.bookingflight.domain.Ticket;
import com.cnpm.bookingflight.domain.id.MonthlyRevenueReportId;
import com.cnpm.bookingflight.dto.request.MonthlyRevenueReportRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.MonthlyRevenueReportResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.MonthlyRevenueReportMapper;
import com.cnpm.bookingflight.repository.FlightRepository;
import com.cnpm.bookingflight.repository.MonthlyRevenueReportRepository;
import com.cnpm.bookingflight.repository.TicketRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MonthlyRevenueReportService {
    final FlightRepository flightRepository;
    final TicketRepository ticketRepository;
    final MonthlyRevenueReportRepository monthlyRevenueReportRepository;
    final MonthlyRevenueReportMapper monthlyRevenueReportMapper;

    public ResponseEntity<APIResponse<List<MonthlyRevenueReportResponse>>> generateReport(MonthlyRevenueReportRequest request) {
        int year = request.getYear();
        int month = request.getMonth();

        // Xác định khoảng thời gian của tháng
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1); // Ngày cuối cùng của tháng

        // Tìm tất cả các chuyến bay trong tháng được yêu cầu
        List<Flight> flightsInMonth = flightRepository.findAll().stream()
                .filter(flight -> {
                    LocalDate departureDate = flight.getDepartureDate();
                    return departureDate != null &&
                            !departureDate.isBefore(startDate) &&
                            !departureDate.isAfter(endDate);
                })
                .toList();

        if (flightsInMonth.isEmpty()) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        // Tính doanh thu của tháng
        double monthlyRevenue = 0;
        for (Flight flight : flightsInMonth) {
            List<Ticket> tickets = ticketRepository.findAll().stream()
                    .filter(ticket -> ticket.getFlight().getId().equals(flight.getId()) && ticket.getIsPaid())
                    .toList();
            monthlyRevenue += tickets.stream()
                    .mapToDouble(ticket -> ticket.getSeat().getPrice())
                    .sum();
        }

        // Tìm tất cả các chuyến bay trong năm để tính tổng doanh thu cả năm
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);
        List<Flight> flightsInYear = flightRepository.findAll().stream()
                .filter(flight -> {
                    LocalDate departureDate = flight.getDepartureDate();
                    return departureDate != null &&
                            !departureDate.isBefore(yearStart) &&
                            !departureDate.isAfter(yearEnd);
                })
                .toList();

        double yearlyRevenue = 0;
        for (Flight flight : flightsInYear) {
            List<Ticket> tickets = ticketRepository.findAll().stream()
                    .filter(ticket -> ticket.getFlight().getId().equals(flight.getId()) && ticket.getIsPaid())
                    .toList();
            yearlyRevenue += tickets.stream()
                    .mapToDouble(ticket -> ticket.getSeat().getPrice())
                    .sum();
        }

        // Tính tỷ lệ doanh thu
        double percentage = yearlyRevenue > 0 ? (monthlyRevenue / yearlyRevenue) * 100 : 0.0;

        // Tạo báo cáo
        MonthlyRevenueReport report = MonthlyRevenueReport.builder()
                .id(new MonthlyRevenueReportId(year, month))
                .revenue(monthlyRevenue)
                .percentage(percentage)
                .flightCount(flightsInMonth.size())
                .build();

        // Lưu báo cáo vào cơ sở dữ liệu
        List<MonthlyRevenueReport> reports = new ArrayList<>();
        reports.add(report);
        monthlyRevenueReportRepository.saveAll(reports);

        // Chuyển đổi sang DTO để trả về
        List<MonthlyRevenueReportResponse> responseList = monthlyRevenueReportMapper
                .toMonthlyRevenueReportResponseList(reports);

        APIResponse<List<MonthlyRevenueReportResponse>> response = APIResponse.<List<MonthlyRevenueReportResponse>>builder()
                .status(200)
                .message("Monthly revenue report generated successfully")
                .data(responseList)
                .build();
        return ResponseEntity.ok(response);
    }
}