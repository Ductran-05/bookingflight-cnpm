package com.cnpm.bookingflight.domain.id;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Embeddable
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class FlightTicketSalesReportId implements Serializable {
    Long flightId;
    int month;
    int year;
}
