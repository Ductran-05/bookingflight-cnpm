package com.cnpm.bookingflight.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class FlightRequest {

    @NotNull(message = "planeId must not be null")
    Long planeId;

    @NotNull(message = "departureAirportId must not be null")
    Long departureAirportId;

    @NotNull(message = "arrivalAirportId must not be null")
    Long arrivalAirportId;

    @NotNull(message = "departureDate must not be null")
    @Future(message = "departureDate must be in the future")
    LocalDate departureDate;

    @NotNull(message = "arrivalDate must not be null")
    @Future(message = "arrivalDate must be in the future")
    LocalDate arrivalDate;

    @NotNull(message = "departureTime must not be null")
    LocalTime departureTime;

    @NotNull(message = "arrivalTime must not be null")
    LocalTime arrivalTime;

    @NotNull(message = "originPrice must not be null")
    @Min(value = 0, message = "originPrice must be non-negative")
    Integer originPrice;

    List<@Valid Flight_AirportRequest> interAirports;

    List<@Valid Flight_SeatRequest> seats;
}