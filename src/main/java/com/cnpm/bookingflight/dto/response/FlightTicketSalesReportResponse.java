package com.cnpm.bookingflight.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightTicketSalesReportResponse {
    int year;
    int month;
    Long flightId;
    String flightCode;
    int ticketCount;
    double percentage;
}