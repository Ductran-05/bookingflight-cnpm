package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.Airline;
import com.cnpm.bookingflight.dto.request.AirlineRequest;
import com.cnpm.bookingflight.dto.response.AirlineResponse;
import com.cnpm.bookingflight.repository.PlaneRepository;
import com.cnpm.bookingflight.repository.FlightRepository;
import com.cnpm.bookingflight.repository.Flight_SeatRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor

public class AirlineMapper {

    final PlaneRepository planeRepository;
    final FlightRepository flightRepository;
    final Flight_SeatRepository flightSeatRepository;

    public Airline toAirline(AirlineRequest request) {
        return Airline.builder()
                .airlineCode(request.getAirlineCode())
                .airlineName(request.getAirlineName())
                .logo(request.getLogo())
                .build();
    }

    public AirlineResponse toAirlineResponse(Airline airline) {
        boolean hasActiveOrSoldOutFlight = planeRepository.findByAirlineId(airline.getId())
                .stream()
                .flatMap(plane -> flightRepository.findAll().stream()
                        .filter(f -> f.getPlane().getId().equals(plane.getId())))
                .anyMatch(f -> {
                    LocalDate currentDate = LocalDate.now();
                    boolean isPastDeparture = currentDate.isAfter(f.getDepartureDate());
                    boolean hasRemainingTickets = flightSeatRepository.findByIdFlightId(f.getId())
                            .stream().anyMatch(fs -> fs.getRemainingTickets() > 0);
                    String status = isPastDeparture ? "Expired" : hasRemainingTickets ? "Active" : "Sold out";
                    return "Active".equals(status) || "Sold out".equals(status);
                });
        boolean canDelete = !hasActiveOrSoldOutFlight;
        return AirlineResponse.builder()
                .id(airline.getId())
                .airlineCode(airline.getAirlineCode())
                .airlineName(airline.getAirlineName())
                .logo(airline.getLogo())
                .canDelete(canDelete)
                .build();
    }
}