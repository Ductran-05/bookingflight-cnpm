package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.Airport;
import com.cnpm.bookingflight.dto.request.AirportRequest;
import com.cnpm.bookingflight.dto.response.AirportResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.repository.CityRepository;
import com.cnpm.bookingflight.repository.FlightRepository;
import com.cnpm.bookingflight.repository.Flight_AirportRepository;
import com.cnpm.bookingflight.repository.Flight_SeatRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class AirportMapper {
    final CityRepository cityRepository;
    final FlightRepository flightRepository;
    final Flight_AirportRepository flightAirportRepository;
    final Flight_SeatRepository flightSeatRepository;

    public Airport toAirport(AirportRequest request) {
        return Airport.builder()
                .airportCode(request.getAirportCode())
                .airportName(request.getAirportName())
                .city(cityRepository.findById(request.getCityId())
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID)))
                .build();
    }

    public Airport updateAirport(Airport airport, AirportRequest request) {
        Airport updatedAirport = this.toAirport(request);
        updatedAirport.setId(airport.getId());
        return updatedAirport;
    }

    public AirportResponse toAirportResponse(Airport airport) {
        boolean hasActiveOrSoldOutFlight = flightRepository.findAll()
                .stream()
                .filter(f -> f.getDepartureAirport().getId().equals(airport.getId()) || f.getArrivalAirport().getId().equals(airport.getId()))
                .anyMatch(f -> {
                    LocalDate currentDate = LocalDate.now();
                    boolean isPastDeparture = currentDate.isAfter(f.getDepartureDate());
                    boolean hasRemainingTickets = flightSeatRepository.findByIdFlightId(f.getId())
                            .stream().anyMatch(fs -> fs.getRemainingTickets() > 0);
                    String status = isPastDeparture ? "Expired" : hasRemainingTickets ? "Active" : "Sold out";
                    return "Active".equals(status) || "Sold out".equals(status);
                });
        boolean canDelete = !hasActiveOrSoldOutFlight;
        return AirportResponse.builder()
                .id(airport.getId())
                .airportCode(airport.getAirportCode())
                .airportName(airport.getAirportName())
                .city(airport.getCity())
                .canDelete(canDelete)
                .build();
    }
}