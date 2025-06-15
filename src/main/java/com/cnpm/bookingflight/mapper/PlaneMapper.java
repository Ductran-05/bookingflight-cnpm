package com.cnpm.bookingflight.mapper;

import com.cnpm.bookingflight.domain.Plane;
import com.cnpm.bookingflight.dto.request.PlaneRequest;
import com.cnpm.bookingflight.dto.response.PlaneResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.repository.AirlineRepository;
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
public class PlaneMapper {

    final AirlineRepository airlineRepository;
    final FlightRepository flightRepository;
    final Flight_SeatRepository flightSeatRepository;

    public Plane toPlane(PlaneRequest request) {
        return Plane.builder()
                .planeCode(request.getPlaneCode())
                .planeName(request.getPlaneName())
                .airline(airlineRepository.findById(request.getAirlineId())
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID)))
                .build();
    }

    public PlaneResponse toPlaneResponse(Plane plane) {
        boolean hasActiveOrSoldOutFlight = flightRepository.findAll()
                .stream()
                .filter(f -> f.getPlane().getId().equals(plane.getId()))
                .anyMatch(f -> {
                    LocalDate currentDate = LocalDate.now();
                    boolean isPastDeparture = currentDate.isAfter(f.getDepartureDate());
                    boolean hasRemainingTickets = flightSeatRepository.findByIdFlightId(f.getId())
                            .stream().anyMatch(fs -> fs.getRemainingTickets() > 0);
                    String status = isPastDeparture ? "Expired" : hasRemainingTickets ? "Active" : "Sold out";
                    return "Active".equals(status) || "Sold out".equals(status);
                });
        boolean canDelete = !hasActiveOrSoldOutFlight;
        return PlaneResponse.builder()
                .id(plane.getId())
                .planeCode(plane.getPlaneCode())
                .planeName(plane.getPlaneName())
                .airline(plane.getAirline())
                .canDelete(canDelete)
                .build();
    }
}