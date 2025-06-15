package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.City;
import com.cnpm.bookingflight.dto.request.CityRequest;
import com.cnpm.bookingflight.dto.response.CityResponse;
import com.cnpm.bookingflight.repository.AirportRepository;
import com.cnpm.bookingflight.repository.FlightRepository;
import com.cnpm.bookingflight.repository.Flight_SeatRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class CityMapper {
    final AirportRepository airportRepository;
    final FlightRepository flightRepository;
    final Flight_SeatRepository flightSeatRepository;

    public City toCity(CityRequest request) {
        return City.builder()
                .cityCode(request.getCityCode())
                .cityName(request.getCityName())
                .build();
    }

    public CityResponse toCityResponse(City city) {
        boolean hasActiveOrSoldOutFlight = airportRepository.findByCityId(city.getId())
                .stream()
                .flatMap(airport -> flightRepository.findAll().stream()
                        .filter(f -> f.getDepartureAirport().getId().equals(airport.getId()) || f.getArrivalAirport().getId().equals(airport.getId())))
                .anyMatch(f -> {
                    LocalDate currentDate = LocalDate.now();
                    boolean isPastDeparture = currentDate.isAfter(f.getDepartureDate());
                    boolean hasRemainingTickets = flightSeatRepository.findByIdFlightId(f.getId())
                            .stream().anyMatch(fs -> fs.getRemainingTickets() > 0);
                    String status = isPastDeparture ? "Expired" : hasRemainingTickets ? "Active" : "Sold out";
                    return "Active".equals(status) || "Sold out".equals(status);
                });
        boolean canDelete = !hasActiveOrSoldOutFlight;
        return CityResponse.builder()
                .id(city.getId())
                .cityCode(city.getCityCode())
                .cityName(city.getCityName())
                .canDelete(canDelete)
                .build();
    }
}