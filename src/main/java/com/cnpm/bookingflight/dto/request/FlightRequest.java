package com.cnpm.bookingflight.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlightRequest {
    String flightCode;
    String planeId;
    String departureAirportId;
    String arrivalAirportId;
    LocalDate departureDate;
    LocalDate arrivalDate;
    LocalTime departureTime;
    LocalTime arrivalTime;
    Double originPrice;

    List<Flight_AirportRequest> interAirports;
    List<Flight_SeatRequest> seats;
}

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
class Flight_AirportRequest {
    String airportId;
    LocalDate departureDate;
    LocalDate arrivalDate;
    LocalTime departureTime;
    LocalTime arrivalTime;
    String note;

}

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
class Flight_SeatRequest {
    String seatId;
    Integer quantity;
    Integer remainingTickets;
}
