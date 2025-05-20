package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.AnnualRevenueReport;
import com.cnpm.bookingflight.domain.Flight;
import com.cnpm.bookingflight.domain.Ticket;
import com.cnpm.bookingflight.dto.request.AnnualRevenueReportRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.AnnualRevenueReportResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
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

    public ResponseEntity<APIResponse<List<AnnualRevenueReportResponse>>> generateReport(AnnualRevenueReportRequest request) {
        int year = request.getYear();

        // Validation: Chỉ cho phép báo cáo năm trước hoặc sớm hơn
        int currentYear = LocalDate.now().getYear();
        if (year >= currentYear) {
            throw new AppException(ErrorCode.INVALID_REPORT_DATE);
        }

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
            throw new AppException(ErrorCode.NO_FLIGHTS_FOUND);
        }

        // Tính doanh thu của năm
        double yearlyRevenue = 0;
        for (Flight flight : flightsInYear) {
            List<Ticket> tickets = ticketRepository.findAll().stream()
                    .filter(ticket -> ticket.getFlight().getId().equals(flight.getId()))
                    .toList();
            yearlyRevenue += tickets.stream()
                    .mapToDouble(ticket -> ticket.getSeat().getPrice())
                    .sum();
        }

        // Tạo danh sách chi tiết cho 12 tháng
        List<AnnualRevenueReportResponse.MonthDetail> monthDetails = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            // Tìm các chuyến bay trong tháng
            List<Flight> flightsInMonth = flightRepository.findAll().stream()
                    .filter(flight -> {
                        LocalDate departureDate = flight.getDepartureDate();
                        return departureDate != null &&
                                !departureDate.isBefore(monthStart) &&
                                !departureDate.isAfter(monthEnd);
                    })
                    .toList();

            // Tính doanh thu tháng
            double monthlyRevenue = 0;
            for (Flight flight : flightsInMonth) {
                List<Ticket> tickets = ticketRepository.findAll().stream()
                        .filter(ticket -> ticket.getFlight().getId().equals(flight.getId()))
                        .toList();
                monthlyRevenue += tickets.stream()
                        .mapToDouble(ticket -> ticket.getSeat().getPrice())
                        .sum();
            }

            double percentage = yearlyRevenue > 0 ? (monthlyRevenue / yearlyRevenue) * 100 : 0.0;

            monthDetails.add(AnnualRevenueReportResponse.MonthDetail.builder()
                    .month(month)
                    .revenue(monthlyRevenue)
                    .percentage(percentage)
                    .flightCount(flightsInMonth.size())
                    .build());
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
        List<AnnualRevenueReportResponse> responseList = new ArrayList<>();
        responseList.add(AnnualRevenueReportResponse.builder()
                .year(year)
                .revenue(yearlyRevenue)
                .flightCount(flightsInYear.size())
                .months(monthDetails)
                .build());

        APIResponse<List<AnnualRevenueReportResponse>> response = APIResponse.<List<AnnualRevenueReportResponse>>builder()
                .status(200)
                .message("Annual revenue report generated successfully")
                .data(responseList)
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<AnnualRevenueReportResponse>> getReport(int year) {
        // Validation: Chỉ cho phép báo cáo năm trước hoặc sớm hơn
        int currentYear = LocalDate.now().getYear();
        if (year >= currentYear) {
            throw new AppException(ErrorCode.INVALID_REPORT_DATE);
        }

        // Kiểm tra báo cáo năm có tồn tại không
        AnnualRevenueReport report = annualRevenueReportRepository
                .findById(year)
                .orElseGet(() -> {
                    // Nếu không tồn tại, tạo báo cáo mới
                    // Xác định khoảng thời gian của năm
                    LocalDate startDate = LocalDate.of(year, 1, 1);
                    LocalDate endDate = LocalDate.of(year, 12, 31);

                    // Tìm tất cả các chuyến bay trong năm
                    List<Flight> flightsInYear = flightRepository.findAll().stream()
                            .filter(flight -> {
                                LocalDate departureDate = flight.getDepartureDate();
                                return departureDate != null &&
                                        !departureDate.isBefore(startDate) &&
                                        !departureDate.isAfter(endDate);
                            })
                            .toList();

                    if (flightsInYear.isEmpty()) {
                        throw new AppException(ErrorCode.NO_FLIGHTS_FOUND);
                    }

                    // Tính doanh thu của năm
                    double yearlyRevenue = 0;
                    for (Flight flight : flightsInYear) {
                        List<Ticket> tickets = ticketRepository.findAll().stream()
                                .filter(ticket -> ticket.getFlight().getId().equals(flight.getId()))
                                .toList();
                        yearlyRevenue += tickets.stream()
                                .mapToDouble(ticket -> ticket.getSeat().getPrice())
                                .sum();
                    }

                    // Tạo và lưu báo cáo
                    AnnualRevenueReport newReport = AnnualRevenueReport.builder()
                            .year(year)
                            .revenue(yearlyRevenue)
                            .flightCount(flightsInYear.size())
                            .build();
                    annualRevenueReportRepository.save(newReport);

                    return newReport;
                });

        // Tạo danh sách chi tiết cho 12 tháng
        List<AnnualRevenueReportResponse.MonthDetail> monthDetails = new ArrayList<>();
        double yearlyRevenue = report.getRevenue();

        for (int month = 1; month <= 12; month++) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);

            // Tìm các chuyến bay trong tháng
            List<Flight> flightsInMonth = flightRepository.findAll().stream()
                    .filter(flight -> {
                        LocalDate departureDate = flight.getDepartureDate();
                        return departureDate != null &&
                                !departureDate.isBefore(startDate) &&
                                !departureDate.isAfter(endDate);
                    })
                    .toList();

            // Tính doanh thu tháng
            double monthlyRevenue = 0;
            for (Flight flight : flightsInMonth) {
                List<Ticket> tickets = ticketRepository.findAll().stream()
                        .filter(ticket -> ticket.getFlight().getId().equals(flight.getId()))
                        .toList();
                monthlyRevenue += tickets.stream()
                        .mapToDouble(ticket -> ticket.getSeat().getPrice())
                        .sum();
            }

            double percentage = yearlyRevenue > 0 ? (monthlyRevenue / yearlyRevenue) * 100 : 0.0;

            monthDetails.add(AnnualRevenueReportResponse.MonthDetail.builder()
                    .month(month)
                    .revenue(monthlyRevenue)
                    .percentage(percentage)
                    .flightCount(flightsInMonth.size())
                    .build());
        }

        // Tạo response DTO
        AnnualRevenueReportResponse responseDto = AnnualRevenueReportResponse.builder()
                .year(year)
                .revenue(report.getRevenue())
                .flightCount(report.getFlightCount())
                .months(monthDetails)
                .build();

        APIResponse<AnnualRevenueReportResponse> response = APIResponse.<AnnualRevenueReportResponse>builder()
                .status(200)
                .message("Annual revenue report retrieved successfully")
                .data(responseDto)
                .build();
        return ResponseEntity.ok(response);
    }
}