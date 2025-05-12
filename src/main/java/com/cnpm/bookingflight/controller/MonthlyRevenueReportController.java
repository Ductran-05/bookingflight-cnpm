package com.cnpm.bookingflight.controller;

import com.cnpm.bookingflight.dto.request.MonthlyRevenueReportRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.MonthlyRevenueReportResponse;
import com.cnpm.bookingflight.service.MonthlyRevenueReportService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reports/monthly-revenue")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MonthlyRevenueReportController {
    final MonthlyRevenueReportService monthlyRevenueReportService;

    @PostMapping
    public ResponseEntity<APIResponse<List<MonthlyRevenueReportResponse>>> generateReport(
            @Valid @RequestBody MonthlyRevenueReportRequest request) {
        return monthlyRevenueReportService.generateReport(request);
    }
}