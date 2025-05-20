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
public class AnnualRevenueReportResponse {
    int year;
    double revenue;
    int flightCount;
    List<MonthDetail> months;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthDetail {
        private int month;
        private double revenue;
        private double percentage;
        private int flightCount;
    }
}