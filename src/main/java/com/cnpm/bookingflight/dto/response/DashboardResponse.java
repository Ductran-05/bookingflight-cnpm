package com.cnpm.bookingflight.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private double revenueThisYear;
    private double revenueLastYear;
    private double revenueThisMonth;
    private double revenueLastMonth;
    private int flightCountThisYear;
    private int flightCountLastYear;
    private int airlineCount;
    private int airportCount;
    private Map<String, Double> airlinePopularity; // Map: airline name -> percentage
}