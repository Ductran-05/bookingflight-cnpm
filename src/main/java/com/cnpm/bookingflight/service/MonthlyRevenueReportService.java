package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Flight;
import com.cnpm.bookingflight.domain.FlightTicketSalesReport;
import com.cnpm.bookingflight.domain.MonthlyRevenueReport;
import com.cnpm.bookingflight.domain.Ticket;
import com.cnpm.bookingflight.domain.id.FlightTicketSalesReportId;
import com.cnpm.bookingflight.domain.id.MonthlyRevenueReportId;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.MonthlyRevenueReportResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.repository.FlightRepository;
import com.cnpm.bookingflight.repository.FlightTicketSalesReportRepository;
import com.cnpm.bookingflight.repository.MonthlyRevenueReportRepository;
import com.cnpm.bookingflight.repository.TicketRepository;
import com.cnpm.bookingflight.repository.Flight_SeatRepository;
import com.cnpm.bookingflight.domain.id.Flight_SeatId;
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
    final FlightTicketSalesReportRepository flightTicketSalesReportRepository;
    final Flight_SeatRepository flightSeatRepository;

    public ResponseEntity<APIResponse<MonthlyRevenueReportResponse>> getReport(int year, int month) {
        // Validation: Chỉ cho phép báo cáo tháng trước hoặc sớm hơn
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();
        if (year > currentYear || (year == currentYear && month >= currentMonth)) {
            throw new AppException(ErrorCode.INVALID_REPORT_DATE);
        }

        // Kiểm tra báo cáo tháng có tồn tại không
        MonthlyRevenueReport report = monthlyRevenueReportRepository
                .findById(new MonthlyRevenueReportId(year, month))
                .orElseGet(() -> {
                    // Nếu không tồn tại, tạo báo cáo mới
                    // Xác định khoảng thời gian của tháng
                    LocalDate startDate = LocalDate.of(year, month, 1);
                    LocalDate endDate = startDate.plusMonths(1).minusDays(1);

                    // Tìm tất cả các chuyến bay trong tháng
                    List<Flight> flightsInMonth = flightRepository.findAll().stream()
                            .filter(flight -> {
                                LocalDate departureDate = flight.getDepartureDate();
                                return departureDate != null &&
                                        !departureDate.isBefore(startDate) &&
                                        !departureDate.isAfter(endDate);
                            })
                            .toList();

                    // Tính doanh thu của tháng
                    double monthlyRevenue = 0;
                    List<MonthlyRevenueReportResponse.FlightDetail> flightDetails = new ArrayList<>();
                    for (Flight flight : flightsInMonth) {
                        List<Ticket> tickets = ticketRepository.findAll().stream()
                                .filter(ticket -> ticket.getFlight().getId().equals(flight.getId()))
                                .toList();
                        double flightRevenue = tickets.stream()
                                .mapToDouble(ticket -> {
                                    // Tìm Flight_Seat dựa trên flightId và seatId
                                    return flightSeatRepository.findById(new Flight_SeatId(flight.getId(), ticket.getSeat().getId()))
                                            .map(fs -> fs.getPrice().doubleValue())
                                            .orElse(0.0);
                                })
                                .sum();
                        monthlyRevenue += flightRevenue;

                        flightDetails.add(MonthlyRevenueReportResponse.FlightDetail.builder()
                                .flightId(flight.getId())
                                .flightCode(flight.getFlightCode())
                                .ticketCount(tickets.size())
                                .revenue(flightRevenue)
                                .percentage(0.0) // Sẽ cập nhật sau
                                .build());
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
                                .filter(ticket -> ticket.getFlight().getId().equals(flight.getId()))
                                .toList();
                        yearlyRevenue += tickets.stream()
                                .mapToDouble(ticket -> {
                                    // Tìm Flight_Seat dựa trên flightId và seatId
                                    return flightSeatRepository.findById(new Flight_SeatId(flight.getId(), ticket.getSeat().getId()))
                                            .map(fs -> fs.getPrice().doubleValue())
                                            .orElse(0.0);
                                })
                                .sum();
                    }

                    // Tính tỷ lệ doanh thu
                    double percentage = yearlyRevenue > 0 ? (monthlyRevenue / yearlyRevenue) * 100 : 0.0;

                    // Cập nhật tỷ lệ doanh thu cho từng chuyến bay
                    final double finalMonthlyRevenue = monthlyRevenue;
                    flightDetails.forEach(detail ->
                            detail.setPercentage(finalMonthlyRevenue > 0 ? (detail.getRevenue() / finalMonthlyRevenue) * 100 : 0.0));

                    // Tạo và lưu báo cáo
                    MonthlyRevenueReport newReport = MonthlyRevenueReport.builder()
                            .id(new MonthlyRevenueReportId(year, month))
                            .revenue(monthlyRevenue)
                            .percentage(percentage)
                            .flightCount(flightsInMonth.size())
                            .build();
                    monthlyRevenueReportRepository.save(newReport);

                    // Lưu thông tin bán vé của từng chuyến bay vào FlightTicketSalesReport
                    List<FlightTicketSalesReport> salesReports = flightDetails.stream()
                            .map(detail -> FlightTicketSalesReport.builder()
                                    .id(new FlightTicketSalesReportId(detail.getFlightId(), month, year))
                                    .flight(flightRepository.findById(detail.getFlightId())
                                            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)))
                                    .percentage(detail.getPercentage())
                                    .ticketCount(detail.getTicketCount())
                                    .build())
                            .toList();
                    flightTicketSalesReportRepository.saveAll(salesReports);

                    return newReport;
                });

        // Xác định khoảng thời gian của tháng
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // Tìm tất cả các chuyến bay trong tháng
        List<Flight> flightsInMonth = flightRepository.findAll().stream()
                .filter(flight -> {
                    LocalDate departureDate = flight.getDepartureDate();
                    return departureDate != null &&
                            !departureDate.isBefore(startDate) &&
                            !departureDate.isAfter(endDate);
                })
                .toList();

        // Tính doanh thu và thông tin chi tiết cho từng chuyến bay
        List<MonthlyRevenueReportResponse.FlightDetail> flightDetails = new ArrayList<>();
        double monthlyRevenue = report.getRevenue();
        for (Flight flight : flightsInMonth) {
            List<Ticket> tickets = ticketRepository.findAll().stream()
                    .filter(ticket -> ticket.getFlight().getId().equals(flight.getId()))
                    .toList();
            double flightRevenue = tickets.stream()
                    .mapToDouble(ticket -> {
                        // Tìm Flight_Seat dựa trên flightId và seatId
                        return flightSeatRepository.findById(new Flight_SeatId(flight.getId(), ticket.getSeat().getId()))
                                .map(fs -> fs.getPrice().doubleValue())
                                .orElse(0.0);
                    })
                    .sum();
            double flightPercentage = monthlyRevenue > 0 ? (flightRevenue / monthlyRevenue) * 100 : 0.0;

            flightDetails.add(MonthlyRevenueReportResponse.FlightDetail.builder()
                    .flightId(flight.getId())
                    .flightCode(flight.getFlightCode())
                    .ticketCount(tickets.size())
                    .revenue(flightRevenue)
                    .percentage(flightPercentage)
                    .build());
        }

        // Lưu thông tin bán vé của từng chuyến bay vào FlightTicketSalesReport
        List<FlightTicketSalesReport> salesReports = flightDetails.stream()
                .map(detail -> FlightTicketSalesReport.builder()
                        .id(new FlightTicketSalesReportId(detail.getFlightId(), month, year))
                        .flight(flightRepository.findById(detail.getFlightId())
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)))
                        .percentage(detail.getPercentage())
                        .ticketCount(detail.getTicketCount())
                        .build())
                .toList();
        flightTicketSalesReportRepository.saveAll(salesReports);

        // Nếu không có chuyến bay, tạo báo cáo với giá trị mặc định
        if (flightsInMonth.isEmpty()) {
            monthlyRevenue = 0.0;
            flightDetails = new ArrayList<>();
        }

        // Tạo response DTO
        MonthlyRevenueReportResponse responseDto = MonthlyRevenueReportResponse.builder()
                .year(year)
                .month(month)
                .revenue(monthlyRevenue)
                .percentage(report.getPercentage())
                .flightCount(flightsInMonth.size())
                .flights(flightDetails)
                .build();

        APIResponse<MonthlyRevenueReportResponse> response = APIResponse.<MonthlyRevenueReportResponse>builder()
                .status(200)
                .message("Monthly revenue report retrieved successfully")
                .data(responseDto)
                .build();
        return ResponseEntity.ok(response);
    }
}