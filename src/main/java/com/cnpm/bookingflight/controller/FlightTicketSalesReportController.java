package com.cnpm.bookingflight.controller;

import com.cnpm.bookingflight.dto.request.FlightTicketSalesReportRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.FlightTicketSalesReportResponse;
import com.cnpm.bookingflight.service.FlightTicketSalesReportService;
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
@RequestMapping("/reports/flight-ticket-sales")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightTicketSalesReportController {
    final FlightTicketSalesReportService flightTicketSalesReportService;

    @PostMapping
    public ResponseEntity<APIResponse<List<FlightTicketSalesReportResponse>>> generateReport(
            @Valid @RequestBody FlightTicketSalesReportRequest request) {
        return flightTicketSalesReportService.generateReport(request);
    }
}