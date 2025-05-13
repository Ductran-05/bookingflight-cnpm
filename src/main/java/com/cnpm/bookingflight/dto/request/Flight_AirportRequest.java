package com.cnpm.bookingflight.dto.request;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Flight_AirportRequest {
    Long airportId;
    LocalDateTime departureDateTime;
    LocalDateTime arrivalDateTime;
    String note;
}