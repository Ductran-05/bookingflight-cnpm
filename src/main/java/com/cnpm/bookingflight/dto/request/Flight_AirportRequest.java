package com.cnpm.bookingflight.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Flight_AirportRequest {
    @NotNull(message = "airportId must not be null")
    @Positive(message = "airportId must be positive")
    Long airportId;

    @NotNull(message = "arrivalDateTime must not be null")
    @Future(message = "arrivalDateTime must be in the future")
    LocalDateTime arrivalDateTime;

    @NotNull(message = "departureDateTime must not be null")
    @Future(message = "departureDateTime must be in the future")
    LocalDateTime departureDateTime;

    String note;
}