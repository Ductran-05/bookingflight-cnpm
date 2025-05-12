package com.cnpm.bookingflight.controller;

import com.cnpm.bookingflight.dto.request.AnnualRevenueReportRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.AnnualRevenueReportResponse;
import com.cnpm.bookingflight.service.AnnualRevenueReportService;
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
@RequestMapping("/reports/annual-revenue")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnnualRevenueReportController {
    final AnnualRevenueReportService annualRevenueReportService;

    @PostMapping
    public ResponseEntity<APIResponse<List<AnnualRevenueReportResponse>>> generateReport(
            @Valid @RequestBody AnnualRevenueReportRequest request) {
        return annualRevenueReportService.generateReport(request);
    }
}