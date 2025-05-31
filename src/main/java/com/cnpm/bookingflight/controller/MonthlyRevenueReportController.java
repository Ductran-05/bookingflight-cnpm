package com.cnpm.bookingflight.controller;

import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.MonthlyRevenueReportResponse;
import com.cnpm.bookingflight.service.MonthlyRevenueReportService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports/monthly-revenue")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MonthlyRevenueReportController {
    final MonthlyRevenueReportService monthlyRevenueReportService;


    @GetMapping("/{month}/{year}")
    public ResponseEntity<APIResponse<MonthlyRevenueReportResponse>> getReport(
            @PathVariable int year,
            @PathVariable int month) {
        return monthlyRevenueReportService.getReport(year, month);
    }
}