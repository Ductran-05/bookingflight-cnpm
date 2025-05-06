package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.AnnualRevenueReport;
import com.cnpm.bookingflight.dto.response.AnnualRevenueReportResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnnualRevenueReportMapper {
    public AnnualRevenueReportResponse toAnnualRevenueReportResponse(AnnualRevenueReport report) {
        return AnnualRevenueReportResponse.builder()
                .year(report.getYear())
                .revenue(report.getRevenue())
                .flightCount(report.getFlightCount())
                .build();
    }

    public List<AnnualRevenueReportResponse> toAnnualRevenueReportResponseList(List<AnnualRevenueReport> reports) {
        return reports.stream()
                .map(this::toAnnualRevenueReportResponse)
                .toList();
    }
}