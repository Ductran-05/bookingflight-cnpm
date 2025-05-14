package com.cnpm.bookingflight.dto.response;

import java.time.LocalDateTime;

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
public class Flight_AirportResponse {
    AirportResponse airport;
    LocalDateTime departureDateTime;
    LocalDateTime arrivalDateTime;
    String note;
}