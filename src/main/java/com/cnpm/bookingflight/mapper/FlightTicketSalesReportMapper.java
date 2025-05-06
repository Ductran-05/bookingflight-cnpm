package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.FlightTicketSalesReport;
import com.cnpm.bookingflight.dto.response.FlightTicketSalesReportResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightTicketSalesReportMapper {
    public FlightTicketSalesReportResponse toFlightTicketSalesReportResponse(FlightTicketSalesReport report) {
        return FlightTicketSalesReportResponse.builder()
                .year(report.getId().getYear())
                .month(report.getId().getMonth())
                .flightId(report.getId().getFlightId())
                .flightCode(report.getFlight().getFlightCode())
                .ticketCount(report.getTicketCount())
                .percentage(report.getPercentage())
                .build();
    }

    public List<FlightTicketSalesReportResponse> toFlightTicketSalesReportResponseList(List<FlightTicketSalesReport> reports) {
        return reports.stream()
                .map(this::toFlightTicketSalesReportResponse)
                .toList();
    }
}