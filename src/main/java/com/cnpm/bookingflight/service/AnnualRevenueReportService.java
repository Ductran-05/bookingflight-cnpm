package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.AnnualRevenueReport;
import com.cnpm.bookingflight.domain.Flight;
import com.cnpm.bookingflight.domain.Ticket;
import com.cnpm.bookingflight.dto.request.AnnualRevenueReportRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.AnnualRevenueReportResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.AnnualRevenueReportMapper;
import com.cnpm.bookingflight.repository.AnnualRevenueReportRepository;
import com.cnpm.bookingflight.repository.FlightRepository;
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
public class AnnualRevenueReportService {
    final FlightRepository flightRepository;
    final TicketRepository ticketRepository;
    final AnnualRevenueReportRepository annualRevenueReportRepository;
    final AnnualRevenueReportMapper annualRevenueReportMapper;

    public ResponseEntity<APIResponse<List<AnnualRevenueReportResponse>>> generateReport(AnnualRevenueReportRequest request) {
        int year = request.getYear();

        // Xác định khoảng thời gian của năm
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        // Tìm tất cả các chuyến bay trong năm được yêu cầu
        List<Flight> flightsInYear = flightRepository.findAll().stream()
                .filter(flight -> {
                    LocalDate departureDate = flight.getDepartureDate();
                    return departureDate != null &&
                            !departureDate.isBefore(startDate) &&
                            !departureDate.isAfter(endDate);
                })
                .toList();

        if (flightsInYear.isEmpty()) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        // Tính doanh thu của năm
        double yearlyRevenue = 0;
        for (Flight flight : flightsInYear) {
            List<Ticket> tickets = ticketRepository.findAll().stream()
                    .filter(ticket -> ticket.getFlight().getId().equals(flight.getId()) && ticket.getIsPaid())
                    .toList();
            yearlyRevenue += tickets.stream()
                    .mapToDouble(ticket -> ticket.getSeat().getPrice())
                    .sum();
        }

        // Tạo báo cáo
        AnnualRevenueReport report = AnnualRevenueReport.builder()
                .year(year)
                .revenue(yearlyRevenue)
                .flightCount(flightsInYear.size())
                .build();

        // Lưu báo cáo vào cơ sở dữ liệu
        List<AnnualRevenueReport> reports = new ArrayList<>();
        reports.add(report);
        annualRevenueReportRepository.saveAll(reports);

        // Chuyển đổi sang DTO để trả về
        List<AnnualRevenueReportResponse> responseList = annualRevenueReportMapper
                .toAnnualRevenueReportResponseList(reports);

        APIResponse<List<AnnualRevenueReportResponse>> response = APIResponse.<List<AnnualRevenueReportResponse>>builder()
                .status(200)
                .message("Annual revenue report generated successfully")
                .data(responseList)
                .build();
        return ResponseEntity.ok(response);
    }
}