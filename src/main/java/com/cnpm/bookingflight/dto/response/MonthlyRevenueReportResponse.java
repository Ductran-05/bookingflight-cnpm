package com.cnpm.bookingflight.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MonthlyRevenueReportResponse {
    int year;
    int month;
    double revenue;
    double percentage;
    int flightCount;
    List<FlightDetail> flights;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlightDetail {
        private Long flightId;
        private String flightCode;
        private int ticketCount;
        private double revenue;
        private double percentage;
    }
}