package com.cnpm.bookingflight.domain.id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightTicketSalesReportId implements Serializable {
    private Long flightId;
    private int year;
    private int month;
}