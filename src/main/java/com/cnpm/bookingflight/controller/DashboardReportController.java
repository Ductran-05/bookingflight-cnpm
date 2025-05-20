package com.cnpm.bookingflight.controller;

import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.DashboardResponse;
import com.cnpm.bookingflight.dto.response.YearlyTicketResponse;
import com.cnpm.bookingflight.service.DashboardReportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardReportController {
    final DashboardReportService dashboardReportService;

    @GetMapping
    public ResponseEntity<APIResponse<DashboardResponse>> getDashboardReport() {
        return dashboardReportService.getDashboardReport();
    }

    @GetMapping("/{year}")
    public ResponseEntity<APIResponse<YearlyTicketResponse>> getYearlyTicketReport(@PathVariable int year) {
        return dashboardReportService.getYearlyTicketReport(year);
    }
}