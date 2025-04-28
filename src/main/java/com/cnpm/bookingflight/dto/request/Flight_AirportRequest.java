package com.cnpm.bookingflight.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Flight_AirportRequest {
    Long airportId;
    LocalDate departureDate;
    LocalDate arrivalDate;
    LocalTime departureTime;
    LocalTime arrivalTime;
    String note;
}
