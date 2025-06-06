package com.cnpm.bookingflight.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
public class FlightTicketResponse {
    Long id;
    String flightCode;
    PlaneResponse plane;
    AirportResponse departureAirport;
    AirportResponse arrivalAirport;
    LocalDate departureDate;
    LocalDate arrivalDate;
    LocalTime departureTime;
    LocalTime arrivalTime;
    Integer originalPrice;
    List<Flight_AirportResponse> interAirports;
}