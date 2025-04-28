package com.cnpm.bookingflight.dto.request;

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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class FlightRequest {
    String flightCode;
    Long planeId;
    Long departureAirportId;
    Long arrivalAirportId;
    LocalDate departureDate;
    LocalDate arrivalDate;
    LocalTime departureTime;
    LocalTime arrivalTime;
    Integer originPrice;

    List<Flight_AirportRequest> interAirports;
    List<Flight_SeatRequest> seats;
}
