package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.MonthlyRevenueReport;
import com.cnpm.bookingflight.dto.response.MonthlyRevenueReportResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MonthlyRevenueReportMapper {
    public MonthlyRevenueReportResponse toMonthlyRevenueReportResponse(MonthlyRevenueReport report) {
        return MonthlyRevenueReportResponse.builder()
                .year(report.getId().getYear())
                .month(report.getId().getMonth())
                .revenue(report.getRevenue())
                .percentage(report.getPercentage())
                .flightCount(report.getFlightCount())
                .build();
    }

    public List<MonthlyRevenueReportResponse> toMonthlyRevenueReportResponseList(List<MonthlyRevenueReport> reports) {
        return reports.stream()
                .map(this::toMonthlyRevenueReportResponse)
                .toList();
    }
}